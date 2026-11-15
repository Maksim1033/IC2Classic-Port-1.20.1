package ic2.core.block.machines.tiles.ev;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.containers.ev.CrafterContainer;
import ic2.core.block.machines.logic.crafter.CraftRecipe;
import ic2.core.block.machines.logic.crafter.CraftingList;
import ic2.core.block.machines.logic.crafter.IMemorySlotProvider;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.inv.IC2CraftingInventory;
import ic2.core.inventory.inv.INotifyInventory;
import ic2.core.inventory.inv.InventoryWrapper;
import ic2.core.inventory.inv.ListenerInventory;
import ic2.core.inventory.inv.RangedInventory;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.misc.MemoryStickItem;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CrafterTileEntity extends BaseElectricTileEntity implements ITickListener, ITileGui, INotifyInventory, IMemorySlotProvider {
   static final CraftingContainer CRAFTING = new IC2CraftingInventory(3, 3);
   private boolean isCrafting = false;
   @NetworkInfo
   public int enabledSlots = 0;
   int flags = 0;
   LinkedList<ItemStack> clockUps = new LinkedList<>();
   public CraftingList recipeList = new CraftingList(9);
   public SimpleInventory upgrades = new ListenerInventory(3, this);

   public CrafterTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 18, 2048, 25000);
      this.addGuiFields(new String[]{"enabledSlots"});
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      int[] slots = MathUtils.fromTo(0, 18);
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotsForSide(DirectionList.ALL, slots);
      handler.registerSlotAccess(AccessRule.BOTH, slots);
      handler.registerInputFilter(this::isSlotValid, slots);
      handler.registerNamedSlot(SlotType.STORAGE, slots);
   }

   private boolean isSlotValid(int slot, ItemStack stack) {
      return this.isCrafting ? true : slot < 9 || this.recipeList.getRecipe(slot - 9) == null;
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      this.upgrades.addToDrops(drops);
      super.addDrops(drops);
   }

   @Override
   public boolean supportsNotify() {
      return true;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.CRAFTER;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new CrafterContainer(this, player, windowID);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putInt("flags", this.flags);
      compound.put("upgrades", this.upgrades.save(new CompoundTag()));
      ListTag clock = new ListTag();

      for (ItemStack stack : this.clockUps) {
         clock.add(stack.save(new CompoundTag()));
      }

      NBTUtils.put(compound, "clock", clock);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.flags = compound.getInt("flags");
      this.upgrades.load(compound.getCompound("upgrades"));

      for (CompoundTag nbt : NBTListWrapper.wrap(compound.getList("clock", 10), CompoundTag.class)) {
         ItemStack stack = ItemStack.of(nbt);
         if (!stack.isEmpty()) {
            this.clockUps.add(stack);
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
      return this.recipeList;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating()) {
         this.isCrafting = true;
         this.onNotify(this.upgrades, 2);
         this.isCrafting = false;
      }
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.isSimulating() && !this.isCrafting) {
         this.flags = 0;
      }
   }

   @Override
   public void onNotify(IHasInventory inventory, int slot) {
      if (slot == 2 && inventory == this.upgrades) {
         if (!this.isCrafting) {
            this.flags = 0;
         }

         ItemStack stack = inventory.getStackInSlot(slot);
         if (stack.isEmpty()) {
            for (int i = 0; i < 9; i++) {
               this.enabledSlots &= ~(1 << i);
               this.recipeList.removeRecipe(i);
            }
         } else if (stack.getItem() instanceof MemoryStickItem) {
            for (int i = 0; i < 9; i++) {
               this.enabledSlots |= 1 << i;
               CraftRecipe recipe = MemoryStickItem.loadRecipe(stack, i);
               if (recipe != null && recipe.validate(this.getLevel())) {
                  this.recipeList.saveRecipe(i, recipe);
               } else {
                  this.recipeList.removeRecipe(i);
               }
            }
         }

         this.updateGuiField("enabledSlots");
      }
   }

   public int getSpeed() {
      return Math.max(1, 20 - this.upgrades.getStackInSlot(1).getCount());
   }

   public int getCrafts() {
      return this.upgrades.getStackInSlot(0).getCount();
   }

   @Override
   public void onTick() {
      int crafts = this.getCrafts();
      if (!this.invClock(this.getSpeed()) && this.hasEnergy(crafts * 25) && !this.recipeList.isEmpty() && crafts > 0) {
         if (!this.clockUps.isEmpty()) {
            IItemTransporter transporter = TransporterManager.getTransporter(new RangedInventory(this, 0, 1, 2, 3, 4, 5, 6, 7, 8));
            Iterator<ItemStack> iter = this.clockUps.iterator();

            while (iter.hasNext()) {
               ItemStack stack = iter.next();
               stack.shrink(transporter.addItem(stack, null, false));
               if (stack.getCount() <= 0) {
                  iter.remove();
               }
            }
         } else {
            int thisCraft = crafts;
            this.isCrafting = true;
            IItemTransporter transporter = new CrafterTileEntity.CraftTransporter(new InventoryWrapper(this));

            while (thisCraft > 0) {
               boolean ranOut = true;

               for (int i = 0; i < 9 && thisCraft > 0; i++) {
                  CraftRecipe recipe = this.recipeList.getRecipe(i);
                  if (recipe != null && (this.flags & 3 << i * 2) == 0 && this.tryCraft(i, recipe, transporter)) {
                     thisCraft--;
                     ranOut = false;
                  }
               }

               if (ranOut) {
                  break;
               }
            }

            this.isCrafting = false;
            if (thisCraft != crafts) {
               this.useEnergy((crafts - thisCraft) * 25);
               this.flags = 0;
               this.notifyListeners();
            }

            this.handleComparators();
         }
      }
   }

   public boolean tryCraft(int slot, CraftRecipe recipe, IItemTransporter transporter) {
      ItemStack present = (ItemStack)this.inventory.get(slot + 9);
      if (!present.isEmpty() && StackUtil.getStackSizeLeft(present) < recipe.getDisplayItem().getCount()) {
         this.flags |= 1 << slot * 2 + 1;
         return false;
      } else {
         List<ItemStack> toInsertBack = CollectionUtils.<ItemStack>createList();
         ObjectIterator output = Object2ObjectMaps.fastIterable(recipe.getFilters()).iterator();

         while (output.hasNext()) {
            Entry<IFilter, int[]> items = (Entry<IFilter, int[]>)output.next();
            int[] stackInfo = (int[])items.getValue();
            ItemStack provided = transporter.removeItem((IFilter)items.getKey(), null, stackInfo[1], false);
            if (provided.isEmpty()) {
               this.reinsert(toInsertBack, transporter);
               this.flags |= 1 << slot * 2;
               return false;
            }

            toInsertBack.add(provided);
            if (provided.getCount() < stackInfo[1]) {
               this.reinsert(toInsertBack, transporter);
               this.flags |= 1 << slot * 2;
               return false;
            }

            CRAFTING.setItem(stackInfo[0], provided.copy());
         }

         ItemStack outputx = recipe.getOutput(CRAFTING, this.getLevel());
         if (!outputx.isEmpty() && this.canPlace(slot + 9, outputx)) {
            this.setOrGrow(slot + 9, outputx, false);
            NonNullList<ItemStack> remaining = recipe.getRemainingItems(CRAFTING, this.getLevel());
            int i = 0;

            for (int m = remaining.size(); i < m; i++) {
               ItemStack entry = (ItemStack)remaining.get(i);
               if (!entry.isEmpty()) {
                  entry.shrink(transporter.addItem(entry, null, false));
                  if (entry.getCount() > 0) {
                     this.clockUps.add(entry);
                  }
               }
            }

            CRAFTING.clearContent();
            return true;
         } else {
            this.reinsert(toInsertBack, transporter);
            this.flags |= 1 << slot * 2 + 1;
            return false;
         }
      }
   }

   private void reinsert(List<ItemStack> list, IItemTransporter transporter) {
      int i = 0;
      int m = list.size();

      while (i < m) {
         transporter.addItem(list.get(i++), null, false);
      }

      CRAFTING.clearContent();
   }

   private static class CraftTransporter implements IItemTransporter {
      IHasInventory inventory;
      IItemTransporter simple;
      NonNullList<ItemStack> memoryInv;

      public CraftTransporter(IHasInventory inventory) {
         this.inventory = inventory;
         this.simple = TransporterManager.getTransporter(inventory);
         this.memoryInv = NonNullList.withSize(inventory.getSlotCount(), ItemStack.EMPTY);
         int i = 0;

         for (int m = inventory.getSlotCount(); i < m; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
               this.memoryInv.set(i, StackUtil.copyWithSize(stack, 1));
            }
         }
      }

      @Override
      public int addItem(ItemStack stack, Direction dir, boolean simulate) {
         int invSize = this.getInventorySize(dir);
         IntArrayList emptySlots = new IntArrayList(invSize);
         int stackSize = stack.getCount();
         int added = 0;

         for (int i = 0; i < invSize; i++) {
            if (this.inventory.canInsert(i, stack)) {
               ItemStack inv = this.inventory.getStackInSlot(i);
               boolean wasEmpty = false;
               if (inv.isEmpty()) {
                  ItemStack memory = (ItemStack)this.memoryInv.get(i);
                  if (memory.isEmpty()) {
                     emptySlots.add(i);
                     continue;
                  }

                  inv = memory;
                  wasEmpty = true;
               }

               if (StackUtil.isStackEqual(inv, stack)) {
                  if (wasEmpty) {
                     int toAdd = Math.min(this.inventory.getMaxStackSize(i), Math.min(stack.getMaxStackSize(), stackSize - added));
                     if (!simulate) {
                        this.inventory.setStackInSlot(i, StackUtil.copyWithSize(stack, toAdd));
                     }

                     added += toAdd;
                     if (added >= stackSize) {
                        return added;
                     }
                  } else {
                     int room = Math.min(inv.getMaxStackSize() - inv.getCount(), this.inventory.getMaxStackSize(i));
                     if (room <= 0) {
                        continue;
                     }

                     int toAddx = Math.min(room, stackSize - added);
                     if (!simulate) {
                        inv.grow(toAddx);
                        this.inventory.setStackInSlot(i, inv);
                     }

                     added += toAddx;
                  }
               }

               if (added >= stackSize) {
                  return added;
               }
            }
         }

         int ix = 0;

         for (int m = emptySlots.size(); ix < m; ix++) {
            int toAddx = Math.min(this.inventory.getMaxStackSize(ix), Math.min(stack.getMaxStackSize(), stackSize - added));
            if (!simulate) {
               this.inventory.setStackInSlot(emptySlots.getInt(ix), StackUtil.copyWithSize(stack, toAddx));
            }

            added += toAddx;
            if (added >= stackSize) {
               return added;
            }
         }

         return added;
      }

      @Override
      public ItemStack removeItem(IFilter filter, Direction dir, int amount, boolean simulate) {
         return this.simple.removeItem(filter, dir, amount, simulate);
      }

      @Override
      public int getInventorySize(Direction dir) {
         return this.inventory.getSlotCount();
      }

      @Override
      public Object2IntMap<ItemStack> getAllItems(Direction dir, boolean compareNBT) {
         return this.simple.getAllItems(dir, compareNBT);
      }

      @Override
      public IItemTransporter.InvResult getInventory(Direction dir, boolean compareNBT) {
         return this.simple.getInventory(dir, compareNBT);
      }
   }
}
