package ic2.core.block.misc.textured;

import ic2.api.blocks.PainterHelper;
import ic2.core.IC2;
import ic2.core.block.base.IC2ContainerBlock;
import ic2.core.block.base.ICamouflageBlock;
import ic2.core.block.misc.tiles.TexturedBlockTileEntity;
import ic2.core.block.rendering.block.CamouflageModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public class TexturedBlockBlock extends IC2ContainerBlock implements ICustomBlockModel, ICamouflageBlock, PainterHelper.IPaintable {
   public static final BooleanProperty LIGHT = IC2Properties.LIGHT;
   public static final BooleanProperty REDSTONE = IC2Properties.REDSTONE;

   public TexturedBlockBlock() {
      super(
         "textured_block",
         Properties.of()
            .noOcclusion()
            .isRedstoneConductor((T, V, X) -> false)
            .lightLevel(T -> T.getValue(LIGHT) ? 15 : 0)
            .strength(3.0F, 30.0F)
            .sound(SoundType.STONE)
      );
      this.setHarvestTool(Tool.PICKAXE);
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(LIGHT, false)).setValue(REDSTONE, false));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{LIGHT, REDSTONE});
   }

   public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
      consumer.accept(new ICamouflageBlock.CamouflageWrapper(this));
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this).disableCreative();
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile, boolean wrench) {
      return ItemStack.EMPTY;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return IC2Tiles.TEXTURED_BLOCK.create(pos, state);
   }

   @Override
   public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof TexturedBlockTileEntity ? ((TexturedBlockTileEntity)tile).setColor(dir, color, false) : false;
   }

   @Override
   public DyeColor getColor(BlockState state) {
      return null;
   }

   @Override
   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      BlockEntity tile = world.getBlockEntity(pos);
      if (tile instanceof TexturedBlockTileEntity && target instanceof BlockHitResult) {
         BlockState resultState = ((TexturedBlockTileEntity)tile).getStorage().getCache(((BlockHitResult)target).getDirection()).getDisplayBlock();
         if (resultState != null) {
            try {
               return resultState.getCloneItemStack(target, world, pos, player);
            } catch (Exception var9) {
            }
         }
      }

      return super.getCloneItemStack(state, target, world, pos, player);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return CamouflageModel.getInstance();
   }

   public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
      return true;
   }

   @Override
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

      return super.use(state, worldIn, pos, player, handIn, hit);
   }

   @Override
   public int getDirectSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
      return state.getValue(REDSTONE) ? 15 : 0;
   }

   @Override
   public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
      return state.getValue(REDSTONE) ? 15 : 0;
   }

   @Override
   public boolean shouldCheckWeakPower(BlockState state, LevelReader world, BlockPos pos, Direction side) {
      return (Boolean)state.getValue(REDSTONE);
   }

   @Override
   public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return (Boolean)state.getValue(REDSTONE);
   }
}
