package ic2.core.block.misc.base;

import ic2.api.blocks.DyeableMap;
import ic2.api.blocks.PainterHelper;
import ic2.core.block.base.IAutoCreator;
import ic2.core.block.base.IToolProvider;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.rendering.block.base.StairsModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class IC2StairsBlock extends StairBlock implements ICustomBlockModel, IAutoCreator, PainterHelper.IPaintable, IRegistryProvider {
   private String textureName;
   private String textureFolder;
   private Block material;
   private DyeableMap colorMap;
   private DyeColor color;
   private ResourceLocation id;

   public IC2StairsBlock(String name, Block material, String textureFolder, String textureName) {
      super(material.defaultBlockState(), Properties.copy(material));
      this.id = GameData.checkPrefix(name, false);
      this.textureName = textureName;
      this.textureFolder = textureFolder;
      this.material = material;
   }

   public IC2StairsBlock(String name, Block material, Properties props, String textureFolder, String textureName) {
      super(material.defaultBlockState(), props);
      this.id = GameData.checkPrefix(name, false);
      this.textureName = textureName;
      this.textureFolder = textureFolder;
      this.material = material;
   }

   public IC2StairsBlock(DyeableBlock material, String name, String texture, DyeableMap colorMap) {
      this(name + material.getColor().getName(), material, texture, material.getColor().getName());
      this.color = material.getColor();
      this.colorMap = colorMap;
      colorMap.addBlock(this, this.color);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   public static IC2StairsBlock createCFoamStairs(DyeableBlock material, DyeableMap colorMap) {
      return new IC2StairsBlock.IC2LavaStairBlock(material, "cfoam_stairs_", "cfoam/normal", colorMap);
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

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new StairsModel(state, this.textureFolder, this.textureName);
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this, new net.minecraft.world.item.Item.Properties());
   }

   public static class IC2LavaStairBlock extends IC2StairsBlock implements IDualLogged, IToolProvider {
      public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;
      Tool tool = Tool.PICKAXE.withLevel(1);

      public IC2LavaStairBlock(String name, Block material, String textureFolder, String textureName) {
         super(name, material, textureFolder, textureName);
         this.registerDefaultState((BlockState)this.defaultBlockState().setValue(LAVA, false));
      }

      public IC2LavaStairBlock(String name, Block material, Properties props, String textureFolder, String textureName) {
         super(name, material, props, textureFolder, textureName);
         this.registerDefaultState((BlockState)this.defaultBlockState().setValue(LAVA, false));
      }

      public IC2LavaStairBlock(DyeableBlock material, String name, String texture, DyeableMap colorMap) {
         super(material, name, texture, colorMap);
         this.registerDefaultState((BlockState)this.defaultBlockState().setValue(LAVA, false));
      }

      public IC2StairsBlock.IC2LavaStairBlock withTool(Tool tool) {
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

      public FluidState getFluidState(BlockState state) {
         return IDualLogged.getFluidState(state);
      }

      public BlockState getStateForPlacement(BlockPlaceContext context) {
         BlockState state = super.getStateForPlacement(context);
         FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
         return (BlockState)state.setValue(LAVA, fluid.getType() == Fluids.LAVA);
      }

      public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
         super.neighborChanged(state, world, pos, block, fromPos, isMoving);
         if ((Boolean)state.getValue(LAVA)) {
            world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
         }
      }
   }
}
