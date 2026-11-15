package ic2.core.platform.player.friends;

import ic2.core.IC2;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.client.friend.FriendSyncRequest;
import ic2.core.networking.packets.server.FriendPacket;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;

public class FriendManager extends SavedData {
   static final FriendManager CLIENT_MANAGER = new FriendManager();
   Map<UUID, Map<UUID, Friend>> friend_data = CollectionUtils.<UUID, Map<UUID, Friend>>createLinkedMap();
   Map<UUID, Set<Friend>> known_Friends = CollectionUtils.<UUID, Set<Friend>>createLinkedMap();
   boolean ignoreUpdates = false;

   public FriendManager() {
   }

   public FriendManager(CompoundTag nbt) {
      this.known_Friends.clear();
      this.friend_data.clear();

      for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("data", 10), CompoundTag.class)) {
         UUID id = data.getUUID("id");
         Map<UUID, Friend> friendMap = CollectionUtils.<UUID, Friend>createMap();
         Set<Friend> friends = CollectionUtils.<Friend>createLinkedSet();

         for (CompoundTag friendEntry : NBTListWrapper.wrap(data.getList("friends", 10), CompoundTag.class)) {
            Friend friend = new Friend(friendEntry);
            friends.add(friend);
            friendMap.put(friend.getId(), friend);
         }

         if (!friends.isEmpty()) {
            this.known_Friends.put(id, friends);
            this.friend_data.put(id, friendMap);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static FriendManager getClientFriends() {
      return CLIENT_MANAGER;
   }

   public static FriendManager getFriends() {
      return IC2.PLATFORM.isRendering() ? CLIENT_MANAGER : getServerFriends();
   }

   public static FriendManager getServerFriends() {
      return (FriendManager)ServerLifecycleHooks.getCurrentServer()
         .getLevel(Level.OVERWORLD)
         .getDataStorage()
         .computeIfAbsent(FriendManager::new, FriendManager::new, "ic2_friend_data");
   }

   public CompoundTag save(CompoundTag compound) {
      ListTag list = new ListTag();

      for (Entry<UUID, Set<Friend>> entry : this.known_Friends.entrySet()) {
         CompoundTag nbt = new CompoundTag();
         nbt.putUUID("id", entry.getKey());
         ListTag friends = new ListTag();

         for (Friend friend : entry.getValue()) {
            friends.add(friend.write(new CompoundTag()));
         }

         nbt.put("friends", friends);
         list.add(nbt);
      }

      compound.put("data", list);
      return compound;
   }

   public void updateFriendData(Player player, List<Friend> friend) {
      UUID data = player.getUUID();
      this.known_Friends.remove(data);
      this.friend_data.remove(data);
      int i = 0;

      for (int m = friend.size(); i < m; i++) {
         this.addFriend(data, friend.get(i));
      }

      this.setDirty();
      if (IC2.PLATFORM.isSimulating()) {
         this.sync();
      }
   }

   public boolean addFriend(UUID person, Friend friend) {
      if (person == friend.getId()) {
         return false;
      } else {
         Set<Friend> friends = this.known_Friends.computeIfAbsent(person, k -> CollectionUtils.createLinkedSet());
         if (friends.add(friend)) {
            Map<UUID, Friend> map = this.friend_data.computeIfAbsent(person, k -> CollectionUtils.createMap());
            this.setDirty();
            return map.put(friend.getId(), friend) == null;
         } else {
            return false;
         }
      }
   }

   public boolean removeFriend(UUID person, UUID otherPerson) {
      Map<UUID, Friend> map = this.friend_data.get(person);
      if (map == null) {
         return false;
      } else {
         Set<Friend> friends = this.known_Friends.get(person);
         if (friends.remove(map.remove(otherPerson))) {
            if (friends.isEmpty()) {
               this.known_Friends.remove(person);
            }

            if (map.isEmpty()) {
               this.friend_data.remove(person);
            }

            this.setDirty();
            return true;
         } else {
            return false;
         }
      }
   }

   public Friend getFriend(UUID owner, UUID friend) {
      Map<UUID, Friend> friends = this.friend_data.get(owner);
      return friends == null ? null : friends.get(friend);
   }

   public List<Friend> getFriends(UUID owner) {
      Set<Friend> set = this.known_Friends.get(owner);
      return (List<Friend>)(set == null ? Collections.emptyList() : new ObjectArrayList<Friend>(set));
   }

   public void sendDataToPlayer(Player player) {
      PacketManager.INSTANCE.sendToPlayer(new FriendPacket(this.known_Friends), player);
   }

   public void sync() {
      PacketManager.INSTANCE.sendToAllPlayers(new FriendPacket(this.known_Friends));
   }

   public void requestSync() {
      PacketManager.INSTANCE.sendToServer(new FriendSyncRequest());
   }

   public void setIgnoreUpdates(boolean value) {
      this.ignoreUpdates = value;
   }

   @OnlyIn(Dist.CLIENT)
   public void receiveData(Object2ObjectMap<UUID, Set<Friend>> updateData) {
      if (!this.ignoreUpdates) {
         this.known_Friends.clear();
         this.friend_data.clear();
         ObjectIterator var2 = Object2ObjectMaps.fastIterable(updateData).iterator();

         while (var2.hasNext()) {
            Entry<UUID, Set<Friend>> entry = (Entry<UUID, Set<Friend>>)var2.next();
            UUID owner = entry.getKey();

            for (Friend friend : entry.getValue()) {
               this.addFriend(owner, friend);
            }
         }
      }
   }
}
