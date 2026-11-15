package ic2.core.inventory.gui.components.simple;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.tiles.readers.IWorkProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public class FlagBarComponent extends GuiWidget {
   BooleanSupplier base;
   Vec2i posTexture;
   Component tooltip;

   public FlagBarComponent(Box2i box, BooleanSupplier base, Vec2i posTexture) {
      super(box);
      this.base = base;
      this.posTexture = posTexture;
   }

   public static FlagBarComponent createActiveBar(Box2i box, BaseTileEntity tile, Vec2i posTexture) {
      return new FlagBarComponent(box, tile::isActive, posTexture);
   }

   public static FlagBarComponent createWorkBar(Box2i box, IWorkProvider tile, Vec2i posTexture) {
      return new FlagBarComponent(box, tile::isWorking, posTexture);
   }

   public FlagBarComponent withTooltip(Component tooltip) {
      this.tooltip = tooltip;
      return this;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      if (this.tooltip != null) {
         requests.add(GuiWidget.ActionRequest.TOOLTIP);
      }
   }

   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      if (this.base.getAsBoolean()) {
         this.gui
            .drawTextureRegion(
               matrix,
               (float)(this.box.getX() + this.gui.getGuiLeft()),
               (float)(this.box.getY() + this.gui.getGuiTop()),
               (float)this.posTexture.getX(),
               (float)this.posTexture.getY(),
               (float)this.box.getWidth(),
               (float)this.box.getHeight()
            );
      }
   }

   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (this.tooltip != null && this.isMouseOver(mouseX, mouseY) && this.base.getAsBoolean()) {
         tooltips.accept(this.tooltip);
      }
   }
}
