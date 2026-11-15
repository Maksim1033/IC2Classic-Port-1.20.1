package ic2.core.wiki.recipes.providers;

import ic2.core.utils.collection.CollectionUtils;
import ic2.core.wiki.recipes.renderers.IRecipeRenderer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

public interface IRecipeProvider {
   void reloadRecipes();

   List<IRecipeRenderer> getFromInput(ItemStack var1, boolean var2);

   List<IRecipeRenderer> getFromOutput(ItemStack var1, boolean var2);

   default <K, V> void addToSet(K key, V value, Map<K, Set<V>> map) {
      Set<V> set = map.computeIfAbsent(key, k -> CollectionUtils.createLinkedSet());
      set.add(value);
   }

   default <K, V> void addToList(K key, V value, Map<K, List<V>> map) {
      List<V> list = map.computeIfAbsent(key, k -> CollectionUtils.createList());
      list.add(value);
   }
}
