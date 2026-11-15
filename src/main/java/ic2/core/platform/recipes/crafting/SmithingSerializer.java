package ic2.core.platform.recipes.crafting;

import com.google.gson.JsonObject;
import ic2.core.platform.recipes.misc.AdvRecipeRegistry;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

public class SmithingSerializer implements RecipeSerializer<SmithingTransformRecipe> {
   @Nullable
   public SmithingTransformRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
      if (json.get("enabled").getAsBoolean()) {
         Supplier<SmithingTransformRecipe> provider = AdvRecipeRegistry.INSTANCE.getSmithingRecipes().getValue(recipeId);
         return provider == null ? null : provider.get();
      } else {
         return null;
      }
   }

   public SmithingTransformRecipe fromNetwork(ResourceLocation p_199426_1_, FriendlyByteBuf p_199426_2_) {
      return null;
   }

   public void toNetwork(FriendlyByteBuf p_199427_1_, SmithingTransformRecipe p_199427_2_) {
   }
}
