package ic2.core.block.base.tiles.impls;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.api.items.electric.ElectricItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.readers.IEUStorage;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.IWrenchRemovable;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.block.storage.container.BatteryStationContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseBatteryStationTileEntity
   extends BaseInventoryTileEntity
   implements IMultiEnergySource,
   IEUStorage,
   ITickListener,
   ITileGui,
   IWrenchRemovable {
   @NetworkInfo
   public int energy;
   public int maxEnergy;
   public int tier;
   public int maxOutput;
   @NetworkInfo
   public Averager averager = new Averager();
   IntList dischargeable = new IntArrayList();
   public boolean charge = false;
   public boolean addedToEnet = false;

   public BaseBatteryStationTileEntity(BlockPos pos, BlockState state, int maxEnergy, int maxOutput) {
      super(pos, state, 17);
      this.maxEnergy = maxEnergy;
      this.maxOutput = maxOutput;
      this.tier = EnergyNet.INSTANCE.getTierFromPower(maxOutput);
      this.addGuiFields(new String[]{"energy", "averager"});
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerBlockSides(DirectionList.ALL);
      int[] slots = MathUtils.fromTo(0, 17);
      handler.registerSlotAccess(AccessRule.BOTH, slots);
      handler.registerSlotsForSide(DirectionList.ALL, slots);
      slots = MathUtils.fromTo(0, 16);
      handler.registerInputFilter(ElectricItemFilter.DISCHARGE_EXTERNAL_FILTER, slots);
      handler.registerInputFilter(ElectricItemFilter.CHARGE_FILTER, 16);
      handler.registerNamedSlot(SlotType.BATTERY, slots);
      handler.registerNamedSlot(SlotType.CHARGE, 16);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new BatteryStationContainer(this, player, windowID);
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public boolean canEmitEnergy(IEnergyAcceptor acceptor, Direction side) {
      return true;
   }

   @Override
   public int getSourceTier() {
      return this.tier;
   }

   @Override
   public int getMaxEnergyOutput() {
      return this.maxOutput;
   }

   public int getChargingSlots() {
      return this.dischargeable.size();
   }

   @Override
   public int getProvidedEnergy() {
      return Math.min(this.energy, this.maxOutput);
   }

   @Override
   public void consumeEnergy(int consumed) {
      this.energy -= consumed;
      this.averager.addValue(consumed);
      this.updateGuiField("energy");
   }

   @Override
   public boolean hasMultiplePackets() {
      return true;
   }

   @Override
   public int getPacketCount() {
      return 4;
   }

   @Override
   public int getStoredEU() {
      return this.energy;
   }

   @Override
   public int getMaxEU() {
      return this.maxEnergy;
   }

   @Override
   public int getTier() {
      return this.tier;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating() && !this.addedToEnet) {
         this.addedToEnet = true;
         EnergyNet.INSTANCE.addTile(this);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.addedToEnet) {
         this.addedToEnet = false;
         EnergyNet.INSTANCE.removeTile(this);
      }

      super.onUnloaded(chunk);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.energy = compound.getInt("energy");
      this.dischargeable = IntArrayList.wrap(compound.getIntArray("discharge_slot"));
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putInt(compound, "energy", this.energy, 0);
      NBTUtils.putIntArray(compound, "discharge_slot", this.dischargeable.toIntArray());
   }

   @Override
   public void onTick() {
      if (this.charge && this.energy > 0 && !((ItemStack)this.inventory.get(16)).isEmpty()) {
         int use = ElectricItem.MANAGER.charge((ItemStack)this.inventory.get(16), this.energy, 2147483647, false, false);
         this.energy -= use;
         if (use > 0) {
            this.averager.addValue(use);
            this.updateGuiField("energy");
         } else {
            this.charge = false;
         }
      }

      if (this.averager.shouldRun()) {
         this.averager.finished();
         this.updateGuiField("averager");
      }

      if (this.energy < this.maxEnergy && !this.dischargeable.isEmpty()) {
         int i = 0;

         for (int m = this.dischargeable.size(); i < m; i++) {
            ItemStack stack = (ItemStack)this.inventory.get(this.dischargeable.getInt(i));
            int use = ElectricItem.MANAGER.discharge(stack, this.maxEnergy - this.energy, 2147483647, false, true, false);
            this.energy += use;
            if (use > 0) {
               this.updateGuiField("energy");
               if (this.energy >= this.maxEnergy) {
                  break;
               }
            } else {
               this.dischargeable.removeInt(i--);
               m--;
            }
         }
      }
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.isSimulating()) {
         if (slot < 16) {
            if (stack.isEmpty()) {
               this.dischargeable.rem(slot);
               return;
            }

            this.dischargeable.add(slot);
         } else {
            this.charge = !stack.isEmpty();
         }
      }
   }

   public Entry getMissingEnergy() {
      long energy = 0L;
      long transfer = 0L;

      for (int i = 0; i < 16; i++) {
         ItemStack stack = (ItemStack)this.inventory.get(i);
         if (!stack.isEmpty()) {
            int entry = ElectricItem.MANAGER.getCharge(stack);
            energy += (long)entry;
            transfer += (long)Math.min(entry, ElectricItem.MANAGER.getTransferLimit(stack));
         }
      }

      return new BasicEntry(energy > 2147483647L ? 2147483647 : (int)energy, transfer > 2147483647L ? 2147483647 : (int)transfer);
   }
}
