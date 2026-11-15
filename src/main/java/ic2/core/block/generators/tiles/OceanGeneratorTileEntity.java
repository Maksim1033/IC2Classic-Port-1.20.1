package ic2.core.block.generators.tiles;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.tiles.readers.IEUProducer;
import ic2.api.tiles.readers.IEUStorage;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.AABBUtil;
import ic2.core.utils.helpers.NBTUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class OceanGeneratorTileEntity
   extends BaseTileEntity
   implements IEnergySource,
   IEUStorage,
   ITickListener,
   AABBUtil.IBlockMapper,
   Consumer<AABBUtil.SearchResult>,
   IWrenchableTile,
   IEUProducer,
   ITileActivityProvider {
   static final Predicate<BlockState> CORAL = T -> T.is(BlockTags.CORALS) || T.is(BlockTags.CORAL_BLOCKS) || T.is(BlockTags.WALL_CORALS);
   public int storage = 0;
   boolean checking = false;
   public int waterFound = 0;
   public int coralsFound = 0;
   public int production = 0;
   boolean addedToEnet = false;

   public OceanGeneratorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putShort(compound, "energy", this.storage, 0);
      NBTUtils.putShort(compound, "production", this.production, 0);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.storage = compound.getInt("energy");
      this.production = compound.getInt("production");
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.GENERATOR_OCEAN;
   }

   @Override
   public boolean canSetFacing(Direction dir) {
      return this.getFacing() != dir && dir.getAxis().isHorizontal();
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
   public boolean canEmitEnergy(IEnergyAcceptor acceptor, Direction side) {
      return true;
   }

   @Override
   public IEnergySource.SourceType getSourceType() {
      return IEnergySource.SourceType.PASSIVE_PRODUCING;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating() && !this.addedToEnet) {
         EnergyNet.INSTANCE.addTile(this);
         this.addedToEnet = true;
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.addedToEnet) {
         EnergyNet.INSTANCE.removeTile(this);
         this.addedToEnet = false;
      }

      super.onUnloaded(chunk);
   }

   @Override
   public float getEUProduction() {
      return (float)this.production;
   }

   @Override
   public void onTick() {
      if (!this.checking && this.clock(512)) {
         this.checking = true;
         AABBUtil.createOffThreadTask(this.getLevel(), this.getBlockPos(), 5, this, 33, DirectionList.ALL, this);
      }

      if (this.production > 0 && this.storage < this.getMaxEU()) {
         this.storage = Math.min(this.storage + this.production, this.getMaxEU());
      }

      this.setActive(this.production > 0);
   }

   public void accept(AABBUtil.SearchResult t) {
      this.checking = false;
      this.coralsFound = 0;
      this.waterFound = 0;
      int productionPenalty = 1;
      ObjectIterator var3 = Object2ObjectMaps.fastIterable(t.getMappedPositions()).iterator();

      while (var3.hasNext()) {
         Entry<Block, List<BlockPos>> entry = (Entry<Block, List<BlockPos>>)var3.next();
         if (entry.getKey() == Blocks.WATER) {
            this.waterFound = this.waterFound + ((List)entry.getValue()).size();
         } else if (CORAL.test(((Block)entry.getKey()).defaultBlockState())) {
            this.coralsFound = this.coralsFound + ((List)entry.getValue()).size();
         }

         if (entry.getKey() == IC2Blocks.OCEAN_GENERATOR) {
            productionPenalty = Math.max(((List)entry.getValue()).size(), 1);
         }
      }

      this.production = (int)(
         Math.max(
               0.0F,
               Math.min(1.0F, (float)this.waterFound * 0.001F)
                  * Math.min((float)(this.coralsFound - 25) * 0.04F, 1.0F)
                  * (float)IC2.CONFIG.oceanGenOutput.get()
            )
            / (float)productionPenalty
      );
   }

   @Override
   public int getStoredEU() {
      return this.storage;
   }

   @Override
   public int getMaxEU() {
      return 8000;
   }

   @Override
   public int getTier() {
      return 2;
   }

   @Override
   public int getSourceTier() {
      return 2;
   }

   @Override
   public int getMaxEnergyOutput() {
      return 100;
   }

   @Override
   public int getProvidedEnergy() {
      return Math.min(100, this.storage);
   }

   @Override
   public void consumeEnergy(int consumed) {
      this.storage -= consumed;
   }

   @Override
   public boolean isValid(LevelReader world, BlockPos pos) {
      return this.isValid(world.getBlockState(pos));
   }

   @Override
   public boolean isValid(BlockState state) {
      return CORAL.test(state) || state.getFluidState().getType() == Fluids.WATER || state.getBlock() == IC2Blocks.OCEAN_GENERATOR;
   }

   @Override
   public void mapBlocks(LevelReader reader, BlockPos pos, Map<Block, List<BlockPos>> mapper) {
      BlockState state = reader.getBlockState(pos);
      if (state.getFluidState().getType() == Fluids.WATER) {
         AABBUtil.mapResult(Blocks.WATER, pos, mapper);
      }

      if (CORAL.test(state) || state.getBlock() == IC2Blocks.OCEAN_GENERATOR) {
         AABBUtil.mapResult(state.getBlock(), pos, mapper);
      }
   }
}
