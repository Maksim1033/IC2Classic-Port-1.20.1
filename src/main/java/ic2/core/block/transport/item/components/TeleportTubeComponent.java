package ic2.core.block.transport.item.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.transport.item.tubes.TeleportTubeTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ImprovedTextWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.platform.player.friends.Action;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class TeleportTubeComponent extends GuiWidget {
   TeleportTubeTileEntity tile;
   String id = "";
   int flags = 0;

   public TeleportTubeComponent(TeleportTubeTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.KEY_INPUT);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      boolean canChange = this.tile.canDoAction(gui.getPlayerID(), Action.TELEPORT_SETTINGS, false);
      ((ItemCheckBox)gui.addRenderableWidget(
            0, new ItemCheckBox(x + 5, y + 56, 16, 16, T -> this.toggle(1), IC2Items.ICON_DISPLAY.create(3), (this.tile.state & 1) != 0)
         ))
         .setToolTip("gui.ic2.teleport_tube.send")
         .active = this.tile.isPublic() || canChange;
      ((ItemCheckBox)gui.addRenderableWidget(
            1, new ItemCheckBox(x + 23, y + 56, 16, 16, T -> this.toggle(2), IC2Items.ICON_DISPLAY.create(4), (this.tile.state & 2) != 0)
         ))
         .setToolTip("gui.ic2.teleport_tube.receive")
         .active = this.tile.isPublic() || canChange;
      ((ItemCheckBox)gui.addRenderableWidget(
            2, new ItemCheckBox(x + 41, y + 56, 16, 16, T -> this.toggle(4), new ItemStack(Items.SHIELD), (this.tile.state & 4) != 0)
         ))
         .setToolTip("gui.ic2.personal.mode.private")
         .active = this.tile.canDoAction(gui.getPlayerID(), Action.TELEPORT_SETTINGS, true);
      gui.addRenderableWidget(4, new ExtendedButton(x + 5, y + 74, 89, 16, this.translate("gui.ic2.chunkloader.confirm"), T -> this.save()));
      ImprovedTextWidget text = gui.addRenderableWidget(3, new ImprovedTextWidget(x + 8, y + 43, 87, 14));
      text.setValue(this.tile.frequency);
      text.setBordered(false);
      text.setMaxLength(14);
      text.setCanLoseFocus(true);
      text.setResponder(T -> this.id = T);
      this.flags = this.tile.state;
      this.id = this.tile.frequency;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      boolean canChange = this.tile.canDoAction(gui.getPlayerID(), Action.TELEPORT_SETTINGS, false);
      ((ItemCheckBox)gui.getCastedButton(0, ItemCheckBox.class)).setChecked((this.flags & 1) != 0).active = this.tile.isPublic() || canChange;
      ((ItemCheckBox)gui.getCastedButton(1, ItemCheckBox.class)).setChecked((this.flags & 2) != 0).active = this.tile.isPublic() || canChange;
      ((ItemCheckBox)gui.getCastedButton(2, ItemCheckBox.class)).setChecked((this.flags & 4) != 0).active = this.tile
         .canDoAction(gui.getPlayerID(), Action.TELEPORT_SETTINGS, true);
      gui.getButton(4).active = this.flags != this.tile.state || !this.id.equals(this.tile.frequency);
   }

   private void toggle(int flag) {
      this.flags ^= flag;
   }

   private void save() {
      this.tile.sendToServer("frequency", new NBTBuffer("frequency", StringTag.valueOf(this.id)));
      this.tile.sendToServer(0, this.flags);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      this.gui.drawString(matrix, this.translate("gui.ic2.teleport_tube.network_id"), 7, 32, 4210752);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onKeyTyped(int keyCode) {
      return keyCode == 69 && ((ImprovedTextWidget)this.gui.getCastedButton(3, ImprovedTextWidget.class)).isFocused();
   }
}
