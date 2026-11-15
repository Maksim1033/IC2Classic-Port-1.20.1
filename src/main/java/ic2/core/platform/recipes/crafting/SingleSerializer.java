package ic2.core.platform.recipes.crafting;

import com.google.gson.JsonObject;
import ic2.core.platform.recipes.misc.AdvRecipeRegistry;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleItemRecipe;

public class SingleSerializer implements RecipeSerializer<SingleItemRecipe> {
   @Nullable
   public SingleItemRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
      if (json.get("enabled").getAsBoolean()) {
         Supplier<SingleItemRecipe> provider = AdvRecipeRegistry.INSTANCE.getSingleRecipes().getValue(recipeId);
         return provider == null ? null : provider.get();
      } else {
         return null;
      }
   }

   public SingleItemRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
      return null;
   }

   public void toNetwork(FriendlyByteBuf buffer, SingleItemRecipe recipe) {
   }
}
