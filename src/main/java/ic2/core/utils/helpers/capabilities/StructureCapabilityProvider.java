package ic2.core.utils.helpers.capabilities;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class StructureCapabilityProvider<Y> implements IToggleableCapabilityProvider {
   Capability<Y> capability;
   LazyOptional<Y> value;
   Y entry;
   boolean active;

   public StructureCapabilityProvider(Capability<Y> capability, Y entry) {
      this(capability, entry, true);
   }

   public StructureCapabilityProvider(Capability<Y> capability, Y entry, boolean active) {
      this.capability = capability;
      this.entry = entry;
      this.active = active;
      this.value = active && entry != null ? LazyOptional.of(() -> entry) : null;
   }

   @SuppressWarnings("unchecked")
   public static <T> StructureCapabilityProvider<T> create(Capability<T> cap, Object entry) {
      return new StructureCapabilityProvider<T>(cap, (T)entry);
   }

   @SuppressWarnings("unchecked")
   public static <T> StructureCapabilityProvider<T> create(Capability<T> cap, Object entry, boolean active) {
      return new StructureCapabilityProvider<T>(cap, (T)entry, active);
   }

   @Override
   public void setActive(boolean value) {
      if (this.active != value) {
         this.active = value;
         if (this.active && this.value == null) {
            this.value = LazyOptional.of(() -> this.entry);
         } else if (!this.active && this.value != null) {
            this.value.invalidate();
            this.value = null;
         }
      }
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      return this.active && this.capability == cap && this.value != null ? this.value.cast() : LazyOptional.empty();
   }

   @Override
   public boolean isActive() {
      return this.active;
   }
}
