package ic2.core.block.base.tiles;

import ic2.api.core.IC2Classic;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.items.electric.ElectricItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.INotifiableMachine;
import ic2.api.tiles.readers.IEUStorage;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.cache.CapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.EUComparator;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseElectricTileEntity extends BaseInventoryTileEntity implements IEnergySink, IEUStorage, IWrenchableTile {
   @NetworkInfo
   public int energy;
   @NetworkInfo
   public int maxEnergy;
   @NetworkInfo
   public int maxInput;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int tier;
   public int baseTier;
   int fuelSlot = -1;
   protected boolean redstoneIsFull = true;
   protected int redstoneLeft = 0;
   public boolean charge_slot = false;
   CapabilityCache<INotifiableMachine> listeners = new CapabilityCache<>(this, DirectionList.ALL, IC2Classic.NOTIFY_CAPABILITY);
   public boolean addedToEnet = false;

   public BaseElectricTileEntity(BlockPos pos, BlockState state, int size, int maxInput, int maxEnergy) {
      super(pos, state, size);
      this.maxInput = maxInput;
      this.maxEnergy = maxEnergy;
      this.tier = EnergyNet.INSTANCE.getTierFromPower(maxInput);
      this.baseTier = this.tier;
      this.addGuiFields(new String[]{"energy", "maxEnergy", "tier", "maxInput"});
      if (this.supportsNotify()) {
         this.addCaches(new ICache[]{this.listeners});
      }

      this.addComparator(new EUComparator("eu_storage", ComparatorNames.EU_STORAGE, this));
   }

   public abstract boolean supportsNotify();

   public void setFuelSlot(int fuelSlot) {
      this.fuelSlot = fuelSlot;
   }

   @Override
   public boolean canAcceptEnergy(IEnergyEmitter emitter, Direction side) {
      return true;
   }

   @Override
   public int getSinkTier() {
      return this.tier;
   }

   @Override
   public int getRequestedEnergy() {
      return this.maxEnergy - this.energy;
   }

   @Override
   public int acceptEnergy(Direction side, int amount, int voltage) {
      if (amount <= this.maxEnergy && amount > 0) {
         int added = Math.min(amount, this.maxEnergy - this.energy);
         if (added > 0) {
            this.energy += added;
            this.updateGuiField("energy");
            this.handleNonTickComparators();
            if (this.needsUpdateTick() && this instanceof ITickListener && !this.isTicking()) {
               this.addToTick();
            }
         }

         return amount - added;
      } else {
         return 0;
      }
   }

   @Override
   public int getMaxEU() {
      return this.maxEnergy;
   }

   @Override
   public int getStoredEU() {
      return this.energy;
   }

   public void setMaxEnergy(int max) {
      this.maxEnergy = max;
      this.updateGuiField("maxEnergy");
   }

   public void setMaxInput(int inputEnergy) {
      this.maxInput = inputEnergy;
      this.tier = EnergyNet.INSTANCE.getTierFromPower(inputEnergy);
      this.updateGuiFields(new String[]{"tier", "maxInput"});
   }

   public void setTier(int newTier) {
      this.tier = newTier;
      this.maxInput = EnergyNet.INSTANCE.getPowerFromTier(newTier);
      this.updateGuiFields(new String[]{"tier", "maxInput"});
   }

   @Override
   public int getTier() {
      return this.tier;
   }

   public int getMaxInput() {
      return this.maxInput;
   }

   public boolean hasEnergy(int amount) {
      return this.energy >= amount;
   }

   public int useEnergy(int used) {
      int removed = Math.min(used, this.energy);
      if (removed > 0) {
         this.energy -= removed;
         this.redstoneLeft = Math.max(0, this.redstoneLeft - removed);
         this.updateGuiField("energy");
      }

      return removed;
   }

   public int addEnergy(int amount) {
      int added = Math.min(amount, this.maxEnergy - this.energy);
      if (added > 0) {
         this.energy += added;
         this.updateGuiField("energy");
      }

      return added;
   }

   public boolean handleChargeSlot(int needed) {
      if (this.fuelSlot != -1 && this.energy < needed && this.provideEnergy()) {
         this.updateGuiField("energy");
         return true;
      } else {
         return false;
      }
   }

   public boolean provideEnergy() {
      if (!this.charge_slot) {
         return false;
      } else {
         int needed = this.maxEnergy - this.energy;
         if (needed <= 0) {
            return false;
         } else {
            ItemStack fuel = (ItemStack)this.inventory.get(this.fuelSlot);
            if (fuel.isEmpty()) {
               return false;
            } else {
               Item item = fuel.getItem();
               if (item == Items.REDSTONE) {
                  if (needed < (this.redstoneIsFull ? this.maxEnergy / 2 : Math.min(500, this.maxEnergy))) {
                     return false;
                  } else {
                     int added = this.addEnergy(this.redstoneIsFull ? this.maxEnergy : 500);
                     if (this.redstoneIsFull) {
                        this.redstoneLeft += added;
                     }

                     fuel.shrink(1);
                     this.charge_slot = !fuel.isEmpty();
                     this.handleNonTickComparators();
                     return true;
                  }
               } else if (!this.redstoneIsFull && item == Items.REDSTONE_BLOCK) {
                  if (needed < Math.min(4500, this.maxEnergy)) {
                     return false;
                  } else {
                     this.addEnergy(4500);
                     fuel.shrink(1);
                     this.charge_slot = !fuel.isEmpty();
                     this.handleNonTickComparators();
                     return true;
                  }
               } else if (item == IC2Items.SU_BATTERY) {
                  if (needed < Math.min(1000, this.maxEnergy)) {
                     return false;
                  } else {
                     this.addEnergy(1000);
                     fuel.shrink(1);
                     this.charge_slot = !fuel.isEmpty();
                     this.handleNonTickComparators();
                     return true;
                  }
               } else {
                  int transfer = ElectricItem.MANAGER.discharge(fuel, this.maxEnergy - this.energy, this.getTier(), false, true, false);
                  if (transfer > 0) {
                     this.addEnergy(transfer);
                     return true;
                  } else {
                     this.charge_slot = false;
                     return false;
                  }
               }
            }
         }
      }
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.isSimulating() && slot == this.fuelSlot) {
         this.charge_slot = !stack.isEmpty();
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 32766 && IC2.CONFIG.allowMachineDrain.get()) {
         AbstractContainerMenu menu = this.getContainer(entity, AbstractContainerMenu.class);
         if (menu == null || menu.getCarried().isEmpty()) {
            return;
         }

         int toDrain = this.energy - this.redstoneLeft;
         if (toDrain <= 0) {
            return;
         }

         int charged = ElectricItem.MANAGER.charge(menu.getCarried(), toDrain, 2147483647, true, true);
         if (charged <= 0) {
            return;
         }

         int cache = this.redstoneLeft;
         int drained = this.useEnergy(toDrain);
         this.redstoneLeft = cache;
         ElectricItem.MANAGER.charge(menu.getCarried(), drained, 2147483647, true, false);
      }
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
   public void setFacing(Direction facing) {
      super.setFacing(facing);
      EnergyNet.INSTANCE.updateTile(this);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.energy = compound.getInt("energy");
      this.charge_slot = compound.getBoolean("charge_slot");
      this.redstoneLeft = compound.getInt("red_block_left");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putInt(compound, "energy", this.energy, 0);
      NBTUtils.putBoolean(compound, "charge_slot", this.charge_slot, false);
      NBTUtils.putInt(compound, "red_block_left", this.redstoneLeft, 0);
   }

   @Override
   public boolean canSetFacing(Direction dir) {
      return dir != this.getFacing() && dir.getAxis().isHorizontal();
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.85 - 0.05 * (double)this.baseTier;
   }

   public void notifyListeners() {
      for (Direction dir : this.listeners) {
         INotifiableMachine machine = this.listeners.getHandler(dir);
         if (machine != null) {
            machine.onNotify();
         }
      }
   }
}
