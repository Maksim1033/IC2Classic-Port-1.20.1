package ic2.api.crops;

import net.minecraft.world.item.ItemStack;

public interface ICropSeed {
   int[] SCAN_COST = new int[]{10, 90, 900, 9000};
   String[] TIERS = new String[]{"0", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI"};

   ICrop getCrop(ItemStack var1);

   int getScanLevel(ItemStack var1);

   void setScanLevel(ItemStack var1, int var2);

   default void increaseScanLevel(ItemStack stack) {
      this.setScanLevel(stack, this.getScanLevel(stack) + 1);
   }

   int getGrowth(ItemStack var1);

   void setGrowth(ItemStack var1, int var2);

   int getGain(ItemStack var1);

   void setGain(ItemStack var1, int var2);

   int getResistance(ItemStack var1);

   void setResistance(ItemStack var1, int var2);
}
