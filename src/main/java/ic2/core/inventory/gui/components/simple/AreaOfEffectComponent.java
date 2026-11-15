package ic2.core.inventory.gui.components.simple;

import ic2.core.block.base.features.IAreaOfEffect;
import ic2.core.block.rendering.world.impl.BlockHighlighter;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class AreaOfEffectComponent extends GuiWidget {
   BlockEntity tile;
   IAreaOfEffect effect;
   Box2i buttonBox;

   public <T extends BlockEntity & IAreaOfEffect> AreaOfEffectComponent(T tile, Box2i buttonBox) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
      this.effect = tile;
      this.buttonBox = buttonBox;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(
         new ExtendedButton(
            x + this.buttonBox.getX(),
            y + this.buttonBox.getY(),
            this.buttonBox.getWidth(),
            this.buttonBox.getHeight(),
            this.translate("gui.ic2.player_detector.visualize"),
            T -> this.visualize()
         )
      );
   }

   @OnlyIn(Dist.CLIENT)
   public void visualize() {
      if (this.effect.getVisualizationId() != -1) {
         this.effect.setVisualizationId(-1);
      } else {
         this.effect.setVisualizationId(this.tile.getLevel().random.nextInt());
         BlockHighlighter.INSTANCE.addHighlight(this.tile.getBlockPos(), 500, this.effect.getAreaOfEffectColor(), true, T -> {
            T.renderBox = this.effect.getAreaOfEffect();
            if (T.ticksLeft == 1) {
               this.effect.setVisualizationId(-1);
            }

            return this.effect.getVisualizationId() != -1 && !this.tile.isRemoved();
         });
      }
   }
}
