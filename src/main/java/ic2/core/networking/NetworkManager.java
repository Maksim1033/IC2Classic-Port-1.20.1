package ic2.core.networking;

import ic2.api.network.INetworkManager;
import ic2.api.network.buffer.EmptyDataBuffer;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkFieldProvider;
import ic2.api.network.tile.PacketRange;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasCustomGui;
import ic2.core.inventory.base.IHasGui;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.custom.FriendsHandler;
import ic2.core.networking.buffers.FieldBuffer;
import ic2.core.networking.buffers.OutputBuffer;
import ic2.core.networking.buffers.data.BaseTeleporterBuffer;
import ic2.core.networking.buffers.data.HighlightBuffer;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.networking.buffers.data.NodeBuffer;
import ic2.core.networking.buffers.data.ObscuratorBuffer;
import ic2.core.networking.buffers.data.StringDataBuffer;
import ic2.core.networking.buffers.data.VillagerBuffer;
import ic2.core.networking.misc.NetworkUtils;
import ic2.core.networking.packets.client.TileDataBufferEventPacket;
import ic2.core.networking.packets.common.ItemDataBufferPacket;
import ic2.core.networking.packets.common.ItemEventPacket;
import ic2.core.networking.packets.server.gui.open.OpenCustomGuiPacket;
import ic2.core.networking.packets.server.gui.open.OpenEntityGuiPacket;
import ic2.core.networking.packets.server.gui.open.OpenItemGuiPacket;
import ic2.core.networking.packets.server.gui.open.OpenTileGuiPacket;
import ic2.core.networking.packets.server.gui.sync.CustomNetworkPacket;
import ic2.core.networking.packets.server.gui.sync.GuiFieldUpdatePacket;
import ic2.core.networking.packets.server.gui.sync.NetworkFieldUpdatePacket;
import ic2.core.networking.packets.server.gui.sync.ServerTileEventPacket;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.FunctionRegistry;
import ic2.core.utils.collection.SupplierRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.mutable.MutableInt;

public class NetworkManager implements INetworkManager {
   public static final String IC2_DATA = "ic2cData";
   static Map<Class<? extends BlockEntity>, FieldBuffer> FIELD_BUFFERS = CollectionUtils.<Class<? extends BlockEntity>, FieldBuffer>createMap();
   static SupplierRegistry<INetworkDataBuffer, Supplier<INetworkDataBuffer>> BUFFER_REGISTRY = CollectionUtils.createSupplierRegistry();
   static FunctionRegistry<IHasCustomGui, IInputBuffer, Function<IInputBuffer, IHasCustomGui>> CUSTOM_GUI = new FunctionRegistry<>();
   static Map<Level, NetworkManager.FieldStorage> NETWORK_FIELDS = CollectionUtils.<Level, NetworkManager.FieldStorage>createMap();
   static Map<Level, NetworkManager.FieldStorage> GUI_FIELDS = CollectionUtils.<Level, NetworkManager.FieldStorage>createMap();
   static Map<Level, MutableInt> DELAYS = CollectionUtils.<Level, MutableInt>createMap();
   final Function<Level, NetworkManager.FieldStorage> creatorFunction = W -> new NetworkManager.FieldStorage();

   public void init() {
      PacketManager.INSTANCE.init();
      BUFFER_REGISTRY.register(new ResourceLocation("ic2", "nbt_buffer"), NBTBuffer::new, NBTBuffer.class);
      BUFFER_REGISTRY.register(new ResourceLocation("ic2", "string_buffer"), StringDataBuffer::new, StringDataBuffer.class);
      BUFFER_REGISTRY.register(new ResourceLocation("ic2", "obscurator_buffer"), ObscuratorBuffer::new, ObscuratorBuffer.class);
      BUFFER_REGISTRY.register(new ResourceLocation("ic2", "node"), NodeBuffer::new, NodeBuffer.class);
      BUFFER_REGISTRY.register(new ResourceLocation("ic2", "base_teleporter"), BaseTeleporterBuffer::new, BaseTeleporterBuffer.class);
      BUFFER_REGISTRY.register(new ResourceLocation("ic2", "highlight"), HighlightBuffer::new, HighlightBuffer.class);
      BUFFER_REGISTRY.register(new ResourceLocation("ic2", "empty"), () -> EmptyDataBuffer.INSTANCE, EmptyDataBuffer.class);
      BUFFER_REGISTRY.register(new ResourceLocation("ic2", "villager"), VillagerBuffer::new, VillagerBuffer.class);
      CUSTOM_GUI.register(new ResourceLocation("ic2", "friends"), T -> new FriendsHandler(), FriendsHandler.class);
   }

   public static ResourceLocation getID(INetworkDataBuffer buffer) {
      return BUFFER_REGISTRY.getKey(buffer);
   }

   public static INetworkDataBuffer createBuffer(ResourceLocation location) {
      return BUFFER_REGISTRY.getValue(location).get();
   }

   public void onServerStopped() {
      NETWORK_FIELDS.clear();
      GUI_FIELDS.clear();
      DELAYS.clear();
   }

   public void onWorldUnload(Level world) {
      DELAYS.remove(world);
      NETWORK_FIELDS.remove(world);
      GUI_FIELDS.remove(world);
   }

   public void onWorldTick(Level world) {
      MutableInt counter = DELAYS.computeIfAbsent(world, k -> new MutableInt(5));
      if (counter.decrementAndGet() <= 0) {
         counter.setValue(5);
         this.processNetworkPackets(world);
      }
   }

   public void onServerTick() {
      for (NetworkManager.FieldStorage storage : GUI_FIELDS.values()) {
         storage.clear();
      }
   }

   public void processNetworkPackets(Level world) {
      NetworkManager.FieldStorage storage = NETWORK_FIELDS.get(world);
      if (storage != null && !storage.isEmpty()) {
         for (Entry<ChunkPos, Map<BlockPos, List<NetworkManager.FieldData>>> data : storage.getData().entrySet()) {
            ChunkPos pos = data.getKey();
            if (world.hasChunk(pos.x, pos.z)) {
               PacketManager.INSTANCE.sendToAllChunkWatchers(world.getChunk(pos.x, pos.z), new NetworkFieldUpdatePacket(data.getValue()));
            }
         }
      }
   }

   public void registerCustomGui(ResourceLocation location, Class<? extends IHasCustomGui> clz, Function<IInputBuffer, IHasCustomGui> creator) {
      CUSTOM_GUI.register(location, creator, clz);
   }

   @Override
   public void registerDataBuffer(ResourceLocation location, Class<? extends INetworkDataBuffer> clz, Supplier<INetworkDataBuffer> creator) {
      BUFFER_REGISTRY.register(location, creator, clz);
   }

   public void sendCustomUIPacket(Player player, IHasCustomGui gui, int windowId) {
      ResourceLocation id = CUSTOM_GUI.getKey(gui);
      if (id != null) {
         ByteBuf buf = Unpooled.buffer();
         gui.sendInitialData(new OutputBuffer(new FriendlyByteBuf(buf)));
         byte[] data = new byte[buf.writerIndex()];
         buf.readBytes(data);
         PacketManager.INSTANCE.sendToPlayer(new OpenCustomGuiPacket(id, data, windowId), player);
      }
   }

   public IHasCustomGui createCustomGui(Player player, ResourceLocation gui, IInputBuffer buffer) {
      Function<IInputBuffer, IHasCustomGui> function = CUSTOM_GUI.getValue(gui);
      return function == null ? null : function.apply(buffer);
   }

   public void sendUIPacket(Player player, InteractionHand hand, Direction dir, IHasGui gui, int windowID) {
      hand = hand == null ? InteractionHand.MAIN_HAND : hand;
      dir = dir == null ? Direction.NORTH : dir;
      if (gui instanceof BlockEntity tile) {
         PacketManager.INSTANCE.sendToPlayer(new OpenTileGuiPacket(tile.getBlockPos(), hand, dir, windowID), player);
         if (tile instanceof INetworkFieldProvider) {
            this.sendInitialGuiData((INetworkFieldProvider)tile, player);
         }

         this.startGuiTracking(tile, player);
      } else if (gui instanceof Entity entity) {
         PacketManager.INSTANCE.sendToPlayer(new OpenEntityGuiPacket(entity.getId(), hand == InteractionHand.MAIN_HAND, windowID), player);
      } else {
         ItemStack stack = player.getItemInHand(hand);
         if (stack.getItem() instanceof IHasHeldGui) {
            int slot = hand == InteractionHand.OFF_HAND ? -1 : player.getInventory().selected;
            PacketManager.INSTANCE.sendToPlayer(new OpenItemGuiPacket(slot, windowID), player);
         }
      }
   }

   @Override
   public void startGuiTracking(BlockEntity tile, Player player) {
      PlayerHandler.getHandler(player).trackedTile = tile;
   }

   @Override
   public void sendInitialGuiData(INetworkFieldProvider prov, Player player) {
      if (prov instanceof BlockEntity tile) {
         FieldBuffer buffer = getFieldBuffer(tile);
         ArrayList data = new ArrayList();

         for (String s : prov.getGuiFields()) {
            FieldBuffer.FieldInfo info = buffer.getInfo(s);
            if (info != null) {
               data.add(new NetworkManager.FieldData(tile, info));
            }
         }

         PacketManager.INSTANCE.sendToPlayer(new GuiFieldUpdatePacket(tile, data), player);
      }
   }

   @Override
   public void updateGuiData(BlockEntity tile, Player player) {
      NetworkManager.FieldStorage storage = GUI_FIELDS.get(tile.getLevel());
      if (storage != null && !storage.isEmpty()) {
         List<NetworkManager.FieldData> data = storage.getFields(tile);
         if (!data.isEmpty()) {
            PacketManager.INSTANCE.sendToPlayer(new GuiFieldUpdatePacket(tile, data), player);
         }
      }
   }

   @Override
   public void updateTileField(BlockEntity tile, String field) {
      NETWORK_FIELDS.computeIfAbsent(tile.getLevel(), this.creatorFunction).addTileToUpdate(tile, field);
   }

   @Override
   public void updateTileFields(BlockEntity tile, String... fields) {
      NETWORK_FIELDS.computeIfAbsent(tile.getLevel(), this.creatorFunction).addTileToUpdate(tile, fields);
   }

   @Override
   public void updateGuiField(BlockEntity tile, String field) {
      GUI_FIELDS.computeIfAbsent(tile.getLevel(), this.creatorFunction).addTileToUpdate(tile, field);
   }

   @Override
   public void updateGuiFields(BlockEntity tile, String... fields) {
      GUI_FIELDS.computeIfAbsent(tile.getLevel(), this.creatorFunction).addTileToUpdate(tile, fields);
   }

   @Override
   public void sendInitialData(INetworkFieldProvider prov, CompoundTag nbt) {
      List<NetworkManager.FieldData> data = NetworkUtils.createFieldData(prov);
      if (!data.isEmpty()) {
         ByteBuf buf = Unpooled.buffer();
         NetworkUtils.writeEntries(new OutputBuffer(new FriendlyByteBuf(buf)), data);
         byte[] result = new byte[buf.writerIndex()];
         buf.readBytes(result);
         nbt.putByteArray("ic2cData", result);
      }
   }

   @Override
   public void handleInitialChange(BlockEntity prov, CompoundTag nbt) {
   }

   @Override
   public void requestInitialData(INetworkFieldProvider prov) {
      if (prov instanceof BlockEntity tile) {
         for (String s : prov.getNetworkFields()) {
            this.updateTileField(tile, s);
         }
      }
   }

   @Override
   public void sendTileEvent(BlockEntity tile, int key, int value, PacketRange target) {
      switch (target) {
         case SHORT_RANGE:
            PacketManager.INSTANCE.sendToNearby(tile.getLevel(), tile.getBlockPos(), 64, new ServerTileEventPacket(tile, key, value));
            break;
         case LONG_RANGE:
            PacketManager.INSTANCE.sendToNearby(tile.getLevel(), tile.getBlockPos(), 256, new ServerTileEventPacket(tile, key, value));
            break;
         case CHUNK_TRACKED:
            PacketManager.INSTANCE.sendToAllChunkWatchers(tile.getLevel().getChunkAt(tile.getBlockPos()), new ServerTileEventPacket(tile, key, value));
            break;
         case ALL_DIM:
            PacketManager.INSTANCE.sendToAllDim(tile.getLevel(), new ServerTileEventPacket(tile, key, value));
            break;
         case ALL_SERVER:
            PacketManager.INSTANCE.sendToAllPlayers(new ServerTileEventPacket(tile, key, value));
      }
   }

   @Override
   public void sendTileDataBufferEvent(BlockEntity tile, String id, INetworkDataBuffer buffer, PacketRange target) {
      if (getID(buffer) == null) {
         IC2.LOGGER.info("DataBuffer wasn't registered. Ignoring Packet Request");
      } else {
         switch (target) {
            case SHORT_RANGE:
               PacketManager.INSTANCE.sendToNearby(tile.getLevel(), tile.getBlockPos(), 64, new TileDataBufferEventPacket(tile, id, buffer, true));
               break;
            case LONG_RANGE:
               PacketManager.INSTANCE.sendToNearby(tile.getLevel(), tile.getBlockPos(), 256, new TileDataBufferEventPacket(tile, id, buffer, true));
               break;
            case CHUNK_TRACKED:
               PacketManager.INSTANCE
                  .sendToAllChunkWatchers(tile.getLevel().getChunkAt(tile.getBlockPos()), new TileDataBufferEventPacket(tile, id, buffer, true));
               break;
            case ALL_DIM:
               PacketManager.INSTANCE.sendToAllDim(tile.getLevel(), new TileDataBufferEventPacket(tile, id, buffer, true));
               break;
            case ALL_SERVER:
               PacketManager.INSTANCE.sendToAllPlayers(new TileDataBufferEventPacket(tile, id, buffer, true));
         }
      }
   }

   @Override
   public void sendClientTileEvent(BlockEntity tile, int key, int value) {
   }

   @Override
   public void sendClientTileDataBufferEvent(BlockEntity tile, String id, INetworkDataBuffer buffer) {
   }

   @Override
   public void sendItemEvent(Player player, ItemStack stack, int key, int value) {
      PacketManager.INSTANCE.sendToPlayer(new ItemEventPacket(stack, key, value, true), player);
   }

   @Override
   public void sendItemBuffer(Player player, ItemStack stack, String id, INetworkDataBuffer buffer) {
      PacketManager.INSTANCE.sendToPlayer(new ItemDataBufferPacket(true, id, buffer, stack), player);
   }

   @Override
   public void sendClientItemEvent(ItemStack stack, int key, int value) {
   }

   @Override
   public void sendClientItemBuffer(ItemStack stack, String id, INetworkDataBuffer buffer) {
   }

   public void sendCustomBuffer(Player player, String id, INetworkDataBuffer buffer) {
      PacketManager.INSTANCE.sendToPlayer(new CustomNetworkPacket(id, true, buffer), player);
   }

   public void sendCustomClientBuffer(String id, INetworkDataBuffer buffer) {
   }

   public static FieldBuffer getFieldBuffer(BlockEntity tile) {
      FieldBuffer buffer = FIELD_BUFFERS.get(tile.getClass());
      if (buffer == null) {
         buffer = new FieldBuffer((Class<? extends BlockEntity>)tile.getClass());
         FIELD_BUFFERS.put((Class<? extends BlockEntity>)tile.getClass(), buffer);
         buffer.initClass(tile);
      }

      return buffer;
   }

   public static long countNetworkBytes(BlockEntity tile, boolean all, boolean gui) {
      if (!(tile instanceof INetworkFieldProvider provider)) {
         return 0L;
      } else {
         FieldBuffer buffer = getFieldBuffer(tile);
         ObjectList data = CollectionUtils.createList();
         ByteBuf buf = Unpooled.buffer();
         if (all) {
            for (FieldBuffer.FieldInfo info : buffer.getAllInfos()) {
               data.add(new NetworkManager.FieldData(tile, info));
            }
         } else {
            for (String s : gui ? provider.getGuiFields() : provider.getNetworkFields()) {
               FieldBuffer.FieldInfo info = buffer.getInfo(s);
               if (info != null && !info.isDefault(provider)) {
                  data.add(new NetworkManager.FieldData(tile, info));
               }
            }
         }

         NetworkUtils.writeEntries(new OutputBuffer(buf), data);
         return (long)buf.writerIndex();
      }
   }

   public static class FieldData {
      public String fieldName;
      public int index;
      public boolean isNew;
      public NetworkInfo.BitLevel bitLevel;
      public Object data;

      public FieldData(String field, Object theData) {
         this.fieldName = field;
         this.index = -1;
         this.isNew = false;
         this.bitLevel = NetworkInfo.BitLevel.BIT_0;
         this.data = theData;
      }

      public FieldData(int id, NetworkInfo.BitLevel level, Object theData) {
         this.index = id;
         this.isNew = true;
         this.bitLevel = level;
         this.data = theData;
      }

      public FieldData(BlockEntity tile, FieldBuffer.FieldInfo info) {
         this.fieldName = info.getFieldID();
         this.index = info.getIndex();
         this.isNew = info.isNew();
         this.bitLevel = info.getBits();
         this.data = info.getData(tile);
      }
   }

   public static class FieldStorage {
      Map<BlockEntity, Set<String>> fieldsToSync = CollectionUtils.<BlockEntity, Set<String>>createLinkedMap();

      public boolean isEmpty() {
         return this.fieldsToSync.isEmpty();
      }

      public void addTileToUpdate(BlockEntity tile, String... id) {
         Set<String> set = this.fieldsToSync.computeIfAbsent(tile, k -> new ObjectLinkedOpenHashSet());
         set.addAll(Arrays.asList(id));
      }

      public void addTileToUpdate(BlockEntity tile, String id) {
         Set<String> set = this.fieldsToSync.computeIfAbsent(tile, k -> new ObjectLinkedOpenHashSet());
         set.add(id);
      }

      public void clear() {
         this.fieldsToSync.clear();
      }

      public List<NetworkManager.FieldData> getFields(BlockEntity tile) {
         Set<String> set = this.fieldsToSync.get(tile);
         if (set != null && !set.isEmpty()) {
            FieldBuffer buffer = NetworkManager.getFieldBuffer(tile);
            List<NetworkManager.FieldData> data = new ArrayList<>();

            for (String s : set) {
               FieldBuffer.FieldInfo info = buffer.getInfo(s);
               if (info != null) {
                  data.add(new NetworkManager.FieldData(tile, info));
               }
            }

            return data;
         } else {
            return Collections.emptyList();
         }
      }

      public Map<ChunkPos, Map<BlockPos, List<NetworkManager.FieldData>>> getData() {
         Map<ChunkPos, Map<BlockPos, List<NetworkManager.FieldData>>> map = CollectionUtils.<ChunkPos, Map<BlockPos, List<NetworkManager.FieldData>>>createLinkedMap();

         for (Entry<BlockEntity, Set<String>> entry : this.fieldsToSync.entrySet()) {
            BlockEntity tile = entry.getKey();
            if (!tile.isRemoved()) {
               BlockPos pos = tile.getBlockPos();
               ChunkPos chunkPos = new ChunkPos(pos);
               Map<BlockPos, List<NetworkManager.FieldData>> infos = map.computeIfAbsent(chunkPos, k -> CollectionUtils.createLinkedMap());
               FieldBuffer buffer = NetworkManager.getFieldBuffer(tile);
               List<NetworkManager.FieldData> data = new ArrayList<>();

               for (String s : entry.getValue()) {
                  FieldBuffer.FieldInfo info = buffer.getInfo(s);
                  if (info != null) {
                     data.add(new NetworkManager.FieldData(tile, info));
                  }
               }

               if (!data.isEmpty()) {
                  infos.put(pos, data);
               }
            }
         }

         this.fieldsToSync.clear();
         return map;
      }
   }
}
