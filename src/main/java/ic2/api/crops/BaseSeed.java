package ic2.api.crops;

import net.minecraft.world.item.ItemStack;

public class BaseSeed {
   public final ICrop crop;
   public int stage;
   public int growth;
   public int gain;
   public int resistance;
   public int stack_size;

   public BaseSeed(ICrop crop, int stage, int growth, int gain, int resistance, int stack_size) {
      this.crop = crop;
      this.stage = stage;
      this.growth = growth;
      this.gain = gain;
      this.resistance = resistance;
      this.stack_size = stack_size;
   }

   public BaseSeed(ItemStack stack) {
      ICropSeed seed = (ICropSeed)stack.getItem();
      this.crop = seed.getCrop(stack);
      this.stage = 1;
      this.growth = seed.getGrowth(stack);
      this.gain = seed.getGain(stack);
      this.resistance = seed.getResistance(stack);
      this.stack_size = 1;
   }
}
