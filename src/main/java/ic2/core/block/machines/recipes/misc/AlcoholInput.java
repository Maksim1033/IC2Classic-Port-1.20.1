package ic2.core.block.machines.recipes.misc;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.core.item.food_and_drink.drinks.Beer;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

public class AlcoholInput implements IInput {
   Item filter;
   int size;
   Ingredient ingredient;

   public AlcoholInput(Item item) {
      this(item, 1);
   }

   public AlcoholInput(Item filter, int size) {
      this.filter = filter;
      this.size = size;
      this.ingredient = Ingredient.of(new ItemLike[]{filter});
   }

   public AlcoholInput(JsonObject obj) {
      this(ForgeRegistryWrapper.getItemValue(ResourceLocation.tryParse(obj.get("item").getAsString())), obj.get("size").getAsInt());
   }

   public AlcoholInput(FriendlyByteBuf buf) {
      this((Item)buf.readRegistryIdUnsafe(ForgeRegistries.ITEMS), buf.readVarInt());
   }

   @Override
   public List<ItemStack> getComponents() {
      List<ItemStack> list = CollectionUtils.<ItemStack>createList();

      for (int time = 2; time < 5; time++) {
         for (int hops = 2; hops <= 6; hops++) {
            for (int solid = 0; solid <= 6; solid++) {
               list.add(Beer.createData(new ItemStack(this.filter), time, solid, hops));
            }
         }
      }

      return list;
   }

   @Override
   public int getInputSize() {
      return this.size;
   }

   @Override
   public Ingredient asIngredient() {
      return this.ingredient;
   }

   @Override
   public boolean matches(ItemStack stack) {
      int data = StackUtil.getNbtData(stack).getInt("data");
      int time = Beer.getTime(data);
      return stack.is(this.filter) && Beer.getHops(data) >= 2 && time >= 2 && time < 5;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, this.filter);
      buffer.writeVarInt(this.size);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("item", ForgeRegistryWrapper.getItemKey(this.filter).toString());
      obj.addProperty("size", this.size);
      return obj;
   }
}
