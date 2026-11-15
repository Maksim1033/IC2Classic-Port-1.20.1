package ic2.core.block.storage.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.util.DirectionList;
import ic2.core.block.storage.tiles.RedirectorMasterTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.networking.buffers.data.NodeBuffer;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class MasterComponent extends GuiWidget {
   public static final Predicate<String> PREDICATE = T -> {
      try {
         int value = Integer.parseInt(T);
         return value >= 0 && value <= 100;
      } catch (Exception var2) {
         return T.isEmpty();
      }
   };
   public static final Predicate<String> NUMBER_PREDICATE = T -> {
      try {
         return Integer.parseInt(T) != 0;
      } catch (Exception var2) {
         return T.isEmpty();
      }
   };
   RedirectorMasterTileEntity tile;

   public MasterComponent(RedirectorMasterTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      DirectionList dirs = DirectionList.ofNumber(this.tile.sides);

      for (int i = 0; i < 6; i++) {
         boolean enabled = i != this.tile.getFacing().get3DDataValue() && dirs.contains(Direction.from3DDataValue(i));
         EditBox widget = new EditBox(gui.getFont(), x + 41, y + i * 14 + 19, 54, 14, this.string());
         widget.setValue(enabled ? Integer.toString(this.tile.shares[i]) : "0");
         widget.setCanLoseFocus(true);
         widget.setBordered(false);
         widget.setMaxLength(3);
         widget.setFilter(PREDICATE);
         widget.setEditable(enabled);
         gui.addRenderableWidget(i, widget);
      }

      gui.addRenderableWidget(7, new ExtendedButton(x + 15, y + 114, 40, 12, this.translate("gui.ic2.master_node.apply"), T -> this.confirmData(gui)));
      EditBox widget = new EditBox(gui.getFont(), x + 66, y + 116, 54, 14, this.string());
      widget.setFilter(NUMBER_PREDICATE);
      widget.setValue("128");
      widget.setCanLoseFocus(true);
      widget.setBordered(false);
      widget.setMaxLength(4);
      gui.addRenderableWidget(8, widget);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();

      for (int i = 0; i < 6; i++) {
         this.gui.drawTextureRegion(matrix, (float)(x + 40), (float)(y + i * 14 + 17), 0.0F, 133.0F, 20.0F, 12.0F);
      }

      this.gui.drawTextureRegion(matrix, (float)(x + 65), (float)(y + 114), 0.0F, 145.0F, 38.0F, 12.0F);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      int total = 0;
      float[] entry = new float[6];

      for (Direction dir : DirectionList.ALL) {
         this.gui.drawRightString(matrix, DirectionList.getName(dir), 35, 14 * dir.get3DDataValue() + 20, 4210752);
         String s = ((EditBox)this.gui.getCastedButton(dir.get3DDataValue(), EditBox.class)).getValue();
         entry[dir.get3DDataValue()] = s.isEmpty() ? 0.0F : (float)Integer.parseInt(s);
         total += (int)entry[dir.get3DDataValue()];
      }

      this.gui.drawCenterString(matrix, this.translate("gui.ic2.master_node.average", new Object[]{total}), 35, 105, 4210752);
      this.gui.getButton(7).active = total == 100;
      this.gui.drawString(matrix, this.translate("gui.ic2.master_node.eu"), 73, 105, 4210752);
      String s = ((EditBox)this.gui.getCastedButton(8, EditBox.class)).getValue();
      int eu = s.isEmpty() ? 0 : Integer.parseInt(s);

      for (int i = 0; i < 6; i++) {
         int myEU = (int)Math.ceil((double)((float)eu * (entry[i] / (float)total)));
         this.gui.drawString(matrix, this.translate("gui.ic2.master_node.eu_average", new Object[]{entry[i] == 0.0F ? 0 : myEU}), 61, 14 * i + 19, 4210752);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void confirmData(IC2Screen gui) {
      float total = 0.0F;
      int[] data = new int[6];

      for (int i = 0; i < 6; i++) {
         String s = ((EditBox)gui.getCastedButton(i, EditBox.class)).getValue();
         data[i] = s.isEmpty() ? 0 : Integer.parseInt(s);
         total += (float)data[i];
      }

      if (total == 100.0F) {
         this.tile.sendToServer("", new NodeBuffer(data));
      }
   }
}
