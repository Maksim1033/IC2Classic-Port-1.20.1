package ic2.core.block.base.tiles;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.redstone.IComparable;
import ic2.core.block.base.misc.comparator.ComparatorManager;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.capabilities.CapabilityWrapperCache;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class BaseLinkingTileEntity extends BaseTileEntity implements ITileActivityProvider {
   @NetworkInfo
   int state;
   @NetworkInfo
   BlockPos linkedTile = BlockPos.ZERO;
   BlockEntity reference;
   protected CapabilityWrapperCache wrapper = new CapabilityWrapperCache();

   public BaseLinkingTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"state", "linkedTile"});
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putShort(compound, "state", this.state, 0);
      NBTUtils.putLong(compound, "linked", this.linkedTile.asLong(), 0L);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.state = compound.getInt("state");
      this.linkedTile = BlockPos.of(compound.getLong("linked"));
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("state") || fields.contains("linkedTile")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.LINKING_TILE;
   }

   public BlockEntity getMaster() {
      if (this.linkedTile.equals(BlockPos.ZERO)) {
         return null;
      } else {
         if (this.reference == null || this.reference.isRemoved()) {
            this.reference = this.getLevel().getBlockEntity(this.linkedTile);
            this.wrapper.setOwner(this.reference);
         }

         return this.reference;
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating()) {
         this.wrapper.clearOwner();
      }

      super.onUnloaded(chunk);
   }

   public boolean clearMaster(BlockPos origin) {
      if (this.linkedTile != BlockPos.ZERO && this.linkedTile.equals(origin)) {
         this.updateTileField("linkedTile");
         this.linkedTile = BlockPos.ZERO;
         this.reference = null;
         this.wrapper.clearOwner();
         if (!this.isRemoved()) {
            this.setActive(false);
            this.setState(0);
         }

         this.notifyChanges(false, DirectionList.ALL);
         return true;
      } else {
         return false;
      }
   }

   public void setMaster(BlockEntity tile) {
      if (tile != null) {
         this.reference = tile;
         this.linkedTile = tile.getBlockPos();
         this.wrapper.setOwner(tile);
         this.updateTileField("linkedTile");
         this.notifyChanges(false, DirectionList.ALL);
      }
   }

   public BaseLinkingTileEntity setState(int state) {
      if (this.state != state) {
         this.state = state;
         this.updateTileField("state");
         this.onStateChanged();
      }

      return this;
   }

   public BaseLinkingTileEntity setState(int range, int state) {
      int value = state << 2 | range & 3;
      if (this.state != value) {
         this.state = value;
         this.updateTileField("state");
         this.onStateChanged();
      }

      return this;
   }

   public int getState() {
      return this.state;
   }

   protected boolean isTile(BlockEntity tile) {
      BlockEntity master = this.getMaster();
      if (tile == master || master == null) {
         return false;
      } else {
         return tile instanceof BaseLinkingTileEntity && master == ((BaseLinkingTileEntity)tile).getMaster()
            ? false
            : !((BaseMultiBlockTileEntity)master).getBox().intersectsWith(tile.getBlockPos());
      }
   }

   @Override
   public ComparatorManager getManager() {
      BlockEntity master = this.getMaster();
      return master instanceof IComparable ? ((IComparable)master).getManager() : null;
   }

   @Override
   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (this.reference == null && !this.linkedTile.equals(BlockPos.ZERO)) {
         this.getMaster();
      }

      return this.wrapper.getCapability(cap, side);
   }
}
