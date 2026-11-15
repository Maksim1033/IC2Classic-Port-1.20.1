package ic2.core.networking.packets.client;

import ic2.api.network.tile.INetworkFieldProvider;
import ic2.core.IC2;
import ic2.core.inventory.container.ContainerHasGui;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.IC2Packet;
import ic2.core.networking.packets.server.PollGuiPacket;
import ic2.core.platform.player.PlayerHandler;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.Stack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PollContainerPacket extends IC2Packet {
   @Override
   public void write(FriendlyByteBuf buffer) {
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
   }

   @Override
   public void handlePacket(Player source) {
      Stack<PlayerHandler.GuiEntry> entries = PlayerHandler.getHandler(source).cachedGUIs;
      if (!entries.isEmpty() && source instanceof ServerPlayer player) {
         PlayerHandler.GuiEntry entry = entries.pop();
         player.containerCounter = entry.getContainer().containerId;
         player.containerMenu = entry.getContainer();
         player.initMenu(player.containerMenu);
         this.openContainer(player.containerMenu, player);
         if (player.containerMenu instanceof ContainerHasGui && ((ContainerHasGui)player.containerMenu).getHolder() instanceof BlockEntity) {
            BlockEntity tile = PlayerHandler.getHandler(source).trackedTile = (BlockEntity)((ContainerHasGui)player.containerMenu).getHolder();
            if (tile instanceof INetworkFieldProvider) {
               IC2.NETWORKING.get(true).sendInitialGuiData((INetworkFieldProvider)tile, player);
            }
         }

         PacketManager.INSTANCE.sendToPlayer(new PollGuiPacket(), player);
      }
   }

   private void openContainer(AbstractContainerMenu menu, Player player) {
      Set<Container> containers = new ObjectOpenHashSet();

      for (Slot slot : menu.slots) {
         if (slot.container != null) {
            containers.add(slot.container);
         }
      }

      try {
         for (Container container : containers) {
            container.startOpen(player);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }
}
