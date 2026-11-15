package ic2.core.block.base.misc;

import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IAtlasProvider {
   @OnlyIn(Dist.CLIENT)
   ResourceLocation getAtlas();

   @OnlyIn(Dist.CLIENT)
   ResourceLocation getAtlasTexture();

   @OnlyIn(Dist.CLIENT)
   default Material createMaterial() {
      return new Material(this.getAtlas(), this.getAtlasTexture());
   }
}
