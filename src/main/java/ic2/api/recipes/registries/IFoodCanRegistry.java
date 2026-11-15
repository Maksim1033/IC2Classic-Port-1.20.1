package ic2.api.recipes.registries;

import ic2.api.recipes.misc.ICanEffect;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IFoodCanRegistry {
   Item registerEffect(ICanEffect var1);

   ICanEffect getEffect(ResourceLocation var1);

   List<ResourceLocation> getAllEffects();

   Item getEffectItem(ResourceLocation var1);

   void registerItemForEffect(ItemStack var1, ResourceLocation var2);

   ResourceLocation getEffectForFood(ItemStack var1);

   default Item getItemForFood(ItemStack food) {
      ResourceLocation location = this.getEffectForFood(food);
      return location == null ? null : this.getEffectItem(location);
   }
}
