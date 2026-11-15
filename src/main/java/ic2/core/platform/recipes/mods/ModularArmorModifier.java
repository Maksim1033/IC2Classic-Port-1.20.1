package ic2.core.platform.recipes.mods;

import com.google.gson.JsonObject;
import ic2.core.item.wearable.base.IC2ModularElectricArmor;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ModularArmorModifier implements IRecipeModifier {
   ItemStack original;

   public ModularArmorModifier() {
   }

   public ModularArmorModifier(JsonObject obj) {
   }

   public ModularArmorModifier(FriendlyByteBuf buffer) {
   }

   @Override
   public void reset() {
      this.original = null;
   }

   @Override
   public boolean isSlotValid(ItemStack input) {
      if (input.getItem() instanceof IC2ModularElectricArmor) {
         if (this.original != null) {
            return false;
         }

         this.original = input.copy();
      }

      return true;
   }

   @Override
   public boolean isOutputItem(ItemStack input) {
      return false;
   }

   @Override
   public ItemStack applyChanges(ItemStack input, boolean forDisplay) {
      if (this.original != null) {
         CompoundTag data = this.original.save(new CompoundTag());
         data.putString("id", ForgeRegistryWrapper.getItemKey(input.getItem()).toString());
         return ItemStack.of(data);
      } else {
         return input;
      }
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
   }

   @Override
   public JsonObject serialize() {
      return new JsonObject();
   }
}
