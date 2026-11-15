package ic2.core.block.cables;

import ic2.core.block.machines.components.misc.ComparingCableComponent;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ComparingDetectorCableContainer extends ContainerComponent<ComparingDetectorCableTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/misc/gui_comparing_cable.png");

   public ComparingDetectorCableContainer(ComparingDetectorCableTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addPlayerInventoryWithOffset(player.getInventory(), 0, -17);
      this.addComponent(new ComparingCableComponent(key));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setMaxSize(176, 149);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }
}
