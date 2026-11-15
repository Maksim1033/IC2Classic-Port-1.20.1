package ic2.core.block.machines.components.mv.planner;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ImprovedTextWidget;
import ic2.core.networking.buffers.data.StringDataBuffer;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NamingTabComponent extends BaseInfoTabComponent {
   public static final Predicate<String> NUMBERS_ONLY = T -> {
      try {
         return T.isEmpty() || Integer.parseInt(T) >= 0;
      } catch (Exception var2) {
         return false;
      }
   };
   @OnlyIn(Dist.CLIENT)
   ImprovedTextWidget naming;
   @OnlyIn(Dist.CLIENT)
   ImprovedTextWidget startHeat;
   @OnlyIn(Dist.CLIENT)
   ImprovedTextWidget customTime;
   boolean isUpdating = false;

   public NamingTabComponent(ReactorPlannerTileEntity tile) {
      super(178, tile, Component.translatable("gui.ic2.reactor_planner.settings"), new ItemStack(Items.WRITABLE_BOOK), 3);
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      super.addRequests(requests);
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.KEY_INPUT);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      this.isUpdating = true;
      this.naming = new ImprovedTextWidget(gui.getFont(), gui.getGuiLeft() + 8, gui.getGuiTop() + 143, 150, 10, this.string());
      this.naming.setValue(this.tile.setupName);
      this.naming.setResponder(T -> {
         if (!this.isUpdating) {
            this.tile.sendToServer("", new StringDataBuffer(0, T));
         }
      });
      this.naming.setCanLoseFocus(true);
      this.naming.setAutoUpdating(true);
      this.naming.visible = this.isSelected();
      gui.addRenderableWidget(this.naming);
      this.startHeat = new ImprovedTextWidget(gui.getFont(), gui.getGuiLeft() + 8, gui.getGuiTop() + 166, 150, 10, this.string());
      this.startHeat.setValue(Integer.toString(this.tile.getSettings().startingHeat));
      this.startHeat.setFilter(NUMBERS_ONLY);
      this.startHeat.setResponder(T -> {
         if (!this.isUpdating) {
            this.tile.sendToServer(11, T.isEmpty() ? 0 : Integer.parseInt(T));
         }
      });
      this.startHeat.setCanLoseFocus(true);
      this.startHeat.setAutoUpdating(true);
      this.startHeat.visible = this.isSelected();
      gui.addRenderableWidget(this.startHeat);
      this.customTime = new ImprovedTextWidget(gui.getFont(), gui.getGuiLeft() + 8, gui.getGuiTop() + 189, 150, 10, this.string());
      this.customTime.setValue(Integer.toString(this.tile.getSettings().maxTicks));
      this.customTime.setFilter(NUMBERS_ONLY);
      this.customTime.setResponder(T -> {
         if (!this.isUpdating) {
            this.tile.sendToServer(12, T.isEmpty() ? 0 : Integer.parseInt(T));
         }
      });
      this.customTime.setCanLoseFocus(true);
      this.customTime.setAutoUpdating(true);
      this.customTime.visible = this.isSelected();
      gui.addRenderableWidget(this.customTime);
      this.isUpdating = false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      if (this.isSelected()) {
         this.gui.drawString(matrix, this.translate("gui.ic2.reactor_planner.settings.name"), 8, 133, 4210752);
         this.gui.drawString(matrix, this.translate("gui.ic2.reactor_planner.settings.heat"), 8, 158, 4210752);
         this.gui.drawString(matrix, this.translate("gui.ic2.reactor_planner.settings.sim"), 8, 180, 4210752);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      this.naming.visible = this.isSelected();
      this.startHeat.visible = this.isSelected();
      this.customTime.visible = this.isSelected();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onKeyTyped(int keyCode) {
      return this.naming.isFocused() && keyCode == 69;
   }

   @OnlyIn(Dist.CLIENT)
   public void onChanged() {
      this.isUpdating = true;
      this.naming.setValue(this.tile.setupName);
      this.startHeat.setValue(Integer.toString(this.tile.getSettings().startingHeat));
      this.customTime.setValue(Integer.toString(this.tile.getSettings().maxTicks));
      this.isUpdating = false;
   }
}
