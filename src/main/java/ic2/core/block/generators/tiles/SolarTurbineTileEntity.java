package ic2.core.block.generators.tiles;

import ic2.api.energy.tile.IEnergySource;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.readers.IPumpTile;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.special.HeatComparator;
import ic2.core.block.base.misc.readers.IHeatProvider;
import ic2.core.block.base.tiles.impls.BaseGeneratorTileEntity;
import ic2.core.block.generators.containers.SolarTurbineContainer;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.FluidFilter;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.FluidHelper;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class SolarTurbineTileEntity extends BaseGeneratorTileEntity implements IFluidHandler, IPumpTile, IHeatProvider, IClickable {
   public static final int MAX_WATER = 40000;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_16)
   public int heat = 0;
   float consumed = 0.0F;
   float subStorage = 0.0F;

   public SolarTurbineTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3);
      this.maxStorage = 100;
      this.production = (int)(32.0 * IC2.CONFIG.solarTurbine.get());
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, this);
      this.addGuiFields(new String[]{"heat"});
      this.addComparator(new HeatComparator("heat", ComparatorNames.HEAT, this));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.UP.invert());
      handler.registerBlockAccess(DirectionList.UP.invert(), AccessRule.BOTH);
      handler.registerSlotsForSide(DirectionList.UP.invert(), 0);
      handler.registerSlotsForSide(DirectionList.UP.invert(), 0, 2);
      handler.registerSlotsForSide(DirectionList.HORIZONTAL, 1);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.registerSlotAccess(AccessRule.IMPORT, 1);
      handler.registerSlotAccess(AccessRule.EXPORT, 2);
      handler.setSlotAccess(0, Direction.UP, AccessRule.DISABLED);
      handler.setSlotAccess(1, Direction.UP, AccessRule.DISABLED);
      handler.setSlotAccess(2, Direction.UP, AccessRule.DISABLED);
      handler.registerInputFilter(ElectricItemFilter.CHARGE_FILTER, 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_CHARGE_FILTER, 0);
      handler.registerInputFilter(new FluidFilter(Fluids.WATER), 1);
      handler.registerNamedSlot(SlotType.CHARGE, 0);
      handler.registerNamedSlot(SlotType.FUEL, 1);
      handler.registerNamedSlot(SlotType.OUTPUT, 2);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putShort(compound, "heat", this.heat, 24000);
      NBTUtils.putFloat(compound, "consumed", this.consumed, 0.0F);
      NBTUtils.putFloat(compound, "sub_storage", this.subStorage, 0.0F);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.heat = NBTUtils.getInt(compound, "heat", 24000);
      this.subStorage = compound.getFloat("sub_storage");
      this.consumed = compound.getFloat("consumed");
   }

   @Override
   public IEnergySource.SourceType getSourceType() {
      return IEnergySource.SourceType.PASSIVE_PRODUCING;
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      return FluidHelper.drainContainers(player.getItemInHand(hand), player, this);
   }

   @Override
   public int getPumpProgress() {
      return this.fuel;
   }

   @Override
   public int getPumpMaxProgress() {
      return 40000;
   }

   @Override
   public int getMaxFuel() {
      return 40000;
   }

   @Override
   public int getHeat() {
      return this.heat;
   }

   @Override
   public int getMaxHeat() {
      return 24000;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new SolarTurbineContainer(this, player, windowID);
   }

   @Override
   public boolean needsFuel() {
      return this.fuel < 38000;
   }

   @Override
   public boolean gainFuel() {
      return FluidHelper.drainContainers(this, 1, 2, this);
   }

   @Override
   public boolean gainEnergy() {
      if (this.clock(128)) {
         this.setActive(SolarPanelTileEntity.isSunVisible(this.getLevel(), this.getBlockPos().above()));
      }

      if (this.isActive()) {
         if (this.heat < 24000) {
            this.heat += 2;
            if (this.heat > 24000) {
               this.heat = 24000;
            }

            this.updateGuiField("heat");
         }
      } else if (this.heat > 0) {
         this.heat--;
         this.updateGuiField("heat");
      }

      if (this.fuel > 0 && this.heat > 0) {
         float per = (float)this.heat / 24000.0F;
         this.consumed += 4.0F * per;
         int drain = (int)this.consumed;
         this.consumed -= (float)drain;
         this.updateGuiField("fuel");
         if (this.storage >= this.maxStorage) {
            this.fuel -= drain;
            if (this.fuel < 0) {
               this.fuel = 0;
            }

            return this.isActive();
         }

         float gen = (float)this.fuel * per / 1250.0F * (float)IC2.CONFIG.solarTurbine.get();
         this.subStorage += gen;
         int adding = (int)this.subStorage;
         if (adding > 0) {
            this.storage += adding;
            this.subStorage -= (float)adding;
         }

         if ((int)gen != this.production) {
            this.production = (int)gen;
            this.updateGuiField("production");
         }

         this.fuel -= drain;
         if (this.fuel < 0) {
            this.fuel = 0;
         }
      }

      return this.isActive();
   }

   @Override
   public float getEUProduction() {
      return this.fuel > 0 && this.heat > 0 ? (float)this.heat / 24000.0F * (float)this.fuel / 1250.0F : 0.0F;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating()) {
         this.setActive(SolarPanelTileEntity.isSunVisible(this.getLevel(), this.getBlockPos().above()));
      }
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.GENERATOR_SOLAR_TURBINE;
   }

   public int getTanks() {
      return 1;
   }

   public FluidStack getFluidInTank(int tank) {
      return new FluidStack(Fluids.WATER, this.fuel);
   }

   public int getTankCapacity(int tank) {
      return 40000;
   }

   public boolean isFluidValid(int tank, FluidStack stack) {
      return stack.getFluid() == Fluids.WATER;
   }

   public int fill(FluidStack resource, FluidAction action) {
      if (resource.getFluid() != Fluids.WATER) {
         return 0;
      } else {
         int toAdd = Math.min(resource.getAmount(), (40000 - this.fuel) / 2);
         if (action.execute()) {
            this.fuel += toAdd * 2;
            this.updateGuiField("fuel");
         }

         return toAdd;
      }
   }

   public FluidStack drain(FluidStack resource, FluidAction action) {
      return FluidStack.EMPTY;
   }

   public FluidStack drain(int maxDrain, FluidAction action) {
      return FluidStack.EMPTY;
   }

   @Override
   public int getMaxEnergyOutput() {
      return (int)(32.0 * IC2.CONFIG.solarTurbine.get());
   }

   @Override
   public int getProvidedEnergy() {
      return Math.min(this.storage, this.getMaxEnergyOutput());
   }
}
