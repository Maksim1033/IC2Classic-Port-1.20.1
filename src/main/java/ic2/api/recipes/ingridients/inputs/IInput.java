package ic2.api.recipes.ingridients.inputs;

import com.google.gson.JsonObject;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

public interface IInput {
   default Ingredient asIngredient() {
      List<ItemStack> stacks = this.getComponents();
      return Ingredient.of(stacks.toArray(ItemStack[]::new));
   }

   List<ItemStack> getComponents();

   int getInputSize();

   boolean matches(ItemStack var1);

   default boolean matchesAll(ItemStack... stacks) {
      for (ItemStack stack : stacks) {
         if (!this.matches(stack)) {
            return false;
         }
      }

      return true;
   }

   default boolean matchesAny(ItemStack... stacks) {
      for (ItemStack stack : stacks) {
         if (this.matches(stack)) {
            return true;
         }
      }

      return false;
   }

   void serialize(FriendlyByteBuf var1);

   JsonObject serialize();

   static JsonObject writeItemStack(ItemStack stack, boolean includeSize) {
      JsonObject obj = new JsonObject();
      obj.addProperty("item", ForgeRegistryWrapper.getItemKey(stack.getItem()).toString());
      if (includeSize) {
         obj.addProperty("count", stack.getCount());
      }

      if (stack.hasTag()) {
         obj.addProperty("nbt", stack.getTag().toString());
      }

      return obj;
   }

   static JsonObject writeFluidStack(FluidStack stack) {
      JsonObject obj = new JsonObject();
      obj.addProperty("fluid", ForgeRegistryWrapper.getFluidKey(stack.getFluid()).toString());
      obj.addProperty("amount", stack.getAmount());
      if (stack.hasTag()) {
         obj.addProperty("nbt", stack.getTag().toString());
      }

      return obj;
   }

   static FluidStack readFluidStack(JsonObject obj) {
      return new FluidStack(
         ForgeRegistryWrapper.getFluidValue(ResourceLocation.tryParse(obj.get("fluid").getAsString())),
         obj.get("amount").getAsInt(),
         obj.has("nbt") ? readNBT(obj.get("nb").getAsString()) : null
      );
   }

   static CompoundTag readNBT(String s) {
      try {
         return TagParser.parseTag(s);
      } catch (Exception var2) {
         return null;
      }
   }

   static ItemStack copyWithSize(ItemStack stack, int newSize) {
      ItemStack copy = stack.copy();
      copy.setCount(newSize);
      return copy;
   }

   static boolean isStackEqual(ItemStack key, ItemStack other) {
      return ItemStack.isSameItem(key, other) && isNBTExact(key, other);
   }

   static boolean isNBTExact(ItemStack subject, ItemStack target) {
      boolean key = subject.hasTag();
      boolean value = target.hasTag();
      if (!key && !value) {
         return true;
      } else if (key && value) {
         CompoundTag keyTag = subject.getTag();
         CompoundTag valueTag = target.getTag();
         if (keyTag.size() != valueTag.size()) {
            return false;
         } else {
            for (String entry : keyTag.getAllKeys()) {
               Tag subjectNBT = keyTag.get(entry);
               Tag targetNBT = valueTag.get(entry);
               if (subjectNBT == null || targetNBT == null || subjectNBT.getId() != targetNBT.getId() || !subjectNBT.equals(targetNBT)) {
                  return false;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }
}
