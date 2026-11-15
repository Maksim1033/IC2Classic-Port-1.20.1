package ic2.core.inventory.base;

import ic2.core.inventory.container.IC2Container;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IHasGui {
   default boolean hasGui(Player player, InteractionHand hand, Direction side) {
      return true;
   }

   IC2Container createContainer(Player var1, InteractionHand var2, Direction var3, int var4);

   @OnlyIn(Dist.CLIENT)
   Screen createGui(Player var1, InteractionHand var2, Direction var3, IC2Container var4);

   boolean canInteractWith(Player var1);

   default void onGuiClosed(Player player) {
   }
}
