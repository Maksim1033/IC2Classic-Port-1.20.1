package ic2.core.block.base.tiles.impls.machine.single;

import ic2.api.core.IC2Classic;
import ic2.api.items.IUpgradeItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.ingridients.queue.IInputter;
import ic2.api.recipes.ingridients.queue.IStackOutput;
import ic2.api.recipes.misc.RecipeMods;
import ic2.api.tiles.readers.IProgressMachine;
import ic2.api.tiles.tubes.ITube;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.audio.IAudioSource;
import ic2.core.block.base.cache.CapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.IInventoryMachine;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.misc.UpgradeStorage;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.inv.INotifyInventory;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

public abstract class BaseMachineTileEntity
   extends BaseElectricTileEntity
   implements IInventoryMachine,
   IInputter,
   IProgressMachine,
   ITileActivityProvider,
   INotifyInventory {
   protected String isWorkingString = "isActive";
   public int defaultEnergyConsume;
   public int defaultOperationLength;
   public int defaultEnergyStorage;
   public float defaultProgressPerTick = 1.0F;
   public int energyConsume;
   public int operationLength;
   public float progressPerTick = 1.0F;
   @NetworkInfo
   public float soundLevel = 1.0F;
   public IAudioSource audio;
   public final int upgradeSlots;
   protected UpgradeStorage storage = new UpgradeStorage();
   protected ICache<IItemHandler> invCache = new CapabilityCache<>(this, DirectionList.ALL, ForgeCapabilities.ITEM_HANDLER);
   protected ICache<ITube> tubeCache = new CapabilityCache<>(this, DirectionList.ALL, IC2Classic.TUBE_CAPABILITY);
   protected Object2IntLinkedOpenHashMap<ResourceLocation> processedRecipes = new Object2IntLinkedOpenHashMap();
   protected List<IStackOutput> outputs = CollectionUtils.<IStackOutput>createList();
   protected IHasInventory[] inOut;

   public BaseMachineTileEntity(BlockPos pos, BlockState state, int size, int upgradeSlots, int energyPerTick, int maxProgress, int maxEnergy, int maxInput) {
      super(pos, state, size + upgradeSlots, maxInput, maxEnergy);
      this.defaultEnergyConsume = this.energyConsume = energyPerTick;
      this.defaultOperationLength = this.operationLength = maxProgress;
      this.defaultEnergyStorage = maxEnergy;
      this.upgradeSlots = upgradeSlots;
      this.addCaches(new ICache[]{this.invCache, this.tubeCache});
      this.addNetworkFields(new String[]{"soundLevel"});
   }

   public boolean canProcess() {
      return !this.isRedstoneSensitive() || this.isRedstonePowered();
   }

   protected ResourceLocation getInterruptSound() {
      return IC2Sounds.INTERRUPTION;
   }

   protected ResourceLocation getStartupSound() {
      return null;
   }

   protected ResourceLocation getWorkingSound() {
      return null;
   }

   protected abstract void createInvCaches();

   protected abstract void handleMods();

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);

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
      ListTag list = new ListTag();

      for (IStackOutput stack : this.outputs) {
         list.add(IngredientRegistry.INSTANCE.writeQueue(stack));
      }

      NBTUtils.put(compound, "cache", list);
   }

   public int getEnergyPerTick() {
      return this.energyConsume;
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
      return this.isActive();
   }

   @Override
   public void setRedstoneSensitive(boolean flag) {
      this.sensitive = flag;
   }

   @Override
   public boolean isRedstoneSensitive() {
      return this.sensitive;
   }

   @Override
   public IItemHandler getConnectedInventory(Direction dir) {
      InventoryHandler handler = this.getInventoryHandler();
      return handler.isBlockSideUnlocked(dir) ? this.invCache.getHandler(dir) : null;
   }

   @Override
   public ITube getConnectedTube(Direction dir) {
      InventoryHandler handler = this.getInventoryHandler();
      return handler.isBlockSideUnlocked(dir) ? this.tubeCache.getHandler(dir) : null;
   }

   @Override
   public IHasInventory getInputInventory() {
      if (this.inOut == null) {
         this.createInvCaches();
      }

      return this.inOut[0];
   }

   @Override
   public IHasInventory getOutputInventory() {
      if (this.inOut == null) {
         this.createInvCaches();
      }

      return this.inOut[1];
   }

   public void playSound(boolean interrupt) {
      IC2.AUDIO.playSound(this, interrupt ? this.getInterruptSound() : this.getStartupSound(), AudioManager.SoundType.STATIC, this.soundLevel, 1.0F);
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains(this.isWorkingString)) {
         if (this.audio == null || !this.audio.isValid()) {
            this.audio = IC2.AUDIO.createSource(this, this.getWorkingSound(), AudioManager.SoundType.STATIC, this.soundLevel, true, false);
         }

         this.playStop(this.audio, this.isMachineWorking());
      }

      if (fields.contains("soundLevel") && this.audio != null) {
         this.audio.setVolume(this.soundLevel);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      this.audio = null;
      IC2.AUDIO.removeSource(this);
      super.onUnloaded(chunk);
   }

   protected void onRecipeProcessed(ResourceLocation location) {
      this.processedRecipes.addTo(location, 1);
   }

   @Override
   public void onNotify(IHasInventory inventory, int slot) {
   }

   @Override
   public void onUpgradesChanged() {
      this.handleUpgrades(false);
   }

   protected void handleUpgrades(boolean defaultSensitive) {
      int extraSpeed = 0;
      double speedMultiplier = 1.0;
      int extraTime = 0;
      double timeMultiplier = 1.0;
      int extraUsage = 0;
      double usageMultiplier = 1.0;
      int extraEnergy = 0;
      double energyMultiplier = 1.0;
      int extraTier = 0;
      float soundMultiplier = 1.0F;
      boolean redstoneInverted = false;
      this.sensitive = defaultSensitive;
      this.storage.clear();
      EnumSet<IUpgradeItem.UpgradeType> types = this.getSupportedUpgradeTypes();

      for (int i = 0; i < this.upgradeSlots; i++) {
         ItemStack item = (ItemStack)this.inventory.get(i + this.inventory.size() - this.upgradeSlots);
         Item var21 = item.getItem();
         if (var21 instanceof IUpgradeItem) {
            IUpgradeItem upgrade = (IUpgradeItem)var21;
            if (types.contains(upgrade.getType(item))) {
               upgrade.onInstall(item, this);
               this.storage.validate(i + this.inventory.size() - this.upgradeSlots, upgrade.getFunctions(item));
               extraSpeed += upgrade.getExtraProcessingSpeed(item, this) * item.getCount();
               speedMultiplier *= Math.pow(upgrade.getProcessingSpeedMultiplier(item, this), (double)item.getCount());
               extraTime += upgrade.getExtraProcessingTime(item, this) * item.getCount();
               timeMultiplier *= Math.pow(upgrade.getProcessingTimeMultiplier(item, this), (double)item.getCount());
               extraUsage += upgrade.getExtraEnergyDemand(item, this) * item.getCount();
               usageMultiplier *= Math.pow(upgrade.getEnergyDemandMultiplier(item, this), (double)item.getCount());
               extraEnergy += upgrade.getExtraEnergyStorage(item, this) * item.getCount();
               energyMultiplier *= Math.pow(upgrade.getEnergyStorageMultiplier(item, this), (double)item.getCount());
               soundMultiplier *= (float)Math.pow((double)upgrade.getSoundMultiplier(item, this), (double)item.getCount());
               extraTier += upgrade.getExtraTier(item, this) * item.getCount();
               if (upgrade.useRedstoneInvertion(item, this)) {
                  redstoneInverted = true;
               }
            }
         }
      }

      this.soundLevel = soundMultiplier;
      this.inverted = redstoneInverted;
      this.progressPerTick = Math.max(0.01F, RecipeMods.apply(this.defaultProgressPerTick, (float)extraSpeed, speedMultiplier));
      this.energyConsume = Math.max(this.getMinimumEnergyUsage(), RecipeMods.apply(this.defaultEnergyConsume, extraUsage, usageMultiplier));
      this.operationLength = Math.max(1, RecipeMods.apply(this.defaultOperationLength, extraTime, timeMultiplier));
      this.setMaxEnergy(Math.max(1, RecipeMods.apply(this.defaultEnergyStorage, extraEnergy, energyMultiplier)));
      this.setTier(Mth.clamp(this.baseTier + extraTier, 0, 13));
      if (this.energy > this.maxEnergy) {
         this.energy = this.maxEnergy;
         this.updateGuiFields(new String[]{"energy"});
      }

      this.updateTileField("soundLevel");
      this.handleMods();
      IC2.TICK_HANDLER.addWorldCallback(this.getLevel(), T -> {
         this.addToTick();
         return 0;
      });
   }

   protected int getMinimumEnergyUsage() {
      return 1;
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

   protected boolean addItemsToInventory() {
      if (this.outputs.isEmpty()) {
         return true;
      } else {
         this.outputs.removeIf(iStackOutput -> iStackOutput.addToInventory(this));
         return this.outputs.isEmpty();
      }
   }

   @Override
   public boolean supportsNotify() {
      return true;
   }

   public static enum RecipeResult {
      FAIL,
      PASS,
      SUCCESS,
      IGNORE;
   }
}
