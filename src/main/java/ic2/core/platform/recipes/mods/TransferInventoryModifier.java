package ic2.core.platform.recipes.mods;

import com.google.gson.JsonObject;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class TransferInventoryModifier implements IRecipeModifier {
   Item input;
   ItemStack originalInput = ItemStack.EMPTY;
   String inventoryTag;

   public TransferInventoryModifier(Item input) {
      this(input, "items");
   }

   public TransferInventoryModifier(Item input, String inventoryTag) {
      this.input = input;
      this.inventoryTag = inventoryTag;
   }

   public TransferInventoryModifier(JsonObject obj) {
      this(ForgeRegistryWrapper.getItemValue(ResourceLocation.tryParse(obj.get("item").getAsString())), obj.get("tag").getAsString());
   }

   public TransferInventoryModifier(FriendlyByteBuf buffer) {
      this.input = (Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
      this.inventoryTag = buffer.readUtf(32767);
   }

   @Override
   public void reset() {
      this.originalInput = ItemStack.EMPTY;
   }

   @Override
   public boolean isSlotValid(ItemStack input) {
      return true;
   }

   @Override
   public boolean isOutputItem(ItemStack input) {
      if (input.getItem() == this.input) {
         this.originalInput = input;
      }

      return false;
   }

   @Override
   public ItemStack applyChanges(ItemStack input, boolean forDisplay) {
      if (!this.originalInput.isEmpty()) {
         CompoundTag nbt = StackUtil.getNbtData(this.originalInput);
         if (nbt.contains(this.inventoryTag)) {
            input = input.copy();
            input.getOrCreateTag().put(this.inventoryTag, nbt.get(this.inventoryTag));
         }
      }

      return input;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, this.input);
      buffer.writeUtf(this.inventoryTag);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("item", ForgeRegistryWrapper.getItemKey(this.input).toString());
      obj.addProperty("tag", this.inventoryTag);
      return obj;
   }
}
