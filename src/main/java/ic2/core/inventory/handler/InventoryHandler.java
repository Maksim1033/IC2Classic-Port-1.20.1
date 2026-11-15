package ic2.core.inventory.handler;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.handler.filter.ISlotFilter;
import ic2.core.inventory.handler.filter.WrapperSlotFilter;
import ic2.core.inventory.handler.slots.ListModularSlot;
import ic2.core.inventory.inv.ListenerInventory;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.slot.SlotBase;
import ic2.core.item.base.features.IHandlerItem;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.capabilities.IToggleableCapabilityProvider;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.ints.Int2ByteMaps;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.Arrays;
import java.util.function.IntPredicate;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class InventoryHandler implements INetworkDataBuffer, IToggleableCapabilityProvider, IHandlerItem.IConfigurableInventory {
   IHasInventory owner;
   Int2ObjectMap<SlotType> allSlots = new Int2ObjectLinkedOpenHashMap();
   Object2ObjectMap<SlotType, IntList> namedSlots = CollectionUtils.createMap();
   boolean active = true;
   byte blockSides = 0;
   short blockAccess = 0;
   Int2ShortMap slotAccess = new Int2ShortOpenHashMap();
   IModularSlot[] modularSlots = new IModularSlot[6];
   IntList[] sideToSlot = CollectionUtils.createIntLists(6);
   Int2ByteMap slotToSide = new Int2ByteOpenHashMap();
   Int2ObjectMap<ISlotFilter> inputFilters = new Int2ObjectOpenHashMap();
   Int2ObjectMap<ISlotFilter> outputFilters = new Int2ObjectOpenHashMap();
   LazyOptional<IItemHandler>[] inventories = new LazyOptional[7];
   Int2IntMap defaultSlotValues = new Int2IntOpenHashMap();
   int defaultMachineValues = 0;
   boolean singleItem = false;
   boolean singleStack = false;
   boolean locked = false;
   SimpleInventory inv = new ListenerInventory(5, (K, V) -> this.syncFilters()).setMaxStackSize(1);
   Object2IntMap<Item> mappedItem = new Object2IntLinkedOpenHashMap();

   public InventoryHandler(IHasInventory inv) {
      this.mappedItem.defaultReturnValue(-1);
      this.owner = inv;
   }

   public void registerNamedSlot(SlotType type, int... slots) {
      IntList list = IntArrayList.wrap(slots);
      this.namedSlots.put(type, list);

      for (int slot : slots) {
         this.allSlots.put(slot, type);
      }
   }

   public void registerInvisibleSlot(SlotType type, int... slots) {
      this.namedSlots.put(type, IntArrayList.wrap(slots));
   }

   public void registerInputFilter(IFilter filter, int... slots) {
      this.registerInputFilter(new WrapperSlotFilter(filter), slots);
   }

   public void registerInputFilter(ISlotFilter filter, int... slots) {
      for (int slot : slots) {
         this.inputFilters.put(slot, filter);
      }
   }

   public void registerOutputFilter(IFilter filter, int... slots) {
      this.registerOutputFilter(new WrapperSlotFilter(filter), slots);
   }

   public void registerOutputFilter(ISlotFilter filter, int... slots) {
      for (int slot : slots) {
         this.outputFilters.put(slot, filter);
      }
   }

   public void registerBlockSides(DirectionList rotation) {
      this.blockSides = (byte)rotation.getCode();
   }

   public void registerBlockAccess(DirectionList rotation, AccessRule rule) {
      for (Direction dir : rotation) {
         this.blockAccess = this.setValue(dir.get3DDataValue() * 2, rule.getIndex(), this.blockAccess);
      }
   }

   public void registerCustomSlot(IModularSlot slot) {
      for (Direction dir : DirectionList.ALL) {
         this.modularSlots[dir.get3DDataValue()] = slot;
         IntListIterator var4 = this.sideToSlot[dir.get3DDataValue()].iterator();

         while (var4.hasNext()) {
            int entry = (Integer)var4.next();
            this.slotToSide.remove(entry);
         }

         this.sideToSlot[dir.get3DDataValue()].clear();
      }
   }

   public void registerSlotsForSide(DirectionList rotation, int... slots) {
      IntList toAdd = IntArrayList.wrap(slots);

      for (Direction dir : rotation) {
         if (this.modularSlots[dir.get3DDataValue()] == null) {
            this.sideToSlot[dir.get3DDataValue()].addAll(toAdd);
         }
      }

      for (int i : slots) {
         this.slotToSide.put(i, (byte)DirectionList.ofNumber(this.slotToSide.get(i)).add(rotation).getCode());
      }
   }

   public void registerSlotAccess(AccessRule rule, int... slots) {
      for (int slot : slots) {
         short data = this.slotAccess.get(slot);

         for (Direction dir : DirectionList.ALL) {
            data = this.setValue(dir.get3DDataValue() * 2, rule.getIndex(), data);
         }

         this.slotAccess.put(slot, data);
      }
   }

   public void setSlotSide(int slot, DirectionList validSides) {
      for (Direction dir : DirectionList.ofNumber(this.slotToSide.get(slot))) {
         this.sideToSlot[dir.get3DDataValue()].rem(slot);
      }

      this.slotToSide.put(slot, (byte)validSides.getCode());

      for (Direction dir : validSides) {
         this.sideToSlot[dir.get3DDataValue()].add(slot);
      }

      for (IntList integers : this.sideToSlot) {
         integers.removeIf(InventoryHandler.AntiSlotDuplicator.use());
         integers.sort(null);
      }
   }

   public void setSlotAccess(int slot, Direction dir, AccessRule rule) {
      this.slotAccess.put(slot, this.setValue(dir.get3DDataValue() * 2, rule.getIndex(), this.slotAccess.get(slot)));
   }

   public void setBlockAccess(Direction dir, AccessRule rule) {
      this.blockAccess = this.setValue(dir.get3DDataValue() * 2, rule.getIndex(), this.blockAccess);
   }

   public void remove() {
      for (int i = 0; i < 7; i++) {
         LazyOptional<IItemHandler> handler = this.inventories[i];
         if (handler.isPresent()) {
            handler.invalidate();
         }

         this.inventories[i] = null;
      }
   }

   public void validateSlots() {
      for (IntList integers : this.sideToSlot) {
         integers.removeIf(InventoryHandler.AntiSlotDuplicator.use());
         integers.sort(null);
      }

      for (Direction dir : DirectionList.ALL) {
         if ((this.blockSides & 1 << dir.get3DDataValue()) != 0 && this.active) {
            if (this.modularSlots[dir.get3DDataValue()] != null) {
               this.inventories[dir.get3DDataValue()] = LazyOptional.of(() -> new BasicInventory(this, dir, this.modularSlots[dir.get3DDataValue()]));
            } else {
               this.inventories[dir.get3DDataValue()] = LazyOptional.of(
                  () -> new BasicInventory(this, dir, new ListModularSlot(this.sideToSlot[dir.get3DDataValue()]))
               );
            }
         } else {
            this.inventories[dir.get3DDataValue()] = LazyOptional.empty();
         }
      }

      this.inventories[6] = LazyOptional.of(() -> new InternalInventory(this));
      this.defaultMachineValues = MathUtils.putInt(this.blockAccess, this.blockSides);
      ObjectIterator var6 = this.slotToSide.int2ByteEntrySet().iterator();

      while (var6.hasNext()) {
         it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry entry = (it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry)var6.next();
         this.defaultSlotValues.put(entry.getIntKey(), MathUtils.putInt(this.slotAccess.get(entry.getIntKey()), entry.getByteValue()));
      }
   }

   protected void syncInventories() {
      for (Direction dir : DirectionList.ALL) {
         if ((this.blockSides & 1 << dir.get3DDataValue()) != 0 && this.active) {
            if (!this.inventories[dir.get3DDataValue()].isPresent()) {
               if (this.modularSlots[dir.get3DDataValue()] != null) {
                  this.inventories[dir.get3DDataValue()] = LazyOptional.of(() -> new BasicInventory(this, dir, this.modularSlots[dir.get3DDataValue()]));
               } else {
                  this.inventories[dir.get3DDataValue()] = LazyOptional.of(
                     () -> new BasicInventory(this, dir, new ListModularSlot(this.sideToSlot[dir.get3DDataValue()]))
                  );
               }
            }
         } else if (this.inventories[dir.get3DDataValue()].isPresent()) {
            this.inventories[dir.get3DDataValue()].invalidate();
            this.inventories[dir.get3DDataValue()] = LazyOptional.empty();
         }
      }
   }

   protected void syncFilters() {
      if (!IC2.PLATFORM.isRendering()) {
         this.mappedItem.defaultReturnValue(-1);
         this.mappedItem.clear();
         int i = 0;

         for (int m = this.inv.getSlotCount(); i < m; i++) {
            ItemStack stack = this.inv.getStackInSlot(i);
            if (stack.getItem() instanceof IHandlerItem) {
               ((IHandlerItem)stack.getItem()).handleInventory(stack, this);
            }
         }
      }
   }

   @Override
   public void setDefaultMaxStackSize(int max) {
      this.mappedItem.defaultReturnValue(max);
   }

   @Override
   public void setMaxStackSizeForItem(Item item, int max) {
      this.mappedItem.put(item, max);
   }

   public IHasInventory getInventory() {
      return this.owner;
   }

   public IHasInventory getFilters() {
      return this.inv;
   }

   public ISlotFilter getInputFilter(int slot) {
      return (ISlotFilter)this.inputFilters.get(slot);
   }

   ISlotFilter getOutputFilter(int slot) {
      return (ISlotFilter)this.outputFilters.get(slot);
   }

   int getMaxStackSize(Item item) {
      return this.mappedItem.getInt(item);
   }

   public void reset() {
      this.allSlots.clear();
      this.namedSlots.clear();
      this.inputFilters.clear();
      this.outputFilters.clear();
      this.slotToSide.clear();
      this.blockAccess = 0;
      this.blockSides = 0;
      Arrays.fill(this.modularSlots, null);

      for (IntList list : this.sideToSlot) {
         list.clear();
      }

      this.defaultSlotValues.clear();
   }

   public CompoundTag save(CompoundTag nbt) {
      NBTUtils.putBoolean(nbt, "locked", this.locked, false);
      NBTUtils.putByte(nbt, "side", this.blockSides, MathUtils.getShortValue(this.defaultMachineValues));
      NBTUtils.putShort(nbt, "access", this.blockAccess, MathUtils.getShortKey(this.defaultMachineValues));
      NBTUtils.putByte(nbt, "flags", (byte)((this.singleItem ? 1 : 0) | (this.singleStack ? 2 : 0)), 0);
      LongList data = new LongArrayList();
      ObjectIterator var3 = this.slotToSide.int2ByteEntrySet().iterator();

      while (var3.hasNext()) {
         it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry entry = (it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry)var3.next();
         int slot = entry.getIntKey();
         int sides = entry.getByteValue();
         int access = this.slotAccess.get(slot);
         int slotData = this.defaultSlotValues.get(slot);
         if (access != MathUtils.getShortKey(slotData) || sides != MathUtils.getShortValue(slotData)) {
            data.add(MathUtils.putIntoLong((long)MathUtils.putInt(slot, access), (long)sides));
         }
      }

      NBTUtils.putLongArray(nbt, "slot_data", data);
      NBTUtils.put(nbt, "inv", this.inv.save(new CompoundTag()));
      return nbt;
   }

   public void load(CompoundTag nbt) {
      if (!nbt.isEmpty()) {
         this.locked = nbt.getBoolean("locked");
         this.blockSides = NBTUtils.getByte(nbt, "side", (byte)MathUtils.getShortValue(this.defaultMachineValues));
         this.blockAccess = NBTUtils.getShort(nbt, "access", (short)((byte)MathUtils.getShortKey(this.defaultMachineValues)));
         int flags = nbt.getInt("flags");
         this.singleItem = (flags & 1) != 0;
         this.singleStack = (flags & 2) != 0;
         long[] data = nbt.getLongArray("slot_data");

         for (long datum : data) {
            int value = MathUtils.getIntKey(datum);
            short slot = (short)MathUtils.getShortKey(value);
            if (this.allSlots.containsKey(slot)) {
               this.slotAccess.put(slot, (short)MathUtils.getShortValue(value));
               this.slotToSide.put(slot, (byte)MathUtils.getIntValue(datum));
            }
         }

         for (IntList list : this.sideToSlot) {
            list.clear();
         }

         ObjectIterator var12 = Int2ByteMaps.fastIterable(this.slotToSide).iterator();

         while (var12.hasNext()) {
            it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry entry = (it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry)var12.next();
            int slot = entry.getIntKey();

            for (Direction side : DirectionList.ofNumber(entry.getByteValue())) {
               if (this.modularSlots[side.get3DDataValue()] == null) {
                  this.sideToSlot[side.get3DDataValue()].add(slot);
               }
            }
         }

         for (IntList integers : this.sideToSlot) {
            integers.removeIf(InventoryHandler.AntiSlotDuplicator.use());
            integers.sort(null);
         }

         this.inv.load(nbt.getCompound("inv"));
         this.syncInventories();
         this.syncFilters();
      }
   }

   public void saveSettings(CompoundTag tag) {
      if (!this.isLocked()) {
         NBTUtils.putByte(tag, "side", this.blockSides, MathUtils.getShortValue(this.defaultMachineValues));
         NBTUtils.putShort(tag, "access", this.blockAccess, MathUtils.getShortKey(this.defaultMachineValues));
         NBTUtils.putByte(tag, "flags", (byte)((this.singleItem ? 1 : 0) | (this.singleStack ? 2 : 0)), 0);
         LongList data = new LongArrayList();
         ObjectIterator var3 = this.slotToSide.int2ByteEntrySet().iterator();

         while (var3.hasNext()) {
            it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry entry = (it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry)var3.next();
            int slot = entry.getIntKey();
            int sides = entry.getByteValue();
            int access = this.slotAccess.get(slot);
            int slotData = this.defaultSlotValues.get(slot);
            if (access != MathUtils.getShortKey(slotData) || sides != MathUtils.getShortValue(slotData)) {
               data.add(MathUtils.putIntoLong((long)MathUtils.putInt(slot, access), (long)sides));
            }
         }

         NBTUtils.putLongArray(tag, "slot_data", data);
      }
   }

   public void loadSettings(CompoundTag tag) {
      if (!this.isLocked()) {
         this.blockSides = NBTUtils.getByte(tag, "side", (byte)MathUtils.getShortValue(this.defaultMachineValues));
         this.blockAccess = NBTUtils.getShort(tag, "access", (short)((byte)MathUtils.getShortKey(this.defaultMachineValues)));
         int flags = tag.getInt("flags");
         this.singleItem = (flags & 1) != 0;
         this.singleStack = (flags & 2) != 0;
         long[] data = tag.getLongArray("slot_data");

         for (long datum : data) {
            int value = MathUtils.getIntKey(datum);
            short slot = (short)MathUtils.getShortKey(value);
            if (this.allSlots.containsKey(slot)) {
               this.slotAccess.put(slot, (short)MathUtils.getShortValue(value));
               this.slotToSide.put(slot, (byte)MathUtils.getIntValue(datum));
            }
         }

         for (IntList list : this.sideToSlot) {
            list.clear();
         }

         ObjectIterator var12 = Int2ByteMaps.fastIterable(this.slotToSide).iterator();

         while (var12.hasNext()) {
            it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry entry = (it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry)var12.next();
            int slot = entry.getIntKey();

            for (Direction side : DirectionList.ofNumber(entry.getByteValue())) {
               if (this.modularSlots[side.get3DDataValue()] == null) {
                  this.sideToSlot[side.get3DDataValue()].add(slot);
               }
            }
         }

         for (IntList integers : this.sideToSlot) {
            integers.removeIf(InventoryHandler.AntiSlotDuplicator.use());
            integers.sort(null);
         }

         this.syncInventories();
         this.syncFilters();
      }
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeByte(this.blockSides);
      buffer.writeShort(this.blockAccess);
      buffer.writeByte((byte)((this.singleItem ? 1 : 0) | (this.singleStack ? 2 : 0) | (this.locked ? 4 : 0)));
      buffer.writeShort((short)this.slotToSide.size());
      ObjectIterator var2 = this.slotToSide.int2ByteEntrySet().iterator();

      while (var2.hasNext()) {
         it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry entry = (it.unimi.dsi.fastutil.ints.Int2ByteMap.Entry)var2.next();
         buffer.writeShort((short)entry.getIntKey());
         buffer.writeByte(entry.getByteValue());
         buffer.writeShort(this.slotAccess.get(entry.getIntKey()));
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.blockSides = buffer.readByte();
      this.blockAccess = buffer.readShort();
      int flags = buffer.readByte();
      this.singleItem = (flags & 1) != 0;
      this.singleStack = (flags & 2) != 0;
      this.locked = (flags & 4) != 0;
      int size = buffer.readShort();

      for (int i = 0; i < size; i++) {
         int slot = buffer.readShort();
         this.slotToSide.put(slot, buffer.readByte());
         this.slotAccess.put(slot, buffer.readShort());
      }
   }

   public LazyOptional<IItemHandler> getHandler(Direction dir) {
      if (this.owner.getSlotCount() > 0 && (dir == null || (this.blockSides & 1 << dir.get3DDataValue()) != 0)) {
         LazyOptional<IItemHandler> handler = this.inventories[dir == null ? 6 : dir.get3DDataValue()];
         return handler == null ? LazyOptional.empty() : handler.cast();
      } else {
         return LazyOptional.empty();
      }
   }

   public boolean containsSlot(Slot slot) {
      return !(slot instanceof SlotBase)
         ? false
         : ((SlotBase)slot).getInventory() == this.owner
            && this.allSlots.containsKey(slot.getSlotIndex())
            && (this.modularSlots[0] == null || !this.modularSlots[0].containsSlot(slot.getSlotIndex()));
   }

   public SlotType getSlotType(int slot) {
      return (SlotType)this.allSlots.get(slot);
   }

   public IntList getAllSlots() {
      return new IntArrayList(this.allSlots.keySet());
   }

   public Object2ObjectMap<SlotType, IntList> getNamedSlots() {
      return Object2ObjectMaps.unmodifiable(this.namedSlots);
   }

   public IntList getAllOfType(SlotType type) {
      IntList list = new IntArrayList();
      ObjectIterator var3 = Object2ObjectMaps.fastIterable(this.namedSlots).iterator();

      while (var3.hasNext()) {
         Entry<SlotType, IntList> entry = (Entry<SlotType, IntList>)var3.next();
         if (((SlotType)entry.getKey()).isParent(type)) {
            list.addAll((IntList)entry.getValue());
         }
      }

      return list;
   }

   public Object2ObjectMap<SlotType, IntList> getSlotsOfType(SlotType type) {
      Object2ObjectMap<SlotType, IntList> result = new Object2ObjectLinkedOpenHashMap();
      ObjectIterator var3 = Object2ObjectMaps.fastIterable(this.namedSlots).iterator();

      while (var3.hasNext()) {
         Entry<SlotType, IntList> entry = (Entry<SlotType, IntList>)var3.next();
         if (((SlotType)entry.getKey()).isParent(type)) {
            result.put((SlotType)entry.getKey(), (IntList)entry.getValue());
         }
      }

      return result;
   }

   public AccessRule getSlotAccess(int slot, Direction dir) {
      return AccessRule.byIndex(this.slotAccess.get(slot) >> dir.get3DDataValue() * 2 & 3);
   }

   public boolean isSlotSideUnlocked(int slot, Direction dir) {
      return this.modularSlots[dir.get3DDataValue()] != null
         ? this.modularSlots[dir.get3DDataValue()].containsSlot(slot)
         : (this.slotToSide.get(slot) & 1 << dir.get3DDataValue()) != 0;
   }

   public AccessRule getBlockAccess(Direction dir) {
      return AccessRule.byIndex(this.blockAccess >> dir.get3DDataValue() * 2 & 3);
   }

   public boolean isBlockSideUnlocked(Direction dir) {
      return (this.blockSides & 1 << dir.get3DDataValue()) != 0;
   }

   public boolean areAllSidesBlocked() {
      return this.blockSides == 0;
   }

   public void toggleMachineSide(Direction dir) {
      if (!this.locked) {
         this.blockSides = (byte)(this.blockSides ^ (byte)(1 << dir.get3DDataValue()));
         this.syncInventories();
      }
   }

   public void toggleMachineAccess(Direction dir) {
      if (!this.locked) {
         this.blockAccess = this.setValue(
            dir.get3DDataValue() * 2,
            AccessRule.byIndex(this.blockAccess >> dir.get3DDataValue() * 2 & 3)
               .cycleRule(AccessRule.byIndex(MathUtils.getShortKey(this.defaultMachineValues) >> dir.get3DDataValue() * 2 & 3))
               .getIndex(),
            this.blockAccess
         );
      }
   }

   public void resetMachineData() {
      if (!this.locked) {
         this.blockSides = (byte)MathUtils.getShortValue(this.defaultMachineValues);
         this.blockAccess = (short)MathUtils.getShortKey(this.defaultMachineValues);
      }
   }

   public void toggleSlotSide(int slot, Direction dir) {
      if (!this.locked) {
         this.setSlotSide(slot, DirectionList.ofNumber(this.slotToSide.get(slot) ^ 1 << dir.get3DDataValue()));
      }
   }

   public void toggleSlotAccess(int slot, Direction dir) {
      if (!this.locked) {
         short data = this.slotAccess.get(slot);
         this.slotAccess
            .put(
               slot,
               this.setValue(
                  dir.get3DDataValue() * 2,
                  AccessRule.byIndex(data >> dir.get3DDataValue() * 2 & 3)
                     .cycleRule(AccessRule.byIndex(MathUtils.getShortKey(this.defaultSlotValues.get(slot)) >> dir.get3DDataValue() * 2 & 3))
                     .getIndex(),
                  data
               )
            );
      }
   }

   public void resetSlotData(int slot) {
      if (!this.locked) {
         int slotData = this.defaultSlotValues.get(slot);
         this.slotAccess.put(slot, (short)MathUtils.getShortKey(slotData));
         this.slotToSide.put(slot, (byte)MathUtils.getShortValue(slotData));
      }
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      return cap == ForgeCapabilities.ITEM_HANDLER ? this.getHandler(side).cast() : LazyOptional.empty();
   }

   @Override
   public boolean isActive() {
      return this.active;
   }

   @Override
   public void setActive(boolean value) {
      if (this.active != value) {
         this.active = value;
         this.syncInventories();
      }
   }

   public boolean isLocked() {
      return this.locked;
   }

   public boolean isSingleItem() {
      return this.singleItem;
   }

   public void setSingleItem(boolean value) {
      if (!this.locked) {
         this.singleItem = value;
      }
   }

   public boolean isSingleStack() {
      return this.singleStack;
   }

   public void setSingleStack(boolean value) {
      if (!this.locked) {
         this.singleStack = value;
      }
   }

   public boolean isMultiStack() {
      return this.isSingleStack() || !this.mappedItem.isEmpty() || this.mappedItem.defaultReturnValue() > -1;
   }

   public boolean isNoDuplicateMode() {
      return this.isMultiStack() || this.isSingleItem();
   }

   private short setValue(int index, int data, int original) {
      return (short)(original & ~(3 << index) | (data & 3) << index);
   }

   static class AntiSlotDuplicator implements IntPredicate {
      static final ThreadLocal<InventoryHandler.AntiSlotDuplicator> LOCALS = ThreadLocal.withInitial(InventoryHandler.AntiSlotDuplicator::new);
      IntSet set = new IntOpenHashSet();

      public static InventoryHandler.AntiSlotDuplicator use() {
         InventoryHandler.AntiSlotDuplicator local = LOCALS.get();
         local.set.clear();
         return local;
      }

      @Override
      public boolean test(int value) {
         return !this.set.add(value);
      }
   }
}
