package ic2.core.block.machines.tiles.mv;

import ic2.api.items.IFuelableItem;
import ic2.api.items.IRepairable;
import ic2.api.items.IUpgradeItem;
import ic2.api.items.armor.IFoamSupplier;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.registries.ICannerRecipeRegistry;
import ic2.api.tiles.readers.ISpeedMachine;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.IInventoryMachine;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.misc.comparator.types.base.SpeedComparator;
import ic2.core.block.base.tiles.impls.machine.single.BaseMachineTileEntity;
import ic2.core.block.machines.containers.mv.VacuumCannerContainer;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.inv.RangedInventory;
import ic2.core.platform.recipes.misc.FoodCanRegistry;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.StackUtil;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class VacuumCannerTileEntity extends BaseMachineTileEntity implements ITickListener, IInventoryMachine, ITileGui, ISpeedMachine {
   public static final Component SPEED = Component.translatable("info.block.ic2.vacuum_canner.speed");
   public static final EnumSet<IUpgradeItem.UpgradeType> TYPES = EnumSet.of(
      IUpgradeItem.UpgradeType.REDSTONE_MOD,
      IUpgradeItem.UpgradeType.CUSTOM_MOD,
      IUpgradeItem.UpgradeType.PROCESSING_MOD,
      IUpgradeItem.UpgradeType.MACHINE_MOD,
      IUpgradeItem.UpgradeType.TRANSPORT_MOD
   );
   @NetworkInfo
   public int progress = 0;
   @NetworkInfo
   public int speed = 0;
   public int fuelStorage = 0;
   public int step = 0;
   public boolean isProcessing = false;

   public VacuumCannerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 7, 2, 15, 8000, 4500, 128);
      this.setFuelSlot(0);
      this.addGuiFields(new String[]{"progress", "speed"});
      this.addComparator(new ProgressComparator("progress", ComparatorNames.PROGRESS, this));
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
      this.addComparator(new FlagComparator("working", ComparatorNames.WORKING, () -> this.isProcessing, 0, 15));
      this.addComparator(new SpeedComparator("speed", ComparatorNames.SPEED, this));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerSlotAccess(AccessRule.IMPORT, 1, 2, 3, 4);
      handler.registerSlotAccess(AccessRule.EXPORT, 5, 6);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.registerSlotsForSide(DirectionList.UP, 1);
      handler.registerSlotsForSide(DirectionList.HORIZONTAL, 2, 3, 4);
      handler.registerSlotsForSide(DirectionList.UP.invert(), 0, 5, 6);
      handler.registerInputFilter(SpecialFilters.createChargeFilter(), 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_DISCHARGE_FILTER, 0);
      handler.registerInputFilter(T -> this.getValidContainer(T, false) > 0, 1);
      handler.registerInputFilter(this::canInsertItem, 2, 3, 4);
      handler.registerInputFilter(SpecialFilters.ALWAYS_FALSE, 5, 6);
      handler.registerNamedSlot(SlotType.BATTERY, 0);
      handler.registerNamedSlot(SlotType.CONTAINER, 1);
      handler.registerNamedSlot(SlotType.INPUT, 2, 3, 4);
      handler.registerNamedSlot(SlotType.OUTPUT, 5);
      handler.registerNamedSlot(SlotType.EXTRA_OUTPUT, 6);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.VACUUM_CANNER;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new VacuumCannerContainer(this, player, windowID);
   }

   public Component getSpeedName() {
      return SPEED;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.speed = compound.getInt("speed");
      this.progress = compound.getInt("progress");
      this.fuelStorage = compound.getInt("fuel_value");
      this.step = compound.getInt("step");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putInt("progress", this.progress);
      compound.putInt("fuel_value", this.fuelStorage);
      compound.putByte("step", (byte)this.step);
      compound.putInt("speed", this.speed);
   }

   @Override
   protected void createInvCaches() {
      this.inOut = new IHasInventory[2];
      this.inOut[0] = new RangedInventory(this, 1, 2, 3, 4);
      this.inOut[1] = new RangedInventory(this, 5, 6).setOutputOnly();
   }

   @Override
   public void onTick() {
      if (this.clock(10)) {
         this.transferStack(4, 3);
         this.transferStack(3, 2);
      }

      this.handleChargeSlot(this.maxEnergy);
      this.handleRedstone();
      int mode = this.getMode((ItemStack)this.inventory.get(1));
      boolean canOperate = this.canOperate(mode);
      boolean canRun = canOperate || this.isRedstonePowered();
      if (canRun && this.hasEnergy(1)) {
         if (this.speed < 10000) {
            this.speed++;
            this.updateGuiField("speed");
         }

         this.useEnergy(1);
         this.setActive(true);
      } else {
         if (this.speed > 0) {
            this.speed = Math.max(0, this.speed - 4);
            this.updateGuiField("speed");
         }

         this.setActive(false);
      }

      this.progressPerTick = (float)(this.speed / 30);
      if (canOperate && this.hasEnergy(this.energyConsume)) {
         if (this.canProceedInMode(mode)) {
            this.operate(mode);
            this.notifyListeners();
            this.storage.onRecipeFinished(this.inventory, this);
         } else {
            this.progress = this.progress + (int)this.progressPerTick;
            this.updateGuiField("progress");
         }

         this.isProcessing = true;
         this.useEnergy(this.energyConsume);
      } else {
         if ((((ItemStack)this.inventory.get(1)).isEmpty() || ((ItemStack)this.inventory.get(2)).isEmpty()) && this.progress > 0) {
            this.progress = 0;
            this.updateGuiField("progress");
            if (((ItemStack)this.inventory.get(1)).isEmpty()) {
               this.fuelStorage = 0;
               this.step = 0;
            }
         }

         if (this.progress > 0) {
            int speedLeft = Math.max((10000 - this.speed) / 30, 1);
            this.progress = Math.max(0, !canOperate ? 0 : this.progress - speedLeft);
            this.updateGuiField("progress");
         }

         this.isProcessing = false;
      }

      this.storage.onTick(this.inventory, this);
      this.handleComparators();
   }

   @Override
   public int getSpeed() {
      return this.speed;
   }

   @Override
   public int getMaxSpeed() {
      return 10000;
   }

   @Override
   public float getProgress() {
      return (float)this.progress;
   }

   @Override
   public float getMaxProgress() {
      switch (this.getMode((ItemStack)this.inventory.get(1))) {
         case 1:
            return (float)(800 * this.getFoodAmount((ItemStack)this.inventory.get(2)));
         case 2:
            return 8000.0F;
         case 3:
         case 4:
         case 5:
            return 1200.0F;
         default:
            return 0.0F;
      }
   }

   public void operate(int mode) {
      switch (mode) {
         case 1:
            ItemStack output = this.getFoodOutput(this.getFoodAmount((ItemStack)this.inventory.get(2)));
            this.setOrGrow(5, output, false);
            ((ItemStack)this.inventory.get(1)).shrink(output.getCount());
            if (((ItemStack)this.inventory.get(2)).hasCraftingRemainingItem()) {
               this.setOrGrow(6, ((ItemStack)this.inventory.get(2)).getCraftingRemainingItem(), false);
            }

            ((ItemStack)this.inventory.get(2)).shrink(1);
            break;
         case 2:
            ICannerRecipeRegistry.FuelValue value = this.getRecipes().getValueForStack((ItemStack)this.inventory.get(2));
            this.fuelStorage = value.apply(this.fuelStorage);
            if (((ItemStack)this.inventory.get(2)).hasCraftingRemainingItem()) {
               this.setOrGrow(6, ((ItemStack)this.inventory.get(2)).getCraftingRemainingItem(), false);
            }

            ((ItemStack)this.inventory.get(2)).shrink(1);
            this.step++;
            if (this.progress < 8000) {
               return;
            }

            this.step = 0;
            if (((ItemStack)this.inventory.get(1)).getItem() instanceof IFuelableItem item) {
               this.inventory.set(5, item.fill(((ItemStack)this.inventory.get(1)).copy(), this.fuelStorage));
               ((ItemStack)this.inventory.get(1)).shrink(1);
            }
            break;
         case 3:
            IFoamSupplier supplier = (IFoamSupplier)((ItemStack)this.inventory.get(1)).getItem();
            ((ItemStack)this.inventory.get(2)).shrink(1);
            supplier.fillFoam((ItemStack)this.inventory.get(1), 26);
            if (supplier.getFreeFoamSpace((ItemStack)this.inventory.get(1)) < 26) {
               this.inventory.set(5, (ItemStack)this.inventory.get(1));
               this.inventory.set(1, ItemStack.EMPTY);
            }
            break;
         case 4:
            Tuple<IInput, Integer> entry = this.getRecipes().getRepairableItem((ItemStack)this.inventory.get(1), (ItemStack)this.inventory.get(2), true);
            if (entry != null) {
               if (((ItemStack)this.inventory.get(2)).hasCraftingRemainingItem()) {
                  ItemStack container = ((ItemStack)this.inventory.get(2)).getCraftingRemainingItem();
                  container.setCount(Mth.clamp(((IInput)entry.getA()).getInputSize(), 1, container.getMaxStackSize()));
                  this.setOrGrow(6, container, false);
               }

               if (!((IRepairable)((ItemStack)this.inventory.get(1)).getItem()).repairDamage((ItemStack)this.inventory.get(1), (Integer)entry.getB())) {
                  this.inventory.set(5, (ItemStack)this.inventory.get(1));
                  this.inventory.set(1, ItemStack.EMPTY);
               } else {
                  ((ItemStack)this.inventory.get(2)).shrink(((IInput)entry.getA()).getInputSize());
               }
            }
            break;
         case 5:
            Tuple<IInput, ItemStack> result = this.getRecipes().getFillOutput((ItemStack)this.inventory.get(1), (ItemStack)this.inventory.get(2), true);
            if (result != null) {
               this.setOrGrow(5, ((ItemStack)result.getB()).copy(), false);
               ((ItemStack)this.inventory.get(1)).shrink(1);
               ((ItemStack)this.inventory.get(2)).shrink(((IInput)result.getA()).getInputSize());
            }
      }

      this.fuelStorage = 0;
      this.progress = 0;
      this.step = 0;
      this.updateGuiField("progress");
   }

   public boolean canProceedInMode(int mode) {
      switch (mode) {
         case 1:
            return this.progress >= this.getFoodAmount((ItemStack)this.inventory.get(2)) * 800;
         case 2:
            return this.progress > 0 && this.progress / 800 > this.step;
         case 3:
         case 4:
         case 5:
            return this.progress >= 1200;
         default:
            return false;
      }
   }

   public boolean canOperate(int mode) {
      if (!((ItemStack)this.inventory.get(1)).isEmpty() && mode != 0) {
         switch (mode) {
            case 1:
               return this.canFoodFitIntoOutput(this.getFoodAmount((ItemStack)this.inventory.get(2)));
            case 2:
               return this.getFuelValue((ItemStack)this.inventory.get(2)) > 0
                  && ((ItemStack)this.inventory.get(5)).isEmpty()
                  && this.canContainerFit((ItemStack)this.inventory.get(2));
            case 3:
               return ((ItemStack)this.inventory.get(2)).getItem() == IC2Items.CF_PELLET
                  && ((IFoamSupplier)((ItemStack)this.inventory.get(1)).getItem()).getFreeFoamSpace((ItemStack)this.inventory.get(1)) >= 26
                  && ((ItemStack)this.inventory.get(5)).isEmpty();
            case 4:
               return ((ItemStack)this.inventory.get(5)).isEmpty()
                  && this.getRecipes().getRepairableItem((ItemStack)this.inventory.get(1), (ItemStack)this.inventory.get(2), true) != null
                  && this.canContainerFit((ItemStack)this.inventory.get(2));
            case 5:
               Tuple<IInput, ItemStack> output = this.getRecipes().getFillOutput((ItemStack)this.inventory.get(1), (ItemStack)this.inventory.get(2), true);
               return output != null
                  && (
                     ((ItemStack)this.inventory.get(5)).isEmpty()
                        || StackUtil.isStackEqual((ItemStack)this.inventory.get(5), (ItemStack)output.getB())
                           && StackUtil.getStackSizeLeft((ItemStack)this.inventory.get(5)) >= ((ItemStack)output.getB()).getCount()
                  );
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public int getMode(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         Item item = stack.getItem();
         if (item == IC2Items.TIN_CAN) {
            return 1;
         } else if (item instanceof IFuelableItem && ((IFuelableItem)item).canFuel(stack)) {
            return 2;
         } else if (item instanceof IFoamSupplier) {
            return 3;
         } else if (item instanceof IRepairable) {
            return 4;
         } else {
            return this.getRecipes().hasContainer(stack) ? 5 : 0;
         }
      }
   }

   public int getFuelValue(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         ICannerRecipeRegistry.FuelValue value = this.getRecipes().getValueForStack(stack);
         return value == null ? 0 : value.test(this.fuelStorage);
      }
   }

   public int getFoodAmount(ItemStack stack) {
      Item item = stack.getItem();
      return item.isEdible() ? Math.max(1, Mth.ceil((double)item.getFoodProperties().getNutrition() / 2.0)) : (item == Items.CAKE ? 6 : 0);
   }

   public ItemStack getFoodOutput(int amount) {
      if (amount <= 0) {
         return ItemStack.EMPTY;
      } else {
         Item output = FoodCanRegistry.INSTANCE.getItemForFood((ItemStack)this.inventory.get(2));
         return new ItemStack(output == null ? IC2Items.TIN_CAN_FILLED : output, amount);
      }
   }

   public boolean canFoodFitIntoOutput(int amount) {
      ItemStack stack = this.getFoodOutput(amount);
      if (stack.isEmpty()) {
         return false;
      } else {
         ItemStack stored = (ItemStack)this.inventory.get(5);
         return (stored.isEmpty() || StackUtil.isStackEqual(stored, stack))
            && StackUtil.getStackSizeLeft(stored) >= amount
            && ((ItemStack)this.inventory.get(1)).getCount() >= amount
            && this.canContainerFit(stored);
      }
   }

   public boolean canContainerFit(ItemStack input) {
      if (!input.hasCraftingRemainingItem()) {
         return true;
      } else {
         ItemStack container = input.getCraftingRemainingItem();
         ItemStack stored = (ItemStack)this.inventory.get(6);
         return stored.isEmpty() || StackUtil.isStackEqual(stored, container) && StackUtil.getStackSizeLeft(stored) >= container.getCount();
      }
   }

   public int getValidContainer(ItemStack stack, boolean container) {
      if (stack.isEmpty()) {
         return 0;
      } else if (((ItemStack)this.inventory.get(1)).isEmpty()) {
         return this.getMode(stack) > 0 ? stack.getMaxStackSize() : 0;
      } else {
         return !StackUtil.isStackEqual((ItemStack)this.inventory.get(1), stack) && (!container || this.getMode(stack) <= 0)
            ? 0
            : StackUtil.getStackSizeLeft((ItemStack)this.inventory.get(1));
      }
   }

   public boolean canInsertItem(ItemStack stack) {
      switch (this.getMode((ItemStack)this.inventory.get(1))) {
         case 1:
            return this.getFoodAmount(stack) > 0;
         case 2:
            return this.getFuelValue(stack) > 0;
         case 3:
            return stack.getItem() == IC2Items.CF_PELLET;
         case 4:
            return this.getRecipes().getRepairableItem((ItemStack)this.inventory.get(1), stack, false) != null;
         case 5:
            return this.getRecipes().getFillOutput((ItemStack)this.inventory.get(1), stack, false) != null;
         default:
            return this.getFoodAmount(stack) > 0 || this.getRecipes().getValueForStack(stack) != null || stack.getItem() == IC2Items.CF_PELLET;
      }
   }

   @Override
   public int getValidRoom(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         int mode = this.getMode((ItemStack)this.inventory.get(1));
         if (mode > 0) {
            for (int i = 2; i < 5; i++) {
               ItemStack inv = (ItemStack)this.inventory.get(i);
               if (inv.isEmpty()) {
                  int size = this.getRoomSize(mode, stack);
                  if (size > 0) {
                     return size;
                  }
               } else if (StackUtil.isStackEqual(inv, stack)) {
                  return StackUtil.getStackSizeLeft(inv);
               }
            }
         }

         return this.getValidContainer(stack, false);
      }
   }

   protected int getRoomSize(int mode, ItemStack stack) {
      switch (mode) {
         case 1:
            return this.getFoodAmount(stack) > 0 ? stack.getMaxStackSize() : 0;
         case 2:
            return this.getFuelValue(stack) > 0 ? stack.getMaxStackSize() : 0;
         case 3:
            return stack.getItem() == IC2Items.CF_PELLET ? stack.getMaxStackSize() : 0;
         case 4:
            return this.getRecipes().getRepairableItem((ItemStack)this.inventory.get(1), stack, false) != null ? stack.getMaxStackSize() : 0;
         case 5:
            return this.getRecipes().getFillOutput((ItemStack)this.inventory.get(1), stack, false) != null ? stack.getMaxStackSize() : 0;
         default:
            return 0;
      }
   }

   public ICannerRecipeRegistry getRecipes() {
      return IC2.RECIPES.get(this.isSimulating()).canner;
   }

   @Override
   public EnumSet<IUpgradeItem.UpgradeType> getSupportedUpgradeTypes() {
      return TYPES;
   }

   @Override
   public int getAvailableEnergy() {
      return this.energy;
   }

   @Override
   public boolean useEnergy(int toUse, boolean doUse) {
      if (this.hasEnergy(toUse)) {
         if (doUse) {
            this.useEnergy(toUse);
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isMachineWorking() {
      return this.isProcessing;
   }

   @Override
   public boolean isRedstoneSensitive() {
      return this.sensitive;
   }

   @Override
   public void setRedstoneSensitive(boolean flag) {
      this.sensitive = flag;
   }

   @Override
   public void onUpgradesChanged() {
      this.handleUpgrades(true);
   }

   @Override
   protected void handleMods() {
   }
}
