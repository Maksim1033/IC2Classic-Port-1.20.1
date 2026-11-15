package ic2.core.block.machines.logic.miner;

import ic2.core.block.machines.tiles.lv.MinerTileEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface IMiningTarget {
   BlockPos getPos();

   BlockState getState();

   default boolean isValid(MinerTileEntity miner) {
      return this.getPos().getY() >= miner.getLevel().getMinBuildHeight() && this.getState() == miner.getLevel().getBlockState(this.getPos());
   }

   boolean canMine(MinerTileEntity var1, boolean var2);

   boolean canContinue(MinerTileEntity var1);

   List<ItemStack> createDrops(MinerTileEntity var1);

   CompoundTag save();

   byte getID();
}
