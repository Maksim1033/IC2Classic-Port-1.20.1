package ic2.core.block.machines.tiles.lv;

import ic2.api.recipes.misc.RecipeFlags;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.IC2;
import ic2.core.block.base.tiles.impls.machine.single.BasicMachineTileEntity;
import ic2.core.block.machines.recipes.misc.ScrapBoxOutput;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tags;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ExtractorTileEntity extends BasicMachineTileEntity {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/lv/gui_extractor.png");

   public ExtractorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3, 2, 400, 32);
   }

   @Override
   public IMachineRecipeList getRecipeList() {
      return IC2.RECIPES.get(this.isSimulating()).extractor;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   protected ResourceLocation getWorkingSound() {
      return IC2Sounds.EXTRACTOR_PROCESSING;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.EXTRACTOR;
   }

   public static void loadRecipes(IMachineRecipeList list) {
      list.addIC2XPRecipe("coal_extraction", new ItemStack(Items.COAL, 5), 0.3F, net.minecraftforge.common.Tags.Items.ORES_COAL);
      list.addIC2XPRecipe("lapis_extraction", new ItemStack(Items.LAPIS_LAZULI, 10), 0.5F, net.minecraftforge.common.Tags.Items.ORES_LAPIS);
      list.addIC2XPRecipe("diamond_extraction", new ItemStack(Items.DIAMOND, 3), 1.0F, net.minecraftforge.common.Tags.Items.ORES_DIAMOND);
      list.addIC2XPRecipe("emerald_extraction", new ItemStack(Items.EMERALD, 3), 1.0F, net.minecraftforge.common.Tags.Items.ORES_EMERALD);
      list.addIC2XPRecipe("quartz_extraction", new ItemStack(Items.QUARTZ, 3), 1.0F, net.minecraftforge.common.Tags.Items.ORES_QUARTZ);
      list.addIC2SimpleRecipe("uranium_extraction", new ItemStack(IC2Items.ORE_URANIUM_DROP, 3), IC2Tags.ORE_URANIUM);
      list.addIC2SimpleRecipe("flint_extraction", new ItemStack(Items.FLINT, 2), Items.GRAVEL);
      list.addIC2SimpleRecipe("sticky_extraction", new ItemStack(IC2Items.RUBBER, 3), IC2Items.STICKY_RESIN);
      list.addIC2SimpleRecipe("rubber_log_extraction", new ItemStack(IC2Items.RUBBER), IC2Blocks.RUBBERWOOD_LOG);
      list.addIC2SimpleRecipe("rubber_sapling_extraction", new ItemStack(IC2Items.RUBBER), IC2Blocks.RUBBER_SAPLING);
      list.addIC2SimpleRecipe("poppy_extraction", new ItemStack(Items.RED_DYE, 3), Items.POPPY);
      list.addIC2SimpleRecipe("dandelion_extraction", new ItemStack(Items.YELLOW_DYE, 3), Items.DANDELION);
      list.addIC2SimpleRecipe("blue_orchid_extraction", new ItemStack(Items.LIGHT_BLUE_DYE, 3), Items.BLUE_ORCHID);
      list.addIC2SimpleRecipe("allium_extraction", new ItemStack(Items.MAGENTA_DYE, 3), Items.ALLIUM);
      list.addIC2SimpleRecipe("azure_bluet_extraction", new ItemStack(Items.LIGHT_GRAY_DYE, 3), Items.AZURE_BLUET);
      list.addIC2SimpleRecipe("red_tulip_extraction", new ItemStack(Items.RED_DYE, 3), Items.RED_TULIP);
      list.addIC2SimpleRecipe("orange_tulip_extraction", new ItemStack(Items.ORANGE_DYE, 3), Items.ORANGE_TULIP);
      list.addIC2SimpleRecipe("white_tulip_extraction", new ItemStack(Items.LIGHT_GRAY_DYE, 3), Items.WHITE_TULIP);
      list.addIC2SimpleRecipe("pink_tulip_extraction", new ItemStack(Items.PINK_DYE, 3), Items.PINK_TULIP);
      list.addIC2SimpleRecipe("oxeye_daisy_extraction", new ItemStack(Items.LIGHT_GRAY_DYE, 3), Items.OXEYE_DAISY);
      list.addIC2SimpleRecipe("cornflower_extraction", new ItemStack(Items.BLUE_DYE, 3), Items.CORNFLOWER);
      list.addIC2SimpleRecipe("lily_of_the_valley_extraction", new ItemStack(Items.WHITE_DYE, 3), Items.LILY_OF_THE_VALLEY);
      list.addIC2SimpleRecipe("wither_rose_extraction", new ItemStack(Items.BLACK_DYE, 3), Items.WITHER_ROSE);
      list.addIC2SimpleRecipe("beetroot_extraction", new ItemStack(Items.RED_DYE, 3), Items.BEETROOT);
      list.addIC2SimpleRecipe("sunflower_extraction", new ItemStack(Items.YELLOW_DYE, 6), Items.SUNFLOWER);
      list.addIC2SimpleRecipe("lilac_extraction", new ItemStack(Items.MAGENTA_DYE, 6), Items.LILAC);
      list.addIC2SimpleRecipe("rose_bush_extraction", new ItemStack(Items.RED_DYE, 6), Items.ROSE_BUSH);
      list.addIC2SimpleRecipe("peony_extraction", new ItemStack(Items.PINK_DYE, 6), Items.PEONY);
      list.addIC2SimpleRecipe("hydration_cell", new ItemStack(IC2Items.HYDRATION_CELL), RecipeFlags.CONSUME_CONTAINERS.apply(), IC2Items.CELL_WATER);
      list.addIC2XPRecipe("bio_cell_refinement", new ItemStack(IC2Items.CELL_BIO_FUEL), 0.1F, IC2Items.CELL_BIO);
      list.addIC2XPRecipe("wood_gas_refinement", new ItemStack(IC2Items.CELL_BIO_FUEL), 0.1F, RecipeFlags.CONSUME_CONTAINERS.apply(), IC2Items.CELL_WOODGAS);
      list.addIC2XPRecipe("coal_cell_refinement", new ItemStack(IC2Items.CELL_COAL_FUEL), 0.1F, IC2Items.CELL_HYDRATED_COAL);
      list.addIC2XPRecipe("water_cell_refinement", new ItemStack(IC2Items.CELL_WATER), 0.1F, IC2Items.HYDRATION_CELL);
      list.addIC2SimpleRecipe("resin_removal", new ItemStack(IC2Blocks.DYNAMITE), IC2Items.STICKY_DYNAMITE);
      list.addIC2Recipe("scrapbox_unpacking", new ScrapBoxOutput(), IC2Items.SCRAPBOX);
      list.addIC2SimpleRecipe("slimy_business", new ItemStack(Items.SLIME_BALL), Items.MAGMA_CREAM);
   }
}
