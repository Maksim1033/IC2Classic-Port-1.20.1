package ic2.core.block.machines.tiles.lv;

import ic2.api.items.IUpgradeItem;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.ingridients.queue.SimpleStackOutput;
import ic2.api.recipes.registries.IRareEarthRegistry;
import ic2.api.tiles.readers.ISubProgressMachine;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.IInventoryMachine;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.tiles.impls.machine.single.BaseMachineTileEntity;
import ic2.core.block.base.tiles.impls.machine.single.BasicMachineTileEntity;
import ic2.core.block.machines.containers.lv.RareEarthExtractorContainer;
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
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.StackUtil;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RareEarthExtractorTileEntity extends BaseMachineTileEntity implements ITickListener, ITileGui, IInventoryMachine, ISubProgressMachine {
   public static final ResourceLocation EMPTY = new ResourceLocation("empty");
   public static final ResourceLocation RARE_EARTH = new ResourceLocation("ic2:rare_earth");
   public static final ResourceLocation ALUMINIUM = new ResourceLocation("ic2:aluminium");
   @NetworkInfo
   public ResourceLocation currentID = EMPTY;
   @NetworkInfo
   public float materialProgress = 0.0F;
   @NetworkInfo
   public float progress = 0.0F;
   public IFilter gui_Filter = T -> this.getRegistry().getOutputFor(T) != null;

   public RareEarthExtractorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3, 2, 1, 35, 1120, 32);
      this.sensitive = false;
      this.setFuelSlot(0);
      this.addGuiFields(new String[]{"currentID", "materialProgress", "progress"});
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
      handler.registerSlotsForSide(DirectionList.UP.invert(), 0, 2);
      handler.registerSlotsForSide(DirectionList.DOWN.invert(), 1);
      handler.registerInputFilter(SpecialFilters.createChargeFilter(), 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_DISCHARGE_FILTER, 0);
      handler.registerInputFilter(new MachineFilter(this), 1);
      handler.registerNamedSlot(SlotType.BATTERY, 0);
      handler.registerNamedSlot(SlotType.INPUT, 1);
      handler.registerNamedSlot(SlotType.OUTPUT, 2);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.RARE_EARTH_EXTRACTOR;
   }

   @Override
   protected void createInvCaches() {
      this.inOut = new IHasInventory[2];
      this.inOut[0] = new RangedInventory(this, 1);
      this.inOut[1] = new RangedInventory(this, 2).setOutputOnly();
   }

   @Override
   protected void handleMods() {
   }

   public IRareEarthRegistry getRegistry() {
      return IC2.RECIPES.get(this.isSimulating()).rare_earth;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.currentID = new ResourceLocation(compound.getString("recipe_id"));
      this.materialProgress = compound.getFloat("material");
      this.progress = compound.getFloat("progress");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putString("recipe_id", this.currentID.toString());
      compound.putFloat("material", this.materialProgress);
      compound.putFloat("progress", this.progress);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new RareEarthExtractorContainer(this, player, windowID);
   }

   @Override
   public void onTick() {
      this.handleRedstone();
      this.handleChargeSlot((int)((double)this.maxEnergy * 0.9));
      if (this.addItemsToInventory() && this.canProcess() && this.hasEnergy(this.energyConsume) && !((ItemStack)this.inventory.get(1)).isEmpty()) {
         IRareEarthRegistry.RareEntry entry = this.getRegistry().getOutputFor((ItemStack)this.inventory.get(1));
         if (entry == null || !this.canAdd(entry)) {
            if (this.progress != 0.0F) {
               this.progress = 0.0F;
               this.updateGuiField("progress");
            }

            this.setActive(false);
            return;
         }

         if (!entry.matches(this.currentID)) {
            this.currentID = entry.getID();
            this.materialProgress = 0.0F;
            this.updateGuiField("materialProgress");
            this.updateGuiField("currentID");
         }

         this.setActive(true);
         this.progress = this.progress + this.progressPerTick;
         this.useEnergy(this.energyConsume);
         if (this.progress >= 35.0F) {
            this.progress = 0.0F;
            this.materialProgress = this.materialProgress + entry.getValue();
            this.updateGuiField("materialProgress");
            ItemStack stack = (ItemStack)this.inventory.get(1);
            if (stack.hasCraftingRemainingItem()) {
               this.outputs.add(new SimpleStackOutput(stack, 1));
            } else {
               stack.shrink(1);
            }

            if (this.materialProgress >= 1000.0F) {
               this.materialProgress -= 1000.0F;
               this.outputs.add(new SimpleStackOutput(entry.getOutput().copy(), 2));
            }

            this.notifyListeners();
            this.storage.onRecipeFinishedPost(this.inventory, this, null, this.outputs);
         }

         this.updateGuiField("progress");
      } else {
         if (this.progress != 0.0F) {
            this.progress = Math.max(0.0F, ((ItemStack)this.inventory.get(1)).isEmpty() ? 0.0F : this.progress - this.progressPerTick);
            this.updateGuiField("progress");
         }

         this.setActive(false);
      }

      this.storage.onTick(this.inventory, this);
      this.handleComparators();
   }

   private boolean canAdd(IRareEarthRegistry.RareEntry entry) {
      return !(this.materialProgress + entry.getValue() < 1000.0F) && !((ItemStack)this.inventory.get(2)).isEmpty()
         ? StackUtil.isStackEqual((ItemStack)this.inventory.get(2), entry.getOutput())
            && ((ItemStack)this.inventory.get(2)).getCount() < ((ItemStack)this.inventory.get(2)).getMaxStackSize()
         : true;
   }

   @Override
   public EnumSet<IUpgradeItem.UpgradeType> getSupportedUpgradeTypes() {
      return BasicMachineTileEntity.BASIC_MACHINE_UPGRADES;
   }

   @Override
   public float getProgress() {
      return this.progress;
   }

   @Override
   public float getMaxProgress() {
      return 35.0F;
   }

   @Override
   public float getSubProgress() {
      return this.materialProgress;
   }

   @Override
   public float getMaxSubProgress() {
      return 1000.0F;
   }

   public ItemStack getCurrentOutput() {
      return this.getRegistry().getOutputFor(this.currentID);
   }

   @Override
   public int getValidRoom(ItemStack stack) {
      if (this.getRegistry().getOutputFor(stack) == null) {
         return 0;
      } else {
         ItemStack inv = (ItemStack)this.inventory.get(1);
         if (inv.isEmpty()) {
            return stack.getMaxStackSize();
         } else {
            return StackUtil.isStackEqual(inv, stack) ? StackUtil.getStackSizeLeft(inv) : 0;
         }
      }
   }

   public static void loadRecipes(IRareEarthRegistry registry) {
      registry.registerOutput(RARE_EARTH, new ItemStack(IC2Items.DUST_RARE_EARTH));
      registry.registerInput(RARE_EARTH, 3.125F, new ItemStack(Items.STONE), new ItemStack(Items.DEEPSLATE));
      registry.registerInput(
         RARE_EARTH, 62.5F, new ItemStack(Items.GRANITE), new ItemStack(Items.DIORITE), new ItemStack(Items.ANDESITE), new ItemStack(Items.TUFF)
      );
      registry.registerInput(RARE_EARTH, 7.8125F, new ItemStack(Items.NETHERRACK));
      registry.registerInput(RARE_EARTH, 100.0F, new ItemStack(Items.MYCELIUM));
      registry.registerInput(RARE_EARTH, 200.0F, new ItemStack(Items.OBSIDIAN), new ItemStack(Items.SOUL_SAND), new ItemStack(Items.SOUL_SOIL));
      registry.registerOutput(ALUMINIUM, new ItemStack(IC2Items.DUST_ALUMINIUM));
      registry.registerInput(
         ALUMINIUM,
         500.0F,
         new ItemStack(Items.CLAY_BALL),
         new ItemStack(Items.TERRACOTTA),
         new ItemStack(Items.WHITE_TERRACOTTA),
         new ItemStack(Items.ORANGE_TERRACOTTA),
         new ItemStack(Items.MAGENTA_TERRACOTTA),
         new ItemStack(Items.LIGHT_BLUE_TERRACOTTA),
         new ItemStack(Items.YELLOW_TERRACOTTA),
         new ItemStack(Items.LIME_TERRACOTTA),
         new ItemStack(Items.PINK_TERRACOTTA),
         new ItemStack(Items.GRAY_TERRACOTTA),
         new ItemStack(Items.LIGHT_GRAY_TERRACOTTA),
         new ItemStack(Items.CYAN_TERRACOTTA),
         new ItemStack(Items.PURPLE_TERRACOTTA),
         new ItemStack(Items.BLUE_TERRACOTTA),
         new ItemStack(Items.BROWN_TERRACOTTA),
         new ItemStack(Items.GREEN_TERRACOTTA),
         new ItemStack(Items.RED_TERRACOTTA),
         new ItemStack(Items.BLACK_TERRACOTTA)
      );
      registry.registerInput(ALUMINIUM, 100.0F, new ItemStack(Items.DIRT));
   }
}
