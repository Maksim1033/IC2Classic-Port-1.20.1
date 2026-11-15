package ic2.core.block.base.misc;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.block.base.tiles.BaseMultiBlockTileEntity;
import ic2.core.inventory.base.INBTSavable;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LinkStorage implements INBTSavable, INetworkDataBuffer {
   final BaseMultiBlockTileEntity master;
   final String name;
   LongSet positions = new LongLinkedOpenHashSet();
   List<BaseLinkingTileEntity> tiles = CollectionUtils.<BaseLinkingTileEntity>createList();

   public LinkStorage(String name, BaseMultiBlockTileEntity tile) {
      this.name = name;
      this.master = tile;
   }

   public void add(BaseLinkingTileEntity tile) {
      if (tile != null) {
         if (this.positions.add(tile.getBlockPos().asLong())) {
            this.tiles.add(tile);
         }
      }
   }

   public boolean contains(BlockPos pos) {
      return this.positions.contains(pos.asLong());
   }

   public void refreshReferences() {
      if (this.tiles.isEmpty() && !this.positions.isEmpty()) {
         MutableBlockPos pos = new MutableBlockPos();
         LongIterator var2 = this.positions.iterator();

         while (var2.hasNext()) {
            long entry = (Long)var2.next();
            BlockEntity tile = this.master.getLevel().getBlockEntity(pos.set(entry));
            if (tile instanceof BaseLinkingTileEntity) {
               this.tiles.add((BaseLinkingTileEntity)tile);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public List<BaseLinkingTileEntity> getClientTiles() {
      List<BaseLinkingTileEntity> base = CollectionUtils.<BaseLinkingTileEntity>createList();
      LongIterator var2 = this.positions.iterator();

      while (var2.hasNext()) {
         long pos = (Long)var2.next();
         BlockEntity tile = this.master.getLevel().getBlockEntity(BlockPos.of(pos));
         if (tile instanceof BaseLinkingTileEntity) {
            base.add((BaseLinkingTileEntity)tile);
         }
      }

      return base;
   }

   public List<BaseLinkingTileEntity> getTiles() {
      return this.tiles;
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      if (!this.positions.isEmpty()) {
         LongList list = LongArrayList.wrap(this.master.structure.structurePositions);
         if (list.size() > 32767) {
            nbt.putLongArray(this.name, this.positions.toLongArray());
         } else if (list.size() > 255) {
            ListTag type = new ListTag();
            LongIterator iter = this.positions.iterator();

            while (iter.hasNext()) {
               type.add(ShortTag.valueOf((short)list.indexOf(iter.nextLong())));
            }

            nbt.put(this.name, type);
         } else {
            byte[] data = new byte[this.positions.size()];
            int index = 0;

            for (LongIterator iter = this.positions.iterator(); iter.hasNext(); index++) {
               data[index] = (byte)(list.indexOf(iter.nextLong()) - 128);
            }

            nbt.putByteArray(this.name, data);
         }
      }

      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      long[] data = this.master.structure.structurePositions;
      if (data != null) {
         if (data.length > 32767) {
            this.positions.addAll(LongArrayList.wrap(nbt.getLongArray(this.name)));
         } else if (data.length > 255) {
            for (ShortTag value : NBTListWrapper.wrap(nbt.getList(this.name, 2), ShortTag.class)) {
               this.positions.add(data[value.getAsInt()]);
            }
         } else {
            byte[] array = nbt.getByteArray(this.name);

            for (byte b : array) {
               this.positions.add(data[b + 128]);
            }
         }
      }
   }

   public void finish() {
      this.refreshReferences();
      int i = 0;

      for (int m = this.tiles.size(); i < m; i++) {
         this.tiles.get(i).setMaster(this.master);
      }
   }

   public void setActive(boolean active) {
      int i = 0;

      for (int m = this.tiles.size(); i < m; i++) {
         this.tiles.get(i).setActive(active);
      }
   }

   public void clear(boolean removeMaster) {
      if (removeMaster) {
         this.refreshReferences();
         ObjectListIterator var2 = new ObjectArrayList(this.tiles).iterator();

         while (var2.hasNext()) {
            BaseLinkingTileEntity tile = (BaseLinkingTileEntity)var2.next();
            tile.clearMaster(this.master.getBlockPos());
         }
      }

      this.positions.clear();
      this.tiles.clear();
   }

   @Override
   public void write(IOutputBuffer buffer) {
      if (!this.positions.isEmpty() && this.master.structure.structurePositions != null) {
         buffer.writeBoolean(true);
         LongList list = LongArrayList.wrap(this.master.structure.structurePositions);
         buffer.writeVarInt(this.positions.size());
         if (list.size() > 32767) {
            buffer.writeByte((byte)2);
            LongIterator iter = this.positions.iterator();

            while (iter.hasNext()) {
               buffer.writeLong(iter.nextLong());
            }
         } else if (list.size() > 255) {
            buffer.writeByte((byte)1);
            LongIterator iter = this.positions.iterator();

            while (iter.hasNext()) {
               buffer.writeShort((short)list.indexOf(iter.nextLong()));
            }
         } else {
            buffer.writeByte((byte)0);
            LongIterator iter = this.positions.iterator();

            while (iter.hasNext()) {
               buffer.writeByte((byte)(list.indexOf(iter.nextLong()) - 128));
            }
         }
      } else {
         buffer.writeBoolean(false);
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.positions.clear();
      if (buffer.readBoolean()) {
         long[] data = this.master.structure.structurePositions;
         if (data == null) {
            return;
         }

         int size = buffer.readVarInt();
         byte type = buffer.readByte();

         for (int i = 0; i < size; i++) {
            switch (type) {
               case 0:
                  this.positions.add(data[buffer.readByte() + 128]);
                  break;
               case 1:
                  this.positions.add(data[buffer.readShort()]);
                  break;
               case 2:
                  this.positions.add(buffer.readLong());
            }
         }
      }
   }
}
