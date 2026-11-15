package ic2.core.block.cables;

import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedComparatorTileEntity extends BlockEntity {
   public BlockState defaultState = null;
   public int output;

   public AdvancedComparatorTileEntity(BlockPos pos, BlockState state) {
      super(IC2Tiles.ADVANCED_COMPARATOR, pos, state);
   }

   public int getOutputSignal() {
      return this.output;
   }

   public void setOutputSignal(int value) {
      this.output = value;
   }

   public void load(CompoundTag compound) {
      super.load(compound);
      this.output = compound.getInt("output");
   }

   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putByte(compound, "output", this.output, 0);
   }

   public BlockState getBlockState() {
      return this.defaultState != null ? this.defaultState : super.getBlockState();
   }
}
