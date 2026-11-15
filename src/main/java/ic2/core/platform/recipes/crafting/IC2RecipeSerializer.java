package ic2.core.platform.recipes.crafting;

import com.google.gson.JsonObject;
import ic2.core.platform.recipes.misc.AdvRecipeRegistry;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.SimpleRegistry;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public class IC2RecipeSerializer implements RecipeSerializer<RecipeIC2Base> {
   public static final IC2RecipeSerializer INSTANCE = new IC2RecipeSerializer();
   public static final SimpleRegistry<BiFunction<FriendlyByteBuf, ResourceLocation, RecipeIC2Base>> RECIPE_SERIALIZER = CollectionUtils.createRegistry();
   public static final SimpleRegistry<BiFunction<ResourceLocation, JsonObject, RecipeIC2Base>> RECIPE_PARSER = CollectionUtils.createRegistry();

   public static void register() {
      ForgeRegistries.RECIPE_SERIALIZERS.register(new ResourceLocation("ic2", "crafting_ic2_automated"), INSTANCE);
      ForgeRegistries.RECIPE_SERIALIZERS.register(new ResourceLocation("ic2", "cooking_ic2_serializer"), new CookingSerializer());
      ForgeRegistries.RECIPE_SERIALIZERS.register(new ResourceLocation("ic2", "single_item_ic2_serializer"), new SingleSerializer());
      ForgeRegistries.RECIPE_SERIALIZERS.register(new ResourceLocation("ic2", "smithing_ic2_serializer"), new SmithingSerializer());
      RECIPE_SERIALIZER.register(new ResourceLocation("ic2", "shaped"), ShapedIC2Recipe::new);
      RECIPE_SERIALIZER.register(new ResourceLocation("ic2", "shapeless"), ShapelessIC2Recipe::new);
      RECIPE_SERIALIZER.register(new ResourceLocation("ic2", "repair"), RepairRecipe::new);
      RECIPE_PARSER.register(new ResourceLocation("ic2", "shaped"), ShapedIC2Recipe::create);
      RECIPE_PARSER.register(new ResourceLocation("ic2", "shapeless"), ShapelessIC2Recipe::create);
      RECIPE_PARSER.register(new ResourceLocation("ic2", "repair"), RepairRecipe::create);
   }

   @Nullable
   public RecipeIC2Base fromJson(ResourceLocation recipeId, JsonObject json) {
      if (!GsonHelper.getAsBoolean(json, "enabled", true)) {
         return null;
      } else {
         return json.has("sub_type")
            ? RECIPE_PARSER.getValue(ResourceLocation.tryParse(GsonHelper.getAsString(json, "sub_type"))).apply(recipeId, json)
            : AdvRecipeRegistry.INSTANCE.getRecipes().getValue(recipeId);
      }
   }

   public RecipeIC2Base fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
      try {
         return RECIPE_SERIALIZER.getValue(buffer.readResourceLocation()).apply(buffer, recipeId);
      } catch (Exception var4) {
         var4.printStackTrace();
         return null;
      }
   }

   public void toNetwork(FriendlyByteBuf buffer, RecipeIC2Base recipe) {
      try {
         buffer.writeResourceLocation(recipe.getMetaSerializer());
         recipe.serialize(buffer);
      } catch (Exception var4) {
         var4.printStackTrace();
         throw new RuntimeException(var4);
      }
   }
}
