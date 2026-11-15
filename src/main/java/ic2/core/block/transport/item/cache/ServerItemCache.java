package ic2.core.block.transport.item.cache;

import ic2.api.tiles.tubes.IItemCache;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.helpers.StackUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ServerItemCache implements IItemCache, Strategy<ItemStack>, ToIntFunction<ItemStack> {
   int id = 1;
   Object2IntMap<ItemStack> reverseIds = new Object2IntOpenCustomHashMap(this);
   Int2ObjectMap<ItemStack> ids = new Int2ObjectOpenHashMap();

   @Override
   public Supplier<ItemStack> getItem(int id) {
      throw new IllegalStateException("Server can not provide Items");
   }

   @Override
   public int registerItem(ItemStack stack) {
      return this.reverseIds.computeIfAbsent(stack.copy(), this);
   }

   @Override
   public void updateCache() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void clearCache() {
      this.id = 1;
      this.reverseIds.clear();
      this.ids.clear();
   }

   public byte[] createData(int[] requestsIds) {
      ByteBuf buf = Unpooled.buffer();
      FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
      buffer.writeVarInt(requestsIds.length);

      for (int requestsId : requestsIds) {
         buffer.writeVarInt(requestsId);
         buffer.writeItem((ItemStack)this.ids.get(requestsId));
      }

      byte[] data = new byte[buf.writerIndex()];
      buf.readBytes(data);
      return data;
   }

   public int applyAsInt(ItemStack value) {
      if (this.id < 1) {
         this.id = 1;
      }

      this.ids.put(this.id, value);
      return this.id++;
   }

   public int hashCode(ItemStack stack) {
      return Objects.hash(ForgeRegistryWrapper.getItemKey(stack.getItem()).toString(), stack.getTag());
   }

   public boolean equals(ItemStack key, ItemStack value) {
      return key != null && value != null && StackUtil.isStackEqual(key, value);
   }
}
