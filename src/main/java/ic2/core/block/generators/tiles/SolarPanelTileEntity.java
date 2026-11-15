package ic2.core.block.generators.tiles;

import carbonconfiglib.config.ConfigEntry.IntValue;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.items.electric.ElectricItem;
import ic2.api.tiles.readers.IEUProducer;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.block.generators.BaseCompressedGeneratorBlock;
import ic2.core.block.generators.containers.SolarPanelContainer;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SolarPanelTileEntity
   extends BaseInventoryTileEntity
   implements ITickListener,
   IEnergySource,
   ITileGui,
   IWrenchableTile,
   IEUProducer,
   ITileActivityProvider {
   boolean addedToEnergyNet = false;
   int storage = 0;
   int production;
   public int tier;
   public boolean charge_slot = false;

   public SolarPanelTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 1);
      this.production = this.getProduction().get();
      this.tier = 1;
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.UP.invert());
      handler.registerBlockAccess(DirectionList.UP.invert(), AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, 0);
      handler.setSlotAccess(0, Direction.UP, AccessRule.DISABLED);
      handler.registerSlotsForSide(DirectionList.UP.invert(), 0);
      handler.registerInputFilter(ElectricItemFilter.CHARGE_FILTER, 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_CHARGE_FILTER, 0);
      handler.registerNamedSlot(SlotType.CHARGE, 0);
   }

   public IntValue getProduction() {
      return IC2.CONFIG.solarPanel;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.charge_slot = compound.getBoolean("charge_slot");
      this.storage = compound.getInt("storage");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putBoolean(compound, "charge_slot", this.charge_slot, false);
      NBTUtils.putInt(compound, "storage", this.storage, 0);
   }

   @Override
   public void onTick() {
      if (this.clock(128)) {
         this.setActive(isSunVisible(this.getLevel(), this.getBlockPos().above()));
      }

      if (this.isActive()) {
         this.storage = this.production;
      }

      if (this.storage > 0 && this.charge_slot && !((ItemStack)this.inventory.get(0)).isEmpty()) {
         int change = ElectricItem.MANAGER.charge((ItemStack)this.inventory.get(0), this.storage, this.tier, false, false);
         this.storage -= change;
         if (change <= 0) {
            this.charge_slot = false;
         }
      }

      this.handleComparators();
   }

   @Override
   public float getEUProduction() {
      return this.isActive() ? (float)this.production : 0.0F;
   }

   @Override
   public IEnergySource.SourceType getSourceType() {
      return IEnergySource.SourceType.PASSIVE_PRODUCING;
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.isSimulating() && slot == 0) {
         this.charge_slot = !stack.isEmpty();
      }
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new SolarPanelContainer(this, player, windowID);
   }

   @Override
   public boolean canEmitEnergy(IEnergyAcceptor acceptor, Direction side) {
      return this.getBlockState().getBlock() instanceof BaseCompressedGeneratorBlock ? side == Direction.DOWN : side != Direction.UP;
   }

   @Override
   public int getSourceTier() {
      return this.tier;
   }

   @Override
   public int getMaxEnergyOutput() {
      return this.production;
   }

   @Override
   public int getProvidedEnergy() {
      return this.isActive() ? this.storage : 0;
   }

   @Override
   public void consumeEnergy(int consumed) {
      this.storage -= consumed;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating() && !this.addedToEnergyNet) {
         this.addedToEnergyNet = true;
         EnergyNet.INSTANCE.addTile(this);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.addedToEnergyNet) {
         this.addedToEnergyNet = false;
         EnergyNet.INSTANCE.removeTile(this);
      }

      super.onUnloaded(chunk);
   }

   @Override
   public void setFacing(Direction facing) {
      super.setFacing(facing);
      EnergyNet.INSTANCE.updateTile(this);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.GENERATOR_SOLAR_PANEL;
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

   public static boolean isSunVisible(Level world, BlockPos pos) {
      if (!world.dimensionType().hasSkyLight() || !world.isDay()) {
         return false;
      } else if (!world.canSeeSkyFromBelowWater(pos)) {
         return false;
      } else {
         Biome biome = (Biome)world.getBiome(pos).value();
         return biome.getPrecipitationAt(pos) == Precipitation.NONE ? true : !world.isRaining() && !world.isThundering();
      }
   }

   public static class HVSolarPanelTileEntity extends SolarPanelTileEntity {
      public HVSolarPanelTileEntity(BlockPos pos, BlockState state) {
         super(pos, state);
         this.tier = 3;
      }

      @Override
      public IntValue getProduction() {
         return IC2.CONFIG.hvSolarPanel;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.GENERATOR_SOLAR_PANEL_HV;
      }

      @Override
      public double getDropRate(Player player) {
         return 1.0;
      }

      @Override
      public boolean isHarvestWrenchRequired(Player player) {
         return false;
      }
   }

   public static class LVSolarPanelTileEntity extends SolarPanelTileEntity {
      public LVSolarPanelTileEntity(BlockPos pos, BlockState state) {
         super(pos, state);
         this.tier = 1;
      }

      @Override
      public IntValue getProduction() {
         return IC2.CONFIG.lvSolarPanel;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.GENERATOR_SOLAR_PANEL_LV;
      }

      @Override
      public double getDropRate(Player player) {
         return 1.0;
      }

      @Override
      public boolean isHarvestWrenchRequired(Player player) {
         return false;
      }
   }

   public static class MVSolarPanelTileEntity extends SolarPanelTileEntity {
      public MVSolarPanelTileEntity(BlockPos pos, BlockState state) {
         super(pos, state);
         this.tier = 2;
      }

      @Override
      public IntValue getProduction() {
         return IC2.CONFIG.mvSolarPanel;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.GENERATOR_SOLAR_PANEL_MV;
      }

      @Override
      public double getDropRate(Player player) {
         return 1.0;
      }

      @Override
      public boolean isHarvestWrenchRequired(Player player) {
         return false;
      }
   }
}
