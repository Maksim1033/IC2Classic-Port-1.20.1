package ic2.core.block.machines.tiles.mv;

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
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ChargedElectrolyzerTileEntity
   extends BaseInventoryTileEntity
   implements ILocation,
   ITickListener,
   ITileGui,
   IEUStorage,
   IWrenchableTile,
   ITileActivityProvider {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/mv/gui_electrolyzer_charged.png");
   @NetworkInfo
   public int energy = 0;
   @NetworkInfo
   public int maxEnergy = 0;
   IElectrolyzerRecipeList.ElectrolyzerRecipe entry;
   InterfaceCache<IElectrolyzerProvider> providers = new InterfaceCache<>(this, DirectionList.ALL, IElectrolyzerProvider.class);

   public ChargedElectrolyzerTileEntity(BlockPos pos, BlockState state) {
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
      handler.registerInputFilter(T -> this.isValidItem(T, 0, true), 0);
      handler.registerInputFilter(T -> this.isValidItem(T, 1, false), 1);
      handler.registerNamedSlot(SlotType.INPUT, 0);
      handler.registerNamedSlot(SlotType.OUTPUT, 1);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ElectrolyzerContainer<>(this, player, windowID, TEXTURE);
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
      return 2;
   }

   public boolean isValidItem(ItemStack stack, int slot, boolean input) {
      return !((ItemStack)this.inventory.get(slot)).isEmpty()
         ? StackUtil.isStackEqual((ItemStack)this.inventory.get(slot), stack)
         : IC2.RECIPES.get(this.isSimulating()).electrolyzer.getRecipe(stack, input, false) != null;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.CHARGED_ELECTROLYZER;
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
      return 0.75;
   }

   @Override
   public void onTick() {
      if (this.providers.isEmpty()) {
         this.setActive(false);
         this.handleComparators();
      } else {
         boolean working = false;

         for (Direction dir : this.providers) {
            IElectrolyzerProvider provider = this.providers.getHandler(dir);
            if (provider != null) {
               if (this.shouldDrain(provider)) {
                  this.drain(provider);
                  working = true;
               }

               if (this.canPower(provider)) {
                  this.power(provider);
                  working = true;
               }
            }
         }

         this.setActive(working);
         this.handleComparators();
      }
   }

   public boolean shouldDrain(IElectrolyzerProvider prov) {
      if (!(this.getPowerLevel(prov) < 0.7) && !((ItemStack)this.inventory.get(0)).isEmpty()) {
         this.entry = IC2.RECIPES.get(this.isSimulating()).electrolyzer.getRecipe((ItemStack)this.inventory.get(0), true, true);
         return this.entry != null
            && StackUtil.isStackEqual((ItemStack)this.inventory.get(1), this.entry.getOutput(), 22)
            && StackUtil.getStackSizeLeft((ItemStack)this.inventory.get(1)) >= this.entry.getInput().getCount();
      } else {
         return false;
      }
   }

   public boolean shouldDrain() {
      boolean result = false;

      for (Direction dir : this.providers) {
         IElectrolyzerProvider provider = this.providers.getHandler(dir);
         if (provider != null) {
            result |= this.shouldDrain(provider);
         }
      }

      return result;
   }

   public void drain(IElectrolyzerProvider prov) {
      int needed = this.entry.getEnergy();
      if (this.maxEnergy != needed) {
         this.maxEnergy = needed;
         this.updateGuiField("maxEnergy");
      }

      this.energy = this.energy + prov.drawEnergy(Math.min(this.maxEnergy - this.energy, prov.getProcessRate()));
      this.updateGuiField("energy");
      if (this.energy >= this.maxEnergy) {
         this.energy = 0;
         ((ItemStack)this.inventory.get(0)).shrink(this.entry.getInput().getCount());
         if (((ItemStack)this.inventory.get(1)).isEmpty()) {
            this.inventory.set(1, this.entry.getOutput().copy());
         } else if (StackUtil.isStackEqual((ItemStack)this.inventory.get(1), this.entry.getOutput())
            && StackUtil.getStackSizeLeft((ItemStack)this.inventory.get(1)) >= this.entry.getInput().getCount()) {
            ((ItemStack)this.inventory.get(1)).grow(this.entry.getOutput().getCount());
         }
      }
   }

   public boolean canPower(IElectrolyzerProvider prov) {
      if (this.getPowerLevel(prov) >= 0.3) {
         return false;
      } else if (this.energy > 0) {
         return true;
      } else if (((ItemStack)this.inventory.get(1)).isEmpty()) {
         return false;
      } else {
         this.entry = IC2.RECIPES.get(this.isSimulating()).electrolyzer.getRecipe((ItemStack)this.inventory.get(1), false, true);
         return this.entry != null
            && StackUtil.isStackEqual((ItemStack)this.inventory.get(0), this.entry.getInput(), 22)
            && StackUtil.getStackSizeLeft((ItemStack)this.inventory.get(0)) >= this.entry.getOutput().getCount();
      }
   }

   public boolean canPower() {
      boolean result = false;

      for (Direction dir : this.providers) {
         IElectrolyzerProvider provider = this.providers.getHandler(dir);
         if (provider != null) {
            result |= this.canPower(provider);
         }
      }

      return result;
   }

   public void power(IElectrolyzerProvider prov) {
      if (this.energy > 0) {
         this.energy = this.energy - prov.addEnergy(Math.min(this.energy, prov.getProcessRate()));
         this.updateGuiField("energy");
      } else {
         int toAdd = this.entry.getEnergy() / 10;
         int max = toAdd * 6 + toAdd * prov.getTier();
         this.energy = max;
         this.maxEnergy = max;
         this.updateGuiFields(new String[]{"energy", "maxEnergy"});
         ((ItemStack)this.inventory.get(1)).shrink(this.entry.getOutput().getCount());
         if (((ItemStack)this.inventory.get(0)).isEmpty()) {
            this.inventory.set(0, this.entry.getInput().copy());
         } else if (StackUtil.isStackEqual((ItemStack)this.inventory.get(0), this.entry.getInput())) {
            ((ItemStack)this.inventory.get(0)).grow(this.entry.getInput().getCount());
         }
      }
   }

   protected double getPowerLevel(IElectrolyzerProvider prov) {
      return prov == null ? 0.0 : prov.getChargeLevel();
   }

   public int getTransferRate() {
      int value = 0;

      for (Direction dir : this.providers) {
         IElectrolyzerProvider provider = this.providers.getHandler(dir);
         if (provider != null) {
            value += provider.getProcessRate();
         }
      }

      return value;
   }
}
