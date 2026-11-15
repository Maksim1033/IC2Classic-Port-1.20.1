package ic2.core.block.generators.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.ISteamReactor;
import ic2.core.IC2;
import ic2.core.block.base.tiles.impls.BaseNuclearReactorTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.utils.math.geometry.Box2i;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ReactorComponent extends GuiWidget {
   IReactor reactor;
   int size;

   public ReactorComponent(IReactor reactor, int size) {
      super(Box2i.EMPTY_BOX);
      this.reactor = reactor;
      this.size = size;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      int progress = (int)((double)this.reactor.getHeat() / (double)this.reactor.getMaxHeat() * 160.0);
      this.gui.drawTextureRegion(matrix, (float)(x + 8), (float)(y + 130), 0.0F, 238.0F, (float)progress, 10.0F);
      if (this.size >= 8 && this.reactor instanceof ISteamReactor) {
         this.gui.drawTextureRegion(matrix, (float)(x - 18), (float)y, 194.0F, 0.0F, 22.0F, 132.0F);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      if (this.reactor instanceof BaseNuclearReactorTileEntity nuclear) {
         IHasInventory inventory = nuclear.filter;
         int startX = 87 - 9 * this.size;
         int startY = 16;

         for (int i = 0; i < 54; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && nuclear.getStackInSlot(i).isEmpty()) {
               int x = i % 9;
               if (x < this.size) {
                  int xOff = startX + 18 * x;
                  int yOff = startY + 18 * (i / 9);
                  this.gui.drawItemStack(matrix, (float)xOff, (float)yOff, stack);
                  this.gui.bindDefaultTexture();
                  RenderSystem.disableCull();
                  this.gui.drawTextureRegion(matrix, (float)(xOff + 2), (float)(yOff + 2), 8.0F, 156.0F, 16.0F, 16.0F, 1604998485);
                  RenderSystem.enableCull();
               }
            }
         }
      }

      this.gui.drawCenterString(matrix, this.translate("gui.ic2.reactor.heat"), 89, 131, 4210752);
      if (this.reactor instanceof ISteamReactor) {
         this.gui
            .drawRightString(
               matrix,
               this.translate("gui.ic2.reactor.fluid_output", new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.reactor.getEnergyOutput() * 3.2)}),
               169,
               146,
               4210752
            );
      } else {
         this.gui
            .drawRightString(
               matrix,
               this.translate("gui.ic2.reactor.energy_output", new Object[]{(int)(this.reactor.getEnergyOutput() * (double)IC2.CONFIG.reactorOutput.get())}),
               169,
               146,
               4210752
            );
      }
   }

   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (mouseX >= 7 && mouseX <= 169 && mouseY >= 129 && mouseY <= 141 && PlayerHandler.getClientHandler().hasThermometer()) {
         tooltips.accept(this.translate("gui.ic2.reactor.heat_amount", new Object[]{this.reactor.getHeat(), this.reactor.getMaxHeat()}));
      }

      if (this.reactor instanceof BaseNuclearReactorTileEntity nuclear) {
         IHasInventory inventory = nuclear.filter;
         int startX = 87 - 9 * this.size;
         int startY = 16;

         for (int i = 0; i < 54; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && nuclear.getStackInSlot(i).isEmpty()) {
               int x = i % 9;
               if (x < this.size) {
                  int xOff = startX + 18 * x;
                  int yOff = startY + 18 * (i / 9);
                  if (mouseX >= xOff && mouseX <= xOff + 18 && mouseY >= yOff && mouseY <= yOff + 18) {
                     tooltips.accept(this.translate("gui.ic2.reactor.filter").append(stack.getDisplayName()));
                  }
               }
            }
         }
      }
   }
}
