package ic2.core.block.machines.tiles.lv;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.recipes.registries.IElectrolyzerRecipeList;
import ic2.api.tiles.IElectrolyzerProvider;
import ic2.api.tiles.readers.IEUStorage;
import ic2.api.util.DirectionList;
import ic2.api.util.ILocation;
import ic2.core.IC2;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.cache.InterfaceCache;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.EUComparator;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.block.machines.containers.lv.ElectrolyzerContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.StackUtil;
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

public class ElectrolyzerTileEntity
   extends BaseInventoryTileEntity
   implements ITickListener,
   ILocation,
   ITileGui,
   IEUStorage,
   IWrenchableTile,
   ITileActivityProvider {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/lv/gui_electrolyzer.png");
   @NetworkInfo
   public int energy = 0;
   @NetworkInfo
   public int maxEnergy = 0;
   IElectrolyzerProvider prov;
   IElectrolyzerRecipeList.ElectrolyzerRecipe entry;
   ICache<IElectrolyzerProvider> providers = new InterfaceCache<>(this, DirectionList.ALL, IElectrolyzerProvider.class);

   public ElectrolyzerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 2);
      this.addGuiFields(new String[]{"energy", "maxEnergy"});
      this.addCaches(new ICache[]{this.providers});
      this.addComparator(new EUComparator("eu_storage", ComparatorNames.EU_STORAGE, this));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, 0, 1);
      handler.registerSlotsForSide(DirectionList.UP, 0);
      handler.registerSlotsForSide(DirectionList.DOWN, 1);
      handler.registerInputFilter((S, T) -> this.isValidItem(T, S, S == 0), 0, 1);
      handler.registerNamedSlot(SlotType.INPUT, 0);
      handler.registerNamedSlot(SlotType.OUTPUT, 1);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ElectrolyzerContainer<>(this, player, windowID, TEXTURE);
   }

   @Override
   public int getMaxEU() {
      return this.maxEnergy;
   }

   @Override
   public int getStoredEU() {
      return this.energy;
   }

   @Override
   public int getTier() {
      return 1;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putInt("energy", this.energy);
      compound.putInt("maxEnergy", this.maxEnergy);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.energy = compound.getInt("energy");
      this.maxEnergy = compound.getInt("maxEnergy");
   }

   public boolean isValidItem(ItemStack stack, int slot, boolean input) {
      return !((ItemStack)this.inventory.get(slot)).isEmpty()
         ? StackUtil.isStackEqual((ItemStack)this.inventory.get(slot), stack)
         : IC2.RECIPES.get(this.isSimulating()).electrolyzer.getRecipe(stack, input, false) != null;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.ELECTROLYZER;
   }

   @Override
   public void onTick() {
      this.prov = this.getSource();
      if (this.prov == null) {
         this.setActive(false);
         this.handleComparators();
      } else {
         boolean active = false;
         if (this.shouldDrain()) {
            this.drain();
            active = true;
         }

         if (this.canPower()) {
            this.power();
            active = true;
         }

         this.setActive(active);
         this.handleComparators();
      }
   }

   public boolean shouldDrain() {
      if (!(this.getPowerLevel() < 0.7) && !((ItemStack)this.inventory.get(0)).isEmpty()) {
         this.entry = IC2.RECIPES.get(this.isSimulating()).electrolyzer.getRecipe((ItemStack)this.inventory.get(0), true, true);
         return this.entry != null && StackUtil.canFitInto((ItemStack)this.inventory.get(1), this.entry.getOutput(), 22);
      } else {
         return false;
      }
   }

   public void drain() {
      int needed = this.entry.getEnergy();
      if (this.maxEnergy != needed) {
         this.maxEnergy = needed;
         this.updateGuiField("maxEnergy");
      }

      this.energy = this.energy + this.prov.drawEnergy(Math.min(this.maxEnergy - this.energy, this.prov.getProcessRate()));
      this.updateGuiField("energy");
      if (this.energy >= this.maxEnergy) {
         this.energy = 0;
         ((ItemStack)this.inventory.get(0)).shrink(this.entry.getInput().getCount());
         this.setOrGrow(1, this.entry.getOutput().copy(), true);
      }
   }

   public boolean canPower() {
      if (this.getPowerLevel() >= 0.3) {
         return false;
      } else if (this.energy > 0) {
         return true;
      } else if (((ItemStack)this.inventory.get(1)).isEmpty()) {
         return false;
      } else {
         this.entry = IC2.RECIPES.get(this.isSimulating()).electrolyzer.getRecipe((ItemStack)this.inventory.get(1), false, true);
         return this.entry != null && StackUtil.canFitInto((ItemStack)this.inventory.get(0), this.entry.getInput(), 22);
      }
   }

   public void power() {
      if (this.energy > 0) {
         this.energy = this.energy - this.prov.addEnergy(Math.min(this.energy, this.prov.getProcessRate()));
         this.updateGuiField("energy");
      } else {
         int toAdd = this.entry.getEnergy() / 10;
         int max = toAdd * 6 + toAdd * this.prov.getTier();
         this.energy = max;
         this.maxEnergy = max;
         this.updateGuiFields(new String[]{"energy", "maxEnergy"});
         ((ItemStack)this.inventory.get(1)).shrink(this.entry.getOutput().getCount());
         this.setOrGrow(0, this.entry.getInput().copy(), true);
      }
   }

   protected double getPowerLevel() {
      return this.prov == null ? 0.0 : this.prov.getChargeLevel();
   }

   public int getTransferRate() {
      return this.prov == null ? 0 : this.prov.getProcessRate();
   }

   public IElectrolyzerProvider getSource() {
      return this.providers.isEmpty() ? null : this.providers.getHandler(this.providers.getPresentSides().getDefaultFacing());
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
      return 0.8;
   }

   public static void loadRecipe(IElectrolyzerRecipeList list) {
      list.addDualRecipe(
         new ResourceLocation("ic2:water_electrolyzation"), new ItemStack(IC2Items.CELL_WATER), new ItemStack(IC2Items.CELL_ELECTROLYZED_WATER), 20000
      );
      list.addChargeRecipe(new ResourceLocation("ic2:magnet_charging"), new ItemStack(IC2Items.DEAD_MAGNET), new ItemStack(IC2Items.MAGNET), 20000);
      list.addDischargeRecipe(new ResourceLocation("ic2:dio_quartz"), new ItemStack(Items.QUARTZ), new ItemStack(Items.DIORITE), 1000);
      list.addDischargeRecipe(new ResourceLocation("ic2:gran_quartz"), new ItemStack(Items.QUARTZ), new ItemStack(Items.GRANITE), 1000);
      list.addDischargeRecipe(new ResourceLocation("ic2:and_quartz"), new ItemStack(Items.QUARTZ), new ItemStack(Items.ANDESITE), 1000);
   }
}
