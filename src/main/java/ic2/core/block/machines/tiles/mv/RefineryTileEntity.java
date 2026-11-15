package ic2.core.block.machines.tiles.mv;

import ic2.api.items.IUpgradeItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.generators.ItemGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.INullableInput;
import ic2.api.recipes.ingridients.inputs.SubItemInput;
import ic2.api.recipes.ingridients.queue.MultiStackOutput;
import ic2.api.recipes.ingridients.recipes.IFluidRecipeOutput;
import ic2.api.recipes.misc.RecipeFlags;
import ic2.api.recipes.misc.RecipeMods;
import ic2.api.recipes.registries.IRefiningRecipeList;
import ic2.api.tiles.IFluidMachine;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.cache.CapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.IXPMachine;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.tiles.impls.machine.single.BaseMachineTileEntity;
import ic2.core.block.machines.containers.mv.RefineryContainer;
import ic2.core.block.machines.recipes.misc.AlcoholInput;
import ic2.core.block.machines.recipes.misc.AlcoholOutput;
import ic2.core.block.machines.recipes.misc.WhiskyOutput;
import ic2.core.fluid.InsertionTank;
import ic2.core.fluid.LayeredFluidTank;
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
import ic2.core.platform.registries.IC2Fluids;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.FluidHelper;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.Tags.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class RefineryTileEntity extends BaseMachineTileEntity implements ITileGui, ITickListener, IFluidHandler, IFluidMachine, IXPMachine, IClickable {
   public static final EnumSet<IUpgradeItem.UpgradeType> BASIC_MACHINE_UPGRADES = EnumSet.allOf(IUpgradeItem.UpgradeType.class);
   @NetworkInfo
   public InsertionTank firstTank = new InsertionTank(16000);
   @NetworkInfo
   public InsertionTank secondTank = new InsertionTank(16000);
   @NetworkInfo
   public LayeredFluidTank output = new LayeredFluidTank(64000);
   @NetworkInfo
   public float progress = 0.0F;
   @NetworkInfo
   public int recipeOperation;
   @NetworkInfo
   public int recipeEnergy;
   protected IRefiningRecipeList.FluidRecipe usedRecipe = null;
   protected IRefiningRecipeList.FluidRecipe lastRecipe = null;
   protected boolean rebuildCache = true;
   List<FluidStack> fluidOutputs = CollectionUtils.<FluidStack>createList();
   ICache<IFluidHandler> fluidCache = new CapabilityCache<>(this, DirectionList.ALL, ForgeCapabilities.FLUID_HANDLER);

   public RefineryTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 5, 4, 20, 250, 5624, 128);
      this.addCaches(new ICache[]{this.fluidCache});
      this.setFuelSlot(0);
      this.addGuiFields(new String[]{"recipeOperation", "recipeEnergy", "progress", "firstTank", "secondTank", "output"});
      this.firstTank.addListener(T -> this.updateGuiField("firstTank")).addListener(T -> this.revalidateRecipe());
      this.secondTank.addListener(T -> this.updateGuiField("secondTank")).addListener(T -> this.revalidateRecipe());
      this.output.addListener(T -> this.updateGuiField("output"));
      this.output.addListener(T -> this.revalidateRecipe());
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, this);
      this.addComparator(new ProgressComparator("progress", ComparatorNames.PROGRESS, this));
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.registerSlotAccess(AccessRule.IMPORT, 1);
      handler.registerSlotAccess(AccessRule.EXPORT, 2, 3, 4);
      handler.registerSlotsForSide(DirectionList.DOWN.invert(), 1);
      handler.registerSlotsForSide(DirectionList.UP.invert(), 0, 2, 3, 4);
      handler.registerInputFilter(SpecialFilters.createChargeFilter(), 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_DISCHARGE_FILTER, 0);
      handler.registerInputFilter(new MachineFilter(this), 1);
      handler.registerInputFilter(SpecialFilters.ALWAYS_FALSE, 2, 3, 4);
      handler.registerNamedSlot(SlotType.BATTERY, 0);
      handler.registerNamedSlot(SlotType.INPUT, 1);
      handler.registerNamedSlot(SlotType.OUTPUT, 2, 3, 4);
   }

   protected void revalidateRecipe() {
      this.addToTick();
      this.rebuildCache = this.rebuildCache | !this.isOperating();
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.progress = compound.getFloat("progress");
      this.firstTank.readFromNBT(compound.getCompound("firstTank"));
      this.secondTank.readFromNBT(compound.getCompound("secondTank"));
      this.output.read(compound.getCompound("outputTank"));
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putFloat("progress", this.progress);
      compound.put("firstTank", this.firstTank.writeToNBT(new CompoundTag()));
      compound.put("secondTank", this.secondTank.writeToNBT(new CompoundTag()));
      compound.put("outputTank", this.output.write(new CompoundTag()));
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.REFINERY;
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      return stack.isEmpty() ? false : FluidHelper.fillContainers(stack, player, this) || FluidHelper.drainContainers(stack, player, this);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new RefineryContainer(this, player, windowID);
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         this.firstTank.setFluid(FluidStack.EMPTY);
         this.updateGuiField("firstTank");
      } else if (key == 1) {
         this.secondTank.setFluid(FluidStack.EMPTY);
         this.updateGuiField("secondTank");
      } else if (key == 2) {
         FluidStack stack = this.output.getFluidInTank(0);
         if (stack.isEmpty()) {
            return;
         }

         int filled = this.firstTank.fill(stack, FluidAction.EXECUTE);
         if (filled > 0) {
            this.output.drain(new FluidStack(stack, filled), FluidAction.EXECUTE);
            this.updateGuiFields(new String[]{"output", "firstTank"});
         } else if ((filled = this.secondTank.fill(stack, FluidAction.EXECUTE)) > 0) {
            this.output.drain(new FluidStack(stack, filled), FluidAction.EXECUTE);
            this.updateGuiFields(new String[]{"output", "secondTank"});
         }
      }
   }

   @Override
   public float getProgress() {
      return this.progress;
   }

   @Override
   public float getMaxProgress() {
      return (float)this.recipeOperation;
   }

   @Override
   protected void createInvCaches() {
      this.inOut = new IHasInventory[2];
      this.inOut[0] = new RangedInventory(this, 1);
      this.inOut[1] = new RangedInventory(this, 2, 3, 4).setOutputOnly();
   }

   @Override
   public int getCreatedXP(boolean consume) {
      if (this.processedRecipes.isEmpty()) {
         return 0;
      } else {
         float totalXP = 0.0F;
         ObjectIterator var3 = Object2IntMaps.fastIterable(this.processedRecipes).iterator();

         while (var3.hasNext()) {
            Entry<ResourceLocation> entry = (Entry<ResourceLocation>)var3.next();
            IRefiningRecipeList.FluidRecipe recipe = this.getRecipeList().getRecipe((ResourceLocation)entry.getKey());
            if (recipe != null) {
               totalXP += recipe.getOutput().getExperience() * (float)entry.getIntValue();
            }
         }

         if (consume) {
            this.processedRecipes.clear();
         }

         return (int)totalXP;
      }
   }

   @Override
   protected boolean addItemsToInventory() {
      if (!this.fluidOutputs.isEmpty()) {
         Iterator<FluidStack> iter = this.fluidOutputs.iterator();

         while (iter.hasNext()) {
            FluidStack out = iter.next();
            out.shrink(this.output.fill(out, FluidAction.EXECUTE));
            if (out.getAmount() <= 0) {
               iter.remove();
            }
         }
      }

      return this.fluidOutputs.isEmpty() && super.addItemsToInventory();
   }

   public IRefiningRecipeList getRecipeList() {
      return IC2.RECIPES.get(this.isSimulating()).refining;
   }

   @Override
   public void onTick() {
      this.handleRedstone();
      boolean charging = this.handleChargeSlot((int)((double)this.maxEnergy * 0.9));
      boolean hasRecipe = this.hasRecipe();
      this.rebuildCache = false;
      boolean canWork = this.canProcess() || hasRecipe && this.hasEnergy(1);
      boolean canOperate = this.addItemsToInventory() && canWork && hasRecipe;
      if (canOperate && this.hasEnergy(this.recipeEnergy)) {
         this.setOperating(true);
         this.useEnergy(this.recipeEnergy);
         this.progress = this.progress + this.progressPerTick;
         if (this.progress >= (float)this.recipeOperation) {
            this.operate();
            this.progress = 0.0F;
            this.notifyListeners();
            this.rebuildCache = true;
         }

         this.updateGuiField("progress");
      } else {
         this.setOperating(false);
         if (this.progress > 0.0F) {
            this.progress = Math.max(0.0F, hasRecipe ? this.progress - 1.0F : 0.0F);
            this.updateGuiField("progress");
         }

         if (this.canStopTicking(charging)) {
            this.removeFromTick();
         }
      }

      this.storage.onTick(this.inventory, this);
      this.handleComparators();
   }

   public void operate() {
      CompoundTag recipeData = new CompoundTag();
      this.storage.onRecipeFinishedPre(this.inventory, this, this.usedRecipe.getOutput(), recipeData);
      this.operateOnce(
         1, this.usedRecipe.getItemInput(), this.usedRecipe.getFirstTank(), this.usedRecipe.getSecondTank(), this.usedRecipe.getOutput(), recipeData
      );
      this.onRecipeProcessed(this.usedRecipe.getLocation());
      this.storage.onRecipeFinishedPost(this.inventory, this, null, this.outputs);
      if (!this.outputs.isEmpty() || !this.fluidOutputs.isEmpty()) {
         this.addItemsToInventory();
      }
   }

   public void operateOnce(int slot, IInput input, FluidStack first, FluidStack second, IFluidRecipeOutput output, CompoundTag recipeData) {
      for (ItemStack stack : output.onRecipeProcessed(this.getLevel().random, this.getPersistentData(), recipeData)) {
         this.addOutput(slot, stack);
      }

      this.fluidOutputs
         .addAll(output.onFluidRecipeProcessed(this.getLevel().random, this.getPersistentData(), recipeData, ((ItemStack)this.inventory.get(slot)).copy()));
      this.consumeInput(input, first, second, slot, RecipeFlags.CONSUME_CONTAINERS.getFlag(output.getMetadata(), false));
   }

   public void consumeInput(IInput input, FluidStack first, FluidStack second, int slot, boolean emptyContainer) {
      this.firstTank.drainInternally(first.getAmount(), FluidAction.EXECUTE);
      this.secondTank.drainInternally(second.getAmount(), FluidAction.EXECUTE);
      if (!(input instanceof INullableInput) || !((ItemStack)this.inventory.get(slot)).isEmpty()) {
         if (!emptyContainer && ((ItemStack)this.inventory.get(slot)).hasCraftingRemainingItem()) {
            this.addOutput(slot, ((ItemStack)this.inventory.get(slot)).getCraftingRemainingItem());
         }

         ((ItemStack)this.inventory.get(slot)).shrink(input.getInputSize());
      }
   }

   protected void addOutput(int inputSlot, ItemStack output) {
      this.outputs.add(new MultiStackOutput(output, 2, 3, 4));
   }

   public boolean hasRecipe() {
      return (this.usedRecipe = this.getRecipe()) != null;
   }

   public IRefiningRecipeList.FluidRecipe getRecipe() {
      if (!this.rebuildCache) {
         return this.usedRecipe;
      } else {
         IRefiningRecipeList.FluidRecipe last = this.lastRecipe;
         if (last != null) {
            IInput input = last.getItemInput();
            if (!((ItemStack)this.inventory.get(1)).isEmpty()
               && input.matches((ItemStack)this.inventory.get(1))
               && this.firstTank.getFluid().isFluidEqual(last.getFirstTank())
               && this.secondTank.getFluid().isFluidEqual(last.getSecondTank())) {
               if (input.getInputSize() > ((ItemStack)this.inventory.get(1)).getCount()
                  || !this.firstTank.getFluid().containsFluid(last.getFirstTank())
                  || !this.secondTank.getFluid().containsFluid(last.getSecondTank())) {
                  return null;
               }
            } else {
               last = null;
            }
         }

         this.lastRecipe = last;
         if (last == null) {
            IRefiningRecipeList.FluidRecipe recipe = this.getRecipeList()
               .getRecipe((ItemStack)this.inventory.get(1), this.firstTank.getFluid(), this.secondTank.getFluid(), true);
            if (recipe == null) {
               return null;
            }

            last = recipe;
            this.lastRecipe = recipe;
            this.handleMods(recipe);
         }

         List<ItemStack> outputs = last.getOutput().getAllOutputs();
         return (this.fitIntoOutput(2, outputs) || this.fitIntoOutput(3, outputs) || this.fitIntoOutput(4, outputs))
               && this.fitIntoOutputs(last.getOutput().getAllFluidOutputs())
            ? last
            : null;
      }
   }

   public boolean fitIntoOutputs(List<FluidStack> fluids) {
      if (fluids.isEmpty()) {
         return true;
      } else {
         for (FluidStack fluid : fluids) {
            if (this.output.fill(fluid, FluidAction.SIMULATE) >= fluid.getAmount()) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean fitIntoOutput(int slot, List<ItemStack> items) {
      if (items.isEmpty()) {
         return true;
      } else {
         ItemStack inv = (ItemStack)this.inventory.get(slot);
         if (inv.isEmpty()) {
            return true;
         } else if (StackUtil.getStackSizeLeft(inv) <= 0) {
            return false;
         } else {
            for (ItemStack stack : items) {
               if (StackUtil.canFitInto(inv, stack)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   @Override
   public void onNotify(IHasInventory inventory, int slot) {
      if (this.isSimulating()) {
         this.addToTick();
         this.rebuildCache = true;
      }
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      if (this.isSimulating()) {
         this.addToTick();
         this.rebuildCache = true;
         this.onSlotChanged(slot, (ItemStack)this.inventory.get(slot), stack);
      }

      super.setStackInSlot(slot, stack);
   }

   @Override
   protected void handleMods() {
      this.handleMods(this.usedRecipe);
   }

   @Override
   public int getValidRoom(ItemStack stack) {
      if (this.getRecipeList().getRecipe(stack, this.firstTank.getFluid(), this.secondTank.getFluid(), false) == null) {
         return 0;
      } else {
         ItemStack ref = (ItemStack)this.inventory.get(1);
         if (ref.isEmpty()) {
            return stack.getMaxStackSize();
         } else {
            return StackUtil.isStackEqual(ref, stack) ? StackUtil.getStackSizeLeft(ref) : 0;
         }
      }
   }

   @Override
   public EnumSet<IUpgradeItem.UpgradeType> getSupportedUpgradeTypes() {
      return BASIC_MACHINE_UPGRADES;
   }

   @Override
   public IFluidHandler getConnectedTank(Direction dir) {
      return this.fluidCache.getHandler(dir);
   }

   public int getTanks() {
      return 2 + this.output.getTanks();
   }

   public FluidStack getFluidInTank(int tank) {
      switch (tank) {
         case 0:
            return this.firstTank.getFluid();
         case 1:
            return this.secondTank.getFluid();
         default:
            return this.output.getFluidInTank(tank - 2);
      }
   }

   public int getTankCapacity(int tank) {
      switch (tank) {
         case 0:
            return this.firstTank.getTankCapacity(0);
         case 1:
            return this.secondTank.getTankCapacity(0);
         default:
            return this.output.getTankCapacity(tank - 2);
      }
   }

   public boolean isFluidValid(int tank, FluidStack stack) {
      return tank == 0 || tank == 1;
   }

   public int fill(FluidStack resource, FluidAction action) {
      boolean first = this.firstTank.getFluid().isFluidEqual(resource);
      boolean second = this.secondTank.getFluid().isFluidEqual(resource);
      if (first && !second) {
         return this.firstTank.fill(resource, action);
      } else if (!first && second) {
         return this.secondTank.fill(resource, action);
      } else if (!first) {
         int fill = this.firstTank.fill(resource, action);
         return fill != 0 ? fill : this.secondTank.fill(resource, action);
      } else {
         return 0;
      }
   }

   public FluidStack drain(FluidStack resource, FluidAction action) {
      return this.output.drain(resource, action);
   }

   public FluidStack drain(int maxDrain, FluidAction action) {
      return this.output.drain(maxDrain, action);
   }

   protected void onSlotChanged(int slot, ItemStack oldStack, ItemStack newStack) {
      if (slot == 1 && !StackUtil.isStackEqual(oldStack, newStack)) {
         if (this.progress > 0.0F) {
            this.playSound(true);
         }

         this.progress = 0.0F;
         this.updateGuiField("progress");
      }
   }

   protected boolean canStopTicking(boolean charging) {
      return !charging && this.progress <= 0.0F && !this.storage.has(IUpgradeItem.Functions.TICK);
   }

   protected void setOperating(boolean value) {
      if (this.setActive(value) && !value && !((ItemStack)this.inventory.get(1)).isEmpty() && this.progress > 0.0F) {
         this.playSound(true);
      }
   }

   protected boolean isOperating() {
      return this.isActive();
   }

   protected void handleMods(IRefiningRecipeList.FluidRecipe entry) {
      this.updateGuiFields(new String[]{"recipeEnergy", "recipeOperation"});
      if (entry == null) {
         this.recipeEnergy = Math.max(1, this.energyConsume);
         this.recipeOperation = Math.max(1, this.operationLength);
      } else {
         CompoundTag nbt = entry.getOutput().getMetadata();
         this.recipeEnergy = RecipeMods.ENERGY_USAGE.apply(nbt, this.energyConsume);
         this.recipeOperation = RecipeMods.RECIPE_TIME.apply(nbt, this.operationLength);
      }
   }

   public static void loadRecipes(IRefiningRecipeList recipes) {
      createOreBuilder(recipes, "iron_to_raw", Items.ORES_IRON, net.minecraft.world.item.Items.RAW_IRON, 2);
      createOreBuilder(recipes, "gold_to_raw", Items.ORES_GOLD, net.minecraft.world.item.Items.RAW_GOLD, 1);
      createOreBuilder(recipes, "copper_to_raw", IC2Tags.ORE_COPPER, net.minecraft.world.item.Items.RAW_COPPER, 3);
      createOreBuilder(recipes, "tin_to_raw", IC2Tags.ORE_TIN, IC2Items.RAW_TIN, 3);
      createOreBuilder(recipes, "silver_to_raw", IC2Tags.ORE_SILVER, IC2Items.RAW_SILVER, 1);
      createOreBuilder(recipes, "aluminum_to_raw", IC2Tags.ORE_ALUMINIUM, IC2Items.RAW_ALUMINIUM, 15);
      recipes.addIC2Recipe("polished_uranium")
         .mainInput(IC2Fluids.ALCOHOL, 100)
         .catalyst(IC2Tags.ORE_URANIUM)
         .buildSimple(new ItemGenerator(IC2Items.ORE_URANIUM_DROP, 5));
      recipes.addIC2Recipe("enriched_lava")
         .mainInput(Fluids.LAVA, 16000)
         .catalyst(new ItemStack(net.minecraft.world.item.Items.BLAZE_POWDER, 5))
         .addMod(RecipeMods.ENERGY_USAGE, 8.0)
         .addMod(RecipeMods.RECIPE_TIME, 2.0)
         .buildSimple(new FluidStack(IC2Fluids.BLAZING_LAVA, 16000));
      recipes.addIC2Recipe("polished_gold")
         .mainInput(IC2Fluids.ALCOHOL, 500)
         .catalyst(net.minecraft.world.item.Items.GOLD_INGOT)
         .buildSimple(new ItemGenerator(IC2Items.INGOT_POLISHED_GOLD));
      recipes.addIC2Recipe("bio_gas_fuel")
         .mainInput(IC2Fluids.WOOD_GAS, 1000)
         .secondInput(Fluids.WATER, 1000)
         .addMod(RecipeMods.RECIPE_TIME, 2.0)
         .buildSimple(new FluidStack(IC2Fluids.BIO_FUEL, 100));
      recipes.addIC2Recipe("bio_fuel")
         .catalyst(new ItemStack(IC2Items.PLANT_BALL_COMPRESSED, 2))
         .mainInput(Fluids.WATER, 2000)
         .buildSimple(new FluidStack(IC2Fluids.BIO_FUEL, 500));
      recipes.addIC2Recipe("alcohol_mug")
         .mainInput(Fluids.WATER, 1000)
         .catalyst(new AlcoholInput(IC2Items.MUG_BEER))
         .buildCustom(T -> new AlcoholOutput(IC2Items.MUG));
      recipes.addIC2Recipe("alcohol_glass")
         .mainInput(Fluids.WATER, 1000)
         .catalyst(new AlcoholInput(IC2Items.GLASS_BEER))
         .buildCustom(T -> new AlcoholOutput(IC2Items.GLASS));
      recipes.addIC2Recipe("whisky_mug")
         .mainInput(Fluids.WATER, 4000)
         .catalyst(new SubItemInput(IC2Items.MUG_WHISKY))
         .buildCustom(T -> new WhiskyOutput(IC2Items.MUG));
      recipes.addIC2Recipe("whisky_glass")
         .mainInput(Fluids.WATER, 4000)
         .catalyst(new SubItemInput(IC2Items.GLASS_WHISKY))
         .buildCustom(T -> new WhiskyOutput(IC2Items.GLASS));
      recipes.addIC2Recipe("unrefined_whisky")
         .mainInput(Fluids.WATER, 1000)
         .catalyst(new ItemStack(IC2Items.FLOUR, 4))
         .addMod(RecipeMods.RECIPE_TIME, 0.5)
         .buildSimple(new FluidStack(IC2Fluids.WHISKY, 250));
      recipes.addIC2Recipe("refined_whisky").mainInput(IC2Fluids.WHISKY, 1000).buildSimple(new ItemGenerator(IC2Items.WHISKY_GRIST));
      recipes.addIC2Recipe("coal_dust_washing")
         .addMod(RecipeMods.RECIPE_TIME, 0.1)
         .mainInput(Fluids.WATER, 100)
         .catalyst(IC2Items.DUST_COAL)
         .buildSimple(new ItemGenerator(IC2Items.DUST_HYDRATED_COAL));
      recipes.addIC2Recipe("charcoal_dust_washing")
         .addMod(RecipeMods.RECIPE_TIME, 0.1)
         .mainInput(Fluids.WATER, 100)
         .catalyst(IC2Items.DUST_CHARCOAL)
         .buildSimple(new ItemGenerator(IC2Items.DUST_HYDRATED_CHARCOAL));
      recipes.addIC2Recipe("mud_to_clay")
         .catalyst(net.minecraft.world.item.Items.MUD)
         .buildSimple(
            ObjectArrayList.of(
               new IOutputGenerator[]{new ItemGenerator(net.minecraft.world.item.Items.DIRT), new ItemGenerator(net.minecraft.world.item.Items.CLAY_BALL)}
            ),
            ObjectLists.singleton(new FluidStack(Fluids.WATER, 500))
         );
      recipes.addIC2Recipe("chocolate")
         .catalyst(new ItemStack(net.minecraft.world.item.Items.COCOA_BEANS, 8))
         .mainInput(ForgeMod.MILK, 250)
         .buildSimple(new ItemGenerator(IC2Items.CHOCOLATE));
   }

   private static void createOreBuilder(IRefiningRecipeList recipe, String id, TagKey<Item> ore, Item output, int base) {
      recipe.addIC2Recipe(id)
         .addMod(RecipeMods.RECIPE_TIME, 0.5)
         .catalyst(ore)
         .mainInput(Fluids.LAVA, 25 * base)
         .buildRange(new ItemGenerator(output), base, base * 3);
      recipe.addIC2Recipe(id + "_advanced")
         .addMod(RecipeMods.RECIPE_TIME, 0.5)
         .catalyst(ore)
         .mainInput(IC2Fluids.BIO_FUEL, 50 * base)
         .buildRange(new ItemGenerator(output), base * 3, base * 5);
      recipe.addIC2Recipe(id + "_alk")
         .addMod(RecipeMods.RECIPE_TIME, 0.5)
         .catalyst(ore)
         .mainInput(IC2Fluids.ALCOHOL, 25 * base)
         .buildRange(new ItemGenerator(output), base * 5, base * 7);
   }
}
