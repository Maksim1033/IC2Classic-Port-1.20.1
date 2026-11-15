package ic2.api.energy;

import ic2.api.energy.tile.IEnergyTile;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IEnergyNet {
   IEnergyTile getTile(Level var1, BlockPos var2);

   IEnergyTile getSubTile(Level var1, BlockPos var2);

   IEnergyNet.GridTile getTiles(Level var1, BlockPos var2);

   void addTile(IEnergyTile var1);

   void removeTile(IEnergyTile var1);

   void updateTile(IEnergyTile var1);

   int getPowerFromTier(int var1);

   int getTierFromPower(int var1);

   String getDisplayTier(int var1);

   TransferStats getStats(IEnergyTile var1);

   List<PacketStats> getPacketStats(IEnergyTile var1);

   public static class GridTile {
      IEnergyTile mainTile;
      IEnergyTile subTile;

      public GridTile(IEnergyTile mainTile, IEnergyTile subTile) {
         this.mainTile = mainTile;
         this.subTile = subTile;
      }

      public IEnergyTile getMainTile() {
         return this.mainTile;
      }

      public IEnergyTile getSubTile() {
         return this.subTile;
      }

      public BlockPos getPos() {
         return this.subTile.getPosition();
      }

      public Level getWorld() {
         return this.subTile.getWorldObj();
      }
   }
}
