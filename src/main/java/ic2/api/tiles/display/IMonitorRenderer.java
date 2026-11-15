package ic2.api.tiles.display;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IMonitorRenderer {
   BufferSource getBatcher();

   Font getFont();

   ItemRenderer getItemRenderer();

   void renderGuiItems(PoseStack var1, ItemStack var2, float var3, float var4);

   default void renderGuiItemText(PoseStack matrixStack, ItemStack stack, float x, float y, String text) {
      this.renderGuiItemText(matrixStack, this.getFont(), stack, x, y, text);
   }

   void renderGuiItemText(PoseStack var1, Font var2, ItemStack var3, float var4, float var5, String var6);
}
