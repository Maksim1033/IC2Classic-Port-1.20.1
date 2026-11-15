package ic2.core.platform.recipes.mods;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public interface IRecipeModifier {
   void reset();

   boolean isSlotValid(ItemStack var1);

   boolean isOutputItem(ItemStack var1);

   ItemStack applyChanges(ItemStack var1, boolean var2);

   void serialize(FriendlyByteBuf var1);

   JsonObject serialize();
}
