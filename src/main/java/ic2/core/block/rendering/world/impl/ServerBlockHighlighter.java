package ic2.core.block.rendering.world.impl;

import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.server.HighlightPacket;
import ic2.core.networking.packets.server.MultiHighlightPacket;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class ServerBlockHighlighter {
   public static final ServerBlockHighlighter INSTANCE = new ServerBlockHighlighter();

   public void addServerHighlight(Player player, BlockPos pos) {
      this.addServerHighlight(player, pos, 1200);
   }

   public void addServerHighlight(Player player, BlockPos pos, int ticks) {
      this.addServerHighlight(player, pos, ticks, -2130720768);
   }

   public void addServerHighlight(Player player, BlockPos pos, int ticks, int color) {
      this.addServerHighlight(player, pos, ticks, color, false);
   }

   public void addServerHighlight(Player player, BlockPos pos, int ticks, int color, boolean doubleSided) {
      HighlightPacket packet = new HighlightPacket(pos, ticks, color, doubleSided);
      if (player == null) {
         PacketManager.INSTANCE.sendToAllPlayers(packet);
      } else {
         PacketManager.INSTANCE.sendToPlayer(packet, player);
      }
   }

   public void addServerHighlight(Player player, LongList list, int ticks, int color) {
      this.addServerHighlight(player, list, ticks, color, false);
   }

   public void addServerHighlight(Player player, LongList list, int ticks, int color, boolean doubleSided) {
      MultiHighlightPacket packet = new MultiHighlightPacket(list, ticks, color, doubleSided);
      if (player == null) {
         PacketManager.INSTANCE.sendToAllPlayers(packet);
      } else {
         PacketManager.INSTANCE.sendToPlayer(packet, player);
      }
   }
}
