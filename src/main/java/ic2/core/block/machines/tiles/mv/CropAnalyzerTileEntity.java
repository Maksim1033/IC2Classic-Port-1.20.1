package ic2.core.block.machines.tiles.mv;

import ic2.api.crops.ICropSeed;
import ic2.api.items.IUpgradeItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.readers.IProgressMachine;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.IInventoryMachine;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.tiles.impls.machine.single.BaseMachineTileEntity;
import ic2.core.block.machines.containers.mv.ContainerCropAnalyzer;
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
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CropAnalyzerTileEntity extends BaseMachineTileEntity implements ITickListener, IInventoryMachine, IProgressMachine, ITileGui {
   public static final EnumSet<IUpgradeItem.UpgradeType> TYPES = EnumSet.of(
      IUpgradeItem.UpgradeType.TRANSPORT_MOD,
      IUpgradeItem.UpgradeType.CUSTOM_MOD,
      IUpgradeItem.UpgradeType.MACHINE_MOD,
      IUpgradeItem.UpgradeType.PROCESSING_MOD,
      IUpgradeItem.UpgradeType.RECIPE_MOD
   );
   @NetworkInfo
   public int progress = 0;
   int delay = 20;

   public CropAnalyzerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3, 4, 1, 1000, 1020, 32);
      this.setFuelSlot(0);
      this.addGuiFields(new String[]{"progress"});
      this.sensitive = false;
      this.addComparator(new ProgressComparator("progress", ComparatorNames.PROGRESS, this));
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.registerSlotAccess(AccessRule.IMPORT, 1);
      handler.registerSlotAccess(AccessRule.EXPORT, 2);
      handler.registerSlotsForSide(DirectionList.DOWN, 0);
      handler.registerSlotsForSide(DirectionList.DOWN.invert(), 1);
      handler.registerSlotsForSide(DirectionList.UP.invert(), 2);
      handler.registerInputFilter(SpecialFilters.createChargeFilter(), 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_DISCHARGE_FILTER, 0);
      handler.registerInputFilter(new MachineFilter(this), 1);
      handler.registerNamedSlot(SlotType.BATTERY, 0);
      handler.registerNamedSlot(SlotType.INPUT, 1);
      handler.registerNamedSlot(SlotType.OUTPUT, 2);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.delay = NBTUtils.getInt(compound, "delay", 0);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putInt(compound, "delay", this.delay, 0);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ContainerCropAnalyzer(this, player, windowID);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.CROP_ANALYZER;
   }

   @Override
   protected void createInvCaches() {
      this.inOut = new IHasInventory[2];
      this.inOut[0] = new RangedInventory(this, 1);
      this.inOut[1] = new RangedInventory(this, 2).setOutputOnly();
   }

   @Override
   public float getProgress() {
      return (float)this.progress;
   }

   @Override
   public float getMaxProgress() {
      ICropSeed seed = this.getSeed();
      if (seed == null) {
         return 0.0F;
      } else {
         int level = seed.getScanLevel((ItemStack)this.inventory.get(1));
         return level >= 0 && level <= 3 ? (float)(ICropSeed.SCAN_COST[level] / 10) : 0.0F;
      }
   }

   @Override
   public void onTick() {
      this.handleChargeSlot(this.maxEnergy);
      ICropSeed seed = this.getSeed();
      if (((ItemStack)this.inventory.get(2)).isEmpty() && seed != null && this.delay <= 0) {
         int level = seed.getScanLevel((ItemStack)this.inventory.get(1));
         if (level < 0 || level > 3) {
            this.onProgressFinished();
            this.progress = 0;
            this.updateGuiField("progress");
            return;
         }

         if (this.hasEnergy(this.energyConsume)) {
            this.setActive(true);
            this.progress = this.progress + this.energyConsume;
            this.useEnergy(this.energyConsume);
            if (this.progress >= ICropSeed.SCAN_COST[level] / 10) {
               this.progress = 0;
               seed.increaseScanLevel((ItemStack)this.inventory.get(1));
               int lvl = seed.getScanLevel((ItemStack)this.inventory.get(1));
               if (lvl >= 4 || lvl < 0) {
                  this.onProgressFinished();
               }
            }

            this.updateGuiField("progress");
         } else {
            this.setActive(false);
            if (this.progress != 0) {
               this.progress = Math.max(0, this.progress - 1);
               this.updateGuiField("progress");
            }
         }
      } else {
         this.setActive(false);
         if (this.progress != 0) {
            this.progress = 0;
            this.updateGuiField("progress");
         }
      }

      if (this.delay > 0) {
         this.delay--;
      }

      this.storage.onTick(this.inventory, this);
      this.handleComparators();
   }

   public void onProgressFinished() {
      this.inventory.set(2, (ItemStack)this.inventory.get(1));
      this.inventory.set(1, ItemStack.EMPTY);
      this.storage.onRecipeFinished(this.inventory, this);
      this.notifyListeners();
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      if (slot == 1) {
         this.delay = 20;
      }

      super.setStackInSlot(slot, stack);
   }

   public ICropSeed getSeed() {
      Item item = ((ItemStack)this.inventory.get(1)).getItem();
      return item instanceof ICropSeed ? (ICropSeed)item : null;
   }

   @Override
   public int getValidRoom(ItemStack stack) {
      return ((ItemStack)this.inventory.get(1)).isEmpty() && stack.getItem() instanceof ICropSeed ? 1 : 0;
   }

   @Override
   public EnumSet<IUpgradeItem.UpgradeType> getSupportedUpgradeTypes() {
      return TYPES;
   }

   @Override
   protected void handleMods() {
   }
}
