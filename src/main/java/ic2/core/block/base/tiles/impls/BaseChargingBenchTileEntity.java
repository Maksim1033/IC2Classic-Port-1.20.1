package ic2.core.block.base.tiles.impls;

import ic2.api.items.electric.ElectricItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.storage.container.ChargingBenchContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.utils.collection.Averager;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.AbstractInt2IntMap.BasicEntry;
import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseChargingBenchTileEntity extends BaseElectricTileEntity implements ITileGui, ITickListener {
   @NetworkInfo
   public boolean sortedMode = false;
   @NetworkInfo
   public Averager averager = new Averager();
   public int state = 0;
   IntList chargeableSlot = new IntArrayList();
   int slots;

   public BaseChargingBenchTileEntity(BlockPos pos, BlockState state, int maxInput, int maxEnergy) {
      super(pos, state, 17, maxInput, maxEnergy);
      this.setFuelSlot(16);
      this.addNetworkFields(new String[]{"sortedMode"});
      this.addGuiFields(new String[]{"averager"});
      this.redstoneIsFull = false;
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerBlockSides(DirectionList.ALL);
      int[] slots = MathUtils.fromTo(0, 17);
      handler.registerSlotAccess(AccessRule.BOTH, slots);
      handler.registerSlotsForSide(DirectionList.ALL, slots);
      handler.registerNamedSlot(SlotType.CHARGE, MathUtils.fromTo(0, 16));

      for (int i = 0; i < 16; i++) {
         handler.registerInputFilter(this.createFilter(i), i);
      }

      handler.registerInputFilter(ElectricItemFilter.DISCHARGE_EXTERNAL_FILTER, 16);
      handler.registerNamedSlot(SlotType.BATTERY, 16);
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   private int createState() {
      return this.slots <= 0 ? 0 : (this.energy <= 0 && !this.chargeableSlot.isEmpty() ? 1 : (this.energy > 0 && !this.chargeableSlot.isEmpty() ? 2 : 3));
   }

   public int getChargingSlots() {
      return this.chargeableSlot.size();
   }

   public int getSlots() {
      return this.slots;
   }

   @Override
   public int getMaxStackSize(int slot) {
      return 1;
   }

   public Entry getMissingEnergy() {
      long energy = 0L;
      long transfer = 0L;

      for (int i = 0; i < 16; i++) {
         ItemStack stack = (ItemStack)this.inventory.get(i);
         if (!stack.isEmpty()) {
            int entry = ElectricItem.MANAGER.charge(stack, 2147483647, 2147483647, true, true);
            energy += (long)entry;
            transfer += (long)Math.min(entry, ElectricItem.MANAGER.getTransferLimit(stack));
         }
      }

      return new BasicEntry(energy > 2147483647L ? 2147483647 : (int)energy, transfer > 2147483647L ? 2147483647 : (int)transfer);
   }

   public int getState() {
      return this.state;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ChargingBenchContainer(this, player, windowID);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putIntArray(compound, "chargeable", this.chargeableSlot.toIntArray());
      NBTUtils.putByte(compound, "states", (byte)this.state, 0);
      NBTUtils.putInt(compound, "present", this.slots, 0);
      NBTUtils.putBoolean(compound, "sorted", this.sortedMode, false);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.chargeableSlot.addAll(IntArrayList.wrap(compound.getIntArray("chargeable")));
      this.state = compound.getInt("states");
      this.slots = compound.getInt("present");
      this.sortedMode = compound.getBoolean("sorted");
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.isSimulating() && slot < 16) {
         if (stack.isEmpty()) {
            this.chargeableSlot.rem(slot);
            this.slots &= ~(1 << slot);
            return;
         }

         if (!this.chargeableSlot.contains(slot)) {
            this.chargeableSlot.add(slot);
         }

         this.slots |= 1 << slot;
      }
   }

   @Override
   public int acceptEnergy(Direction side, int amount, int voltage) {
      return amount <= this.maxEnergy && amount > 0 ? amount - this.addEnergy(amount) : 0;
   }

   @Override
   public int addEnergy(int amount) {
      int added = Math.min(amount, this.maxEnergy - this.energy);
      if (added > 0) {
         this.energy += added;
         this.averager.addValue(added);
         this.updateGuiField("energy");
      }

      return added;
   }

   @Override
   public void onTick() {
      this.handleChargeSlot((int)((float)this.maxEnergy * 0.85F));
      if (this.averager.shouldRun()) {
         this.averager.finished();
      }

      this.updateGuiField("averager");
      if (this.clock(10)) {
         int newState = this.createState();
         if (newState != this.state) {
            this.state = newState;
            this.onStateChanged();
         }
      }

      if (!this.chargeableSlot.isEmpty() && this.energy > 0) {
         int i = 0;

         for (int m = this.chargeableSlot.size(); i < m && this.energy > 0; i++) {
            ItemStack stack = (ItemStack)this.inventory.get(this.chargeableSlot.getInt(i));
            int use = ElectricItem.MANAGER.charge(stack, this.energy, 2147483647, false, false);
            this.useEnergy(use);
            if (use <= 0) {
               this.chargeableSlot.removeInt(i--);
               m--;
            }
         }
      }
   }

   @Override
   public void onGuiFieldChanged(Set<String> fields, Player player) {
      super.onGuiFieldChanged(fields, player);
      if (fields.contains("sortedMode")) {
         this.getContainer(player, ChargingBenchContainer.class).applySlots(null);
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         this.sortedMode = !this.sortedMode;
         this.updateTileField("sortedMode");
         this.updateGuiField("sortedMode");
         this.getContainer(entity, ChargingBenchContainer.class).applySlots(entity);
      } else if (this.sortedMode) {
         switch (key) {
            case 1:
               this.swap(0, entity.getInventory());
               break;
            case 2:
               this.swap(2, entity.getInventory());
               break;
            case 3:
               this.swap(1, entity.getInventory());
         }
      }
   }

   public void swap(int rule, Inventory player) {
      for (int i = 0; i < 4; i++) {
         this.swap(i * 4, 3 - i, player.armor, rule);
      }

      this.swap(5, player.selected, player.items, rule);
      this.swap(9, 0, player.offhand, rule);
   }

   protected void swap(int invSlot, int playerSlot, NonNullList<ItemStack> playerInv, int flag) {
      ItemStack stored = (ItemStack)this.inventory.get(invSlot);
      if ((stored.isEmpty() || flag != 0) && (!stored.isEmpty() || flag != 1)) {
         ItemStack player = (ItemStack)playerInv.get(playerSlot);
         if ((player.isEmpty() || flag != 1) && (!player.isEmpty() || flag != 0) && (player.isEmpty() || ElectricItemFilter.ELECTRIC_ITEM.matches(player))) {
            this.setStackInSlot(invSlot, player);
            playerInv.set(playerSlot, stored);
         }
      }
   }

   public IFilter createFilter(int slot) {
      return T -> this.sortedMode && slot % 4 == 0
            ? ElectricItemFilter.ELECTRIC_ITEM.matches(T) && T.canEquip(ChargingBenchContainer.VALID_EQUIPMENT_SLOTS[slot / 4], null)
            : ElectricItemFilter.ELECTRIC_ITEM.matches(T);
   }
}
