package ic2.core.block.generators.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.generators.tiles.ThermalGeneratorTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ThermalGeneratorComponent extends GuiWidget {
   ThermalGeneratorTileEntity tile;

   public ThermalGeneratorComponent(ThermalGeneratorTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawString(matrix, this.translate("gui.ic2.thermal_generator.heat"), 7, 36, 4210752);
      this.gui
         .drawString(
            matrix,
            this.translate(
               "gui.ic2.thermal_generator.production",
               new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)this.tile.subProduction.getProduction(2000.0F))}
            ),
            7,
            44,
            4210752
         );
   }
}
