package ic2.core.networking.packets.server;

import ic2.core.IC2;
import ic2.core.networking.packets.IC2Packet;
import ic2.core.platform.player.friends.Friend;
import ic2.core.platform.player.friends.FriendManager;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class FriendPacket extends IC2Packet {
   Object2ObjectMap<UUID, Set<Friend>> updateData = CollectionUtils.<UUID, Set<Friend>>createLinkedMap();

   public FriendPacket() {
   }

   public FriendPacket(Map<UUID, Set<Friend>> friends) {
      this.updateData.putAll(friends);
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.updateData.size());
      ObjectIterator var2 = Object2ObjectMaps.fastIterable(this.updateData).iterator();

      while (var2.hasNext()) {
         Entry<UUID, Set<Friend>> entry = (Entry<UUID, Set<Friend>>)var2.next();
         buffer.writeUUID((UUID)entry.getKey());
         Set<Friend> friends = (Set<Friend>)entry.getValue();
         buffer.writeVarInt(friends.size());

         for (Friend friend : friends) {
            friend.write(buffer);
         }
      }
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         UUID owner = buffer.readUUID();
         Set<Friend> friends = CollectionUtils.<Friend>createLinkedSet();
         int subSize = buffer.readVarInt();

         for (int j = 0; j < subSize; j++) {
            friends.add(new Friend(buffer));
         }

         this.updateData.put(owner, friends);
      }
   }

   @Override
   public void handlePacket(Player source) {
      FriendManager.getClientFriends().receiveData(this.updateData);
      IC2.PLATFORM.sendPlayerSpecificConfig();
   }
}
