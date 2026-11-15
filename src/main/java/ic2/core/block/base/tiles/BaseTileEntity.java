package ic2.core.block.base.tiles;

import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkClientEventListener;
import ic2.api.network.tile.INetworkFieldNotifier;
import ic2.api.network.tile.INetworkFieldProvider;
import ic2.api.network.tile.PacketRange;
import ic2.api.tiles.IMachine;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.audio.IAudioSource;
import ic2.core.block.base.IStateController;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.IProfileListener;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.redstone.IComparable;
import ic2.core.block.base.misc.comparator.BaseComparator;
import ic2.core.block.base.misc.comparator.ComparatorManager;
import ic2.core.networking.NetworkManager;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.client.TileDataBufferEventPacket;
import ic2.core.networking.packets.server.gui.sync.ServerTileEventPacket;
import ic2.core.platform.corehacks.mixins.server.ChunkMixin;
import ic2.core.platform.events.ChunkSaver;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.LongAverager;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.tooltips.ILangHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import org.apache.logging.log4j.util.Strings;

public abstract class BaseTileEntity
   extends BlockEntity
   implements INetworkFieldProvider,
   INetworkFieldNotifier,
   Nameable,
   IProfileListener,
   IComparable,
   INetworkClientEventListener,
   ILangHelper {
   private final BlockEntityType<?> type;
   private final boolean isServer;
   private boolean isTicking;
   private final NetworkManager network;
   boolean loaded = false;
   boolean locked = false;
   boolean recache = false;
   boolean adding = false;
   boolean removing = false;
   LongAverager profiler = null;
   @NetworkInfo
   private boolean isActive = false;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   private int facing = Direction.NORTH.get3DDataValue();
   @NetworkInfo
   private String customName = "";
   long position;
   public boolean needsRedstoneUpdate = this.needsInitialRedstoneCheck();
   public boolean sensitive = true;
   public boolean directionalSignal = false;
   public boolean inverted = false;
   protected byte[] sidedSignals = new byte[6];
   protected byte signal = 0;
   @NetworkInfo
   protected ComparatorManager comparators = new ComparatorManager(this);
   List<String> networkFields = CollectionUtils.<String>createList();
   List<String> guiFields = CollectionUtils.<String>createList();
   Map<Capability<?>, LazyOptional<?>> capabilities = CollectionUtils.<Capability<?>, LazyOptional<?>>createMap();
   List<ICache<?>> caches = CollectionUtils.<ICache<?>>createList();

   protected BaseTileEntity(BlockPos pos, BlockState state) {
      super(null, pos, state);
      this.isServer = IC2.PLATFORM.isSimulating();
      this.isTicking = this instanceof ITickListener;
      this.network = IC2.NETWORKING.get(this.isServer);
      this.type = this.createType();
      this.position = Mth.getSeed(pos) & 4294967295L;
      this.addNetworkFields("isActive", "facing", "customName");
   }

   public final void addComparator(BaseComparator comp) {
      this.comparators.addComparator(comp);
   }

   public abstract BlockEntityType<?> createType();

   public final boolean isSimulating() {
      return this.isServer;
   }

   public final boolean isRendering() {
      return !this.isServer;
   }

   public final NetworkManager getNetwork() {
      return this.network;
   }

   public final AudioManager getSounds() {
      return IC2.AUDIO;
   }

   protected final void playStop(IAudioSource source, boolean play) {
      if (source != null) {
         source.playStop(play);
      }
   }

   public final void sendToServer(int key, int value) {
      this.network.sendClientTileEvent(this, key, value);
   }

   public final void sendToClient(int key, int value, PacketRange range) {
      this.network.sendTileEvent(this, key, value, range);
   }

   public final void sendToPlayer(Player player, int key, int value) {
      PacketManager.INSTANCE.sendToPlayer(new ServerTileEventPacket(this, key, value), player);
   }

   public final void sendToServer(String id, INetworkDataBuffer buffer) {
      this.network.sendClientTileDataBufferEvent(this, id, buffer);
   }

   public final void sendToClient(String id, INetworkDataBuffer buffer, PacketRange range) {
      this.network.sendTileDataBufferEvent(this, id, buffer, range);
   }

   public final void sendToPlayer(Player player, String id, INetworkDataBuffer buffer) {
      PacketManager.INSTANCE.sendToPlayer(new TileDataBufferEventPacket(this, id, buffer, true), player);
   }

   public final void updateGuiField(String name) {
      this.network.updateGuiField(this, name);
   }

   public final void updateGuiFields(String... names) {
      this.network.updateGuiFields(this, names);
   }

   public final void updateTileField(String name) {
      this.network.updateTileField(this, name);
   }

   public final void updateTileFields(String... names) {
      this.network.updateTileFields(this, names);
   }

   public final boolean isLoaded() {
      return this.loaded;
   }

   public BlockEntityType<?> getType() {
      return this.type == null ? this.createType() : this.type;
   }

   public final void lock() {
      this.locked = true;
   }

   public final void unlock() {
      this.locked = false;
   }

   public final void onStateChanged() {
      if (!this.locked && !this.isRendering()) {
         Block block = this.getBlockState().getBlock();
         if (block instanceof IStateController) {
            ((IStateController)block).onStateUpdate(this.getLevel(), this.getBlockPos(), this.getBlockState(), this);
         }
      }
   }

   public boolean needsUpdateTick() {
      return this.isSimulating();
   }

   public boolean shouldBlockUpdateEnableTick() {
      return false;
   }

   @Override
   public ComparatorManager getManager() {
      return this.comparators;
   }

   protected void disableProfiling() {
      this.profiler = null;
   }

   @Override
   public void onProfile(long time) {
      if (this.profiler == null) {
         this.profiler = new LongAverager(20);
      }

      this.profiler.addEntry(time);
   }

   @Override
   public long getLag() {
      return this.profiler == null ? 0L : this.profiler.getCachedAverage();
   }

   @Override
   public MutableComponent showResults() {
      if (this.profiler != null) {
         long time = this.profiler.getAverage();
         return this.translate(
            "misc.ic2.tick.lag",
            new Object[]{
               Formatters.EU_READER_FORMAT.format(time / 1000000L), Formatters.EU_READER_FORMAT.format(time / 1000L), Formatters.EU_READER_FORMAT.format(time)
            }
         );
      } else {
         return this.translate("misc.ic2.tick.disabled");
      }
   }

   public boolean hasCustomName() {
      return Strings.isNotEmpty(this.customName);
   }

   @Nonnull
   public Component getCustomName() {
      return this.string(this.customName);
   }

   @Nonnull
   public Component getName() {
      return this.translate(this.getBlockState().getBlock().getDescriptionId());
   }

   @Nonnull
   public Level getLevel() {
      return this.level;
   }

   public void addNetworkFields(String... fields) {
      this.networkFields.addAll(ObjectArrayList.wrap(fields));
   }

   public void addGuiFields(String... fields) {
      this.guiFields.addAll(ObjectArrayList.wrap(fields));
   }

   protected void clearNetworkFields() {
      this.networkFields.clear();
   }

   protected void clearGuiFields() {
      this.guiFields.clear();
   }

   @Override
   public List<String> getNetworkFields() {
      return this.networkFields;
   }

   @Override
   public List<String> getGuiFields() {
      return this.guiFields;
   }

   @Override
   public boolean isDefaultData(String fieldName) {
      if (fieldName.equals("isActive")) {
         return !this.isActive;
      } else if (fieldName.equals("facing")) {
         return this.facing == Direction.NORTH.get3DDataValue();
      } else {
         return fieldName.equals("customName") ? Strings.isEmpty(this.customName) : false;
      }
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
   }

   @Override
   public void onGuiFieldChanged(Set<String> fields, Player player) {
   }

   public void addCapability(Capability<?> cap, Object value) {
      this.capabilities.put(cap, LazyOptional.of(() -> value));
   }

   public void refreshCapability(Capability<?> cap, Object value) {
      this.removeCapability(cap);
      this.addCapability(cap, value);
   }

   public void removeCapability(Capability<?> cap) {
      LazyOptional<?> value = this.capabilities.remove(cap);
      if (value != null) {
         value.invalidate();
      }
   }

   public <T> T getInternalCapability(Capability<T> cap) {
      LazyOptional<?> optional = this.capabilities.get(cap);
      return (T)(optional == null ? null : optional.orElseGet(null));
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      LazyOptional<?> cached = this.capabilities.get(cap);
      return cached != null ? cached.cast() : super.getCapability(cap, side);
   }

   public void addCaches(ICache<?>... cache) {
      this.caches.addAll(ObjectArrayList.wrap(cache));

      for (ICache<?> entry : cache) {
         entry.setCallback(this::recache);
      }
   }

   public boolean isActive() {
      return this.isActive;
   }

   public boolean setActive(boolean isActive) {
      if (this.isActive != isActive) {
         this.isActive = isActive;
         this.updateTileField("isActive");
         this.onStateChanged();
         return true;
      } else {
         return false;
      }
   }

   public void setActiveSilent(boolean isActive) {
      this.isActive = isActive;
   }

   public Direction getFacing() {
      return Direction.from3DDataValue(this.facing);
   }

   public void setFacing(Direction facing) {
      if (facing != null) {
         if (this.facing != facing.get3DDataValue()) {
            this.facing = facing.get3DDataValue();
            this.updateTileField("facing");
            this.onStateChanged();
         }
      }
   }

   public void setFacingSilent(Direction facing) {
      if (facing != null) {
         this.facing = facing.get3DDataValue();
      }
   }

   public Component setCustomName(Component comp) {
      if (comp == null) {
         this.customName = "";
      } else {
         this.customName = comp.getString();
      }

      this.updateTileField("customName");
      return this.getCustomName();
   }

   public CompoundTag getUpdateTag() {
      CompoundTag nbt = super.getUpdateTag();
      IC2.NETWORKING.get(this.isSimulating()).sendInitialData(this, nbt);
      return nbt;
   }

   public void handleUpdateTag(CompoundTag tag) {
      super.handleUpdateTag(tag);
      IC2.NETWORKING.get(this.isSimulating()).handleInitialChange(this, tag);
   }

   public void load(CompoundTag compound) {
      super.load(compound);
      this.isActive = compound.getBoolean("active");
      this.facing = NBTUtils.getInt(compound, "facing", Direction.NORTH.get3DDataValue());
      this.customName = NBTUtils.getString(compound, "customName", "");
      this.comparators.load(compound);
   }

   protected void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.removeIfEmpty(compound, "ForgeData");
      NBTUtils.removeIfEmpty(compound, "ForgeCaps");
      NBTUtils.putBoolean(compound, "active", this.isActive, false);
      NBTUtils.putByte(compound, "facing", this.facing, Direction.NORTH.get3DDataValue());
      NBTUtils.putString(compound, "customName", this.customName, "");
      this.comparators.save(compound);
   }

   public void setRemoved() {
      if (this.loaded) {
         this.onUnloaded(false);
      }

      super.setRemoved();
   }

   public void onChunkUnloaded() {
      if (this.loaded) {
         this.onUnloaded(true);
      }

      super.onChunkUnloaded();
   }

   public boolean checkTicking() {
      if (this.needsUpdateTick() && this.isTicking) {
         return false;
      } else {
         ((ChunkMixin)this.getLevel().getChunkAt(this.getBlockPos())).removeTileEntity(this.getBlockPos());
         this.isTicking = false;
         return true;
      }
   }

   public void onLoad() {
      super.onLoad();
      if (!this.loaded) {
         if (this.isSimulating()) {
            this.comparators.onLoaded();
            IC2.TICK_HANDLER.addWorldCallback(this.getLevel(), world -> {
               if (this.isRemoved()) {
                  return 0;
               } else {
                  this.onLoaded();
                  return 0;
               }
            });
         } else {
            this.onLoaded();
         }
      }
   }

   public void onLoaded() {
      this.loaded = true;
      if (this.isSimulating()) {
         for (ICache<?> entry : this.caches) {
            entry.update();
         }

         this.onCachesUpdated();
         if (this instanceof IMachine) {
            ((IMachine)this).onUpgradesChanged();
         }

         ChunkSaver.getSaver(this.getLevel()).addPosition(this.getBlockPos());
      }
   }

   public void onUnloaded(boolean chunk) {
      this.loaded = false;

      for (LazyOptional<?> entry : this.capabilities.values()) {
         entry.invalidate();
      }

      this.capabilities.clear();
      this.caches.clear();
      if (this.isSimulating()) {
         ChunkSaver.getSaver(this.getLevel()).removePosition(this.getBlockPos());
      }
   }

   protected boolean needsInitialRedstoneCheck() {
      return false;
   }

   public void onBlockUpdate(Block block, BlockPos from) {
      this.needsRedstoneUpdate = this.sensitive;
      if (this.isSimulating() && !this.caches.isEmpty()) {
         for (ICache<?> cache : this.caches) {
            cache.update();
         }

         this.onCachesUpdated();
      }

      if (this.shouldBlockUpdateEnableTick()) {
         this.addToTick();
      }
   }

   public void onComparatorUpdate(BlockPos from) {
   }

   protected void onCachesUpdated() {
   }

   protected final void handleNonTickComparators() {
      if (!this.isTicking) {
         this.handleComparators(true);
      }
   }

   protected final void handleComparators() {
      this.handleComparators(false);
   }

   protected boolean handleComparators(boolean ignoreTick) {
      boolean result = this.comparators.update(ignoreTick);
      if (this.comparators.shouldSync()) {
         this.updateGuiField("comparators");
      }

      return result;
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      int baseId = 65534;
      if (key >= baseId && key <= baseId + 4) {
         key -= baseId;
         switch (key) {
            case 0:
               this.comparators.setSync(value == 1);
               if (value == 1) {
                  this.updateGuiField("comparators");
               }
               break;
            case 1:
               this.comparators.add(value >> 3, value & 7);
               this.updateGuiField("comparators");
               break;
            case 2:
               this.comparators.remove(value >> 3, value & 7);
               this.updateGuiField("comparators");
               break;
            case 3:
               this.comparators.setFlag(value >> 5, value & 7, value >> 3 & 3);
               this.updateGuiField("comparators");
               break;
            case 4:
               this.comparators.flipMax(value);
               this.updateGuiField("comparators");
         }
      }
   }

   public void handleRedstone() {
      if (this.needsRedstoneUpdate && this.sensitive) {
         this.needsRedstoneUpdate = false;
         if (this.directionalSignal) {
            byte max = 0;

            for (Direction dir : DirectionList.ALL) {
               byte strength = (byte)Mth.clamp(this.getLevel().getSignal(this.getBlockPos(), dir), 0, 15);
               max = (byte)Math.max(max, strength);
               this.sidedSignals[dir.get3DDataValue()] = strength;
            }

            this.signal = max;
            return;
         }

         this.signal = (byte)Mth.clamp(this.getLevel().getBestNeighborSignal(this.worldPosition), 0, 15);
      }
   }

   public int getRedstoneStrength() {
      if (this.sensitive) {
         return this.inverted ? 15 - this.signal : this.signal;
      } else {
         return this.inverted ? 15 : 0;
      }
   }

   public int getSidedRedstoneStrength(Direction dir) {
      if (this.sensitive) {
         return this.inverted ? 15 - this.sidedSignals[dir.get3DDataValue()] : this.sidedSignals[dir.get3DDataValue()];
      } else {
         return this.inverted ? 15 : 0;
      }
   }

   public boolean isRedstonePowered() {
      return this.getRedstoneStrength() > 0;
   }

   public boolean isSideRedstonePowered(Direction dir) {
      return this.getSidedRedstoneStrength(dir) > 0;
   }

   public final long clockTime(int value) {
      return Math.abs(this.getLevel().getGameTime() + this.position) % (long)value;
   }

   public final boolean clock(int value) {
      return Math.abs(this.getLevel().getGameTime() + this.position) % (long)value == 0L;
   }

   public final boolean invClock(int value) {
      return Math.abs(this.getLevel().getGameTime() + this.position) % (long)value != 0L;
   }

   public final boolean isAreaLoaded(int radius) {
      return this.getLevel().isAreaLoaded(this.worldPosition, radius);
   }

   public final boolean isAreaLoaded(BlockPos pos, int radius) {
      return this.getLevel().isAreaLoaded(pos, radius);
   }

   public final boolean setState(BlockState state) {
      return state != this.getBlockState() && this.getLevel().isLoaded(this.worldPosition) && this.getLevel().setBlockAndUpdate(this.worldPosition, state);
   }

   public final boolean setState(BlockState state, int flags) {
      return state != this.getBlockState() && this.getLevel().isLoaded(this.worldPosition) && this.getLevel().setBlock(this.worldPosition, state, flags);
   }

   public final <T extends Comparable<T>> boolean withState(Property<T> key, T value) {
      return this.setState((BlockState)this.getBlockState().setValue(key, value));
   }

   public final <T extends Comparable<T>> boolean withState(Property<T> key, T value, int flags) {
      return this.setState((BlockState)this.getBlockState().setValue(key, value), flags);
   }

   public final <T extends Comparable<T>> T getValue(Property<T> prop) {
      return (T)this.getBlockState().getValue(prop);
   }

   public final <T extends Comparable<T>> T getValue(Property<T> prop, T defaultValue) {
      return (T)(this.getBlockState().hasProperty(prop) ? this.getBlockState().getValue(prop) : defaultValue);
   }

   public void notifyChanges(boolean self, DirectionList sides, BlockPos blockPos) {
      notifyChanges(self, this.getLevel(), this.getBlockPos(), this.getBlockState(), blockPos, sides);
   }

   public void notifyChanges(boolean self, DirectionList sides) {
      notifyChanges(self, this.getLevel(), this.getBlockPos(), this.getBlockState(), this.getBlockPos(), sides);
   }

   public static void notifyChanges(boolean self, Level level, BlockPos pos, BlockState state, BlockPos sourcePos, DirectionList sides) {
      if (self) {
         ForgeEventFactory.onNeighborNotify(level, sourcePos, level.getBlockState(sourcePos), EnumSet.noneOf(Direction.class), false);
         level.neighborChanged(sourcePos, state.getBlock(), pos);
      }

      for (Direction dir : sides) {
         BlockPos mainOffset = sourcePos.relative(dir);
         if (level.isLoaded(mainOffset)) {
            ForgeEventFactory.onNeighborNotify(level, mainOffset, level.getBlockState(mainOffset), EnumSet.noneOf(Direction.class), false);
            level.neighborChanged(mainOffset, state.getBlock(), pos);

            for (Direction side : DirectionList.ALL.remove(dir.getOpposite())) {
               BlockPos subOffset = mainOffset.relative(side);
               if (level.isLoaded(subOffset)) {
                  ForgeEventFactory.onNeighborNotify(level, subOffset, level.getBlockState(subOffset), EnumSet.noneOf(Direction.class), false);
                  level.neighborChanged(subOffset, state.getBlock(), pos);
               }
            }
         }
      }
   }

   protected boolean isTicking() {
      return this.isTicking;
   }

   protected boolean isRemoving() {
      return this.removing;
   }

   protected boolean isAdding() {
      return this.adding;
   }

   protected void recache() {
      if (!this.recache && !this.isRendering() && this.loaded) {
         this.recache = true;
         IC2.TICK_HANDLER.addWorldCallback(this.getLevel(), T -> {
            this.recache = false;
            if (!this.loaded) {
               return 0;
            } else {
               for (ICache<?> cache : this.caches) {
                  cache.update();
               }

               this.onCachesUpdated();
               return 0;
            }
         });
      }
   }

   protected void addToTick() {
      if (this.isSimulating() && !this.adding && !this.isTicking) {
         this.adding = true;
         IC2.TICK_HANDLER.addWorldCallback(this.getLevel(), T -> {
            ((ChunkMixin)this.getLevel().getChunkAt(this.getBlockPos())).updateTileEntity(this);
            this.isTicking = true;
            this.adding = false;
            return 0;
         });
      }
   }

   protected void removeFromTick() {
      if (this.isSimulating() && !this.removing && this.isTicking) {
         this.removing = true;
         IC2.TICK_HANDLER.addWorldCallback(this.getLevel(), T -> {
            ((ChunkMixin)this.getLevel().getChunkAt(this.getBlockPos())).removeTileEntity(this.getBlockPos());
            this.disableProfiling();
            this.isTicking = false;
            this.removing = false;
            return 0;
         });
      }
   }
}
