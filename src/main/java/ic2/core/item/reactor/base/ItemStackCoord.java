package ic2.core.item.reactor.base;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import net.minecraft.world.item.ItemStack;

public class ItemStackCoord {
   public ItemStack stack;
   public int x;
   public int y;

   public ItemStackCoord(ItemStack stack, int x, int y) {
      this.stack = stack;
      this.x = x;
      this.y = y;
   }

   public int storeHeat(IReactorComponent component, IReactor reactor, int toAdd) {
      return component.storeHeat(this.stack, reactor, this.x, this.y, toAdd);
   }

   public int getTransferRate(IReactorComponent component, IReactor reactor, double medium) {
      return (int)(
         medium * (double)component.getMaxStoredHeat(this.stack, reactor, this.x, this.y)
            - (double)component.getStoredHeat(this.stack, reactor, this.x, this.y)
      );
   }

   public static class ItemStackDirCoord extends ItemStackCoord {
      double average;

      public ItemStackDirCoord(ItemStack stack, int x, int y, double average) {
         super(stack, x, y);
         this.average = average;
      }

      public double average() {
         return this.average;
      }
   }
}
