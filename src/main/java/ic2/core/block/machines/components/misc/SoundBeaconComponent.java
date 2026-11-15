package ic2.core.block.machines.components.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.tiles.nv.SoundBeaconTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SoundBeaconComponent extends GuiWidget {
   SoundBeaconTileEntity tile;

   public SoundBeaconComponent(SoundBeaconTileEntity tile) {
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
      int itemEffect = (int)(this.tile.itemMod * 100.0F);
      int armorEffect = (int)(this.tile.armorMod * 100.0F);
      int blockEffect = (int)(this.tile.blockMod * 100.0F);
      matrix.pushPose();
      matrix.translate(30.0, 38.0, 0.0);
      matrix.scale(0.5F, 0.5F, 1.0F);
      this.gui.drawString(matrix, this.string("Blocks"), 0, -30, 4210752);
      this.gui.drawString(matrix, this.string("Range: " + this.tile.blockRange), 0, -20, 4210752);
      this.gui.drawString(matrix, this.string("Effect: " + blockEffect + "%"), 0, -10, 4210752);
      this.gui.drawString(matrix, this.string("Item"), 100, -30, 4210752);
      this.gui.drawString(matrix, this.string("Range: " + this.tile.itemRange), 100, -20, 4210752);
      this.gui.drawString(matrix, this.string("Effect: " + itemEffect + "%"), 100, -10, 4210752);
      this.gui.drawString(matrix, this.string("Armor"), 200, -30, 4210752);
      this.gui.drawString(matrix, this.string("Range: " + this.tile.armorRange), 200, -20, 4210752);
      this.gui.drawString(matrix, this.string("Effect: " + armorEffect + "%"), 200, -10, 4210752);
      matrix.popPose();
   }
}
