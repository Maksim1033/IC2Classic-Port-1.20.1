package ic2.core.block.storage.container;

import ic2.core.block.base.tiles.impls.BaseElectricLoaderTileEntity;
import ic2.core.block.storage.components.LoaderComponent;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.ChargeBarComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LoaderContainer extends ContainerComponent<BaseElectricLoaderTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/storage/gui_loader.png");

   public LoaderContainer(BaseElectricLoaderTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addSlot(FilterSlot.createDischargeSlot(key, 0, 62, 18));
      this.addPlayerInventoryWithOffset(player.getInventory(), 0, -33);
      this.addComponent(new ChargeBarComponent(new Box2i(84, 17, 24, 17), key, new Vec2i(176, 0), false));
      this.addComponent(new LoaderComponent(key));
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setYSize(133);
   }
}
