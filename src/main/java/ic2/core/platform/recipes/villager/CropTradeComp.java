package ic2.core.platform.recipes.villager;

import ic2.api.crops.ICrop;
import ic2.core.item.misc.CropSeedItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class CropTradeComp implements ITradeComp {
   ICrop crop;
   int minStats;
   int maxStats;
   boolean scanned;
   ITradeComp.Target target;

   public CropTradeComp(ICrop crop, int stats, boolean scanned, ITradeComp.Target target) {
      this(crop, stats, stats, scanned, target);
   }

   public CropTradeComp(ICrop crop, int minStats, int maxStats, boolean scanned, ITradeComp.Target target) {
      this.crop = crop;
      this.minStats = minStats;
      this.maxStats = maxStats;
      this.scanned = scanned;
      this.target = target;
   }

   private int getStat(RandomSource rand) {
      return this.minStats == this.maxStats ? this.minStats : this.minStats + rand.nextInt(this.maxStats - this.minStats + 1);
   }

   @Override
   public ItemStack getItem(RandomSource rand) {
      return CropSeedItem.createStack(this.crop, this.getStat(rand), this.getStat(rand), this.getStat(rand), this.scanned ? 4 : 0);
   }

   @Override
   public ITradeComp.Target getTarget() {
      return this.target;
   }
}
