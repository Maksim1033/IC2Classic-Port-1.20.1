package ic2.core.platform.recipes.crafting;

import com.google.gson.JsonObject;
import ic2.core.platform.recipes.misc.AdvRecipeRegistry;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CookingSerializer implements RecipeSerializer<AbstractCookingRecipe> {
   @Nullable
   public AbstractCookingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
      if (json.get("enabled").getAsBoolean()) {
         Supplier<AbstractCookingRecipe> provider = AdvRecipeRegistry.INSTANCE.getCooking().getValue(recipeId);
         return provider == null ? null : provider.get();
      } else {
         return null;
      }
   }

   public AbstractCookingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
      return null;
   }

   public void toNetwork(FriendlyByteBuf buffer, AbstractCookingRecipe recipe) {
   }
}
