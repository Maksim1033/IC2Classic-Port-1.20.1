package ic2.core.block.machines.logic.villager;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class VillagerTradeList {
   UUID villager;
   int lastSize;
   int xp;
   List<VillagerTrade> knownTrades = CollectionUtils.<VillagerTrade>createList();

   public VillagerTradeList(Villager villager) {
      this.villager = villager.getUUID();
      this.xp = villager.getVillagerXp();
      MerchantOffers offers = villager.getOffers();
      int i = 0;

      for (int m = offers.size(); i < m; i++) {
         this.knownTrades.add(new VillagerTrade(this.villager, i, (MerchantOffer)offers.get(i)));
      }

      this.lastSize = offers.size();
   }

   public VillagerTradeList(CompoundTag nbt) {
      this.villager = nbt.getUUID("villager");
      this.lastSize = nbt.getInt("size");
      this.xp = nbt.getInt("xp");

      for (CompoundTag tag : NBTListWrapper.wrap(nbt.getList("trades", 10), CompoundTag.class)) {
         this.knownTrades.add(new VillagerTrade(this.villager, tag));
      }
   }

   public VillagerTradeList(IInputBuffer buffer) {
      this.villager = buffer.readUUID();
      this.xp = buffer.readVarInt();
      int size = buffer.readVarInt();
      this.knownTrades.clear();

      for (int i = 0; i < size; i++) {
         this.knownTrades.add(new VillagerTrade(this.villager, buffer));
      }
   }

   public boolean isDefault() {
      for (VillagerTrade trade : this.knownTrades) {
         if (trade.getState()) {
            return false;
         }
      }

      return true;
   }

   public int findIndex(VillagerTrade trade) {
      int i = 0;

      for (int m = this.knownTrades.size(); i < m; i++) {
         if (this.knownTrades.get(i) == trade) {
            return i;
         }
      }

      return -1;
   }

   public List<VillagerTrade> getKnownTrades() {
      return this.knownTrades;
   }

   public UUID getOwner() {
      return this.villager;
   }

   public int getXp() {
      return this.xp;
   }

   public CompoundTag save() {
      CompoundTag nbt = new CompoundTag();
      nbt.putUUID("villager", this.villager);
      nbt.putInt("size", this.lastSize);
      nbt.putInt("xp", this.xp);
      ListTag list = new ListTag();
      int i = 0;

      for (int m = this.knownTrades.size(); i < m; i++) {
         list.add(this.knownTrades.get(i).save());
      }

      NBTUtils.put(nbt, "trades", list);
      return nbt;
   }

   public void write(IOutputBuffer buffer) {
      buffer.writeUUID(this.villager);
      buffer.writeVarInt(this.xp);
      buffer.writeVarInt(this.knownTrades.size());
      int i = 0;

      for (int m = this.knownTrades.size(); i < m; i++) {
         this.knownTrades.get(i).write(buffer);
      }
   }

   public boolean updateTrades(Villager villager) {
      return this.syncTrades(villager, villager.getOffers());
   }

   private boolean syncTrades(Villager villager, MerchantOffers offers) {
      int newSize = offers.size();
      boolean result = false;
      if (newSize > this.lastSize) {
         for (int i = this.lastSize; i < newSize; i++) {
            this.knownTrades.add(new VillagerTrade(this.villager, i, (MerchantOffer)offers.get(i)));
         }

         result = true;
      }

      if (this.xp != villager.getVillagerXp()) {
         this.xp = villager.getVillagerXp();
         result = true;
      }

      this.lastSize = newSize;
      Optional<List<VillagerTrade>> trades = this.rebuildTradeList(offers);
      if (trades.isPresent()) {
         this.knownTrades = trades.get();
         return true;
      } else {
         return result;
      }
   }

   private Optional<List<VillagerTrade>> rebuildTradeList(MerchantOffers offers) {
      boolean changed = false;
      List<VillagerTrade> trades = CollectionUtils.<VillagerTrade>createList();
      int i = 0;

      for (int m = this.knownTrades.size(); i < m; i++) {
         VillagerTrade trade = this.knownTrades.get(i);
         int index = this.findTrade(offers, trade);
         if (index == -1) {
            changed = true;
         } else {
            changed |= trade.updateIndex(index);
            changed |= trade.syncItems((MerchantOffer)offers.get(index));
            trades.add(trade);
         }
      }

      return changed ? Optional.of(trades) : Optional.empty();
   }

   public int findTrade(MerchantOffers offer, VillagerTrade trade) {
      int index = trade.getIndex();
      if (index >= 0 && index < offer.size() && trade.matches((MerchantOffer)offer.get(index))) {
         return index;
      } else {
         int i = 0;

         for (int m = offer.size(); i < m; i++) {
            if (trade.matches((MerchantOffer)offer.get(i))) {
               return i;
            }
         }

         return -1;
      }
   }
}
