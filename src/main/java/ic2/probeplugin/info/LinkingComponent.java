package ic2.probeplugin.info;

import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.platform.player.PlayerHandler;
import ic2.probeplugin.base.ProbeTileListener;
import java.util.List;
import java.util.Set;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LinkingComponent implements ITileInfoComponent<BaseLinkingTileEntity> {
   ProbeMode mode = ProbeMode.NORMAL;

   @Override
   public boolean isValid(ProbeMode mode, PlayerHandler handler) {
      if (ITileInfoComponent.super.isValid(mode, handler)) {
         this.mode = mode;
         return true;
      } else {
         return false;
      }
   }

   public void addInfo(IProbeInfo info, Player player, Direction dir, BaseLinkingTileEntity tile) {
      BlockEntity master = tile.getMaster();
      if (master != null) {
         List<ITileInfoComponent<?>> list = ProbeTileListener.getData((Class<? extends BlockEntity>)master.getClass());
         if (!list.isEmpty()) {
            PlayerHandler handler = PlayerHandler.getHandler(player);
            int i = 0;

            for (int m = list.size(); i < m; i++) {
               ITileInfoComponent<?> comp = list.get(i);
               if (comp.isValid(this.mode, handler)) {
                  comp.addTileInfo(info, player, dir, master);
               }
            }
         }
      }
   }

   @Override
   public void disableClasses(Set<Class<?>> toCheck) {
      toCheck.clear();
   }
}
