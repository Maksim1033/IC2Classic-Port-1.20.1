package ic2.core.block.machines.tiles.lv;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.readers.IPumpTile;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.audio.IAudioSource;
import ic2.core.block.base.cache.CapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.PumpComparator;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.containers.lv.PumpContainer;
import ic2.core.block.transport.fluid.graph.FluidNet;
import ic2.core.block.transport.fluid.graph.ISimpleFluidSource;
import ic2.core.fluid.ExtractionTank;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.FluidFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.FluidHelper;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;

public class PumpTileEntity extends BaseElectricTileEntity implements IPumpTile, ITickListener, ITileGui, ISimpleFluidSource, ITileActivityProvider, IClickable {
   @NetworkInfo
   public ExtractionTank tank = new ExtractionTank(4000);
   @NetworkInfo
   public int progress = 0;
   int delay = 0;
   ICache<IFluidHandler> fluid_cache = new CapabilityCache<>(this, DirectionList.DOWN.invert(), ForgeCapabilities.FLUID_HANDLER);
   ICache<IItemHandler> inventory_cache = new CapabilityCache<>(this, DirectionList.DOWN.invert(), ForgeCapabilities.ITEM_HANDLER);
   IAudioSource source;
   boolean addedToFluidNet = false;

   public PumpTileEntity(BlockPos pos, BlockState state) {
      this(pos, state, 32, 200);
   }

   public PumpTileEntity(BlockPos pos, BlockState state, int maxInput, int maxEnergy) {
      super(pos, state, 3, maxInput, maxEnergy);
      this.setFuelSlot(0);
      this.addGuiFields(new String[]{"progress", "tank"});
      this.tank.addListener(T -> this.updateGuiField("tank"));
      this.addCaches(new ICache[]{this.fluid_cache, this.inventory_cache});
      this.addComparator(new PumpComparator("pump", ComparatorNames.PUMP, this));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.DOWN.invert());
      handler.registerBlockAccess(DirectionList.DOWN.invert(), AccessRule.BOTH);
      handler.registerBlockAccess(DirectionList.DOWN, AccessRule.DISABLED);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.registerSlotAccess(AccessRule.IMPORT, 1);
      handler.registerSlotAccess(AccessRule.EXPORT, 2);
      handler.registerSlotsForSide(DirectionList.HORIZONTAL, 0);
      handler.registerSlotsForSide(DirectionList.UP, 1);
      handler.registerSlotsForSide(DirectionList.HORIZONTAL, 2);
      handler.registerInputFilter(SpecialFilters.createChargeFilter(), 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_DISCHARGE_FILTER, 0);
      handler.registerInputFilter(FluidFilter.EMPTY_CONTAINER, 1);
      handler.registerNamedSlot(SlotType.BATTERY, 0);
      handler.registerNamedSlot(SlotType.INPUT, 1);
      handler.registerNamedSlot(SlotType.OUTPUT, 2);
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      return FluidHelper.fillContainers(player.getItemInHand(hand), player, this.tank);
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("tank", this.tank.writeToNBT(new CompoundTag()));
      compound.putShort("progress", (short)this.progress);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.tank.readFromNBT(compound.getCompound("tank"));
      this.progress = compound.getInt("progress");
   }

   @Override
   public void onTick() {
      this.handleChargeSlot((int)((float)this.maxEnergy * 0.9F));
      if (this.delay > 0) {
         this.delay--;
      }

      if (this.progress < this.getPumpMaxProgress() && this.hasEnergy(this.getPumpCost())) {
         this.progress++;
         this.useEnergy(this.getPumpCost());
         this.updateGuiField("progress");
      }

      if (this.delay <= 0 && this.progress >= this.getPumpMaxProgress() && this.tank.getFluidAmount() <= 3000) {
         FluidStack stack = this.getFluidBlockBelow(false);
         if (!stack.isEmpty() && this.tank.fillInternal(stack, FluidAction.SIMULATE) == stack.getAmount()) {
            this.tank.fillInternal(stack, FluidAction.EXECUTE);
            this.getFluidBlockBelow(true);
            this.progress = 0;
            this.updateGuiField("progress");
         } else {
            this.delay = 20;
         }
      }

      if (this.clock(5)) {
         if (this.tank.getFluidAmount() > 0) {
            this.drainStorage();
         }

         if (!((ItemStack)this.inventory.get(2)).isEmpty()) {
            this.drainContainers();
         }
      }

      if (this.tank.getFluidAmount() > 0) {
         int drained = FluidNet.INSTANCE.sendFluid(this, null, this.tank.getFluid(), 100);
         if (drained > 0) {
            this.tank.drain(drained, FluidAction.EXECUTE);
         }
      }

      if (this.isActive() == this.progress >= this.getPumpMaxProgress() && this.energy > 0) {
         this.setActive(!this.isActive());
      }

      this.handleComparators();
   }

   protected void drainContainers() {
      InventoryHandler handler = this.getInventoryHandler();

      for (Direction dir : this.inventory_cache) {
         if (handler.isBlockSideUnlocked(dir) && handler.getBlockAccess(dir).canExport()) {
            IItemTransporter transporter = TransporterManager.getTransporter(this.inventory_cache.getHandler(dir));
            if (transporter != null) {
               int added = transporter.addItem((ItemStack)this.inventory.get(2), dir.getOpposite(), false);
               if (added > 0) {
                  ((ItemStack)this.inventory.get(2)).shrink(added);
                  if (((ItemStack)this.inventory.get(2)).isEmpty()) {
                     return;
                  }
               }
            }
         }
      }
   }

   protected void drainStorage() {
      if (!((ItemStack)this.inventory.get(1)).isEmpty()) {
         FluidActionResult result = FluidUtil.tryFillContainer((ItemStack)this.inventory.get(1), this.tank, 2147483647, null, false);
         if (result.isSuccess()) {
            result = FluidUtil.tryFillContainer((ItemStack)this.inventory.get(1), this.tank, 2147483647, null, true);
            if (!result.isSuccess()) {
               return;
            }

            this.setOrGrow(2, result.getResult(), true);
            ((ItemStack)this.inventory.get(1)).shrink(1);
            return;
         }
      }

      InventoryHandler handler = this.getInventoryHandler();

      for (Direction dir : this.fluid_cache) {
         if (handler.isBlockSideUnlocked(dir) && handler.getBlockAccess(dir).canExport()) {
            IFluidHandler entry = this.fluid_cache.getHandler(dir);
            if (entry != null) {
               this.tank
                  .drain(entry.fill(new FluidStack(this.tank.getFluid(), Math.min(this.tank.getFluidAmount(), 1000)), FluidAction.EXECUTE), FluidAction.EXECUTE);
               if (this.tank.getFluidAmount() <= 0) {
                  return;
               }
            }
         }
      }
   }

   public InteractionResult pumpExternal(FluidStack fluid, boolean doPump) {
      if (fluid.isEmpty() || this.tank.fillInternal(fluid, FluidAction.SIMULATE) != fluid.getAmount()) {
         return InteractionResult.FAIL;
      } else if (this.getPumpProgress() < this.getPumpMaxProgress()) {
         return InteractionResult.PASS;
      } else {
         if (doPump) {
            this.tank.fillInternal(fluid, FluidAction.EXECUTE);
            this.progress = 0;
            this.updateGuiField("progress");
         }

         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public boolean canReceiveFluid(Direction dir) {
      return false;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating() && !this.addedToFluidNet) {
         FluidNet.INSTANCE.addPipe(this);
         this.addedToFluidNet = true;
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.addedToFluidNet) {
         FluidNet.INSTANCE.removePipe(this);
         this.addedToFluidNet = false;
      }

      this.source = null;
      IC2.AUDIO.removeSource(this);
      super.onUnloaded(chunk);
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("isActive")) {
         if (this.source == null || !this.source.isValid()) {
            this.source = IC2.AUDIO.createSource(this, IC2Sounds.PUMP_PROCESSING);
            if (this.source == null) {
               return;
            }
         }

         this.source.playStop(this.isActive() && this.hasEnergy(1));
      }
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.PUMP;
   }

   @Override
   public int getPumpProgress() {
      return this.progress;
   }

   @Override
   public int getPumpMaxProgress() {
      return 120;
   }

   public int getPumpCost() {
      return 1;
   }

   public boolean hasNoFluidBelow() {
      return this.getFluidBlockBelow(false).isEmpty();
   }

   public FluidStack getFluidBlockBelow(boolean remove) {
      BlockPos pos = this.getBlockPos().below();
      BlockState state = this.getLevel().getBlockState(pos);
      FluidState fluid = state.getFluidState();
      if (fluid.isSource() && state.getBlock() instanceof BucketPickup bucket) {
         if (remove) {
            bucket.pickupBlock(this.getLevel(), pos, state);
         }

         return new FluidStack(fluid.getType(), 1000);
      } else {
         return FluidStack.EMPTY;
      }
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new PumpContainer(this, player, windowID);
   }
}
