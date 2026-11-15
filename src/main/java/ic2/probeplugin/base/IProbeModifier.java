package ic2.probeplugin.base;

import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface IProbeModifier {
   default void modifyData(ProbeMode probe, IProbeInfo info, Player player, Level Level, BlockState state, IProbeHitData hitData) {
   }

   default boolean hasOverrideStandardInfo() {
      return false;
   }

   default void overrideStandardInfo(
      IProbeConfig config,
      ProbeMode mode,
      IProbeInfo probeInfo,
      BlockState blockState,
      Block block,
      Level Level,
      BlockPos pos,
      Player player,
      IProbeHitData data
   ) {
   }
}
