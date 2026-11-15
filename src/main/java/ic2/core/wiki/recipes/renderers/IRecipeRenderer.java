package ic2.core.wiki.recipes.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.platform.rendering.RenderUtils;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IRecipeRenderer {
   @OnlyIn(Dist.CLIENT)
   void renderBackground(IC2Screen var1, PoseStack var2, int var3, int var4);

   @OnlyIn(Dist.CLIENT)
   void renderItems(IC2Screen var1, PoseStack var2, int var3, int var4);

   @OnlyIn(Dist.CLIENT)
   List<Component> getHoverInfo(IC2Screen var1, int var2, int var3, int var4, int var5);

   @OnlyIn(Dist.CLIENT)
   ItemStack getHoverStack(IC2Screen var1, int var2, int var3, int var4, int var5);

   @OnlyIn(Dist.CLIENT)
   default List<Component> getToolTip(ItemStack stack) {
      return stack.getTooltipLines(IC2.PLATFORM.getClientPlayerInstance(), Default.NORMAL);
   }

   @OnlyIn(Dist.CLIENT)
   default void renderMultiOutput(PoseStack matrix, List<ItemStack> items, int x, int y, int clock, Font font, ItemRenderer render) {
      if (!items.isEmpty()) {
         ItemStack stack = items.get(clock % items.size());
         RenderUtils.renderGuiItem(render, matrix, stack, (float)x, (float)y);
         RenderUtils.renderGuiItemDecorations(matrix, font, stack, x, y);
      }
   }

   @OnlyIn(Dist.CLIENT)
   default void renderMultiOutput(PoseStack matrix, ItemStack[] items, int x, int y, int clock, Font font, ItemRenderer render) {
      if (items.length > 0) {
         ItemStack stack = items[clock % items.length];
         RenderUtils.renderGuiItem(render, matrix, stack, (float)x, (float)y);
         RenderUtils.renderGuiItemDecorations(matrix, font, stack, x, y);
      }
   }
}
