package ic2.core.block.generators.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.generators.tiles.SolarTurbineTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SolarTurbineComponent extends GuiWidget {
   SolarTurbineTileEntity tile;

   public SolarTurbineComponent(SolarTurbineTileEntity tile) {
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
      this.gui.drawCenterString(matrix, this.translate("gui.ic2.gen"), 75, 52, 4210752);
      this.gui.drawString(matrix, this.string(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)((float)this.tile.heat / 240.0F)) + "%"), 60, 62, 4210752);
   }
}
