package ic2.core.platform.rendering.features.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IToolModel extends ICustomItemModelTransform {
   @Override
   default boolean hasCustomTransform(ItemStack stack) {
      return true;
   }

   @Override
   default ResourceLocation getCustomTransform(ItemStack stack) {
      return new ResourceLocation("minecraft:models/item/handheld");
   }
}
