package ic2.core.block.transport.item.cache;

import ic2.api.tiles.tubes.IItemCache;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.client.custom.CacheRequestPacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ClientItemCache implements IItemCache {
   Int2ObjectMap<ClientItemCache.Wrapper> wrappers = new Int2ObjectOpenHashMap();
   IntSet missingIds = new IntLinkedOpenHashSet();

   @Override
   public Supplier<ItemStack> getItem(int id) {
      ClientItemCache.Wrapper wrapper = (ClientItemCache.Wrapper)this.wrappers.get(id);
      if (wrapper == null) {
         this.missingIds.add(id);
         wrapper = new ClientItemCache.Wrapper();
         this.wrappers.put(id, wrapper);
      }

      return wrapper;
   }

   @Override
   public int registerItem(ItemStack stack) {
      throw new IllegalStateException("Can not register Items on the Client");
   }

   @Override
   public void clearCache() {
      this.wrappers.clear();
      this.missingIds.clear();
   }

   public void onDataReceived(FriendlyByteBuf buffer) {
      int i = 0;

      for (int m = buffer.readVarInt(); i < m; i++) {
         int id = buffer.readVarInt();
         ClientItemCache.Wrapper wrapper = (ClientItemCache.Wrapper)this.wrappers.get(id);
         if (wrapper == null) {
            wrapper = new ClientItemCache.Wrapper();
            this.wrappers.put(id, wrapper);
         }

         wrapper.set(buffer.readItem());
      }
   }

   @Override
   public void updateCache() {
      if (!this.missingIds.isEmpty()) {
         int[] ids = this.missingIds.toIntArray();
         this.missingIds.clear();
         PacketManager.INSTANCE.sendToServer(new CacheRequestPacket(ids));
      }
   }

   static class Wrapper implements Supplier<ItemStack> {
      ItemStack item = ItemStack.EMPTY;

      public void set(ItemStack stack) {
         this.item = stack;
      }

      public ItemStack get() {
         return this.item;
      }
   }
}
