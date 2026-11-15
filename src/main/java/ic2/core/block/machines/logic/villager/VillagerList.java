package ic2.core.block.machines.logic.villager;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.platform.corehacks.mixins.server.VillagerMixin;
import ic2.core.platform.events.IC2EventHandler;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.entity.LevelEntityGetter;

public class VillagerList implements INetworkDataBuffer {
   Map<UUID, VillagerTradeList> trades = CollectionUtils.<UUID, VillagerTradeList>createMap();
   Set<UUID> activeTrades = CollectionUtils.<UUID>createLinkedSet();

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeVarInt(this.activeTrades.size());

      for (UUID owner : this.activeTrades) {
         this.trades.get(owner).write(buffer);
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.trades.clear();
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         VillagerTradeList list = new VillagerTradeList(buffer);
         this.trades.put(list.getOwner(), list);
      }
   }

   public CompoundTag save(CompoundTag nbt) {
      ListTag list = new ListTag();

      for (VillagerTradeList trade : this.trades.values()) {
         CompoundTag tag = new CompoundTag();
         tag.put("trade", trade.save());
         NBTUtils.putBoolean(tag, "active", this.activeTrades.contains(trade.getOwner()), false);
         list.add(tag);
      }

      NBTUtils.put(nbt, "trades", list);
      return nbt;
   }

   public void load(CompoundTag nbt) {
      for (CompoundTag tag : NBTListWrapper.wrap(nbt.getList("trades", 10), CompoundTag.class)) {
         VillagerTradeList list = new VillagerTradeList(tag.getCompound("trade"));
         this.trades.put(list.getOwner(), list);
         if (tag.getBoolean("active")) {
            this.activeTrades.add(list.getOwner());
         }
      }
   }

   public int getActiveTrades() {
      return this.activeTrades.size();
   }

   public List<UUID> getVisibleVillagerIds() {
      return new ObjectArrayList(this.trades.keySet());
   }

   public List<VillagerTradeList> getVisibleVillagerTrades() {
      return new ObjectArrayList(this.trades.values());
   }

   public VillagerTradeList getTrade(UUID owner) {
      return this.trades.get(owner);
   }

   public boolean loadData(UUID owner, int index, boolean state) {
      VillagerTradeList list = this.trades.get(owner);
      if (list == null) {
         return false;
      } else {
         List<VillagerTrade> trades = list.getKnownTrades();
         if (index >= 0 && index < trades.size()) {
            trades.get(index).setState(state);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean update(List<Villager> villagers, Player trader) {
      boolean changed = false;
      Set<UUID> toRemove = new ObjectLinkedOpenHashSet(this.activeTrades);

      for (Villager villager : villagers) {
         UUID id = villager.getUUID();
         toRemove.remove(id);
         if (villager.getTradingPlayer() == null) {
            changed |= this.activeTrades.add(id);
            this.startTrading(trader, villager);
            VillagerTradeList list = this.trades.get(id);
            if (list == null) {
               list = new VillagerTradeList(villager);
               this.trades.put(id, list);
               changed = true;
               villager.setTradingPlayer(null);
            } else {
               changed |= list.updateTrades(villager);
               villager.setTradingPlayer(null);
            }
         }
      }

      changed |= this.activeTrades.removeAll(toRemove);

      for (UUID id : toRemove) {
         VillagerTradeList list = this.trades.get(id);
         if (list != null && list.isDefault()) {
            this.trades.remove(id);
            changed = true;
         }
      }

      return changed;
   }

   public boolean trade(LevelEntityGetter<Entity> getter, Player trader, IItemTransporter input, IItemTransporter output, IntConsumer xp) {
      boolean result = false;

      for (UUID activeTrade : this.activeTrades) {
         Entity entity = (Entity)getter.get(activeTrade);
         if (entity instanceof Villager) {
            Villager villager = (Villager)entity;
            if (villager.getTradingPlayer() == null) {
               VillagerTradeList list = this.trades.get(activeTrade);
               if (list != null) {
                  this.startTrading(trader, villager);
                  boolean didTrade = false;
                  MerchantOffers offers = villager.getOffers();

                  for (VillagerTrade trade : list.getKnownTrades()) {
                     if (trade.getState() && !trade.isOutOfStock()) {
                        for (MerchantOffer offer = (MerchantOffer)offers.get(trade.getIndex());
                           !offer.isOutOfStock() && this.trade(input, output, offer, true);
                           didTrade = true
                        ) {
                           this.trade(input, output, offer, false);
                           IC2EventHandler.XP_ABSORBER = xp;
                           villager.notifyTrade(offer);
                           IC2EventHandler.XP_ABSORBER = null;
                        }
                     }
                  }

                  if (didTrade) {
                     list.updateTrades(villager);
                     result = true;
                  }

                  villager.setTradingPlayer(null);
               }
            }
         }
      }

      return result;
   }

   public boolean trade(IItemTransporter input, IItemTransporter output, MerchantOffer offer, boolean simulate) {
      ItemStack main = offer.getCostA();
      ItemStack sub = offer.getCostB();
      ItemStack out = offer.getResult();
      if (input.removeItem(StackFilter.defaultCompare(main), null, main.getCount(), simulate).getCount() < main.getCount()) {
         return false;
      } else {
         return !sub.isEmpty() && input.removeItem(StackFilter.defaultCompare(sub), null, sub.getCount(), simulate).getCount() < sub.getCount()
            ? false
            : output.addItem(out, null, simulate) >= out.getCount();
      }
   }

   protected void startTrading(Player player, Villager villager) {
      if (player != null) {
         ((VillagerMixin)villager).updatePrices(player);
         villager.setTradingPlayer(player);
      }
   }
}
