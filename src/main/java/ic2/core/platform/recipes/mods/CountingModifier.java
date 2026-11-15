package ic2.core.platform.recipes.mods;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.ItemInput;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CountingModifier implements IRecipeModifier {
   IInput desiredItem;
   int amountWanted;
   int found;

   public CountingModifier(Item input, int amountWanted) {
      this(new ItemInput(input), amountWanted);
   }

   public CountingModifier(IInput desiredItem, int amountWanted) {
      this.desiredItem = desiredItem;
      this.amountWanted = amountWanted;
   }

   public CountingModifier(FriendlyByteBuf buffer) {
      this.desiredItem = IngredientRegistry.INSTANCE.readInput(buffer);
      this.amountWanted = buffer.readVarInt();
   }

   public CountingModifier(JsonObject obj) {
      this.desiredItem = IngredientRegistry.INSTANCE.readInput(obj.getAsJsonObject("input"));
      this.amountWanted = obj.get("amount").getAsInt();
   }

   @Override
   public void reset() {
      this.found = 0;
   }

   @Override
   public boolean isSlotValid(ItemStack input) {
      if (this.desiredItem.matches(input)) {
         this.found = this.found + input.getCount();
      }

      return true;
   }

   @Override
   public boolean isOutputItem(ItemStack input) {
      return false;
   }

   @Override
   public ItemStack applyChanges(ItemStack input, boolean forDisplay) {
      if (!forDisplay) {
         int amount = this.found / this.amountWanted;
         if (amount <= 0) {
            return ItemStack.EMPTY;
         }

         input.setDamageValue(input.getMaxDamage() - amount);
      }

      return input;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      IngredientRegistry.INSTANCE.writeInput(this.desiredItem, buffer);
      buffer.writeVarInt(this.amountWanted);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.add("input", IngredientRegistry.INSTANCE.serializeInput(this.desiredItem));
      obj.addProperty("amount", this.amountWanted);
      return obj;
   }
}
