package ic2.core.block.personal.components;

import ic2.core.block.base.tiles.impls.BasePersonalTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PersonalComponent extends GuiWidget {
   static final Component[] COMPONENTS = new Component[]{
      Component.translatable("gui.ic2.personal.mode", new Object[]{Component.translatable("gui.ic2.personal.mode.public")}),
      Component.translatable("gui.ic2.personal.mode", new Object[]{Component.translatable("gui.ic2.personal.mode.protected")}),
      Component.translatable("gui.ic2.personal.mode", new Object[]{Component.translatable("gui.ic2.personal.mode.private")})
   };
   static final ItemStack[] COMPONENT_ITEMS = new ItemStack[]{new ItemStack(Items.GOLDEN_SWORD), new ItemStack(Items.BOW), new ItemStack(Items.SHIELD)};
   BasePersonalTileEntity tile;
   int lastMode;

   public PersonalComponent(BasePersonalTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      this.lastMode = this.tile.mode;
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(
         0, new IconButton(x + 176, y, 16, 16, COMPONENT_ITEMS[this.lastMode], T -> this.changeMode()).setToolTip(COMPONENTS[this.lastMode])
      );
      gui.addRenderableWidget(
         1, new IconButton(x + 176, y + 17, 16, 16, new ItemStack(Items.ENDER_PEARL), T -> this.voidContents()).setToolTip("gui.ic2.personal.void")
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      if (this.lastMode != this.tile.mode) {
         this.lastMode = this.tile.mode;
         ((IconButton)gui.getCastedButton(0, IconButton.class)).setDisplay(COMPONENT_ITEMS[this.lastMode]).setToolTip(COMPONENTS[this.lastMode]);
      }

      gui.getButton(1).active = Screen.hasControlDown() && Screen.hasShiftDown();
   }

   private void changeMode() {
      this.tile.sendToServer(0, 0);
   }

   @OnlyIn(Dist.CLIENT)
   private void voidContents() {
      if (Screen.hasControlDown() && Screen.hasShiftDown()) {
         this.tile.sendToServer(1, 0);
      }
   }
}
