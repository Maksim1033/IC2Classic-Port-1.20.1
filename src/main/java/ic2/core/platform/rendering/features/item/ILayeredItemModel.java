package ic2.core.platform.rendering.features.item;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ILayeredItemModel {
   boolean isLayered(ItemStack var1);

   int getLayerCount(ItemStack var1);

   @OnlyIn(Dist.CLIENT)
   TextureAtlasSprite getSpriteForLayer(ItemStack var1, int var2);
}
