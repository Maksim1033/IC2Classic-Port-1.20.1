package ic2.core.inventory.base;

import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.gui.ComponentContainerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHasCustomGui {
   boolean hasGui(Player var1);

   IC2Container createContainer(Player var1, int var2);

   @OnlyIn(Dist.CLIENT)
   default Screen createGui(Player player, IC2Container container) {
      return new ComponentContainerScreen((ContainerComponent<?>)container);
   }

   default void sendInitialData(IOutputBuffer buffer) {
   }

   default void receiveInitialData(String id, INetworkDataBuffer buffer, Dist target) {
   }
}
