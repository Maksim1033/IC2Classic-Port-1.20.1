package ic2.core.item.inv.inventory;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.PacketStats;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.tiles.readers.IEUStorage;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.PortableInventory;
import ic2.core.item.inv.container.EUReaderContainer;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.LongAverager;
import ic2.core.utils.collection.NBTListWrapper;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class EUReaderInventory extends PortableInventory {
   Long2ObjectMap<EUReaderInventory.SinkInfo> receivers = new Long2ObjectLinkedOpenHashMap();
   Long2ObjectMap<EUReaderInventory.SourceInfo> emitters = new Long2ObjectLinkedOpenHashMap();
   public List<EUReaderInventory.SinkInfo> sortedReceivers = CollectionUtils.<EUReaderInventory.SinkInfo>createList();
   public List<EUReaderInventory.SourceInfo> sortedEmitters = CollectionUtils.<EUReaderInventory.SourceInfo>createList();

   public EUReaderInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot) {
      super(owner, held, stack, slot);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new EUReaderContainer(this, player, this.getID(), windowID);
   }

   @Override
   public int getSlotCount() {
      return 0;
   }

   @OnlyIn(Dist.CLIENT)
   public void accept(NBTBuffer buffer) {
      LongSet receivedSinks = new LongOpenHashSet(this.receivers.keySet());
      LongSet receivedSource = new LongOpenHashSet(this.emitters.keySet());
      boolean changedSinks = false;
      boolean changedSources = false;
      MutableBlockPos positions = new MutableBlockPos();

      for (CompoundTag nbt : NBTListWrapper.wrap(buffer.getNBT().getList("data", 10), CompoundTag.class)) {
         long pos = nbt.getLong("pos");
         receivedSinks.remove(pos);
         receivedSource.remove(pos);
         BlockEntity tile = Minecraft.getInstance().level.getBlockEntity(positions.set(pos));
         Component name;
         if (tile != null) {
            if (tile instanceof Nameable nameable) {
               name = nameable.hasCustomName() ? nameable.getCustomName() : nameable.getName();
            } else {
               name = tile.getBlockState().getBlock().getName();
            }
         } else {
            name = ForgeRegistryWrapper.getBlockValue(new ResourceLocation(nbt.getString("blockId"))).getName();
         }

         if (nbt.getBoolean("accepting")) {
            EUReaderInventory.SinkInfo info = (EUReaderInventory.SinkInfo)this.receivers.get(pos);
            if (info == null) {
               info = new EUReaderInventory.SinkInfo(nbt.getString("id"), name, pos, nbt.contains("tier") ? nbt.getInt("tier") : 1);
               this.receivers.put(pos, info);
               changedSinks = true;
            }

            info.addPacket(nbt.getLong("packets"), nbt.getLong("power"));
            info.updateEUData(nbt.getInt("stored_eu"), nbt.getInt("max_eu"));
         } else {
            EUReaderInventory.SourceInfo info = (EUReaderInventory.SourceInfo)this.emitters.get(pos);
            if (info == null) {
               info = new EUReaderInventory.SourceInfo(nbt.getString("id"), name, pos, nbt.contains("tier") ? nbt.getInt("tier") : 1);
               this.emitters.put(pos, info);
               changedSources = true;
            }

            info.addPacket(nbt.getLong("packets"), nbt.getLong("power"), nbt.contains("max") ? nbt.getInt("max") : -1);
            info.updateEUData(nbt.getInt("stored_eu"), nbt.getInt("max_eu"));
         }
      }

      if (!receivedSinks.isEmpty()) {
         this.receivers.keySet().removeAll(receivedSinks);
         changedSinks = true;
      }

      if (!receivedSource.isEmpty()) {
         this.emitters.keySet().removeAll(receivedSource);
         changedSources = true;
      }

      if (changedSinks) {
         this.sortedReceivers.clear();
         this.sortedReceivers.addAll(this.receivers.values());
      }

      if (changedSources) {
         this.sortedEmitters.clear();
         this.sortedEmitters.addAll(this.emitters.values());
      }
   }

   public static void processEnergyNet(ItemStack stack, Player player, BlockPos pos) {
      IEnergyTile tile = EnergyNet.INSTANCE.getTile(player.level(), pos);
      if (tile != null) {
         ListTag list = new ListTag();

         for (PacketStats stat : EnergyNet.INSTANCE.getPacketStats(tile)) {
            CompoundTag nbt = pack(stat);
            if (nbt != null) {
               list.add(nbt);
            }
         }

         IC2.NETWORKING.get(true).sendItemBuffer(player, stack, "EU_INFO", new NBTBuffer("data", list));
      }
   }

   public static CompoundTag pack(PacketStats stat) {
      CompoundTag nbt = new CompoundTag();
      nbt.putBoolean("accepting", stat.isAccepting());
      nbt.putLong("power", stat.getPower());
      nbt.putLong("packets", stat.getPackets());
      IEnergyTile tile = stat.getTile();
      nbt.putString("id", ForgeRegistryWrapper.getItemKey(tile.getWorldObj().getBlockState(tile.getPosition()).getBlock().asItem()).toString());
      nbt.putLong("pos", tile.getPosition().asLong());
      if (tile instanceof IEUStorage storage) {
         nbt.putInt("stored_eu", storage.getStoredEU());
         nbt.putInt("max_eu", storage.getMaxEU());
      }

      nbt.putString("blockId", ForgeRegistryWrapper.getItemKey(tile.getWorldObj().getBlockState(tile.getPosition()).getBlock().asItem()).toString());
      if (!stat.isAccepting() && tile instanceof IEnergySource source) {
         nbt.putInt("max", source.getMaxEnergyOutput());
         nbt.putByte("tier", (byte)source.getSourceTier());
         return nbt;
      } else if (stat.isAccepting() && tile instanceof IEnergySink) {
         nbt.putByte("tier", (byte)((IEnergySink)tile).getSinkTier());
         return nbt;
      } else {
         return null;
      }
   }

   public static class SinkInfo {
      public final ItemStack item;
      public final BlockPos position;
      public final Component name;
      public final LongAverager packetsReceived = new LongAverager(80);
      public final LongAverager energyReceived = new LongAverager(80);
      public final int tier;
      public final int max;
      public long lastPower = -1L;
      public long lastPackets = -1L;
      public int storedEU;
      public int maxEU;

      public SinkInfo(String id, Component name, long pos, int tier) {
         this.item = new ItemStack((ItemLike)ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)));
         this.name = name;
         this.position = BlockPos.of(pos);
         this.tier = tier;
         this.max = EnergyNet.INSTANCE.getPowerFromTier(tier);
      }

      public SinkInfo(List<EUReaderInventory.SinkInfo> infos) {
         EUReaderInventory.SinkInfo info = infos.get(0);
         this.item = info.item;
         this.name = info.name;
         this.tier = info.tier;
         this.max = info.max;
         this.position = BlockPos.ZERO;
         long[] packetArray = new long[80];
         long[] energyArray = new long[80];
         int i = 0;

         for (int m = infos.size(); i < m; i++) {
            info = infos.get(i);
            info.energyReceived.extract(energyArray);
            info.packetsReceived.extract(packetArray);
            this.storedEU = this.storedEU + info.storedEU;
            this.maxEU = this.maxEU + info.maxEU;
         }

         for (int ix = 0; ix < 80; ix++) {
            this.energyReceived.addEntry(energyArray[ix]);
            this.packetsReceived.addEntry(packetArray[ix]);
         }

         this.energyReceived.buildCache();
         this.packetsReceived.buildCache();
      }

      public void updateEUData(int stored, int max) {
         this.storedEU = stored;
         this.maxEU = max;
      }

      public void addPacket(long packets, long power) {
         if (this.lastPower == -1L) {
            this.lastPower = power;
            this.lastPackets = packets;
         } else {
            this.packetsReceived.addEntry(packets - this.lastPackets);
            this.energyReceived.addEntry(power - this.lastPower);
            this.lastPower = power;
            this.lastPackets = packets;
         }
      }

      public double getEnergy() {
         return this.energyReceived.getCachedDoubleAverage();
      }

      public long getPackets() {
         return (long)Math.ceil(this.packetsReceived.getCachedDoubleAverage());
      }
   }

   public static class SourceInfo {
      public final ItemStack item;
      public final BlockPos position;
      public final Component name;
      public final LongAverager packetsReceived = new LongAverager(80);
      public final LongAverager energyReceived = new LongAverager(80);
      public int output;
      public final int tier;
      public final int max;
      public long lastPower = -1L;
      public long lastPackets = -1L;
      public int storedEU;
      public int maxEU;

      public SourceInfo(String id, Component name, long pos, int tier) {
         this.item = new ItemStack(ForgeRegistryWrapper.getItemValue(new ResourceLocation(id)));
         this.name = name;
         this.position = BlockPos.of(pos);
         this.tier = tier;
         this.max = EnergyNet.INSTANCE.getPowerFromTier(tier);
      }

      public SourceInfo(List<EUReaderInventory.SourceInfo> infos) {
         EUReaderInventory.SourceInfo info = infos.get(0);
         this.item = info.item;
         this.name = info.name;
         this.tier = info.tier;
         this.max = info.max;
         this.position = BlockPos.ZERO;
         long[] packetArray = new long[80];
         long[] energyArray = new long[80];
         int i = 0;

         for (int m = infos.size(); i < m; i++) {
            info = infos.get(i);
            info.energyReceived.extract(energyArray);
            info.packetsReceived.extract(packetArray);
            this.output = this.output + info.output;
            this.storedEU = this.storedEU + info.storedEU;
            this.maxEU = this.maxEU + info.maxEU;
         }

         for (int ix = 0; ix < 80; ix++) {
            this.energyReceived.addEntry(energyArray[ix]);
            this.packetsReceived.addEntry(packetArray[ix]);
         }

         this.energyReceived.buildCache();
         this.packetsReceived.buildCache();
      }

      public void updateEUData(int stored, int max) {
         this.storedEU = stored;
         this.maxEU = max;
      }

      public void addPacket(long packets, long power, int output) {
         this.output = output;
         if (this.lastPower == -1L) {
            this.lastPower = power;
            this.lastPackets = packets;
         } else {
            this.packetsReceived.addEntry(packets - this.lastPackets);
            this.energyReceived.addEntry(power - this.lastPower);
            this.lastPower = power;
            this.lastPackets = packets;
         }
      }

      public double getEnergy() {
         return this.energyReceived.getCachedDoubleAverage();
      }

      public long getPackets() {
         return this.packetsReceived.getCachedAverage();
      }
   }
}
