package ic2.core.block.base.tiles.impls;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.api.items.electric.ElectricItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.IEnergyStorage;
import ic2.api.tiles.readers.IEUStorage;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.features.redstone.IRedstoneProvider;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.EUComparator;
import ic2.core.block.base.misc.comparator.types.special.MinecartComparator;
import ic2.core.block.base.misc.readers.ILoader;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.block.storage.components.UnloaderComponent;
import ic2.core.block.storage.container.UnloaderContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.utils.helpers.NBTUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class BaseElectricUnloaderTileEntity
   extends BaseInventoryTileEntity
   implements ITickListener,
   ILoader,
   ITileGui,
   IRedstoneProvider,
   IEUStorage,
   IMultiEnergySource,
   IWrenchableTile {
   IEnergyStorage lastMinecart = null;
   int lastTransfer = 0;
   @NetworkInfo
   public byte redstoneMode = 0;
   boolean lastRedstone = false;
   boolean addedToEnet = false;
   boolean chargeBattery = false;
   @NetworkInfo
   public int energy = 0;
   public final int maxEnergy;
   public final int maxOutput;
   public final int maxPackets;
   public final int tier;

   public BaseElectricUnloaderTileEntity(BlockPos pos, BlockState state, int maxEnergy, int maxOutput, int maxPackets) {
      super(pos, state, 1);
      this.maxEnergy = maxEnergy;
      this.maxOutput = maxOutput;
      this.maxPackets = maxPackets;
      this.tier = EnergyNet.INSTANCE.getTierFromPower(maxOutput);
      this.addGuiFields(new String[]{"redstoneMode", "energy"});
      this.addComparator(new EUComparator("eu_storage", ComparatorNames.EU_STORAGE, this));
      this.addComparator(new MinecartComparator("minecarts", ComparatorNames.HAS_MINECART, this, MinecartComparator.InfoType.HAS_MINECART));
      this.addComparator(new MinecartComparator("has_transfer", ComparatorNames.HAS_TRANSFERRED, this, MinecartComparator.InfoType.HAS_TRANSFER));
      this.addComparator(new MinecartComparator("minecart_energy", ComparatorNames.MINECART_ENERGY, this, MinecartComparator.InfoType.MINECART_ENERGY));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.registerSlotsForSide(DirectionList.ALL, 0);
      handler.registerInputFilter(ElectricItemFilter.CHARGE_FILTER, 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_CHARGE_FILTER, 0);
      handler.registerNamedSlot(SlotType.CHARGE, 0);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.energy = compound.getInt("energy");
      this.chargeBattery = compound.getBoolean("charge_slot");
      this.redstoneMode = compound.getByte("mode");
      this.lastRedstone = compound.getBoolean("state");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putInt(compound, "energy", this.energy, 0);
      NBTUtils.putBoolean(compound, "charge_slot", this.chargeBattery, false);
      NBTUtils.putByte(compound, "mode", this.redstoneMode, 0);
      NBTUtils.putBoolean(compound, "state", this.lastRedstone, false);
   }

   @Override
   public int getMaxEU() {
      return this.maxEnergy;
   }

   @Override
   public int getStoredEU() {
      return this.energy;
   }

   @Override
   public int getTier() {
      return this.tier;
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
   public int getSourceTier() {
      return this.getTier();
   }

   @Override
   public int getMaxEnergyOutput() {
      return this.maxOutput;
   }

   @Override
   public int getProvidedEnergy() {
      return Math.min(this.maxOutput, this.energy);
   }

   @Override
   public void consumeEnergy(int consumed) {
      this.energy -= consumed;
      this.updateGuiField("energy");
   }

   @Override
   public boolean canEmitEnergy(IEnergyAcceptor acceptor, Direction side) {
      return true;
   }

   @Override
   public boolean hasMultiplePackets() {
      return this.maxPackets > 1;
   }

   @Override
   public int getPacketCount() {
      return this.maxPackets;
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.isSimulating() && slot == 0) {
         this.chargeBattery = !stack.isEmpty();
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
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new UnloaderContainer(this, player, windowID);
   }

   @Override
   public void onTick() {
      if (this.chargeBattery && this.energy > 0 && !((ItemStack)this.inventory.get(0)).isEmpty()) {
         int removed = ElectricItem.MANAGER.charge((ItemStack)this.inventory.get(0), this.energy, this.getTier(), false, false);
         this.energy -= removed;
         if (removed > 0) {
            this.updateGuiField("energy");
         } else {
            this.chargeBattery = false;
         }
      }

      if (this.lastMinecart != null || !this.invClock(20)) {
         this.lastMinecart = null;
         this.lastTransfer = 0;
         IEnergyStorage storage = this.findMinecart();
         if (storage != null) {
            this.lastMinecart = storage;
            this.lastTransfer = this.lastTransfer
               + this.lastMinecart.drawEnergy(this.addEnergy(Math.min(this.lastMinecart.getStoredEU(), this.maxOutput * 10)));
         }

         boolean nextRedstone = this.getRedstoneState();
         if (nextRedstone != this.lastRedstone) {
            this.lastRedstone = nextRedstone;
            this.notifyChanges(true, DirectionList.ALL);
         }

         this.handleComparators();
      }
   }

   public IEnergyStorage findMinecart() {
      List<AbstractMinecart> list = this.getLevel().getEntitiesOfClass(AbstractMinecart.class, new AABB(this.getBlockPos().relative(this.getFacing())));
      if (list.isEmpty()) {
         return null;
      } else {
         int i = 0;

         for (int m = list.size(); i < m; i++) {
            Object var5 = list.get(i);
            if (var5 instanceof IEnergyStorage) {
               return (IEnergyStorage)var5;
            }
         }

         return null;
      }
   }

   protected int addEnergy(int toAdd) {
      int added = Math.min(this.maxEnergy - this.energy, toAdd);
      this.energy += added;
      return added;
   }

   public boolean getRedstoneState() {
      switch (this.redstoneMode) {
         case 1:
            return this.hasMinecart() && this.lastMinecart.getStoredEU() <= 0;
         case 2:
            return this.hasMinecart() && this.getTransferred() <= 0;
         case 3:
            return this.hasMinecart() && this.lastMinecart.getChargeLevel() >= 0.5;
         case 4:
            return this.hasMinecart() && this.lastMinecart.getChargeLevel() <= 0.5;
         case 5:
            return this.energy >= this.maxEnergy;
         default:
            return false;
      }
   }

   @Override
   public boolean hasMinecart() {
      return this.lastMinecart != null;
   }

   @Override
   public int getTransferred() {
      return this.lastTransfer;
   }

   @Override
   public IEnergyStorage getMinecart() {
      return this.lastMinecart;
   }

   @Override
   public int getCommonSignalStrength(Direction side) {
      return side == this.getFacing().getOpposite() && this.lastRedstone ? 15 : 0;
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         this.redstoneMode = (byte)((this.redstoneMode + 1) % 6);
         boolean newRedstone = this.getRedstoneState();
         if (this.lastRedstone != newRedstone) {
            this.lastRedstone = newRedstone;
            this.notifyChanges(true, DirectionList.ALL);
         }

         this.updateGuiField("redstoneMode");
         entity.displayClientMessage(UnloaderComponent.NAMES[this.redstoneMode], false);
      }
   }
}
