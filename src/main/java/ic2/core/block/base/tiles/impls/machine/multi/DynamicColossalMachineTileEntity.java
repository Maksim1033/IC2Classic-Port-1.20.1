package ic2.core.block.base.tiles.impls.machine.multi;

import ic2.api.network.buffer.NetworkInfo;
import ic2.core.block.base.features.IWrenchRemovable;
import ic2.core.block.base.features.multiblock.ITexturedMultiBlock;
import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.math.StructureBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public abstract class DynamicColossalMachineTileEntity extends BaseColossalMachineTileEntity implements ITexturedMultiBlock, IWrenchRemovable {
   @NetworkInfo
   protected int structureSize;

   public DynamicColossalMachineTileEntity(BlockPos pos, BlockState state, int energyPerTick, int progressPerTick, int maxProgress, int maxInput) {
      this(pos, state, 17, 8, energyPerTick, progressPerTick, maxProgress, maxInput);
   }

   public DynamicColossalMachineTileEntity(
      BlockPos pos, BlockState state, int size, int recipeSlots, int energyPerTick, int progressPerTick, int maxProgress, int maxInput
   ) {
      super(pos, state, size, recipeSlots, energyPerTick, maxProgress, 100000, maxInput);
      this.defaultProgressPerTick = (float)progressPerTick;
      this.progressPerTick = (float)progressPerTick;
      this.addNetworkFields(new String[]{"children", "structureSize"});
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putByte(compound, "structure_size", this.structureSize, 0);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.structureSize = compound.getInt("structure_size");
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.65;
   }

   @Override
   public int[] getAllSlots(boolean input) {
      return input ? new int[]{0, 1, 2, 3, 4, 5, 6, 7} : new int[]{8, 9, 10, 11, 12, 13, 14, 15};
   }

   @Override
   public int[] getOutputSlots(int recipeSlot) {
      return new int[]{recipeSlot + 8};
   }

   @Override
   public int getRecipeSlots(int slot, boolean input) {
      return input ? (slot >= 0 && slot < 8 ? slot : -1) : (slot >= 8 && slot <= 15 ? slot - 8 : -1);
   }

   @Override
   public int getInputSlot(int recipeSlot) {
      return recipeSlot;
   }

   @Override
   public int getStructureSize() {
      return this.structureSize;
   }

   @Override
   public void onStructureValidated(boolean revalidation) {
      super.onStructureValidated(revalidation);
      this.updateTileField("children");
   }

   @Override
   protected void onInitialValidation() {
      super.onInitialValidation();
      List<BaseLinkingTileEntity> tiles = this.children.getTiles();
      int i = 0;

      for (int m = tiles.size(); i < m; i++) {
         BaseLinkingTileEntity tile = tiles.get(i);
         tile.lock();
         tile.setState(this.structureSize, i + 1);
         tile.setFacing(this.getFacing());
         tile.unlock();
         tile.onStateChanged();
      }

      this.onStateChanged();
      switch (this.structureSize) {
         case 0:
            this.setSlotsInUse(2);
            break;
         case 1:
            this.setSlotsInUse(4);
            break;
         case 2:
            this.setSlotsInUse(8);
      }
   }

   @Override
   public boolean isStateStillValid(BlockPos actualPos, BlockPos structurePos, BlockState newState) {
      return newState.getBlock() == IC2Blocks.COLOSSAL_BASE;
   }

   @Override
   public boolean testStructure(StructureBuilder builder) {
      int radius = builder.findRadius(3, true, this::valid);
      if (radius <= 0) {
         return false;
      } else {
         for (int i = 1; builder.cubicStructure(i, radius + 1, radius + 1, radius + 1); i++) {
            if (!builder.isBlock(IC2Blocks.COLOSSAL_BASE)) {
               return false;
            }

            this.children.add(builder.getTile(BaseLinkingTileEntity.class));
         }

         this.structureSize = radius - 1;
         this.updateTileField("structureSize");
         return true;
      }
   }

   protected boolean valid(StructureBuilder builder) {
      if (!builder.isBlock(IC2Blocks.COLOSSAL_BASE)) {
         return false;
      } else {
         BaseLinkingTileEntity tile = builder.getTile(BaseLinkingTileEntity.class);
         return tile != null && tile.getMaster() == null;
      }
   }
}
