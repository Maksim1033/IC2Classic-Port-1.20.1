package ic2.core.block.machines.logic.villager;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public class VillagerTrade {
   UUID owner;
   int index;
   boolean outOfStock = false;
   boolean state = false;
   ItemStack input;
   ItemStack subInput;
   ItemStack output;
   int realCost;

   public VillagerTrade(UUID owner, int index, MerchantOffer offer) {
      this.owner = owner;
      this.index = index;
      this.syncItems(offer);
   }

   public VillagerTrade(UUID owner, CompoundTag tag) {
      this.owner = owner;
      this.index = tag.getInt("index");
      this.state = tag.getBoolean("state");
      this.outOfStock = tag.getBoolean("stock");
      this.input = NBTUtils.loadItem(tag, "main", ItemStack.EMPTY);
      this.subInput = NBTUtils.loadItem(tag, "sub", ItemStack.EMPTY);
      this.output = NBTUtils.loadItem(tag, "out", ItemStack.EMPTY);
      this.realCost = NBTUtils.getInt(tag, "realCost", this.input.getCount());
   }

   public VillagerTrade(UUID owner, IInputBuffer buffer) {
      this.owner = owner;
      this.index = buffer.readVarInt();
      this.state = buffer.readBoolean();
      this.outOfStock = buffer.readBoolean();
      this.input = buffer.readItemStack();
      this.subInput = buffer.readItemStack();
      this.output = buffer.readItemStack();
      this.realCost = buffer.readVarInt();
   }

   public boolean matches(VillagerTrade other) {
      return other.owner.equals(this.owner) && this.index == other.index;
   }

   public int getIndex() {
      return this.index;
   }

   public UUID getOwner() {
      return this.owner;
   }

   public void setState(boolean newState) {
      this.state = newState;
   }

   public boolean getState() {
      return this.state;
   }

   public ItemStack getMainItem() {
      return this.input;
   }

   public ItemStack getSubItem() {
      return this.subInput;
   }

   public ItemStack getOutputItem() {
      return this.output;
   }

   public int getRealPrice() {
      return this.realCost;
   }

   public boolean isDifferentPrice() {
      return this.input.getCount() != this.realCost;
   }

   public boolean isOutOfStock() {
      return this.outOfStock;
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putInt("index", this.index);
      NBTUtils.putBoolean(tag, "state", this.state, false);
      NBTUtils.putBoolean(tag, "stock", this.outOfStock, false);
      NBTUtils.saveItem(tag, "main", this.input);
      NBTUtils.saveItem(tag, "sub", this.subInput);
      NBTUtils.saveItem(tag, "out", this.output);
      NBTUtils.putInt(tag, "realCost", this.realCost, this.input.getCount());
      return tag;
   }

   public void write(IOutputBuffer buffer) {
      buffer.writeVarInt(this.index);
      buffer.writeBoolean(this.state);
      buffer.writeBoolean(this.outOfStock);
      buffer.writeItemStack(this.input);
      buffer.writeItemStack(this.subInput);
      buffer.writeItemStack(this.output);
      buffer.writeVarInt(this.realCost);
   }

   public boolean updateIndex(int newIndex) {
      if (this.index == newIndex) {
         return false;
      } else {
         this.index = newIndex;
         return true;
      }
   }

   public boolean matches(MerchantOffer offer) {
      return StackUtil.isStackEqual(this.input, offer.getBaseCostA())
         && (this.subInput.isEmpty() || StackUtil.isStackEqual(this.subInput, offer.getCostB()))
         && StackUtil.isStackEqual(this.output, offer.getResult());
   }

   public boolean syncItems(MerchantOffer offer) {
      boolean result = false;
      this.input = offer.getBaseCostA();
      this.subInput = offer.getCostB();
      this.output = offer.getResult();
      if (this.realCost != offer.getCostA().getCount()) {
         this.realCost = offer.getCostA().getCount();
         result = true;
      }

      if (this.outOfStock != offer.isOutOfStock()) {
         this.outOfStock = offer.isOutOfStock();
         result = true;
      }

      return result;
   }
}
