package ic2.core.block.base.tiles.impls.machine.multi;

import ic2.api.items.IUpgradeItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.INullableInput;
import ic2.api.recipes.ingridients.queue.MultiStackOutput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.recipes.misc.RecipeFlags;
import ic2.api.recipes.misc.RecipeMods;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.tiles.IRecipeMachine;
import ic2.api.tiles.readers.IProgressMachine;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.block.base.features.IXPMachine;
import ic2.core.block.base.features.multiblock.IMultiBlockClickable;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.misc.comparator.types.base.TankComparator;
import ic2.core.block.machines.containers.ev.ColossalMachineContainer;
import ic2.core.fluid.ArrayFluidHandler;
import ic2.core.fluid.ExtractionTank;
import ic2.core.fluid.InsertionTank;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.inv.RangedInventory;
import ic2.core.platform.registries.IC2Fluids;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public abstract class BaseColossalMachineTileEntity extends BaseMultiMachineTileEntity implements IRecipeMachine, ITileGui, IMultiBlockClickable, IXPMachine {
   public static final EnumSet<IUpgradeItem.UpgradeType> TYPES = EnumSet.complementOf(EnumSet.of(IUpgradeItem.UpgradeType.RECIPE_MOD));
   @NetworkInfo
   public InsertionTank waterTank = new InsertionTank(16000, T -> T.getFluid() == Fluids.WATER);
   @NetworkInfo
   public ExtractionTank steamTank = new ExtractionTank(16000);
   @NetworkInfo
   protected int slotsInUse;
   @NetworkInfo
   public float[] progress;
   @NetworkInfo
   public int[] recipeEnergy;
   @NetworkInfo
   public int[] recipeOperation;
   protected IMachineRecipeList.RecipeEntry[] activeRecipe;
   protected IMachineRecipeList.RecipeEntry[] usedRecipe;
   protected int dirtyRecipes = 0;
   protected int activeRecipeProgress = 0;
   protected IntSet activeRecipes = new IntLinkedOpenHashSet();

   public BaseColossalMachineTileEntity(
      BlockPos pos, BlockState state, int size, int maxRecipeSlots, int energyPerTick, int maxProgress, int maxEnergy, int maxInput
   ) {
      this(pos, state, size, 2, maxRecipeSlots, energyPerTick, maxProgress, maxEnergy, maxInput);
   }

   public BaseColossalMachineTileEntity(
      BlockPos pos, BlockState state, int size, int upgradeSlots, int maxRecipeSlots, int energyPerTick, int maxProgress, int maxEnergy, int maxInput
   ) {
      super(pos, state, size, upgradeSlots, energyPerTick, maxProgress, maxEnergy, maxInput);
      this.progress = new float[maxRecipeSlots];
      this.recipeEnergy = new int[maxRecipeSlots];
      this.recipeOperation = new int[maxRecipeSlots];
      this.activeRecipe = new IMachineRecipeList.RecipeEntry[maxRecipeSlots];
      this.usedRecipe = new IMachineRecipeList.RecipeEntry[maxRecipeSlots];
      Arrays.fill(this.recipeEnergy, energyPerTick);
      Arrays.fill(this.recipeOperation, maxProgress);
      this.slotsInUse = maxRecipeSlots;
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, new ArrayFluidHandler(this.waterTank, this.steamTank));
      this.addGuiFields(new String[]{"waterTank", "steamTank", "progress", "recipeEnergy", "recipeOperation"});
      this.addNetworkFields(new String[]{"slotsInUse"});
      this.waterTank.addListener(this::checkTank).addListener(T -> this.updateGuiField("waterTank"));
      this.steamTank.addListener(this::checkTank).addListener(T -> this.updateGuiField("steamTank"));
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
      this.addComparator(new TankComparator("water_tank", ComparatorNames.WATER_TANK, this.waterTank));
      this.addComparator(new TankComparator("steam_tank", ComparatorNames.STEAM_TANK, this.steamTank));

      for (int i = 0; i < maxRecipeSlots; i++) {
         this.addComparator(
            new ProgressComparator(
                  "progress_" + i, this.translate("comparator.ic2.multiprogress", new Object[]{i}), new BaseColossalMachineTileEntity.MachineProgress(this, i)
               )
               .setVisibleHandler(new BaseColossalMachineTileEntity.MachineVisibility(this, i))
         );
      }
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      int[] input = this.getAllSlots(true);
      int[] output = this.getAllSlots(false);
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.IMPORT, input);
      handler.registerSlotAccess(AccessRule.EXPORT, output);
      handler.registerSlotsForSide(DirectionList.DOWN.invert(), input);
      handler.registerSlotsForSide(DirectionList.UP.invert(), output);
      handler.registerInputFilter(this::canInsertInSlot, input);
      handler.registerNamedSlot(SlotType.INPUT, input);
      handler.registerNamedSlot(SlotType.OUTPUT, output);
   }

   public abstract ResourceLocation getGuiTexture();

   public abstract int getStructureSize();

   public abstract int getRecipeSlots(int var1, boolean var2);

   public abstract int getInputSlot(int var1);

   public abstract int[] getOutputSlots(int var1);

   public abstract int[] getAllSlots(boolean var1);

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      ListTag list = new ListTag();

      for (int i = 0; i < this.progress.length; i++) {
         if (this.progress[i] > 0.0F) {
            CompoundTag data = new CompoundTag();
            data.putFloat("progress", this.progress[i]);
            data.putByte("slot", (byte)i);
            list.add(data);
         }
      }

      NBTUtils.put(compound, "progress", list);
      NBTUtils.putSortedIntArray(compound, "activeSlots", this.activeRecipes.toIntArray(), MathUtils.fromTo(0, this.slotsInUse));
      NBTUtils.putByte(compound, "slotsInUse", this.slotsInUse, this.progress.length);
      NBTUtils.put(compound, "steamTank", this.steamTank.writeToNBT(new CompoundTag()));
      NBTUtils.put(compound, "waterTank", this.waterTank.writeToNBT(new CompoundTag()));
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);

      for (CompoundTag nbt : NBTListWrapper.wrap(compound.getList("progress", 10), CompoundTag.class)) {
         int slot = nbt.getInt("slot");
         if (slot >= 0 && slot < this.progress.length) {
            this.progress[slot] = nbt.getFloat("progress");
            this.dirtyRecipes |= 1 << slot;
         }
      }

      this.activeRecipes.addAll(IntArrayList.wrap(NBTUtils.getIntArray(compound, "activeSlots", MathUtils.fromTo(0, this.slotsInUse))));
      this.slotsInUse = NBTUtils.getInt(compound, "slotsInUse", this.progress.length);
      this.steamTank.readFromNBT(compound.getCompound("steamTank"));
      this.waterTank.readFromNBT(compound.getCompound("waterTank"));
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ColossalMachineContainer(this, player, windowID);
   }

   @Override
   public EnumSet<IUpgradeItem.UpgradeType> getSupportedUpgradeTypes() {
      return TYPES;
   }

   public int getSlotsInUse() {
      return this.slotsInUse;
   }

   @Override
   public int getEnergyPerTick() {
      int energyUsage = 0;
      IntIterator iter = this.activeRecipes.iterator();

      while (iter.hasNext()) {
         energyUsage += this.recipeEnergy[iter.nextInt()];
      }

      return energyUsage;
   }

   public IntIterator getActiveSlots() {
      return IntIterators.unmodifiable(this.activeRecipes.iterator());
   }

   public float getProgress(int slot) {
      return this.progress[slot];
   }

   public float getMaxProgress(int slot) {
      return (float)this.recipeOperation[slot];
   }

   protected void setSlotsInUse(int size) {
      if (size >= 0 && size < this.recipeEnergy.length) {
         if (size < this.slotsInUse) {
            for (int i = size; i < this.slotsInUse; i++) {
               this.dropSlot(i);
            }
         }

         this.slotsInUse = size;
         this.inOut = null;
         this.updateTileField("slotsInUse");
      }
   }

   protected void dropSlot(int recipeSlot) {
      int slot = this.getInputSlot(recipeSlot);
      ItemStack stack = (ItemStack)this.inventory.get(slot);
      this.inventory.set(slot, ItemStack.EMPTY);
      if (!stack.isEmpty()) {
         Block.popResource(this.getLevel(), this.worldPosition, stack);
      }

      for (int subSlot : this.getOutputSlots(recipeSlot)) {
         stack = (ItemStack)this.inventory.get(subSlot);
         this.inventory.set(subSlot, ItemStack.EMPTY);
         if (!stack.isEmpty()) {
            Block.popResource(this.getLevel(), this.worldPosition, stack);
         }
      }
   }

   protected void checkTank(FluidTank tank) {
      if (this.canProcess() && this.hasEnergy((int)((double)this.maxEnergy * 0.01))) {
         this.addToTick();
         if (this.activeRecipes.size() == this.slotsInUse) {
            return;
         }

         for (int i = 0; i < this.slotsInUse; i++) {
            if (!this.activeRecipes.contains(i) && !((ItemStack)this.inventory.get(this.getInputSlot(i))).isEmpty()) {
               this.dirtyRecipes |= 1 << i;
               this.activeRecipes.add(i);
            }
         }
      }
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
   public void onStructureTick() {
      this.handleRedstone();
      boolean charging = this.handleChargeSlot((int)((double)this.maxEnergy * 0.9));
      boolean hasRecipe = !this.activeRecipes.isEmpty();
      boolean canWork = this.canProcess() || hasRecipe && this.hasEnergy(1);
      boolean canOperate = this.addItemsToInventory() && canWork && hasRecipe;
      this.onPreTick(hasRecipe, canWork, canOperate);
      if (canOperate && this.hasEnergy(this.isActive() ? this.activeRecipes.size() * this.energyConsume : (int)((double)this.maxEnergy * 0.1))) {
         boolean updateProgress = false;
         IntIterator iter = this.activeRecipes.iterator();

         while (iter.hasNext()) {
            updateProgress |= this.updateRecipeSlot(iter.nextInt());
         }

         if (updateProgress) {
            this.updateGuiField("progress");
         }

         this.setActive(!this.activeRecipes.isEmpty());
      } else {
         if (this.isActive()) {
            if (this.activeRecipeProgress > 0) {
               IC2.AUDIO.playSound(this, this.getInterruptSound(), AudioManager.SoundType.STATIC, this.soundLevel, 1.0F);
            }

            this.setActive(false);
         }

         if (this.activeRecipeProgress > 0) {
            IntIterator updateProgress = this.activeRecipes.iterator();

            while (updateProgress.hasNext()) {
               int slot = (Integer)updateProgress.next();
               if (this.progress[slot] > 0.0F) {
                  this.progress[slot] = Math.max(0.0F, this.getRecipe(slot) == null ? 0.0F : this.progress[slot] - this.progressPerTick);
                  if (this.progress[slot] <= 0.0F) {
                     this.activeRecipeProgress &= ~(1 << slot);
                  }
               }
            }

            this.updateGuiField("progress");
         }

         if (this.canStopTicking(charging)) {
            this.removeFromTick();
         }
      }

      this.storage.onTick(this.inventory, this);
      this.handleComparators();
   }

   protected boolean canStopTicking(boolean charging) {
      return !charging && !this.canWorkWithoutItems() && this.activeRecipeProgress <= 0 && !this.storage.has(IUpgradeItem.Functions.TICK);
   }

   protected void onPreTick(boolean hasRecipe, boolean canWork, boolean canOperate) {
   }

   protected boolean updateRecipeSlot(int recipeSlot) {
      IMachineRecipeList.RecipeEntry entry = this.getRecipe(recipeSlot);
      this.dirtyRecipes &= ~(1 << recipeSlot);
      if (this.hasEnergy(this.recipeEnergy[recipeSlot]) && entry != null) {
         this.activeRecipeProgress |= 1 << recipeSlot;
         this.progress[recipeSlot] = this.progress[recipeSlot] + this.progressPerTick;
         this.useEnergy(this.recipeEnergy[recipeSlot]);
         if (this.progress[recipeSlot] >= (float)this.recipeOperation[recipeSlot]) {
            this.operate(recipeSlot, entry);
            this.progress[recipeSlot] = 0.0F;
            this.dirtyRecipes |= 1 << recipeSlot;
            this.activeRecipeProgress &= ~(1 << recipeSlot);
         }

         return true;
      } else {
         if (entry == null) {
            this.activeRecipes.remove(recipeSlot);
         }

         if (this.progress[recipeSlot] > 0.0F) {
            this.progress[recipeSlot] = Math.max(0.0F, entry == null ? 0.0F : this.progress[recipeSlot] - this.progressPerTick);
            this.playSound(true);
            if (this.progress[recipeSlot] <= 0.0F) {
               this.activeRecipeProgress &= ~(1 << recipeSlot);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public void operate(int recipeSlot, IMachineRecipeList.RecipeEntry entry) {
      CompoundTag recipeData = new CompoundTag();
      this.storage.onRecipeFinishedPre(this.inventory, this, entry.getOutput(), recipeData);
      this.operateOnce(recipeSlot, entry.getInputs(), entry.getOutput(), recipeData);
      this.onRecipeProcessed(entry.getLocation());
      this.storage.onRecipeFinishedPost(this.inventory, this, entry, this.outputs);
      if (!this.outputs.isEmpty()) {
         this.addItemsToInventory();
      }
   }

   public void operateOnce(int recipeSlot, IInput[] input, IRecipeOutput output, CompoundTag recipeData) {
      for (ItemStack stack : this instanceof IRecipeOutput.IRecipeOverride
         ? output.onRecipeProcessed(this.getLevel().random, this.getPersistentData(), recipeData, (IRecipeOutput.IRecipeOverride)this)
         : output.onRecipeProcessed(this.getLevel().random, this.getPersistentData(), recipeData)) {
         this.addOutput(recipeSlot, stack);
      }

      this.steamTank.fillInternal(new FluidStack(IC2Fluids.STEAM, 25), FluidAction.EXECUTE);
      this.consumeInput(input, recipeSlot, RecipeFlags.CONSUME_CONTAINERS.getFlag(output.getMetadata(), false) || this.consumeContainers());
   }

   public void consumeInput(IInput[] inputs, int recipeSlot, boolean emptyContainer) {
      IInput input = inputs[0];
      int slot = this.getInputSlot(recipeSlot);
      this.waterTank.drainInternally(50, FluidAction.EXECUTE);
      if (!(input instanceof INullableInput) || !((ItemStack)this.inventory.get(slot)).isEmpty()) {
         if (!emptyContainer && ((ItemStack)this.inventory.get(slot)).hasCraftingRemainingItem()) {
            this.addOutput(recipeSlot, ((ItemStack)this.inventory.get(slot)).getCraftingRemainingItem());
         }

         ((ItemStack)this.inventory.get(slot)).shrink(input.getInputSize());
      }
   }

   protected void addOutput(int recipeSlot, ItemStack output) {
      this.outputs.add(new MultiStackOutput(output, this.getOutputSlots(recipeSlot)));
   }

   public IMachineRecipeList.RecipeEntry getRecipe(int recipeSlot) {
      if ((this.dirtyRecipes & 1 << recipeSlot) == 0) {
         IMachineRecipeList.RecipeEntry used = this.usedRecipe[recipeSlot];
         if (used == null || !used.hasNullInput()) {
            return used;
         }
      }

      int inputSlot = this.getInputSlot(recipeSlot);
      if (((ItemStack)this.inventory.get(inputSlot)).isEmpty() && !this.canWorkWithoutItems()) {
         return this.setRecipe(recipeSlot, null, true);
      } else {
         IMachineRecipeList.RecipeEntry recipe = this.activeRecipe[recipeSlot];
         if (recipe != null) {
            IInput input = recipe.getInputs()[0];
            boolean nullable = input instanceof INullableInput;
            if (nullable && !input.matches((ItemStack)this.inventory.get(inputSlot))) {
               this.setRecipe(recipeSlot, null, true);
            } else if (!nullable) {
               switch (this.isRecipeStillValid(recipeSlot, recipe)) {
                  case FAIL:
                     this.setRecipe(recipeSlot, null, true);
                     break;
                  case PASS:
                     return this.setRecipe(recipeSlot, null, false);
                  case IGNORE:
                     if (!((ItemStack)this.inventory.get(inputSlot)).isEmpty() && input.matches((ItemStack)this.inventory.get(inputSlot))) {
                        if (input.getInputSize() > ((ItemStack)this.inventory.get(inputSlot)).getCount()) {
                           return this.setRecipe(recipeSlot, null, false);
                        }
                     } else {
                        this.setRecipe(recipeSlot, null, true);
                     }
                  case SUCCESS:
               }
            }
         }

         if (recipe == null) {
            IMachineRecipeList.RecipeEntry entry = this.getRecipe(recipeSlot, ((ItemStack)this.inventory.get(inputSlot)).copy());
            if (entry == null) {
               return this.setRecipe(recipeSlot, null, true);
            }

            recipe = this.setRecipe(recipeSlot, entry, true);
            this.handleMods(recipeSlot, entry);
         }

         switch (this.canFillRecipeIntoOutputs(recipeSlot, recipe.getOutput())) {
            case SUCCESS:
               return this.setRecipe(recipeSlot, recipe, false);
            case PASS:
               return this.setRecipe(recipeSlot, null, false);
            default:
               if (this.waterTank.getFluidAmount() - (this.activeRecipes.size() - 1) * 50 < 50) {
                  return this.setRecipe(recipeSlot, null, false);
               } else {
                  List<ItemStack> outputs = recipe.getOutput().getAllOutputs();

                  for (int out : this.getOutputSlots(recipeSlot)) {
                     ItemStack inv = (ItemStack)this.inventory.get(out);
                     if (inv.isEmpty()) {
                        return this.setRecipe(recipeSlot, recipe, false);
                     }

                     int amount = StackUtil.getStackSizeLeft(inv);
                     if (amount > 0) {
                        for (ItemStack stack : outputs) {
                           if (StackUtil.canFitInto(inv, stack)) {
                              return this.setRecipe(recipeSlot, recipe, false);
                           }
                        }
                     }
                  }

                  return this.setRecipe(recipeSlot, null, false);
               }
         }
      }
   }

   protected IMachineRecipeList.RecipeEntry setRecipe(int slot, IMachineRecipeList.RecipeEntry entry, boolean active) {
      if (active) {
         this.activeRecipe[slot] = entry;
      }

      this.usedRecipe[slot] = entry;
      return entry;
   }

   protected BaseMultiMachineTileEntity.RecipeResult isRecipeStillValid(int recipeSlot, IMachineRecipeList.RecipeEntry entry) {
      return BaseMultiMachineTileEntity.RecipeResult.IGNORE;
   }

   protected InteractionResult canFillRecipeIntoOutputs(int recipeSlot, IRecipeOutput output) {
      return InteractionResult.FAIL;
   }

   public IMachineRecipeList.RecipeEntry getRecipe(int recipeSlot, ItemStack stack) {
      return this.getRecipeList().getRecipe(stack, true);
   }

   protected boolean canWorkWithoutItems() {
      return false;
   }

   protected boolean consumeContainers() {
      return false;
   }

   @Override
   public boolean canProcess() {
      return this.waterTank.getFluidAmount() >= this.activeRecipes.size() * 100 && this.steamTank.getSpace() >= this.activeRecipes.size() * 25
         ? !this.isRedstoneSensitive() || this.isRedstonePowered()
         : false;
   }

   @Override
   public int getValidRoom(ItemStack stack) {
      if (this.getRecipeList().getRecipe(stack, false) == null) {
         return 0;
      } else {
         for (int i = 0; i < this.slotsInUse; i++) {
            ItemStack invStack = this.getStackInSlot(this.getInputSlot(i));
            if (invStack.isEmpty()) {
               return stack.getMaxStackSize();
            }

            int left = StackUtil.getStackSizeLeft(invStack);
            if (left > 0 && StackUtil.isStackEqual(invStack, stack)) {
               return left;
            }
         }

         return 0;
      }
   }

   public boolean canInsertInSlot(int slot, ItemStack stack) {
      int recipeSlot = this.getRecipeSlots(slot, true);
      if (recipeSlot >= 0 && recipeSlot < this.slotsInUse && this.getRecipeList().getRecipe(stack, false) != null) {
         ItemStack invStack = this.getStackInSlot(slot);
         return invStack.isEmpty() ? true : StackUtil.isStackEqual(invStack, stack);
      } else {
         return false;
      }
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      if (this.isSimulating()) {
         this.addToTick();
         int recipeSlot = this.getRecipeSlots(slot, true);
         if (recipeSlot != -1 && (this.dirtyRecipes & 1 << recipeSlot) == 0) {
            this.dirtyRecipes |= 1 << recipeSlot;
            this.activeRecipes.add(recipeSlot);
         } else {
            recipeSlot = this.getRecipeSlots(slot, false);
            if (recipeSlot != -1 && (this.dirtyRecipes & 1 << recipeSlot) == 0) {
               this.dirtyRecipes |= 1 << recipeSlot;
               this.activeRecipes.add(recipeSlot);
            }
         }
      }

      super.setStackInSlot(slot, stack);
   }

   @Override
   protected void createInvCaches() {
      this.inOut = new IHasInventory[2];
      this.inOut[0] = new RangedInventory(this, this.getAllSlots(true));
      this.inOut[1] = new RangedInventory(this, this.getAllSlots(false)).setOutputOnly();
   }

   @Override
   protected void handleMods() {
      for (int i = 0; i < this.slotsInUse; i++) {
         this.handleMods(i, this.usedRecipe[i]);
      }
   }

   protected void handleMods(int recipeSlot, IMachineRecipeList.RecipeEntry entry) {
      this.updateGuiFields(new String[]{"recipeEnergy", "recipeOperation"});
      if (entry == null) {
         this.recipeEnergy[recipeSlot] = Math.max(1, this.energyConsume);
         this.recipeOperation[recipeSlot] = Math.max(1, this.operationLength);
      } else {
         CompoundTag nbt = entry.getOutput().getMetadata();
         this.recipeEnergy[recipeSlot] = RecipeMods.ENERGY_USAGE.apply(nbt, this.energyConsume);
         this.recipeOperation[recipeSlot] = RecipeMods.RECIPE_TIME.apply(nbt, this.operationLength);
      }
   }

   @Override
   public void onStructureInvalidated(boolean wasValid, boolean removeMaster) {
      super.onStructureInvalidated(wasValid, removeMaster);
      if (wasValid && !removeMaster) {
         this.addToTick();
         Arrays.fill(this.progress, 0.0F);
      }
   }

   private static class MachineProgress implements IProgressMachine {
      BaseColossalMachineTileEntity tile;
      int index;

      public MachineProgress(BaseColossalMachineTileEntity tile, int index) {
         this.tile = tile;
         this.index = index;
      }

      @Override
      public float getProgress() {
         return this.tile.getProgress(this.index);
      }

      @Override
      public float getMaxProgress() {
         return this.tile.getMaxProgress(this.index);
      }
   }

   private static class MachineVisibility implements BooleanSupplier {
      BaseColossalMachineTileEntity tile;
      int index;

      public MachineVisibility(BaseColossalMachineTileEntity tile, int index) {
         this.tile = tile;
         this.index = index;
      }

      @Override
      public boolean getAsBoolean() {
         return this.tile.slotsInUse > this.index;
      }
   }
}
