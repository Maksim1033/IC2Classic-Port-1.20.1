package ic2.core.platform.recipes.villager;

import ic2.api.crops.ICrop;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface ITradeComp {
   ItemStack getItem(RandomSource var1);

   ITradeComp.Target getTarget();

   static ITradeComp emeralds(int amount, ITradeComp.Target target) {
      return new EmeraldTradeComp(amount, target);
   }

   static ITradeComp emeralds(int min, int max, ITradeComp.Target target) {
      return new EmeraldTradeComp(min, max, target);
   }

   static ITradeComp item(ItemLike item, ITradeComp.Target target) {
      return new ItemTradeComp(item, 1, target);
   }

   static ITradeComp item(ItemLike item, int count, ITradeComp.Target target) {
      return new ItemTradeComp(item, count, target);
   }

   static ITradeComp item(ItemLike item, int min, int max, ITradeComp.Target target) {
      return new ItemTradeComp(item, min, max, target);
   }

   static ITradeComp item(ItemStack item, ITradeComp.Target target) {
      return new ItemTradeComp(item, 1, target);
   }

   static ITradeComp item(ItemStack item, int count, ITradeComp.Target target) {
      return new ItemTradeComp(item, count, target);
   }

   static ITradeComp item(ItemStack item, int min, int max, ITradeComp.Target target) {
      return new ItemTradeComp(item, min, max, target);
   }

   static ITradeComp crop(ICrop crop, int stat, boolean scan, ITradeComp.Target target) {
      return new CropTradeComp(crop, stat, scan, target);
   }

   static ITradeComp crop(ICrop crop, int min, int max, boolean scan, ITradeComp.Target target) {
      return new CropTradeComp(crop, min, max, scan, target);
   }

   public static enum Target {
      MAIN,
      SUB,
      OUT;
   }
}
