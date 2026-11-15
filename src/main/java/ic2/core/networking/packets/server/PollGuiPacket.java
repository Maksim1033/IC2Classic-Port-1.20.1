package ic2.core.networking.packets.server;

import ic2.core.networking.packets.IC2Packet;
import ic2.core.platform.player.PlayerHandler;
import java.util.Stack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class PollGuiPacket extends IC2Packet {
   @Override
   public void write(FriendlyByteBuf buffer) {
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
   }

   @Override
   public void handlePacket(Player source) {
      Stack<PlayerHandler.GuiEntry> entries = PlayerHandler.getClientHandler().cachedGUIs;
      if (!entries.isEmpty()) {
         PlayerHandler.GuiEntry entry = entries.pop();
         source.containerMenu = entry.getContainer();
         if (entry.getScreen() != null) {
            Minecraft.getInstance().setScreen(entry.getScreen());
         }
      }
   }
}
