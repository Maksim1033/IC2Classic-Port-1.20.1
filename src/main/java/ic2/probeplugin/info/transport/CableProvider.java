package ic2.probeplugin.info.transport;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.PacketStats;
import ic2.api.energy.TransferStats;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.block.cables.CableTileEntity;
import ic2.core.utils.collection.LongAverager;
import ic2.core.utils.helpers.Formatters;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import java.util.List;
import java.util.concurrent.TimeUnit;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class CableProvider implements ITileInfoComponent<CableTileEntity> {
   static final Cache<BlockPos, CableProvider.EnergyContainer> CACHE = CacheBuilder.newBuilder()
      .expireAfterAccess(1L, TimeUnit.SECONDS)
      .maximumSize(128L)
      .build();

   public void addInfo(IProbeInfo info, Player player, Direction dir, CableTileEntity tile) {
      Panel generator = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = generator.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text("tooltip.item.ic2.eu_reader.cable_limit", tile.getConductorBreakdownEnergy() - 1);
      basicInfo.text("tooltip.item.ic2.eu_reader.cable_loss", Formatters.CABLE_LOSS_FORMAT.format(tile.getConductionLoss()));
      CableProvider.EnergyContainer result = getContainer(tile);
      if (result.getAverageOut() > 0) {
         basicInfo.padding(0, 5);
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.cable_flow", new Object[]{Formatters.EU_FORMAT.format((long)result.getAverageOut())})
               .withStyle(ChatFormatting.AQUA)
         );
         basicInfo.text(
            this.translate("tooltip.item.ic2.eu_reader.packet_flow", new Object[]{Formatters.EU_FORMAT.format((long)result.getPacketsOut())})
               .withStyle(ChatFormatting.AQUA)
         );
      }

      this.addSecurely(info, 1, generator);
   }

   public static CableProvider.EnergyContainer getContainer(IEnergyTile tile) {
      CableProvider.EnergyContainer result = (CableProvider.EnergyContainer)CACHE.getIfPresent(tile.getPosition());
      if (result == null) {
         result = new CableProvider.EnergyContainer();
         CACHE.put(tile.getPosition(), result);
      }

      result.tick(tile.getWorldObj().getGameTime(), EnergyNet.INSTANCE.getStats(tile), EnergyNet.INSTANCE.getPacketStats(tile));
      return result;
   }

   public static class EnergyContainer {
      LongAverager energyIn = new LongAverager(5);
      LongAverager energyOut = new LongAverager(5);
      LongAverager packetsIn = new LongAverager(5);
      LongAverager packetsOut = new LongAverager(5);
      long lastTime;
      long lastIn;
      long lastOut;
      long lastPacketsIn;
      long lastPacketsOut;

      public void tick(long time, TransferStats stat, List<PacketStats> stats) {
         if (this.lastTime == 0L) {
            this.lastTime = time;
            this.lastIn = stat.getEnergyIn();
            this.lastOut = stat.getEnergyOut();
            this.lastPacketsIn = this.countPackets(stats, false);
            this.lastPacketsOut = this.countPackets(stats, true);
         } else {
            long diff = time - this.lastTime;
            if (diff <= 0L) {
               return;
            }

            long in = this.countPackets(stats, false);
            long out = this.countPackets(stats, true);
            this.energyIn.addEntry((stat.getEnergyIn() - this.lastIn) / diff);
            this.energyOut.addEntry((stat.getEnergyOut() - this.lastOut) / diff);
            this.packetsIn.addEntry((in - this.lastPacketsIn) / diff);
            this.packetsOut.addEntry((out - this.lastPacketsOut) / diff);
            this.lastTime = time;
            this.lastIn = stat.getEnergyIn();
            this.lastOut = stat.getEnergyOut();
            this.lastPacketsIn = in;
            this.lastPacketsOut = out;
         }
      }

      private long countPackets(List<PacketStats> stats, boolean in) {
         long result = 0L;
         int i = 0;

         for (int m = stats.size(); i < m; i++) {
            PacketStats stat = stats.get(i);
            if (stat.isAccepting() != in) {
               result += stat.getPackets();
            }
         }

         return result;
      }

      public int getPacketsIn() {
         return (int)this.packetsIn.getAverage();
      }

      public int getPacketsOut() {
         return (int)this.packetsOut.getAverage();
      }

      public int getAverageIn() {
         return (int)this.energyIn.getAverage();
      }

      public int getAverageOut() {
         return (int)this.energyOut.getAverage();
      }
   }
}
