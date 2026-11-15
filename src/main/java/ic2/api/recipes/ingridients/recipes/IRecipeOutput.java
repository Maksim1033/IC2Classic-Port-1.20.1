package ic2.api.recipes.ingridients.recipes;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public interface IRecipeOutput {
   CompoundTag EMPTY_COMPOUND = new CompoundTag();

   List<ItemStack> onRecipeProcessed(RandomSource var1, CompoundTag var2, CompoundTag var3);

   default List<ItemStack> onRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags, IRecipeOutput.IRecipeOverride overrides) {
      return this.onRecipeProcessed(rand, persistentData, recipeFlags);
   }

   List<ItemStack> getAllOutputs();

   @Nonnull
   CompoundTag getMetadata();

   float getExperience();

   void serialize(FriendlyByteBuf var1);

   JsonObject serialize();

   static List<ItemStack> copyItems(List<ItemStack> copy) {
      List<ItemStack> list = new ObjectArrayList();

      for (ItemStack stack : copy) {
         list.add(stack.copy());
      }

      return list;
   }

   public interface IRecipeOverride {
      float getChance(float var1);
   }
}
