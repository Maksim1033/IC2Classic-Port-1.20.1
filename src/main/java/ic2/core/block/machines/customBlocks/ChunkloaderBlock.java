package ic2.core.block.machines.customBlocks;

import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.block.machines.tiles.mv.ChunkloaderTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ChunkloaderBlock extends BaseMachineBlock {
   public ChunkloaderBlock() {
      super("chunkloader", IBlockDropProvider.SELF_OR_ADV_MACHINE, ITextureProvider.toggleIC2("machine/mv/chunkloader"), IC2Tiles.CHUNKLOADER);
   }

   @Override
   protected void setPlaceData(BaseTileEntity tile, BlockState state, LivingEntity placer, ItemStack stack) {
      if (tile instanceof ChunkloaderTileEntity && placer instanceof Player) {
         ((ChunkloaderTileEntity)tile).setOwner(placer.getUUID());
      }
   }
}
