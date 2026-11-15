package ic2.core.platform.recipes.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.core.IC2;
import ic2.core.utils.collection.CollectionUtils;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class IC2RecipeLoader extends SimpleJsonResourceReloadListener {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   public static final IC2RecipeLoader INSTANCE = new IC2RecipeLoader();

   public IC2RecipeLoader() {
      super(GSON, "ic2_recipes");
   }

   protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resources, ProfilerFiller profiler) {
      Map<String, Map<ResourceLocation, JsonObject>> result = CollectionUtils.<String, Map<ResourceLocation, JsonObject>>createLinkedMap();

      for (Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
         String[] s = entry.getKey().getPath().split("\\/", 2);

         try {
            result.computeIfAbsent(s[0], T -> CollectionUtils.createLinkedMap())
               .put(new ResourceLocation(entry.getKey().getNamespace(), s[1]), GsonHelper.convertToJsonObject(entry.getValue(), "Top Element"));
         } catch (Exception var9) {
            var9.printStackTrace();
         }
      }

      IC2.RECIPES.get(true).loadDataPackRecipes(result);
   }
}
