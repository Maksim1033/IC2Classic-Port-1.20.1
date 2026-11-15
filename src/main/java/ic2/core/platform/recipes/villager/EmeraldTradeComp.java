package ic2.core.platform.recipes.villager;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EmeraldTradeComp implements ITradeComp {
   int min;
   int max;
   ITradeComp.Target target;

   public EmeraldTradeComp(int count, ITradeComp.Target target) {
      this(count, count, target);
   }

   public EmeraldTradeComp(int min, int max, ITradeComp.Target target) {
      this.min = min;
      this.max = max;
      this.target = target;
   }

   @Override
   public ItemStack getItem(RandomSource rand) {
      return new ItemStack(Items.EMERALD, this.min == this.max ? this.min : this.min + rand.nextInt(this.max - this.min + 1));
   }

   @Override
   public ITradeComp.Target getTarget() {
      return this.target;
   }
}
