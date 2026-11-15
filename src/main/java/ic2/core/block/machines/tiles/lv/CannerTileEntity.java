package ic2.core.block.machines.tiles.lv;

import ic2.api.items.IFuelableItem;
import ic2.api.items.IRepairable;
import ic2.api.items.IUpgradeItem;
import ic2.api.items.armor.IFoamSupplier;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.ItemInput;
import ic2.api.recipes.ingridients.inputs.ItemStackInput;
import ic2.api.recipes.ingridients.inputs.ItemTagInput;
import ic2.api.recipes.registries.ICannerRecipeRegistry;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.IInventoryMachine;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.tiles.impls.machine.single.BaseMachineTileEntity;
import ic2.core.block.machines.containers.lv.CannerContainer;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.filter.special.MachineFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.inv.RangedInventory;
import ic2.core.item.reactor.base.IUraniumRod;
import ic2.core.item.reactor.urantypes.BlazeUranium;
import ic2.core.item.reactor.urantypes.CharcoalUranium;
import ic2.core.item.reactor.urantypes.EnderUranium;
import ic2.core.item.reactor.urantypes.NetherStarUranium;
import ic2.core.item.reactor.urantypes.RedstoneUranium;
import ic2.core.item.reactor.urantypes.StandardUranium;
import ic2.core.platform.recipes.misc.FoodCanRegistry;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.StackUtil;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CannerTileEntity extends BaseMachineTileEntity implements ITickListener, IInventoryMachine, ITileGui {
   public static final EnumSet<IUpgradeItem.UpgradeType> TYPES = EnumSet.complementOf(
      EnumSet.of(IUpgradeItem.UpgradeType.REDSTONE_MOD, IUpgradeItem.UpgradeType.AUDIO_MOD)
   );
   @NetworkInfo(NetworkInfo.BitLevel.BIT_16)
   public int progress = 0;
   public int fuelStorage = 0;
   public int step = 0;
   public int recipeSpeed = 1;

   public CannerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 5, 4, 1, 1, 700, 32);
      this.setFuelSlot(0);
      this.addGuiFields(new String[]{"progress"});
      this.addComparator(new ProgressComparator("progress", ComparatorNames.PROGRESS, this));
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
   }

   public static void loadRecipes(ICannerRecipeRegistry registry) {
      registry.registerFuelValue(new ItemStack(IC2Items.CELL_COAL_FUEL), 2548);
      registry.registerFuelValue(new ItemStack(IC2Items.CELL_BIO_FUEL), 868);
      registry.registerFuelMultiplier(new ItemStack(Items.REDSTONE), 0.2F);
      registry.registerFuelMultiplier(new ItemStack(Items.GLOWSTONE_DUST), 0.3F);
      registry.registerFuelMultiplier(new ItemStack(Items.GUNPOWDER), 0.4F);
      registry.registerFuelMultiplier(new ItemStack(IC2Items.CELL_FUEL), 0.7F);
      registry.registerFillable(new ItemStack(IC2Items.CELL_EMPTY), new ItemInput(IC2Items.HYDRATED_COAL_COMPRESSED), new ItemStack(IC2Items.CELL_COAL_FUEL));
      registry.registerFillable(new ItemStack(IC2Items.CELL_EMPTY), new ItemInput(IC2Items.PLANT_BALL_COMPRESSED), new ItemStack(IC2Items.CELL_BIO_FUEL));
      registry.registerRepairable((IRepairable)IC2Items.CONDENSATOR, new ItemTagInput(net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE), 10000);
      registry.registerRepairable((IRepairable)IC2Items.CONDENSATOR_LAP, new ItemTagInput(net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE), 4000);
      registry.registerRepairable((IRepairable)IC2Items.CONDENSATOR_LAP, new ItemTagInput(net.minecraftforge.common.Tags.Items.GEMS_LAPIS), 40000);
      registry.registerRepairable((IRepairable)IC2Items.REFLECTOR_IRIDIUM, new ItemTagInput(IC2Tags.DUST_COAL), 10000);
      registry.registerRepairable((IRepairable)IC2Items.REFLECTOR_IRIDIUM, new ItemTagInput(IC2Tags.DUST_CHARCOAL), 5000);
      registry.registerFillable(new ItemStack(IC2Items.CELL_EMPTY), new ItemTagInput(IC2Tags.INGOT_URANIUM), StandardUranium.INSTANCE.createSingleRod());
      addUraniumRodRecipe(registry, BlazeUranium.INSTANCE);
      addUraniumRodRecipe(registry, CharcoalUranium.INSTANCE);
      addUraniumRodRecipe(registry, EnderUranium.INSTANCE);
      addUraniumRodRecipe(registry, NetherStarUranium.INSTANCE);
      addUraniumRodRecipe(registry, RedstoneUranium.INSTANCE);
   }

   public static void addUraniumRodRecipe(ICannerRecipeRegistry registry, IUraniumRod rod) {
      if (!IC2.CONFIG.enableHardEnrichedUranium.get() || !rod.isEnrichedUranium()) {
         registry.registerFillable(new ItemStack(IC2Items.CELL_EMPTY), new ItemStackInput(rod.getBaseIngot()), rod.createSingleRod());
      }
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerSlotAccess(AccessRule.IMPORT, 1, 2);
      handler.registerSlotAccess(AccessRule.EXPORT, 3, 4);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.registerSlotsForSide(DirectionList.UP, 1);
      handler.registerSlotsForSide(DirectionList.HORIZONTAL, 2);
      handler.registerSlotsForSide(DirectionList.UP.invert(), 0, 3, 4);
      handler.registerInputFilter(SpecialFilters.createChargeFilter(), 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_DISCHARGE_FILTER, 0);
      handler.registerInputFilter(T -> this.getValidContainer(T, false) > 0, 1);
      handler.registerInputFilter(new MachineFilter(this), 2);
      handler.registerInputFilter(SpecialFilters.ALWAYS_FALSE, 3, 4);
      handler.registerNamedSlot(SlotType.BATTERY, 0);
      handler.registerNamedSlot(SlotType.CONTAINER, 1);
      handler.registerNamedSlot(SlotType.INPUT, 2);
      handler.registerNamedSlot(SlotType.OUTPUT, 3);
      handler.registerNamedSlot(SlotType.EXTRA_OUTPUT, 4);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.CANNER;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new CannerContainer(this, player, windowID);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.progress = compound.getInt("progress");
      this.fuelStorage = compound.getInt("fuel_value");
      this.step = compound.getInt("step");
      this.recipeSpeed = compound.getInt("recipe_speed");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putInt("progress", this.progress);
      compound.putInt("fuel_value", this.fuelStorage);
      compound.putByte("step", (byte)this.step);
      compound.putInt("recipe_speed", this.recipeSpeed);
   }

   @Override
   protected void createInvCaches() {
      this.inOut = new IHasInventory[2];
      this.inOut[0] = new RangedInventory(this, 1, 2);
      this.inOut[1] = new RangedInventory(this, 3, 4).setOutputOnly();
   }

   @Override
   public float getProgress() {
      return (float)this.progress;
   }

   @Override
   public float getMaxProgress() {
      switch (this.getMode((ItemStack)this.inventory.get(1))) {
         case 1:
            return (float)(this.getFoodAmount((ItemStack)this.inventory.get(2)) * 50);
         case 2:
            return 600.0F;
         case 3:
         case 4:
         case 5:
            return 50.0F;
         default:
            return 0.0F;
      }
   }

   @Override
   public void onTick() {
      this.handleRedstone();
      this.handleChargeSlot((int)((double)this.maxEnergy * 0.9));
      int mode = this.getMode((ItemStack)this.inventory.get(1));
      if (this.canOperate(mode) && this.hasEnergy(this.energyConsume)) {
         if (this.canProceedInMode(mode)) {
            this.operate(mode);
            this.notifyListeners();
            this.storage.onRecipeFinished(this.inventory, this);
         } else {
            this.progress = this.progress + this.recipeSpeed;
            this.updateGuiField("progress");
         }

         this.setActive(true);
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
            this.progress--;
            this.updateGuiField("progress");
         }

         this.setActive(false);
      }

      this.storage.onTick(this.inventory, this);
      this.handleComparators();
   }

   public void operate(int mode) {
      switch (mode) {
         case 1:
            ItemStack output = this.getFoodOutput(this.getFoodAmount((ItemStack)this.inventory.get(2)));
            this.setOrGrow(3, output, false);
            ((ItemStack)this.inventory.get(1)).shrink(output.getCount());
            if (((ItemStack)this.inventory.get(2)).hasCraftingRemainingItem()) {
               this.setOrGrow(4, ((ItemStack)this.inventory.get(2)).getCraftingRemainingItem(), false);
            }

            ((ItemStack)this.inventory.get(2)).shrink(1);
            break;
         case 2:
            ICannerRecipeRegistry.FuelValue value = this.getRecipes().getValueForStack((ItemStack)this.inventory.get(2));
            this.fuelStorage = value.apply(this.fuelStorage);
            if (((ItemStack)this.inventory.get(2)).hasCraftingRemainingItem()) {
               this.setOrGrow(4, ((ItemStack)this.inventory.get(2)).getCraftingRemainingItem(), false);
            }

            ((ItemStack)this.inventory.get(2)).shrink(1);
            this.step++;
            if (this.progress < 600) {
               return;
            }

            this.step = 0;
            if (((ItemStack)this.inventory.get(1)).getItem() instanceof IFuelableItem item) {
               this.inventory.set(3, item.fill(((ItemStack)this.inventory.get(1)).copy(), this.fuelStorage));
               ((ItemStack)this.inventory.get(1)).shrink(1);
            }
            break;
         case 3:
            IFoamSupplier supplier = (IFoamSupplier)((ItemStack)this.inventory.get(1)).getItem();
            ((ItemStack)this.inventory.get(2)).shrink(1);
            supplier.fillFoam((ItemStack)this.inventory.get(1), 26);
            if (supplier.getFreeFoamSpace((ItemStack)this.inventory.get(1)) < 26) {
               this.inventory.set(3, (ItemStack)this.inventory.get(1));
               this.inventory.set(1, ItemStack.EMPTY);
            }
            break;
         case 4:
            Tuple<IInput, Integer> entry = this.getRecipes().getRepairableItem((ItemStack)this.inventory.get(1), (ItemStack)this.inventory.get(2), true);
            if (entry != null) {
               if (((ItemStack)this.inventory.get(2)).hasCraftingRemainingItem()) {
                  ItemStack container = ((ItemStack)this.inventory.get(2)).getCraftingRemainingItem();
                  container.setCount(Mth.clamp(((IInput)entry.getA()).getInputSize(), 1, container.getMaxStackSize()));
                  this.inventory.set(4, container);
               }

               if (!((IRepairable)((ItemStack)this.inventory.get(1)).getItem()).repairDamage((ItemStack)this.inventory.get(1), (Integer)entry.getB())) {
                  this.inventory.set(3, (ItemStack)this.inventory.get(1));
                  this.inventory.set(1, ItemStack.EMPTY);
               } else {
                  ((ItemStack)this.inventory.get(2)).shrink(((IInput)entry.getA()).getInputSize());
               }
            }
            break;
         case 5:
            Tuple<IInput, ItemStack> result = IC2.RECIPES.get().canner.getFillOutput((ItemStack)this.inventory.get(1), (ItemStack)this.inventory.get(2), true);
            if (result != null) {
               this.setOrGrow(3, ((ItemStack)result.getB()).copy(), false);
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
            return this.progress >= this.getFoodAmount((ItemStack)this.inventory.get(2)) * 50;
         case 2:
            return this.progress > 0 && this.progress / 100 > this.step;
         case 3:
         case 4:
         case 5:
            return this.progress >= 50;
         default:
            return false;
      }
   }

   public boolean canOperate(int mode) {
      if (!((ItemStack)this.inventory.get(1)).isEmpty() && mode != 0) {
         switch (mode) {
            case 1:
               int foodAmount = this.getFoodAmount((ItemStack)this.inventory.get(2));
               return this.canFoodFitIntoOutput(foodAmount) && this.hasEnoughContainersForFood(this.getFoodOutput(foodAmount).getCount());
            case 2:
               return this.getFuelValue((ItemStack)this.inventory.get(2)) > 0
                  && ((ItemStack)this.inventory.get(3)).isEmpty()
                  && this.canContainerFit((ItemStack)this.inventory.get(2));
            case 3:
               return ((ItemStack)this.inventory.get(2)).getItem() == IC2Items.CF_PELLET
                  && ((IFoamSupplier)((ItemStack)this.inventory.get(1)).getItem()).getFreeFoamSpace((ItemStack)this.inventory.get(1)) >= 26
                  && ((ItemStack)this.inventory.get(3)).isEmpty();
            case 4:
               return ((ItemStack)this.inventory.get(3)).isEmpty()
                  && this.getRecipes().getRepairableItem((ItemStack)this.inventory.get(1), (ItemStack)this.inventory.get(2), true) != null
                  && this.canContainerFit((ItemStack)this.inventory.get(2));
            case 5:
               Tuple<IInput, ItemStack> output = this.getRecipes().getFillOutput((ItemStack)this.inventory.get(1), (ItemStack)this.inventory.get(2), true);
               return output != null
                  && (((ItemStack)this.inventory.get(3)).isEmpty() || StackUtil.canFitInto((ItemStack)this.inventory.get(3), (ItemStack)output.getB()));
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
         ItemStack stored = (ItemStack)this.inventory.get(3);
         return (stored.isEmpty() || StackUtil.canFitInto(stored, stack)) && this.canContainerFit(stored);
      }
   }

   public boolean hasEnoughContainersForFood(int amount) {
      return ((ItemStack)this.inventory.get(1)).getCount() >= amount;
   }

   public boolean canContainerFit(ItemStack input) {
      if (!input.hasCraftingRemainingItem()) {
         return true;
      } else {
         ItemStack container = input.getCraftingRemainingItem();
         ItemStack stored = (ItemStack)this.inventory.get(4);
         return stored.isEmpty() || StackUtil.canFitInto(stored, container);
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
            return this.getFoodAmount(stack) > 0 || this.getFuelValue(stack) > 0 || stack.getItem() == IC2Items.CF_PELLET;
      }
   }

   @Override
   public int getValidRoom(ItemStack stack) {
      ItemStack inv = (ItemStack)this.inventory.get(2);
      if (!inv.isEmpty() && StackUtil.isStackEqual(inv, stack)) {
         StackUtil.getStackSizeLeft(inv);
      }

      switch (this.getMode((ItemStack)this.inventory.get(1))) {
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
            return this.getValidContainer(stack, false);
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
   protected void handleMods() {
      this.recipeSpeed = (int)this.progressPerTick;
   }
}
