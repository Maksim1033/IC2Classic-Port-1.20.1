package ic2.api.recipes.registries;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface IScrapBoxRegistry extends IListenableRegistry<IScrapBoxRegistry> {
   default void addDrop(ItemLike prov, float chance) {
      if (prov != null) {
         this.addDrop(new ItemStack(prov), chance);
      }
   }

   void addDrop(ItemStack var1, float var2);

   IScrapBoxRegistry.IDrop getRandomDrop(ItemStack var1, boolean var2);

   void removeDrops(ItemStack var1);

   void removeDrop(IScrapBoxRegistry.IDrop var1);

   List<IScrapBoxRegistry.IDrop> getAllDrops();

   public interface IDrop {
      ItemStack getDrop();

      float getChance();

      float getPoolChance();
   }
}
