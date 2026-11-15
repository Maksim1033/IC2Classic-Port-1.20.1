package ic2.core.inventory.gui.components.simple;

import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import java.util.Set;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TankVoiderComponent extends GuiWidget {
   BaseTileEntity entity;
   Vec2i pos;
   int id;
   int buttonId;

   public TankVoiderComponent(BaseTileEntity entity, Vec2i pos, int id, int buttonId) {
      super(new Box2i(pos.getX(), pos.getY(), 16, 16));
      this.entity = entity;
      this.pos = pos;
      this.id = id;
      this.buttonId = buttonId;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      gui.addRenderableWidget(
         this.buttonId,
         new IconButton(
               gui.getGuiLeft() + this.pos.getX(), gui.getGuiTop() + this.pos.getY(), 16, 16, new ItemStack(Items.ENDER_PEARL), T -> this.voidContents()
            )
            .setToolTip("gui.ic2.personal.void")
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(this.buttonId).active = Screen.hasControlDown() && Screen.hasShiftDown();
   }

   public void voidContents() {
      if (Screen.hasControlDown() && Screen.hasShiftDown()) {
         this.entity.sendToServer(this.id, 0);
      }
   }
}
