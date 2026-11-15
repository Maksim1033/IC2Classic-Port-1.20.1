package ic2.core.block.machines.tiles.lv;

import ic2.api.recipes.ingridients.generators.ItemGenerator;
import ic2.api.recipes.ingridients.recipes.SimpleRecipeOutput;
import ic2.api.recipes.misc.RecipeMods;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.core.IC2;
import ic2.core.block.base.tiles.impls.machine.single.BasicMachineTileEntity;
import ic2.core.block.machines.recipes.misc.ScrapOutput;
import ic2.core.platform.recipes.crafting.helpers.DamagedInput;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RecyclerTileEntity extends BasicMachineTileEntity {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/lv/gui_recycler.png");
   public static final IMachineRecipeList.RecipeEntry SCRAP_BOX = ScrapOutput.createEntry(68);

   public RecyclerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3, 1, 45, 32);
   }

   @Override
   public IMachineRecipeList.RecipeEntry getRecipe(int slot, ItemStack input) {
      return input.getItem() == IC2Items.SCRAP ? (input.getCount() >= 9 ? SCRAP_BOX : null) : this.getRecipeList().getRecipe(input, true);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   protected ResourceLocation getWorkingSound() {
      return IC2Sounds.RECYCLER_PROCESSING;
   }

   @Override
   public IMachineRecipeList getRecipeList() {
      return IC2.RECIPES.get(this.isSimulating()).recycler;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.RECYCLER;
   }

   @Override
   protected boolean consumeContainers() {
      return true;
   }

   public static void loadRecipes(IMachineRecipeList list) {
      list.addIC2RangeRecipe("flesh_composting", new ItemStack(IC2Items.FERTILIZER), 0, 1, Items.ROTTEN_FLESH);
      list.addIC2Recipe(
         "rare_earth_recycling",
         new SimpleRecipeOutput(ObjectLists.singleton(new ItemGenerator(IC2Items.SCRAP_METAL)), RecipeMods.RECIPE_TIME.create(10.0), 0.0F),
         IC2Items.RARE_EARTH_CHUNK
      );
      list.addIC2ChanceRecipe("basalt_fertilizer", new ItemStack(IC2Items.FERTILIZER), 0.0F, 0.25F, Items.BASALT);
      addRecycleRecipes(list);
   }

   public static void addRecycleRecipes(IMachineRecipeList list) {
      list.addIC2XPRecipe("recycle_iron_horse_armor", new ItemStack(Items.IRON_INGOT, 5), 0.2F, Items.IRON_HORSE_ARMOR);
      list.addIC2XPRecipe("recycle_gold_horse_armor", new ItemStack(Items.GOLD_INGOT, 5), 0.5F, Items.GOLDEN_HORSE_ARMOR);
      list.addIC2XPRecipe("recycle_diamond_horse_armor", new ItemStack(Items.DIAMOND, 5), 1.0F, Items.DIAMOND_HORSE_ARMOR);
      String[] type = new String[]{"diamond", "iron", "gold", "bronze"};
      Item[] axe = new Item[]{Items.DIAMOND_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, IC2Items.AXE_BRONZE};
      Item[] pickaxe = new Item[]{Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE, IC2Items.PICKAXE_BRONZE};
      Item[] shovel = new Item[]{Items.DIAMOND_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, IC2Items.SHOVEL_BRONZE};
      Item[] hoe = new Item[]{Items.DIAMOND_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, IC2Items.HOE_BRONZE};
      Item[] sword = new Item[]{Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, IC2Items.SWORD_BRONZE};
      Item[] helmet = new Item[]{Items.DIAMOND_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, IC2Items.BRONZE_HELMET};
      Item[] chestplate = new Item[]{Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, IC2Items.BRONZE_CHEST};
      Item[] leggings = new Item[]{Items.DIAMOND_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, IC2Items.BRONZE_LEGGINGS};
      Item[] boots = new Item[]{Items.DIAMOND_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, IC2Items.BRONZE_BOOTS};
      ItemStack[] results = new ItemStack[]{
         new ItemStack(Items.DIAMOND), new ItemStack(Items.IRON_INGOT), new ItemStack(Items.GOLD_INGOT), new ItemStack(IC2Items.INGOT_BRONZE)
      };

      for (int i = 0; i < type.length; i++) {
         list.addIC2RangeRecipe("recycling_axe_" + type[i], results[i].copy(), 0, 3, new DamagedInput(axe[i]).setUndamaged());
         list.addIC2RangeRecipe("recycling_pickaxe_" + type[i], results[i].copy(), 0, 3, new DamagedInput(pickaxe[i]).setUndamaged());
         list.addIC2RangeRecipe("recycling_shovel_" + type[i], results[i].copy(), 0, 1, new DamagedInput(shovel[i]).setUndamaged());
         list.addIC2RangeRecipe("recycling_hoe_" + type[i], results[i].copy(), 0, 2, new DamagedInput(hoe[i]).setUndamaged());
         list.addIC2RangeRecipe("recycling_sword_" + type[i], results[i].copy(), 0, 2, new DamagedInput(sword[i]).setUndamaged());
         list.addIC2RangeRecipe("recycling_helmet_" + type[i], results[i].copy(), 0, 5, new DamagedInput(helmet[i]).setUndamaged());
         list.addIC2RangeRecipe("recycling_chestplate_" + type[i], results[i].copy(), 0, 8, new DamagedInput(chestplate[i]).setUndamaged());
         list.addIC2RangeRecipe("recycling_leggings_" + type[i], results[i].copy(), 0, 7, new DamagedInput(leggings[i]).setUndamaged());
         list.addIC2RangeRecipe("recycling_boots_" + type[i], results[i].copy(), 0, 4, new DamagedInput(boots[i]).setUndamaged());
      }

      for (int i = 0; i < type.length; i++) {
         list.addIC2RangeRecipe("recycling_damaged_axe_" + type[i], new ItemStack(IC2Items.SCRAP_METAL), 0, 3, new DamagedInput(axe[i]));
         list.addIC2RangeRecipe("recycling_damaged_pickaxe_" + type[i], new ItemStack(IC2Items.SCRAP_METAL), 0, 3, new DamagedInput(pickaxe[i]));
         list.addIC2RangeRecipe("recycling_damaged_shovel_" + type[i], new ItemStack(IC2Items.SCRAP_METAL), 0, 1, new DamagedInput(shovel[i]));
         list.addIC2RangeRecipe("recycling_damaged_hoe_" + type[i], new ItemStack(IC2Items.SCRAP_METAL), 0, 2, new DamagedInput(hoe[i]));
         list.addIC2RangeRecipe("recycling_damaged_sword_" + type[i], new ItemStack(IC2Items.SCRAP_METAL), 0, 2, new DamagedInput(sword[i]));
         list.addIC2RangeRecipe("recycling_damaged_helmet_" + type[i], new ItemStack(IC2Items.SCRAP_METAL), 0, 5, new DamagedInput(helmet[i]));
         list.addIC2RangeRecipe("recycling_damaged_chestplate_" + type[i], new ItemStack(IC2Items.SCRAP_METAL), 0, 8, new DamagedInput(chestplate[i]));
         list.addIC2RangeRecipe("recycling_damaged_leggings_" + type[i], new ItemStack(IC2Items.SCRAP_METAL), 0, 7, new DamagedInput(leggings[i]));
         list.addIC2RangeRecipe("recycling_damaged_boots_" + type[i], new ItemStack(IC2Items.SCRAP_METAL), 0, 4, new DamagedInput(boots[i]));
      }

      list.addIC2RangeRecipe("recycling_ancient_debris", new ItemStack(Items.NETHERITE_SCRAP), 1, 5, Blocks.ANCIENT_DEBRIS);
   }
}
