package ic2.api.recipes.registries;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IElectrolyzerRecipeList extends IListenableRegistry<IElectrolyzerRecipeList> {
   int CHARGE = 1;
   int DISCHARGE = 2;

   default void addChargeRecipe(ResourceLocation id, ItemStack input, ItemStack output, int energy) {
      this.addRecipe(id, input, output, 1, energy);
   }

   default void addDischargeRecipe(ResourceLocation id, ItemStack input, ItemStack output, int energy) {
      this.addRecipe(id, input, output, 2, energy);
   }

   default void addDualRecipe(ResourceLocation id, ItemStack input, ItemStack output, int energy) {
      this.addRecipe(id, input, output, 3, energy);
   }

   void addRecipe(ResourceLocation var1, ItemStack var2, ItemStack var3, int var4, int var5);

   void removeRecipe(ItemStack var1, boolean var2);

   void removeRecipe(ResourceLocation var1);

   List<IElectrolyzerRecipeList.ElectrolyzerRecipe> getRecipes();

   IElectrolyzerRecipeList.ElectrolyzerRecipe getRecipe(ItemStack var1, boolean var2, boolean var3);

   public static class ElectrolyzerRecipe {
      ResourceLocation id;
      ItemStack input;
      ItemStack output;
      int flags;
      int energy;

      public ElectrolyzerRecipe(FriendlyByteBuf buffer) {
         this.id = buffer.readResourceLocation();
         this.input = buffer.readItem();
         this.output = buffer.readItem();
         this.flags = buffer.readByte();
         this.energy = buffer.readInt();
      }

      public ElectrolyzerRecipe(ResourceLocation id, ItemStack input, ItemStack output, int flags, int energy) {
         this.id = id;
         this.input = input;
         this.output = output;
         this.flags = flags;
         this.energy = energy;
      }

      public ResourceLocation getId() {
         return this.id;
      }

      public ItemStack getInput() {
         return this.input;
      }

      public ItemStack getOutput() {
         return this.output;
      }

      public int getEnergy() {
         return this.energy;
      }

      public boolean isChargeRecipe() {
         return (this.flags & 1) != 0;
      }

      public boolean isDischargeRecipe() {
         return (this.flags & 2) != 0;
      }

      public boolean isDual() {
         return (this.flags & 3) != 0;
      }

      public boolean matches(ItemStack input, boolean charge) {
         return charge ? input.getCount() >= this.input.getCount() : input.getCount() >= this.output.getCount();
      }

      public void serialize(FriendlyByteBuf buffer) {
         buffer.writeResourceLocation(this.id);
         buffer.writeItem(this.input);
         buffer.writeItem(this.output);
         buffer.writeByte(this.flags);
         buffer.writeInt(this.energy);
      }
   }
}
