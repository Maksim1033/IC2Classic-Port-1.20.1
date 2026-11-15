package ic2.core.utils.collection;

import ic2.core.IC2;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;

public class SupplierRegistry<K, V extends Supplier<K>> extends SimpleRegistry<V> {
   Map<Class<?>, ResourceLocation> keys = CollectionUtils.<Class<?>, ResourceLocation>createLinkedMap();

   public void register(ResourceLocation location, V value, Class<? extends K> key) {
      super.register(location, value);
      this.keys.put(key, location);
   }

   @Deprecated
   public void register(ResourceLocation location, V value) {
      IC2.LOGGER.info("Invalid Function Please use the other function");
   }

   public ResourceLocation getKey(K key) {
      return this.getKey(key.getClass());
   }

   public ResourceLocation getKey(Class<?> clz) {
      return this.keys.get(clz);
   }

   public boolean containsValue(K key) {
      return this.keys.containsKey(key.getClass());
   }
}
