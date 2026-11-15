package ic2.core.block.machines.components.hv;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.recipes.misc.EnrichRecipe;
import ic2.core.block.machines.tiles.hv.UraniumEnricherTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.helpers.SanityHelper;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UraniumEnricherComponent extends GuiWidget {
   UraniumEnricherTileEntity tile;

   public UraniumEnricherComponent(Box2i box, UraniumEnricherTileEntity tile) {
      super(box);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      EnrichRecipe recipe = this.tile.getStoredRecipe();
      if (recipe != null) {
         float pixel = (float)this.getBox().getHeight() * ((float)this.tile.getPoints() / (float)this.tile.getMaxPoints());
         this.gui
            .drawColoredRegion(
               matrix,
               (float)(this.box.getX() + this.gui.getGuiLeft() + 1),
               (float)(this.box.getY() + this.gui.getGuiTop() + this.box.getHeight()) - pixel,
               (float)(this.box.getWidth() - 1),
               pixel,
               recipe.getColor()
            );
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (this.getBox().isInBox(mouseX, mouseY)) {
         EnrichRecipe recipe = this.tile.getStoredRecipe();
         if (recipe != null) {
            tooltips.accept(this.string(SanityHelper.toPascalCase(recipe.getId().getPath())));
            tooltips.accept(this.string(this.tile.getPoints() + " / 1000"));
         }
      }
   }
}
