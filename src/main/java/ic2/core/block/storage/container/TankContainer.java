package ic2.core.block.storage.container;

import ic2.core.block.storage.tiles.tank.TankTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.TankComponent;
import ic2.core.inventory.gui.components.simple.TankVoiderComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TankContainer extends ContainerComponent<TankTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/storage/gui_tank.png");
   public static final Box2i TANK_BOX = new Box2i(80, 14, 16, 58);
   public static final Vec2i VOID_POS = new Vec2i(176, 0);

   public TankContainer(TankTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addSlot(FilterSlot.createFluidDrainSlot(key, 0, 44, 14, key.tank));
      this.addSlot(FilterSlot.createFluidOutputSlot(key, 1, 44, 56));
      this.addSlot(FilterSlot.createFluidFillSlot(key, 2, 116, 14, key.tank));
      this.addSlot(FilterSlot.createFluidOutputSlot(key, 3, 116, 56));
      this.addPlayerInventory(player.getInventory());
      this.addComponent(new TankComponent(TANK_BOX, key.tank));
      this.addComponent(new TankVoiderComponent(key, VOID_POS, 0, 0));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setContainerOffset(0, -2);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }
}
