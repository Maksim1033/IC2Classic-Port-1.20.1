package ic2.core.block.misc.tiles;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkFieldProvider;
import ic2.core.IC2;
import ic2.core.block.base.features.personal.IPersonalTile;
import ic2.core.platform.player.friends.Action;
import ic2.core.platform.player.friends.Friend;
import ic2.core.platform.player.friends.FriendManager;
import ic2.core.platform.registries.IC2Tiles;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IridiumStoneTileEntity extends BlockEntity implements IPersonalTile, INetworkFieldProvider {
   @NetworkInfo
   UUID owner = null;

   public IridiumStoneTileEntity(BlockPos pos, BlockState state) {
      super(IC2Tiles.IRIDIUM_STONE, pos, state);
   }

   @Override
   public List<String> getNetworkFields() {
      return ObjectArrayList.wrap(new String[]{"owner"});
   }

   @Override
   public List<String> getGuiFields() {
      return Collections.emptyList();
   }

   public CompoundTag getUpdateTag() {
      CompoundTag nbt = super.getUpdateTag();
      IC2.NETWORKING.get(IC2.PLATFORM.isSimulating()).sendInitialData(this, nbt);
      return nbt;
   }

   public void handleUpdateTag(CompoundTag tag) {
      super.handleUpdateTag(tag);
      IC2.NETWORKING.get(IC2.PLATFORM.isSimulating()).handleInitialChange(this, tag);
   }

   @Override
   public void setOwner(UUID owner) {
      if (this.owner == null) {
         this.owner = owner;
         this.setChanged();
         IC2.NETWORKING.get(IC2.PLATFORM.isSimulating()).updateTileField(this, "owner");
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
      } else if (!this.owner.equals(user) && !IC2.PLATFORM.isOp(user)) {
         Friend friend = FriendManager.getFriends().getFriend(this.owner, user);
         return friend != null && friend.canApplyAction(action);
      } else {
         return true;
      }
   }

   public void load(CompoundTag nbt) {
      super.load(nbt);
      this.owner = nbt.contains("owner") ? nbt.getUUID("owner") : null;
   }

   public void saveAdditional(CompoundTag nbt) {
      super.saveAdditional(nbt);
      if (this.owner != null) {
         nbt.putUUID("owner", this.owner);
      }
   }
}
