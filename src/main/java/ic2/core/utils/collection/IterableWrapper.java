package ic2.core.utils.collection;

import java.util.Iterator;
import java.util.stream.Stream;

public class IterableWrapper<T> implements Iterable<T> {
   Iterator<T> iter;

   IterableWrapper(Iterator<T> iter) {
      this.iter = iter;
   }

   @Override
   public Iterator<T> iterator() {
      return this.iter;
   }

   public static <T> Iterable<T> wrap(Iterator<T> iter) {
      return new IterableWrapper<>(iter);
   }

   public static <T> Iterable<T> wrap(Stream<T> stream) {
      return new IterableWrapper<>(stream.iterator());
   }
}
