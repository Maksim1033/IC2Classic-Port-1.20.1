package ic2.core.block.machines.logic.miner;

import ic2.core.block.machines.tiles.lv.MinerTileEntity;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.common.util.FakePlayerFactory;

public class BlockMiningTarget implements IMiningTarget {
   BlockPos pos;
   BlockState state;

   public BlockMiningTarget(CompoundTag nbt) {
      this(BlockPos.of(nbt.getLong("position")), NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt));
   }

   public BlockMiningTarget(BlockPos pos, BlockState state) {
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
      }

      try {
         return miner.canMine(this.pos, this.state, miner.hasPumps());
      } catch (Exception var4) {
         this.state = miner.getLevel().getBlockState(this.pos);
         return this.canMine(miner, false);
      }
   }

   @Override
   public boolean canContinue(MinerTileEntity miner) {
      return true;
   }

   @Override
   public List<ItemStack> createDrops(MinerTileEntity miner) {
      this.state = miner.getLevel().getBlockState(this.pos);
      List<ItemStack> drops = CollectionUtils.<ItemStack>createList();
      drops.addAll(
         Block.getDrops(
            this.state,
            (ServerLevel)miner.getLevel(),
            this.pos,
            miner.getLevel().getBlockEntity(this.pos),
            FakePlayerFactory.getMinecraft((ServerLevel)miner.getLevel()),
            miner.getStackInSlot(miner.getDrillSlot()).copy()
         )
      );
      miner.getLevel().setBlock(this.pos, Blocks.AIR.defaultBlockState(), 3);
      ((ServerLevel)miner.getLevel()).getFluidTicks().clearArea(BoundingBox.fromCorners(this.pos.offset(-2, -2, -2), this.pos.offset(2, 2, 2)));
      return drops;
   }

   @Override
   public CompoundTag save() {
      CompoundTag nbt = NbtUtils.writeBlockState(this.state);
      nbt.putLong("position", this.pos.asLong());
      return nbt;
   }

   @Override
   public byte getID() {
      return 0;
   }
}
