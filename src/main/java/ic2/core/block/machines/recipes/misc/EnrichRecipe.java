package ic2.core.block.machines.recipes.misc;

import ic2.api.recipes.RecipeRegistry;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.ItemInput;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class EnrichRecipe {
   ResourceLocation id;
   ObjectSet<Item> input;
   Object2IntMap<IInput> fuels;
   ItemStack output;
   int points;
   int energyCost;
   int color;

   public EnrichRecipe(ResourceLocation id, Item input, Object2IntMap<IInput> fuels, ItemStack output, int points, int color, int energyCost) {
      this(id, ObjectLinkedOpenHashSet.of(input), fuels, output, points, color, energyCost);
   }

   public EnrichRecipe(ResourceLocation id, ObjectSet<Item> input, Object2IntMap<IInput> fuels, ItemStack output, int points, int color, int energyCost) {
      this.id = id;
      this.input = input;
      this.fuels = fuels;
      this.output = output;
      this.points = points;
      this.color = color;
      this.energyCost = energyCost;
   }

   public EnrichRecipe(FriendlyByteBuf buffer) {
      this.id = buffer.readResourceLocation();
      int inputCount = buffer.readVarInt();
      this.input = ObjectLinkedOpenHashSet.of();

      for (int i = 0; i < inputCount; i++) {
         this.input.add((Item)buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS));
      }

      this.fuels = new Object2IntLinkedOpenHashMap();
      int size = buffer.readVarInt();

      for (int i = 0; i < size; i++) {
         int points = buffer.readVarInt();
         IInput input = RecipeRegistry.INGREDIENTS.readInput(buffer);
         if (input != null) {
            this.fuels.put(input, points);
         }
      }

      this.output = buffer.readItem();
      this.points = buffer.readVarInt();
      this.energyCost = buffer.readVarInt();
      this.color = buffer.readInt();
   }

   public static EnrichRecipe createRecipe(Item input, Object2IntMap<IInput> fuels, Item output, int points, int color, int energyCost, ResourceLocation id) {
      return new EnrichRecipe(id, input, fuels, new ItemStack(output), points, color, energyCost);
   }

   public static EnrichRecipe createRecipe(Item input, Item fuel, int fuelPoints, Item output, int points, int color, int energyCost, ResourceLocation id) {
      return new EnrichRecipe(id, input, Object2IntMaps.singleton(new ItemInput(fuel), fuelPoints), new ItemStack(output), points, color, energyCost);
   }

   public static EnrichRecipe createIC2Recipe(Item input, Object2IntMap<IInput> fuels, Item output, int points, int color, int energyCost, String id) {
      return new EnrichRecipe(new ResourceLocation("ic2", id), input, fuels, new ItemStack(output), points, color, energyCost);
   }

   public static EnrichRecipe createIC2Recipe(Item input, Item fuel, int fuelPoints, Item output, int points, int color, int energyCost, String id) {
      return new EnrichRecipe(
         new ResourceLocation("ic2", id), input, Object2IntMaps.singleton(new ItemInput(fuel), fuelPoints), new ItemStack(output), points, color, energyCost
      );
   }

   public boolean isValidInput(ItemStack inputStack, ItemStack outputStack) {
      return this.input.contains(inputStack.getItem())
         && (outputStack.isEmpty() || StackUtil.isStackEqual(this.output, outputStack) && StackUtil.getStackSizeLeft(outputStack) >= this.output.getCount());
   }

   public List<ItemStack> getFuels() {
      List<ItemStack> list = new ObjectArrayList();
      ObjectIterator var2 = this.fuels.keySet().iterator();

      while (var2.hasNext()) {
         IInput input = (IInput)var2.next();
         list.addAll(input.getComponents());
      }

      return list;
   }

   public int getFuelValue(ItemStack stack) {
      ObjectIterator var2 = Object2IntMaps.fastIterable(this.fuels).iterator();

      while (var2.hasNext()) {
         Entry<IInput> entry = (Entry<IInput>)var2.next();
         if (((IInput)entry.getKey()).matches(stack)) {
            return entry.getIntValue();
         }
      }

      return 0;
   }

   public int getRequiredPoints() {
      return this.points;
   }

   public int getEnergyCost() {
      return this.energyCost;
   }

   public Set<Item> getInputs() {
      return ObjectSets.unmodifiable(this.input);
   }

   public void addInput(Item item) {
      this.input.add(item);
   }

   public void addInputs(Set<Item> items) {
      this.input.addAll(items);
   }

   public ItemStack getOutput() {
      return this.output.copy();
   }

   public int getColor() {
      return this.color;
   }

   public Object2IntMap<IInput> getFuelValues() {
      return Object2IntMaps.unmodifiable(this.fuels);
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public void write(FriendlyByteBuf buffer) {
      buffer.writeResourceLocation(this.id);
      buffer.writeVarInt(this.input.size());
      ObjectIterator var2 = this.input.iterator();

      while (var2.hasNext()) {
         Item i = (Item)var2.next();
         buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, i);
      }

      buffer.writeVarInt(this.fuels.size());
      var2 = Object2IntMaps.fastIterable(this.fuels).iterator();

      while (var2.hasNext()) {
         Entry<IInput> entry = (Entry<IInput>)var2.next();
         buffer.writeVarInt(entry.getIntValue());
         RecipeRegistry.INGREDIENTS.writeInput((IInput)entry.getKey(), buffer);
      }

      buffer.writeItem(this.output);
      buffer.writeVarInt(this.points);
      buffer.writeVarInt(this.energyCost);
      buffer.writeInt(this.color);
   }
}
