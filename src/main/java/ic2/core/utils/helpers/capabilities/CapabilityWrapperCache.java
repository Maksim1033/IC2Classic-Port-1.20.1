package ic2.core.utils.helpers.capabilities;

import ic2.core.utils.collection.CollectionUtils;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.apache.commons.lang3.mutable.MutableObject;

public class CapabilityWrapperCache implements ICapabilityProvider {
   Map<Capability<?>, LazyOptional<?>>[] wrapper = CollectionUtils.<Capability<?>, LazyOptional<?>>createMaps(7, false);
   BiFunction<Capability<?>, MutableObject<Object>, MutableObject<Object>> values;
   ICapabilityProvider owner;

   public void setOwner(ICapabilityProvider provider) {
      if (provider != null) {
         this.owner = provider;
      }
   }

   public boolean hasOwner() {
      return this.owner != null;
   }

   public void clearOwner() {
      if (this.owner != null) {
         this.owner = null;

         for (Map<Capability<?>, LazyOptional<?>> capabilityLazyOptionalMap : this.wrapper) {
            capabilityLazyOptionalMap.values().forEach(LazyOptional::invalidate);
            capabilityLazyOptionalMap.clear();
         }
      }
   }

   public void setMapper(BiFunction<Capability<?>, MutableObject<Object>, MutableObject<Object>> mapper) {
      this.values = mapper;
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      if (this.owner == null) {
         return LazyOptional.empty();
      } else {
         LazyOptional<?> lazy = this.wrapper[side == null ? 6 : side.get3DDataValue()].get(cap);
         if (lazy == null) {
            LazyOptional<T> optional = this.owner.getCapability(cap, side);
            T value = (T)(this.values == null ? optional.orElse(null) : this.values.apply(cap, new MutableObject(optional.orElse(null))).getValue());
            lazy = value == null ? LazyOptional.empty() : LazyOptional.of(() -> value);
            this.wrapper[side == null ? 6 : side.get3DDataValue()].put(cap, lazy);
            if (value != null) {
               optional.addListener(new CapabilityWrapperCache.Listener(this.owner, side, cap));
            }
         }

         return lazy.cast();
      }
   }

   private void onInvalidated(Direction dir, Capability<?> cap) {
      LazyOptional<?> optional = this.wrapper[dir == null ? 6 : dir.get3DDataValue()].remove(cap);
      if (optional != null) {
         optional.invalidate();
      }
   }

   class Listener<T> implements NonNullConsumer<LazyOptional<T>> {
      ICapabilityProvider tempOwner;
      Direction dir;
      Capability<T> cap;

      public Listener(ICapabilityProvider owner, Direction dir, Capability<T> cap) {
         this.tempOwner = owner;
         this.dir = dir;
         this.cap = cap;
      }

      public void accept(LazyOptional<T> t) {
         if (this.tempOwner == CapabilityWrapperCache.this.owner) {
            CapabilityWrapperCache.this.onInvalidated(this.dir, this.cap);
         }
      }
   }
}
