package ic2.core.block.machines.tiles.nv;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.readers.IFuelStorage;
import ic2.api.tiles.readers.IProgressMachine;
import ic2.api.tiles.readers.IPumpTile;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.IParticleSpawner;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.FuelComparator;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.misc.comparator.types.base.TankComparator;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.block.machines.containers.nv.WoodGassifierContainer;
import ic2.core.block.transport.fluid.graph.FluidNet;
import ic2.core.block.transport.fluid.graph.ISimpleFluidSource;
import ic2.core.fluid.ExtractionTank;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.filter.TagFilter;
import ic2.core.inventory.filter.special.FuelFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.registries.IC2Fluids;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class StoneWoodGassifierTileEntity
   extends BaseInventoryTileEntity
   implements ITickListener,
   IProgressMachine,
   IFuelStorage,
   ITileGui,
   IPumpTile,
   IParticleSpawner,
   ISimpleFluidSource,
   ITileActivityProvider,
   IClickable,
   IWrenchableTile {
   @NetworkInfo
   protected ExtractionTank tank = new ExtractionTank(9000);
   @NetworkInfo
   int fuel = 0;
   @NetworkInfo
   int maxFuel = 0;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_16)
   int progress = 0;
   boolean addedToFluidNet = false;

   public StoneWoodGassifierTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 5);
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, this.tank);
      this.addGuiFields(new String[]{"tank", "fuel", "maxFuel", "progress"});
      this.tank.addListener(T -> this.updateGuiField("tank"));
      this.addComparator(new ProgressComparator("progress", ComparatorNames.PROGRESS, this));
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
      this.addComparator(new FuelComparator("fuel", ComparatorNames.FUEL, this));
      this.addComparator(new TankComparator("tank", ComparatorNames.TANK, this.tank));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.registerSlotAccess(AccessRule.IMPORT, 1, 3);
      handler.registerSlotAccess(AccessRule.EXPORT, 2, 4);
      handler.registerSlotsForSide(DirectionList.UP.invert(), 0, 2, 4);
      handler.registerSlotsForSide(DirectionList.DOWN.invert(), 1, 3);
      handler.registerInputFilter(FuelFilter.WITH_LAVA, 0);
      handler.registerOutputFilter(FuelFilter.NOT_WITH_LAVA, 0);
      handler.registerInputFilter(new TagFilter(ItemTags.LOGS), 1);
      handler.registerInputFilter(new SimpleFilter(IC2Items.CELL_EMPTY), 3);
      handler.registerNamedSlot(SlotType.FUEL, 0);
      handler.registerNamedSlot(SlotType.INPUT, 1);
      handler.registerNamedSlot(SlotType.OUTPUT, 2);
      handler.registerNamedSlot(SlotType.CONTAINER, 3);
      handler.registerNamedSlot(SlotType.EXTRA_OUTPUT, 4);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.tank.readFromNBT(compound.getCompound("tank"));
      this.fuel = compound.getInt("fuel");
      this.maxFuel = compound.getInt("max_fuel");
      this.progress = compound.getInt("progress");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("tank", this.tank.writeToNBT(new CompoundTag()));
      compound.putInt("fuel", this.fuel);
      compound.putInt("max_fuel", this.maxFuel);
      compound.putShort("progress", (short)this.progress);
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      return FluidHelper.fillContainers(player.getItemInHand(hand), player, this.tank);
   }

   @Override
   public void onTick() {
      if (this.fuel <= 0 && (!((ItemStack)this.inventory.get(0)).isEmpty() && this.canRun() || this.progress > 0)) {
         int newFuel = IC2.RECIPES.get(this.isSimulating()).getFuel((ItemStack)this.inventory.get(0), false);
         if (newFuel > 0) {
            this.fuel = newFuel;
            this.maxFuel = newFuel;
            this.updateGuiField("maxFuel");
            this.updateGuiField("fuel");
            ItemStack fuel = (ItemStack)this.inventory.get(0);
            if (fuel.hasCraftingRemainingItem()) {
               this.inventory.set(0, fuel.getCraftingRemainingItem());
            } else {
               fuel.shrink(1);
            }
         }
      }

      if (this.fuel > 0) {
         if (this.progress == 0 && this.canRun()) {
            this.progress = (int)this.getMaxProgress();
            ((ItemStack)this.inventory.get(1)).shrink(1);
            this.updateGuiField("progress");
         }

         if (this.progress > 0) {
            this.progress--;
            this.tank.fillInternal(new FluidStack(IC2Fluids.WOOD_GAS, 6), FluidAction.EXECUTE);
            if (this.progress <= 0) {
               this.progress = 0;
               this.tank.fillInternal(new FluidStack(IC2Fluids.WOOD_GAS, 40), FluidAction.EXECUTE);
               if (((ItemStack)this.inventory.get(2)).isEmpty()) {
                  this.inventory.set(2, new ItemStack(Items.CHARCOAL));
               } else {
                  ((ItemStack)this.inventory.get(2)).grow(1);
               }
            }

            this.updateGuiField("progress");
         }

         this.fuel--;
         this.updateGuiField("fuel");
      }

      if (this.clock(10) && this.canFill()) {
         FluidHelper.fillContainers(this, 3, 4, this.tank);
      }

      if (this.clock(10) && this.tank.getFluidAmount() > 0) {
         this.tank.drain(FluidNet.INSTANCE.sendFluid(this, null, this.tank.getFluid(), 25), FluidAction.EXECUTE);
      }

      this.setActive(this.fuel > 0);
   }

   @Override
   public int getFuel() {
      return this.fuel;
   }

   @Override
   public int getMaxFuel() {
      return this.maxFuel;
   }

   @Override
   public float getProgress() {
      return this.progress == 0 ? 0.0F : this.getMaxProgress() - (float)this.progress;
   }

   @Override
   public float getMaxProgress() {
      return 160.0F;
   }

   @Override
   public int getPumpMaxProgress() {
      return this.tank.getCapacity();
   }

   @Override
   public int getPumpProgress() {
      return this.tank.getFluidAmount();
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.WOOD_GASSIFIER;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new WoodGassifierContainer(this, player, windowID);
   }

   public boolean canRun() {
      if (this.tank.getFluidAmount() > 8000) {
         return false;
      } else {
         ItemStack input = (ItemStack)this.inventory.get(1);
         return !input.isEmpty() && input.is(ItemTags.LOGS) && this.hasOutputRoom();
      }
   }

   public boolean hasOutputRoom() {
      return ((ItemStack)this.inventory.get(2)).isEmpty()
         || ((ItemStack)this.inventory.get(2)).getItem() == Items.CHARCOAL
            && ((ItemStack)this.inventory.get(2)).getCount() < ((ItemStack)this.inventory.get(2)).getMaxStackSize();
   }

   public boolean canFill() {
      return this.tank.getFluidAmount() >= 1000 && !((ItemStack)this.inventory.get(3)).isEmpty();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void animationTick(RandomSource rand) {
      if (this.isActive()) {
         Direction direction = this.getFacing();
         Axis axis = direction.getAxis();
         double d0 = (double)this.worldPosition.getX() + 0.5 + (axis == Axis.X ? (double)direction.getStepX() * 0.52 : rand.nextDouble() * 0.6 - 0.3);
         double d1 = (double)this.worldPosition.getY() + rand.nextDouble() * 6.0 / 16.0;
         double d2 = (double)this.worldPosition.getZ() + 0.5 + (axis == Axis.Z ? (double)direction.getStepZ() * 0.52 : rand.nextDouble() * 0.6 - 0.3);
         if (rand.nextDouble() < 0.1) {
            this.getLevel().playLocalSound(d0, d1, d2, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         this.getLevel().addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
         this.getLevel().addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0, 0.0, 0.0);
      }
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating() && !this.addedToFluidNet) {
         this.addedToFluidNet = true;
         FluidNet.INSTANCE.addPipe(this);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.addedToFluidNet) {
         this.addedToFluidNet = false;
         FluidNet.INSTANCE.removePipe(this);
      }

      super.onUnloaded(chunk);
   }

   @Override
   public boolean canReceiveFluid(Direction dir) {
      return false;
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
      return 1.0;
   }

   @Override
   public boolean isHarvestWrenchRequired(Player player) {
      return false;
   }
}
