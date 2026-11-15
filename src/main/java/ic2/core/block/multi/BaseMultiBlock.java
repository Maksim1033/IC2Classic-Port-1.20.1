package ic2.core.block.multi;

import ic2.core.block.base.blocks.BaseActivityBlock;
import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.HitResult;

public class BaseMultiBlock extends BaseActivityBlock<BaseLinkingTileEntity> {
   public BaseMultiBlock(String name, ITextureProvider provider, BlockEntityType<? extends BlockEntity> tile) {
      this(name, provider, BaseMachineBlock.BASE_MACHINE, tile);
   }

   public BaseMultiBlock(String blockName, ITextureProvider provider, Properties properties, BlockEntityType<? extends BlockEntity> tile) {
      super(blockName, properties, provider, tile);
   }

   @Override
   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
      if (!player.isShiftKeyDown()) {
         BlockEntity tile = world.getBlockEntity(pos);
         if (tile instanceof BaseLinkingTileEntity) {
            tile = ((BaseLinkingTileEntity)tile).getMaster();
            if (tile != null) {
               return tile.getBlockState().getCloneItemStack(target, world, tile.getBlockPos(), player);
            }
         }
      }

      return super.getCloneItemStack(state, target, world, pos, player);
   }
}
