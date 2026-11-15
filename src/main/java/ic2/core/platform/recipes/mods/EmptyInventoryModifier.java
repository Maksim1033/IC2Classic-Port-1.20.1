package ic2.core.platform.recipes.mods;

import com.google.gson.JsonObject;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class EmptyInventoryModifier implements IRecipeModifier {
   Item item;
   String tag;

   public EmptyInventoryModifier(Item item) {
      this(item, "items");
   }

   public EmptyInventoryModifier(Item item, String tag) {
      this.item = item;
      this.tag = tag;
   }

   public EmptyInventoryModifier(JsonObject obj) {
      this(ForgeRegistryWrapper.getItemValue(ResourceLocation.tryParse(obj.get("item").getAsString())), obj.get("tag").getAsString());
   }

   public EmptyInventoryModifier(FriendlyByteBuf buffer) {
      this.item = (Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
      this.tag = buffer.readUtf(32767);
   }

   @Override
   public void reset() {
   }

   @Override
   public boolean isSlotValid(ItemStack input) {
      return input.getItem() == this.item ? NBTUtils.getItems(input, this.tag).isEmpty() : true;
   }

   @Override
   public boolean isOutputItem(ItemStack input) {
      return false;
   }

   @Override
   public ItemStack applyChanges(ItemStack input, boolean forDisplay) {
      return input;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, this.item);
      buffer.writeUtf(this.tag);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("item", ForgeRegistryWrapper.getItemKey(this.item).toString());
      obj.addProperty("tag", this.tag);
      return obj;
   }
}
