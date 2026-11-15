package ic2.core.block.cables.mointor;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.tiles.display.IDisplayInfo;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.server.custom.MonitorSyncPacket;
import ic2.core.utils.collection.CollectionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MonitorDataManager implements INetworkDataBuffer {
   List<IDisplayInfo> infos = CollectionUtils.<IDisplayInfo>createList();
   List<Tag> data = CollectionUtils.<Tag>createList();

   public void addData(IDisplayInfo info) {
      this.infos.add(info);
      this.data.add(info.getServerData());
   }

   public boolean isEmpty() {
      return this.infos.isEmpty();
   }

   public int size() {
      return this.infos.size();
   }

   public IDisplayInfo get(int index) {
      return this.infos.get(index);
   }

   public void clear() {
      this.infos.clear();
      this.data.clear();
   }

   public void onSyncReceived(FriendlyByteBuf buffer) {
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         int slot = buffer.readVarInt();
         if (slot >= this.infos.size()) {
            return;
         }

         this.infos.set(slot, IDisplayInfo.REGISTRY.deserialize(buffer));
      }
   }

   public boolean tick(BlockEntity tile) {
      IntList list = new IntArrayList();
      int i = 0;

      for (int m = this.infos.size(); i < m; i++) {
         IDisplayInfo info = this.infos.get(i);
         if (!info.isValid()) {
            this.infos.clear();
            this.data.clear();
            return true;
         }

         Tag newData = info.getServerData();
         if (!newData.equals(this.data.get(i))) {
            this.data.set(i, newData);
            list.add(i);
         }
      }

      if (!list.isEmpty()) {
         ByteBuf buf = Unpooled.buffer();
         FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
         buffer.writeVarInt(list.size());
         int ix = 0;

         for (int m = list.size(); ix < m; ix++) {
            int slot = list.getInt(ix);
            buffer.writeVarInt(slot);
            IDisplayInfo.REGISTRY.serialize(this.infos.get(slot), buffer);
         }

         byte[] data = new byte[buf.writerIndex()];
         buf.readBytes(data);
         PacketManager.INSTANCE.sendToAllChunkWatchers(tile.getLevel().getChunkAt(tile.getBlockPos()), new MonitorSyncPacket(tile.getBlockPos(), data));
      }

      return false;
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeVarInt(this.infos.size());
      int i = 0;

      for (int m = this.infos.size(); i < m; i++) {
         this.serialize(this.infos.get(i), buffer);
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.infos.clear();
      int i = 0;

      for (int m = buffer.readVarInt(); i < m; i++) {
         this.infos.add(this.deserialize(buffer));
      }
   }

   private void serialize(IDisplayInfo info, IOutputBuffer buffer) {
      ByteBuf buf = Unpooled.buffer();
      IDisplayInfo.REGISTRY.serialize(info, new FriendlyByteBuf(buf));
      byte[] data = new byte[buf.writerIndex()];
      buf.readBytes(data);
      buffer.writeBytes(data);
   }

   private IDisplayInfo deserialize(IInputBuffer buffer) {
      return IDisplayInfo.REGISTRY.deserialize(new FriendlyByteBuf(Unpooled.wrappedBuffer(buffer.readBytes())));
   }
}
