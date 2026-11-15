package ic2.core.wiki.recipes.renderers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.wiki.components.IWikiComponent;
import ic2.core.wiki.helper.ProgressBar;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MachineRecipeRenderer implements IRecipeRenderer {
   IMachineRecipeList.RecipeEntry entry;
   ItemStack machine;
   ProgressBar bar;

   public MachineRecipeRenderer(IMachineRecipeList.RecipeEntry entry, ItemStack machine, ProgressBar bar) {
      this.entry = entry;
      this.machine = machine;
      this.bar = bar;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderBackground(IC2Screen screen, PoseStack matrix, int x, int y) {
      screen.bindDefaultTexture();
      screen.drawTextureRegion(matrix, (float)x, (float)y, 0.0F, 248.0F, 116.0F, 4.0F);
      screen.drawTextureRegion(matrix, (float)x, (float)(y + 60), 0.0F, 252.0F, 116.0F, 4.0F);
      screen.bindTexture(IWikiComponent.TEXTURE);
      x += 33;
      y += 20;
      screen.drawTextureRegion(matrix, (float)x, (float)y, 158.0F, 0.0F, 20.0F, 20.0F);
      screen.drawTextureRegion(matrix, (float)(x + 56), (float)(y - 3), 124.0F, 0.0F, 24.0F, 24.0F, 18.0F, 18.0F);
      screen.bindTexture(this.bar.getTexture());
      int timer = screen.getClock() / 2 % this.bar.getWidth();
      screen.drawTextureRegion(
         matrix,
         (float)(x + 26),
         (float)(y + 2),
         (float)this.bar.getMinX(false),
         (float)this.bar.getMinY(false),
         (float)this.bar.getWidth(),
         (float)this.bar.getHeight()
      );
      screen.drawTextureRegion(
         matrix,
         (float)(x + 26),
         (float)(y + 2),
         (float)this.bar.getMinX(true),
         (float)this.bar.getMinY(true),
         (float)(this.bar.getWidth() - (this.bar.getWidth() - timer)),
         (float)this.bar.getHeight()
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderItems(IC2Screen screen, PoseStack matrix, int x, int y) {
      x += 33;
      y += 20;
      int time = screen.getClock() / 20;
      Lighting.setupFor3DItems();
      screen.drawItemStack(matrix, (float)(x - 26), (float)(y + 2), this.machine);
      this.renderMultiOutput(matrix, this.entry.getInputs()[0].getComponents(), x + 2, y + 2, time, screen.getFont(), screen.getRenderItem());
      this.renderMultiOutput(matrix, this.entry.getOutput().getAllOutputs(), x + 60, y + 1, time, screen.getFont(), screen.getRenderItem());
      Lighting.setupForFlatItems();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<Component> getHoverInfo(IC2Screen screen, int mouseX, int mouseY, int x, int y) {
      x += 33;
      y += 20;
      if (screen.isOverArea(mouseX, mouseY, x + 2, y + 2, 16, 16)) {
         List<ItemStack> list = this.entry.getInputs()[0].getComponents();
         if (!list.isEmpty()) {
            int time = screen.getClock() / 20;
            return this.getToolTip(list.get(time % list.size()));
         }
      }

      if (screen.isOverArea(mouseX, mouseY, x + 60, y + 1, 16, 16)) {
         List<ItemStack> list = this.entry.getOutput().getAllOutputs();
         if (!list.isEmpty()) {
            int time = screen.getClock() / 20;
            return this.getToolTip(list.get(time % list.size()));
         }
      }

      return screen.isOverArea(mouseX, mouseY, x - 26, y + 1, 16, 16) ? this.getToolTip(this.machine) : Collections.emptyList();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public ItemStack getHoverStack(IC2Screen screen, int mouseX, int mouseY, int x, int y) {
      x += 33;
      y += 20;
      if (screen.isOverArea(mouseX, mouseY, x + 2, y + 2, 16, 16)) {
         List<ItemStack> list = this.entry.getInputs()[0].getComponents();
         if (!list.isEmpty()) {
            int time = screen.getClock() / 20;
            return list.get(time % list.size()).copy();
         }
      }

      if (screen.isOverArea(mouseX, mouseY, x + 60, y + 1, 16, 16)) {
         List<ItemStack> list = this.entry.getOutput().getAllOutputs();
         if (!list.isEmpty()) {
            int time = screen.getClock() / 20;
            return list.get(time % list.size()).copy();
         }
      }

      return screen.isOverArea(mouseX, mouseY, x - 26, y + 1, 16, 16) ? this.machine.copy() : ItemStack.EMPTY;
   }
}
