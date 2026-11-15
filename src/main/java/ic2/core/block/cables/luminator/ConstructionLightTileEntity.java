package ic2.core.block.cables.luminator;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.impl.LinkedSink;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMultiEnergyTile;
import ic2.api.tiles.readers.IEUStorage;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public class ConstructionLightTileEntity
   extends BaseTileEntity
   implements ITickListener,
   IEnergySink,
   IMultiEnergyTile,
   IWrenchableTile,
   IEUStorage,
   ITileActivityProvider {
   int energy = 0;
   boolean addedToEnet;

   public ConstructionLightTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putInt(compound, "energy", this.energy, 50);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.energy = NBTUtils.getInt(compound, "energy", 50);
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("isActive")) {
         this.getLevel().getChunkSource().getLightEngine().checkBlock(this.getBlockPos());
         IC2.PLATFORM.markBlockForRenderUpdate(this.getBlockPos());
      }
   }

   @Override
   public void onTick() {
      if (this.clock(10)) {
         if (this.energy > 0) {
            if (!this.isActive()) {
               this.energy--;
            }

            this.setActive(true);
            this.energy--;
         } else {
            this.setActive(false);
         }
      }
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.CONSTRUCTION_LIGHT;
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
      return 1.0;
   }

   @Override
   public boolean canAcceptEnergy(IEnergyEmitter emitter, Direction side) {
      return false;
   }

   @Override
   public int getSinkTier() {
      return 4;
   }

   @Override
   public int getRequestedEnergy() {
      return 50 - this.energy;
   }

   @Override
   public int acceptEnergy(Direction side, int amount, int voltage) {
      if (amount <= 0) {
         return 0;
      } else {
         int added = Math.min(amount, 50 - this.energy);
         if (added > 0) {
            this.energy += added;
         }

         return amount - added;
      }
   }

   @Override
   public List<IEnergyTile> getTiles() {
      List<IEnergyTile> tiles = CollectionUtils.<IEnergyTile>createList();
      tiles.add(this);
      tiles.add(new LinkedSink(this.getLevel(), this.getBlockPos().below(), DirectionList.DOWN.getCode()));
      return tiles;
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      if (this.getBlockState().hasProperty(BlockStateProperties.WATERLOGGED) && (Boolean)this.getBlockState().getValue(BlockStateProperties.WATERLOGGED)) {
         this.getLevel().scheduleTick(this.getBlockPos(), Fluids.WATER, Fluids.WATER.getTickDelay(this.getLevel()));
      } else if (this.getBlockState().hasProperty(IC2Properties.LAVA_LOGGED) && (Boolean)this.getBlockState().getValue(IC2Properties.LAVA_LOGGED)) {
         this.getLevel().scheduleTick(this.getBlockPos(), Fluids.LAVA, Fluids.LAVA.getTickDelay(this.getLevel()));
      }
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (!this.addedToEnet && IC2.PLATFORM.isSimulating()) {
         this.addedToEnet = true;
         EnergyNet.INSTANCE.addTile(this);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.addedToEnet && IC2.PLATFORM.isSimulating()) {
         this.addedToEnet = false;
         EnergyNet.INSTANCE.removeTile(this);
      }

      super.onUnloaded(chunk);
   }

   @Override
   public int getStoredEU() {
      return this.energy;
   }

   @Override
   public int getMaxEU() {
      return 50;
   }

   @Override
   public int getTier() {
      return this.getSinkTier();
   }
}
