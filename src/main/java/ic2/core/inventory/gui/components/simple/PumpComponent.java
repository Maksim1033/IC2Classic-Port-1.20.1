package ic2.core.inventory.gui.components.simple;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.tiles.readers.IPumpTile;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PumpComponent extends GuiWidget {
   IPumpTile tile;
   Vec2i texPos;
   Vec2i barPos;
   Vec2i glassPos;
   String translationKey;

   public PumpComponent(Box2i box, IPumpTile tile, Vec2i texPos, Vec2i barPos, Vec2i glassPos, String translationKey) {
      super(box);
      this.tile = tile;
      this.texPos = texPos;
      this.barPos = barPos;
      this.glassPos = glassPos;
      this.translationKey = translationKey;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      Box2i box = this.getBox();
      int maxY = box.getHeight();
      int maxX = box.getWidth();
      float lvl = (float)maxY * Mth.clamp((float)this.tile.getPumpProgress() / (float)this.tile.getPumpMaxProgress(), 0.0F, 1.0F);
      int xPos = x + box.getX();
      int yPos = y + box.getY();
      this.gui
         .drawTextureRegion(
            matrix, (float)xPos, (float)yPos + ((float)(maxY - 6) - lvl), (float)this.barPos.getX(), (float)this.barPos.getY(), (float)maxX, 5.0F
         );
      if (lvl > 0.0F) {
         this.gui
            .drawTextureRegion(
               matrix, (float)xPos, (float)yPos + ((float)(maxY - 1) - lvl), (float)this.texPos.getX(), (float)this.texPos.getY(), (float)maxX, lvl
            );
      }

      yPos -= 6;
      this.gui.drawTextureRegion(matrix, (float)xPos, (float)yPos, (float)this.glassPos.getX(), (float)this.glassPos.getY(), (float)maxX, (float)(maxY + 5));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (this.isMouseOver(mouseX, mouseY) && this.translationKey != null && this.hasEUReader()) {
         tooltips.accept(
            this.translate(
               this.translationKey,
               new Object[]{Formatters.EU_FORMAT.format((long)this.tile.getPumpProgress()), Formatters.EU_FORMAT.format((long)this.tile.getPumpMaxProgress())}
            )
         );
      }
   }
}
