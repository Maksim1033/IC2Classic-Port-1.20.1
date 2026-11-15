package ic2.core.block.base.tiles.impls;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.IEnergyStorage;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.redstone.IRedstoneProvider;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.special.MinecartComparator;
import ic2.core.block.base.misc.readers.ILoader;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.storage.components.LoaderComponent;
import ic2.core.block.storage.container.LoaderContainer;
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
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class BaseElectricLoaderTileEntity extends BaseElectricTileEntity implements ITickListener, ILoader, ITileGui, IRedstoneProvider {
   IEnergyStorage lastMinecart = null;
   int lastTransfer = 0;
   @NetworkInfo
   public byte redstoneMode = 0;
   boolean lastRedstone = false;

   public BaseElectricLoaderTileEntity(BlockPos pos, BlockState state, int maxInput, int maxEnergy) {
      super(pos, state, 1, maxInput, maxEnergy);
      this.setFuelSlot(0);
      this.addGuiFields(new String[]{"redstoneMode"});
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
      handler.registerInputFilter(
         new ArrayOrFilter(
            ElectricItemFilter.DISCHARGE_EXTERNAL_FILTER,
            new SimpleFilter(Items.REDSTONE),
            new SimpleFilter(Items.REDSTONE_BLOCK),
            new SimpleFilter(IC2Items.SU_BATTERY)
         ),
         1
      );
      handler.registerNamedSlot(SlotType.BATTERY, 0);
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.redstoneMode = compound.getByte("mode");
      this.lastRedstone = compound.getBoolean("state");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putByte(compound, "mode", this.redstoneMode, 0);
      NBTUtils.putBoolean(compound, "state", this.lastRedstone, false);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new LoaderContainer(this, player, windowID);
   }

   @Override
   public void onTick() {
      this.handleChargeSlot((int)((double)this.maxEnergy * 0.9));
      if (this.lastMinecart != null || !this.invClock(20)) {
         this.lastMinecart = null;
         this.lastTransfer = 0;
         IEnergyStorage storage = this.findMinecart();
         if (storage != null) {
            this.lastMinecart = storage;
            this.lastTransfer = this.lastTransfer + this.useEnergy(this.lastMinecart.addEnergy(Math.min(this.energy, this.getMaxInput() * 10)));
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

   public boolean getRedstoneState() {
      switch (this.redstoneMode) {
         case 1:
            return this.hasMinecart() && this.lastMinecart.getStoredEU() >= this.lastMinecart.getMaxEU();
         case 2:
            return this.hasMinecart() && this.getTransferred() <= 0;
         case 3:
            return this.hasMinecart() && this.lastMinecart.getChargeLevel() >= 0.5;
         case 4:
            return this.hasMinecart() && this.lastMinecart.getChargeLevel() <= 0.5;
         case 5:
            return this.energy <= 0;
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
         entity.displayClientMessage(LoaderComponent.NAMES[this.redstoneMode], false);
      }
   }
}
