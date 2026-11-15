package ic2.core.block.base.cache;

import ic2.api.util.DirectionList;
import java.util.Iterator;
import net.minecraft.core.Direction;

public interface ICache<T> extends Iterable<Direction> {
   void markDirty();

   void invalidateCache();

   void setCallback(Runnable var1);

   void update();

   T getHandler(Direction var1);

   DirectionList getPresentSides();

   default boolean isEmpty() {
      return this.getPresentSides().isEmpty();
   }

   default boolean contains(Direction dir) {
      return this.getPresentSides().contains(dir);
   }

   @Override
   default Iterator<Direction> iterator() {
      return this.getPresentSides().iterator();
   }
}
