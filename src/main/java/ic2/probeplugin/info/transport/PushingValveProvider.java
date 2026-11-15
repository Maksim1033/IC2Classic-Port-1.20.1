package ic2.probeplugin.info.transport;

import ic2.core.block.storage.tiles.tank.PushingValveTileEntity;
import ic2.core.utils.helpers.Formatters;
import ic2.probeplugin.info.ITileInfoComponent;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.components.Panel;
import ic2.probeplugin.styles.IC2Styles;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class PushingValveProvider implements ITileInfoComponent<PushingValveTileEntity> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, PushingValveTileEntity tile) {
      Panel electric = new Panel(IC2Styles.OUTER_STYLE, Panel.Type.VERTICAL);
      IExpandedProbeInfo basicInfo = electric.vertical(IC2Styles.INNER_STYLE);
      basicInfo.text(this.translate("ic2.probe.pump.pressure", new Object[]{100}));
      basicInfo.text(this.translate("ic2.probe.pump.amount", new Object[]{Formatters.EU_FORMAT.format(2000L)}));
      this.addSecurely(info, 1, electric);
   }
}
