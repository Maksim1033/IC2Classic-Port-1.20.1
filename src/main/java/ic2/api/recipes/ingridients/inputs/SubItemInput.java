package ic2.api.recipes.ingridients.inputs;

import com.google.gson.JsonObject;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class SubItemInput implements IInput {
   Item item;
   int size;
   Ingredient ingredient;

   public SubItemInput(JsonObject obj) {
      this(ForgeRegistryWrapper.getItemValue(new ResourceLocation(obj.get("item").getAsString())), obj.get("size").getAsInt());
   }

   public SubItemInput(FriendlyByteBuf buffer) {
      this(Item.byId(buffer.readVarInt()), buffer.readByte());
   }

   public SubItemInput(ItemLike prov, int size) {
      this(prov.asItem(), size);
   }

   public SubItemInput(Item item, int size) {
      this.item = item;
      this.size = size;
      NonNullList<ItemStack> items = NonNullList.create();
      items.add(new ItemStack(item));
      this.ingredient = Ingredient.of((ItemStack[])items.toArray(ItemStack[]::new));
   }

   public SubItemInput(ItemLike prov) {
      this(prov.asItem());
   }

   public SubItemInput(Item item) {
      this(item, 1);
   }

   @Override
   public Ingredient asIngredient() {
      return this.ingredient;
   }

   @Override
   public List<ItemStack> getComponents() {
      NonNullList<ItemStack> items = NonNullList.create();
      items.add(new ItemStack(this.item));
      return items;
   }

   @Override
   public int getInputSize() {
      return this.size;
   }

   @Override
   public boolean matches(ItemStack stack) {
      return stack.getItem() == this.item;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeVarInt(Item.getId(this.item));
      buffer.writeByte(this.size);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("item", ForgeRegistryWrapper.getItemKey(this.item).toString());
      obj.addProperty("size", this.size);
      return obj;
   }
}
