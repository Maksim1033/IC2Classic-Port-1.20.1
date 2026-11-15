package ic2.core.block.base.cache;

import ic2.api.util.DirectionList;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class DirectionalCapability<K> implements ICapabilityProvider {
   LazyOptional<K>[] optionals = new LazyOptional[6];
   Capability<K> cap;
   K value;
   DirectionList dirs;

   public DirectionalCapability(Capability<K> cap, K value) {
      this(cap, value, DirectionList.ALL);
   }

   public DirectionalCapability(Capability<K> cap, K value, DirectionList initials) {
      this.cap = cap;
      this.value = value;
      this.dirs = initials;

      for (Direction dir : DirectionList.ALL) {
         this.optionals[dir.get3DDataValue()] = initials.contains(dir) ? LazyOptional.of(() -> value) : null;
      }
   }

   public void setActiveDirections(DirectionList newDirs) {
      if (this.dirs.getCode() != newDirs.getCode()) {
         DirectionList toRemove = this.dirs.remove(newDirs);
         DirectionList toAdd = newDirs.remove(this.dirs);

         for (Direction dir : toRemove) {
            if (this.optionals[dir.get3DDataValue()] != null) {
               this.optionals[dir.get3DDataValue()].invalidate();
               this.optionals[dir.get3DDataValue()] = null;
            }
         }

         for (Direction dirx : toAdd) {
            if (this.optionals[dirx.get3DDataValue()] == null) {
               this.optionals[dirx.get3DDataValue()] = LazyOptional.of(() -> this.value);
            }
         }

         this.dirs = newDirs;
      }
   }

   public void unload() {
      for (Direction dir : this.dirs) {
         if (this.optionals[dir.get3DDataValue()] != null) {
            this.optionals[dir.get3DDataValue()].invalidate();
            this.optionals[dir.get3DDataValue()] = null;
         }
      }
   }

   public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
      return cap == this.cap && side != null && this.optionals[side.get3DDataValue()] != null
         ? this.optionals[side.get3DDataValue()].cast()
         : LazyOptional.empty();
   }
}
