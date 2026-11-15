package ic2.core.block.machines.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IRecipeList {
   void writeRecipes(FriendlyByteBuf var1);

   void readRecipes(FriendlyByteBuf var1);

   default Map<ResourceLocation, JsonObject> writeRecipes() {
      return Collections.emptyMap();
   }

   default void readRecipe(ResourceLocation id, JsonObject data) throws Exception {
   }

   default String getFolder() {
      return "Empty";
   }

   default void onJsonRecipeListLoaded() {
   }

   static JsonObject mapArray(String id, JsonArray array) {
      JsonObject obj = new JsonObject();
      obj.add(id, array);
      return obj;
   }

   static boolean isRemover(JsonObject obj) {
      return obj.has("remove");
   }

   static void getEntriesToRemove(JsonObject obj, Consumer<ResourceLocation> toRemove) {
      iteratePrimitive(obj.get("remove"), T -> {
         ResourceLocation result = ResourceLocation.tryParse(T.getAsString());
         if (result != null) {
            toRemove.accept(result);
         }
      });
   }

   static void iterateObject(JsonElement element, Consumer<JsonObject> result) {
      if (element != null) {
         if (element.isJsonObject()) {
            result.accept(element.getAsJsonObject());
         } else if (element.isJsonArray()) {
            for (JsonElement el : element.getAsJsonArray()) {
               iterateObject(el, result);
            }
         }
      }
   }

   static void iteratePrimitive(JsonElement element, Consumer<JsonPrimitive> result) {
      if (element != null) {
         if (element.isJsonPrimitive()) {
            result.accept(element.getAsJsonPrimitive());
         } else if (element.isJsonArray()) {
            for (JsonElement el : element.getAsJsonArray()) {
               iteratePrimitive(el, result);
            }
         }
      }
   }
}
