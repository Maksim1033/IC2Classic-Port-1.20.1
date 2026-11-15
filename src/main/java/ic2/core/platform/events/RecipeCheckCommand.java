package ic2.core.platform.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.registries.ICannerRecipeRegistry;
import ic2.api.recipes.registries.IElectrolyzerRecipeList;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.recipes.registries.IRareEarthRegistry;
import ic2.api.recipes.registries.IRefiningRecipeList;
import ic2.core.IC2;
import ic2.core.block.machines.recipes.misc.EnrichRecipe;
import ic2.core.platform.recipes.misc.GlobalRecipes;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.server.command.EnumArgument;

public class RecipeCheckCommand {
   public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
      LiteralArgumentBuilder<CommandSourceStack> source = Commands.literal("missingrecipes");
      source.then(Commands.argument("type", EnumArgument.enumArgument(RecipeCheckCommand.TestType.class)).executes(RecipeCheckCommand::testIC2Recipes));
      dispatcher.register(source);
   }

   private static int testIC2Recipes(CommandContext<CommandSourceStack> entry) {
      RecipeCheckCommand.TestType type = (RecipeCheckCommand.TestType)entry.getArgument("type", RecipeCheckCommand.TestType.class);
      testRecipes("ic2", type.isInput(), type.isOutput(), type == RecipeCheckCommand.TestType.XOR);
      return 0;
   }

   public static void testRecipes(String mod, boolean inputs, boolean outputs, boolean xOr) {
      Set<Item> input = CollectionUtils.<Item>createSet();
      Set<Item> output = CollectionUtils.<Item>createSet();
      fillRecipes(input, output);
      StringBuilder builder = new StringBuilder();
      int count = 0;

      for (Item item : getItemsToTest(mod)) {
         boolean missingInput = !input.contains(item);
         boolean missingOutput = !output.contains(item);
         if (!inputs) {
            missingInput = false;
         }

         if (!outputs) {
            missingOutput = false;
         }

         if (xOr && (!missingInput || !missingOutput)) {
            missingInput = false;
            missingOutput = false;
         }

         if (missingInput || missingOutput) {
            builder.append("\n");
            builder.append(ForgeRegistryWrapper.getItemKey(item)).append(" ");
            builder.append("Is Missing: ").append(missingInput ? (missingOutput ? "Input/Output" : "Input") : "Output");
            count++;
         }
      }

      if (count > 0) {
         IC2.LOGGER.info(builder.toString());
      }
   }

   public static void fillRecipes(Set<Item> inputs, Set<Item> outputs) {
      for (Recipe<?> recipe : ServerLifecycleHooks.getCurrentServer().getRecipeManager().getRecipes()) {
         outputs.add(recipe.getResultItem(ServerLifecycleHooks.getCurrentServer().registryAccess()).getItem());

         for (Ingredient input : recipe.getIngredients()) {
            addTo(input.getItems(), inputs);
         }
      }

      GlobalRecipes recipes = IC2.RECIPES.get(true);
      fillMachineRecipes(recipes.blast_furnace, inputs, outputs);
      fillMachineRecipes(recipes.smoker, inputs, outputs);
      fillMachineRecipes(recipes.sawmill, inputs, outputs);
      fillMachineRecipes(recipes.compressor, inputs, outputs);
      fillMachineRecipes(recipes.extractor, inputs, outputs);
      fillMachineRecipes(recipes.furnace, inputs, outputs);
      fillMachineRecipes(recipes.macerator, inputs, outputs);
      fillMachineRecipes(recipes.massFabricator, inputs, outputs);
      fillMachineRecipes(recipes.mixingFurnace, inputs, outputs);

      for (IRareEarthRegistry.RareEntry entry : recipes.rare_earth.getAllRecipes()) {
         outputs.add(entry.getOutput().getItem());
         inputs.add(entry.getInput().getItem());
      }

      for (List<Tuple<IInput, Integer>> entry : recipes.canner.getRepairItems().values()) {
         for (Tuple<IInput, Integer> subEntry : entry) {
            addTo(((IInput)subEntry.getA()).getComponents(), inputs);
         }
      }

      for (ICannerRecipeRegistry.FuelValue fuel : recipes.canner.getFuels()) {
         inputs.add(fuel.getStack().getItem());
      }

      for (Entry<ItemStack, List<Tuple<IInput, ItemStack>>> entry : recipes.canner.getFillables().entrySet()) {
         inputs.add(entry.getKey().getItem());

         for (Tuple<IInput, ItemStack> subEntry : entry.getValue()) {
            addTo(((IInput)subEntry.getA()).getComponents(), inputs);
            outputs.add(((ItemStack)subEntry.getB()).getItem());
         }
      }

      for (IElectrolyzerRecipeList.ElectrolyzerRecipe entry : recipes.electrolyzer.getRecipes()) {
         inputs.add(entry.getInput().getItem());
         outputs.add(entry.getOutput().getItem());
      }

      for (EnrichRecipe entry : recipes.enricher.getRecipes()) {
         inputs.addAll(entry.getInputs());
         outputs.add(entry.getOutput().getItem());
      }

      for (IRefiningRecipeList.FluidRecipe entry : recipes.refining.getAllRecipes()) {
         addTo(entry.getItemInput().getComponents(), inputs);
         addTo(entry.getOutput().getAllOutputs(), outputs);
      }
   }

   public static void fillMachineRecipes(IMachineRecipeList list, Set<Item> inputs, Set<Item> outputs) {
      for (IMachineRecipeList.RecipeEntry entry : list.getAllEntries()) {
         for (IInput input : entry.getInputs()) {
            addTo(input.getComponents(), inputs);
         }

         addTo(entry.getOutput().getAllOutputs(), outputs);
      }
   }

   public static void addTo(Collection<ItemStack> items, Set<Item> result) {
      for (ItemStack item : items) {
         result.add(item.getItem());
      }
   }

   public static void addTo(ItemStack[] items, Set<Item> result) {
      for (ItemStack item : items) {
         result.add(item.getItem());
      }
   }

   public static List<Item> getItemsToTest(String mod) {
      List<Item> items = CollectionUtils.<Item>createList();

      for (Item item : ForgeRegistries.ITEMS.getValues()) {
         if (ForgeRegistryWrapper.getItemKey(item).getNamespace().equalsIgnoreCase(mod)) {
            items.add(item);
         }
      }

      return items;
   }

   public static enum TestType {
      INPUTS,
      OUTPUTS,
      BOTH,
      XOR;

      public boolean isInput() {
         return this == INPUTS || this == BOTH || this == XOR;
      }

      public boolean isOutput() {
         return this == OUTPUTS || this == BOTH || this == XOR;
      }
   }
}
