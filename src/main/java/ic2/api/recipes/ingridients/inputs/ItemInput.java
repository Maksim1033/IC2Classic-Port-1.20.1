package ic2.api.recipes.ingridients.inputs;

import com.google.gson.JsonObject;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class ItemInput implements IInput {
   Item item;
   int size;
   Ingredient ingredient;

   public ItemInput(JsonObject obj) {
      this(ForgeRegistryWrapper.getItemValue(ResourceLocation.tryParse(obj.get("item").getAsString())), obj.get("size").getAsInt());
   }

   public ItemInput(FriendlyByteBuf buffer) {
      this(Item.byId(buffer.readVarInt()), buffer.readByte());
   }

   public ItemInput(ItemLike prov, int size) {
      this(prov.asItem(), size);
   }

   public ItemInput(Item item, int size) {
      this.item = item;
      this.size = size;
      this.ingredient = Ingredient.of(new ItemStack[]{new ItemStack(item, size)});
   }

   public ItemInput(ItemLike prov) {
      this(prov.asItem());
   }

   public ItemInput(Item item) {
      this(item, 1);
   }

   @Override
   public Ingredient asIngredient() {
      return this.ingredient;
   }

   @Override
   public List<ItemStack> getComponents() {
      return ObjectLists.singleton(new ItemStack(this.item, this.size));
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
