package ic2.core.block.generators.tiles;

import ic2.api.energy.tile.IEnergySource;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.tiles.impls.BaseGeneratorTileEntity;
import ic2.core.block.generators.containers.GeoGenContainer;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.FluidFilter;
import ic2.core.inventory.filter.InvertedFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.registries.IC2Fluids;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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

public class GeoGenTileEntity extends BaseGeneratorTileEntity implements IFluidHandler, IClickable {
   @NetworkInfo
   public int maxFuel = 24000;

   public GeoGenTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3);
      this.maxStorage = 20;
      this.production = IC2.CONFIG.geothermalOutput.get();
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, this);
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, 0, 1, 2);
      handler.registerSlotsForSide(DirectionList.DOWN, 0);
      handler.registerSlotsForSide(DirectionList.ALL, 1, 2);
      handler.registerInputFilter(new FluidFilter(Fluids.LAVA), 1);
      handler.registerOutputFilter(SpecialFilters.ALWAYS_FALSE, 1);
      handler.registerOutputFilter(new InvertedFilter(new FluidFilter(Fluids.LAVA)), 2);
      handler.registerInputFilter(SpecialFilters.ALWAYS_FALSE, 2);
      handler.registerInputFilter(ElectricItemFilter.CHARGE_FILTER, 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_CHARGE_FILTER, 0);
      handler.registerNamedSlot(SlotType.CHARGE, 0);
      handler.registerNamedSlot(SlotType.FUEL, 1);
      handler.registerNamedSlot(SlotType.OUTPUT, 2);
   }

   @Override
   public int getMaxFuel() {
      return this.maxFuel;
   }

   @Override
   public IEnergySource.SourceType getSourceType() {
      return IEnergySource.SourceType.INTELLIGENT_CONSUMING;
   }

   @Override
   public ResourceLocation getProcessingSound() {
      return IC2Sounds.GEOTHERMAL_OPERATING;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new GeoGenContainer(this, player, windowID);
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      return FluidHelper.drainContainers(player.getItemInHand(hand), player, this);
   }

   @Override
   public boolean needsFuel() {
      return this.fuel <= 23000;
   }

   @Override
   public float getEUProduction() {
      return this.fuel > 0 ? (float)this.production : 0.0F;
   }

   @Override
   public boolean gainFuel() {
      return FluidHelper.drainContainers(this, 1, 2, this);
   }

   @Override
   public boolean hasAverageActivity() {
      return true;
   }

   @Override
   public int getDelay() {
      return 10;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.GENERATOR_GEOTHERMAL;
   }

   public int getTanks() {
      return 1;
   }

   public FluidStack getFluidInTank(int tank) {
      return this.fuel == 0 ? FluidStack.EMPTY : new FluidStack(Fluids.LAVA, this.fuel);
   }

   public int getTankCapacity(int tank) {
      return this.maxFuel;
   }

   public boolean isFluidValid(int tank, FluidStack stack) {
      return stack.getFluid() == Fluids.LAVA || stack.getFluid() == IC2Fluids.BLAZING_LAVA;
   }

   public int fill(FluidStack resource, FluidAction action) {
      if (resource.getFluid() == Fluids.LAVA) {
         int toAdd = Math.min(resource.getAmount(), this.maxFuel - this.fuel);
         if (toAdd > 0 && action.execute()) {
            this.fuel += toAdd;
            this.updateTileField("fuel");
         }

         return toAdd;
      } else if (resource.getFluid() == IC2Fluids.BLAZING_LAVA) {
         int toAdd = Math.min(resource.getAmount(), (this.maxFuel - this.fuel) / 5);
         if (toAdd > 0 && action.execute()) {
            this.fuel += toAdd * 5;
            this.updateTileField("fuel");
         }

         return toAdd;
      } else {
         return 0;
      }
   }

   public FluidStack drain(FluidStack resource, FluidAction action) {
      return resource.getFluid() != Fluids.LAVA ? FluidStack.EMPTY : this.drain(resource.getAmount(), action);
   }

   public FluidStack drain(int maxDrain, FluidAction action) {
      int drain = Math.min(this.fuel, maxDrain);
      if (drain > 0 && action.execute()) {
         this.fuel -= drain;
         this.updateTileField("fuel");
      }

      return new FluidStack(Fluids.LAVA, drain);
   }

   @Override
   public void onPacketFailed() {
      if (this.storage < this.maxStorage && this.fuel > 0) {
         this.storage = Math.min(this.maxStorage, this.storage + this.production);
         this.fuel--;
      }
   }
}
