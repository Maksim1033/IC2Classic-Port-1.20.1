package ic2.core.block.misc.base;

import ic2.api.blocks.DyeableMap;
import ic2.api.blocks.PainterHelper;
import ic2.core.block.base.IAutoCreator;
import ic2.core.block.base.IC2Block;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.helpers.Tool;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2SurfaceBlock extends IC2Block implements IBlockModel, IAutoCreator, IBlockModifiers, PainterHelper.IPaintable {
   public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   private static final AABB BOTTOM = new AABB(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
   private static final AABB TOP = new AABB(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
   private static final VoxelShape BOTTOM_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
   private static final VoxelShape TOP_SHAPE = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
   private String textureName;
   private String textureFolder;
   private Block material;
   private Block doubleBlock;
   protected DyeableMap colorMap;
   private DyeColor color;

   public IC2SurfaceBlock(String name, Block material, String textureFolder, String textureName) {
      super(name, Properties.copy(material));
      this.textureName = textureName;
      this.textureFolder = textureFolder;
      this.material = material;
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(HALF, Half.BOTTOM));
   }

   public IC2SurfaceBlock(DyeableBlock material, String name, String texture, DyeableMap colorMap) {
      this(name + material.getColor().getName(), material, texture, material.getColor().getName());
      this.color = material.getColor();
      this.colorMap = colorMap;
      colorMap.addBlock(this, this.color);
   }

   public static IC2SurfaceBlock createCFoamTile(DyeableBlock block, DyeableMap colorMap) {
      return new IC2SurfaceBlock.IC2LavaSurfaceBlock(block, "cfoam_surface_", "cfoam/normal", colorMap);
   }

   public void setDouble(Block doubleBlock) {
      this.doubleBlock = doubleBlock;
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return state.getValue(HALF) == Half.TOP ? TOP_SHAPE : BOTTOM_SHAPE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2(this.textureFolder).get(this.textureName);
   }

   @Override
   public AABB getModelBounds(BlockState state) {
      return state.getValue(HALF) == Half.TOP ? TOP : BOTTOM;
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

   public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
      return this.doubleBlock != null
         && Block.byItem(context.getItemInHand().getItem()) instanceof IC2SurfaceBlock
         && context.getPlayer() != null
         && context.getPlayer().isShiftKeyDown();
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this, new net.minecraft.world.item.Item.Properties());
   }

   @Override
   public boolean hasTextureRotation(BlockState state, Direction side) {
      return false;
   }

   @Override
   public int getTextureRotation(BlockState state, Direction side) {
      return 0;
   }

   @Override
   public boolean hasCustomTextureUVs(BlockState state, Direction side) {
      return true;
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return false;
   }

   @Override
   public float[] getCustomTextureUVs(BlockState state, Direction side) {
      float[] output;
      if (Direction.UP == side | Direction.DOWN == side) {
         output = new float[]{1.0F, 1.0F, 14.0F, 14.0F};
      } else {
         output = side.getAxisDirection() == AxisDirection.POSITIVE ? new float[]{1.0F, 3.0F, 14.0F, 14.0F} : new float[]{1.0F, 1.0F, 14.0F, 3.0F};
      }

      return output;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{HALF}).add(new Property[]{WATER});
   }

   @Nonnull
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction direction = context.getClickedFace();
      BlockPos blockpos = context.getClickedPos();
      FluidState fluid = context.getLevel().getFluidState(blockpos);
      if (this.doubleBlock != null && context.isSecondaryUseActive()) {
         BlockState original = context.getLevel().getBlockState(blockpos);
         if (original.getBlock() instanceof IC2SurfaceBlock) {
            DyeColor orColor = ((IC2SurfaceBlock)original.getBlock()).color;
            boolean top = original.getValue(HALF) == Half.TOP;
            return (BlockState)((BlockState)((BlockState)this.doubleBlock
                     .defaultBlockState()
                     .setValue(IC2DoubleSurfaceBlock.MAIN_COLOR, top ? this.color : orColor))
                  .setValue(IC2DoubleSurfaceBlock.SECOND_COLOR, top ? orColor : this.color))
               .setValue(WATER, fluid.getType() == Fluids.WATER);
         }
      }

      return (BlockState)((BlockState)this.defaultBlockState()
            .setValue(
               HALF,
               direction == Direction.DOWN || direction != Direction.UP && context.getClickLocation().y - (double)blockpos.getY() > 0.5
                  ? Half.TOP
                  : Half.BOTTOM
            ))
         .setValue(WATER, fluid.getType() == Fluids.WATER);
   }

   public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if ((Boolean)state.getValue(WATER)) {
         world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
      }
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATER) ? Fluids.WATER.defaultFluidState() : Fluids.EMPTY.defaultFluidState();
   }

   public static class IC2LavaSurfaceBlock extends IC2SurfaceBlock implements IDualLogged {
      public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;

      public IC2LavaSurfaceBlock(String name, Block material, String textureFolder, String textureName) {
         super(name, material, textureFolder, textureName);
         this.setHarvestTool(Tool.PICKAXE.withLevel(1));
      }

      public IC2LavaSurfaceBlock(DyeableBlock material, String name, String texture, DyeableMap colorMap) {
         super(material, name, texture, colorMap);
         this.setHarvestTool(Tool.PICKAXE.withLevel(1));
      }

      @Override
      protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
         super.createBlockStateDefinition(builder);
         builder.add(new Property[]{LAVA});
      }

      @Override
      public FluidState getFluidState(BlockState state) {
         return IDualLogged.getFluidState(state);
      }

      @Nonnull
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext context) {
         FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
         return (BlockState)super.getStateForPlacement(context).setValue(LAVA, fluid.getType() == Fluids.LAVA);
      }

      @Override
      public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
         super.neighborChanged(state, world, pos, block, fromPos, isMoving);
         if ((Boolean)state.getValue(LAVA)) {
            world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
         }
      }
   }
}
