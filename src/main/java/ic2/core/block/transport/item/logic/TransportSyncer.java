package ic2.core.block.transport.item.logic;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.core.IC2;
import ic2.core.networking.buffers.InputBuffer;
import ic2.core.networking.buffers.OutputBuffer;
import ic2.core.networking.buffers.data.NBTBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TransportSyncer {
   ObjectSet<TransportedItem> items = new ObjectLinkedOpenHashSet();

   public void markDirty(TransportedItem item) {
      this.items.add(item);
   }

   public void removeAll(Set<TransportedItem> toDelete) {
      this.items.removeAll(toDelete);
   }

   public boolean hasData() {
      return !this.items.isEmpty();
   }

   public void clear() {
      this.items.clear();
   }

   public byte[] serialize() {
      ByteBuf buf = Unpooled.buffer();
      IOutputBuffer buffer = new OutputBuffer(buf);
      buffer.writeVarInt(this.items.size());
      ObjectIterator data = this.items.iterator();

      while (data.hasNext()) {
         TransportedItem item = (TransportedItem)data.next();
         buffer.writeVarInt(item.getId());
         buffer.writeShort(item.serializeEssentials());
      }

      this.items.clear();
      byte[] datax = new byte[buf.writerIndex()];
      buf.readBytes(datax);
      return datax;
   }

   public void deserialize(byte[] itemData, BlockEntity receiver, String tag, List<TransportedItem> stored) {
      IInputBuffer buffer = new InputBuffer(itemData);
      Int2ShortMap items = new Int2ShortOpenHashMap();
      int i = 0;

      for (int m = buffer.readVarInt(); i < m; i++) {
         int id = buffer.readVarInt();
         items.put(id, buffer.readShort());
      }

      i = 0;

      for (int m = stored.size(); i < m; i++) {
         TransportedItem item = stored.get(i);
         if (items.containsKey(item.getId())) {
            item.deserializeEssentials(items.remove(item.getId()));
         }
      }

      if (!items.isEmpty()) {
         IC2.NETWORKING.get().sendClientTileDataBufferEvent(receiver, tag, new NBTBuffer("data", new IntArrayTag(items.keySet().toIntArray())));
      }
   }
}
