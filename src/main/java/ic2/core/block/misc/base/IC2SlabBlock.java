package ic2.core.block.misc.base;

import ic2.api.blocks.DyeableMap;
import ic2.api.blocks.PainterHelper;
import ic2.core.block.base.IAutoCreator;
import ic2.core.block.base.IToolProvider;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class IC2SlabBlock extends SlabBlock implements IBlockModel, IAutoCreator, IBlockModifiers, PainterHelper.IPaintable, IRegistryProvider {
   private static final AABB BOTTOM = new AABB(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
   private static final AABB TOP = new AABB(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
   private String textureName;
   private String textureFolder;
   private Block material;
   private DyeableMap colorMap;
   private DyeColor color;
   private ResourceLocation id;

   public IC2SlabBlock(String name, Block material, String textureFolder, String textureName) {
      super(Properties.copy(material));
      this.id = GameData.checkPrefix(name, false);
      this.textureName = textureName;
      this.textureFolder = textureFolder;
      this.material = material;
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
   }

   public IC2SlabBlock(String name, Block material, Properties props, String textureFolder, String textureName) {
      super(props);
      this.id = GameData.checkPrefix(name, false);
      this.textureName = textureName;
      this.textureFolder = textureFolder;
      this.material = material;
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
   }

   public IC2SlabBlock(DyeableBlock material, String name, String texture, DyeableMap colorMap) {
      this(name + material.getColor().getName(), material, texture, material.getColor().getName());
      this.color = material.getColor();
      this.colorMap = colorMap;
      colorMap.addBlock(this, this.color);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   public static IC2SlabBlock createCFoamSlab(DyeableBlock block, DyeableMap colorMap) {
      return new IC2SlabBlock.IC2LavaSlabBlock(block, "cfoam_slab_", "cfoam/normal", colorMap);
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this, new net.minecraft.world.item.Item.Properties());
   }

   @Override
   public List<BlockState> getModelStates() {
      return this.getStateDefinition().getPossibleStates();
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

   @Override
   public AABB getModelBounds(BlockState state) {
      switch ((SlabType)state.getValue(SlabBlock.TYPE)) {
         case BOTTOM:
            return BOTTOM;
         case TOP:
            return TOP;
         case DOUBLE:
            return DEFAULT_BOUNDS;
         default:
            return DEFAULT_BOUNDS;
      }
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2(this.textureFolder).get(this.textureName);
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
      return side.getAxis().isHorizontal() && state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE;
   }

   @Override
   public float[] getCustomTextureUVs(BlockState state, Direction side) {
      return state.getValue(SlabBlock.TYPE) == SlabType.TOP ? new float[]{0.0F, 8.0F, 16.0F, 16.0F} : new float[]{0.0F, 0.0F, 16.0F, 8.0F};
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      LootTable loottable = builder.getLevel().getServer().getLootData().getLootTable(this.getLootTable());
      if (loottable != LootTable.EMPTY) {
         return loottable.getRandomItems(builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK));
      } else {
         List<ItemStack> drops = new ArrayList<>();
         BlockEntity tile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
         ItemStack stack = new ItemStack(this, state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE ? 2 : 1);
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

   public static class IC2LavaSlabBlock extends IC2SlabBlock implements IDualLogged, IToolProvider {
      Tool tool = Tool.PICKAXE.withLevel(1);
      public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;

      public IC2LavaSlabBlock(String name, Block material, String textureFolder, String textureName) {
         super(name, material, textureFolder, textureName);
      }

      public IC2LavaSlabBlock(String name, Block material, Properties props, String textureFolder, String textureName) {
         super(name, material, props, textureFolder, textureName);
      }

      public IC2LavaSlabBlock(DyeableBlock material, String name, String texture, DyeableMap colorMap) {
         super(material, name, texture, colorMap);
      }

      public IC2SlabBlock.IC2LavaSlabBlock withTool(Tool tool) {
         this.tool = tool;
         return this;
      }

      @Override
      public void registerTools() {
         this.tool.register(this);
      }

      protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
         super.createBlockStateDefinition(builder);
         builder.add(new Property[]{LAVA});
      }

      @Override
      public boolean canPlaceLiquid(BlockGetter p_204510_1_, BlockPos p_204510_2_, BlockState p_204510_3_, Fluid p_204510_4_) {
         return p_204510_3_.getValue(TYPE) != SlabType.DOUBLE && IDualLogged.super.canPlaceLiquid(p_204510_1_, p_204510_2_, p_204510_3_, p_204510_4_);
      }

      @Override
      public boolean placeLiquid(LevelAccessor p_204509_1_, BlockPos p_204509_2_, BlockState p_204509_3_, FluidState p_204509_4_) {
         return IDualLogged.super.placeLiquid(p_204509_1_, p_204509_2_, p_204509_3_, p_204509_4_);
      }

      public FluidState getFluidState(BlockState state) {
         return IDualLogged.getFluidState(state);
      }

      public BlockState getStateForPlacement(BlockPlaceContext context) {
         BlockState state = super.getStateForPlacement(context);
         if (state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
            return state;
         } else {
            FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
            return (BlockState)state.setValue(LAVA, fluid.getType() == Fluids.LAVA);
         }
      }

      public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
         super.neighborChanged(state, world, pos, block, fromPos, isMoving);
         if ((Boolean)state.getValue(LAVA)) {
            world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
         }
      }
   }
}
