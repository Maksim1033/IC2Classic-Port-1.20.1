package ic2.core.block.machines.logic.miner;

import ic2.core.block.machines.tiles.lv.MinerTileEntity;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.fluids.FluidStack;

public class FluidMiningTarget implements IMiningTarget {
   BlockPos pos;
   BlockState state;
   FluidStack cache = null;

   public FluidMiningTarget(CompoundTag nbt) {
      this(BlockPos.of(nbt.getLong("position")), NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt));
   }

   public FluidMiningTarget(BlockPos pos, BlockState state) {
      this.pos = pos;
      this.state = state;
   }

   @Override
   public BlockPos getPos() {
      return this.pos;
   }

   @Override
   public BlockState getState() {
      return this.state;
   }

   @Override
   public boolean canMine(MinerTileEntity miner, boolean finale) {
      if (finale) {
         this.state = miner.getLevel().getBlockState(this.pos);
         this.cache = null;
      }

      try {
         if (!miner.hasPumps()) {
            return false;
         } else {
            return miner.canMine(this.pos, this.state, miner.hasPumps()) ? miner.pumpFluid(this.getCache(), false) != InteractionResult.FAIL : false;
         }
      } catch (Exception var4) {
         this.state = miner.getLevel().getBlockState(this.pos);
         this.cache = null;
         return this.canMine(miner, false);
      }
   }

   @Override
   public boolean canContinue(MinerTileEntity miner) {
      return !miner.hasPumps() ? true : miner.pumpFluid(this.getCache(), false) == InteractionResult.SUCCESS;
   }

   @Override
   public List<ItemStack> createDrops(MinerTileEntity miner) {
      if (miner.hasPumps()) {
         miner.pumpFluid(this.getCache(), true);
      }

      miner.getLevel().setBlock(this.pos, Blocks.AIR.defaultBlockState(), 11);
      ((ServerLevel)miner.getLevel()).getFluidTicks().clearArea(BoundingBox.fromCorners(this.pos.offset(-2, -2, -2), this.pos.offset(2, 2, 2)));
      return Collections.emptyList();
   }

   @Override
   public CompoundTag save() {
      CompoundTag nbt = NbtUtils.writeBlockState(this.state);
      nbt.putLong("position", this.pos.asLong());
      return nbt;
   }

   @Override
   public byte getID() {
      return 1;
   }

   public FluidStack getCache() {
      if (this.cache == null) {
         this.cache = new FluidStack(this.state.getFluidState().getType(), 1000);
      }

      return this.cache;
   }
}
