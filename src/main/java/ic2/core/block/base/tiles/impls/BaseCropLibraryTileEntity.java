package ic2.core.block.base.tiles.impls;

import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRegistry;
import ic2.api.crops.ICropSeed;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.block.machines.logic.crop.CropSorter;
import ic2.core.block.machines.logic.crop.ISeedEntry;
import ic2.core.block.machines.logic.crop.SeedEntry;
import ic2.core.block.machines.logic.crop.SeedStorage;
import ic2.core.block.machines.logic.crop.SeedSyncer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BaseCropLibraryTileEntity extends BaseInventoryTileEntity implements ITickListener, ITileGui, IWrenchableTile, IClickable {
   public static final List<CropSorter> SORTERS = ObjectArrayList.wrap(
      new CropSorter[]{new CropSorter(0), new CropSorter(1), new CropSorter(2), new CropSorter(3), new CropSorter(4), new CropSorter(5)}
   );
   public SeedStorage storage;
   @NetworkInfo
   public SeedSyncer syncer;
   @NetworkInfo
   public ICrop crop = null;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_16)
   public int cropOffset = 0;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_8)
   public int sorter = 0;
   @NetworkInfo
   public boolean sorting = false;
   @NetworkInfo
   public boolean inverting = false;

   public BaseCropLibraryTileEntity(BlockPos pos, BlockState state, SeedStorage storage) {
      this(pos, state, 4, storage);
   }

   public BaseCropLibraryTileEntity(BlockPos pos, BlockState state, int slots, SeedStorage storage) {
      super(pos, state, slots);
      this.storage = storage;
      this.syncer = storage.getSyncer();
      this.addGuiFields(new String[]{"syncer", "crop", "selected", "sorter", "sorting"});
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.IMPORT);
      handler.registerBlockSides(DirectionList.ALL);
      int[] slots = MathUtils.fromTo(0, this.getSlotCount());
      handler.registerSlotAccess(AccessRule.IMPORT, slots);
      handler.registerSlotsForSide(DirectionList.ALL, slots);
      handler.registerInputFilter(SpecialFilters.CROP_FILTER_SCANNED, slots);
      handler.registerNamedSlot(SlotType.INPUTS, slots);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.storage.read(compound.getCompound("storage"));
      String id = NBTUtils.getString(compound, "crop", "empty");
      this.crop = id.equalsIgnoreCase("empty") ? null : ICropRegistry.INSTANCE.getCrop(new ResourceLocation(id));
      this.cropOffset = compound.getInt("selectionIndex");
      this.sorter = compound.getInt("sorter");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.put(compound, "storage", this.storage.write(new CompoundTag()));
      NBTUtils.putString(compound, "crop", this.crop == null ? "empty" : this.crop.id().toString(), "empty");
      NBTUtils.putShort(compound, "selectionIndex", (short)this.cropOffset, 0);
      NBTUtils.putByte(compound, "sorter", (byte)this.sorter, 0);
   }

   @Override
   public boolean canSetFacing(Direction dir) {
      return this.getFacing() != dir && this.getFacing().getAxis().isHorizontal();
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating()) {
         this.storage.updateDisplay(this.crop);
      }
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (stack.is(IC2Items.MEMORY_STICK)) {
         CompoundTag tag = StackUtil.getNbtData(stack);
         if (tag.contains("crops")) {
            if (this.isSimulating()) {
               ListTag crops = tag.getList("crops", 10);
               int originalSize = crops.size();
               ListTag result = this.storage.loadFromList(crops);
               if (result.isEmpty()) {
                  stack.removeTagKey("crops");
               } else {
                  tag.put("crops", result);
               }

               originalSize -= result.size();
               player.sendSystemMessage(this.translate("tooltip.item.ic2.memory_stick.remove_crops", new Object[]{originalSize}));
            }

            return true;
         } else {
            if (this.isSimulating()) {
               ListTag crops = this.storage.saveToList();
               stack.getOrCreateTag().put("crops", crops);
               player.sendSystemMessage(this.translate("tooltip.item.ic2.memory_stick.add_crops", new Object[]{crops.size()}));
               this.storage.clear();
               this.crop = null;
               this.cropOffset = 0;
               this.storage.offset(null, -2147483648);
               this.storage.updateDisplay(this.crop);
               this.updateGuiFields(new String[]{"crop", "cropOffset", "syncer"});
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void onTick() {
      if (this.storage.processSorting(this.crop, (Comparator<ISeedEntry>)(this.inverting ? SORTERS.get(this.sorter).reversed() : SORTERS.get(this.sorter)))) {
         this.storage.updateDisplay(this.crop);
      }

      if (this.clock(10)) {
         this.addCrop();
         this.transferStack(1, 0);
         this.transferStack(2, 1);
         this.transferStack(3, 2);
      }

      this.handleComparators();
   }

   protected boolean addCrop() {
      if (!((ItemStack)this.inventory.get(0)).isEmpty()) {
         ItemStack stack = (ItemStack)this.inventory.get(0);
         ICropSeed seed = this.getCrop(stack);
         if (seed != null) {
            ICrop crop = seed.getCrop(stack);
            if (crop == null || seed.getScanLevel(stack) < 4) {
               return false;
            }

            ISeedEntry entry = new SeedEntry(
               seed.getGrowth(stack), seed.getGain(stack), seed.getResistance(stack), crop.getProperties().getTier(), stack.getCount()
            );
            if (this.storage.addCrop(crop, entry)) {
               stack.setCount(entry.getCount());
               this.storage.updateDisplay(this.crop);
               this.updateGuiField("syncer");
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0) {
         this.cropOffset = Mth.clamp(this.cropOffset + value, 0, (this.syncer.size() + 1) / 5);
         this.updateGuiField("cropOffset");
      } else if (key == 1) {
         value = Mth.clamp(value, 0, this.syncer.size() + 1);
         this.crop = value == 0 ? null : ICropRegistry.INSTANCE.getCrop(this.syncer.get(value - 1));
         this.storage.offset(this.crop, -2147483648);
         this.storage.updateDisplay(this.crop);
         this.updateGuiFields(new String[]{"crop"});
      } else if (key == 2) {
         this.storage.offset(this.crop, value);
         this.storage.updateDisplay(this.crop);
      } else if (key == 3) {
         this.sorting = value > 0;
         this.updateGuiField("sorting");
         if (this.sorting) {
            this.storage.markAllDirty(this.crop);
            if (this.storage
               .processSorting(this.crop, (Comparator<ISeedEntry>)(this.inverting ? SORTERS.get(this.sorter).reversed() : SORTERS.get(this.sorter)))) {
               this.storage.updateDisplay(this.crop);
            }
         }
      } else if (key == 4) {
         this.sorter = (this.sorter + 1) % SORTERS.size();
         this.updateGuiField("sorter");
         this.storage.markAllDirty(this.crop);
         if (this.storage.processSorting(this.crop, (Comparator<ISeedEntry>)(this.inverting ? SORTERS.get(this.sorter).reversed() : SORTERS.get(this.sorter)))) {
            this.storage.updateDisplay(this.crop);
         }
      } else if (key == 5) {
         this.inverting = !this.inverting;
         this.updateGuiField("inverting");
         this.storage.markAllDirty(this.crop);
         if (this.storage.processSorting(this.crop, (Comparator<ISeedEntry>)(this.inverting ? SORTERS.get(this.sorter).reversed() : SORTERS.get(this.sorter)))) {
            this.storage.updateDisplay(this.crop);
         }
      } else if (key == 6) {
         int slot = MathUtils.getShortKey(value);
         ItemStack stack = this.storage.remove(this.crop, slot, MathUtils.getShortValue(value), false);
         if (!stack.isEmpty()) {
            int added = TransporterManager.getTransporter(entity).addItem(stack, Direction.DOWN, false);
            if (added > 0) {
               this.storage.remove(this.crop, slot, added, true);
               this.updateGuiField("syncer");
               if (!this.storage.containsCrop(this.crop)) {
                  this.crop = null;
                  this.cropOffset = 0;
                  this.storage.offset(null, -2147483648);
                  this.updateGuiFields(new String[]{"crop", "cropOffset"});
               }

               this.storage.markCurrentDirty(this.crop);
            }
         }
      }
   }

   protected ICropSeed getCrop(ItemStack stack) {
      return !(stack.getItem() instanceof ICropSeed) ? null : (ICropSeed)stack.getItem();
   }
}
