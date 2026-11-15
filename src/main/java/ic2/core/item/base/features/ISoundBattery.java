package ic2.core.item.base.features;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface ISoundBattery {
   boolean wantsToPlay(ItemStack var1);

   ResourceLocation getSound(ItemStack var1);
}
