package ic2.probeplugin.info.generators;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.probeplugin.info.ITileInfoComponent;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public class ReactorChamberComponent implements ITileInfoComponent<IReactorChamber> {
   public void addInfo(IProbeInfo info, Player player, Direction dir, IReactorChamber tile) {
      IReactor reactor = tile.getReactor();
      if (reactor != null) {
         NuclearReactorComponent.INSTANCE.addInfo(info, player, dir, reactor);
      }
   }
}
