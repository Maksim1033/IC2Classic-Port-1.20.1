package ic2.core.platform.recipes.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class ShapedIC2Recipe extends RecipeIC2Base implements IShapedRecipe<CraftingContainer> {
   private static final String RECIPE_CHARACTERS = "XYCVBNMKJHG";
   public int width;
   public int height;
   public IInput[] inputs;
   public IOutputGenerator generator;
   public int xOffset = 0;
   public int yOffset = 0;

   protected ShapedIC2Recipe(
      ResourceLocation id,
      Tuple<ItemStack, IOutputGenerator> output,
      boolean hidden,
      boolean consume,
      IRecipeModifier mod,
      int width,
      int height,
      IInput[] inputs
   ) {
      super(id, (ItemStack)output.getA(), hidden, consume, mod);
      this.generator = (IOutputGenerator)output.getB();
      this.mod = mod;
      this.width = width;
      this.height = height;
      this.inputs = inputs;
   }

   public ShapedIC2Recipe(FriendlyByteBuf buffer, ResourceLocation id) {
      super(id, buffer.readItem(), buffer.readBoolean(), buffer.readBoolean(), IngredientRegistry.INSTANCE.readMod(buffer));
      this.width = buffer.readByte();
      this.height = buffer.readByte();
      this.inputs = new IInput[buffer.readByte()];

      for (int i = 0; i < this.inputs.length; i++) {
         this.inputs[i] = IngredientRegistry.INSTANCE.readInput(buffer);
      }
   }

   @Override
   public void serialize(FriendlyByteBuf buffer) {
      buffer.writeItemStack(this.output, false);
      buffer.writeBoolean(this.hidden);
      buffer.writeBoolean(this.consume);
      IngredientRegistry.INSTANCE.writeMod(this.mod, buffer);
      buffer.writeByte(this.width);
      buffer.writeByte(this.height);
      buffer.writeByte(this.inputs.length);

      for (IInput input : this.inputs) {
         IngredientRegistry.INSTANCE.writeInput(input, buffer);
      }
   }

   @Override
   public JsonObject serialize() {
      JsonObject obj = new JsonObject();
      obj.addProperty("type", "ic2:crafting_ic2_automated");
      obj.addProperty("sub_type", "ic2:shaped");
      obj.addProperty("enabled", true);
      obj.add("output", IngredientRegistry.INSTANCE.serializeOutputGenerator(this.generator));
      obj.addProperty("hidden", this.hidden);
      obj.addProperty("consume", this.consume);
      if (this.mod != null) {
         obj.add("modifier", IngredientRegistry.INSTANCE.serializeModifier(this.mod));
      }

      CharList[] lanes = new CharList[this.height];

      for (int i = 0; i < this.height; i++) {
         lanes[i] = this.getLines();
      }

      int index = 0;
      JsonArray list = new JsonArray();

      for (Entry<IInput, IntList> entry : this.mapInputs().entrySet()) {
         char letter = "XYCVBNMKJHG".charAt(index);
         JsonObject data = IngredientRegistry.INSTANCE.serializeInput(entry.getKey());
         data.addProperty("letter", letter);
         list.add(data);
         IntListIterator var9 = entry.getValue().iterator();

         while (var9.hasNext()) {
            int slot = (Integer)var9.next();
            lanes[slot / this.width].set(slot % this.width, letter);
         }

         index++;
      }

      obj.add("inputs", list);
      list = new JsonArray();

      for (CharList lane : lanes) {
         list.add(new String(lane.toCharArray()));
      }

      obj.add("pattern", list);
      return obj;
   }

   public static ShapedIC2Recipe create(ResourceLocation id, JsonObject object) {
      try {
         Tuple<ItemStack, IOutputGenerator> output = generateOutput(GsonHelper.getAsJsonObject(object, "output"));
         if (((ItemStack)output.getA()).isEmpty()) {
            throw new IllegalStateException("Output is Empty!");
         } else {
            List<Object> result = CollectionUtils.<Object>createList();
            if (GsonHelper.getAsBoolean(object, "hidden", false)) {
               result.add(RecipeMods.HIDDEN_RECIPE);
            }

            if (GsonHelper.getAsBoolean(object, "consume", false)) {
               result.add(RecipeMods.CONSUME_CONTAINERS);
            }

            if (object.has("modifier")) {
               result.add(IngredientRegistry.INSTANCE.readMod(GsonHelper.getAsJsonObject(object, "modifier")));
            }

            for (JsonElement element : GsonHelper.getAsJsonArray(object, "pattern")) {
               result.add(GsonHelper.convertToString(element, "pattern entry"));
            }

            for (JsonElement element : GsonHelper.getAsJsonArray(object, "inputs")) {
               JsonObject obj = GsonHelper.convertToJsonObject(element, "Input Entry");
               IInput input = IngredientRegistry.INSTANCE.readInput(obj);
               if (input == null) {
                  throw new IllegalStateException("Input not found!");
               }

               String letter = GsonHelper.getAsString(obj, "letter");
               if (letter.length() != 1) {
                  throw new JsonSyntaxException("letter needs to be a single character");
               }

               result.add(letter.charAt(0));
               result.add(input);
            }

            return create(id, output, result.toArray(Object[]::new));
         }
      } catch (Exception var9) {
         throw new RuntimeException("failed to load " + id.toString(), var9);
      }
   }

   private CharList getLines() {
      CharList builder = new CharArrayList();

      for (int i = 0; i < this.width; i++) {
         builder.add(' ');
      }

      return builder;
   }

   private Map<IInput, IntList> mapInputs() {
      Map<IInput, IntList> map = CollectionUtils.<IInput, IntList>createLinkedMap();

      for (int i = 0; i < this.inputs.length; i++) {
         IInput input = this.inputs[i];
         if (input != null) {
            map.computeIfAbsent(input, T -> new IntArrayList()).add(i);
         }
      }

      return map;
   }

   @Override
   public ResourceLocation getMetaSerializer() {
      return new ResourceLocation("ic2", "shaped");
   }

   public boolean matches(CraftingContainer inv, Level worldIn) {
      for (int x = 0; x <= inv.getWidth() - this.width; x++) {
         for (int y = 0; y <= inv.getHeight() - this.height; y++) {
            if (this.checkMatch(inv, x, y)) {
               this.xOffset = x;
               this.yOffset = y;
               return true;
            }
         }
      }

      this.xOffset = -1;
      this.yOffset = -1;
      return false;
   }

   public ItemStack assemble(CraftingContainer inv) {
      int totalCharge = 0;
      ItemStack out = this.output.copy();

      for (int i = 0; i < inv.getContainerSize(); i++) {
         ItemStack stack = inv.getItem(i);
         if (!stack.isEmpty()) {
            if (this.mod != null && this.mod.isOutputItem(stack)) {
               out = stack.copy();
            } else {
               totalCharge += ElectricItem.MANAGER.getCharge(stack);
            }
         }
      }

      if (totalCharge > 0) {
         ElectricItem.MANAGER.charge(out, totalCharge, 2147483647, true, false);
      }

      if (this.mod != null) {
         out = this.mod.applyChanges(out, false);
      }

      return out;
   }

   public boolean checkMatch(CraftingContainer inv, int xStart, int yStart) {
      if (this.mod != null) {
         this.mod.reset();
      }

      for (int x = 0; x < inv.getWidth(); x++) {
         for (int y = 0; y < inv.getHeight(); y++) {
            int subX = x - xStart;
            int subY = y - yStart;
            IInput target = null;
            if (subX >= 0 && subY >= 0 && subX < this.width && subY < this.height) {
               target = this.inputs[subX + subY * this.width];
            }

            ItemStack slot = inv.getItem(x + y * inv.getWidth());
            if (slot.isEmpty() && target != null || !slot.isEmpty() && target == null) {
               return false;
            }

            if (target != null) {
               if (!target.matches(slot) || slot.getCount() < target.getInputSize()) {
                  return false;
               }

               if (this.mod != null && !this.mod.isSlotValid(slot)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
      if (this.xOffset >= 0 && this.yOffset >= 0) {
         IInput[] toRemove = new IInput[inv.getContainerSize()];
         int max = 0;

         for (int i = 0; i < this.inputs.length; i++) {
            IInput input = this.inputs[i];
            if (input != null) {
               int row = i / this.width + this.yOffset;
               int col = i % this.width + this.xOffset;
               int slot = row * inv.getWidth() + col;
               if (input.getInputSize() > 1 || this.canBeDrained(inv.getItem(slot))) {
                  toRemove[slot] = input;
                  max++;
               }
            }
         }

         if (max <= 0) {
            return super.getRemainingItems(inv);
         } else {
            NonNullList<ItemStack> result = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

            for (int ix = 0; ix < toRemove.length; ix++) {
               IInput input = toRemove[ix];
               if (input != null && !this.canBeDrained(inv.getItem(ix))) {
                  ItemStack stack = inv.getItem(ix).copy();
                  inv.setItem(ix, ItemStack.EMPTY);
                  result.set(ix, stack);
                  if (!stack.isEmpty()) {
                     stack.shrink(input.getInputSize());
                  }
               } else if (input != null) {
                  result.set(ix, StackUtil.copyWithSize(inv.getItem(ix).getCraftingRemainingItem(), input.getInputSize()));
                  inv.getItem(ix).shrink(input.getInputSize() - 1);
               } else {
                  result.set(ix, inv.getItem(ix).getCraftingRemainingItem());
               }
            }

            return result;
         }
      } else {
         return super.getRemainingItems(inv);
      }
   }

   public boolean canCraftInDimensions(int width, int height) {
      return width >= this.width && height >= this.height;
   }

   public RecipeType<?> getType() {
      return RecipeType.CRAFTING;
   }

   public int getRecipeWidth() {
      return this.width;
   }

   public int getRecipeHeight() {
      return this.height;
   }

   @Override
   public NonNullList<Ingredient> getIngredients() {
      NonNullList<Ingredient> result = NonNullList.withSize(this.inputs.length, Ingredient.EMPTY);

      for (int i = 0; i < this.inputs.length; i++) {
         if (this.inputs[i] != null) {
            try {
               result.set(i, this.inputs[i].asIngredient());
            } catch (Exception var4) {
               IC2.LOGGER.info("Recipe id [{}] has had a crash during Ingredient creation", this.getId(), var4);
               throw var4;
            }
         }
      }

      return result;
   }

   public static ShapedIC2Recipe create(ResourceLocation id, ItemStack out, Object... args) {
      return create(id, new Tuple(out.copy(), new ItemWithNBTGenerator(out)), args);
   }

   public static ShapedIC2Recipe create(ResourceLocation id, Tuple<ItemStack, IOutputGenerator> out, Object... args) {
      IRecipeModifier mod = null;
      boolean hidden = false;
      boolean consume = false;
      int height = 0;
      List<String> template = new ArrayList<>();
      Char2ObjectMap<IInput> charToInput = new Char2ObjectOpenHashMap();
      Character lastChar = null;

      for (Object obj : args) {
         if (obj != null) {
            if (obj instanceof String) {
               template.add((String)obj);
            } else if (obj instanceof Boolean) {
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
               if (mod != null) {
                  throw new RuntimeException("Only 1 Mod is allowed");
               }

               mod = (IRecipeModifier)obj;
            } else if (obj instanceof Character) {
               if (lastChar != null) {
                  throw new RuntimeException("Expecting Input");
               }

               lastChar = (Character)obj;
            } else {
               IInput input = RecipeRegistry.INGREDIENTS.createInputFrom(obj);
               if (input == null) {
                  throw new RuntimeException("Input is EMPTY");
               }

               if (lastChar == null) {
                  throw new RuntimeException("Expecting Char");
               }

               charToInput.put(lastChar, input);
               lastChar = null;
            }
         }
      }

      if (!template.isEmpty() && template.size() <= 3) {
         StringBuilder shape = new StringBuilder();
         int width = -1;

         for (String key : template) {
            shape.append(key);
            if (width != -1 && width != key.length()) {
               throw new RuntimeException("All Template keys need to have the same length");
            }

            width = key.length();
            height++;
         }

         if (width * height != shape.length()) {
            throw new RuntimeException("Template is not Matching");
         } else {
            IInput[] inputs = new IInput[shape.length()];
            int slot = 0;

            for (int y = 0; y < height; y++) {
               for (int x = 0; x < width; x++) {
                  inputs[slot] = (IInput)charToInput.get(shape.charAt(slot));
                  slot++;
               }
            }

            return new ShapedIC2Recipe(id, out, hidden, consume, mod, width, height, inputs);
         }
      } else {
         throw new RuntimeException("Template is not Matching");
      }
   }
}
