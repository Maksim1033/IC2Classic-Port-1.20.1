package ic2.core.block.personal.trade;

import ic2.api.items.ICoinItem;
import ic2.api.items.ITagItem;
import ic2.api.items.ItemRegistries;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.INBTSavable;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.UUID;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public abstract class Trade implements INetworkDataBuffer, INBTSavable {
   public int moneyNeeded;
   public SimpleInventory neededItems = new SimpleInventory(4);
   public int tradeCap = 0;
   public int tradeDelay = 0;
   public boolean infinite = false;
   public boolean tradeReset = false;
   public long lastTrade = 0L;
   public int tradesDone = 0;
   public int totalTradesDone = 0;
   public boolean perPlayer = false;
   public Object2IntMap<UUID> playerTradesDone = new Object2IntLinkedOpenHashMap();
   public Object2LongMap<UUID> playerLastTradesDone = new Object2LongLinkedOpenHashMap();

   public boolean canDoPerPlayer() {
      return false;
   }

   public boolean canRemove(UUID player) {
      return !this.tradeReset || !this.playerTradesDone.containsKey(player) && !this.playerLastTradesDone.containsKey(player);
   }

   public void reset() {
      this.playerLastTradesDone.clear();
      this.playerTradesDone.clear();
   }

   public int getStock(int original, UUID player) {
      if (this.tradeCap <= 0) {
         return original;
      } else {
         return this.perPlayer
            ? (player == null ? 0 : Math.min(this.tradeCap - this.playerTradesDone.getInt(player), original))
            : Math.min(this.tradeCap - this.tradesDone, original);
      }
   }

   public boolean shouldRefresh(long currentTime, UUID player) {
      if (!this.tradeReset) {
         return false;
      } else {
         if (this.perPlayer && player != null) {
            long last = this.playerLastTradesDone.getLong(player);
            if (last > 0L && currentTime - last >= (long)this.tradeDelay) {
               this.playerTradesDone.removeInt(player);
               this.playerLastTradesDone.removeLong(player);
               return true;
            }
         } else if (!this.perPlayer && this.lastTrade > 0L && currentTime - this.lastTrade >= (long)this.tradeDelay) {
            this.tradesDone = 0;
            this.lastTrade = 0L;
            return true;
         }

         return false;
      }
   }

   public boolean canTrade(long currentTime, UUID player) {
      if (this.tradeReset) {
         this.shouldRefresh(currentTime, player);
         return this.perPlayer ? player != null && this.playerTradesDone.getInt(player) < this.tradeCap : this.tradesDone < this.tradeCap;
      } else {
         return this.perPlayer
            ? player != null
               && (this.tradeDelay <= 0 || this.playerLastTradesDone.getLong(player) + (long)this.tradeDelay <= currentTime)
               && (this.tradeCap <= 0 || this.playerTradesDone.getInt(player) < this.tradeCap)
            : (this.tradeDelay <= 0 || this.lastTrade + (long)this.tradeDelay <= currentTime) && (this.tradeCap <= 0 || this.tradesDone < this.tradeCap);
      }
   }

   public void onTrade(long currentTime, UUID player) {
      if (!this.tradeReset || (this.perPlayer ? this.playerTradesDone.getInt(player) == 0 : this.lastTrade == 0L)) {
         if (this.perPlayer) {
            this.playerLastTradesDone.put(player, currentTime);
         } else {
            this.lastTrade = currentTime;
         }
      }

      if (this.perPlayer) {
         ((Object2IntLinkedOpenHashMap)this.playerTradesDone).addTo(player, 1);
      } else {
         this.tradesDone++;
      }

      this.totalTradesDone++;
   }

   public void updateMoneyTrade(int value) {
      if (this.moneyNeeded != value) {
         this.moneyNeeded = value;
         this.neededItems.clear();
         if (this.moneyNeeded > 0) {
            NonNullList<ItemStack> result = ItemRegistries.generateCoins(value, NonNullList.create(), true);
            int i = 0;

            for (int m = Math.min(4, result.size()); i < m; i++) {
               this.neededItems.setStackInSlot(i, (ItemStack)result.get(i));
            }
         }
      }
   }

   public boolean hasItems(IItemTransporter transporter, int filters, NonNullList<ItemStack> result) {
      int foundItems = 0;
      int i = 0;

      for (int m = this.neededItems.getSlotCount(); i < m; i++) {
         ItemStack stack = this.neededItems.getStackInSlot(i);
         if (!stack.isEmpty()) {
            foundItems++;
            ItemStack found = transporter.removeItem(
               new StackFilter(stack, filters | (stack.getItem() instanceof ITagItem ? 32 : 0)), null, stack.getCount(), false
            );
            if (found.getCount() < stack.getCount()) {
               return false;
            }

            result.add(found);
         }
      }

      return foundItems > 0;
   }

   public void findItems(IItemTransporter source, IItemTransporter output, int inputFilter) {
      int i = 0;

      for (int m = this.neededItems.getSlotCount(); i < m; i++) {
         ItemStack stack = this.neededItems.getStackInSlot(i);
         if (!stack.isEmpty()) {
            IFilter filter = new StackFilter(stack, inputFilter | (stack.getItem() instanceof ITagItem ? 32 : 0));
            source.removeItem(filter, null, output.addItem(source.removeItem(filter, null, stack.getCount(), true), null, false), false);
         }
      }
   }

   public int getFoundMoney(IHasInventory inventory) {
      int moneyFound = 0;
      int i = 0;

      for (int m = inventory.getSlotCount(); i < m; i++) {
         ItemStack stack = inventory.getStackInSlot(i);
         if (stack.getItem() instanceof ICoinItem) {
            moneyFound += ((ICoinItem)stack.getItem()).getMoneyValue(stack) * stack.getCount();
         }
      }

      return moneyFound;
   }

   public int consumeCoins(IHasInventory inventory, int needed) {
      int i = 0;

      for (int m = inventory.getSlotCount(); i < m && needed > 0; i++) {
         ItemStack stack = inventory.getStackInSlot(i);
         if (stack.getItem() instanceof ICoinItem) {
            int amount = ((ICoinItem)stack.getItem()).getMoneyValue(stack);
            int toRemove = Math.min(Mth.ceil((double)needed / (double)amount), stack.getCount());
            needed -= toRemove * amount;
            stack.shrink(toRemove);
         }
      }

      return needed;
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeBoolean(this.infinite);
      buffer.writeBoolean(this.tradeReset);
      buffer.writeBoolean(this.perPlayer);
      buffer.writeVarInt(this.moneyNeeded);
      buffer.writeVarInt(this.tradeCap);
      buffer.writeVarInt(this.tradeDelay);
      buffer.writeVarInt(this.totalTradesDone);
      if (this.perPlayer && this.canDoPerPlayer()) {
         buffer.writeVarInt(this.playerLastTradesDone.size());
         ObjectIterator var2 = Object2LongMaps.fastIterable(this.playerLastTradesDone).iterator();

         while (var2.hasNext()) {
            Entry<UUID> id = (Entry<UUID>)var2.next();
            buffer.writeUUID((UUID)id.getKey());
            buffer.writeLong(id.getLongValue());
            buffer.writeVarInt(this.playerTradesDone.getInt(id.getKey()));
         }
      } else {
         buffer.writeLong(this.lastTrade);
         buffer.writeVarInt(this.tradesDone);
      }
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.infinite = buffer.readBoolean();
      this.tradeReset = buffer.readBoolean();
      this.perPlayer = buffer.readBoolean();
      this.moneyNeeded = buffer.readVarInt();
      this.tradeCap = buffer.readVarInt();
      this.tradeDelay = buffer.readVarInt();
      this.totalTradesDone = buffer.readVarInt();
      if (this.perPlayer && this.canDoPerPlayer()) {
         int i = 0;

         for (int m = buffer.readVarInt(); i < m; i++) {
            UUID id = buffer.readUUID();
            this.playerLastTradesDone.put(id, buffer.readLong());
            this.playerTradesDone.put(id, buffer.readVarInt());
         }
      } else {
         this.lastTrade = buffer.readLong();
         this.tradesDone = buffer.readVarInt();
      }
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      NBTUtils.put(nbt, "wanted", this.neededItems.save(new CompoundTag()));
      NBTUtils.putInt(nbt, "money", this.moneyNeeded, 0);
      NBTUtils.putInt(nbt, "cap", this.tradeCap, 0);
      NBTUtils.putBoolean(nbt, "infinite", this.infinite, false);
      NBTUtils.putBoolean(nbt, "reset", this.tradeReset, false);
      NBTUtils.putBoolean(nbt, "perPlayer", this.perPlayer, false);
      NBTUtils.putInt(nbt, "delay", this.tradeDelay, 0);
      NBTUtils.putInt(nbt, "total", this.totalTradesDone, 0);
      if (this.perPlayer && this.canDoPerPlayer()) {
         ListTag list = new ListTag();
         ObjectIterator var3 = Object2LongMaps.fastIterable(this.playerLastTradesDone).iterator();

         while (var3.hasNext()) {
            Entry<UUID> id = (Entry<UUID>)var3.next();
            CompoundTag data = new CompoundTag();
            data.putUUID("id", (UUID)id.getKey());
            data.putLong("last", id.getLongValue());
            data.putInt("count", this.playerTradesDone.getInt(id.getKey()));
            list.add(data);
         }

         NBTUtils.put(nbt, "players", list);
      } else {
         NBTUtils.putInt(nbt, "done", this.tradesDone, 0);
         NBTUtils.putLong(nbt, "last", this.lastTrade, 0L);
      }

      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      this.neededItems.load(nbt.getCompound("wanted"));
      this.moneyNeeded = nbt.getInt("money");
      this.tradeCap = nbt.getInt("cap");
      this.infinite = nbt.getBoolean("infinite");
      this.tradeReset = nbt.getBoolean("reset");
      this.perPlayer = nbt.getBoolean("perPlayer");
      this.tradeDelay = nbt.getInt("delay");
      this.totalTradesDone = nbt.getInt("total");
      if (this.perPlayer && this.canDoPerPlayer()) {
         for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("players", 10), CompoundTag.class)) {
            UUID id = data.getUUID("id");
            this.playerLastTradesDone.put(id, data.getLong("last"));
            this.playerTradesDone.put(id, data.getInt("count"));
         }
      } else {
         this.tradesDone = nbt.getInt("done");
         this.lastTrade = nbt.getLong("last");
      }
   }
}
