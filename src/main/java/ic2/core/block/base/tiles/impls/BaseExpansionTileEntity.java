package ic2.core.block.base.tiles.impls;

import ic2.core.block.base.features.IWrenchRemovable;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.machines.logic.crafter.IWorkbenchExpansion;
import ic2.core.block.machines.logic.crafter.Snapshot;
import ic2.core.block.machines.tiles.nv.IndustrialWorkbenchTileEntity;
import ic2.core.utils.collection.CollectionUtils;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseExpansionTileEntity extends BaseTileEntity implements IWorkbenchExpansion, IWrenchRemovable {
   public Set<IndustrialWorkbenchTileEntity> unloadListener = CollectionUtils.<IndustrialWorkbenchTileEntity>createLinkedSet();
   public Snapshot snapshot = null;

   protected BaseExpansionTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   @Override
   public boolean addUnloadListener(IndustrialWorkbenchTileEntity tile) {
      return this.unloadListener.add(tile);
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (!this.isRendering()) {
         IWorkbenchExpansion.searchForBenches(this);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      for (IndustrialWorkbenchTileEntity tile : this.unloadListener) {
         if (!tile.isRemoved()) {
            tile.onExpansionUnloaded(this);
         }
      }

      this.unloadListener.clear();
      super.onUnloaded(chunk);
   }

   @Override
   public void destroySnapshot() {
      if (this.snapshot == null) {
         throw new IllegalStateException("Snapshot is missing");
      } else {
         this.snapshot = null;
      }
   }

   @Override
   public Snapshot takeSnapshot() {
      if (this.snapshot == null) {
         return null;
      } else {
         Snapshot shot = this.snapshot;
         this.snapshot = null;
         return shot;
      }
   }

   @Override
   public void setSnapshot(Snapshot shot) {
      if (this.snapshot != null) {
         throw new IllegalStateException("Snapshot already present");
      } else {
         this.snapshot = shot;
      }
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.85;
   }
}
