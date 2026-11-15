package ic2.core.platform.recipes.mods;

import com.google.gson.JsonObject;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantmentModifier extends BaseRecipeModifier {
   Enchantment enchantment;
   int level;

   public EnchantmentModifier(JsonObject obj) {
      this(
         ForgeRegistryWrapper.getItemValue(ResourceLocation.tryParse(obj.get("item").getAsString())),
         obj.get("tag").getAsString(),
         (Enchantment)ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryParse(obj.get("enchantment").getAsString())),
         obj.get("level").getAsInt()
      );
      if (obj.has("tooltip")) {
         this.setTooltip(Serializer.fromJson(obj.get("tooltip").getAsString()));
      }

      this.usesInput = obj.has("usesInput") && obj.get("usesInput").getAsBoolean();
   }

   public EnchantmentModifier(Item type, String tag, Enchantment enchantment, int level) {
      super(type, tag);
      this.enchantment = enchantment;
      this.level = level;
   }

   public EnchantmentModifier(FriendlyByteBuf buffer) {
      super(buffer);
      this.enchantment = (Enchantment)buffer.readRegistryIdUnsafe(ForgeRegistries.ENCHANTMENTS);
      this.level = buffer.readVarInt();
   }

   @Override
   protected boolean isTagUsable(ItemStack input) {
      Map<Enchantment, Integer> data = EnchantmentHelper.getEnchantments(input);
      int level = data.getOrDefault(this.enchantment, 0);
      if (level <= 0) {
         for (Enchantment enchantment : data.keySet()) {
            if (!enchantment.isCompatibleWith(this.enchantment)) {
               return false;
            }
         }
      }

      return level < this.level;
   }

   @Override
   public ItemStack applyChanges(ItemStack input, boolean forDisplay) {
      Map<Enchantment, Integer> data = EnchantmentHelper.getEnchantments(input);
      data.put(this.enchantment, this.level);
      EnchantmentHelper.setEnchantments(data, input);
      return super.applyChanges(input, forDisplay);
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      super.serialize(buffer);
      buffer.writeRegistryIdUnsafe(ForgeRegistries.ENCHANTMENTS, this.enchantment);
      buffer.writeVarInt(this.level);
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("enchantment", ForgeRegistryWrapper.getEnchantmentKey(this.enchantment).toString());
      obj.addProperty("level", this.level);
      obj.addProperty("item", ForgeRegistryWrapper.getItemKey(this.type).toString());
      obj.addProperty("usesInput", this.usesInput);
      if (this.tooltip != null) {
         obj.addProperty("tooltip", Serializer.toJson(this.tooltip));
      }

      return obj;
   }
}
