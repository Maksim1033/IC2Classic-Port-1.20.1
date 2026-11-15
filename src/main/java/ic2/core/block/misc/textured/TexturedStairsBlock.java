package ic2.core.block.misc.textured;

import ic2.api.blocks.PainterHelper;
import ic2.core.block.base.ICamouflageBlock;
import ic2.core.block.base.features.redstone.IRedstoneProvider;
import ic2.core.block.misc.base.IC2StairsBlock;
import ic2.core.block.misc.tiles.TexturedBlockTileEntity;
import ic2.core.block.rendering.block.CamouflageModel;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.models.BaseModel;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public class TexturedStairsBlock extends IC2StairsBlock.IC2LavaStairBlock implements ICamouflageBlock, PainterHelper.IPaintable, EntityBlock {
   public TexturedStairsBlock() {
      super("textured_stairs", Blocks.STONE, Properties.copy(Blocks.STONE).noOcclusion().isRedstoneConductor((T, V, X) -> false), "", "");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return CamouflageModel.getInstance();
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return IC2Tiles.TEXTURED_STAIRS.create(pos, state);
   }

   public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
      consumer.accept(new ICamouflageBlock.CamouflageWrapper(this));
   }

   @Override
   public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactPos, Direction dir, DyeColor color) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof TexturedBlockTileEntity ? ((TexturedBlockTileEntity)tile).setColor(dir, color, false) : false;
   }

   @Override
   public DyeColor getColor(BlockState state) {
      return null;
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this).disableCreative();
   }

   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
      return Collections.emptyList();
   }

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

   public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
      return true;
   }

   public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
      BlockEntity tile = blockAccess.getBlockEntity(pos);
      return tile instanceof IRedstoneProvider ? ((IRedstoneProvider)tile).getStrongSignalStrength(side) : 0;
   }

   public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
      BlockEntity tile = blockAccess.getBlockEntity(pos);
      return tile instanceof IRedstoneProvider ? ((IRedstoneProvider)tile).getWeakSignalStrength(side) : 0;
   }
}
