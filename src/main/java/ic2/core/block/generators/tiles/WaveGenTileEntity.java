package ic2.core.block.generators.tiles;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.tiles.readers.IEUProducer;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.AABBUtil;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.math.MathUtils;
import ic2.core.utils.math.geometry.Box;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WaveGenTileEntity extends BaseTileEntity implements IEnergySource, ITickListener, IWrenchableTile, IEUProducer, ITileActivityProvider {
   public int storage = 0;
   public int production = 0;
   private int targetProduction = 0;
   boolean addedToEnet = false;

   public WaveGenTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   @Override
   public void onTick() {
      if (this.clock(256)) {
         this.updateProduction();
      }

      if (this.targetProduction != this.production) {
         this.production = this.production + MathUtils.sign(this.targetProduction - this.production);
      }

      if (this.production > 0 && this.storage < 1000) {
         this.storage = Math.min(1000, this.storage + this.production);
      }

      this.setActive(this.production > 0);
   }

   @Override
   public float getEUProduction() {
      return (float)this.production;
   }

   public void updateProduction() {
      double distance = (double)AABBUtil.countBlocks(
         this.getLevel(),
         this.getBlockPos(),
         Box.withRange(this.getBlockPos(), 1).expand(DirectionList.ofFacing(this.getFacing()), 50),
         WaterMillTileEntity.WATER_FILTER,
         0,
         DirectionList.ofFacing(this.getFacing()),
         50
      );
      if (this.getLevel().isRaining()) {
         distance *= 1.2;
      }

      if (this.getLevel().isThundering()) {
         distance *= 4.0;
      }

      if (this.getLevel().random.nextInt(3) != 0) {
         distance *= 0.1 + this.getLevel().random.nextDouble();
      }

      this.targetProduction = (int)(Math.max(0.0, distance) / 50.0 * (double)IC2.CONFIG.waveGenOutput.get());
   }

   @Override
   public IEnergySource.SourceType getSourceType() {
      return IEnergySource.SourceType.PASSIVE_PRODUCING;
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating() && !this.addedToEnet) {
         this.addedToEnet = true;
         EnergyNet.INSTANCE.addTile(this);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.addedToEnet) {
         this.addedToEnet = false;
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
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putShort(compound, "storage", this.storage, 0);
      NBTUtils.putShort(compound, "production", this.production, 15);
      NBTUtils.putShort(compound, "target", this.targetProduction, 15);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.storage = compound.getInt("storage");
      this.production = NBTUtils.getInt(compound, "production", 15);
      this.targetProduction = NBTUtils.getInt(compound, "target", 15);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.GENERATOR_WAVE_GEN;
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
   public int getSourceTier() {
      return 2;
   }

   @Override
   public int getMaxEnergyOutput() {
      return 128;
   }

   @Override
   public int getProvidedEnergy() {
      return Math.min(this.storage, 128);
   }

   @Override
   public void consumeEnergy(int consumed) {
      this.storage -= consumed;
   }
}
