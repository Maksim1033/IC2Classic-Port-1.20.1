package ic2.core.block.machines.customBlocks;

import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.block.machines.tiles.hv.RocketMinerTileEntity;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RocketMinerBlock extends BaseMachineBlock {
   public RocketMinerBlock(String blockName, IBlockDropProvider drop, ITextureProvider provider, BlockEntityType<? extends BaseTileEntity> type) {
      super(blockName, drop, provider, type);
   }

   @Override
   protected void setPlaceData(BaseTileEntity tile, BlockState state, LivingEntity placer, ItemStack stack) {
      if (tile instanceof RocketMinerTileEntity rocket && placer instanceof Player) {
         rocket.owner = placer.getUUID();
      }
   }
}
