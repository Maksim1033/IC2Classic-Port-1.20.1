package ic2.core.platform.rendering.features.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface ICustomItemModelTransform {
   boolean hasCustomTransform(ItemStack var1);

   ResourceLocation getCustomTransform(ItemStack var1);
}
