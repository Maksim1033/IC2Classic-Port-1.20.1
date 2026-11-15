package ic2.core.block.misc.base;

import com.google.common.collect.ImmutableMap;
import ic2.api.blocks.DyeableMap;
import ic2.api.blocks.PainterHelper;
import ic2.core.block.base.IAutoCreator;
import ic2.core.block.base.IToolProvider;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.rendering.block.base.WallModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class IC2WallBlock extends WallBlock implements IAutoCreator, ICustomBlockModel, PainterHelper.IPaintable, IRegistryProvider {
   private String textureName;
   private String textureFolder;
   private Block material;
   private DyeableMap colorMap;
   private DyeColor color;
   private ResourceLocation id;

   public IC2WallBlock(String name, Block material, String textureFolder, String textureName) {
      super(Properties.copy(material));
      this.id = GameData.checkPrefix(name, false);
      this.textureName = textureName;
      this.textureFolder = textureFolder;
      this.material = material;
   }

   public IC2WallBlock(String name, Block material, Properties props, String textureFolder, String textureName) {
      super(props);
      this.id = GameData.checkPrefix(name, false);
      this.textureName = textureName;
      this.textureFolder = textureFolder;
      this.material = material;
   }

   public IC2WallBlock(DyeableBlock material, String name, String texture, DyeableMap colorMap) {
      this(name + material.getColor().getName(), material, texture, material.getColor().getName());
      this.color = material.getColor();
      this.colorMap = colorMap;
      colorMap.addBlock(this, this.color);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   public static IC2WallBlock createCFoamWall(DyeableBlock block, DyeableMap colorMap) {
      return new IC2WallBlock.IC2LavaWallBlock(block, "cfoam_wall_", "cfoam/normal", colorMap);
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this, new net.minecraft.world.item.Item.Properties());
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         List<ItemStack> drops = new ArrayList<>();
         BlockEntity tile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
         ItemStack stack = new ItemStack(this);
         if (!stack.isEmpty()) {
            if (tile instanceof Nameable && ((Nameable)tile).hasCustomName()) {
               stack.setHoverName(((Nameable)tile).getCustomName());
            }

            drops.add(stack);
         }

         if (tile instanceof IDropProvider) {
            ((IDropProvider)tile).addDrops(drops);
         }

         return drops;
      }
   }

   public DyeableMap getColorMap() {
      return this.colorMap;
   }

   public DyeColor getColor() {
      return this.color;
   }

   @Override
   public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
      if (this.colorMap == null) {
         return false;
      } else {
         Block block = this.colorMap.getBlock(color);
         return block != null && world.setBlockAndUpdate(pos, PainterHelper.copyProperties(state, block.defaultBlockState()));
      }
   }

   @Override
   public DyeColor getColor(BlockState state) {
      return this.color;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new WallModel(state, this.textureFolder, this.textureName);
   }

   public static class IC2LavaWallBlock extends IC2WallBlock implements IDualLogged, IToolProvider {
      Tool tool = Tool.PICKAXE.withLevel(1);
      public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;
      private final Map<BlockState, VoxelShape> shapeByIndex;
      private final Map<BlockState, VoxelShape> collisionShapeByIndex;

      public IC2LavaWallBlock(String name, Block material, String textureFolder, String textureName) {
         super(name, material, textureFolder, textureName);
         this.registerDefaultState((BlockState)this.defaultBlockState().setValue(LAVA, false));
         this.shapeByIndex = this.makeShapes(4.0F, 3.0F, 16.0F, 0.0F, 14.0F, 16.0F);
         this.collisionShapeByIndex = this.makeShapes(4.0F, 3.0F, 24.0F, 0.0F, 24.0F, 24.0F);
      }

      public IC2LavaWallBlock(String name, Block material, Properties props, String textureFolder, String textureName) {
         super(name, material, props, textureFolder, textureName);
         this.registerDefaultState((BlockState)this.defaultBlockState().setValue(LAVA, false));
         this.shapeByIndex = this.makeShapes(4.0F, 3.0F, 16.0F, 0.0F, 14.0F, 16.0F);
         this.collisionShapeByIndex = this.makeShapes(4.0F, 3.0F, 24.0F, 0.0F, 24.0F, 24.0F);
      }

      public IC2LavaWallBlock(DyeableBlock material, String name, String texture, DyeableMap colorMap) {
         super(material, name, texture, colorMap);
         this.registerDefaultState((BlockState)this.defaultBlockState().setValue(LAVA, false));
         this.shapeByIndex = this.makeShapes(4.0F, 3.0F, 16.0F, 0.0F, 14.0F, 16.0F);
         this.collisionShapeByIndex = this.makeShapes(4.0F, 3.0F, 24.0F, 0.0F, 24.0F, 24.0F);
      }

      public IC2WallBlock.IC2LavaWallBlock withTool(Tool tool) {
         this.tool = tool;
         return this;
      }

      @Override
      public void registerTools() {
         this.tool.register(this);
      }

      protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
         builder.add(new Property[]{LAVA});
         super.createBlockStateDefinition(builder);
      }

      public FluidState getFluidState(BlockState state) {
         return IDualLogged.getFluidState(state);
      }

      public BlockState getStateForPlacement(BlockPlaceContext context) {
         BlockState state = super.getStateForPlacement(context);
         FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
         return (BlockState)state.setValue(LAVA, fluid.getType() == Fluids.LAVA);
      }

      public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
         return this.shapeByIndex.get(state);
      }

      public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
         return this.collisionShapeByIndex.get(state);
      }

      public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
         super.neighborChanged(state, world, pos, block, fromPos, isMoving);
         if ((Boolean)state.getValue(LAVA)) {
            world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
         }
      }

      private static VoxelShape applyWallShape(VoxelShape center, WallSide wallHeight, VoxelShape side, VoxelShape top) {
         if (wallHeight == WallSide.TALL) {
            return Shapes.or(center, top);
         } else {
            return wallHeight == WallSide.LOW ? Shapes.or(center, side) : center;
         }
      }

      private Map<BlockState, VoxelShape> makeShapes(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
         float f = 8.0F - minX;
         float f1 = 8.0F + minX;
         float f2 = 8.0F - minY;
         float f3 = 8.0F + minY;
         VoxelShape voxelShape_0 = Block.box((double)f, 0.0, (double)f, (double)f1, (double)minZ, (double)f1);
         VoxelShape voxelShape_1 = Block.box((double)f2, (double)maxX, 0.0, (double)f3, (double)maxY, (double)f3);
         VoxelShape voxelShape_2 = Block.box((double)f2, (double)maxX, (double)f2, (double)f3, (double)maxY, 16.0);
         VoxelShape voxelShape_3 = Block.box(0.0, (double)maxX, (double)f2, (double)f3, (double)maxY, (double)f3);
         VoxelShape voxelShape_4 = Block.box((double)f2, (double)maxX, (double)f2, 16.0, (double)maxY, (double)f3);
         VoxelShape voxelShape_5 = Block.box((double)f2, (double)maxX, 0.0, (double)f3, (double)maxZ, (double)f3);
         VoxelShape voxelShape_6 = Block.box((double)f2, (double)maxX, (double)f2, (double)f3, (double)maxZ, 16.0);
         VoxelShape voxelShape_7 = Block.box(0.0, (double)maxX, (double)f2, (double)f3, (double)maxZ, (double)f3);
         VoxelShape voxelShape_8 = Block.box((double)f2, (double)maxX, (double)f2, 16.0, (double)maxZ, (double)f3);
         com.google.common.collect.ImmutableMap.Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();

         for (Boolean up : UP.getPossibleValues()) {
            for (WallSide wallHeight_0 : EAST_WALL.getPossibleValues()) {
               for (WallSide wallHeight_1 : NORTH_WALL.getPossibleValues()) {
                  for (WallSide wallHeight_2 : WEST_WALL.getPossibleValues()) {
                     for (WallSide wallHeight_3 : SOUTH_WALL.getPossibleValues()) {
                        VoxelShape voxelShape_9 = Shapes.empty();
                        voxelShape_9 = applyWallShape(voxelShape_9, wallHeight_0, voxelShape_4, voxelShape_8);
                        voxelShape_9 = applyWallShape(voxelShape_9, wallHeight_2, voxelShape_3, voxelShape_7);
                        voxelShape_9 = applyWallShape(voxelShape_9, wallHeight_1, voxelShape_1, voxelShape_5);
                        voxelShape_9 = applyWallShape(voxelShape_9, wallHeight_3, voxelShape_2, voxelShape_6);
                        if (up) {
                           voxelShape_9 = Shapes.or(voxelShape_9, voxelShape_0);
                        }

                        BlockState blockstate = (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(UP, up))
                                    .setValue(EAST_WALL, wallHeight_0))
                                 .setValue(WEST_WALL, wallHeight_2))
                              .setValue(NORTH_WALL, wallHeight_1))
                           .setValue(SOUTH_WALL, wallHeight_3);

                        for (Boolean lava : LAVA.getPossibleValues()) {
                           builder.put((BlockState)((BlockState)blockstate.setValue(LAVA, lava)).setValue(WATERLOGGED, Boolean.FALSE), voxelShape_9);
                           builder.put((BlockState)((BlockState)blockstate.setValue(LAVA, lava)).setValue(WATERLOGGED, Boolean.TRUE), voxelShape_9);
                        }
                     }
                  }
               }
            }
         }

         return builder.build();
      }
   }
}
