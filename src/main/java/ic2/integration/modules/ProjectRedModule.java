package ic2.integration.modules;

import ic2.api.addons.IC2Feature;
import ic2.api.addons.IC2Plugin;
import ic2.api.addons.IModule;
import ic2.api.recipes.ingridients.inputs.ItemTagInput;
import ic2.api.recipes.misc.RecipeMods;
import ic2.core.IC2;
import ic2.core.block.machines.tiles.hv.MassFabricatorTileEntity;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tags;
import ic2.integration.IntegrationItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;

@IC2Plugin(
   id = "projectred",
   name = "ProjectRed - Module"
)
public class ProjectRedModule implements IModule {
   @Override
   public boolean canLoad(Dist side) {
      return ModList.get().isLoaded("projectred-core");
   }

   @IC2Feature(
      key = "alloyFurnace",
      name = "ProjectRedCore: Alloy Furnace",
      comment = "Add Alloy Furnace Recipes",
      depends = {"projectred-core"}
   )
   public void alloyFurnace() {
      IC2.RECIPES.get(true)
         .mixingFurnace
         .registerListener(
            recipes -> {
               if (IntegrationItems.RED_ALLOY_INGOT != null
                  && IntegrationItems.ELECTROTINE_INGOT != null
                  && IntegrationItems.BOULE != null
                  && IntegrationItems.REDSTONE_SILICON != null
                  && IntegrationItems.GLOWSTONE_SILICON != null
                  && IntegrationItems.ELECTROTINE_SILICON != null
                  && IntegrationItems.SILICON != null) {
                  recipes.addIC2SimpleRecipe(
                     "red_alloy_ingot_from_copper",
                     new ItemStack(IntegrationItems.RED_ALLOY_INGOT),
                     new ItemTagInput(IC2Tags.INGOT_COPPER),
                     new ItemStack(Items.REDSTONE, 4)
                  );
                  recipes.addIC2SimpleRecipe(
                     "red_alloy_ingot_from_iron",
                     new ItemStack(IntegrationItems.RED_ALLOY_INGOT),
                     new ItemStack(Items.IRON_INGOT),
                     new ItemStack(Items.REDSTONE, 4)
                  );
                  recipes.addIC2SimpleRecipe(
                     "red_alloy_ingot_from_refined_iron",
                     new ItemStack(IntegrationItems.RED_ALLOY_INGOT),
                     RecipeMods.RECIPE_TIME.create(0.8999999761581421),
                     new ItemStack(IC2Items.INGOT_REFINED_IRON),
                     new ItemStack(Items.REDSTONE, 4)
                  );
                  recipes.addIC2SimpleRecipe(
                     "electrotine_ingot_from_copper",
                     new ItemStack(IntegrationItems.ELECTROTINE_INGOT),
                     new ItemTagInput(IC2Tags.INGOT_COPPER),
                     new ItemStack(IntegrationItems.ELECTROTINE_DUST, 4)
                  );
                  recipes.addIC2SimpleRecipe(
                     "electrotine_ingot_from_iron",
                     new ItemStack(IntegrationItems.ELECTROTINE_INGOT),
                     new ItemStack(Items.IRON_INGOT),
                     new ItemStack(IntegrationItems.ELECTROTINE_DUST, 4)
                  );
                  recipes.addIC2SimpleRecipe(
                     "electrotine_ingot_from_refined_iron",
                     new ItemStack(IntegrationItems.ELECTROTINE_INGOT),
                     RecipeMods.RECIPE_TIME.create(0.8999999761581421),
                     new ItemStack(IC2Items.INGOT_REFINED_IRON),
                     new ItemStack(IntegrationItems.ELECTROTINE_DUST, 4)
                  );
                  recipes.addIC2SimpleRecipe("boule", new ItemStack(IntegrationItems.BOULE), new ItemStack(Items.COAL, 8), new ItemStack(Items.SAND, 8));
                  recipes.addIC2SimpleRecipe(
                     "infused_silicon",
                     new ItemStack(IntegrationItems.REDSTONE_SILICON),
                     new ItemStack(IntegrationItems.SILICON),
                     new ItemStack(Items.REDSTONE, 4)
                  );
                  recipes.addIC2SimpleRecipe(
                     "glowstone_silicon",
                     new ItemStack(IntegrationItems.GLOWSTONE_SILICON),
                     new ItemStack(IntegrationItems.SILICON),
                     new ItemStack(Items.GLOWSTONE_DUST, 4)
                  );
                  recipes.addIC2SimpleRecipe(
                     "electrotine_silicon",
                     new ItemStack(IntegrationItems.ELECTROTINE_SILICON),
                     new ItemStack(IntegrationItems.SILICON),
                     new ItemStack(IntegrationItems.ELECTROTINE_DUST, 4)
                  );
               }
            }
         );
   }

   @IC2Feature(
      key = "gemExtraction",
      name = "ProjectRedExploration: Gem Extraction",
      comment = "Add Gem Extracting Recipes",
      depends = {"projectred-exploration"}
   )
   public void gemExtraction() {
      IC2.RECIPES.get(true).extractor.registerListener(recipes -> {
         if (IntegrationItems.RUBY_ORE != null && IntegrationItems.SAPPHIRE_ORE != null && IntegrationItems.PERIDOT_ORE != null) {
            recipes.addIC2XPRecipe("ruby_ore_to_gem", new ItemStack(IntegrationItems.RUBY_GEM, 3), 0.7F, IntegrationItems.RUBY_ORE);
            recipes.addIC2XPRecipe("sapphire_ore_to_gem", new ItemStack(IntegrationItems.SAPPHIRE_GEM, 3), 0.7F, IntegrationItems.SAPPHIRE_ORE);
            recipes.addIC2XPRecipe("peridot_ore_to_gem", new ItemStack(IntegrationItems.PERIDOT_GEM, 3), 0.7F, IntegrationItems.PERIDOT_ORE);
         }
      });
   }

   @IC2Feature(
      key = "oreMaceration",
      name = "ProjectRedExploration: Ore Maceration",
      comment = "Add Ore Macerating Recipe",
      depends = {"projectred-exploration"}
   )
   public void oreMaceration() {
      IC2.RECIPES.get(true).macerator.registerListener(recipes -> {
         if (IntegrationItems.ELECTROTINE_ORE != null) {
            recipes.addIC2XPRecipe("electrotine_ore_to_dust", new ItemStack(IntegrationItems.ELECTROTINE_DUST, 16), 0.7F, IntegrationItems.ELECTROTINE_ORE);
         }
      });
   }

   @IC2Feature(
      key = "massFabAmplifier",
      name = "ProjectRedExploration: Mass Fabricator Amplifier",
      comment = "Add Amplifiers",
      depends = {"projectred-core"}
   )
   public void massFabAmplifier() {
      IC2.RECIPES.get(true).massFabricator.registerListener(recipes -> {
         if (IntegrationItems.BOULE != null) {
            recipes.addIC2Recipe("boule", MassFabricatorTileEntity.createCount(75000), IntegrationItems.BOULE);
         }
      });
   }
}
