package ic2.core.block.cables;

import ic2.api.energy.EnergyNet;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.util.DirectionList;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.redstone.IRedstoneListener;
import ic2.core.block.base.features.redstone.IRedstoneProvider;
import ic2.core.inventory.base.ICamouflageGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ComparingDetectorCableTileEntity
   extends CableTileEntity
   implements ITickListener,
   IRedstoneProvider,
   IRedstoneListener,
   ICamouflageGui,
   ITileActivityProvider {
   long lastEU = -1L;
   int value;
   @NetworkInfo
   public int minEnergy = 0;
   @NetworkInfo
   public int maxEnergy = 1;

   public ComparingDetectorCableTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 0);
      this.addGuiFields(new String[]{"minEnergy", "maxEnergy"});
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.value = compound.getInt("value");
      this.minEnergy = compound.getInt("min_value");
      this.maxEnergy = NBTUtils.getInt(compound, "max_value", 1);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putByte(compound, "value", this.value, 0);
      NBTUtils.putInt(compound, "min_value", this.minEnergy, 0);
      NBTUtils.putInt(compound, "max_value", this.maxEnergy, 1);
   }

   @Override
   public void onTick() {
      if (this.clock(20)) {
         long out = EnergyNet.INSTANCE.getStats(this).getEnergyOut();
         if (this.lastEU == -1L) {
            this.lastEU = out;
            return;
         }

         int lastValue = this.value;
         int produced = (int)((out - this.lastEU) / 20L);
         if (produced < this.minEnergy) {
            this.value = 0;
         } else if (produced > this.maxEnergy) {
            this.value = 15;
         } else {
            this.value = (int)(15.0F * ((float)(produced - this.minEnergy) / (float)(this.maxEnergy - this.minEnergy)));
         }

         this.lastEU = out;
         if (lastValue != this.value) {
            this.notifyChanges(true, DirectionList.ALL);
            this.setActive(this.value > 0);
         }
      }
   }

   @Override
   public double getConductionLoss() {
      return 0.5;
   }

   @Override
   public int getInsulationEnergyAbsorption() {
      return 9001;
   }

   @Override
   public int getInsulationBreakdownEnergy() {
      return 9001;
   }

   @Override
   public int getConductorBreakdownEnergy() {
      return 2049;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.COMPARING_CABLE;
   }

   @NotNull
   @Override
   public Component getName() {
      return IC2Blocks.COMPARING_CABLE.getName();
   }

   @Override
   public int getCommonSignalStrength(Direction side) {
      return this.value;
   }

   @Override
   public boolean canConnectToRedstone(Direction dir) {
      return true;
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      if (key == 0) {
         this.minEnergy = Math.min(Mth.clamp(value, 0, 8191), this.maxEnergy - 1);
         this.updateGuiField("minEnergy");
      } else if (key == 1) {
         this.maxEnergy = Math.max(Mth.clamp(value, 0, 8192), this.minEnergy + 1);
         this.updateGuiField("maxEnergy");
      }

      super.onClientDataReceived(entity, key, value);
   }

   @Override
   public boolean isFoamed() {
      return this.foamed > 0;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ComparingDetectorCableContainer(this, player, windowID);
   }
}
