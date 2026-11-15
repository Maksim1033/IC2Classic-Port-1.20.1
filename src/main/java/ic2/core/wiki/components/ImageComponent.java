package ic2.core.wiki.components;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.inventory.gui.IC2Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ImageComponent extends BaseWikiComponent {
   ResourceLocation texture;
   float height;
   float width;
   float offset;

   public ImageComponent(ResourceLocation texture, float width, float height, float offset) {
      super(Mth.ceil(height));
      this.texture = texture;
      this.height = height;
      this.width = width;
      this.offset = offset;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderBackground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
      screen.bindTexture(this.texture);
      screen.drawTextureRegion(stack, (float)x + this.offset, (float)y, 0.0F, 0.0F, this.width, this.height, 256.0F, 256.0F, -1);
   }
}
