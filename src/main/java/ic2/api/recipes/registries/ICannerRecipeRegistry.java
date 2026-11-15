package ic2.api.recipes.registries;

import ic2.api.items.IRepairable;
import ic2.api.recipes.ingridients.inputs.IInput;
import java.util.List;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ICannerRecipeRegistry extends IListenableRegistry<ICannerRecipeRegistry> {
   void registerRepairable(IRepairable var1, IInput var2, int var3);

   void removeRepairable(IRepairable var1);

   Tuple<IInput, Integer> getRepairableItem(ItemStack var1, ItemStack var2, boolean var3);

   Map<Item, List<Tuple<IInput, Integer>>> getRepairItems();

   void registerFuelValue(ItemStack var1, int var2);

   void registerFuelMultiplier(ItemStack var1, float var2);

   void removeValue(ItemStack var1);

   ICannerRecipeRegistry.FuelValue getValueForStack(ItemStack var1);

   List<ICannerRecipeRegistry.FuelValue> getFuels();

   void registerFillable(ItemStack var1, IInput var2, ItemStack var3);

   boolean hasContainer(ItemStack var1);

   void removeFillable(ItemStack var1);

   void removeFillable(ItemStack var1, ItemStack var2);

   Tuple<IInput, ItemStack> getFillOutput(ItemStack var1, ItemStack var2, boolean var3);

   Map<ItemStack, List<Tuple<IInput, ItemStack>>> getFillables();

   public static class FuelValue {
      ItemStack owner;
      int fuelValue;
      float fuelMultiplier;
      boolean multiplier;

      public FuelValue(FriendlyByteBuf buffer) {
         this.owner = buffer.readItem();
         this.multiplier = buffer.readBoolean();
         this.fuelValue = buffer.readInt();
         this.fuelMultiplier = buffer.readFloat();
      }

      public FuelValue(ItemStack owner, int fuelValue) {
         this(owner, fuelValue, 0.0F, false);
      }

      public FuelValue(ItemStack owner, float fuelMultiplier) {
         this(owner, 0, fuelMultiplier, true);
      }

      public FuelValue(ItemStack owner, int fuelValue, float fuelMultiplier, boolean multiplier) {
         this.owner = owner;
         this.fuelValue = fuelValue;
         this.fuelMultiplier = fuelMultiplier;
         this.multiplier = multiplier;
      }

      public int test(int base) {
         return this.multiplier ? (int)((float)base * this.fuelMultiplier) : this.fuelValue;
      }

      public int apply(int base) {
         return this.multiplier ? base + (int)((float)base * this.fuelMultiplier) : base + this.fuelValue;
      }

      public boolean isMultiplier() {
         return this.multiplier;
      }

      public float getFuelMultiplier() {
         return this.fuelMultiplier;
      }

      public int getFuelValue() {
         return this.fuelValue;
      }

      public ItemStack getStack() {
         return this.owner.copy();
      }

      public void write(FriendlyByteBuf buffer) {
         buffer.writeItem(this.owner);
         buffer.writeBoolean(this.multiplier);
         buffer.writeInt(this.fuelValue);
         buffer.writeFloat(this.fuelMultiplier);
      }
   }
}
