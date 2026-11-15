package ic2.core.platform.recipes.mods;

import com.google.gson.JsonObject;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FlagModifier extends BaseRecipeModifier {
   boolean flag;

   public FlagModifier(JsonObject obj) {
      this(
         ForgeRegistryWrapper.getItemValue(ResourceLocation.tryParse(obj.get("item").getAsString())),
         obj.get("tag").getAsString(),
         obj.get("flag").getAsBoolean()
      );
      if (obj.has("tooltip")) {
         this.setTooltip(Serializer.fromJson(obj.get("tooltip").getAsString()));
      }

      this.usesInput = obj.has("usesInput") && obj.get("usesInput").getAsBoolean();
   }

   public FlagModifier(Item type, String tag, boolean flag) {
      super(type, tag);
      this.flag = flag;
   }

   public FlagModifier(FriendlyByteBuf buffer) {
      super(buffer);
      this.flag = buffer.readBoolean();
   }

   @Override
   protected boolean isTagUsable(ItemStack input) {
      return StackUtil.getNbtData(input).getBoolean(this.tag) != this.flag;
   }

   @Override
   public ItemStack applyChanges(ItemStack input, boolean forDisplay) {
      input.getOrCreateTag().putBoolean(this.tag, this.flag);
      return super.applyChanges(input, forDisplay);
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      super.serialize(buffer);
      buffer.writeBoolean(this.flag);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("flag", this.flag);
      obj.addProperty("item", ForgeRegistryWrapper.getItemKey(this.type).toString());
      obj.addProperty("usesInput", this.usesInput);
      obj.addProperty("tag", this.tag);
      if (this.tooltip != null) {
         obj.addProperty("tooltip", Serializer.toJson(this.tooltip));
      }

      return obj;
   }
}
