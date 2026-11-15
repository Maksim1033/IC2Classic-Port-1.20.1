package ic2.core.inventory.base;

import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.gui.ComponentContainerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ITileGui extends IHasGui {
   @Override
   default boolean canInteractWith(Player player) {
      return !((BlockEntity)this).isRemoved();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   default Screen createGui(Player player, InteractionHand hand, Direction side, IC2Container container) {
      return new ComponentContainerScreen((ContainerComponent<?>)container);
   }
}
