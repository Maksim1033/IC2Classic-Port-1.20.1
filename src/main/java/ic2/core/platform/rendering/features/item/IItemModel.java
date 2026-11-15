package ic2.core.platform.rendering.features.item;

import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IItemModel extends IMultiItemModel {
   List<ItemStack> getModelTypes();

   @OnlyIn(Dist.CLIENT)
   TextureAtlasSprite getSprite(ItemStack var1);
}
