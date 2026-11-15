package ic2.core.block.transport.item.tubes;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.ICopyableSettings;
import ic2.api.tiles.tubes.IProviderTube;
import ic2.api.tiles.tubes.ITube;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.misc.ITubeBlock;
import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.block.transport.item.TubeAction;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.block.transport.item.container.ProviderTubeContainer;
import ic2.core.inventory.base.ICamouflageGui;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.filter.StackFilter;
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
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.UUID;
import java.util.function.ObjIntConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class ProviderTubeTileEntity extends TubeTileEntity implements IProviderTube, ICamouflageGui, ICopyableSettings {
   public IHasInventory filterInv = new SimpleInventory(1);
   @NetworkInfo
   public int globalKeepItems;
   @NetworkInfo
   public int keepItems;
   @NetworkInfo
   public boolean compareNBT;
   @NetworkInfo
   public boolean whiteList = true;
   @NetworkInfo
   public int selectedIndex = -1;
   @NetworkInfo
   public SynchronizedList<ProviderTubeTileEntity.ProvideEntry> filters = new SynchronizedList<>(
      ProviderTubeTileEntity.ProvideEntry::write, ProviderTubeTileEntity.ProvideEntry::new
   );
   @NetworkInfo
   public boolean keepMode = false;
   public int priorityDir = 0;
   public Object2IntMap<ItemStack> mappedFilter;
   public int requestLimit = 512;
   public int maxTubeRequest = 512;
   long lastRequest = -1L;
   Object2IntMap<ItemStack> foundItems = null;

   public ProviderTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addGuiFields(new String[]{"globalKeepItems", "keepItems", "compareNBT", "whiteList", "keepMode", "filters"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.PROVIDER_TUBE;
   }

   @Override
   public void applySpeed(TransportedItem item) {
   }

   @Override
   public boolean isFoamed() {
      return this.foamed > 0;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ProviderTubeContainer(this, player, windowID);
   }

   @Override
   public int getPrioritySide() {
      return this.priorityDir;
   }

   @Override
   public boolean doSpecialAction(Direction side, Vec3 hit, Player player) {
      Direction clicked = ITubeBlock.isClickingAt(hit, side, 0.1875F);
      if (player.isShiftKeyDown() && IC2.KEYBOARD.isAltKeyDown(player) && clicked != null && clicked != this.getFacing()) {
         if (clicked.ordinal() != this.priorityDir - 1) {
            this.priorityDir = clicked.ordinal() + 1;
         } else {
            this.priorityDir = 0;
         }

         this.onStateChanged();
         return true;
      } else {
         return super.doSpecialAction(side, hit, player);
      }
   }

   @Override
   public boolean doTubeAction(Direction side, Vec3 hit, Player player, TubeAction action) {
      if (action == TubeAction.OUTPUT_PRIORITY) {
         Direction clicked = ITubeBlock.isClickingAt(hit, side, 0.1875F);
         if (clicked != null && clicked != this.getFacing()) {
            if (clicked.ordinal() != this.priorityDir - 1) {
               this.priorityDir = clicked.ordinal() + 1;
            } else {
               this.priorityDir = 0;
            }

            this.onStateChanged();
            return true;
         }
      }

      return false;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putInt(compound, "global_keep", this.globalKeepItems, 0);
      NBTUtils.putBoolean(compound, "compare_nbt", this.compareNBT, false);
      NBTUtils.putBoolean(compound, "white_list", this.whiteList, false);
      NBTUtils.putByte(compound, "priority", this.priorityDir, 0);
      NBTUtils.putBoolean(compound, "keep_mode", this.keepMode, false);
      ListTag list = new ListTag();
      int i = 0;

      for (int m = this.filters.size(); i < m; i++) {
         list.add(((ProviderTubeTileEntity.ProvideEntry)this.filters.get(i)).save());
      }

      NBTUtils.put(compound, "filters", list);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.globalKeepItems = compound.getInt("global_keep");
      this.compareNBT = compound.getBoolean("compare_nbt");
      this.whiteList = compound.getBoolean("white_list");
      this.priorityDir = compound.getInt("priority");
      this.keepMode = compound.getBoolean("keep_mode");

      for (CompoundTag tag : NBTListWrapper.wrap(compound.getList("filters", 10), CompoundTag.class)) {
         ProviderTubeTileEntity.ProvideEntry entry = ProviderTubeTileEntity.ProvideEntry.read(tag);
         if (entry != null) {
            this.filters.add(entry);
         }
      }
   }

   @Override
   public void saveSettings(CompoundTag compound) {
      NBTUtils.putInt(compound, "global_keep", this.globalKeepItems, 0);
      NBTUtils.putBoolean(compound, "compare_nbt", this.compareNBT, false);
      NBTUtils.putBoolean(compound, "white_list", this.whiteList, false);
      NBTUtils.putByte(compound, "priority", this.priorityDir, 0);
      NBTUtils.putBoolean(compound, "keep_mode", this.keepMode, false);
      ListTag list = new ListTag();
      int i = 0;

      for (int m = this.filters.size(); i < m; i++) {
         list.add(((ProviderTubeTileEntity.ProvideEntry)this.filters.get(i)).save());
      }

      NBTUtils.put(compound, "filters", list);
   }

   @Override
   public void loadSettings(CompoundTag compound) {
      this.globalKeepItems = compound.getInt("global_keep");
      this.compareNBT = compound.getBoolean("compare_nbt");
      this.whiteList = compound.getBoolean("white_list");
      this.priorityDir = compound.getInt("priority");
      this.keepMode = compound.getBoolean("keep_mode");
      this.keepItems = 0;
      this.selectedIndex = -1;
      this.filterInv.setStackInSlot(0, ItemStack.EMPTY);
      this.filters.clear();

      for (CompoundTag tag : NBTListWrapper.wrap(compound.getList("filters", 10), CompoundTag.class)) {
         ProviderTubeTileEntity.ProvideEntry entry = ProviderTubeTileEntity.ProvideEntry.read(tag);
         if (entry != null) {
            this.filters.add(entry);
         }
      }

      this.updateGuiFields(new String[]{"globalKeepItems", "keepItems", "compareNBT", "whiteList", "keepMode"});
   }

   @Override
   public boolean isRotatable() {
      return true;
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
   public boolean canConnect(ITube other, Direction dir) {
      return super.canConnect(other, dir) && other.getTubeType() == ITube.TubeType.SIMPLE;
   }

   @Override
   public DirectionList getValidDirections(TransportedItem item) {
      DirectionList list = super.getValidDirections(item).remove(this.getFacing());
      if (this.priorityDir > 0) {
         DirectionList newList = list.keep(DirectionList.ofFacing(Direction.from3DDataValue(this.priorityDir - 1)));
         if (!newList.isEmpty()) {
            return newList;
         }
      }

      return list;
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      if (this.inventories.getHandler(this.getFacing()) == null && !this.inventories.getPresentSides().isEmpty()) {
         this.setFacing(this.inventories.getPresentSides().getDefaultFacing());
      }
   }

   @Override
   public void onTubeUpdate() {
      super.onTubeUpdate();
      this.requestLimit = this.maxTubeRequest;
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         if (!this.filterInv.getStackInSlot(0).isEmpty()) {
            if (this.selectedIndex != -1) {
               ((ProviderTubeTileEntity.ProvideEntry)this.filters.get(this.selectedIndex)).setData(this.filterInv.getStackInSlot(0).copy(), this.keepItems);
            } else if (this.filters.size() < 18) {
               this.filters.add(new ProviderTubeTileEntity.ProvideEntry(this.filterInv.getStackInSlot(0).copy(), this.keepItems));
            }

            this.keepItems = 0;
            this.selectedIndex = -1;
            this.filterInv.setStackInSlot(0, ItemStack.EMPTY);
            this.updateGuiFields(new String[]{"keepItems", "selectedIndex", "filters"});
         }
      } else if (key == 1) {
         this.compareNBT = !this.compareNBT;
         this.updateGuiField("compareNBT");
         this.mappedFilter = null;
      } else if (key == 3) {
         this.whiteList = !this.whiteList;
         this.updateGuiField("whiteList");
      } else if (key == 4) {
         if (value < 0 || value >= this.filters.size()) {
            return;
         }

         this.selectedIndex = value;
         ProviderTubeTileEntity.ProvideEntry entry = (ProviderTubeTileEntity.ProvideEntry)this.filters.get(value);
         this.keepItems = entry.getAmount();
         this.filterInv.setStackInSlot(0, entry.getStack());
         this.updateGuiFields(new String[]{"selectedIndex", "keepItems"});
      } else if (key == 5) {
         if (value < 0 || value >= this.filters.size()) {
            return;
         }

         this.filters.remove(value);
         this.keepItems = 0;
         this.selectedIndex = -1;
         this.filterInv.setStackInSlot(0, ItemStack.EMPTY);
         this.mappedFilter = null;
         this.updateGuiFields(new String[]{"keepItems", "selectedIndex", "filters"});
      } else if (key == 6) {
         this.globalKeepItems = Mth.clamp(value, 0, 9999);
         this.updateGuiField("globalKeepItems");
      } else if (key == 7) {
         this.keepItems = Mth.clamp(value, 0, 9999);
         this.updateGuiField("keepItems");
      } else if (key == 8) {
         this.keepMode = !this.keepMode;
         this.updateGuiField("keepMode");
      }
   }

   @Override
   public long getProviderSource() {
      return this.getBlockPos().relative(this.getFacing()).asLong();
   }

   @Override
   public void getItemsProvided(ObjIntConsumer<ItemStack> listener) {
      if (this.requestLimit > 0) {
         IItemTransporter transporter = TransporterManager.getTransporter(this.inventories.getHandler(this.getFacing()));
         if (transporter != null) {
            if (this.lastRequest < this.getLevel().getGameTime() || this.foundItems == null) {
               this.lastRequest = this.getLevel().getGameTime();
               this.foundItems = transporter.getAllItems(this.getFacing().getOpposite(), this.compareNBT);
            }

            if (!this.foundItems.isEmpty()) {
               ObjectIterator var3 = Object2IntMaps.fastIterable(this.foundItems).iterator();

               while (var3.hasNext()) {
                  Entry<ItemStack> entry = (Entry<ItemStack>)var3.next();
                  ItemStack stack = (ItemStack)entry.getKey();
                  int amount = entry.getIntValue() - this.mapItems().getOrDefault(stack, this.globalKeepItems);
                  if (amount > 0) {
                     listener.accept(stack, amount);
                  }
               }
            }
         }
      }
   }

   @Override
   public int provideItems(ItemStack stack, int amount, DyeColor color, UUID request) {
      if (amount > 0 && this.requestLimit > 0 && this.isValid(stack) && !this.hasAnchor(this.getFacing())) {
         IItemTransporter transporter = TransporterManager.getTransporter(this.inventories.getHandler(this.getFacing()));
         if (transporter == null) {
            return 0;
         } else {
            if (this.lastRequest < this.getLevel().getGameTime() || this.foundItems == null) {
               this.lastRequest = this.getLevel().getGameTime();
               this.foundItems = transporter.getAllItems(this.getFacing().getOpposite(), this.compareNBT);
            }

            int found = this.foundItems.getInt(stack) - this.mapItems().getOrDefault(stack, this.globalKeepItems);
            int toRemove = Math.min(Math.min(found, Math.min(this.requestLimit, amount)), 64);
            if (toRemove > 0) {
               ItemStack extracted = transporter.removeItem(
                  (IFilter)(this.compareNBT ? StackFilter.defaultCompare(stack) : new SimpleFilter(stack.getItem())),
                  this.getFacing().getOpposite(),
                  toRemove,
                  false
               );
               if (!extracted.isEmpty()) {
                  stack = StackUtil.copyWithSize(extracted, 1);
                  int value = extracted.getCount();
                  if (found - value > 0) {
                     this.foundItems.put(stack, found - value);
                  } else {
                     this.foundItems.removeInt(stack);
                  }

                  this.addItem(new TransportedItem(extracted).setColor(color).setRequestId(request).setStartSpeed(15), this.getFacing());
                  this.requestLimit -= value;
                  return value;
               }
            }

            return 0;
         }
      } else {
         return 0;
      }
   }

   public boolean isValid(ItemStack stack) {
      if (this.keepMode) {
         return true;
      } else {
         Object2IntMap<ItemStack> mapped = this.mapItems();
         return mapped.isEmpty() || mapped.containsKey(stack) == this.whiteList;
      }
   }

   public Object2IntMap<ItemStack> mapItems() {
      if (this.mappedFilter == null) {
         this.mappedFilter = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(this.compareNBT));
         int i = 0;

         for (int m = this.filters.size(); i < m; i++) {
            ProviderTubeTileEntity.ProvideEntry entry = (ProviderTubeTileEntity.ProvideEntry)this.filters.get(i);
            this.mappedFilter.put(entry.getStack(), entry.getAmount());
         }
      }

      return this.mappedFilter;
   }

   public static class ProvideEntry {
      ItemStack stack;
      int keep;

      public ProvideEntry(IInputBuffer buffer) {
         this.stack = buffer.readItemStack();
         this.keep = buffer.readInt();
      }

      public ProvideEntry(ItemStack stack, int amount) {
         this.setData(stack, amount);
      }

      public static ProviderTubeTileEntity.ProvideEntry read(CompoundTag nbt) {
         ItemStack stack = ItemStack.of(nbt);
         return stack.isEmpty() ? null : new ProviderTubeTileEntity.ProvideEntry(stack, nbt.getInt("amount"));
      }

      public void setData(ItemStack stack, int keep) {
         this.stack = stack;
         this.keep = keep;
      }

      public void write(IOutputBuffer buffer) {
         buffer.writeItemStack(this.stack);
         buffer.writeInt(this.keep);
      }

      public CompoundTag save() {
         CompoundTag nbt = this.stack.save(new CompoundTag());
         nbt.putInt("amount", this.keep);
         return nbt;
      }

      public int getAmount() {
         return this.keep;
      }

      public ItemStack getStack() {
         return this.stack;
      }

      public boolean isValid() {
         return !this.stack.isEmpty();
      }
   }
}
