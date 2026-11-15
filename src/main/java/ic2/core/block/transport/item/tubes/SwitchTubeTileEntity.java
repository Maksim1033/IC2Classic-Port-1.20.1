package ic2.core.block.transport.item.tubes;

import ic2.api.tiles.tubes.ITube;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.block.transport.item.logistic.TubeNet;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SwitchTubeTileEntity extends TubeTileEntity implements ITileActivityProvider {
   public SwitchTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"isActive"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.SWITCH_TUBE;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.self.setActiveDirections(this.isActive() ? DirectionList.EMPTY : DirectionList.ALL.remove(DirectionList.ofNumber(this.anchors)));
   }

   @Override
   public void onTubeUpdate() {
      this.handleRedstone();
      if (this.setActive(this.isRedstonePowered())) {
         this.self.setActiveDirections(this.isActive() ? DirectionList.EMPTY : DirectionList.ALL.remove(DirectionList.ofNumber(this.anchors)));
         this.notifyChanges(true, DirectionList.ALL);
         TubeNet.INSTANCE.updateTube(this);
      }
   }

   @Override
   public boolean canConnect(ITube other, Direction dir) {
      return !this.isActive() && super.canConnect(other, dir);
   }

   @Override
   public boolean canAddItem(TransportedItem item, Direction side) {
      return !this.isActive();
   }

   @Override
   protected void onCachesUpdated() {
      if (this.isActive()) {
         this.connectivity = 0;
         this.updateTileField("connectivity");
      } else {
         super.onCachesUpdated();
      }
   }
}
