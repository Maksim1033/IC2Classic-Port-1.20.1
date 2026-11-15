package ic2.core.block.machines.components.mv.planner;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BaseInfoTabComponent extends GuiWidget {
   protected ReactorPlannerTileEntity tile;
   final Component component;
   final ItemStack stack;
   final int myIndex;

   public BaseInfoTabComponent(int y, ReactorPlannerTileEntity tile, Component component, ItemStack stack, int myIndex) {
      super(new Box2i(-28, y, 28, 28));
      this.tile = tile;
      this.component = component;
      this.stack = stack;
      this.myIndex = myIndex;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
   }

   public boolean isSelected() {
      return this.tile.selectedView == this.myIndex;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      this.drawSideTab(matrix, this.getBox().getX(), this.getBox().getY(), this.stack, this.isSelected());
   }

   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (!this.isSelected()) {
         this.tile.sendToServer(6, this.myIndex);
         IC2.AUDIO.playSound(Minecraft.getInstance().player, SoundEvents.UI_BUTTON_CLICK.value().getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (this.isMouseOver(mouseX, mouseY)) {
         tooltips.accept(this.component);
      }
   }

   @OnlyIn(Dist.CLIENT)
   protected void drawSideTab(PoseStack matrix, int xOffset, int yOffset, ItemStack item, boolean enabled) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      this.gui.drawTextureRegion(matrix, (float)(x + xOffset), (float)(y + yOffset), enabled ? 122.0F : 156.0F, 212.0F, enabled ? 31.0F : 28.0F, 28.0F);
      this.gui.drawItemStack(matrix, (float)(x + xOffset + 8), (float)(y + yOffset + 6), item);
      this.gui.bindDefaultTexture();
   }
}
