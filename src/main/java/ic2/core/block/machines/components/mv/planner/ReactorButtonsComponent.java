package ic2.core.block.machines.components.mv.planner;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.platform.registries.IC2Blocks;
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

public class ReactorButtonsComponent extends GuiWidget {
   public static final Box2i REACTOR_BOX = new Box2i(-28, 17, 31, 28);
   public static final Box2i STEAM_BOX = new Box2i(-28, 47, 31, 28);
   ItemStack reactor;
   ItemStack steam_reactor;
   ReactorPlannerTileEntity tile;

   public ReactorButtonsComponent(ReactorPlannerTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
      this.reactor = new ItemStack(IC2Blocks.NUCLEAR_REACTOR);
      this.steam_reactor = new ItemStack(IC2Blocks.STEAM_REACTOR);
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      this.drawSideTab(matrix, REACTOR_BOX.getX(), REACTOR_BOX.getY(), this.reactor, !this.tile.isSteamReactor());
      this.drawSideTab(matrix, STEAM_BOX.getX(), STEAM_BOX.getY(), this.steam_reactor, this.tile.isSteamReactor());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (mouseButton != 0) {
         return false;
      } else if (REACTOR_BOX.isInBox(mouseX, mouseY) && this.tile.isSteamReactor()) {
         this.tile.sendToServer(5, 0);
         IC2.AUDIO.playSound(Minecraft.getInstance().player, SoundEvents.UI_BUTTON_CLICK.value().getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
         return true;
      } else if (STEAM_BOX.isInBox(mouseX, mouseY) && !this.tile.isSteamReactor()) {
         this.tile.sendToServer(5, 1);
         IC2.AUDIO.playSound(Minecraft.getInstance().player, SoundEvents.UI_BUTTON_CLICK.value().getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (REACTOR_BOX.isInBox(mouseX, mouseY)) {
         tooltips.accept(this.translate("gui.ic2.reactor_planner.tabs.reactor"));
      }

      if (STEAM_BOX.isInBox(mouseX, mouseY)) {
         tooltips.accept(this.translate("gui.ic2.reactor_planner.tabs.steam_reactor"));
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void drawSideTab(PoseStack matrix, int xOffset, int yOffset, ItemStack item, boolean enabled) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      this.gui.drawTextureRegion(matrix, (float)(x + xOffset), (float)(y + yOffset), enabled ? 122.0F : 156.0F, 212.0F, enabled ? 31.0F : 28.0F, 28.0F);
      this.gui.drawItemStack(matrix, (float)(x + xOffset + 8), (float)(y + yOffset + 6), item);
      this.gui.bindDefaultTexture();
   }
}
