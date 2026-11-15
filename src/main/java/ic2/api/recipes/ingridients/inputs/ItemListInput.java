package ic2.api.recipes.ingridients.inputs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CraftingHelper;

public class ItemListInput implements IInput {
   List<ItemStack> list = new ObjectArrayList();
   int size;
   boolean or;

   private ItemListInput() {
   }

   public ItemListInput(JsonObject obj) {
      this.size = obj.get("size").getAsInt();
      this.or = obj.get("or").getAsBoolean();

      for (JsonElement el : obj.getAsJsonArray("items")) {
         ItemStack stack = CraftingHelper.getItemStack(el.getAsJsonObject(), true);
         if (!stack.isEmpty()) {
            this.list.add(stack);
         }
      }
   }

   public ItemListInput(FriendlyByteBuf buffer) {
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         ItemStack stack = buffer.readItem();
         if (!stack.isEmpty()) {
            this.list.add(stack);
         }
      }

      this.size = buffer.readVarInt();
      this.or = buffer.readBoolean();
   }

   public ItemListInput(List<ItemStack> list, int size, boolean or) {
      this.list.addAll(list);
      this.size = size;
      this.or = or;
   }

   public static IInput createItemList(ItemLike... prov) {
      return createItemList(1, true, prov);
   }

   public static IInput createItemList(int size, ItemLike... prov) {
      return createItemList(size, true, prov);
   }

   public static IInput createItemList(int size, boolean or, ItemLike... prov) {
      ItemListInput inv = new ItemListInput();
      inv.size = size;
      inv.or = or;

      for (ItemLike itemLike : prov) {
         ItemStack stack = new ItemStack(itemLike);
         if (!stack.isEmpty()) {
            inv.list.add(stack);
         }
      }

      return inv;
   }

   public static IInput createItemList(ItemStack... prov) {
      return createItemList(1, true, prov);
   }

   public static IInput createItemList(int size, ItemStack... prov) {
      return createItemList(size, true, prov);
   }

   public static IInput createItemList(int size, boolean or, ItemStack... stacks) {
      ItemListInput inv = new ItemListInput();
      inv.size = size;
      inv.or = or;

      for (ItemStack stack : stacks) {
         if (!stack.isEmpty()) {
            inv.list.add(stack.copy());
         }
      }

      return inv;
   }

   @Override
   public List<ItemStack> getComponents() {
      List<ItemStack> result = new ObjectArrayList();
      int i = 0;

      for (int m = this.list.size(); i < m; i++) {
         result.add(IInput.copyWithSize(this.list.get(i), this.size));
      }

      return result;
   }

   @Override
   public int getInputSize() {
      return this.size;
   }

   @Override
   public boolean matches(ItemStack stack) {
      if (this.or) {
         int i = 0;

         for (int m = this.list.size(); i < m; i++) {
            if (IInput.isStackEqual(this.list.get(i), stack)) {
               return true;
            }
         }

         return false;
      } else {
         int i = 0;

         for (int mx = this.list.size(); i < mx; i++) {
            if (!IInput.isStackEqual(this.list.get(i), stack)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeVarInt(this.list.size());
      int i = 0;

      for (int m = this.list.size(); i < m; i++) {
         buffer.writeItem(this.list.get(i));
      }

      buffer.writeVarInt(this.size);
      buffer.writeBoolean(this.or);
   }

   @Override
   public JsonObject serialize() {
      JsonArray array = new JsonArray();

      for (ItemStack stack : this.list) {
         array.add(IInput.writeItemStack(stack, false));
      }

      JsonObject obj = new JsonObject();
      obj.add("items", array);
      obj.addProperty("or", this.or);
      obj.addProperty("size", this.size);
      return obj;
   }
}
