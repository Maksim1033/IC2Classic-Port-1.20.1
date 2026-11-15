package ic2.core.block.generators.tiles;

import ic2.api.energy.tile.IEnergySource;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.readers.IWorkProvider;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.IAreaOfEffect;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.misc.SubProduction;
import ic2.core.block.base.tiles.impls.BaseGeneratorTileEntity;
import ic2.core.block.generators.containers.ThermalGenContainer;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.FluidFilter;
import ic2.core.inventory.filter.InvertedFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.platform.corehacks.mixins.server.BiomeMixin;
import ic2.core.platform.registries.IC2Fluids;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.AABBUtil;
import ic2.core.utils.helpers.FluidHelper;
import ic2.core.utils.helpers.NBTUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class ThermalGeneratorTileEntity extends BaseGeneratorTileEntity implements IFluidHandler, IWorkProvider, IClickable, IAreaOfEffect {
   static final int MAX_FUEL = 30000000;
   static final int NEED_FUEL = 29000000;
   static final int FUEL_PER_BUCKET = 1500000;
   static final int FUEL_PER_MB = 1500;
   boolean thermalEnergy = false;
   @NetworkInfo
   public SubProduction subProduction = new SubProduction();
   int visualId = -1;

   public ThermalGeneratorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3);
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, this);
      this.production = IC2.CONFIG.thermalGenerator.getProduction();
      this.maxStorage = 8000;
      this.tier = 2;
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerSlotAccess(AccessRule.BOTH, 0, 1, 2);
      handler.registerSlotsForSide(DirectionList.DOWN, 0);
      handler.registerSlotsForSide(DirectionList.ALL, 1, 2);
      handler.registerInputFilter(new FluidFilter(Fluids.LAVA), 1);
      handler.registerOutputFilter(SpecialFilters.ALWAYS_FALSE, 1);
      handler.registerOutputFilter(new InvertedFilter(new FluidFilter(Fluids.LAVA)), 2);
      handler.registerInputFilter(SpecialFilters.ALWAYS_FALSE, 2);
      handler.registerInputFilter(ElectricItemFilter.CHARGE_FILTER, 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_CHARGE_FILTER, 0);
      handler.registerNamedSlot(SlotType.CHARGE, 0);
      handler.registerNamedSlot(SlotType.FUEL, 1);
      handler.registerNamedSlot(SlotType.OUTPUT, 2);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putBoolean(compound, "thermal", this.thermalEnergy, false);
      compound.putBoolean("migrated", true);
      NBTUtils.put(compound, "sub", this.subProduction.save(new CompoundTag()));
   }

   @Override
   public IEnergySource.SourceType getSourceType() {
      return IEnergySource.SourceType.INTELLIGENT_CONSUMING;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.thermalEnergy = compound.getBoolean("thermal");
      this.subProduction.load(compound.getCompound("sub"));
      if (!compound.getBoolean("migrated")) {
         this.fuel *= 1000;
      }
   }

   @Override
   public ResourceLocation getProcessingSound() {
      return IC2Sounds.GEOTHERMAL_OPERATING;
   }

   @Override
   protected boolean isAudioActive() {
      return super.isAudioActive() && !this.isThermalEnergy();
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      return FluidHelper.drainContainers(player.getItemInHand(hand), player, this);
   }

   @Override
   public boolean hasAverageActivity() {
      return true;
   }

   @Override
   public int getDelay() {
      return 10;
   }

   @Override
   public AABB getAreaOfEffect() {
      return new AABB(this.worldPosition).inflate(7.0);
   }

   @Override
   public int getAreaOfEffectColor() {
      return -2132855788;
   }

   @Override
   public void setVisualizationId(int id) {
      this.visualId = id;
   }

   @Override
   public int getVisualizationId() {
      return this.visualId;
   }

   public boolean isThermalEnergy() {
      return this.thermalEnergy;
   }

   @Override
   public boolean isWorking() {
      return this.subProduction.getProduction() > 0;
   }

   public void setThermalEnergy(boolean value) {
      if (value != this.thermalEnergy) {
         this.thermalEnergy = value;
         this.onStateChanged();
      }
   }

   @Override
   public int getMaxFuel() {
      return 20000;
   }

   @Override
   public int getFuel() {
      return super.getFuel() / 1500;
   }

   @Override
   public boolean needsFuel() {
      return this.fuel <= 29000000;
   }

   @Override
   public float getEUProduction() {
      return this.fuel > 0 ? (float)(this.production * 3) : this.subProduction.getProduction((float)IC2.CONFIG.thermalGenerator.getPassiveProduction());
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ThermalGenContainer(this, player, windowID);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.GENERATOR_THERMAL;
   }

   @Override
   public boolean gainFuel() {
      return 30000000 - this.fuel >= 1500000 ? FluidHelper.drainContainers(this, 1, 2, this) : false;
   }

   @Override
   public int getMaxEnergyOutput() {
      return this.production * 3;
   }

   @Override
   public int getProvidedEnergy() {
      return Math.min(this.storage, this.production * 3);
   }

   @Override
   public boolean needsEnergy() {
      return this.fuel > 0 && this.storage + this.production * 3 <= this.maxStorage;
   }

   @Override
   protected void consumeFuel() {
   }

   @Override
   public boolean gainEnergy() {
      if (this.needsEnergy()) {
         int toConsume = Math.min(this.fuel, 4500);
         this.fuel -= toConsume;
         this.storage = Math.min(this.maxStorage, this.storage + this.production * Mth.ceil((float)toConsume / 1500.0F));
         this.consumeFuel();
         this.setThermalEnergy(false);
         return true;
      } else {
         if (this.clock(200) || this.subProduction.needsInit()) {
            this.updateProduction();
         }

         if (this.storage < this.maxStorage) {
            this.updateGuiField("subProduction");
            int heatRequired = IC2.CONFIG.thermalGenerator.getPassiveProduction();
            if (this.subProduction.update(heatRequired)) {
               this.storage = this.storage + this.subProduction.consume(heatRequired, true);
               this.updateGuiField("storage");
            }

            this.setThermalEnergy(this.subProduction.getProduction() > 0);
            return true;
         } else {
            return false;
         }
      }
   }

   public void updateProduction() {
      AABBUtil.createOffThreadTask(this.getLevel(), this.getBlockPos(), 7, new ThermalGeneratorTileEntity.Mapper(), 35, DirectionList.ALL, T -> {
         int heatPoints = 0;
         ObjectIterator var3 = Object2ObjectMaps.fastIterable(T.getMappedPositions()).iterator();

         while (var3.hasNext()) {
            Entry<Block, List<BlockPos>> entry = (Entry<Block, List<BlockPos>>)var3.next();
            Block block = (Block)entry.getKey();
            if (block == Blocks.FIRE) {
               heatPoints += ((List)entry.getValue()).size() * 3;
            } else if (block == Blocks.LAVA) {
               heatPoints += ((List)entry.getValue()).size() * 10;
            } else if (block == Blocks.MAGMA_BLOCK) {
               heatPoints += ((List)entry.getValue()).size() * 7;
            } else if (block == Blocks.OBSIDIAN) {
               heatPoints += ((List)entry.getValue()).size() * 5;
            } else if (block == Blocks.STONE) {
               heatPoints += ((List)entry.getValue()).size() * 2;
            } else if (block == Blocks.WATER) {
               heatPoints += ((List)entry.getValue()).size() * -3;
            } else if (block == Blocks.SNOW) {
               heatPoints += ((List)entry.getValue()).size() * -5;
            } else if (block == Blocks.ICE) {
               heatPoints += ((List)entry.getValue()).size() * -10;
            }
         }

         this.subProduction.setProduction((int)Math.max(0.0F, (float)heatPoints * this.getBiomeBuff()));
         this.updateGuiField("subProduction");
      });
   }

   public float getBiomeBuff() {
      return Mth.clamp(((BiomeMixin)(Object)this.getLevel().getBiome(this.getBlockPos()).value()).getTemp(this.getBlockPos()), 0.5F, 1.5F);
   }

   public int getTanks() {
      return 1;
   }

   public FluidStack getFluidInTank(int tank) {
      return new FluidStack(Fluids.LAVA, this.fuel / 1500);
   }

   public int getTankCapacity(int tank) {
      return 20000;
   }

   public boolean isFluidValid(int tank, FluidStack stack) {
      return stack.getFluid() == Fluids.LAVA || stack.getFluid() == IC2Fluids.BLAZING_LAVA;
   }

   public int fill(FluidStack resource, FluidAction action) {
      if (resource.getFluid() == Fluids.LAVA) {
         int mbFree = (30000000 - this.fuel) / 1500;
         if (mbFree <= 0) {
            return 0;
         } else {
            int consumed = Math.min(mbFree, resource.getAmount());
            if (action.execute() && consumed > 0) {
               this.fuel += consumed * 1500;
               this.updateGuiField("fuel");
            }

            return consumed;
         }
      } else if (resource.getFluid() == IC2Fluids.BLAZING_LAVA) {
         int mbFree = (30000000 - this.fuel) / 1500 / 5;
         if (mbFree <= 0) {
            return 0;
         } else {
            int consumed = Math.min(mbFree, resource.getAmount());
            if (action.execute() && consumed > 0) {
               this.fuel += consumed * 1500 * 5;
               this.updateGuiField("fuel");
            }

            return consumed;
         }
      } else {
         return 0;
      }
   }

   public FluidStack drain(FluidStack resource, FluidAction action) {
      return FluidStack.EMPTY;
   }

   public FluidStack drain(int maxDrain, FluidAction action) {
      return FluidStack.EMPTY;
   }

   static class Mapper implements AABBUtil.IBlockMapper {
      @Override
      public boolean isValid(BlockState state) {
         return true;
      }

      @Override
      public void mapBlocks(LevelReader reader, BlockPos pos, Map<Block, List<BlockPos>> mapper) {
         BlockState state = reader.getBlockState(pos);
         Block block = state.getBlock();
         Fluid fluid = state.getFluidState().getType();
         if (fluid == Fluids.FLOWING_LAVA) {
            AABBUtil.mapResult(Blocks.FIRE, pos, mapper);
         } else if (fluid == Fluids.LAVA || state.is(BlockTags.FIRE)) {
            AABBUtil.mapResult(Blocks.LAVA, pos, mapper);
         } else if (block == Blocks.MAGMA_BLOCK) {
            AABBUtil.mapResult(Blocks.MAGMA_BLOCK, pos, mapper);
         } else if (block == Blocks.OBSIDIAN) {
            AABBUtil.mapResult(Blocks.OBSIDIAN, pos, mapper);
         } else if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            AABBUtil.mapResult(Blocks.STONE, pos, mapper);
         } else if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
            AABBUtil.mapResult(Blocks.WATER, pos, mapper);
         } else if (state.is(BlockTags.SNOW)) {
            AABBUtil.mapResult(Blocks.SNOW, pos, mapper);
         } else if (state.is(BlockTags.ICE)) {
            AABBUtil.mapResult(Blocks.ICE, pos, mapper);
         }
      }
   }
}
