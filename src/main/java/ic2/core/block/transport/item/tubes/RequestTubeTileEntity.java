package ic2.core.block.transport.item.tubes;

import com.google.common.collect.Lists;
import ic2.api.items.ITagItem;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.ICopyableSettings;
import ic2.api.tiles.tubes.IRequestTube;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.block.transport.item.TubeAction;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.block.transport.item.container.RequestTubeContainer;
import ic2.core.block.transport.item.logistic.TubeNet;
import ic2.core.inventory.base.ICamouflageGui;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.inv.ListenerInventory;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.inv.SimulatedIItemHandler;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.collection.SynchronizedList;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

public class RequestTubeTileEntity extends TubeTileEntity implements IRequestTube, ICamouflageGui, ICopyableSettings {
   public SimpleInventory filterInv = new ListenerInventory(1, this::onInserted);
   @NetworkInfo
   public boolean nbt;
   @NetworkInfo
   public boolean dump;
   @NetworkInfo
   public int amountRequested;
   @NetworkInfo
   public DyeColor color;
   @NetworkInfo
   public int selectedIndex = -1;
   public boolean redstoneRequest = false;
   public UUID requestId = UUID.randomUUID();
   @NetworkInfo
   public SynchronizedList<RequestTubeTileEntity.RequestEntry> filters = new SynchronizedList<>(
      RequestTubeTileEntity.RequestEntry::write, RequestTubeTileEntity.RequestEntry::new
   );
   public Object2IntLinkedOpenCustomHashMap<ItemStack> requested;
   public Object2IntLinkedOpenCustomHashMap<ItemStack> awaiting;
   public List<ItemStack> toInsert;

   public RequestTubeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.requested = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(this.nbt));
      this.awaiting = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.INSTANCE);
      this.toInsert = new LinkedList<>();
      this.addGuiFields(new String[]{"nbt", "dump", "amountRequested", "color", "selectedIndex", "filters"});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.REQUEST_TUBE;
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
      return new RequestTubeContainer(this, player, windowID);
   }

   @Override
   public boolean doTubeAction(Direction side, Vec3 hit, Player player, TubeAction action) {
      if (action == TubeAction.REDSTONE_CONTROL) {
         this.redstoneRequest = !this.redstoneRequest;
         if (this.isSimulating()) {
            player.displayClientMessage(
               this.translate(this.redstoneRequest ? "gui.ic2.tube.extraction.redstone.enable" : "gui.ic2.tube.extraction.redstone.disable"), false
            );
            TubeNet.INSTANCE.clearRequests(this);
         }

         return true;
      } else {
         return super.doTubeAction(side, hit, player, action);
      }
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putBoolean(compound, "nbt", this.nbt, false);
      NBTUtils.putBoolean(compound, "dump", this.dump, false);
      NBTUtils.putBoolean(compound, "redstone", this.redstoneRequest, false);
      NBTUtils.putEnum(compound, "request_color", this.color);
      compound.putUUID("request_id", this.requestId);
      ListTag list = new ListTag();
      int i = 0;

      for (int m = this.filters.size(); i < m; i++) {
         list.add(((RequestTubeTileEntity.RequestEntry)this.filters.get(i)).save());
      }

      NBTUtils.put(compound, "requests", list);
      list = new ListTag();
      ObjectIterator var8 = Object2IntMaps.fastIterable(this.awaiting).iterator();

      while (var8.hasNext()) {
         Entry<ItemStack> entry = (Entry<ItemStack>)var8.next();
         CompoundTag data = ((ItemStack)entry.getKey()).save(new CompoundTag());
         data.putInt("awaiting", entry.getIntValue());
         list.add(data);
      }

      NBTUtils.put(compound, "awaiting", list);
      list = new ListTag();

      for (ItemStack entry : this.toInsert) {
         list.add(entry.save(new CompoundTag()));
      }

      NBTUtils.put(compound, "toInsert", list);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.nbt = compound.getBoolean("nbt");
      this.dump = compound.getBoolean("dump");
      this.redstoneRequest = compound.getBoolean("redstone");
      if (compound.hasUUID("request_id")) {
         this.requestId = compound.getUUID("request_id");
      }

      this.color = NBTUtils.getEnum(compound, "request_color", DyeColor.class);

      for (CompoundTag data : NBTListWrapper.wrap(compound.getList("requests", 10), CompoundTag.class)) {
         RequestTubeTileEntity.RequestEntry entry = RequestTubeTileEntity.RequestEntry.read(data, this.nbt);
         if (entry != null && entry.isValid()) {
            this.filters.add(entry);
         }
      }

      for (CompoundTag datax : NBTListWrapper.wrap(compound.getList("awaiting", 10), CompoundTag.class)) {
         ItemStack stack = ItemStack.of(datax);
         if (!stack.isEmpty()) {
            int amount = datax.getInt("awaiting");
            this.awaiting.addTo(stack, amount);
            this.requested.addTo(stack, amount);
         }
      }

      for (CompoundTag dataxx : NBTListWrapper.wrap(compound.getList("toInsert", 10), CompoundTag.class)) {
         ItemStack stack = ItemStack.of(dataxx);
         if (!stack.isEmpty()) {
            this.toInsert.add(stack);
         }
      }
   }

   @Override
   public void saveSettings(CompoundTag compound) {
      NBTUtils.putBoolean(compound, "nbt", this.nbt, false);
      NBTUtils.putBoolean(compound, "dump", this.dump, false);
      NBTUtils.putBoolean(compound, "redstone", this.redstoneRequest, false);
      NBTUtils.putEnum(compound, "request_color", this.color);
      ListTag list = new ListTag();
      int i = 0;

      for (int m = this.filters.size(); i < m; i++) {
         list.add(((RequestTubeTileEntity.RequestEntry)this.filters.get(i)).save());
      }

      NBTUtils.put(compound, "requests", list);
   }

   @Override
   public void loadSettings(CompoundTag compound) {
      this.nbt = compound.getBoolean("nbt");
      this.dump = compound.getBoolean("dump");
      this.redstoneRequest = compound.getBoolean("redstone");
      this.color = NBTUtils.getEnum(compound, "request_color", DyeColor.class);
      this.filters.clear();

      for (CompoundTag data : NBTListWrapper.wrap(compound.getList("requests", 10), CompoundTag.class)) {
         RequestTubeTileEntity.RequestEntry entry = RequestTubeTileEntity.RequestEntry.read(data, this.nbt);
         if (entry != null && entry.isValid()) {
            this.filters.add(entry);
         }
      }

      this.amountRequested = 0;
      this.selectedIndex = -1;
      this.filterInv.setStackInSlot(0, ItemStack.EMPTY);
      this.updateGuiFields(new String[]{"nbt", "dump", "amountRequested", "color", "selectedIndex", "filters"});
   }

   public void onInserted(IHasInventory inventory, int slot) {
      if (this.amountRequested <= 0) {
         ItemStack stack = inventory.getStackInSlot(slot);
         if (!stack.isEmpty()) {
            this.amountRequested = stack.getMaxStackSize();
            this.updateGuiField("amountRequested");
         }
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         if (!this.filterInv.getStackInSlot(0).isEmpty()) {
            if (this.selectedIndex != -1) {
               ((RequestTubeTileEntity.RequestEntry)this.filters.get(this.selectedIndex))
                  .setData(this.filterInv.getStackInSlot(0).copy(), this.amountRequested, this.nbt);
            } else if (this.filters.size() < 18) {
               this.filters.add(new RequestTubeTileEntity.RequestEntry(this.filterInv.getStackInSlot(0).copy(), this.amountRequested, this.nbt));
            }

            this.amountRequested = 0;
            this.selectedIndex = -1;
            this.filterInv.setStackInSlot(0, ItemStack.EMPTY);
            this.updateGuiFields(new String[]{"amountRequested", "selectedIndex", "filters"});
         }
      } else if (key == 1) {
         this.nbt = !this.nbt;

         for (int i = 0; i < this.filters.size(); i++) {
            ((RequestTubeTileEntity.RequestEntry)this.filters.get(i)).updateNBT(this.nbt);
         }

         this.updateGuiField("nbt");
         this.recreateTodoList();
      } else if (key == 3) {
         this.dump = !this.dump;
         this.updateGuiField("dump");
         this.recreateTodoList();
      } else if (key == 4) {
         this.amountRequested = Mth.clamp(value, 0, 1000000);
         this.updateGuiField("amountRequested");
      } else if (key == 5) {
         if (value < 0 || value >= this.filters.size()) {
            return;
         }

         this.selectedIndex = value;
         RequestTubeTileEntity.RequestEntry entry = (RequestTubeTileEntity.RequestEntry)this.filters.get(value);
         this.filterInv.setStackInSlot(0, entry.getStack());
         this.amountRequested = entry.getAmount();
         this.updateGuiFields(new String[]{"selectedIndex", "amountRequested"});
      } else if (key == 6) {
         if (value < 0 || value >= this.filters.size()) {
            return;
         }

         this.filters.remove(value);
         this.amountRequested = 0;
         this.selectedIndex = -1;
         this.filterInv.setStackInSlot(0, ItemStack.EMPTY);
         this.updateGuiFields(new String[]{"amountRequested", "selectedIndex", "filters"});
      } else if (key == 7) {
         this.color = value == 0 ? null : DyeColor.byId(value - 1);
         this.updateGuiField("color");
      } else if (key == 8) {
         this.awaiting.clear();
         this.requested.clear();
         TubeNet.INSTANCE.clearRequests(this);
         if (!this.toInsert.isEmpty()) {
            for (ItemStack stack : this.toInsert) {
               if (!entity.getInventory().add(stack)) {
                  Block.popResource(this.getLevel(), this.getBlockPos(), stack);
               }
            }

            this.toInsert.clear();
         }
      }
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
   public DirectionList getValidDirections(TransportedItem item) {
      DirectionList original = super.getValidDirections(item);
      if (this.requestId.equals(item.getRequestId())) {
         DirectionList override = original.keep(DirectionList.ofFacing(this.getFacing()));
         return override.isEmpty() ? original : override;
      } else {
         return original.remove(this.getFacing());
      }
   }

   @Override
   public void onTubeUpdate() {
      this.handleRedstone();
      if (!this.toInsert.isEmpty() && this.clock(200)) {
         IItemTransporter transporter = TransporterManager.getTransporter(this.inventories.getHandler(this.getFacing()));
         if (transporter != null) {
            Iterator<ItemStack> iter = this.toInsert.iterator();

            while (iter.hasNext()) {
               ItemStack stack = iter.next();
               stack.shrink(transporter.addItem(stack, this.getFacing().getOpposite(), false));
               if (stack.isEmpty()) {
                  iter.remove();
               }
            }
         }
      }
   }

   @Override
   public boolean onSideReached(TransportedItem item) {
      if (this.isSimulating() && item.getTransferDirection() == this.getFacing() && this.requestId.equals(item.getRequestId())) {
         ItemStack stack = item.getServerStack();
         int awaited = Math.min(this.awaiting.getInt(stack), stack.getCount());
         if (awaited > 0) {
            this.awaiting.merge(stack, awaited, MathUtils.REMOVAL);
         }

         int requesting = this.requested.getInt(stack);
         if (requesting > 0) {
            this.requested.merge(stack, awaited, MathUtils.REMOVAL);
         }
      }

      return false;
   }

   @Override
   public boolean onLeftOver(TransportedItem item) {
      this.toInsert.add(item.getServerStack().copy());
      item.invalidate();
      return true;
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      if (this.inventories.getHandler(this.getFacing()) == null && !this.inventories.getPresentSides().isEmpty()) {
         this.setFacing(this.inventories.getPresentSides().getDefaultFacing());
      }
   }

   public void recreateTodoList() {
      if (this.addedToTubeNet) {
         TubeNet.INSTANCE.clearRequests(this);
      }

      this.requested = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(this.nbt));
   }

   @Override
   public UUID getRequestId() {
      return this.requestId;
   }

   public void getMissing(NonNullList<ItemStack> items) {
      if (!this.filters.isEmpty()) {
         IItemHandler handler = this.inventories.getHandler(this.getFacing());
         if (handler != null) {
            IItemTransporter transporter = TransporterManager.getTransporter(SimulatedIItemHandler.createSimulation(handler, this.getFacing(), this.requested));
            if (transporter != null) {
               Object2IntMap<ItemStack> found = transporter.getAllItems(this.getFacing(), this.nbt);
               int i = 0;

               for (int m = this.filters.size(); i < m; i++) {
                  RequestTubeTileEntity.RequestEntry entry = (RequestTubeTileEntity.RequestEntry)this.filters.get(i);
                  int request = this.dump ? 2147483647 : entry.getAmount() - found.getInt(entry.getStack());
                  if (request > 0) {
                     items.add(StackUtil.copyWithSize(entry.getStack(), request));
                  }
               }
            }
         }
      }
   }

   @Override
   public long getRequestSource() {
      return this.getBlockPos().relative(this.getFacing()).asLong();
   }

   private Set<Item> getBlockedItems() {
      if (this.toInsert.isEmpty()) {
         return Collections.emptySet();
      } else {
         Set<Item> items = CollectionUtils.<Item>createSet();
         items.addAll(Lists.transform(this.toInsert, ItemStack::getItem));
         return items;
      }
   }

   @Override
   public void provideRequests(IRequestTube.ITubeRequester requester) {
      if (!this.filters.isEmpty() && (!this.redstoneRequest || this.isRedstonePowered())) {
         IItemHandler handler = this.inventories.getHandler(this.getFacing());
         if (handler != null) {
            IItemTransporter transporter = TransporterManager.getTransporter(SimulatedIItemHandler.createSimulation(handler, this.getFacing(), this.requested));
            if (transporter != null) {
               Object2IntMap<ItemStack> found = transporter.getAllItems(this.getFacing(), this.nbt);
               Set<Item> invalidItems = this.getBlockedItems();
               int i = 0;

               for (int m = this.filters.size(); i < m; i++) {
                  RequestTubeTileEntity.RequestEntry entry = (RequestTubeTileEntity.RequestEntry)this.filters.get(i);
                  int request = this.dump ? 2147483647 : entry.getAmount() - found.getInt(entry.getStack());
                  if (request > 0 && !invalidItems.contains(entry.getStack().getItem())) {
                     request = requester.validateRequest(entry.getStack(), request, this.color);
                     if (request > 0) {
                        request = transporter.addItem(StackUtil.copyWithSize(entry.getStack(), request), this.getFacing().getOpposite(), false);
                        if (request > 0) {
                           this.requested.addTo(entry.getStack(), request);
                           requester.requestItems(entry.getStack(), request, this.color, this.requestId);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void onRequestsReset() {
      this.requested.clear();
   }

   @Override
   public void onRequestLost(ItemStack stack, int amount) {
      int awaited = Math.min(this.awaiting.getInt(stack), amount);
      if (awaited > 0) {
         this.awaiting.merge(stack, awaited, MathUtils.REMOVAL);
      }

      int requesting = this.requested.getInt(stack);
      if (requesting > 0) {
         this.requested.merge(stack, awaited, MathUtils.REMOVAL);
      }
   }

   @Override
   public void onRequestFulfilled(ItemStack stack, int amount) {
      this.awaiting.addTo(stack, amount);
   }

   public static class RequestEntry {
      ItemStack stack;
      int amount;
      IFilter filter;

      public RequestEntry(IInputBuffer buffer) {
         this.stack = buffer.readItemStack();
         this.amount = buffer.readInt();
      }

      public RequestEntry(ItemStack stack, int amount, boolean nbt) {
         this.setData(stack, amount, nbt);
      }

      public static RequestTubeTileEntity.RequestEntry read(CompoundTag nbt, boolean isNBT) {
         ItemStack stack = ItemStack.of(nbt);
         return stack.isEmpty() ? null : new RequestTubeTileEntity.RequestEntry(stack, nbt.getInt("amount"), isNBT);
      }

      public void setData(ItemStack stack, int amount, boolean nbt) {
         this.stack = stack;
         this.amount = amount;
         this.updateNBT(nbt);
      }

      public void updateNBT(boolean nbt) {
         this.filter = new StackFilter(this.stack, 4 | (nbt ? 16 : 0) | (this.stack.getItem() instanceof ITagItem ? 32 : 0));
      }

      public void write(IOutputBuffer buffer) {
         buffer.writeItemStack(this.stack);
         buffer.writeInt(this.amount);
      }

      public IFilter getFilter() {
         return this.filter;
      }

      public CompoundTag save() {
         CompoundTag nbt = this.stack.save(new CompoundTag());
         nbt.putInt("amount", this.amount);
         return nbt;
      }

      public int getAmount() {
         return this.amount;
      }

      public ItemStack getStack() {
         return this.stack;
      }

      public boolean isValid() {
         return !this.stack.isEmpty();
      }
   }
}
