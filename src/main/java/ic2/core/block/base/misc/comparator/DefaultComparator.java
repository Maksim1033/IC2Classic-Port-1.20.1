package ic2.core.block.base.misc.comparator;

import net.minecraft.network.chat.Component;

final class DefaultComparator extends BaseComparator {
   static final BaseComparator INSTANCE = new DefaultComparator();

   private DefaultComparator() {
      super("default", Component.translatable("comparator.ic2.default"));
   }

   @Override
   protected int createValue() {
      return 0;
   }
}
