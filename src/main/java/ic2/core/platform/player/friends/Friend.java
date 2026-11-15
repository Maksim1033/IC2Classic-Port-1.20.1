package ic2.core.platform.player.friends;

import com.mojang.authlib.GameProfile;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class Friend {
   private final UUID id;
   private final String name;
   private final Set<Action> validActions = CollectionUtils.<Action>createLinkedSet();

   public Friend(CompoundTag nbt) {
      this(nbt.getString("name"), nbt.getUUID("id"));

      for (StringTag entry : NBTListWrapper.wrap(nbt.getList("actions", 8), StringTag.class)) {
         Action action = Action.getAction(entry.getAsString());
         if (action != null) {
            this.validActions.add(action);
         }
      }
   }

   public Friend(FriendlyByteBuf buffer) {
      this.id = buffer.readUUID();
      this.name = buffer.readUtf(32767);
      int size = buffer.readByte();

      for (int i = 0; i < size; i++) {
         Action action = Action.getAction(buffer.readUtf(32767));
         if (action != null) {
            this.validActions.add(action);
         }
      }
   }

   public Friend(Player player) {
      this(player.getName().getString(), player.getUUID());
   }

   public Friend(GameProfile profile) {
      this(profile.getName(), profile.getId());
   }

   public Friend(String name, UUID id) {
      this.name = name;
      this.id = id;
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeUUID(this.id);
      buffer.writeUtf(this.name);
      buffer.writeByte(this.validActions.size());

      for (Action action : this.validActions) {
         buffer.writeUtf(action.getName());
      }
   }

   public String getName() {
      return this.name;
   }

   public UUID getId() {
      return this.id;
   }

   public Friend addAction(Action action) {
      this.validActions.add(action);
      return this;
   }

   public Friend removeAction(Action action) {
      this.validActions.remove(action);
      return this;
   }

   public boolean canApplyAction(Action action) {
      return this.validActions.contains(action);
   }

   public CompoundTag write(CompoundTag nbt) {
      ListTag list = new ListTag();

      for (Action action : this.validActions) {
         list.add(StringTag.valueOf(action.getName()));
      }

      nbt.putUUID("id", this.id);
      nbt.putString("name", this.name);
      nbt.put("actions", list);
      return nbt;
   }
}
