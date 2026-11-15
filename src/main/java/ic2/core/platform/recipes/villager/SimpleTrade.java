package ic2.core.platform.recipes.villager;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public class SimpleTrade implements ItemListing {
   ITradeComp[] comps = new ITradeComp[3];
   int maxUses;
   int villagerXP;
   float zombieCheese;

   public SimpleTrade(ITradeComp... comps) {
      this(16, 5, 0.05F, comps);
   }

   public SimpleTrade(int maxUses, int villagerXP, float zombieCheese, ITradeComp... comps) {
      this.maxUses = maxUses;
      this.villagerXP = villagerXP;
      this.zombieCheese = zombieCheese;

      for (ITradeComp comp : comps) {
         this.comps[comp.getTarget().ordinal()] = comp;
      }
   }

   public ItemStack build(ITradeComp.Target target, RandomSource rand) {
      return this.comps[target.ordinal()] == null ? ItemStack.EMPTY : this.comps[target.ordinal()].getItem(rand);
   }

   public MerchantOffer getOffer(Entity entity, RandomSource random) {
      return new MerchantOffer(
         this.build(ITradeComp.Target.MAIN, random),
         this.build(ITradeComp.Target.SUB, random),
         this.build(ITradeComp.Target.OUT, random),
         this.maxUses,
         this.villagerXP,
         this.zombieCheese
      );
   }
}
