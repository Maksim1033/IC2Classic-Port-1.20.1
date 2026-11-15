package ic2.probeplugin.info;

import ic2.core.platform.player.PlayerHandler;
import ic2.core.utils.tooltips.ILangHelper;
import java.util.List;
import java.util.Set;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ITileInfoComponent<T> extends ILangHelper {
   default boolean isValid(ProbeMode mode, PlayerHandler handler) {
      return this.hasValidReader(handler);
   }

   default boolean hasValidReader(PlayerHandler handler) {
      return handler.hasEUReader();
   }

   default void disableClasses(Set<Class<?>> toCheck) {
   }

   default void addTileInfo(IProbeInfo info, Player player, Direction dir, BlockEntity tile) {
      this.addInfo(info, player, dir, (T)tile);
   }

   void addInfo(IProbeInfo var1, Player var2, Direction var3, T var4);

   default void addSecurely(IProbeInfo info, int index, IElement element) {
      List<IElement> elements = info.getElements();
      elements.add(Math.min(index, elements.size()), element);
   }
}
