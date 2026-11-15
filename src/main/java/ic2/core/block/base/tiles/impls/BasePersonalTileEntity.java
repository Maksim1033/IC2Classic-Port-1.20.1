package ic2.core.block.base.tiles.impls;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkClientEventListener;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.personal.IPersonalTile;
import ic2.core.block.base.features.personal.IPersonalWrenchable;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.inventory.base.ITileGui;
import ic2.core.platform.player.friends.Action;
import ic2.core.platform.player.friends.Friend;
import ic2.core.platform.player.friends.FriendManager;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.capabilities.CapabilityWrapperCache;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public abstract class BasePersonalTileEntity extends BaseTileEntity implements IPersonalTile, IPersonalWrenchable, ITileGui, INetworkClientEventListener {
   @NetworkInfo
   UUID owner;
   @NetworkInfo
   public int mode = 1;
   Map<Capability<?>, LazyOptional<?>> privateCapability = CollectionUtils.<Capability<?>, LazyOptional<?>>createMap();
   CapabilityWrapperCache publicCache = new CapabilityWrapperCache();

   public BasePersonalTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"owner", "mode"});
   }

   public <T> void addPersonalCapability(Capability<T> cap, T value) {
      this.privateCapability.put(cap, LazyOptional.of(() -> value));
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      this.writeItemData(compound);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.readItemData(compound, false);
   }

   public void readItemData(CompoundTag nbt, boolean place) {
      this.owner = nbt.hasUUID("owner") ? nbt.getUUID("owner") : null;
      this.mode = nbt.getInt("mode");
      if (this.mode == 0) {
         this.publicCache.setOwner(new BasePersonalTileEntity.PublicWrapper());
      }

      if (place) {
         this.updateTileFields(new String[]{"owner", "mode"});
      }
   }

   public CompoundTag writeItemData(CompoundTag nbt) {
      if (this.owner != null) {
         nbt.putUUID("owner", this.owner);
      }

      NBTUtils.putByte(nbt, "mode", (byte)this.mode, 0);
      return nbt;
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      if (key == 0) {
         if (entity.getUUID().equals(this.owner)) {
            this.mode = (this.mode + 1) % 3;
            this.updateTileField("mode");
            if (this.mode == 0 && !this.publicCache.hasOwner()) {
               this.publicCache.setOwner(new BasePersonalTileEntity.PublicWrapper());
            } else if (this.publicCache.hasOwner()) {
               this.publicCache.clearOwner();
            }

            this.notifyChanges(true, DirectionList.ALL);
         } else {
            entity.displayClientMessage(this.translate("gui.ic2.personal.perm"), false);
         }
      }

      if (key == 1 && this.canDoAction(entity.getUUID(), Action.PERSONAL_BLOCKS_ADMIN, false)) {
         this.voidStorage();
      }
   }

   public void voidStorage() {
   }

   @Override
   public void onUnloaded(boolean chunk) {
      for (LazyOptional<?> entry : this.privateCapability.values()) {
         entry.invalidate();
      }

      this.publicCache.clearOwner();
      this.privateCapability.clear();
      super.onUnloaded(chunk);
   }

   @Override
   public <T> LazyOptional<T> getPersonalCapability(UUID requester, Capability<T> cap, Direction dir) {
      if (this.canDoAction(requester, Action.PERSONAL_BLOCKS_ACCESS, false)) {
         LazyOptional<?> cached = this.privateCapability.get(cap);
         if (cached != null) {
            return cached.cast();
         }
      }

      return LazyOptional.empty();
   }

   @Override
   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (this.isPublic()) {
         LazyOptional<?> casted = this.publicCache.getCapability(cap, side);
         if (casted.isPresent()) {
            return casted.cast();
         }
      }

      return super.getCapability(cap, side);
   }

   @Override
   public void setOwner(UUID owner) {
      if (this.owner == null && owner != null) {
         this.owner = owner;
      }
   }

   @Override
   public UUID getOwner() {
      return this.owner;
   }

   @Override
   public boolean canDoAction(UUID user, Action action, boolean ignorePublic) {
      if (this.owner == null) {
         this.setOwner(user);
         return true;
      } else if (this.owner.equals(user) || !ignorePublic && this.mode == 0 || IC2.PLATFORM.isOp(user)) {
         return true;
      } else if (this.mode == 2) {
         return false;
      } else {
         Friend friend = FriendManager.getFriends().getFriend(this.owner, user);
         return friend != null && friend.canApplyAction(action);
      }
   }

   public boolean isPublic() {
      return this.mode == 0;
   }

   @Override
   public boolean hasGui(Player player, InteractionHand hand, Direction side) {
      return this.canDoAction(player.getUUID(), Action.PERSONAL_BLOCKS_ACCESS, false);
   }

   @Override
   public boolean canInteractWith(Player player) {
      return !this.isRemoved() && this.canDoAction(player.getUUID(), Action.PERSONAL_BLOCKS_ACCESS, false);
   }

   @Override
   public boolean canSetFacing(Direction dir, Player player) {
      return dir != this.getFacing() && dir.getAxis().isHorizontal() && this.canDoAction(player.getUUID(), Action.PERSONAL_BLOCKS_EDIT, false);
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return this.canDoAction(player.getUUID(), Action.PERSONAL_BLOCKS_ADMIN, false);
   }

   @Override
   public double getDropRate(Player player) {
      return 1.0;
   }

   private class PublicWrapper implements ICapabilityProvider {
      public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
         LazyOptional<?> optional = BasePersonalTileEntity.this.privateCapability.get(cap);
         return optional == null ? LazyOptional.empty() : optional.cast();
      }
   }
}
