package ic2.core.block.transport.item.container;

import ic2.core.block.transport.item.components.RoundRobinComponent;
import ic2.core.block.transport.item.tubes.RoundRobinTubeTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RoundRobinTubeContainer extends ContainerComponent<RoundRobinTubeTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/tubes/gui_round_robin_tube.png");

   public RoundRobinTubeContainer(RoundRobinTubeTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addHiddenPlayerInventory(player.getInventory());
      this.addComponent(new RoundRobinComponent(key));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setMaxSize(140, 114);
      screen.clearFlag(1);
   }

   @Override
   public int getInventorySize() {
      return 0;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
   }
}
