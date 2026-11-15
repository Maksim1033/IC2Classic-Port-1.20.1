package ic2.core.block.misc.base;

import ic2.api.blocks.DyeableMap;
import ic2.api.blocks.PainterHelper;
import ic2.core.IC2;
import ic2.core.block.base.IC2Block;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DyeableBlock extends IC2Block implements IBlockModel, PainterHelper.IPaintable {
   public static final BooleanProperty LIGHT = IC2Properties.LIGHT;
   public static final BooleanProperty REDSTONE = IC2Properties.REDSTONE;
   private DyeColor color;
   private DyeableMap colorMap;
   private String textureFolder;
   private CreativeModeTab group = IC2.CFOAM_GROUP;

   public DyeableBlock(String blockName, String textureFolder, DyeColor color, Properties properties) {
      super(blockName + color.getName(), properties.lightLevel(T -> T.hasProperty(LIGHT) && T.getValue(LIGHT) ? 15 : 0));
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(LIGHT, false)).setValue(REDSTONE, false));
      this.setHarvestTool(Tool.PICKAXE);
      this.textureFolder = textureFolder;
      this.color = color;
   }

   public static DyeableBlock createCFoamBlock(DyeColor color, DyeableMap colorMap) {
      return new DyeableBlock("cfoam_block_", "cfoam/normal", color, Block.Properties.of().strength(3.0F, 30.0F).sound(SoundType.STONE))
         .setColorGroup(colorMap);
   }

   public static DyeableBlock createCFoamWoolBlock(DyeColor color, DyeableMap colorMap) {
      return new DyeableBlock("cfoam_wool_", "cfoam/wool", color, Block.Properties.of().strength(3.0F, 30.0F).sound(SoundType.WOOL))
         .setColorGroup(colorMap);
   }

   public DyeableBlock setColorGroup(DyeableMap colorMap) {
      this.colorMap = colorMap;
      colorMap.addBlock(this, this.color);
      return this;
   }

   public DyeableBlock setGroup(CreativeModeTab group) {
      this.group = group;
      return this;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{LIGHT, REDSTONE});
   }

   public DyeColor getColor() {
      return this.color;
   }

   public DyeableMap getColorMap() {
      return this.colorMap;
   }

   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(handIn);
      if (IC2.KEYBOARD.isAltKeyDown(player)) {
         if (stack.getItem() == Items.REDSTONE) {
            worldIn.setBlockAndUpdate(pos, (BlockState)state.setValue(REDSTONE, !(Boolean)state.getValue(REDSTONE)));
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            if (IC2.PLATFORM.isSimulating()) {
               player.displayClientMessage(
                  this.translate(!state.getValue(REDSTONE) ? "tooltip.block.ic2.cfoam.redstone.enable" : "tooltip.block.ic2.cfoam.redstone.disable"), true
               );
            }

            return InteractionResult.SUCCESS;
         }

         if (stack.getItem() == Items.GLOWSTONE_DUST) {
            worldIn.setBlockAndUpdate(pos, (BlockState)state.setValue(LIGHT, !(Boolean)state.getValue(LIGHT)));
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            if (IC2.PLATFORM.isSimulating()) {
               player.displayClientMessage(
                  this.translate(!state.getValue(LIGHT) ? "tooltip.block.ic2.cfoam.light.enable" : "tooltip.block.ic2.cfoam.light.disable"), true
               );
            }

            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
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
   public BlockItem createItem() {
      return new IC2BlockItem(this, new net.minecraft.world.item.Item.Properties());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2(this.textureFolder).get(this.color.getName());
   }

   public int getDirectSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
      return state.getValue(REDSTONE) ? 15 : 0;
   }

   public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
      return state.getValue(REDSTONE) ? 15 : 0;
   }

   public boolean shouldCheckWeakPower(BlockState state, LevelReader world, BlockPos pos, Direction side) {
      return (Boolean)state.getValue(REDSTONE);
   }

   public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return (Boolean)state.getValue(REDSTONE);
   }
}
