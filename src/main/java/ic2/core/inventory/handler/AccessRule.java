package ic2.core.inventory.handler;

import ic2.core.utils.collection.CollectionUtils;
import net.minecraft.network.chat.Component;

public enum AccessRule implements CollectionUtils.IIndexEnum {
   DISABLED(0, "disabled"),
   IMPORT(1, "import"),
   EXPORT(2, "export"),
   BOTH(3, "both");

   final int index;
   final String translation;
   static final AccessRule[] VALUES = CollectionUtils.createSortedArray(values());

   private AccessRule(int index, String name) {
      this.index = index;
      this.translation = "misc.ic2.access." + name;
   }

   @Override
   public int getIndex() {
      return this.index;
   }

   public Component getName() {
      return Component.translatable(this.translation);
   }

   public boolean canImport() {
      return this == IMPORT || this == BOTH;
   }

   public boolean canExport() {
      return this == EXPORT || this == BOTH;
   }

   public static AccessRule byIndex(int index) {
      return VALUES[index % VALUES.length];
   }

   public AccessRule cycleRule(AccessRule rule) {
      if (rule == DISABLED) {
         return DISABLED;
      } else if (rule == BOTH) {
         if (this == BOTH) {
            return DISABLED;
         } else if (this == DISABLED) {
            return EXPORT;
         } else {
            return this == EXPORT ? IMPORT : BOTH;
         }
      } else {
         return this == rule ? DISABLED : rule;
      }
   }
}
