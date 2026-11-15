package ic2.core.block.transport.item.tubes;

import ic2.api.items.ITagItem;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.ICopyableSettings;
import ic2.api.tiles.tubes.ITube;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.redstone.IRedstoneListener;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.cables.AdvancedComparatorBlock;
import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.block.transport.item.TubeAction;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.block.transport.item.container.FilterExtractionTubeContainer;
import ic2.core.inventory.base.ICamouflageGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.ArrayOrFilter;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.InvertedFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.TransferFilter;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.collection.SynchronizedList;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

public class FilteredExtractionTubeTileEntity extends TubeTileEntity implements ICamouflageGui, IRedstoneListener, ICopyableSettings {
   public static final int[] TRANSFER_RATE = new int[]{1, 2, 4, 8, 16, 24, 32, 48, 64};
   public static final int[] DELAY = new int[]{20, 16, 12, 10, 8, 6, 4, 2, 1};
   public int priorityDir = 0;
   public boolean specialConnect = false;
   public boolean pulse = false;
   public boolean comparator = false;
   public int pulseState = 0;
   public SimpleInventory filterInventory = new SimpleInventory(1);
   public SimpleInventory upgradeInventory = new SimpleInventory(2);
   protected IItemTransporter transporter = new FilteredExtractionTubeTileEntity.Handler();
   @NetworkInfo
   public DirectionList extraFacings = DirectionList.EMPTY;
   @NetworkInfo
   public SynchronizedList<FilteredExtractionTubeTileEntity.FilterEntry> filters = new SynchronizedList<>(
      FilteredExtractionTubeTileEntity.FilterEntry::write, FilteredExtractionTubeTileEntity.FilterEntry::new
   );
   @NetworkInfo
   public int flags = 4;
   @NetworkInfo
   public int durability = -1;
   @NetworkInfo
   public boolean whitelist = true;
   @NetworkInfo
   public int keepItems;
   @NetworkInfo
   public int selectedIndex = -1;
   IFilter filter = null;
   boolean useCustomFilter;

   public FilteredExtractionTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addNetworkFields(new String[]{"facing", "extraFacings"});
      this.addGuiFields(new String[]{"filters", "flags", "durability", "whitelist", "keepItems", "selectedIndex"});
      this.sensitive = false;
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      super.addDrops(drops);
      this.upgradeInventory.addToDrops(drops);
   }

   @Override
   public boolean isFoamed() {
      return this.foamed > 0;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new FilterExtractionTubeContainer(this, player, windowID);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      ListTag list = new ListTag();

      for (int i = 0; i < this.filters.size(); i++) {
         list.add(((FilteredExtractionTubeTileEntity.FilterEntry)this.filters.get(i)).save());
      }

      NBTUtils.put(compound, "filters", list);
      NBTUtils.putBoolean(compound, "whitelist", this.whitelist, false);
      NBTUtils.putByte(compound, "extra", this.extraFacings.getCode(), 0);
      NBTUtils.putBoolean(compound, "sensitive", this.sensitive, false);
      NBTUtils.put(compound, "upgrades", this.upgradeInventory.save(new CompoundTag()));
      NBTUtils.putByte(compound, "prio", this.priorityDir, 0);
      NBTUtils.putBoolean(compound, "special_connected", this.specialConnect, false);
      NBTUtils.putBoolean(compound, "pulse", this.pulse, false);
      NBTUtils.putByte(compound, "pulse_state", this.pulseState, 0);
      NBTUtils.putBoolean(compound, "comparator", this.comparator, false);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);

      for (CompoundTag nbt : NBTListWrapper.wrap(compound.getList("filters", 10), CompoundTag.class)) {
         FilteredExtractionTubeTileEntity.FilterEntry entry = FilteredExtractionTubeTileEntity.FilterEntry.read(nbt);
         if (entry != null) {
            this.filters.add(entry);
         }
      }

      this.whitelist = compound.getBoolean("whitelist");
      this.extraFacings = DirectionList.ofNumber(compound.getInt("extra"));
      this.upgradeInventory.load(compound.getCompound("upgrades"));
      this.sensitive = compound.getBoolean("sensitive");
      this.priorityDir = NBTUtils.getInt(compound, "prio", 0);
      this.specialConnect = compound.getBoolean("special_connected");
      this.pulse = compound.getBoolean("pulse");
      this.pulseState = compound.getInt("pulse_state");
      this.comparator = compound.getBoolean("comparator");
   }

   @Override
   public void saveSettings(CompoundTag compound) {
      ListTag list = new ListTag();

      for (int i = 0; i < this.filters.size(); i++) {
         list.add(((FilteredExtractionTubeTileEntity.FilterEntry)this.filters.get(i)).save());
      }

      NBTUtils.put(compound, "filters", list);
      NBTUtils.putBoolean(compound, "whitelist", this.whitelist, false);
      NBTUtils.putByte(compound, "extra", this.extraFacings.getCode(), 0);
      NBTUtils.putBoolean(compound, "sensitive", this.sensitive, false);
      NBTUtils.putByte(compound, "prio", this.priorityDir, 0);
      NBTUtils.putBoolean(compound, "special_connected", this.specialConnect, false);
      NBTUtils.putBoolean(compound, "pulse", this.pulse, false);
      NBTUtils.putByte(compound, "pulse_state", this.pulseState, 0);
      NBTUtils.putBoolean(compound, "comparator", this.comparator, false);
   }

   @Override
   public void loadSettings(CompoundTag compound) {
      this.filters.clear();

      for (CompoundTag nbt : NBTListWrapper.wrap(compound.getList("filters", 10), CompoundTag.class)) {
         FilteredExtractionTubeTileEntity.FilterEntry entry = FilteredExtractionTubeTileEntity.FilterEntry.read(nbt);
         if (entry != null) {
            this.filters.add(entry);
         }
      }

      this.whitelist = compound.getBoolean("whitelist");
      this.extraFacings = DirectionList.ofNumber(compound.getInt("extra"));
      this.sensitive = compound.getBoolean("sensitive");
      this.priorityDir = NBTUtils.getInt(compound, "prio", 0);
      this.specialConnect = compound.getBoolean("special_connected");
      this.pulse = compound.getBoolean("pulse");
      this.pulseState = compound.getInt("pulse_state");
      this.comparator = compound.getBoolean("comparator");
      this.updateTileFields(new String[]{"extraFacings"});
      this.onStateChanged();
      this.notifyChanges(true, DirectionList.ALL);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.FILTERED_EXTRACTION_TUBE;
   }

   @Override
   public boolean canConnectToRedstone(Direction dir) {
      return this.sensitive;
   }

   @Override
   public boolean canConnect(ITube other, Direction dir) {
      return super.canConnect(other, dir) && other.getTubeType() == ITube.TubeType.SIMPLE;
   }

   @Override
   public boolean hasGui(Player player, InteractionHand hand, Direction side) {
      return this.foamed <= 0 || player.getItemInHand(hand).isEmpty();
   }

   @Override
   public ITube.TubeType getTubeType() {
      return this.specialConnect ? ITube.TubeType.SIMPLE : ITube.TubeType.EXTRACTION;
   }

   @Override
   public int getPrioritySide() {
      return this.priorityDir;
   }

   @Override
   public void handleRedstone() {
      if (!this.comparator) {
         super.handleRedstone();
      } else if (this.needsRedstoneUpdate) {
         this.needsRedstoneUpdate = false;
         byte max = 0;

         for (Direction dir : DirectionList.ALL.remove(this.getFacing()).remove(this.extraFacings).remove(DirectionList.ofNumber(this.anchors))) {
            byte strength = (byte)Mth.clamp(AdvancedComparatorBlock.getComparatorOutput(this.getLevel(), this.getBlockPos(), dir, false, 0), 0, 15);
            max = (byte)Math.max(max, strength);
            this.sidedSignals[dir.get3DDataValue()] = strength;
         }

         this.signal = max;
      }
   }

   @Override
   public void onComparatorUpdate(BlockPos from) {
      if (this.comparator) {
         this.needsRedstoneUpdate = true;
      }
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.getItem() == Items.REDSTONE) {
         Direction dir = ITubeBlock.isClickingAt(hit.getLocation().subtract(Vec3.atLowerCornerOf(hit.getBlockPos())), side, 0.1875F);
         if (dir != null && dir != this.getFacing() && this.inventories.getHandler(dir) != null) {
            this.extraFacings = this.extraFacings.flip(dir);
            this.updateTileField("extraFacings");
            return true;
         }
      } else if (stack.getItem() == Items.REDSTONE_TORCH) {
         this.sensitive = !this.sensitive;
         if (this.isSimulating()) {
            player.displayClientMessage(
               this.translate(this.sensitive ? "gui.ic2.tube.extraction.redstone.enable" : "gui.ic2.tube.extraction.redstone.disable"), false
            );
         }

         return true;
      }

      return super.onRightClick(player, hand, side, hit);
   }

   @Override
   public boolean doTubeAction(Direction side, Vec3 hit, Player player, TubeAction action) {
      if (action == TubeAction.EXTRA_EXTRACTION_SIDE) {
         Direction dir = ITubeBlock.isClickingAt(hit, side, 0.1875F);
         if (dir != null
            && dir != this.getFacing()
            && this.inventories.getHandler(dir) != null
            && !this.hasAnchor(dir)
            && (this.priorityDir == 0 || Direction.from3DDataValue(this.priorityDir - 1) != dir)) {
            this.extraFacings = this.extraFacings.flip(dir);
            this.updateTileField("extraFacings");
            return true;
         }
      } else {
         if (action == TubeAction.REDSTONE_CONTROL) {
            this.sensitive = !this.sensitive;
            if (this.isSimulating()) {
               player.displayClientMessage(
                  this.translate(this.sensitive ? "gui.ic2.tube.extraction.redstone.enable" : "gui.ic2.tube.extraction.redstone.disable"), false
               );
            }

            return true;
         }

         if (action == TubeAction.COMPARATOR) {
            this.comparator = !this.comparator;
            if (this.isSimulating()) {
               player.displayClientMessage(
                  this.translate(this.comparator ? "gui.ic2.tube.extraction.comparator.enable" : "gui.ic2.tube.extraction.comparator.disable"), false
               );
            }

            return true;
         }

         if (action == TubeAction.OUTPUT_PRIORITY) {
            Direction clicked = ITubeBlock.isClickingAt(hit, side, 0.1875F);
            if (clicked != null && clicked != this.getFacing() && this.extraFacings.notContains(clicked)) {
               if (clicked.ordinal() != this.priorityDir - 1) {
                  this.priorityDir = clicked.ordinal() + 1;
               } else {
                  this.priorityDir = 0;
               }

               this.onStateChanged();
               return true;
            }
         } else {
            if (action == TubeAction.SPECIAL_EXTRACTION_CONNECTIVITY) {
               this.specialConnect = !this.specialConnect;
               this.tubes.markDirty();
               if (this.isSimulating()) {
                  player.displayClientMessage(
                     this.translate(this.specialConnect ? "gui.ic2.tube.extraction.special.enable" : "gui.ic2.tube.extraction.special.disable"), false
                  );
               }

               this.self.setActiveDirections(DirectionList.EMPTY);
               this.self.setActiveDirections(DirectionList.ALL.remove(DirectionList.ofNumber(this.anchors)));
               this.tubes.invalidateCache();
               this.onStateChanged();
               this.notifyChanges(true, DirectionList.ALL);
               return true;
            }

            if (action == TubeAction.PULSE) {
               this.pulse = !this.pulse;
               this.sensitive = this.sensitive | this.pulse;
               if (this.isSimulating()) {
                  player.displayClientMessage(
                     this.translate(this.pulse ? "gui.ic2.tube.extraction.pulse.enable" : "gui.ic2.tube.extraction.pulse.disable"), false
                  );
               }

               return true;
            }
         }
      }

      return false;
   }

   @Override
   public DirectionList getExtraFacings() {
      return this.extraFacings;
   }

   @Override
   public DirectionList getValidDirections(TransportedItem item) {
      DirectionList list = super.getValidDirections(item).remove(this.getFacing()).remove(this.extraFacings);
      if (this.priorityDir > 0) {
         DirectionList newList = list.keep(DirectionList.ofFacing(Direction.from3DDataValue(this.priorityDir - 1)));
         if (!newList.isEmpty()) {
            return newList;
         }
      }

      return list;
   }

   @Override
   public void setFacing(Direction facing) {
      if (facing != this.getFacing()) {
         this.extraFacings = this.extraFacings.replace(facing, this.getFacing());
      }

      super.setFacing(facing);
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      if (this.inventories.getHandler(this.getFacing()) == null && !this.inventories.getPresentSides().remove(DirectionList.ofNumber(this.anchors)).isEmpty()) {
         this.setFacing(this.inventories.getPresentSides().getDefaultFacing());
      }
   }

   @Override
   protected void onCachesUpdated() {
      super.onCachesUpdated();
      int code = this.extraFacings.getCode();
      this.extraFacings = this.extraFacings.keep(this.inventories.getPresentSides());
      if (code != this.extraFacings.getCode()) {
         this.onStateChanged();
      }
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("extraFacings")) {
         this.requestModelDataUpdate();
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @Override
   public void onTubeUpdate() {
      if (!this.isRendering()) {
         this.handleRedstone();
         this.updatePulseState();
         if (this.clock(DELAY[Math.min(8, this.upgradeInventory.getStackInSlot(0).getCount())])
            && (!this.sensitive || (this.pulse ? this.pulseState == 1 : this.isRedstonePowered()))) {
            if (this.pulse) {
               this.pulseState = 2;
            }

            IFilter filter = this.getFilter();
            this.handleExtraction(filter, this.getFacing());
            if (!this.extraFacings.isEmpty()) {
               for (Direction dir : this.extraFacings) {
                  this.handleExtraction(filter, dir);
               }
            }
         }
      }
   }

   private void updatePulseState() {
      if (this.pulse) {
         if (this.pulseState == 0 && this.isRedstonePowered()) {
            this.pulseState = 1;
         } else if (this.pulseState == 2 && !this.isRedstonePowered()) {
            this.pulseState = 0;
         }
      }
   }

   public void handleExtraction(IFilter filter, Direction dir) {
      if (!this.hasAnchor(dir)) {
         IItemTransporter transporter = this.useCustomFilter ? this.transporter : TransporterManager.getTransporter(this.inventories.getHandler(dir));
         if (transporter != null) {
            this.addItem(
               transporter.removeItem(filter, dir.getOpposite(), TRANSFER_RATE[Math.min(8, this.upgradeInventory.getStackInSlot(1).getCount())], false), dir
            );
         }
      }
   }

   public IFilter getFilter() {
      if (this.filter == null) {
         List<IFilter> data = new ObjectArrayList();
         this.useCustomFilter = false;

         for (int i = 0; i < this.filters.size(); i++) {
            FilteredExtractionTubeTileEntity.FilterEntry entry = (FilteredExtractionTubeTileEntity.FilterEntry)this.filters.get(i);
            if (entry.isValid()) {
               data.add(entry.getFilter());
               this.useCustomFilter = this.useCustomFilter | entry.getKeepItems() > 0;
            }
         }

         this.filter = (IFilter)(data.isEmpty()
            ? (this.whitelist ? SpecialFilters.ALWAYS_FALSE : SpecialFilters.ANY_ITEM)
            : (this.whitelist ? new ArrayOrFilter(data) : new InvertedFilter(new ArrayOrFilter(data))));
      }

      return this.filter;
   }

   @Override
   public boolean canSetFacingInternal(Direction dir) {
      return dir != this.getFacing()
         && (
            this.isRendering()
               ? DirectionList.getNeighborCapability(this, dir, ForgeCapabilities.ITEM_HANDLER).isPresent()
               : this.inventories.getHandler(dir) != null
         );
   }

   @Override
   public boolean isRotatable() {
      return true;
   }

   @Override
   public void onGuiClosed(Player player) {
      if (this.selectedIndex >= 0 && this.selectedIndex < this.filters.size()) {
         if (this.filterInventory.getStackInSlot(0).isEmpty()) {
            if (((FilteredExtractionTubeTileEntity.FilterEntry)this.filters.get(this.selectedIndex)).getStack().isEmpty()) {
               this.filters.remove(this.selectedIndex);
               this.selectedIndex = -1;
               this.updateGuiFields(new String[]{"filters", "selectedIndex"});
            }
         } else {
            ((FilteredExtractionTubeTileEntity.FilterEntry)this.filters.get(this.selectedIndex))
               .setData(this.filterInventory.getStackInSlot(0), this.flags, this.durability, this.keepItems);
            this.filter = null;
            this.selectedIndex = -1;
            this.updateGuiFields(new String[]{"filters", "selectedIndex"});
         }
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 1) {
         this.flags ^= value;
         if (this.flags == 256) {
            this.durability = 0;
            this.updateGuiField("durability");
         }

         this.updateGuiField("flags");
      } else if (key == 2) {
         this.durability = Mth.clamp(value, -1, 10);
         this.updateGuiField("durability");
      } else if (key == 3) {
         if (!this.filterInventory.getStackInSlot(0).isEmpty()) {
            this.keepItems = Mth.clamp(value, 0, 9999);
            this.updateGuiField("keepItems");
         }
      } else if (key == 4) {
         if (this.selectedIndex != -1 && this.selectedIndex != value && this.selectedIndex < this.filters.size()) {
            if (this.filterInventory.getStackInSlot(0).isEmpty()) {
               if (((FilteredExtractionTubeTileEntity.FilterEntry)this.filters.get(this.selectedIndex)).getStack().isEmpty()) {
                  this.filters.remove(this.selectedIndex);
                  this.updateGuiField("filters");
               }
            } else {
               ((FilteredExtractionTubeTileEntity.FilterEntry)this.filters.get(this.selectedIndex))
                  .setData(this.filterInventory.getStackInSlot(0), this.flags, this.durability, this.keepItems);
               this.filter = null;
               this.updateGuiField("filters");
            }
         }

         if (value != -1 && value < this.filters.size()) {
            this.selectedIndex = value;
            FilteredExtractionTubeTileEntity.FilterEntry entry = (FilteredExtractionTubeTileEntity.FilterEntry)this.filters.get(this.selectedIndex);
            this.filterInventory.setStackInSlot(0, entry.getStack());
            this.flags = entry.getFlags();
            this.durability = entry.getDurability();
            this.keepItems = entry.getKeepItems();
         }

         this.updateGuiFields(new String[]{"selectedIndex", "flags", "durability", "keepItems"});
      } else if (key == 5) {
         this.whitelist = value == 0;
         this.filter = null;
         this.updateGuiField("whitelist");
      } else if (key == 6) {
         if (!this.filterInventory.getStackInSlot(0).isEmpty()) {
            if (this.selectedIndex != -1) {
               ((FilteredExtractionTubeTileEntity.FilterEntry)this.filters.get(this.selectedIndex))
                  .setData(this.filterInventory.getStackInSlot(0), this.flags, this.durability, this.keepItems);
               this.filter = null;
               this.filterInventory.setStackInSlot(0, ItemStack.EMPTY);
               this.flags = 4;
               this.durability = -1;
               this.keepItems = 0;
               this.selectedIndex = -1;
            } else if (this.filters.size() < 16) {
               this.filters
                  .add(new FilteredExtractionTubeTileEntity.FilterEntry(this.filterInventory.getStackInSlot(0), this.flags, this.durability, this.keepItems));
               this.filterInventory.setStackInSlot(0, ItemStack.EMPTY);
               this.filter = null;
               this.flags = 4;
               this.durability = -1;
               this.keepItems = 0;
               this.updateGuiField("filters");
            }

            this.updateGuiFields(new String[]{"filters", "selectedIndex", "flags", "durability", "keepItems"});
         }
      } else if (key == 7 && value >= 0 && value < this.filters.size()) {
         if (this.selectedIndex == value) {
            this.selectedIndex = -1;
            this.filterInventory.setStackInSlot(0, ItemStack.EMPTY);
            this.flags = 4;
            this.durability = -1;
            this.keepItems = 0;
         } else if (this.selectedIndex != -1 && value < this.selectedIndex) {
            this.selectedIndex--;
         }

         this.filters.remove(value);
         this.filter = null;
         this.updateGuiFields(new String[]{"filters", "flags", "durability", "keepItems", "selectedIndex"});
      }
   }

   public static class FilterEntry {
      ItemStack stack;
      int flags;
      int durability;
      int keep;
      IFilter filter;

      public FilterEntry(IInputBuffer buffer) {
         this.stack = buffer.readItemStack();
         this.keep = buffer.readVarInt();
      }

      public FilterEntry(ItemStack stack, int flags, int durability, int keep) {
         this.setData(stack, flags, durability, keep);
      }

      public static FilteredExtractionTubeTileEntity.FilterEntry read(CompoundTag nbt) {
         ItemStack stack = ItemStack.of(nbt);
         return stack.isEmpty()
            ? null
            : new FilteredExtractionTubeTileEntity.FilterEntry(stack, nbt.getInt("filter_tag"), nbt.getInt("filter_durability"), nbt.getInt("filter_keep"));
      }

      public void setData(ItemStack stack, int flags, int durability, int keep) {
         this.stack = stack;
         this.flags = flags | (stack.getItem() instanceof ITagItem ? 32 : 0);
         this.durability = durability;
         this.keep = keep;
         this.filter = new TransferFilter(stack, flags, durability);
      }

      public void write(IOutputBuffer buffer) {
         buffer.writeItemStack(this.stack);
         buffer.writeVarInt(this.keep);
      }

      public IFilter getFilter() {
         return this.filter;
      }

      public boolean isValid() {
         return !this.stack.isEmpty();
      }

      public CompoundTag save() {
         CompoundTag nbt = this.stack.save(new CompoundTag());
         nbt.putInt("filter_tag", this.flags);
         nbt.putInt("filter_durability", this.durability);
         nbt.putInt("filter_keep", this.keep);
         return nbt;
      }

      public int getFlags() {
         return this.flags;
      }

      public int getKeepItems() {
         return this.keep;
      }

      public int getDurability() {
         return this.durability;
      }

      public ItemStack getStack() {
         return this.stack;
      }
   }

   public class Handler implements IItemTransporter {
      Object2IntOpenHashMap<FilteredExtractionTubeTileEntity.FilterEntry> filterCounter = new Object2IntOpenHashMap();

      @Override
      public int addItem(ItemStack stack, Direction dir, boolean simulate) {
         return 0;
      }

      @Override
      public ItemStack removeItem(IFilter filter, Direction dir, int amount, boolean simulate) {
         IItemHandler handler = FilteredExtractionTubeTileEntity.this.inventories.getHandler(dir.getOpposite());
         if (amount > 0 && handler != null && handler.getSlots() > 0) {
            this.filterCounter.clear();
            ItemStack result = ItemStack.EMPTY;
            int i = 0;

            for (int m = handler.getSlots(); i < m; i++) {
               ItemStack stack = handler.getStackInSlot(i);
               if (!stack.isEmpty()) {
                  for (int j = 0; j < FilteredExtractionTubeTileEntity.this.filters.size(); j++) {
                     FilteredExtractionTubeTileEntity.FilterEntry entry = (FilteredExtractionTubeTileEntity.FilterEntry)FilteredExtractionTubeTileEntity.this.filters
                        .get(j);
                     if (entry.isValid()
                        && entry.getFilter().matches(stack) == FilteredExtractionTubeTileEntity.this.whitelist
                        && (result.isEmpty() || StackUtil.isStackEqual(stack, result))) {
                        if (entry.getKeepItems() > 0) {
                           int value = this.filterCounter.getInt(entry);
                           int leftOver = stack.getCount();
                           if (value < entry.getKeepItems()) {
                              int toRemove = Math.min(entry.getKeepItems() - value, leftOver);
                              this.filterCounter.put(entry, value + toRemove);
                              leftOver -= toRemove;
                           }

                           if (leftOver > 0) {
                              if (result.isEmpty()) {
                                 result = handler.extractItem(i, Math.min(amount - result.getCount(), leftOver), simulate);
                              } else {
                                 result.grow(handler.extractItem(i, Math.min(amount - result.getCount(), leftOver), simulate).getCount());
                              }
                           }
                        } else if (result.isEmpty()) {
                           result = handler.extractItem(i, amount - result.getCount(), simulate);
                        } else {
                           result.grow(handler.extractItem(i, amount - result.getCount(), simulate).getCount());
                        }

                        if (result.getCount() >= amount) {
                           return result;
                        }
                        break;
                     }
                  }
               }
            }

            return result;
         } else {
            return ItemStack.EMPTY;
         }
      }

      @Override
      public int getInventorySize(Direction dir) {
         IItemHandler handler = FilteredExtractionTubeTileEntity.this.inventories.getHandler(FilteredExtractionTubeTileEntity.this.getFacing());
         return handler == null ? 0 : handler.getSlots();
      }

      @Override
      public Object2IntMap<ItemStack> getAllItems(Direction dir, boolean compareNBT) {
         IItemHandler handler = FilteredExtractionTubeTileEntity.this.inventories.getHandler(dir.getOpposite());
         if (handler != null && handler.getSlots() > 0) {
            int slots = handler.getSlots();
            Object2IntLinkedOpenCustomHashMap<ItemStack> items = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(compareNBT));

            for (int i = 0; i < slots; i++) {
               ItemStack stack = handler.getStackInSlot(i);
               if (!stack.isEmpty()) {
                  items.addTo(StackUtil.copyWithSize(stack, 1), stack.getCount());
               }
            }

            return items;
         } else {
            return Object2IntMaps.emptyMap();
         }
      }

      @Override
      public IItemTransporter.InvResult getInventory(Direction dir, boolean compareNBT) {
         IItemTransporter.InvResult result = new IItemTransporter.InvResult(compareNBT);
         IItemHandler handler = FilteredExtractionTubeTileEntity.this.inventories.getHandler(dir.getOpposite());
         int slots = handler.getSlots();
         if (slots > 0) {
            for (int i = 0; i < slots; i++) {
               result.add(handler.getStackInSlot(i), handler.getSlotLimit(i));
            }
         }

         return result;
      }
   }
}
