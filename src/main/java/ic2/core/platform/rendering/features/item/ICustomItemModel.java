package ic2.core.platform.rendering.features.item;

import ic2.core.platform.rendering.models.BaseModel;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICustomItemModel extends IMultiItemModel {
   List<ItemStack> getCustomTypes();

   @OnlyIn(Dist.CLIENT)
   BaseModel getModel(ItemStack var1);
}
