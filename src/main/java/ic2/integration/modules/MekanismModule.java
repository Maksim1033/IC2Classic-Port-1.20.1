package ic2.integration.modules;

import ic2.api.addons.IC2Feature;
import ic2.api.addons.IC2Plugin;
import ic2.api.addons.IModule;
import ic2.api.recipes.ingridients.generators.ItemGenerator;
import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.api.recipes.ingridients.inputs.ItemInput;
import ic2.api.recipes.misc.RecipeMods;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.recipes.registries.IRefiningRecipeList;
import ic2.core.IC2;
import ic2.core.block.machines.recipes.misc.EnrichRecipe;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Fluids;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.ColorUtils;
import ic2.integration.IntegrationItems;
import ic2.integration.IntegrationTags;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;

@IC2Plugin(
   id = "mekanism",
   name = "Mekanism - Module"
)
public class MekanismModule implements IModule {
   @Override
   public boolean canLoad(Dist side) {
      return ModList.get().isLoaded("mekanism");
   }

   @IC2Feature(
      key = "oreMaceration",
      name = "Mekanism: Ore Maceration",
      comment = "Add Ore Macerating Recipes",
      depends = {"mekanism"}
   )
   public void oreMaceration() {
      IC2.RECIPES.get(true)
         .macerator
         .registerListener(
            recipes -> {
               addMacerationSet(
                  recipes,
                  "osmium",
                  IntegrationItems.OSMIUM_DUST,
                  IntegrationTags.ORE_OSMIUM,
                  IntegrationTags.RAW_OSMIUM,
                  IntegrationTags.STORAGE_RAW_OSMIUM,
                  IntegrationTags.STORAGE_OSMIUM,
                  IntegrationTags.INGOT_OSMIUM
               );
               addMacerationSet(
                  recipes,
                  "lead",
                  IntegrationItems.LEAD_DUST,
                  IntegrationTags.ORE_LEAD,
                  IntegrationTags.RAW_LEAD,
                  IntegrationTags.STORAGE_RAW_LEAD,
                  IntegrationTags.STORAGE_LEAD,
                  IntegrationTags.INGOT_LEAD
               );
            }
         );
   }

   @IC2Feature(
      key = "uraniumEnrichment",
      name = "Mekanism: Uranium Enrichment",
      comment = "Add Uranium Enrichment Recipes",
      depends = {"mekanism"}
   )
   public void uraniumEnrichment() {
      IC2.RECIPES.get(true)
         .enricher
         .registerListener(
            recipes -> {
               if (IntegrationItems.URANIUM_INGOT != null) {
                  recipes.registerRecipe(
                     EnrichRecipe.createIC2Recipe(
                        IntegrationItems.URANIUM_INGOT,
                        Items.BLAZE_ROD,
                        200,
                        IC2Items.INGOT_URANIUM_ENRICHED_BLAZE,
                        100,
                        ColorUtils.rgb(232, 155, 7, 255),
                        20,
                        "blaze"
                     )
                  );
                  Object2IntMap<IInput> inputs = new Object2IntLinkedOpenHashMap();
                  inputs.put(new ItemInput(Items.CHARCOAL), 25);
                  inputs.put(new ItemInput(IC2Blocks.CHARCOAL_BLOCK), 200);
                  recipes.registerRecipe(
                     EnrichRecipe.createIC2Recipe(
                        IntegrationItems.URANIUM_INGOT, inputs, IC2Items.INGOT_URANIUM_ENRICHED_CHARCOAL, 100, ColorUtils.rgb(54, 54, 54, 255), 5, "charcoal"
                     )
                  );
                  recipes.registerRecipe(
                     EnrichRecipe.createIC2Recipe(
                        IntegrationItems.URANIUM_INGOT,
                        Items.ENDER_PEARL,
                        100,
                        IC2Items.INGOT_URANIUM_ENRICHED_ENDERPEARL,
                        100,
                        ColorUtils.rgb(35, 174, 113, 255),
                        75,
                        "ender"
                     )
                  );
                  recipes.registerRecipe(
                     EnrichRecipe.createIC2Recipe(
                        IntegrationItems.URANIUM_INGOT,
                        Items.NETHER_STAR,
                        200,
                        IC2Items.INGOT_URANIUM_ENRICHED_NETHERSTAR,
                        150,
                        ColorUtils.rgb(255, 239, 106, 255),
                        150,
                        "nether_star"
                     )
                  );
                  inputs = new Object2IntLinkedOpenHashMap();
                  inputs.put(new ItemInput(Items.REDSTONE), 25);
                  inputs.put(new ItemInput(Items.REDSTONE_BLOCK), 200);
                  recipes.registerRecipe(
                     EnrichRecipe.createIC2Recipe(
                        IntegrationItems.URANIUM_INGOT, inputs, IC2Items.INGOT_URANIUM_ENRICHED_REDSTONE, 100, ColorUtils.RED, 25, "redstone"
                     )
                  );
               }
            }
         );
   }

   @IC2Feature(
      key = "oreRefinement",
      name = "Mekanism: Ore Refinement",
      comment = "Add Ore Refinement Recipes",
      depends = {"mekanism"}
   )
   public void oreRefinement() {
      IC2.RECIPES.get(true).refining.registerListener(recipes -> {
         if (IntegrationItems.OSMIUM_RAW != null && IntegrationItems.LEAD_RAW != null) {
            createOreBuilder(recipes, "osmium_to_raw", IntegrationTags.ORE_OSMIUM, IntegrationItems.OSMIUM_RAW, 1);
            createOreBuilder(recipes, "lead_to_raw", IntegrationTags.ORE_LEAD, IntegrationItems.LEAD_RAW, 1);
         }
      });
   }

   private static void createOreBuilder(IRefiningRecipeList recipe, String id, TagKey<Item> ore, Item output, int base) {
      recipe.addIC2Recipe(id)
         .addMod(RecipeMods.RECIPE_TIME, 0.5)
         .catalyst(ore)
         .mainInput(Fluids.LAVA, 25 * base)
         .buildRange(new ItemGenerator(output), base, base * 3);
      recipe.addIC2Recipe(id + "_advanced")
         .addMod(RecipeMods.RECIPE_TIME, 0.5)
         .catalyst(ore)
         .mainInput(IC2Fluids.BIO_FUEL, 50 * base)
         .buildRange(new ItemGenerator(output), base * 3, base * 5);
      recipe.addIC2Recipe(id + "_alk")
         .addMod(RecipeMods.RECIPE_TIME, 0.5)
         .catalyst(ore)
         .mainInput(IC2Fluids.ALCOHOL, 25 * base)
         .buildRange(new ItemGenerator(output), base * 5, base * 7);
   }

   private static void addMacerationSet(
      IMachineRecipeList recipes, String name, Item dust, TagKey<?> ore, TagKey<?> raw, TagKey<?> rawBlock, TagKey<?> block, TagKey<?> ingot
   ) {
      if (dust != null) {
         recipes.addIC2XPRecipe(name + "_ore_to_dust", new ItemStack(dust, 2), 0.7F, ore);
         recipes.addIC2XPRecipe(name + "_raw_to_dust", new ItemStack(dust, 2), 0.7F, raw);
         recipes.addIC2XPRecipe(name + "_raw_block_to_dust", new ItemStack(dust, 18), 6.3F, RecipeMods.RECIPE_TIME.create(4.0), rawBlock);
         recipes.addIC2XPRecipe(name + "_block_to_dust", new ItemStack(dust, 9), 0.9F, RecipeMods.RECIPE_TIME.create(4.0), block);
         recipes.addIC2XPRecipe(name + "_ingot_to_dust", new ItemStack(dust, 1), 0.1F, ingot);
      }
   }
}
