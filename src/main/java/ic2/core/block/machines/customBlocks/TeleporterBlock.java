package ic2.core.block.machines.customBlocks;

import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

public class TeleporterBlock extends BaseMachineBlock {
   public TeleporterBlock() {
      super("teleporter", IBlockDropProvider.SELF_OR_ADV_MACHINE, ITextureProvider.toggleIC2("machine/hv/teleporter"), IC2Tiles.TELEPORTER);
   }

   @Override
   protected void setDefaultState() {
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.UP)).setValue(ACTIVE, false));
   }

   @Override
   protected Direction getFacing(BlockPlaceContext context) {
      return Direction.UP;
   }
}
