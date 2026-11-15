package ic2.core.platform.recipes.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.api.items.electric.ElectricItem;
import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.generators.IOutputGenerator;
import ic2.api.recipes.ingridients.generators.ItemWithNBTGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.core.IC2;
import ic2.core.platform.recipes.crafting.helpers.RecipeMods;
import ic2.core.platform.recipes.misc.IngredientRegistry;
import ic2.core.platform.recipes.mods.IRecipeModifier;
import ic2.core.utils.collection.CollectionUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class ShapelessIC2Recipe extends RecipeIC2Base {
   public List<IInput> inputs = new ArrayList<>();
   public IOutputGenerator generator;

   protected ShapelessIC2Recipe(
      ResourceLocation id, Tuple<ItemStack, IOutputGenerator> output, boolean hidden, boolean consume, IRecipeModifier mod, List<IInput> inputs
   ) {
      super(id, (ItemStack)output.getA(), hidden, consume, mod);
      this.generator = (IOutputGenerator)output.getB();
      this.inputs.addAll(inputs);
   }

   public ShapelessIC2Recipe(FriendlyByteBuf buffer, ResourceLocation id) {
      super(id, buffer.readItem(), buffer.readBoolean(), buffer.readBoolean(), IngredientRegistry.INSTANCE.readMod(buffer));
      int size = buffer.readByte();

      for (int i = 0; i < size; i++) {
         IInput input = IngredientRegistry.INSTANCE.readInput(buffer);
         if (input != null) {
            this.inputs.add(input);
         }
      }
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeItemStack(this.output, false);
      buffer.writeBoolean(this.hidden);
      buffer.writeBoolean(this.consume);
      IngredientRegistry.INSTANCE.writeMod(this.mod, buffer);
      buffer.writeByte(this.inputs.size());

      for (int i = 0; i < this.inputs.size(); i++) {
         IngredientRegistry.INSTANCE.writeInput(this.inputs.get(i), buffer);
      }
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("type", "ic2:crafting_ic2_automated");
      obj.addProperty("sub_type", "ic2:shapeless");
      obj.addProperty("enabled", true);
      obj.add("output", IngredientRegistry.INSTANCE.serializeOutputGenerator(this.generator));
      obj.addProperty("hidden", this.hidden);
      obj.addProperty("consume", this.consume);
      if (this.mod != null) {
         obj.add("modifier", IngredientRegistry.INSTANCE.serializeModifier(this.mod));
      }

      JsonArray list = new JsonArray();

      for (IInput input : this.inputs) {
         list.add(IngredientRegistry.INSTANCE.serializeInput(input));
      }

      obj.add("inputs", list);
      return obj;
   }

   public boolean matches(CraftingContainer inv, Level worldIn) {
      return !this.assemble(inv).isEmpty();
   }

   public ItemStack assemble(CraftingContainer inv) {
      if (this.mod != null) {
         this.mod.reset();
      }

      ItemStack out = this.output.copy();
      int totalCharge = 0;
      LinkedList<IInput> toFind = new LinkedList<>(this.inputs);

      for (int i = 0; i < inv.getContainerSize(); i++) {
         ItemStack stack = inv.getItem(i);
         if (!stack.isEmpty()) {
            Iterator<IInput> iter = toFind.iterator();
            boolean inRecipe = false;

            while (iter.hasNext()) {
               IInput in = iter.next();
               if (in.matches(stack) && in.getInputSize() <= stack.getCount() && (this.mod == null || this.mod.isSlotValid(stack))) {
                  iter.remove();
                  inRecipe = true;
                  break;
               }
            }

            if (!inRecipe) {
               return ItemStack.EMPTY;
            }

            if (this.mod != null && this.mod.isOutputItem(stack)) {
               out = stack.copy();
            } else {
               totalCharge += ElectricItem.MANAGER.getCharge(stack);
            }
         }
      }

      if (!toFind.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         if (totalCharge > 0) {
            ElectricItem.MANAGER.charge(out, totalCharge, 2147483647, true, false);
         }

         if (this.mod != null) {
            out = this.mod.applyChanges(out, false);
         }

         return out;
      }
   }

   public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
      LinkedList<IInput> list = new LinkedList<>(this.inputs);
      int[] slot = new int[inv.getContainerSize()];
      int maxSlot = 0;

      for (int i = 0; i < inv.getContainerSize(); i++) {
         ItemStack stack = inv.getItem(i);
         if (!stack.isEmpty()) {
            Iterator<IInput> iter = list.iterator();

            while (iter.hasNext()) {
               IInput in = iter.next();
               if (in.matches(stack) && in.getInputSize() <= stack.getCount()) {
                  if (in.getInputSize() > 1) {
                     slot[i] = in.getInputSize();
                     maxSlot++;
                  }

                  iter.remove();
                  break;
               }
            }
         }
      }

      if (maxSlot <= 0) {
         return super.getRemainingItems(inv);
      } else {
         NonNullList<ItemStack> result = NonNullList.withSize(slot.length, ItemStack.EMPTY);

         for (int ix = 0; ix < slot.length; ix++) {
            if (slot[ix] < 1) {
               result.set(ix, inv.getItem(ix).getCraftingRemainingItem());
            } else {
               ItemStack stack = inv.getItem(ix).copy();
               inv.setItem(ix, ItemStack.EMPTY);
               result.set(ix, stack);
               if (!stack.isEmpty()) {
                  stack.shrink(slot[ix]);
               }
            }
         }

         return result;
      }
   }

   @Override
   public NonNullList<Ingredient> getIngredients() {
      NonNullList<Ingredient> results = NonNullList.withSize(this.inputs.size(), Ingredient.EMPTY);

      for (int i = 0; i < this.inputs.size(); i++) {
         try {
            results.set(i, this.inputs.get(i).asIngredient());
         } catch (Exception var4) {
            IC2.LOGGER.info("Recipe id [{}] has had a crash during Ingredient creation", this.getId(), var4);
            throw var4;
         }
      }

      return results;
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= this.inputs.size();
   }

   @Override
   public ResourceLocation getMetaSerializer() {
      return new ResourceLocation("ic2", "shapeless");
   }

   public static ShapelessIC2Recipe create(ResourceLocation id, JsonObject object) {
      Tuple<ItemStack, IOutputGenerator> output = generateOutput(GsonHelper.getAsJsonObject(object, "output"));
      if (((ItemStack)output.getA()).isEmpty()) {
         throw new IllegalStateException("Output is Empty!");
      } else {
         List<IInput> inputs = CollectionUtils.<IInput>createList();
         boolean hidden = GsonHelper.getAsBoolean(object, "hidden", false);
         boolean consume = GsonHelper.getAsBoolean(object, "consume", false);
         IRecipeModifier mod = object.has("modifier") ? IngredientRegistry.INSTANCE.readMod(GsonHelper.getAsJsonObject(object, "modifier")) : null;

         for (JsonElement el : GsonHelper.getAsJsonArray(object, "inputs")) {
            inputs.add(IngredientRegistry.INSTANCE.readInput(GsonHelper.convertToJsonObject(el, "input Entry")));
         }

         return new ShapelessIC2Recipe(id, output, hidden, consume, mod, inputs);
      }
   }

   public static ShapelessIC2Recipe create(ResourceLocation id, ItemStack out, Object... args) {
      return create(id, new Tuple(out, new ItemWithNBTGenerator(out)), args);
   }

   public static ShapelessIC2Recipe create(ResourceLocation id, Tuple<ItemStack, IOutputGenerator> out, Object... args) {
      List<IInput> inputs = new ArrayList<>();
      IRecipeModifier mod = null;
      boolean hidden = false;
      boolean consume = false;

      for (Object obj : args) {
         if (obj != null) {
            if (obj instanceof Boolean) {
               hidden = (Boolean)obj;
            } else if (obj instanceof RecipeMods) {
               switch ((RecipeMods)obj) {
                  case CONSUME_CONTAINERS:
                     consume = true;
                     break;
                  case HIDDEN_RECIPE:
                     hidden = true;
               }
            } else if (obj instanceof IRecipeModifier) {
               mod = (IRecipeModifier)obj;
            } else {
               IInput input = RecipeRegistry.INGREDIENTS.createInputFrom(obj);
               if (input != null) {
                  inputs.add(input);
               }
            }
         }
      }

      return new ShapelessIC2Recipe(id, out, hidden, consume, mod, inputs);
   }
}
