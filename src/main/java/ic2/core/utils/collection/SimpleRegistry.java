package ic2.core.utils.collection;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;

public class SimpleRegistry<V> implements Iterable<V> {
   Map<ResourceLocation, V> registries = CollectionUtils.<ResourceLocation, V>createLinkedMap();
   Map<String, List<ResourceLocation>> modSorted = CollectionUtils.<String, List<ResourceLocation>>createLinkedMap();

   public void register(ResourceLocation location, V value) {
      this.registries.put(location, value);
      List<ResourceLocation> locations = this.modSorted.computeIfAbsent(location.getNamespace(), k -> CollectionUtils.createList());
      locations.add(location);
   }

   public V getValue(ResourceLocation location) {
      return this.registries.get(location);
   }

   public boolean contains(ResourceLocation location) {
      return this.registries.containsKey(location);
   }

   public boolean remove(ResourceLocation location) {
      V value = this.registries.remove(location);
      if (value != null) {
         List<ResourceLocation> modSpecific = this.modSorted.get(location.getNamespace());
         if (modSpecific != null) {
            modSpecific.remove(location);
         }

         return true;
      } else {
         return false;
      }
   }

   public Set<ResourceLocation> keySet() {
      return this.registries.keySet();
   }

   @Override
   public Iterator<V> iterator() {
      return this.registries.values().iterator();
   }

   public Set<Entry<ResourceLocation, V>> entrySet() {
      return this.registries.entrySet();
   }

   public List<ResourceLocation> getResourceForMod(String mod) {
      List<ResourceLocation> list = this.modSorted.get(mod);
      return (List<ResourceLocation>)(list == null ? ObjectLists.emptyList() : list);
   }

   public void clear() {
      this.registries.clear();
      this.modSorted.clear();
   }
}
