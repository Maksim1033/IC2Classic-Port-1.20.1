package ic2.core.inventory.custom.container;

import ic2.core.inventory.base.IHasGui;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.custom.components.FriendComponent;
import ic2.core.inventory.gui.IC2Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FriendsContainer extends ContainerComponent<IHasGui> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/misc/gui_friends.png");

   public FriendsContainer(Player player, int id) {
      super(null, player, id);
      this.addHiddenPlayerInventory(player.getInventory());
      this.addComponent(new FriendComponent());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setMaxSize(187, 137);
      screen.clearFlag(1);
   }

   @Override
   public Component getName() {
      return Component.translatable("gui.ic2.friends");
   }

   @Override
   public void removed(Player playerIn) {
   }

   @Override
   public boolean stillValid(Player playerIn) {
      return playerIn.isAlive();
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }
}
