package ic2.core.platform.recipes.mods;

import com.google.gson.JsonObject;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NumberModifier extends BaseRecipeModifier {
   int amount;

   public NumberModifier(JsonObject obj) {
      this(
         ForgeRegistryWrapper.getItemValue(ResourceLocation.tryParse(obj.get("item").getAsString())),
         obj.get("tag").getAsString(),
         obj.get("number").getAsInt()
      );
      if (obj.has("tooltip")) {
         this.setTooltip(Serializer.fromJson(obj.get("tooltip").getAsString()));
      }

      this.usesInput = obj.has("usesInput") && obj.get("usesInput").getAsBoolean();
   }

   public NumberModifier(Item type, String tag, int amount) {
      super(type, tag);
      this.amount = amount;
   }

   public NumberModifier(FriendlyByteBuf buffer) {
      super(buffer);
      this.amount = buffer.readInt();
   }

   @Override
   protected boolean isTagUsable(ItemStack input) {
      return StackUtil.getNbtData(input).getInt(this.tag) != this.amount;
   }

   @Override
   public ItemStack applyChanges(ItemStack input, boolean forDisplay) {
      input.getOrCreateTag().putInt(this.tag, this.amount);
      return super.applyChanges(input, forDisplay);
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      super.serialize(buffer);
      buffer.writeInt(this.amount);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("number", this.amount);
      obj.addProperty("item", ForgeRegistryWrapper.getItemKey(this.type).toString());
      obj.addProperty("usesInput", this.usesInput);
      obj.addProperty("tag", this.tag);
      if (this.tooltip != null) {
         obj.addProperty("tooltip", Serializer.toJson(this.tooltip));
      }

      return obj;
   }
}
