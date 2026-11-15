package ic2.core.platform.recipes.mods;

import com.google.gson.JsonObject;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.world.item.ItemStack;

public class InfoModifier implements IRecipeModifier {
   Component tooltip;

   public InfoModifier(JsonObject obj) {
      this.tooltip = Serializer.fromJson(obj.get("tooltip").getAsString());
   }

   public InfoModifier(String name) {
      this(Component.translatable(name).withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GRAY)));
   }

   public InfoModifier(Component tooltip) {
      this.tooltip = tooltip;
   }

   public InfoModifier(FriendlyByteBuf buffer) {
      this.tooltip = buffer.readComponent();
   }

   @Override
   public void reset() {
   }

   @Override
   public boolean isSlotValid(ItemStack input) {
      return true;
   }

   @Override
   public boolean isOutputItem(ItemStack input) {
      return false;
   }

   @Override
   public ItemStack applyChanges(ItemStack input, boolean forDisplay) {
      if (forDisplay) {
         input = input.copy();
         StackUtil.addTooltip(input, this.tooltip);
      }

      return input;
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeComponent(this.tooltip);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("tooltip", Serializer.toJson(this.tooltip));
      return obj;
   }
}
