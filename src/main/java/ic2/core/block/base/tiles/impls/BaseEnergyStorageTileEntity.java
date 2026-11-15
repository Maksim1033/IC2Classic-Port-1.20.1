package ic2.core.block.base.tiles.impls;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.items.electric.ElectricItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.IElectrolyzerProvider;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.features.redstone.IRedstoneListener;
import ic2.core.block.base.features.redstone.IRedstoneProvider;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.EUComparator;
import ic2.core.block.base.misc.comparator.types.special.HasPowerLevelComparator;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.block.storage.components.EnergyStorageComponent;
import ic2.core.block.storage.container.ContainerEnergyStorage;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.ArrayOrFilter;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseEnergyStorageTileEntity
   extends BaseInventoryTileEntity
   implements IEnergySink,
   IEnergySource,
   ITickListener,
   IRedstoneProvider,
   IRedstoneListener,
   ITileGui,
   IElectrolyzerProvider,
   IWrenchableTile {
   public int tier;
   public int output;
   public int maxEnergy;
   @NetworkInfo
   public int energy = 0;
   @NetworkInfo
   public byte redstoneMode = 0;
   public boolean lastRedstone = false;
   @NetworkInfo
   public byte state = 0;
   public int activeSlots = 0;
   protected boolean addedToEnergyNet = false;

   public BaseEnergyStorageTileEntity(BlockPos pos, BlockState state, int tier, int output, int maxEnergy) {
      super(pos, state, 2);
      this.tier = tier;
      this.output = output;
      this.maxEnergy = maxEnergy;
      this.addNetworkFields(new String[]{"state"});
      this.addGuiFields(new String[]{"energy", "redstoneMode"});
      this.addComparator(new EUComparator("eu_storage", ComparatorNames.EU_STORAGE, this));

      for (int i = 1; i < 10; i++) {
         this.addComparator(
            new HasPowerLevelComparator("storage_" + i, this.translate("comparator.ic2.eu_buffer.percent", new Object[]{i * 10}), this, (float)i / 10.0F)
         );
      }
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.registerSlotAccess(AccessRule.BOTH, 1);
      handler.registerSlotsForSide(DirectionList.UP, 0);
      handler.registerSlotsForSide(DirectionList.DOWN, 1);
      handler.registerInputFilter(ElectricItemFilter.CHARGE_FILTER, 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_CHARGE_FILTER, 0);
      handler.registerInputFilter(
         new ArrayOrFilter(
            ElectricItemFilter.DISCHARGE_EXTERNAL_FILTER,
            new SimpleFilter(Items.REDSTONE),
            new SimpleFilter(Items.REDSTONE_BLOCK),
            new SimpleFilter(IC2Items.SU_BATTERY)
         ),
         1
      );
      handler.registerOutputFilter(ElectricItemFilter.NOT_DISCHARGE_FILTER, 1);
      handler.registerNamedSlot(SlotType.CHARGE, 0);
      handler.registerNamedSlot(SlotType.BATTERY, 1);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putInt(compound, "energy", this.energy, 0);
      NBTUtils.putByte(compound, "redstoneMode", this.redstoneMode, 0);
      NBTUtils.putByte(compound, "state", this.state, 0);
      NBTUtils.putByte(compound, "active_slots", (byte)this.activeSlots, 0);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.energy = compound.getInt("energy");
      this.redstoneMode = compound.getByte("redstoneMode");
      this.state = compound.getByte("state");
      this.activeSlots = compound.getByte("active_slots");
   }

   public abstract int getGuiOffset();

   public abstract ResourceLocation getTexture();

   @Override
   public boolean canSetFacing(Direction dir) {
      return dir != this.getFacing();
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
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
   public int addEnergy(int power) {
      int added = Math.min(this.maxEnergy - this.energy, power);
      if (added > 0) {
         this.energy += added;
         this.updateGuiField("energy");
      }

      return added;
   }

   @Override
   public int drawEnergy(int power) {
      int removed = Math.min(power, this.energy);
      if (removed > 0) {
         this.energy -= removed;
         this.updateGuiField("energy");
      }

      return removed;
   }

   @Override
   public int getProcessRate() {
      return this.output / 16;
   }

   @Override
   public int getTier() {
      return this.tier;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating() && !this.addedToEnergyNet) {
         this.addedToEnergyNet = true;
         EnergyNet.INSTANCE.addTile(this);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.addedToEnergyNet) {
         this.addedToEnergyNet = false;
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
   public boolean canAcceptEnergy(IEnergyEmitter emitter, Direction side) {
      return this.getFacing() != side;
   }

   @Override
   public boolean canEmitEnergy(IEnergyAcceptor acceptor, Direction side) {
      return this.getFacing() == side;
   }

   @Override
   public int getSourceTier() {
      return this.tier;
   }

   @Override
   public int getMaxEnergyOutput() {
      return this.output;
   }

   @Override
   public int getProvidedEnergy() {
      return this.energy >= this.output && (!this.isRedstonePowered() || this.redstoneMode != 6 && (this.redstoneMode != 7 || this.energy >= this.maxEnergy))
         ? this.output
         : 0;
   }

   @Override
   public void consumeEnergy(int consumed) {
      this.drawEnergy(consumed);
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
      return amount <= this.maxEnergy && amount > 0 ? amount - this.addEnergy(amount) : 0;
   }

   @Override
   public void onTick() {
      this.handleRedstone();
      if (this.energy > 0 && (this.activeSlots & 1) != 0 && !((ItemStack)this.inventory.get(0)).isEmpty()) {
         int removed = this.drawEnergy(ElectricItem.MANAGER.charge((ItemStack)this.inventory.get(0), this.energy, this.tier, false, false));
         if (removed > 0) {
            this.updateGuiField("energy");
         } else {
            this.activeSlots &= -2;
         }
      }

      if (this.maxEnergy > this.energy && (this.activeSlots & 2) != 0 && !((ItemStack)this.inventory.get(1)).isEmpty()) {
         ItemStack fuel = (ItemStack)this.inventory.get(1);
         Item item = fuel.getItem();
         if (item == Items.REDSTONE) {
            this.addEnergy(500);
            fuel.shrink(1);
         } else if (item == Items.REDSTONE_BLOCK) {
            this.addEnergy(5000);
            fuel.shrink(1);
         } else if (item == IC2Items.SU_BATTERY) {
            this.addEnergy(1000);
            fuel.shrink(1);
         } else {
            int added = this.addEnergy(ElectricItem.MANAGER.discharge(fuel, this.maxEnergy - this.energy, this.tier, false, true, false));
            if (added > 0) {
               this.updateGuiField("energy");
            } else {
               this.activeSlots &= -3;
            }
         }
      }

      byte newState = this.getUpdatedState();
      if (newState != this.state) {
         this.state = newState;
         this.onStateChanged();
      }

      boolean newRedstone = this.isEmittingRedstone();
      if (this.lastRedstone != newRedstone) {
         this.lastRedstone = newRedstone;
         this.notifyChanges(true, DirectionList.ALL);
      }

      this.handleComparators();
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.isSimulating()) {
         if (stack.isEmpty()) {
            this.activeSlots &= ~(1 << slot);
            return;
         }

         this.activeSlots |= 1 << slot;
      }
   }

   public byte getUpdatedState() {
      float charge = (float)this.energy / (float)this.maxEnergy;
      if (charge <= 0.01F) {
         return 0;
      } else if (charge >= 0.99F) {
         return 3;
      } else {
         return (byte)(charge < 0.5F ? 1 : 2);
      }
   }

   public boolean isEmittingRedstone() {
      switch (this.redstoneMode) {
         case 1:
            return this.energy >= this.maxEnergy;
         case 2:
            return this.energy > this.output && this.energy < this.maxEnergy;
         case 3:
            return this.energy >= this.maxEnergy / 2;
         case 4:
            return this.energy <= this.maxEnergy / 2;
         case 5:
            return this.energy < this.output;
         default:
            return false;
      }
   }

   @Override
   public boolean canConnectToRedstone(Direction dir) {
      return true;
   }

   @Override
   public int getCommonSignalStrength(Direction side) {
      return this.isEmittingRedstone() ? 15 : 0;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ContainerEnergyStorage(this, player, windowID);
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         this.redstoneMode = (byte)(++this.redstoneMode % 8);
         boolean newRedstone = this.isEmittingRedstone();
         if (this.lastRedstone != newRedstone) {
            this.lastRedstone = newRedstone;
            this.notifyChanges(true, DirectionList.ALL);
         }

         this.updateGuiField("redstoneMode");
         entity.displayClientMessage(EnergyStorageComponent.NAMES[this.redstoneMode], true);
      }
   }
}
