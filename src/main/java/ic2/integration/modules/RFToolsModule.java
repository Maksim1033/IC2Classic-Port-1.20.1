package ic2.integration.modules;

import ic2.api.addons.IC2Feature;
import ic2.api.addons.IC2Plugin;
import ic2.api.addons.IModule;
import ic2.core.IC2;
import ic2.integration.IntegrationItems;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;

@IC2Plugin(
   id = "rftoolsbase",
   name = "RFToolsBase - Module"
)
public class RFToolsModule implements IModule {
   @Override
   public boolean canLoad(Dist side) {
      return ModList.get().isLoaded("rftoolsbase");
   }

   @IC2Feature(
      key = "shardExtraction",
      name = "RFToolsBase: Shard Extraction",
      comment = "Add Shard Extraction Recipes",
      depends = {"rftoolsbase"}
   )
   public void shardExtraction() {
      IC2.RECIPES.get(true)
         .extractor
         .registerListener(
            recipes -> {
               if (IntegrationItems.SHARD != null
                  && IntegrationItems.SHARD_STONE != null
                  && IntegrationItems.SHARD_NETHER != null
                  && IntegrationItems.SHARD_END != null) {
                  recipes.addIC2RangeRecipe("dimensionalshard_ore_to_shard", new ItemStack(IntegrationItems.SHARD), 4, 7, IntegrationItems.SHARD_STONE);
                  recipes.addIC2RangeRecipe("dimensionalshard_nether_ore_to_shard", new ItemStack(IntegrationItems.SHARD), 4, 7, IntegrationItems.SHARD_NETHER);
                  recipes.addIC2RangeRecipe("dimensionalshard_end_ore_to_shard", new ItemStack(IntegrationItems.SHARD), 4, 7, IntegrationItems.SHARD_END);
               }
            }
         );
   }
}
