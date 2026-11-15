package ic2.core.block.machines.tiles.nv;

import ic2.api.core.IC2Classic;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.network.tile.INetworkDataEventListener;
import ic2.api.tiles.tubes.IRequestTube;
import ic2.api.tiles.tubes.ITube;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.api.util.DirectionList;
import ic2.api.util.ILocation;
import ic2.core.IC2;
import ic2.core.block.base.cache.CapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.IDropProvider;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.block.machines.containers.nv.IndustrialWorkbenchContainer;
import ic2.core.block.machines.logic.crafter.CraftRecipe;
import ic2.core.block.machines.logic.crafter.CraftingCache;
import ic2.core.block.machines.logic.crafter.CraftingList;
import ic2.core.block.machines.logic.crafter.IMemorySlotProvider;
import ic2.core.block.machines.logic.crafter.IWorkbenchExpansion;
import ic2.core.block.machines.logic.crafter.Snapshot;
import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.block.transport.item.logistic.TubeNet;
import ic2.core.fluid.ArrayFluidHandler;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.IngredientNBTFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.inv.IC2CraftingInventory;
import ic2.core.inventory.inv.INotifyInventory;
import ic2.core.inventory.inv.InventoryWrapper;
import ic2.core.inventory.inv.ListenerInventory;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.inventory.transporter.transporters.special.DirectionalTransporter;
import ic2.core.inventory.transporter.transporters.special.ListTransporter;
import ic2.core.inventory.transporter.transporters.special.NonEmptyTransporter;
import ic2.core.item.misc.MemoryStickItem;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.AABBUtil;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.network.chat.HoverEvent.ItemStackInfo;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

public class IndustrialWorkbenchTileEntity
   extends BaseInventoryTileEntity
   implements INotifyInventory,
   ILocation,
   ITileGui,
   IMemorySlotProvider,
   IWrenchableTile,
   IDropProvider,
   INetworkDataEventListener,
   ITube,
   IRequestTube,
   ITickListener {
   public static final int CRAFT_RECURSIVE = 1;
   public static final int RESTOCK = 2;
   static final CraftingContainer CRAFTING = new IC2CraftingInventory(3, 3);
   List<Recipe<?>> recipesToUnlock = CollectionUtils.<Recipe<?>>createList();
   List<IWorkbenchExpansion> expansions = CollectionUtils.<IWorkbenchExpansion>createList();
   public SimpleInventory garbageSlot = new SimpleInventory(9);
   public SimpleInventory memory = new ListenerInventory(2, this);
   public SimpleInventory crafting_inventory = new SimpleInventory(10);
   @NetworkInfo
   public CraftRecipe currentRecipe = new CraftRecipe();
   public CraftingList recipes = new CraftingList(27).setSlotsSaved(0, 9);
   public List<IndustrialWorkbenchTileEntity.RequestEntry> toRequest = new ObjectArrayList();
   public List<ItemStack> toInsert = new ObjectArrayList();
   public SimpleInventory requestedItem = new SimpleInventory(1);
   public SimpleInventory requestCache = new SimpleInventory(12);
   public Object2IntLinkedOpenCustomHashMap<ItemStack> requested = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(false));
   public Object2IntLinkedOpenCustomHashMap<ItemStack> awaiting = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.INSTANCE);
   public MutableBoolean viewOpen = new MutableBoolean();
   public UUID requestId = UUID.randomUUID();
   @NetworkInfo
   public DyeColor requestColor = null;
   protected boolean addedToTubeNet = false;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int flags = 0;
   @NetworkInfo
   public int enabledSlots = 511;
   protected Snapshot snapshot = null;
   ICache<IItemHandler> items = new CapabilityCache<>(this, DirectionList.ALL, ForgeCapabilities.ITEM_HANDLER);
   ICache<IFluidHandler> fluids = new CapabilityCache<>(this, DirectionList.ALL, ForgeCapabilities.FLUID_HANDLER);

   public IndustrialWorkbenchTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 27);
      this.addCaches(new ICache[]{this.items, this.fluids});
      this.addGuiFields(new String[]{"flags", "enabledSlots", "requestColor"});
      this.addCapability(IC2Classic.TUBE_CAPABILITY, this);
   }

   public static int getExpansionLimit() {
      return IC2.CONFIG.industrialWorkbenchExpansionLimit.get();
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      int[] slots = MathUtils.fromTo(0, 27);
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotsForSide(DirectionList.ALL, slots);
      handler.registerSlotAccess(AccessRule.BOTH, slots);
      handler.registerNamedSlot(SlotType.STORAGE, slots);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.INDUSTRIAL_WORKBENCH;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new IndustrialWorkbenchContainer(this, player, windowID);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.put(compound, "garbage", this.garbageSlot.save(new CompoundTag()));
      NBTUtils.put(compound, "memory", this.memory.save(new CompoundTag()));
      NBTUtils.put(compound, "display", this.crafting_inventory.save(new CompoundTag()));
      NBTUtils.put(compound, "current", this.currentRecipe.writeRecipe(new CompoundTag()));
      NBTUtils.put(compound, "recipes", this.recipes.save(new CompoundTag()));
      NBTUtils.putByte(compound, "flags", (byte)this.flags, 0);
      NBTUtils.putEnum(compound, "request_color", this.requestColor);
      compound.putUUID("request_id", this.requestId);
      ListTag list = new ListTag();

      for (IndustrialWorkbenchTileEntity.RequestEntry entry : this.toRequest) {
         CompoundTag data = entry.getKey().save(new CompoundTag());
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
      this.garbageSlot.load(compound.getCompound("garbage"));
      this.memory.load(compound.getCompound("memory"));
      this.crafting_inventory.load(compound.getCompound("display"));
      this.currentRecipe.readRecipe(compound.getCompound("current"));
      this.recipes.load(compound.getCompound("recipes"));
      this.flags = compound.getInt("flags");
      this.requestColor = NBTUtils.getEnum(compound, "request_color", DyeColor.class);

      for (CompoundTag data : NBTListWrapper.wrap(compound.getList("awaiting", 10), CompoundTag.class)) {
         ItemStack stack = ItemStack.of(data);
         if (!stack.isEmpty()) {
            this.toRequest.add(new IndustrialWorkbenchTileEntity.RequestEntry(stack, data.getInt("awaiting")));
         }
      }

      for (CompoundTag datax : NBTListWrapper.wrap(compound.getList("toInsert", 10), CompoundTag.class)) {
         ItemStack stack = ItemStack.of(datax);
         if (!stack.isEmpty()) {
            this.toInsert.add(stack);
         }
      }

      this.syncRequests();
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      super.addDrops(drops);
      this.memory.addToDrops(drops);
      this.garbageSlot.addToDrops(drops);
   }

   @Override
   public void onDataBufferReceived(Player player, String id, INetworkDataBuffer data, Dist target) {
      if (target.isDedicatedServer() && id.equalsIgnoreCase("jei") && data instanceof NBTBuffer buffer) {
         this.crafting_inventory.clear();

         for (CompoundTag compound : NBTListWrapper.wrap(buffer.getNBT().getList("RecipeData", 10), CompoundTag.class)) {
            int slot = compound.getInt("SlotID");
            this.crafting_inventory.setStackInSlot(slot, ItemStack.of(compound));
         }

         this.updateRecipe();
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         this.currentRecipe.flags = value;
         this.currentRecipe.clearFilters();
         this.updateGuiField("currentRecipe");
      } else if (key == 1) {
         this.flags = value;
         this.updateGuiField("flags");
      } else if (key == 2) {
         if (value == 0) {
            IndustrialWorkbenchContainer container = this.getContainer(entity, IndustrialWorkbenchContainer.class);
            if (container == null || !this.currentRecipe.isValid()) {
               return;
            }

            if (!container.isSaving() && !container.getCarried().isEmpty()) {
               return;
            }

            if (container.isSaving()) {
               container.setCarried(ItemStack.EMPTY);
               container.setSaving(false);
            } else {
               container.setCarried(this.currentRecipe.getDisplayItem());
               container.setSaving(true);
            }

            ((ServerPlayer)entity).connection.send(new ClientboundContainerSetSlotPacket(-1, container.incrementStateId(), -1, container.getCarried()));
         } else {
            this.crafting_inventory.clear();
            this.currentRecipe.clear();
            entity.containerMenu.broadcastChanges();
         }
      } else if (key == 3) {
         this.requestColor = value == 0 ? null : DyeColor.byId(value - 1);
         this.updateGuiField("requestColor");
      } else if (key == 4) {
         if (this.toRequest.size() + this.requested.size() >= 12 || value <= 0 || value > 512 || this.requestedItem.getStackInSlot(0).isEmpty()) {
            return;
         }

         this.toRequest.add(new IndustrialWorkbenchTileEntity.RequestEntry(StackUtil.copyWithSize(this.requestedItem.getStackInSlot(0), 1), value));
         this.syncRequests();
      } else if (key == 5) {
         this.removeRequest(value);
      } else if (key == 6) {
         this.tryRequestingItems(entity);
      }
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (!this.isRendering()) {
         this.updateSlots();
         this.recipes.validateRecipes(this.getLevel());
         if (this.currentRecipe.validate(this.getLevel())) {
            this.crafting_inventory.setStackInSlot(9, this.currentRecipe.getFakeOutput(this.getLevel()));
         } else {
            this.currentRecipe.clear();
            this.crafting_inventory.setStackInSlot(9, ItemStack.EMPTY);
         }

         AABBUtil.countBlocks(this.getLevel(), this.worldPosition, getExpansionLimit(), this.createFilter(), 0, DirectionList.ALL);
         if (!this.addedToTubeNet) {
            this.addedToTubeNet = true;
            TubeNet.INSTANCE.addTube(this);
         }
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.addedToTubeNet) {
         this.addedToTubeNet = false;
         TubeNet.INSTANCE.removeTube(this);
      }

      super.onUnloaded(chunk);
   }

   public void addExpansion(IWorkbenchExpansion expansion) {
      if (this.expansions.size() < getExpansionLimit() && expansion.addUnloadListener(this)) {
         this.expansions.add(expansion);
      }
   }

   public void onExpansionUnloaded(IWorkbenchExpansion expansion) {
      this.expansions.remove(expansion);
   }

   protected AABBUtil.IBlockFilter createFilter() {
      return new AABBUtil.IBlockFilter() {
         @Override
         public boolean isValid(LevelReader world, BlockPos pos) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IWorkbenchExpansion) {
               IndustrialWorkbenchTileEntity.this.addExpansion((IWorkbenchExpansion)tile);
               return IndustrialWorkbenchTileEntity.this.expansions.size() < IndustrialWorkbenchTileEntity.getExpansionLimit();
            } else {
               return false;
            }
         }

         @Override
         public boolean isValid(BlockState state) {
            return false;
         }
      };
   }

   public void createSnapshot() {
      if (this.snapshot != null) {
         throw new IllegalStateException("Snapshot already present");
      } else {
         this.snapshot = new Snapshot(2);
         this.snapshot.set(0, SimpleInventory.createCopy(this));
         this.snapshot.set(1, SimpleInventory.createCopy(this.garbageSlot));
         int i = 0;

         for (int m = this.expansions.size(); i < m; i++) {
            this.expansions.get(i).createSnapshot();
         }
      }
   }

   public void releaseSnapshot() {
      if (this.snapshot == null) {
         throw new IllegalStateException("Snapshot is missing");
      } else {
         this.snapshot.get(0, SimpleInventory.class).overrideTo(this);
         this.snapshot.get(1, SimpleInventory.class).overrideTo(this.garbageSlot);
         this.snapshot = null;
         int i = 0;

         for (int m = this.expansions.size(); i < m; i++) {
            this.expansions.get(i).releaseSnapshot();
         }
      }
   }

   public void destroySnapshot() {
      if (this.snapshot == null) {
         throw new IllegalStateException("Snapshot is missing");
      } else {
         this.snapshot = null;
         int i = 0;

         for (int m = this.expansions.size(); i < m; i++) {
            this.expansions.get(i).destroySnapshot();
         }
      }
   }

   public Snapshot[] takeSnapshot() {
      if (this.snapshot == null) {
         return null;
      } else {
         Snapshot shot = this.snapshot;
         this.snapshot = null;
         Snapshot[] data = new Snapshot[1 + this.expansions.size()];
         data[0] = shot;
         int i = 0;

         for (int m = this.expansions.size(); i < m; i++) {
            data[1 + i] = this.expansions.get(i).takeSnapshot();
         }

         return data;
      }
   }

   public void putSnapshot(Snapshot[] snapshot) {
      if (this.snapshot != null) {
         throw new IllegalStateException("Snapshot already present");
      } else {
         this.snapshot = snapshot[0];
         int i = 0;

         for (int m = this.expansions.size(); i < m; i++) {
            this.expansions.get(i).setSnapshot(snapshot[1 + i]);
         }
      }
   }

   public IItemTransporter getTransporter() {
      List<IItemTransporter> transporter = CollectionUtils.<IItemTransporter>createList();
      transporter.add(TransporterManager.getTransporter(this.garbageSlot));
      List<IItemTransporter> helper = CollectionUtils.<IItemTransporter>createList();
      int i = 0;

      for (int m = this.expansions.size(); i < m; i++) {
         this.expansions.get(i).createTrashTransporter(false, helper, transporter);
      }

      for (Direction dir : this.items) {
         IItemHandler handler = this.items.getHandler(dir);
         if (handler != null) {
            transporter.add(new DirectionalTransporter(TransporterManager.getTransporter(handler), dir.getOpposite()));
         }
      }

      return new ListTransporter(transporter);
   }

   public IFluidHandler getFluids() {
      List<IFluidHandler> list = CollectionUtils.<IFluidHandler>createList();

      for (Direction dir : this.fluids) {
         IFluidHandler handler = this.fluids.getHandler(dir);
         if (handler != null) {
            list.add(handler);
         }
      }

      return (IFluidHandler)(list.isEmpty() ? EmptyFluidHandler.INSTANCE : new ArrayFluidHandler(list));
   }

   public void triggerRestock(Player player) {
      if (this.snapshot != null) {
         if ((this.flags & 2) == 0) {
            this.destroySnapshot();
         } else {
            IItemTransporter transporter = this.getTransporter();
            IFluidHandler handler = this.getFluids();
            NonNullList<ItemStack> toAdd = IWorkbenchExpansion.restockInventory(
               transporter, this, this.snapshot.get(0, SimpleInventory.class).getDifference(this, true)
            );
            IItemTransporter trans = TransporterManager.getTransporter(new InventoryWrapper(this));
            int i = 0;

            for (int m = toAdd.size(); i < m; i++) {
               ItemStack stack = (ItemStack)toAdd.get(i);
               stack.shrink(trans.addItem(stack, null, false));
               StackUtil.addOrDrop(player, stack);
            }

            i = 0;

            for (int m = this.expansions.size(); i < m; i++) {
               this.expansions.get(i).triggerRestock(transporter, handler, player);
            }

            this.destroySnapshot();
         }
      }
   }

   public boolean saveRecipe(int index) {
      if ((this.enabledSlots & 1 << index) == 0) {
         return false;
      } else {
         CraftRecipe recipe = this.currentRecipe.copy();
         if (recipe.validate(this.getLevel())) {
            this.recipes.saveRecipe(index, recipe);
            if (index >= 9) {
               ItemStack stack = this.memory.getStackInSlot((index - 9) / 9);
               if (stack.getItem() instanceof MemoryStickItem) {
                  MemoryStickItem.saveRecipe(stack, index % 9, recipe);
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public void deleteRecipe(int index) {
      this.recipes.removeRecipe(index);
      if (index >= 9) {
         ItemStack stack = this.memory.getStackInSlot((index - 9) / 9);
         if (stack.getItem() instanceof MemoryStickItem) {
            MemoryStickItem.deleteRecipe(stack, index % 9);
         }
      }
   }

   public void setRecipe(int index) {
      this.setRecipe(this.recipes.getCopy(index));
   }

   public void setRecipe(CraftRecipe recipe) {
      if (recipe != null && recipe.validate(this.getLevel())) {
         this.currentRecipe = recipe;
         this.currentRecipe.applyTo(this.crafting_inventory, this.getLevel());
         this.updateGuiField("currentRecipe");
      } else {
         this.currentRecipe.clear();
         this.crafting_inventory.clear();
         this.updateGuiField("currentRecipe");
      }
   }

   @Override
   public void onNotify(IHasInventory inventory, int slot) {
      if (inventory == this.memory && !this.isRendering()) {
         ItemStack stack = inventory.getStackInSlot(slot);
         if (stack.isEmpty()) {
            for (int i = 0; i < 9; i++) {
               int index = 9 + slot * 9 + i;
               this.enabledSlots &= ~(1 << index);
               this.recipes.removeRecipe(index);
            }
         } else if (stack.getItem() instanceof MemoryStickItem) {
            for (int i = 0; i < 9; i++) {
               int index = 9 + slot * 9 + i;
               this.enabledSlots |= 1 << index;
               CraftRecipe recipe = MemoryStickItem.loadRecipe(stack, i);
               if (recipe != null && recipe.validate(this.getLevel())) {
                  this.recipes.saveRecipe(index, recipe);
               } else {
                  this.recipes.removeRecipe(index);
               }
            }
         }

         this.updateGuiField("enabledSlots");
      }
   }

   public void updateSlots() {
      this.onNotify(this.memory, 0);
      this.onNotify(this.memory, 1);

      for (int i = 0; i < 2; i++) {
         ItemStack stick = this.memory.getStackInSlot(i);
         if (stick.getItem() instanceof MemoryStickItem) {
            this.enabledSlots |= 511 << 9 * (i + 1);
         } else {
            this.enabledSlots &= ~(511 << 9 * (i + 1));
         }
      }

      this.updateGuiField("enabledSlots");
   }

   public void updateRecipe() {
      for (int i = 0; i < 9; i++) {
         CRAFTING.setItem(i, this.crafting_inventory.getStackInSlot(i).copy());
      }

      CraftingRecipe recipe = (CraftingRecipe)IC2.PLATFORM.getRecipes().getRecipeFor(RecipeType.CRAFTING, CRAFTING, this.getLevel()).orElse(null);
      if (recipe != null) {
         this.currentRecipe.setRecipe(recipe, this.crafting_inventory);
         this.crafting_inventory.setStackInSlot(9, this.currentRecipe.getOutput(CRAFTING, this.getLevel()));
      } else {
         this.currentRecipe.clear();
         this.crafting_inventory.setStackInSlot(9, ItemStack.EMPTY);
      }

      CRAFTING.clearContent();
   }

   protected List<IWorkbenchExpansion> createExpansions() {
      List<IWorkbenchExpansion> expand = CollectionUtils.<IWorkbenchExpansion>createList();
      int i = 0;

      for (int m = this.expansions.size(); i < m; i++) {
         IWorkbenchExpansion work = this.expansions.get(i);
         if (work.needsRecipeInfo()) {
            expand.add(work);
         }
      }

      return expand;
   }

   protected List<CraftingList> createRecipes() {
      List<CraftingList> expand = CollectionUtils.<CraftingList>createList();
      expand.add(this.recipes);
      int i = 0;

      for (int m = this.expansions.size(); i < m; i++) {
         CraftingList recipe = this.expansions.get(i).getExpansionRecipeList();
         if (recipe != null) {
            expand.add(recipe);
         }
      }

      return expand;
   }

   protected CraftingCache createCache(boolean simulate) {
      List<IItemTransporter>[] transporters = CollectionUtils.createLists(4);
      if (simulate) {
         IHasInventory inventory = SimpleInventory.createCopy(this);
         IHasInventory trash = SimpleInventory.createCopy(this.garbageSlot);
         transporters[0].add(new NonEmptyTransporter(inventory));
         transporters[1].add(TransporterManager.getTransporter(inventory));
         IItemTransporter transporter = TransporterManager.getTransporter(trash);
         transporters[2].add(transporter);
         transporters[3].add(transporter);
      } else {
         transporters[0].add(new NonEmptyTransporter(this));
         transporters[1].add(TransporterManager.getTransporter(new InventoryWrapper(this)));
         IItemTransporter transporter = TransporterManager.getTransporter(this.garbageSlot);
         transporters[2].add(transporter);
         transporters[3].add(transporter);
      }

      int i = 0;

      for (int m = this.expansions.size(); i < m; i++) {
         IWorkbenchExpansion work = this.expansions.get(i);
         work.createMainTransporter(simulate, transporters[0], transporters[1]);
         work.createTrashTransporter(simulate, transporters[2], transporters[3]);
      }

      return new CraftingCache(
         new ListTransporter(new ListTransporter(transporters[0]), new ListTransporter(transporters[2])),
         new ListTransporter(new ListTransporter(transporters[3]), new ListTransporter(transporters[1])),
         this.createRecipes(),
         this.createExpansions()
      );
   }

   public int getCraftLimit() {
      if (!this.currentRecipe.isValid()) {
         return 0;
      } else {
         int max = this.currentRecipe.getDisplayItem().getMaxStackSize();
         return max == 1 ? 64 : max;
      }
   }

   public boolean canCraft(Player player) {
      return this.currentRecipe.isValid() ? !this.tryCraft(player, this.currentRecipe, this.createCache(true), false, true).isEmpty() : false;
   }

   public ItemStack craft(Player player) {
      return this.currentRecipe.isValid() ? this.tryCraft(player, this.currentRecipe, this.createCache(false), true, false) : ItemStack.EMPTY;
   }

   public ItemStack tryCraft(Player player, CraftRecipe recipe, CraftingCache craftingData, boolean doCraft, boolean doLog) {
      MutableInt craftsLeft = new MutableInt(512);
      SimpleInventory tempJunkJard = new SimpleInventory(15);
      CraftingCache actualCache = craftingData.createSubCache(tempJunkJard);
      NonNullList<ItemStack> craftItems = NonNullList.withSize(9, ItemStack.EMPTY);
      int[] toConsume = new int[9];
      ObjectIterator output = Object2ObjectMaps.fastIterable(recipe.getFilters()).iterator();

      while (output.hasNext()) {
         Entry<IFilter, int[]> items = (Entry<IFilter, int[]>)output.next();
         int[] stackInfo = (int[])items.getValue();
         actualCache.setActiveRecipe(recipe, stackInfo[0]);
         ItemStack provided = actualCache.getProvider().removeItem((IFilter)items.getKey(), null, stackInfo[1], false);
         actualCache.clearActiveRecipe();
         this.merge(recipe.getToConsume(), toConsume);
         if (provided.isEmpty()) {
            if ((this.flags & 1) == 0) {
               if (doLog) {
                  player.displayClientMessage(this.translate("info.block.ic2.industrial_workbench.subcrafting_disabled"), false);
               }

               craftingData.setMissingItem((IFilter)items.getKey(), stackInfo[1]);
               return ItemStack.EMPTY;
            }

            CraftRecipe superRecipe = craftingData.getRecipeFromFilter((IFilter)items.getKey());
            if (superRecipe == null) {
               if (doLog) {
                  ItemStack stack = recipe.getFilter((IFilter)items.getKey());
                  player.displayClientMessage(
                     this.translate("info.block.ic2.industrial_workbench.no_recipe", new Object[]{stack.getHoverName()})
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_ITEM, new ItemStackInfo(stack)))),
                     false
                  );
               }

               craftingData.setMissingItem((IFilter)items.getKey(), stackInfo[1]);
               return ItemStack.EMPTY;
            }

            provided = this.trySubCraft(player, superRecipe, craftingData, 6, craftsLeft, doCraft, doLog);
            if (provided.isEmpty()) {
               if (doLog) {
                  ItemStack stack = recipe.getFilter((IFilter)items.getKey());
                  player.displayClientMessage(
                     this.translate("info.block.ic2.industrial_workbench.missing_materials", new Object[]{stack.getHoverName()})
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_ITEM, new ItemStackInfo(stack)))),
                     false
                  );
               }

               craftingData.setMissingItem((IFilter)items.getKey(), stackInfo[1]);
               return ItemStack.EMPTY;
            }

            this.recipesToUnlock.add(superRecipe.getRecipe());
         }

         while (provided.getCount() < stackInfo[1]) {
            if ((this.flags & 1) == 0) {
               if (doLog) {
                  player.displayClientMessage(this.translate("info.block.ic2.industrial_workbench.subcrafting_disabled"), false);
               }

               craftingData.setMissingItem((IFilter)items.getKey(), stackInfo[1] - provided.getCount());
               return ItemStack.EMPTY;
            }

            CraftRecipe superRecipex = craftingData.getRecipeFromFilter((IFilter)items.getKey());
            if (superRecipex == null) {
               if (doLog) {
                  ItemStack stack = recipe.getFilter((IFilter)items.getKey());
                  player.displayClientMessage(
                     this.translate("info.block.ic2.industrial_workbench.no_recipe", new Object[]{stack.getHoverName()})
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_ITEM, new ItemStackInfo(stack)))),
                     false
                  );
               }

               craftingData.setMissingItem((IFilter)items.getKey(), stackInfo[1] - provided.getCount());
               return ItemStack.EMPTY;
            }

            ItemStack crafted = this.trySubCraft(player, superRecipex, craftingData, 6, craftsLeft, doCraft, doLog);
            if (crafted.isEmpty()) {
               if (doLog) {
                  ItemStack stack = recipe.getFilter((IFilter)items.getKey());
                  player.displayClientMessage(
                     this.translate("info.block.ic2.industrial_workbench.missing_materials", new Object[]{stack.getHoverName()})
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_ITEM, new ItemStackInfo(stack)))),
                     false
                  );
               }

               craftingData.setMissingItem((IFilter)items.getKey(), stackInfo[1] - provided.getCount());
               return ItemStack.EMPTY;
            }

            provided.grow(crafted.getCount());
            this.recipesToUnlock.add(superRecipex.getRecipe());
         }

         if (provided.getCount() > stackInfo[1]) {
            actualCache.getOutput().addItem(provided.split(provided.getCount() - stackInfo[1]), null, false);
         }

         craftItems.set(stackInfo[0], provided);
      }

      CRAFTING.clearContent();

      for (int i = 0; i < 9; i++) {
         CRAFTING.setItem(i, ((ItemStack)craftItems.get(i)).copy());
      }

      ItemStack outputx = recipe.getOutput(CRAFTING, this.getLevel());
      if (outputx.isEmpty()) {
         if (doLog) {
            player.displayClientMessage(
               this.translate("info.block.ic2.industrial_workbench.no_output", new Object[]{recipe.getDisplayItem().getHoverName()}), false
            );
         }

         return ItemStack.EMPTY;
      } else {
         if (doCraft) {
            ForgeEventFactory.firePlayerCraftingEvent(player, outputx, CRAFTING);
            this.recipesToUnlock.add(recipe.getRecipe());
            player.awardRecipes(this.recipesToUnlock);
            this.recipesToUnlock.clear();
         }

         ForgeHooks.setCraftingPlayer(player);
         NonNullList<ItemStack> leftovers = NonNullList.create();
         NonNullList<ItemStack> remaining = recipe.getRemainingItems(CRAFTING, this.getLevel());
         ForgeHooks.setCraftingPlayer(null);

         for (int i = 0; i < 9; i++) {
            ItemStack subStack = CRAFTING.removeItemNoUpdate(i);
            if (!subStack.isEmpty()) {
               subStack.shrink(1);
               if (!subStack.isEmpty()) {
                  leftovers.add(subStack);
               }
            }

            subStack = (ItemStack)remaining.get(i);
            if (toConsume[i] > 0) {
               subStack.shrink(toConsume[i]);
            }

            if (!subStack.isEmpty()) {
               leftovers.add(subStack);
            }
         }

         int i = 0;

         for (int m = tempJunkJard.getSlotCount(); i < m; i++) {
            ItemStack stack = tempJunkJard.getStackInSlot(i);
            if (!stack.isEmpty()) {
               leftovers.add(stack);
            }
         }

         i = 0;

         for (int mx = leftovers.size(); i < mx; i++) {
            ItemStack stack = (ItemStack)leftovers.get(i);
            if (!stack.isEmpty() && craftingData.getOutput().addItem(stack, null, false) < stack.getCount()) {
               if (doLog) {
                  player.displayClientMessage(this.translate("info.block.ic2.industrial_workbench.junkyard_clearing_error"), false);
               }

               craftingData.setMissingItem(null, 0);
               return ItemStack.EMPTY;
            }
         }

         return outputx;
      }
   }

   public ItemStack trySubCraft(Player player, CraftRecipe recipe, CraftingCache cache, int layer, MutableInt crafts, boolean doCraft, boolean doLog) {
      if (layer <= 0) {
         if (doLog) {
            player.displayClientMessage(this.translate("info.block.ic2.industrial_workbench.to_complex"), false);
         }

         return ItemStack.EMPTY;
      } else if (crafts.decrementAndGet() < 0) {
         if (doLog) {
            player.displayClientMessage(this.translate("info.block.ic2.industrial_workbench.to_many_crafts"), false);
         }

         return ItemStack.EMPTY;
      } else {
         int[] toConsume = new int[9];
         NonNullList<ItemStack> craftItems = NonNullList.withSize(9, ItemStack.EMPTY);
         ObjectIterator output = Object2ObjectMaps.fastIterable(recipe.getFilters()).iterator();

         while (output.hasNext()) {
            Entry<IFilter, int[]> items = (Entry<IFilter, int[]>)output.next();
            int[] stackInfo = (int[])items.getValue();
            cache.setActiveRecipe(recipe, stackInfo[0]);
            ItemStack provided = cache.getProvider().removeItem((IFilter)items.getKey(), null, stackInfo[1], false);
            cache.clearActiveRecipe();
            this.merge(recipe.getToConsume(), toConsume);
            if (provided.isEmpty()) {
               CraftRecipe superRecipe = cache.getRecipeFromFilter((IFilter)items.getKey());
               if (superRecipe == null) {
                  if (doLog) {
                     ItemStack stack = recipe.getFilter((IFilter)items.getKey());
                     player.displayClientMessage(
                        this.translate("info.block.ic2.industrial_workbench.no_recipe", new Object[]{stack.getHoverName()})
                           .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_ITEM, new ItemStackInfo(stack)))),
                        false
                     );
                  }

                  cache.setMissingItem((IFilter)items.getKey(), stackInfo[1]);
                  return ItemStack.EMPTY;
               }

               provided = this.trySubCraft(player, superRecipe, cache, layer - 1, crafts, doCraft, doLog);
               if (provided.isEmpty()) {
                  if (doLog) {
                     ItemStack stack = recipe.getFilter((IFilter)items.getKey());
                     player.displayClientMessage(
                        this.translate("info.block.ic2.industrial_workbench.missing_materials", new Object[]{stack.getHoverName()})
                           .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_ITEM, new ItemStackInfo(stack)))),
                        false
                     );
                  }

                  cache.setMissingItem((IFilter)items.getKey(), stackInfo[1]);
                  return ItemStack.EMPTY;
               }

               this.recipesToUnlock.add(superRecipe.getRecipe());
            }

            while (provided.getCount() < stackInfo[1]) {
               CraftRecipe superRecipex = cache.getRecipeFromFilter((IFilter)items.getKey());
               if (superRecipex == null) {
                  if (doLog) {
                     ItemStack stack = recipe.getFilter((IFilter)items.getKey());
                     player.displayClientMessage(
                        this.translate("info.block.ic2.industrial_workbench.no_recipe", new Object[]{stack.getHoverName()})
                           .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_ITEM, new ItemStackInfo(stack)))),
                        false
                     );
                  }

                  cache.setMissingItem((IFilter)items.getKey(), stackInfo[1] - provided.getCount());
                  return ItemStack.EMPTY;
               }

               ItemStack crafted = this.trySubCraft(player, superRecipex, cache, 6, crafts, doCraft, doLog);
               if (crafted.isEmpty()) {
                  if (doLog) {
                     ItemStack stack = recipe.getFilter((IFilter)items.getKey());
                     player.displayClientMessage(
                        this.translate("info.block.ic2.industrial_workbench.missing_materials", new Object[]{stack.getHoverName()})
                           .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(Action.SHOW_ITEM, new ItemStackInfo(stack)))),
                        false
                     );
                  }

                  cache.setMissingItem((IFilter)items.getKey(), stackInfo[1] - provided.getCount());
                  return ItemStack.EMPTY;
               }

               provided.grow(crafted.getCount());
               this.recipesToUnlock.add(superRecipex.getRecipe());
            }

            if (provided.getCount() > stackInfo[1]) {
               cache.getOutput().addItem(provided.split(provided.getCount() - stackInfo[1]), null, false);
            }

            craftItems.set(stackInfo[0], provided);
         }

         CRAFTING.clearContent();

         for (int i = 0; i < 9; i++) {
            CRAFTING.setItem(i, ((ItemStack)craftItems.get(i)).copy());
         }

         ItemStack outputx = recipe.getOutput(CRAFTING, this.getLevel());
         if (outputx.isEmpty()) {
            if (doLog) {
               player.displayClientMessage(
                  this.translate("info.block.ic2.industrial_workbench.no_output", new Object[]{recipe.getDisplayItem().getHoverName()}), false
               );
            }

            return ItemStack.EMPTY;
         } else {
            if (doCraft) {
               ForgeEventFactory.firePlayerCraftingEvent(player, outputx, CRAFTING);
            }

            ForgeHooks.setCraftingPlayer(player);
            NonNullList<ItemStack> leftOvers = NonNullList.create();
            NonNullList<ItemStack> remaining = recipe.getRemainingItems(CRAFTING, this.getLevel());
            ForgeHooks.setCraftingPlayer(null);

            for (int i = 0; i < 9; i++) {
               ItemStack subStack = CRAFTING.removeItemNoUpdate(i);
               if (!subStack.isEmpty()) {
                  subStack.shrink(1);
                  if (!subStack.isEmpty()) {
                     leftOvers.add(subStack);
                  }
               }

               subStack = (ItemStack)remaining.get(i);
               if (toConsume[i] > 0) {
                  subStack.shrink(toConsume[i]);
               }

               if (!subStack.isEmpty()) {
                  leftOvers.add(subStack);
               }
            }

            int i = 0;

            for (int m = leftOvers.size(); i < m; i++) {
               ItemStack stack = (ItemStack)leftOvers.get(i);
               if (!stack.isEmpty() && cache.getOutput().addItem(stack, null, false) < stack.getCount()) {
                  if (doLog) {
                     player.displayClientMessage(this.translate("info.block.ic2.industrial_workbench.junkyard_full"), false);
                  }

                  cache.setMissingItem(null, 0);
                  return ItemStack.EMPTY;
               }
            }

            return outputx;
         }
      }
   }

   @Override
   public int getEnabledSlots() {
      return this.enabledSlots;
   }

   @Override
   public boolean isServerSided() {
      return this.isSimulating();
   }

   @Override
   public CraftingList getRecipes() {
      return this.recipes;
   }

   @Override
   public boolean canSetFacing(Direction dir) {
      return dir != this.getFacing();
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public double getDropRate(Player player) {
      return 1.0;
   }

   protected void merge(int[] from, int[] to) {
      int min = Math.min(from.length, to.length);

      for (int i = 0; i < min; i++) {
         to[i] += from[i];
      }
   }

   @Override
   public boolean needsUpdateTick() {
      return super.needsUpdateTick() && !this.toInsert.isEmpty();
   }

   @Override
   public void onTick() {
      if (this.clock(20) && this.insertItems()) {
         this.removeFromTick();
      }
   }

   protected boolean insertItems() {
      IItemTransporter transporter = TransporterManager.getTransporter(new InventoryWrapper(this));
      Iterator<ItemStack> iter = this.toInsert.iterator();

      while (iter.hasNext()) {
         ItemStack next = iter.next();
         next.shrink(transporter.addItem(next, null, false));
         if (next.isEmpty()) {
            iter.remove();
         }
      }

      return this.toInsert.isEmpty();
   }

   public InteractionResult canAdvancedCraft(Player player) {
      if (this.currentRecipe.isValid()) {
         CraftingCache cache = this.createCache(true);
         if (!this.tryCraft(player, this.currentRecipe, cache, false, false).isEmpty()) {
            return InteractionResult.SUCCESS;
         } else if (cache.getMissingItem() == null) {
            return InteractionResult.SUCCESS;
         } else {
            return cache.getMissingItem().getKey() == null ? InteractionResult.FAIL : InteractionResult.PASS;
         }
      } else {
         return InteractionResult.CONSUME;
      }
   }

   public void tryRequestingItems(Player player) {
      if (!this.toRequest.isEmpty()) {
         player.displayClientMessage(this.translate("gui.ic2.industrial_workbench.already_requesting"), false);
      } else {
         InteractionResult craftResult = this.canAdvancedCraft(player);
         if (craftResult == InteractionResult.CONSUME) {
            player.displayClientMessage(this.translate("gui.ic2.industrial_workbench.no_recipe"), false);
         } else if (craftResult == InteractionResult.FAIL) {
            player.displayClientMessage(this.translate("gui.ic2.industrial_workbench.out_of_room"), false);
         } else if (craftResult != InteractionResult.PASS) {
            player.displayClientMessage(this.translate("gui.ic2.industrial_workbench.already_valid"), false);
         } else {
            Object2IntMap<Item> items = TubeNet.INSTANCE.getPotentialItems(this);
            if (items.isEmpty()) {
               player.displayClientMessage(this.translate("gui.ic2.industrial_workbench.empty_tube_net"), false);
            } else {
               SimpleInventory inventory = new SimpleInventory(15);
               Object2IntLinkedOpenCustomHashMap<ItemStack> requests = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.SIMPLE_INSTANCE);
               Object2IntLinkedOpenCustomHashMap<ItemStack> missingItems = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.SIMPLE_INSTANCE);
               IItemTransporter transporter = TransporterManager.getTransporter(inventory);

               for (int i = 0; i < 32; i++) {
                  CraftingCache cache = this.createCache(true).createSubCache(SimpleInventory.createCopy(inventory));
                  if (!this.tryCraft(player, this.currentRecipe, cache, false, false).isEmpty()) {
                     break;
                  }

                  if (!(cache.getMissingItem().getKey() instanceof IngredientNBTFilter nbt)) {
                     this.finishRequests(requests, player);
                     player.displayClientMessage(this.translate("gui.ic2.industrial_workbench.out_of_room"), false);
                     return;
                  }

                  int count = cache.getMissingItem().getIntValue();
                  if (count > 0) {
                     ItemStack result = ItemStack.EMPTY;

                     for (ItemStack stack : nbt.getItem().getItems()) {
                        if (items.getInt(stack.getItem()) >= count) {
                           result = stack;
                           break;
                        }
                     }

                     if (result.isEmpty()) {
                        result = nbt.getItem().getItems()[0];
                        if (transporter.addItem(StackUtil.copyWithSize(result, count), null, false) < count) {
                           player.displayClientMessage(this.translate("gui.ic2.industrial_workbench.stack_limit"), false);
                           return;
                        }

                        missingItems.addTo(result, count);
                     } else {
                        if (transporter.addItem(StackUtil.copyWithSize(result, count), null, false) < count) {
                           this.finishRequests(requests, player);
                           player.displayClientMessage(this.translate("gui.ic2.industrial_workbench.stack_limit"), false);
                           return;
                        }

                        requests.addTo(result, count);
                     }
                  }
               }

               this.finishRequests(requests, player);
               if (!missingItems.isEmpty()) {
                  MutableComponent text = this.string();
                  ObjectIterator<it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<ItemStack>> iter = Object2IntMaps.fastIterator(missingItems);

                  while (iter.hasNext()) {
                     it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<ItemStack> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<ItemStack>)iter.next();
                     text.append(entry.getIntValue() + "x ").append(((ItemStack)entry.getKey()).getDisplayName());
                     if (iter.hasNext()) {
                        text.append(", ");
                     }
                  }

                  player.displayClientMessage(this.translate("gui.ic2.industrial_workbench.missing_items", new Object[]{text}), false);
               }
            }
         }
      }
   }

   public void finishRequests(Object2IntMap<ItemStack> toRequest, Player player) {
      MutableComponent text = this.string();
      ObjectIterator<it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<ItemStack>> iter = Object2IntMaps.fastIterator(toRequest);

      while (iter.hasNext()) {
         it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<ItemStack> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<ItemStack>)iter.next();
         text.append(entry.getIntValue() + "x ").append(((ItemStack)entry.getKey()).getDisplayName());
         if (iter.hasNext()) {
            text.append(", ");
         }

         this.toRequest.add(new IndustrialWorkbenchTileEntity.RequestEntry((ItemStack)entry.getKey(), entry.getIntValue()));
      }

      player.displayClientMessage(this.translate("gui.ic2.industrial_workbench.requested_items", new Object[]{text}), false);
   }

   protected void syncRequests() {
      this.requestCache.clear();
      int slot = 0;
      int max = this.requestCache.getSlotCount();
      int i = 0;

      for (int m = this.toRequest.size(); i < m && i < max; i++) {
         IndustrialWorkbenchTileEntity.RequestEntry entry = this.toRequest.get(i);
         int size = entry.getIntValue();
         ItemStack stack = StackUtil.copyWithSize(entry.getKey(), Math.min(size, 64));
         stack.addTagElement("amount", IntTag.valueOf(size));
         if (this.awaiting.containsKey(entry.getKey())) {
            stack.addTagElement("state", ByteTag.valueOf((byte)2));
            stack.addTagElement("awaiting", IntTag.valueOf(this.awaiting.getInt(entry.getKey())));
         } else if (this.requested.containsKey(entry.getKey())) {
            stack.addTagElement("state", ByteTag.valueOf((byte)1));
         } else {
            stack.addTagElement("state", ByteTag.valueOf((byte)0));
         }

         this.requestCache.setStackInSlot(slot, stack);
         slot++;
      }
   }

   @Override
   public void addItem(ItemStack item, Direction side, DyeColor color) {
      this.toInsert.add(item);
      if (!this.insertItems()) {
         this.addToTick();
      }
   }

   @Override
   public void addItem(TransportedItem item, Direction side) {
      if (!this.isRendering()) {
         ItemStack stack = item.getServerStack();
         int awaited = stack.getCount();
         int i = 0;

         for (int m = this.toRequest.size(); i < m; i++) {
            IndustrialWorkbenchTileEntity.RequestEntry entry = this.toRequest.get(i);
            if (StackUtil.isStackEqual(entry.getKey(), stack)) {
               entry.setValue(entry.getIntValue() - awaited);
               if (entry.getIntValue() <= 0) {
                  this.toRequest.remove(i--);
                  m--;
               }
            }
         }

         i = this.awaiting.getInt(stack);
         if (i > 0) {
            this.awaiting.merge(stack, awaited, MathUtils.REMOVAL);
         }

         i = this.requested.getInt(stack);
         if (i > 0) {
            this.requested.merge(stack, awaited, MathUtils.REMOVAL);
         }

         this.toInsert.add(stack);
         if (!this.insertItems()) {
            this.addToTick();
         }

         this.syncRequests();
      }
   }

   @Override
   public boolean canAddItem(TransportedItem item, Direction side) {
      return this.requestId.equals(item.getRequestId());
   }

   @Override
   public boolean canConnect(ITube other, Direction dir) {
      return true;
   }

   @Override
   public ITube.TubeType getTubeType() {
      return ITube.TubeType.SIMPLE;
   }

   @Override
   public long getRequestSource() {
      return this.getBlockPos().asLong();
   }

   @Override
   public void provideRequests(IRequestTube.ITubeRequester requested) {
      if (!this.toRequest.isEmpty()) {
         boolean changed = false;
         int i = 0;

         for (int m = this.toRequest.size(); i < m; i++) {
            IndustrialWorkbenchTileEntity.RequestEntry entry = this.toRequest.get(i);
            ItemStack stack = entry.getKey();
            int amount = entry.getIntValue() - this.requested.getInt(stack);
            if (amount > 0) {
               requested.requestItems(stack, amount, this.requestColor, this.requestId);
               this.requested.addTo(stack, amount);
               changed = true;
            }
         }

         if (changed) {
            this.syncRequests();
         }
      }
   }

   @Override
   public void onRequestsReset() {
      this.requested.clear();
      this.awaiting.clear();
      this.syncRequests();
   }

   @Override
   public void onRequestFulfilled(ItemStack stack, int amount) {
      this.awaiting.put(stack, amount);
      this.syncRequests();
   }

   @Override
   public UUID getRequestId() {
      return this.requestId;
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

      this.syncRequests();
   }

   public void removeRequest(int slot) {
      if (slot >= 0 && slot < this.requestCache.getSlotCount()) {
         ItemStack toRemove = this.requestCache.getStackInSlot(slot);
         boolean found = false;
         Iterator<IndustrialWorkbenchTileEntity.RequestEntry> iter = this.toRequest.iterator();

         while (iter.hasNext()) {
            ItemStack next = iter.next().getKey();
            if (ItemStack.isSameItem(next, toRemove)) {
               iter.remove();
               toRemove = next;
               found = true;
               break;
            }
         }

         if (found) {
            this.awaiting.removeInt(toRemove);
            this.requested.clear();
            this.requested.putAll(this.awaiting);
            TubeNet.INSTANCE.clearRequests(this);
            this.syncRequests();
         }
      }
   }

   public static class RequestEntry {
      ItemStack key;
      int value;

      public RequestEntry(ItemStack key, int value) {
         this.key = key;
         this.value = value;
      }

      public ItemStack getKey() {
         return this.key;
      }

      public int getIntValue() {
         return this.value;
      }

      public void setValue(int value) {
         this.value = value;
      }
   }
}
