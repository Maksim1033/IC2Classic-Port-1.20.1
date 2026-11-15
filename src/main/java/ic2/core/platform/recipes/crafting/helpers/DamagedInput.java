package ic2.core.platform.recipes.crafting.helpers;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DamagedInput implements IInput {
   Item item;
   boolean undamaged = false;

   public DamagedInput(JsonObject obj) {
      this(ForgeRegistryWrapper.getItemValue(ResourceLocation.tryParse(obj.get("item").getAsString())));
      this.undamaged = obj.get("undamaged").getAsBoolean();
   }

   public DamagedInput(FriendlyByteBuf buffer) {
      this.item = Item.byId(buffer.readVarInt());
      this.undamaged = buffer.readBoolean();
   }

   public DamagedInput(Item item) {
      this.item = item;
   }

   public DamagedInput setUndamaged() {
      this.undamaged = true;
      return this;
   }

   @Override
   public List<ItemStack> getComponents() {
      ItemStack stack = new ItemStack(this.item);
      if (!this.undamaged) {
         stack.setDamageValue(1);
      }

      return ObjectLists.singleton(stack);
   }

   @Override
   public int getInputSize() {
      return 1;
   }

   @Override
   public boolean matches(ItemStack stack) {
      return stack.getItem() == this.item && stack.isDamaged() != this.undamaged;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeVarInt(Item.getId(this.item));
      buffer.writeBoolean(this.undamaged);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("item", ForgeRegistryWrapper.getItemKey(this.item).toString());
      obj.addProperty("undamaged", this.undamaged);
      return obj;
   }
}
