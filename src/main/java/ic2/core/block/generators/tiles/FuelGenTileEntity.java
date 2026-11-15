package ic2.core.block.generators.tiles;

import ic2.api.energy.tile.IEnergySource;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.tiles.impls.BaseGeneratorTileEntity;
import ic2.core.block.generators.containers.FuelGenContainer;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.filter.special.FuelFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FuelGenTileEntity extends BaseGeneratorTileEntity {
   @NetworkInfo
   int maxFuel = 0;

   public FuelGenTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 2);
      this.maxStorage = 4000;
      this.production = IC2.CONFIG.generatorOutput.get();
      this.addGuiFields(new String[]{"maxFuel"});
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, 0, 1);
      handler.registerSlotsForSide(DirectionList.DOWN, 0);
      handler.registerSlotsForSide(DirectionList.ALL, 1);
      handler.registerInputFilter(FuelFilter.WITH_LAVA, 1);
      handler.registerOutputFilter(FuelFilter.NOT_WITH_LAVA, 1);
      handler.registerInputFilter(ElectricItemFilter.CHARGE_FILTER, 0);
      handler.registerNamedSlot(SlotType.CHARGE, 0);
      handler.registerNamedSlot(SlotType.FUEL, 1);
   }

   @Override
   public IEnergySource.SourceType getSourceType() {
      return IEnergySource.SourceType.DUMB_CONSUMING;
   }

   @Override
   public int getMaxFuel() {
      return this.maxFuel;
   }

   @Override
   public ResourceLocation getProcessingSound() {
      return IC2Sounds.GENERATOR_PROCESSING;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.maxFuel = compound.getInt("max_fuel");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putInt(compound, "max_fuel", this.maxFuel, 0);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new FuelGenContainer(this, player, windowID);
   }

   @Override
   public boolean needsEnergy() {
      return this.fuel > 0;
   }

   @Override
   public float getEUProduction() {
      return this.fuel > 0 ? (float)this.production : 0.0F;
   }

   @Override
   public boolean gainFuel() {
      ItemStack stack = (ItemStack)this.inventory.get(1);
      int rawFuel = IC2.RECIPES.get(this.isSimulating()).getFuel(stack, false);
      int newFuel = rawFuel / 4;
      if (newFuel > 0) {
         this.fuel = newFuel;
         this.maxFuel = newFuel;
         this.updateGuiField("maxFuel");
         if (stack.hasCraftingRemainingItem()) {
            this.inventory.set(1, stack.getCraftingRemainingItem());
         } else {
            stack.shrink(1);
         }

         return true;
      } else if (rawFuel > 0) {
         if (stack.hasCraftingRemainingItem()) {
            this.inventory.set(1, stack.getCraftingRemainingItem());
         } else {
            stack.shrink(1);
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.GENERATOR;
   }
}
