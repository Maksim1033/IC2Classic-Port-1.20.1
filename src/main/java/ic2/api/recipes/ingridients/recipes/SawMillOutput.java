package ic2.api.recipes.ingridients.recipes;

import com.google.gson.JsonObject;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class SawMillOutput extends SimpleRecipeOutput {
   public static final String RECIPE_FLAG = "saw_upgrade";

   public SawMillOutput(JsonObject obj) {
      super(obj);
   }

   public SawMillOutput(FriendlyByteBuf buffer) {
      super(buffer);
   }

   public SawMillOutput(List<IOutputGenerator> outputs) {
      super(outputs);
   }

   public SawMillOutput(List<IOutputGenerator> outputs, float xp) {
      super(outputs, xp);
   }

   public SawMillOutput(List<IOutputGenerator> outputs, CompoundTag nbt, float xp) {
      super(outputs, nbt, xp);
   }

   @Override
   public List<ItemStack> onRecipeProcessed(RandomSource rand, CompoundTag persistentData, CompoundTag recipeFlags) {
      List<ItemStack> list = IRecipeOutput.copyItems(this.outputs);
      int effect = recipeFlags.getInt("saw_upgrade");
      if (effect != 0) {
         int i = 0;

         for (int m = list.size(); i < m; i++) {
            list.get(i).grow(effect);
         }
      }

      return list;
   }
}
