package ic2.core.utils.collection;

import java.util.Iterator;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class NBTListWrapper {
   public static <T extends Tag> Iterable<T> wrap(final ListTag nbt, Class<T> clz) {
      return IterableWrapper.wrap(new Iterator<T>() {
         Iterator<Tag> iter = nbt.iterator();

         @Override
         public boolean hasNext() {
            return this.iter.hasNext();
         }

         public T next() {
            return (T)this.iter.next();
         }

         @Override
         public void remove() {
            this.iter.remove();
         }
      });
   }
}
