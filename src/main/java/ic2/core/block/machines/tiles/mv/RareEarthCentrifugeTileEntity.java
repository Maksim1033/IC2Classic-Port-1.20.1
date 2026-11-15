package ic2.core.block.machines.tiles.mv;

import ic2.api.items.IUpgradeItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.registries.IRareEarthRegistry;
import ic2.api.tiles.readers.ISpeedMachine;
import ic2.api.tiles.readers.ISubProgressMachine;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.IInventoryMachine;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.misc.comparator.types.base.SpeedComparator;
import ic2.core.block.base.tiles.impls.machine.single.BaseMachineTileEntity;
import ic2.core.block.machines.containers.mv.RareEarthCentrifugeContainer;
import ic2.core.block.machines.tiles.lv.RareEarthExtractorTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.filter.special.MachineFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.inv.RangedInventory;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.StackUtil;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RareEarthCentrifugeTileEntity
   extends BaseMachineTileEntity
   implements ITickListener,
   IInventoryMachine,
   ISubProgressMachine,
   ISpeedMachine,
   ITileGui {
   public static final Component SPEED = Component.translatable("info.block.ic2.rare_earth_centrifuge.speed");
   public static final EnumSet<IUpgradeItem.UpgradeType> UPGRADES = EnumSet.complementOf(EnumSet.of(IUpgradeItem.UpgradeType.RECIPE_MOD));
   public static final int MAX_SPEED = 10000;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_16)
   int speed = 0;
   @NetworkInfo
   ResourceLocation currentID = RareEarthExtractorTileEntity.EMPTY;
   @NetworkInfo
   public float materialProgress = 0.0F;
   @NetworkInfo
   public float progress = 0.0F;
   @NetworkInfo
   public boolean isProcessing;
   @NetworkInfo
   public float soundLevel = 1.0F;
   public IFilter gui_Filter = T -> this.getRegistry().getOutputFor(T) != null;

   public RareEarthCentrifugeTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3, 2, 15, 99, 5120, 128);
      this.setFuelSlot(0);
      this.addGuiFields(new String[]{"speed", "progress"});
      this.addComparator(new ProgressComparator("progress", ComparatorNames.PROGRESS, this));
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
      this.addComparator(new FlagComparator("working", ComparatorNames.WORKING, () -> this.isProcessing, 0, 15));
      this.addComparator(new SpeedComparator("speed", ComparatorNames.SPEED, this));
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
      handler.registerInputFilter(SpecialFilters.createChargeFilter(), 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_DISCHARGE_FILTER, 0);
      handler.registerInputFilter(new MachineFilter(this), 1);
      handler.registerNamedSlot(SlotType.BATTERY, 0);
      handler.registerNamedSlot(SlotType.INPUT, 1);
      handler.registerNamedSlot(SlotType.OUTPUT, 2);
   }

   public Component getSpeedName() {
      return SPEED;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.currentID = new ResourceLocation(compound.getString("recipe_id"));
      this.materialProgress = compound.getFloat("material");
      this.progress = (float)compound.getInt("progress");
      this.speed = compound.getInt("speed");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putString("recipe_id", this.currentID.toString());
      compound.putFloat("material", this.materialProgress);
      compound.putByte("progress", (byte)((int)this.progress));
      compound.putShort("speed", (short)this.speed);
   }

   public IRareEarthRegistry getRegistry() {
      return IC2.RECIPES.get(this.isSimulating()).rare_earth;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.CENTRIFUGAL_RARE_EARTH_EXTRACTOR;
   }

   @Override
   protected void createInvCaches() {
      this.inOut = new IHasInventory[2];
      this.inOut[0] = new RangedInventory(this, 1);
      this.inOut[1] = new RangedInventory(this, 2).setOutputOnly();
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new RareEarthCentrifugeContainer(this, player, windowID);
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
   public float getSubProgress() {
      return this.materialProgress;
   }

   @Override
   public float getMaxSubProgress() {
      return 1000.0F;
   }

   @Override
   public float getProgress() {
      return this.progress;
   }

   @Override
   public float getMaxProgress() {
      return (float)this.operationLength;
   }

   public ItemStack getCurrentOutput() {
      return this.getRegistry().getOutputFor(this.currentID);
   }

   @Override
   public void onTick() {
      this.handleChargeSlot(this.maxEnergy);
      this.handleRedstone();
      if (this.hasEnergy(1) && (!((ItemStack)this.inventory.get(1)).isEmpty() || this.canProcess())) {
         this.setActive(true);
         if (this.speed < 10000) {
            this.speed++;
            this.updateGuiField("speed");
         }

         this.useEnergy(1);
      } else {
         if (this.speed > 0) {
            this.speed = Math.max(0, this.speed - 4);
            this.updateGuiField("speed");
         }

         this.setActive(false);
      }

      this.progressPerTick = (float)this.speed / 300.0F;
      this.handleComparators();
      if (this.hasEnergy(this.energyConsume) && !((ItemStack)this.inventory.get(1)).isEmpty()) {
         IRareEarthRegistry.RareEntry entry = this.getRegistry().getOutputFor((ItemStack)this.inventory.get(1));
         if (entry == null || !this.canAdd(entry)) {
            if (this.progress != 0.0F) {
               this.progress = 0.0F;
               this.updateGuiField("progress");
            }

            this.isProcessing = false;
            this.storage.onTick(this.inventory, this);
            return;
         }

         if (!entry.matches(this.currentID)) {
            this.currentID = entry.getID();
            this.materialProgress = 0.0F;
            this.updateGuiField("materialProgress");
            this.updateGuiField("currentID");
         }

         if (this.speed <= 0) {
            this.isProcessing = false;
            return;
         }

         this.isProcessing = true;
         this.progress = this.progress + this.progressPerTick;
         this.useEnergy(this.energyConsume);
         if (this.progress >= (float)this.operationLength) {
            this.progress = 0.0F;
            this.materialProgress = this.materialProgress + entry.getValue();
            this.updateGuiField("materialProgress");
            ItemStack stack = (ItemStack)this.inventory.get(1);
            if (stack.hasCraftingRemainingItem()) {
               this.inventory.set(1, stack.getCraftingRemainingItem());
            } else {
               stack.shrink(1);
            }

            if (this.materialProgress >= 1000.0F) {
               this.materialProgress -= 1000.0F;
               this.setOrGrow(2, entry.getOutput().copy(), false);
            }

            this.notifyListeners();
            this.storage.onRecipeFinished(this.inventory, this);
         }

         this.updateGuiField("progress");
      } else {
         this.isProcessing = false;
         if (this.progress != 0.0F) {
            this.progress = Math.max(0.0F, ((ItemStack)this.inventory.get(1)).isEmpty() ? 0.0F : this.progress - 1.0F);
            this.updateGuiField("progress");
         }
      }

      this.storage.onTick(this.inventory, this);
   }

   private boolean canAdd(IRareEarthRegistry.RareEntry entry) {
      return this.materialProgress + entry.getValue() < 1000.0F
         || ((ItemStack)this.inventory.get(2)).isEmpty()
         || StackUtil.canFitInto((ItemStack)this.inventory.get(2), entry.getOutput());
   }

   @Override
   public int getValidRoom(ItemStack stack) {
      ItemStack inv = (ItemStack)this.inventory.get(1);
      if (inv.isEmpty()) {
         IRareEarthRegistry.RareEntry entry = this.getRegistry().getOutputFor(stack);
         if (entry != null) {
            return stack.getMaxStackSize();
         }
      } else if (StackUtil.isStackEqual(inv, stack)) {
         return inv.getMaxStackSize() - inv.getCount();
      }

      return 0;
   }

   @Override
   public EnumSet<IUpgradeItem.UpgradeType> getSupportedUpgradeTypes() {
      return UPGRADES;
   }

   @Override
   public boolean isMachineWorking() {
      return this.isProcessing;
   }

   @Override
   public void onUpgradesChanged() {
      this.handleUpgrades(true);
   }

   @Override
   protected void handleMods() {
   }
}
