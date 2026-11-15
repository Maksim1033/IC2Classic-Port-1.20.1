package ic2.core.block.base.misc.color;

public interface IColorListener {
   default boolean needsColoring() {
      return true;
   }
}
