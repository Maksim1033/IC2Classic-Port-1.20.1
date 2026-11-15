package ic2.api.tiles.tubes;

import ic2.api.util.SidedObject;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

public interface IItemCache {
   SidedObject<IItemCache> CACHE = new SidedObject<>();

   static IItemCache getCache() {
      return CACHE.get();
   }

   Supplier<ItemStack> getItem(int var1);

   int registerItem(ItemStack var1);

   void updateCache();

   void clearCache();
}
