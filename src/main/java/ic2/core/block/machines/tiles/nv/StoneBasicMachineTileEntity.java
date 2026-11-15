package ic2.core.block.machines.tiles.nv;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.INullableInput;
import ic2.api.recipes.ingridients.queue.IInputter;
import ic2.api.recipes.ingridients.queue.IStackOutput;
import ic2.api.recipes.ingridients.queue.SimpleStackOutput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.recipes.misc.RecipeFlags;
import ic2.api.recipes.misc.RecipeMods;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.tiles.IRecipeMachine;
import ic2.api.tiles.readers.IFuelStorage;
import ic2.api.tiles.readers.IProgressMachine;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.audio.IAudioSource;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.features.IXPMachine;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.FuelComparator;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.block.machines.containers.nv.BasicStoneMachineContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.FuelFilter;
import ic2.core.inventory.filter.special.MachineFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class StoneBasicMachineTileEntity
   extends BaseInventoryTileEntity
   implements IRecipeMachine,
   IWrenchableTile,
   IFuelStorage,
   IProgressMachine,
   ITickListener,
   IInputter,
   ITileGui,
   ITileActivityProvider,
   IXPMachine {
   @NetworkInfo
   public float fuel = 0.0F;
   @NetworkInfo
   public int maxFuel = 0;
   @NetworkInfo
   public float progress = 0.0F;
   @NetworkInfo
   public float maxProgress;
   @NetworkInfo
   public float fuelConsumption = 1.0F;
   @NetworkInfo
   public boolean isProcessing = false;
   final int defaultMaxProgress;
   public IAudioSource burnSound = null;
   public IAudioSource processSound = null;
   IMachineRecipeList.RecipeEntry lastUsed = null;
   IMachineRecipeList.RecipeEntry lastEntry = null;
   boolean checkRecipe = true;
   Object2IntLinkedOpenHashMap<ResourceLocation> processedRecipes = new Object2IntLinkedOpenHashMap();
   List<IStackOutput> outputs = CollectionUtils.<IStackOutput>createList();

   public StoneBasicMachineTileEntity(BlockPos pos, BlockState state, int maxProgress) {
      super(pos, state, 3);
      this.addGuiFields(new String[]{"fuel", "maxFuel", "progress", "maxProgress"});
      this.addNetworkFields(new String[]{"isProcessing"});
      this.defaultMaxProgress = maxProgress;
      this.maxProgress = (float)maxProgress;
      this.addComparator(new ProgressComparator("progress", ComparatorNames.PROGRESS, this));
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
      this.addComparator(new FuelComparator("fuel", ComparatorNames.FUEL, this));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.registerSlotAccess(AccessRule.IMPORT, 1);
      handler.registerSlotAccess(AccessRule.EXPORT, 2);
      handler.registerSlotsForSide(DirectionList.UP.invert(), 0, 2);
      handler.registerSlotsForSide(DirectionList.DOWN.invert(), 1);
      handler.registerInputFilter(FuelFilter.WITH_LAVA, 0);
      handler.registerOutputFilter(FuelFilter.NOT_WITH_LAVA, 0);
      handler.registerInputFilter(SpecialFilters.ALWAYS_FALSE, 2);
      handler.registerInputFilter(new MachineFilter(this), 1);
      handler.registerNamedSlot(SlotType.FUEL, 0);
      handler.registerNamedSlot(SlotType.INPUT, 1);
      handler.registerNamedSlot(SlotType.OUTPUT, 2);
   }

   public abstract IMachineRecipeList.RecipeEntry getEntry(ItemStack var1);

   public abstract int getFuel(ItemStack var1);

   public int getRawFuel(ItemStack stack) {
      return this.getFuel(stack);
   }

   public abstract ResourceLocation getTexture();

   public boolean allowsLavaFuel() {
      return true;
   }

   protected ResourceLocation getFuelBurnSound() {
      return SoundEvents.FURNACE_FIRE_CRACKLE.getLocation();
   }

   protected ResourceLocation getWorkingSound() {
      return null;
   }

   protected boolean consumeContainers() {
      return false;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.fuel = compound.getFloat("fuel");
      this.maxFuel = compound.getInt("maxFuel");
      this.progress = compound.getFloat("progress");
      this.maxProgress = compound.getFloat("maxProgress");
      this.fuelConsumption = compound.getFloat("fuelUsage");
      this.isProcessing = compound.getBoolean("processing");

      for (CompoundTag nbt : NBTListWrapper.wrap(compound.getList("cache", 10), CompoundTag.class)) {
         IStackOutput output = IngredientRegistry.INSTANCE.readQueue(nbt);
         if (output != null) {
            this.outputs.add(output);
         }
      }
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putFloat("fuel", this.fuel);
      compound.putInt("maxFuel", this.maxFuel);
      compound.putFloat("progress", this.progress);
      compound.putFloat("maxProgress", this.maxProgress);
      compound.putFloat("fuelUsage", this.fuelConsumption);
      compound.putBoolean("processing", this.isProcessing);
      ListTag list = new ListTag();

      for (IStackOutput stack : this.outputs) {
         list.add(IngredientRegistry.INSTANCE.writeQueue(stack));
      }

      compound.put("cache", list);
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("isActive")) {
         if (this.burnSound == null || !this.burnSound.isValid()) {
            this.burnSound = this.getSounds().createSource(this, this.getFuelBurnSound());
         }

         this.playStop(this.burnSound, this.isActive());
      }

      if (fields.contains("isProcessing")) {
         if (this.processSound == null || !this.processSound.isValid()) {
            this.processSound = this.getSounds().createSource(this, this.getWorkingSound());
         }

         this.playStop(this.processSound, this.isProcessing);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      this.burnSound = null;
      this.processSound = null;
      IC2.AUDIO.removeSource(this);
      super.onUnloaded(chunk);
   }

   protected void onRecipeProcessed(ResourceLocation recipe) {
      this.processedRecipes.addTo(recipe, 1);
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
            IMachineRecipeList.RecipeEntry recipe = this.getRecipeList().getRecipe((ResourceLocation)entry.getKey());
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
   public void onTick() {
      boolean hasRoom = this.addItemsToInventory();
      this.lastUsed = this.getRecipe();
      if (this.fuel <= 0.0F && hasRoom && this.lastUsed != null) {
         int newFuel = this.getFuel((ItemStack)this.inventory.get(0));
         if (newFuel > 0) {
            this.fuel = (float)newFuel;
            this.maxFuel = newFuel;
            this.updateGuiField("maxFuel");
            this.updateGuiField("fuel");
            ItemStack fuel = (ItemStack)this.inventory.get(0);
            if (fuel.hasCraftingRemainingItem()) {
               this.inventory.set(0, fuel.getCraftingRemainingItem());
            } else {
               fuel.shrink(1);
            }
         } else if (this.getRawFuel((ItemStack)this.inventory.get(0)) > 0) {
            ItemStack fuel = (ItemStack)this.inventory.get(0);
            if (fuel.hasCraftingRemainingItem()) {
               this.inventory.set(0, fuel.getCraftingRemainingItem());
            } else {
               fuel.shrink(1);
            }
         }
      }

      if (this.fuel > 0.0F) {
         if (hasRoom && this.lastUsed != null) {
            this.fuel = Math.max(0.0F, this.fuel - this.fuelConsumption);
            this.setProcessing(true);
            this.progress++;
            if (this.progress >= this.maxProgress) {
               this.progress = 0.0F;
               this.operate(this.lastUsed);
               this.checkRecipe = true;
            }

            this.updateGuiField("progress");
         } else {
            this.fuel--;
            if (this.progress > 0.0F) {
               this.progress = Math.max(0.0F, this.progress - 1.0F);
               this.updateGuiField("progress");
            }

            this.setProcessing(false);
         }

         this.updateGuiField("fuel");
      } else {
         if (this.progress > 0.0F) {
            this.progress = Math.max(0.0F, this.progress - 1.0F);
            this.updateGuiField("progress");
         }

         this.setProcessing(false);
      }

      if (this.isActive() != this.fuel > 0.0F) {
         this.setActive(this.fuel > 0.0F);
      }

      this.handleComparators();
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      if (this.isSimulating()) {
         this.checkRecipe = true;
         this.onSlotChanged(slot, (ItemStack)this.inventory.get(slot), stack);
      }

      super.setStackInSlot(slot, stack);
   }

   public void operate(IMachineRecipeList.RecipeEntry usedRecipe) {
      CompoundTag recipeData = new CompoundTag();
      this.operateOnce(1, usedRecipe.getInputs(), usedRecipe.getOutput(), recipeData);
      this.onRecipeProcessed(usedRecipe.getLocation());
      if (!this.outputs.isEmpty()) {
         this.addItemsToInventory();
      }
   }

   public void operateOnce(int slot, IInput[] input, IRecipeOutput output, CompoundTag recipeData) {
      for (ItemStack stack : output.onRecipeProcessed(this.getLevel().random, this.getPersistentData(), recipeData)) {
         this.addOutput(slot, stack);
      }

      this.consumeInput(input, slot, RecipeFlags.CONSUME_CONTAINERS.getFlag(output.getMetadata(), false) || this.consumeContainers());
   }

   public void consumeInput(IInput[] inputs, int slot, boolean emptyContainer) {
      IInput input = inputs[0];
      if (!(input instanceof INullableInput) || !((ItemStack)this.inventory.get(slot)).isEmpty()) {
         if (!emptyContainer && ((ItemStack)this.inventory.get(slot)).hasCraftingRemainingItem()) {
            this.addOutput(slot, ((ItemStack)this.inventory.get(slot)).getCraftingRemainingItem());
         }

         ((ItemStack)this.inventory.get(slot)).shrink(input.getInputSize());
      }
   }

   protected void addOutput(int inputSlot, ItemStack output) {
      this.outputs.add(new SimpleStackOutput(output, 2));
   }

   protected void onSlotChanged(int slot, ItemStack oldStack, ItemStack newStack) {
      if (slot == 1 && !StackUtil.isStackEqual(oldStack, newStack)) {
         this.progress = 0.0F;
         this.updateGuiField("progress");
      }
   }

   @Override
   public int getValidRoom(ItemStack stack) {
      if (this.getRecipeList().getRecipe(stack, false) == null) {
         return 0;
      } else if (((ItemStack)this.inventory.get(1)).isEmpty()) {
         return stack.getMaxStackSize();
      } else {
         ItemStack ref = (ItemStack)this.inventory.get(1);
         return StackUtil.isStackEqual(ref, stack) ? StackUtil.getStackSizeLeft(ref) : 0;
      }
   }

   protected IMachineRecipeList.RecipeEntry getRecipe() {
      if (!this.checkRecipe) {
         return this.lastUsed;
      } else {
         this.checkRecipe = false;
         if (((ItemStack)this.inventory.get(1)).isEmpty()) {
            this.lastEntry = null;
            return null;
         } else {
            if (this.lastEntry != null) {
               IInput recipe = this.lastEntry.getInputs()[0];
               if (recipe instanceof INullableInput) {
                  if (!recipe.matches((ItemStack)this.inventory.get(1))) {
                     this.lastEntry = null;
                  }
               } else if (!((ItemStack)this.inventory.get(1)).isEmpty() && recipe.matches((ItemStack)this.inventory.get(1))) {
                  if (recipe.getInputSize() > ((ItemStack)this.inventory.get(1)).getCount()) {
                     return null;
                  }

                  InteractionResult result = this.isRecipeStillValid(this.lastEntry);
                  if (result == InteractionResult.FAIL) {
                     this.lastEntry = null;
                  } else if (result == InteractionResult.PASS) {
                     return null;
                  }
               } else {
                  this.lastEntry = null;
               }
            }

            if (this.lastEntry == null) {
               IMachineRecipeList.RecipeEntry out = this.getEntry(((ItemStack)this.inventory.get(1)).copy());
               if (out == null) {
                  return null;
               }

               this.lastEntry = out;
               this.handleMods(out);
            }

            InteractionResult result = this.canFillRecipeIntoOutputs(this.lastEntry.getOutput());
            if (result == InteractionResult.SUCCESS) {
               return this.lastEntry;
            } else if (result == InteractionResult.PASS) {
               return null;
            } else {
               ItemStack out = (ItemStack)this.inventory.get(2);
               if (out.isEmpty()) {
                  return this.lastEntry;
               } else if (StackUtil.getStackSizeLeft(out) <= 0) {
                  return null;
               } else {
                  for (ItemStack output : this.lastEntry.getOutput().getAllOutputs()) {
                     if (StackUtil.canFitInto(out, output)) {
                        return this.lastEntry;
                     }
                  }

                  return null;
               }
            }
         }
      }
   }

   protected InteractionResult isRecipeStillValid(IMachineRecipeList.RecipeEntry entry) {
      return InteractionResult.SUCCESS;
   }

   protected InteractionResult canFillRecipeIntoOutputs(IRecipeOutput output) {
      return InteractionResult.FAIL;
   }

   public void handleMods(IMachineRecipeList.RecipeEntry entry) {
      this.updateGuiField("fuelConsumption");
      this.updateGuiField("maxProgress");
      if (entry == null) {
         this.fuelConsumption = 1.0F;
         this.maxProgress = (float)this.defaultMaxProgress;
      } else {
         CompoundTag nbt = entry.getOutput().getMetadata();
         this.fuelConsumption = (float)RecipeMods.ENERGY_USAGE.apply(nbt, 1);
         this.maxProgress = (float)RecipeMods.RECIPE_TIME.apply(nbt, this.defaultMaxProgress);
      }
   }

   protected boolean addItemsToInventory() {
      if (this.outputs.isEmpty()) {
         return true;
      } else {
         this.outputs.removeIf(iStackOutput -> iStackOutput.addToInventory(this));
         return this.outputs.isEmpty();
      }
   }

   @Override
   public void addItemIntoSlot(int slot, ItemStack stack) {
      ItemStack inv = (ItemStack)this.inventory.get(slot);
      if (inv.isEmpty()) {
         if (stack.getCount() > stack.getMaxStackSize()) {
            this.inventory.set(slot, StackUtil.copyWithSize(stack, stack.getMaxStackSize()));
            stack.shrink(stack.getMaxStackSize());
         } else {
            this.inventory.set(slot, stack.copy());
            stack.setCount(0);
         }
      } else {
         if (!StackUtil.isStackEqual(inv, stack)) {
            return;
         }

         int left = StackUtil.getStackSizeLeft(inv);
         if (left > 0) {
            if (left >= stack.getCount()) {
               inv.grow(stack.getCount());
               stack.setCount(0);
            } else {
               stack.shrink(left);
               inv.setCount(inv.getMaxStackSize());
            }
         }
      }
   }

   @Override
   public float getProgress() {
      return this.progress;
   }

   @Override
   public float getMaxProgress() {
      return this.maxProgress;
   }

   @Override
   public int getFuel() {
      return (int)this.fuel;
   }

   @Override
   public int getMaxFuel() {
      return this.maxFuel;
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

   protected void setProcessing(boolean newValue) {
      if (this.isProcessing != newValue) {
         this.isProcessing = newValue;
         this.updateTileField("isProcessing");
      }
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new BasicStoneMachineContainer(this, player, windowID);
   }
}
