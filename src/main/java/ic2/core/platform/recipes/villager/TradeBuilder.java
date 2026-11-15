package ic2.core.platform.recipes.villager;

import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;

public class TradeBuilder {
   Int2ObjectMap<List<ItemListing>> trades = new Int2ObjectOpenHashMap();
   int highestLevel = 1;

   public TradeBuilder addTrade(int level, ITradeComp... comps) {
      return this.addTrade(level, new SimpleTrade(comps));
   }

   public TradeBuilder addTrade(int level, int maxUses, ITradeComp... comps) {
      return this.addTrade(level, new SimpleTrade(maxUses, 5, 0.05F, comps));
   }

   public TradeBuilder addTrade(int level, int maxUses, float villagerCheese, ITradeComp... comps) {
      return this.addTrade(level, new SimpleTrade(maxUses, 5, villagerCheese, comps));
   }

   public TradeBuilder addTrade(int level, int maxUses, int xp, float villagerCheese, ITradeComp... comps) {
      return this.addTrade(level, new SimpleTrade(maxUses, xp, villagerCheese, comps));
   }

   public TradeBuilder addTrade(int level, ItemListing trade) {
      ((List)this.trades.computeIfAbsent(level, T -> CollectionUtils.createList())).add(trade);
      this.highestLevel = Math.max(level, this.highestLevel);
      return this;
   }

   public TradeBuilder addTrades(int[] level, ITradeComp... comps) {
      return this.addTrades(level, new SimpleTrade(comps));
   }

   public TradeBuilder addTrades(int[] level, int maxUses, ITradeComp... comps) {
      return this.addTrades(level, new SimpleTrade(maxUses, 5, 0.05F, comps));
   }

   public TradeBuilder addTrades(int[] level, int maxUses, float villagerCheese, ITradeComp... comps) {
      return this.addTrades(level, new SimpleTrade(maxUses, 5, villagerCheese, comps));
   }

   public TradeBuilder addTrades(int[] level, int maxUses, int xp, float villagerCheese, ITradeComp... comps) {
      return this.addTrades(level, new SimpleTrade(maxUses, xp, villagerCheese, comps));
   }

   public void limitTrades(int tradeLimit) {
      ObjectIterator var2 = this.trades.values().iterator();

      while (var2.hasNext()) {
         List<ItemListing> entry = (List<ItemListing>)var2.next();
         Collections.shuffle(entry);

         while (entry.size() > tradeLimit && !entry.isEmpty()) {
            entry.remove(entry.size() - 1);
         }
      }
   }

   public TradeBuilder addTrades(int[] level, ItemListing trade) {
      for (int j : level) {
         ((List)this.trades.computeIfAbsent(j, T -> CollectionUtils.createList())).add(trade);
         this.highestLevel = Math.max(j, this.highestLevel);
      }

      return this;
   }

   public Int2ObjectMap<ItemListing[]> build() {
      Int2ObjectMap<ItemListing[]> trades = new Int2ObjectOpenHashMap();
      Int2ObjectMaps.fastForEach(this.trades, T -> {
         List<ItemListing> values = (List<ItemListing>)T.getValue();
         trades.put(T.getIntKey(), values.toArray(ItemListing[]::new));
      });

      for (int i = 1; i < this.highestLevel; i++) {
         trades.putIfAbsent(i, new ItemListing[0]);
      }

      return trades;
   }

   public void buildVillager(VillagerTradesEvent event) {
      Int2ObjectMaps.fastForEach(this.trades, T -> ((List)event.getTrades().computeIfAbsent(T.getIntKey(), this::create)).addAll((Collection)T.getValue()));
   }

   public void buildWanderingTrade(WandererTradesEvent event, boolean rare) {
      List<ItemListing> target = rare ? event.getRareTrades() : event.getGenericTrades();
      this.trades.values().forEach(target::addAll);
   }

   private List<ItemListing> create(int ignored) {
      return CollectionUtils.<ItemListing>createList();
   }
}
