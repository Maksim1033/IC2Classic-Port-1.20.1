package ic2.core.block.machines.components.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.misc.tiles.PlayerDetectorTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.inventory.gui.components.base.ScrollSlider;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerDetectorComponent extends GuiWidget {
   static final Component[] COMPONENTS = new Component[]{
      Component.translatable("gui.ic2.personal.mode", new Object[]{Component.translatable("gui.ic2.personal.mode.public")}),
      Component.translatable("gui.ic2.personal.mode", new Object[]{Component.translatable("gui.ic2.personal.mode.protected")}),
      Component.translatable("gui.ic2.personal.mode", new Object[]{Component.translatable("gui.ic2.personal.mode.private")})
   };
   static final ItemStack[] COMPONENT_ITEMS = new ItemStack[]{new ItemStack(Items.GOLDEN_SWORD), new ItemStack(Items.BOW), new ItemStack(Items.SHIELD)};
   PlayerDetectorTileEntity tile;
   int lastMode;
   int[] last = new int[3];

   public PlayerDetectorComponent(PlayerDetectorTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      this.lastMode = this.tile.mode;
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(
         0,
         new ScrollSlider(
               x + 25,
               y + 35,
               40,
               10,
               this.translate("gui.ic2.player_detector.x"),
               this.string(),
               0.0,
               64.0,
               (double)this.tile.xRadius,
               T -> this.setRadius(0, T.getValueInt())
            )
            .setScrollEffect(1.0)
      );
      gui.addRenderableWidget(
         1,
         new ScrollSlider(
               x + 70,
               y + 35,
               40,
               10,
               this.translate("gui.ic2.player_detector.y"),
               this.string(),
               0.0,
               64.0,
               (double)this.tile.yRadius,
               T -> this.setRadius(1, T.getValueInt())
            )
            .setScrollEffect(1.0)
      );
      gui.addRenderableWidget(
         2,
         new ScrollSlider(
               x + 115,
               y + 35,
               40,
               10,
               this.translate("gui.ic2.player_detector.z"),
               this.string(),
               0.0,
               64.0,
               (double)this.tile.zRadius,
               T -> this.setRadius(2, T.getValueInt())
            )
            .setScrollEffect(1.0)
      );
      gui.addRenderableWidget(
         3, new IconButton(x + 176, y, 16, 16, COMPONENT_ITEMS[this.lastMode], T -> this.changeMode()).setToolTip(COMPONENTS[this.lastMode])
      );
   }

   private void changeMode() {
      this.tile.sendToServer(3, 0);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawCenterString(matrix, this.translate("gui.ic2.player_detector.scan_radius"), 90, 23, 4210752);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      ScrollSlider slider = gui.getCastedButton(0, ScrollSlider.class);
      if (this.last[0] != this.tile.xRadius) {
         this.last[0] = this.tile.xRadius;
         slider.setValue((double)this.tile.xRadius);
         slider.applyValue();
      }

      slider = gui.getCastedButton(1, ScrollSlider.class);
      if (this.last[1] != this.tile.yRadius) {
         this.last[1] = this.tile.yRadius;
         slider.setValue((double)this.tile.yRadius);
         slider.applyValue();
      }

      slider = gui.getCastedButton(2, ScrollSlider.class);
      if (this.last[2] != this.tile.zRadius) {
         this.last[2] = this.tile.zRadius;
         slider.setValue((double)this.tile.zRadius);
         slider.applyValue();
      }

      if (this.lastMode != this.tile.mode) {
         this.lastMode = this.tile.mode;
         ((IconButton)gui.getCastedButton(3, IconButton.class)).setDisplay(COMPONENT_ITEMS[this.lastMode]).setToolTip(COMPONENTS[this.lastMode]);
      }
   }

   public void setRadius(int index, int value) {
      this.tile.sendToServer(index, value);
      switch (index) {
         case 0:
            this.tile.xRadius = Mth.clamp(value, 0, 64);
            break;
         case 1:
            this.tile.yRadius = Mth.clamp(value, 0, 64);
            break;
         case 2:
            this.tile.zRadius = Mth.clamp(value, 0, 64);
      }
   }
}
