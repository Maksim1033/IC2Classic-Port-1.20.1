package ic2.integration.modules;

import ic2.api.addons.IC2Feature;
import ic2.api.addons.IC2Plugin;
import ic2.api.addons.IModule;
import ic2.api.recipes.misc.RecipeMods;
import ic2.core.IC2;
import ic2.integration.IntegrationItems;
import ic2.integration.IntegrationTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;

@IC2Plugin(
   id = "appliedenergistics2",
   name = "AppliedEnergistics - Module"
)
public class AE2Module implements IModule {
   @Override
   public boolean canLoad(Dist side) {
      return ModList.get().isLoaded("ae2");
   }

   @IC2Feature(
      key = "crystalMaceration",
      name = "AE2: Crystal Maceration",
      comment = "Add Crystal Macerating Recipes",
      depends = {"ae2"}
   )
   public void crystalMaceration() {
      IC2.RECIPES.get(true)
         .macerator
         .registerListener(
            recipes -> {
               if (IntegrationItems.CERTUS_QUARTZ_DUST != null) {
                  recipes.addIC2XPRecipe(
                     "certus_quartz_block_to_dust",
                     new ItemStack(IntegrationItems.CERTUS_QUARTZ_DUST, 4),
                     0.4F,
                     RecipeMods.RECIPE_TIME.create(2.0),
                     IntegrationTags.BLOCK_CERTUS_QUARTZ
                  );
                  recipes.addIC2XPRecipe(
                     "certus_quartz_gem_to_dust", new ItemStack(IntegrationItems.CERTUS_QUARTZ_DUST, 1), 0.1F, IntegrationTags.GEM_CERTUS_QUARTZ
                  );
               }

               if (IntegrationItems.FLUIX_DUST != null) {
                  recipes.addIC2XPRecipe("fluix_gem_to_dust", new ItemStack(IntegrationItems.FLUIX_DUST, 1), 0.1F, IntegrationTags.GEM_FLUIX);
                  recipes.addIC2XPRecipe(
                     "fluix_block_to_dust",
                     new ItemStack(IntegrationItems.FLUIX_DUST, 4),
                     0.1F,
                     RecipeMods.RECIPE_TIME.create(2.0),
                     IntegrationItems.FLUIX_BLOCK
                  );
               }

               if (IntegrationItems.SKY_DUST != null && IntegrationItems.SKY_STONE_BLOCK != null) {
                  recipes.addIC2XPRecipe("sky_stone_block_to_dust", new ItemStack(IntegrationItems.SKY_DUST, 1), 0.1F, IntegrationItems.SKY_STONE_BLOCK);
               }

               if (IntegrationItems.ENDER_DUST != null) {
                  recipes.addIC2XPRecipe("ender_pearl_to_dust", new ItemStack(IntegrationItems.ENDER_DUST, 1), 0.1F, Items.ENDER_PEARL);
               }
            }
         );
   }
}
