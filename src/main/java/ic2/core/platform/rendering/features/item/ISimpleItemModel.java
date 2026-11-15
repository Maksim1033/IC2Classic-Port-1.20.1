package ic2.core.platform.rendering.features.item;

import ic2.core.utils.plugins.IRegistryProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ISimpleItemModel extends IRegistryProvider {
   @OnlyIn(Dist.CLIENT)
   TextureAtlasSprite getTexture();

   default boolean shouldLoadModel() {
      return true;
   }
}
