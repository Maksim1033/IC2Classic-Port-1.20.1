package ic2.core.block.base.tiles.impls;

import ic2.api.items.ICoinItem;
import ic2.api.items.ItemRegistries;
import ic2.api.network.buffer.NetworkInfo;
import ic2.core.IC2;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.personal.IPersonalTile;
import ic2.core.block.base.features.personal.IPersonalWrenchable;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.block.personal.components.BaseTradeOMatComponent;
import ic2.core.block.personal.provider.ITradeProvider;
import ic2.core.block.personal.trade.Trade;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.inventory.transporter.transporters.special.DirectionalTransporter;
import ic2.core.platform.player.friends.Action;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BaseTradeOMatTileEntity<T extends Trade, V extends ITradeProvider>
   extends BaseInventoryTileEntity
   implements IPersonalTile,
   IPersonalWrenchable,
   ITileGui,
   ITickListener {
   @NetworkInfo
   protected UUID owner;
   @NetworkInfo
   protected T trade;
   @NetworkInfo
   public int stock = 0;
   @NetworkInfo
   public int inputFilter = 4;
   @NetworkInfo
   public int outputFilter = 4;
   protected Map<UUID, V> playerTradeInfo = CollectionUtils.<UUID, V>createLinkedMap();
   Set<UUID> watchingPlayers = CollectionUtils.<UUID>createSet();

   public BaseTradeOMatTileEntity(BlockPos pos, BlockState state, int slotCount, T trade) {
      super(pos, state, slotCount);
      this.trade = trade;
      this.addNetworkFields(new String[]{"owner", "trade"});
      this.addGuiFields(new String[]{"trade", "stock", "inputFilter", "outputFilter"});
   }

   protected abstract boolean attemptTrade(V var1);

   protected abstract int recalculateStock();

   protected abstract IHasInventory getInput();

   protected abstract V createForPlayer(UUID var1);

   protected abstract V getSelf();

   public T getTrade() {
      return this.trade;
   }

   public V getOrCreate(Player player) {
      this.watchingPlayers.add(player.getUUID());
      return this.playerTradeInfo.computeIfAbsent(player.getUUID(), this::createForPlayer);
   }

   @Override
   public void onGuiClosed(Player player) {
      this.watchingPlayers.remove(player.getUUID());
   }

   public void onTraded() {
   }

   public void playTradeSound() {
      IC2.AUDIO.playSound(this, IC2Sounds.TRADED_SOUND);
   }

   protected boolean shouldRefresh() {
      if (!this.trade.perPlayer) {
         return this.trade.shouldRefresh(this.getLevel().getGameTime(), null);
      } else {
         boolean result = false;

         for (UUID key : this.playerTradeInfo.keySet()) {
            result |= this.trade.shouldRefresh(this.getLevel().getGameTime(), key);
         }

         return result;
      }
   }

   @Override
   public void onTick() {
      if (this.clock(5) && this.shouldRefresh()) {
         this.stock = this.recalculateStock();
         this.updateGuiFields(new String[]{"stock", "trade"});
      }

      if (this.clock(20)) {
         boolean didTrade = false;
         if (this.trade.perPlayer && this.trade.canDoPerPlayer()) {
            Iterator<V> iter = this.playerTradeInfo.values().iterator();

            while (iter.hasNext()) {
               V entry = iter.next();
               int i = 0;

               while (this.attemptTrade(entry) && i++ < 256) {
                  didTrade = true;
               }

               if (i == 0 && !this.watchingPlayers.contains(entry.getTradeOwner()) && this.trade.canRemove(entry.getTradeOwner())) {
                  iter.remove();
               }
            }
         } else {
            int i = 0;

            while (this.attemptTrade(this.getSelf()) && i++ < 256) {
               didTrade = true;
            }
         }

         if (didTrade) {
            this.playTradeSound();
         }

         if (didTrade || this.clock(400)) {
            this.stock = this.recalculateStock();
            this.updateGuiFields(new String[]{"stock", "trade"});
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiFieldChanged(Set<String> fields, Player player) {
      super.onGuiFieldChanged(fields, player);
      if (fields.contains("trade") || fields.contains("helper") && this.canDoAction(player.getUUID(), Action.TRADE_SETTINGS, false)) {
         ContainerComponent<?> comp = this.getContainer(player, ContainerComponent.class);
         if (comp != null) {
            BaseTradeOMatComponent trade = comp.getComponent(BaseTradeOMatComponent.class);
            if (trade != null) {
               trade.onChanged();
            }
         }
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      if (this.canDoAction(entity.getUUID(), Action.TRADE_SETTINGS, false)) {
         if (key == 0) {
            this.inputFilter ^= value;
            this.updateGuiField("inputFilter");
         } else if (key == 1) {
            this.outputFilter ^= value;
            this.updateGuiField("outputFilter");
         } else if (key == 2) {
            this.trade.updateMoneyTrade(Math.max(0, value));
            this.updateGuiField("trade");
         } else if (key == 3) {
            this.trade.tradeDelay = Math.max(0, value);
            this.updateGuiField("trade");
         } else if (key == 4) {
            this.trade.tradeCap = Math.max(0, value);
            this.updateGuiField("trade");
         } else if (key == 5) {
            this.trade.tradeReset = !this.trade.tradeReset && this.trade.tradeCap > 0 && this.trade.tradeDelay > 0;
            this.updateGuiField("trade");
         } else if (key == 6) {
            if (IC2.PLATFORM.isOp(entity.getUUID())) {
               this.trade.infinite = !this.trade.infinite;
               this.updateGuiField("trade");
            } else {
               entity.displayClientMessage(this.translate("gui.ic2.trade.infinite_op"), false);
            }
         } else if (key == 8) {
            this.trade.perPlayer = !this.trade.perPlayer && this.trade.canDoPerPlayer();
            this.trade.reset();
            this.updateTileField("trade");
            this.updateGuiField("trade");
         }
      }

      if (key == 7) {
         ITradeProvider provider = this.trade.perPlayer ? this.getOrCreate(entity) : this.getSelf();
         this.trade
            .findItems(
               new DirectionalTransporter(TransporterManager.getTransporter(entity), Direction.DOWN),
               TransporterManager.getTransporter(this.trade.canDoPerPlayer() ? provider.getTradeInput() : this.getInput()),
               this.inputFilter
            );
      }

      super.onClientDataReceived(entity, key, value);
   }

   @Override
   public boolean canSetFacing(Direction dir, Player player) {
      return dir != this.getFacing() && dir.getAxis().isHorizontal() && this.canDoAction(player.getUUID(), Action.TRADE_SETTINGS, false);
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return this.canDoAction(player.getUUID(), Action.TRADE_SETTINGS, false);
   }

   @Override
   public double getDropRate(Player player) {
      return 1.0;
   }

   @Override
   public void setOwner(UUID owner) {
      if (this.owner == null && owner != null) {
         this.owner = owner;
         this.updateTileField("owner");
      }
   }

   @Override
   public UUID getOwner() {
      return this.owner;
   }

   @Override
   public boolean canDoAction(UUID user, Action action, boolean ignorePublic) {
      if (this.owner == null) {
         this.setOwner(user);
         return true;
      } else {
         return this.owner.equals(user) || IC2.PLATFORM.isOp(user);
      }
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.owner = compound.hasUUID("owner") ? compound.getUUID("owner") : null;
      this.trade.load(compound.getCompound("trade"));
      this.inputFilter = NBTUtils.getInt(compound, "input", 4);
      this.outputFilter = NBTUtils.getInt(compound, "output", 4);
      this.stock = compound.getInt("stock");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      if (this.owner != null) {
         compound.putUUID("owner", this.owner);
      }

      NBTUtils.put(compound, "trade", this.trade.save(new CompoundTag()));
      NBTUtils.putInt(compound, "input", this.inputFilter, 4);
      NBTUtils.putInt(compound, "output", this.outputFilter, 4);
      NBTUtils.putInt(compound, "stock", this.stock, 0);
   }

   public static void mergeCoins(IHasInventory inv) {
      int moneyFound = 0;
      int i = 0;

      for (int m = inv.getSlotCount(); i < m; i++) {
         ItemStack stack = inv.getStackInSlot(i);
         if (stack.getItem() instanceof ICoinItem) {
            moneyFound += ((ICoinItem)stack.getItem()).getMoneyValue(stack) * stack.getCount();
            inv.setStackInSlot(i, ItemStack.EMPTY);
         }
      }

      NonNullList<ItemStack> list = NonNullList.create();
      ItemRegistries.generateCoins(moneyFound, list, false);
      insertItems(TransporterManager.getTransporter(inv), list, null, false);
   }

   public static boolean insertItems(IItemTransporter trans, NonNullList<ItemStack> list, Direction dir, boolean simulate) {
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         ItemStack stack = (ItemStack)list.get(i);
         if (!stack.isEmpty() && trans.addItem(stack, dir, simulate) < stack.getCount()) {
            return false;
         }
      }

      return true;
   }
}
