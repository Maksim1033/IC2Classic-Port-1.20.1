package ic2.core.platform.rendering.features.item;

import ic2.core.utils.plugins.IRegistryProvider;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IMultiItemModel extends IRegistryProvider {
   int getModelIndexForStack(ItemStack var1, @Nullable LivingEntity var2);
}
