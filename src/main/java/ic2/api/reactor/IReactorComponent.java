package ic2.api.reactor;

import net.minecraft.world.item.ItemStack;

public interface IReactorComponent extends IReactorProduct {
   @Override
   default boolean isValidForReactor(ItemStack stack, IReactor reactor) {
      return true;
   }

   void processChamber(ItemStack var1, IReactor var2, int var3, int var4, boolean var5, boolean var6);

   boolean acceptUraniumPulse(ItemStack var1, IReactor var2, ItemStack var3, int var4, int var5, int var6, int var7, boolean var8, boolean var9);

   boolean canStoreHeat(ItemStack var1, IReactor var2, int var3, int var4);

   int getStoredHeat(ItemStack var1, IReactor var2, int var3, int var4);

   int getMaxStoredHeat(ItemStack var1, IReactor var2, int var3, int var4);

   int storeHeat(ItemStack var1, IReactor var2, int var3, int var4, int var5);

   float getExplosionInfluence(ItemStack var1, IReactor var2);
}
