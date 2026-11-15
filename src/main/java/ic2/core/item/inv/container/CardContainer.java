package ic2.core.item.inv.container;

import ic2.core.inventory.container.ItemContainer;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.item.inv.inventory.CardInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CardContainer extends ItemContainer<CardInventory> {
   public CardContainer(CardInventory key, Player player, int id, int windowID) {
      super(key, player, id, windowID);
      this.addPlayerInventoryWithOffset(player.getInventory(), 0, key.getPlayerYOffset());
      key.addComponents(this::addComponent);
      this.addComponent(new FilterComponent(this.getInventoryOffset()));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      this.getHolder().onGuiLoaded(screen);
   }

   @Override
   public ResourceLocation getTexture() {
      return this.getHolder().getTexture();
   }
}
