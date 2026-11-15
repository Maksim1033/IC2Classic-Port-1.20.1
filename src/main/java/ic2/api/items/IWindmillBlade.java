package ic2.api.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IWindmillBlade {
   int getRadius(ItemStack var1);

   float getEffectiveness(ItemStack var1);

   ResourceLocation getTexture(ItemStack var1);
}
