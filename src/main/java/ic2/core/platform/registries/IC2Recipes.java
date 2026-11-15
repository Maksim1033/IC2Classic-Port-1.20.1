package ic2.core.platform.registries;

import ic2.api.blocks.PainterHelper;
import ic2.api.items.IDrinkableFluid;
import ic2.api.items.IRepairable;
import ic2.api.recipes.ingridients.inputs.ArrayInput;
import ic2.api.recipes.ingridients.inputs.ItemInput;
import ic2.api.recipes.ingridients.inputs.ItemListInput;
import ic2.api.recipes.ingridients.inputs.ItemTagInput;
import ic2.api.recipes.ingridients.inputs.SubItemInput;
import ic2.api.recipes.registries.IAdvancedCraftingManager;
import ic2.api.recipes.registries.IUUMatterRegistry;
import ic2.core.IC2;
import ic2.core.block.crops.CropRegistry;
import ic2.core.block.crops.PlanterPotBlock;
import ic2.core.item.food_and_drink.IC2Drink;
import ic2.core.item.food_and_drink.IC2FoodsAndDrinks;
import ic2.core.item.reactor.ReactorUraniumRod;
import ic2.core.item.reactor.base.IUraniumRod;
import ic2.core.item.reactor.urantypes.StandardUranium;
import ic2.core.platform.recipes.crafting.helpers.RecipeMods;
import ic2.core.platform.recipes.mods.CountingModifier;
import ic2.core.platform.recipes.mods.EmptyInventoryModifier;
import ic2.core.platform.recipes.mods.FlagModifier;
import ic2.core.platform.recipes.mods.InfoModifier;
import ic2.core.platform.recipes.mods.ModularArmorModifier;
import ic2.core.platform.recipes.mods.TransferInventoryModifier;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public class IC2Recipes {
   public static void addRecipes(IAdvancedCraftingManager recipes) {
      addResources(recipes);
      addRubberwood(recipes);
      addCables(recipes);
      addTransport(recipes);
      addComponents(recipes);
      addFoodAndDrink(recipes);
      addCrops(recipes);
      addMisc(recipes);
      addGenerators(recipes);
      addNVMachines(recipes);
      addMachinesLV(recipes);
      addMachinesMV(recipes);
      addMachinesHV(recipes);
      addMachinesEV(recipes);
      addMachinesLuV(recipes);
      addPersonalBlocks(recipes);
      addUpgrades(recipes);
      addTools(recipes);
      addArmor(recipes);
      addCFoam(recipes);
      addEnergyStorage(recipes);
      addReactorComponents(recipes);
   }

   public static void addUU(IUUMatterRegistry registry) {
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("cobblestone_uum").setOutput(Items.COBBLESTONE, 16).setShape("M  ", "   ", "   "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("stone_uum").setOutput(Items.STONE, 16).setShape("   ", " M ", "   "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("glass_uum").setOutput(Items.GLASS, 32).setShape(" M ", "M M", " M "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("grass_uum").setOutput(Items.GRASS, 16).setShape("   ", "M  ", "M  "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("acacia_log_uum").setOutput(Items.ACACIA_LOG, 8).setShape(" M ", "   ", "   "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("birch_log_uum").setOutput(Items.BIRCH_LOG, 8).setShape("  M", "   ", "   "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("dark_oak_log_uum").setOutput(Items.DARK_OAK_LOG, 8).setShape("   ", "M  ", "   "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("jungle_log_uum").setOutput(Items.JUNGLE_LOG, 8).setShape("   ", "  M", "   "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("oak_log_uum").setOutput(Items.OAK_LOG, 8).setShape("   ", "   ", "M  "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("spruce_log_uum").setOutput(Items.SPRUCE_LOG, 8).setShape("   ", "   ", " M "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("dirt_uum").setOutput(Items.DIRT, 16).setShape("   ", "   ", "  M"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("sponge_uum").setOutput(Items.SPONGE, 6).setShape(" M ", "M M", " MM"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("end_stone_uum").setOutput(Items.END_STONE, 16).setShape("   ", "M M", " MM"));
      registry.registerUUShape(
         new IUUMatterRegistry.UUMatterBuilder("mossy_cobblestone_uum").setOutput(Items.MOSSY_COBBLESTONE, 16).setShape("   ", " M ", "M M")
      );
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("sandstone_uum").setOutput(Items.SANDSTONE, 16).setShape("   ", "  M", " M "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("red_sandstone_uum").setOutput(Items.RED_SANDSTONE, 16).setShape("   ", "M  ", " M "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("snow_uum").setOutput(Items.SNOW, 4).setShape("M M", "   ", "   "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("water_uum").setOutput(Blocks.WATER, 1).setShape("   ", " M ", " M "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("lava_uum").setOutput(Blocks.LAVA, 1).setShape(" M ", " M ", " M "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("iron_ore_uum").setOutput(Items.IRON_ORE, 5).setShape("M M", " M ", "M M"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("gold_ore_uum").setOutput(Items.GOLD_ORE, 2).setShape(" M ", "MMM", " M "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("obsidian_uum").setOutput(Items.OBSIDIAN, 12).setShape("M M", "M M", "   "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("netherrack_uum").setOutput(Items.NETHERRACK, 16).setShape("  M", " M ", "M  "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("nether_wart_uum").setOutput(Items.NETHER_WART, 32).setShape(" MM", "M  ", " MM"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("glowstone__uum").setOutput(Items.GLOWSTONE, 8).setShape(" M ", "M M", "MMM"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("cactus_uum").setOutput(Items.CACTUS, 48).setShape(" M ", "MMM", "M M"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("vine_uum").setOutput(Items.VINE, 24).setShape("M  ", "M  ", "M  "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("chorus_fruit_uum").setOutput(Items.CHORUS_FRUIT, 4).setShape("MM ", " MM", " M "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("white_wool_uum").setOutput(Items.WHITE_WOOL, 12).setShape("M M", "   ", " M "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("coal_uum").setOutput(Items.COAL, 20).setShape("  M", "M  ", "  M"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("diamond_uum").setOutput(Items.DIAMOND, 1).setShape("MMM", "MMM", "MMM"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("redstone_uum").setOutput(Items.REDSTONE, 24).setShape("   ", " M ", "MMM"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("quartz_uum").setOutput(Items.QUARTZ, 32).setShape(" MM", "M M", "MM "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("lapis_lazuli_uum").setOutput(Items.LAPIS_LAZULI, 9).setShape(" M ", " M ", " MM"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("feather_uum").setOutput(Items.FEATHER, 32).setShape(" M ", " M ", "M M"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("leather_uum").setOutput(Items.LEATHER, 32).setShape("M M", "MMM", "   "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("snowball_uum").setOutput(Items.SNOWBALL, 16).setShape("   ", "   ", "MMM"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("gunpowder_uum").setOutput(Items.GUNPOWDER, 15).setShape("MMM", "M  ", "MMM"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("ender_pearl_uum").setOutput(Items.ENDER_PEARL, 2).setShape("MM ", " MM", "M  "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("clay_ball_uum").setOutput(Items.CLAY_BALL, 48).setShape("MM ", "M  ", "MM "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("blaze_rod_uum").setOutput(Items.BLAZE_ROD, 6).setShape(" M ", "M  ", "MMM"));
      registry.registerUUShape(
         new IUUMatterRegistry.UUMatterBuilder("prismarine_crystals_uum").setOutput(Items.PRISMARINE_CRYSTALS, 16).setShape(" MM", "M M", " M ")
      );
      registry.registerUUShape(
         new IUUMatterRegistry.UUMatterBuilder("prismarine_shard_uum").setOutput(Items.PRISMARINE_SHARD, 32).setShape("MM ", "M M", " M ")
      );
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("cocoa_beans_uum").setOutput(Items.COCOA_BEANS, 32).setShape("MM ", "  M", "MM "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("soul_soil_uum").setOutput(Items.SOUL_SOIL, 24).setShape("MMM", " M ", " M "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("ink_sac_uum").setOutput(Items.INK_SAC, 48).setShape(" MM", " MM", " M "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("sugar_cane_uum").setOutput(Items.SUGAR_CANE, 48).setShape("M M", "M M", "M M"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("flint_uum").setOutput(Items.FLINT, 32).setShape(" M ", "MM ", "MM "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("bone_uum").setOutput(Items.BONE, 32).setShape("M  ", "MM ", "M  "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("sticky_resin_uum").setOutput(IC2Items.STICKY_RESIN, 21).setShape("M M", "   ", "M M"));
      registry.registerUUShape(
         new IUUMatterRegistry.UUMatterBuilder("ore_iridium_uum").setOutput(IC2Items.ORE_IRIDIUM, 1).setShape("MMM", " M ", "MMM").setVisible()
      );
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("mycelium_uum").setOutput(Items.MYCELIUM, 24).setShape("   ", "M M", "MMM"));
      registry.registerUUShape(
         new IUUMatterRegistry.UUMatterBuilder("chiseled_stone_brick_uum").setOutput(Items.CHISELED_STONE_BRICKS, 48).setShape("MM ", "MM ", "M  ")
      );
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("copper_ore_uum").setOutput(Blocks.COPPER_ORE, 5).setShape("  M", "M M", "   "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("tin_ore_uum").setOutput(IC2Blocks.TIN_ORE, 5).setShape("   ", "M M", "  M"));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("silver_ore_uum").setOutput(IC2Blocks.SILVER_ORE, 2).setShape(" M ", "M M", "MM "));
      registry.registerUUShape(new IUUMatterRegistry.UUMatterBuilder("elytra_uum").setOutput(Items.ELYTRA, 1).setShape("MMM", "M M", "M M").addLetters('M', 16));
   }

   private static void addResources(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "bronze_block", new ItemStack(IC2Blocks.BRONZE_BLOCK), "MMM", "MXM", "MMM", 'M', IC2Tags.INGOT_BRONZE, 'X', IC2Items.INGOT_BRONZE
      );
      recipes.addShapedIC2Recipe(
         "silver_block", new ItemStack(IC2Blocks.SILVER_BLOCK), "MMM", "MXM", "MMM", 'M', IC2Tags.INGOT_SILVER, 'X', IC2Items.INGOT_SILVER
      );
      recipes.addShapedIC2Recipe(
         "uranium_block", new ItemStack(IC2Blocks.URANIUM_BLOCK), "MMM", "MXM", "MMM", 'M', IC2Tags.INGOT_URANIUM, 'X', IC2Items.INGOT_URANIUM
      );
      recipes.addShapedIC2Recipe("tin_block", new ItemStack(IC2Blocks.TIN_BLOCK), "MMM", "MXM", "MMM", 'M', IC2Tags.INGOT_TIN, 'X', IC2Items.INGOT_TIN);
      recipes.addShapedIC2Recipe(
         "aluminum_block", new ItemStack(IC2Blocks.ALUMINIUM_BLOCK), "MMM", "MXM", "MMM", 'M', IC2Tags.INGOT_ALUMINIUM, 'X', IC2Items.INGOT_ALUMINIUM
      );
      recipes.addShapedIC2Recipe(
         "refined_iron_block",
         new ItemStack(IC2Blocks.REFINED_IRON_BLOCK),
         "MMM",
         "MXM",
         "MMM",
         'M',
         IC2Tags.INGOT_REFINED_IRON,
         'X',
         IC2Items.INGOT_REFINED_IRON
      );
      recipes.addShapedIC2Recipe("charcoal_block", new ItemStack(IC2Blocks.CHARCOAL_BLOCK), "MMM", "MMM", "MMM", 'M', Items.CHARCOAL);
      recipes.addShapedIC2Recipe("machine_block", new ItemStack(IC2Blocks.MACHINE_BLOCK), "XXX", "X X", "XXX", 'X', IC2Tags.INGOT_REFINED_IRON);
      recipes.addShapedIC2Recipe(
         "stabilized_machine_block",
         new ItemStack(IC2Blocks.STABILIZED_MACHINE_BLOCK),
         " C ",
         "XYX",
         " C ",
         'C',
         IC2Items.MAGNET,
         'Y',
         IC2Blocks.ADVANCED_MACHINE_BLOCK,
         'X',
         IC2Tags.INGOT_SILVER
      );
      recipes.addShapedIC2Recipe("raw_tin_block", new ItemStack(IC2Blocks.RAW_TIN_BLOCK), "MMM", "MXM", "MMM", 'M', IC2Tags.RAW_TIN, 'X', IC2Items.RAW_TIN);
      recipes.addShapedIC2Recipe(
         "raw_silver_block", new ItemStack(IC2Blocks.RAW_SILVER_BLOCK), "MMM", "MXM", "MMM", 'M', IC2Tags.RAW_SILVER, 'X', IC2Items.RAW_SILVER
      );
      recipes.addShapedIC2Recipe(
         "raw_aluminium_block", new ItemStack(IC2Blocks.RAW_ALUMINIUM_BLOCK), "MMM", "MXM", "MMM", 'M', IC2Tags.RAW_ALUMINIUM, 'X', IC2Items.RAW_ALUMINIUM
      );
      recipes.addShapedIC2Recipe(
         "raw_uranium_block", new ItemStack(IC2Blocks.URANIUM_DROP_BLOCK), "MMM", "MXM", "MMM", 'M', IC2Tags.RAW_URANIUM, 'X', IC2Items.ORE_URANIUM_DROP
      );
      recipes.addShapelessIC2Recipe("bronze_ingot", new ItemStack(IC2Items.INGOT_BRONZE, 9), IC2Blocks.BRONZE_BLOCK);
      recipes.addShapelessIC2Recipe("silver_ingot", new ItemStack(IC2Items.INGOT_SILVER, 9), IC2Blocks.SILVER_BLOCK);
      recipes.addShapelessIC2Recipe("uranium_ingot", new ItemStack(IC2Items.INGOT_URANIUM, 9), IC2Blocks.URANIUM_BLOCK);
      recipes.addShapelessIC2Recipe("tin_ingot", new ItemStack(IC2Items.INGOT_TIN, 9), IC2Blocks.TIN_BLOCK);
      recipes.addShapelessIC2Recipe("aluminum_ingot", new ItemStack(IC2Items.INGOT_ALUMINIUM, 9), IC2Blocks.ALUMINIUM_BLOCK);
      recipes.addShapelessIC2Recipe("refined_iron_ingot_0", new ItemStack(IC2Items.INGOT_REFINED_IRON, 9), IC2Blocks.REFINED_IRON_BLOCK);
      recipes.addShapelessIC2Recipe("charcoal", new ItemStack(Items.CHARCOAL, 9), IC2Blocks.CHARCOAL_BLOCK);
      recipes.addShapelessIC2Recipe("refined_iron_ingot_1", new ItemStack(IC2Items.INGOT_REFINED_IRON, 8), IC2Blocks.MACHINE_BLOCK);
      recipes.addStoneCutterIC2Recipe("machine_block_plating", new ItemStack(IC2Blocks.MACHINE_BLOCK_PLATED), IC2Blocks.MACHINE_BLOCK);
      recipes.addStoneCutterIC2Recipe("machine_block_framing", new ItemStack(IC2Blocks.STEAM_TUNNEL_MULTIBLOCK), IC2Blocks.MACHINE_BLOCK);
      recipes.addShapelessIC2Recipe("raw_tin", new ItemStack(IC2Items.RAW_TIN, 9), IC2Blocks.RAW_TIN_BLOCK);
      recipes.addShapelessIC2Recipe("raw_silver", new ItemStack(IC2Items.RAW_SILVER, 9), IC2Blocks.RAW_SILVER_BLOCK);
      recipes.addShapelessIC2Recipe("raw_aluminium", new ItemStack(IC2Items.RAW_ALUMINIUM, 9), IC2Blocks.RAW_ALUMINIUM_BLOCK);
      recipes.addShapelessIC2Recipe("raw_uranium", new ItemStack(IC2Items.ORE_URANIUM_DROP, 9), IC2Blocks.URANIUM_DROP_BLOCK);
      recipes.addShapelessIC2Recipe(
         "bronze_dust", new ItemStack(IC2Items.DUST_BRONZE, 4), IC2Tags.DUST_TIN, IC2Tags.DUST_COPPER, IC2Tags.DUST_COPPER, IC2Tags.DUST_COPPER
      );
      recipes.addIC2SmeltingRecipe("sticky_extraction", new ItemStack(IC2Items.RUBBER), IC2Items.STICKY_RESIN, IAdvancedCraftingManager.SmeltingType.FURNACE);
      recipes.addIC2SmeltingRecipe(
         "iron_refining",
         new ItemStack(IC2Items.INGOT_REFINED_IRON),
         net.minecraftforge.common.Tags.Items.INGOTS_IRON,
         IAdvancedCraftingManager.SmeltingType.FURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "blastfurnace_bronze_ingot",
         new ItemStack(IC2Items.INGOT_BRONZE),
         IC2Tags.DUST_BRONZE,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "blastfurnace_tin_ingot_0",
         new ItemStack(IC2Items.INGOT_TIN),
         IC2Tags.DUST_TIN,
         0.2F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "blastfurnace_copper_ingot_0",
         new ItemStack(Items.COPPER_INGOT),
         IC2Tags.DUST_COPPER,
         0.15F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "blastfurnace_silver_ingot_0",
         new ItemStack(IC2Items.INGOT_SILVER),
         IC2Tags.DUST_SILVER,
         0.4F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "blastfurnace_iron_ingot",
         new ItemStack(Items.IRON_INGOT),
         IC2Tags.DUST_IRON,
         0.35F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "blastfurnace_gold_ingot",
         new ItemStack(Items.GOLD_INGOT),
         IC2Tags.DUST_GOLD,
         0.5F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "raw_tin_smelting",
         new ItemStack(IC2Items.INGOT_TIN),
         IC2Items.RAW_TIN,
         0.2F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "raw_silver_smelting",
         new ItemStack(IC2Items.INGOT_SILVER),
         IC2Items.RAW_SILVER,
         0.4F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "iron_dust_block",
         new ItemStack(Items.IRON_BLOCK),
         IC2Blocks.DUST_IRON_BLOCK,
         3.15F,
         4.0F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "gold_dust_block",
         new ItemStack(Items.GOLD_BLOCK),
         IC2Blocks.DUST_GOLD_BLOCK,
         4.5F,
         4.0F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "silver_dust_block",
         new ItemStack(IC2Blocks.SILVER_BLOCK),
         IC2Blocks.DUST_SILVER_BLOCK,
         3.6F,
         4.0F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "copper_dust_block",
         new ItemStack(Items.COPPER_BLOCK),
         IC2Blocks.DUST_COPPER_BLOCK,
         3.15F,
         4.0F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "tin_dust_block",
         new ItemStack(IC2Blocks.TIN_BLOCK),
         IC2Blocks.DUST_TIN_BLOCK,
         1.8F,
         4.0F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "bronze_dust_block",
         new ItemStack(IC2Blocks.BRONZE_BLOCK),
         IC2Blocks.DUST_BRONZE_BLOCK,
         0.0F,
         4.0F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "iron_to_refined_block",
         new ItemStack(IC2Blocks.REFINED_IRON_BLOCK),
         Blocks.IRON_BLOCK,
         0.0F,
         4.0F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addShapedIC2Recipe("iron_dust_to_block", new ItemStack(IC2Blocks.DUST_IRON_BLOCK), "XXX", "XXX", "XXX", 'X', IC2Items.DUST_IRON);
      recipes.addShapedIC2Recipe("gold_dust_to_block", new ItemStack(IC2Blocks.DUST_GOLD_BLOCK), "XXX", "XXX", "XXX", 'X', IC2Items.DUST_GOLD);
      recipes.addShapedIC2Recipe("copper_dust_to_block", new ItemStack(IC2Blocks.DUST_COPPER_BLOCK), "XXX", "XXX", "XXX", 'X', IC2Items.DUST_COPPER);
      recipes.addShapedIC2Recipe(
         "tin_dust_to_block", new ItemStack(IC2Blocks.DUST_TIN_BLOCK), "XXX", "XMX", "XXX", 'X', IC2Tags.DUST_TIN, 'M', IC2Items.DUST_TIN
      );
      recipes.addShapedIC2Recipe(
         "silver_dust_to_block", new ItemStack(IC2Blocks.DUST_SILVER_BLOCK), "XXX", "XMX", "XXX", 'X', IC2Tags.DUST_SILVER, 'M', IC2Items.DUST_SILVER
      );
      recipes.addShapedIC2Recipe(
         "bronze_dust_to_block", new ItemStack(IC2Blocks.DUST_BRONZE_BLOCK), "XXX", "XMX", "XXX", 'X', IC2Tags.DUST_BRONZE, 'M', IC2Items.DUST_BRONZE
      );
      recipes.addShapedIC2Recipe(
         "aluminium_dust_to_block",
         new ItemStack(IC2Blocks.DUST_ALUMINIUM_BLOCK),
         "XXX",
         "XMX",
         "XXX",
         'X',
         IC2Tags.DUST_ALUMINIUM,
         'M',
         IC2Items.DUST_ALUMINIUM
      );
      recipes.addShapelessIC2Recipe("block_to_iron_dust", new ItemStack(IC2Items.DUST_IRON, 9), IC2Blocks.DUST_IRON_BLOCK);
      recipes.addShapelessIC2Recipe("block_to_gold_dust", new ItemStack(IC2Items.DUST_GOLD, 9), IC2Blocks.DUST_GOLD_BLOCK);
      recipes.addShapelessIC2Recipe("block_to_copper_dust", new ItemStack(IC2Items.DUST_COPPER, 9), IC2Blocks.DUST_COPPER_BLOCK);
      recipes.addShapelessIC2Recipe("block_to_tin_dust", new ItemStack(IC2Items.DUST_TIN, 9), IC2Blocks.DUST_TIN_BLOCK);
      recipes.addShapelessIC2Recipe("block_to_silver_dust", new ItemStack(IC2Items.DUST_SILVER, 9), IC2Blocks.DUST_SILVER_BLOCK);
      recipes.addShapelessIC2Recipe("block_to_bronze_dust", new ItemStack(IC2Items.DUST_BRONZE, 9), IC2Blocks.DUST_BRONZE_BLOCK);
      recipes.addShapelessIC2Recipe("block_to_aluminium_dust", new ItemStack(IC2Items.DUST_ALUMINIUM, 9), IC2Blocks.DUST_ALUMINIUM_BLOCK);
      recipes.addIC2SmeltingRecipe(
         "blastfurnace_tin_ingot_1",
         new ItemStack(IC2Items.INGOT_TIN),
         IC2Blocks.TIN_ORE,
         0.4F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "blastfurnace_silver_ingot_1",
         new ItemStack(IC2Items.INGOT_SILVER),
         IC2Blocks.SILVER_ORE,
         0.8F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "blastfurnace_silver_ingot_2",
         new ItemStack(IC2Items.INGOT_SILVER),
         IC2Blocks.SILVER_ORE_NETHER,
         0.8F,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.BLASTFURNACE
      );
      recipes.addShapedIC2Recipe(
         "emerald_uum", new ItemStack(Items.EMERALD, 1), " M ", "MDM", " M ", 'D', Items.DIAMOND, 'M', IC2Items.UUMATTER, RecipeMods.HIDDEN_RECIPE
      );
      recipes.addShapedIC2Recipe(
         "shulker_uum", new ItemStack(Items.SHULKER_SHELL, 2), " M ", "MDM", " M ", 'D', Items.SHULKER_SHELL, 'M', IC2Items.UUMATTER, RecipeMods.HIDDEN_RECIPE
      );
   }

   private static void addRubberwood(IAdvancedCraftingManager recipes) {
      recipes.addShapelessIC2Recipe("rubberwood_planks", new ItemStack(IC2Blocks.RUBBERWOOD_PLANKS, 4), IC2Blocks.RUBBERWOOD_LOG);
      recipes.addShapelessIC2Recipe("rubberwood_planks_bark", new ItemStack(IC2Blocks.RUBBERWOOD_PLANKS, 4), IC2Blocks.RUBBERWOOD_LOG_BARKED);
      recipes.addShapelessIC2Recipe("rubberwood_stripped_planks", new ItemStack(IC2Blocks.RUBBERWOOD_PLANKS, 4), IC2Blocks.RUBBER_LOG_STRIPPED);
      recipes.addShapelessIC2Recipe("rubberwood_stripped_planks_bark", new ItemStack(IC2Blocks.RUBBERWOOD_PLANKS, 4), IC2Blocks.RUBBER_LOG_BARKED_STRIPPED);
      recipes.addShapedIC2Recipe("rubberwood_log_bark", new ItemStack(IC2Blocks.RUBBERWOOD_LOG_BARKED, 3), "XX", "XX", 'X', IC2Blocks.RUBBERWOOD_LOG);
      recipes.addShapedIC2Recipe("rubberwood_door", new ItemStack(IC2Blocks.RUBBERWOOD_DOOR, 3), "XX", "XX", "XX", 'X', IC2Blocks.RUBBERWOOD_PLANKS);
      recipes.addShapedIC2Recipe("rubberwood_slab", new ItemStack(IC2Blocks.RUBBERWOOD_SLAB, 6), "XXX", 'X', IC2Blocks.RUBBERWOOD_PLANKS);
      recipes.addShapedIC2Recipe("rubberwood_stairs_left", new ItemStack(IC2Blocks.RUBBERWOOD_STAIRS, 4), "X  ", "XX ", "XXX", 'X', IC2Blocks.RUBBERWOOD_PLANKS);
      recipes.addShapedIC2Recipe(
         "rubberwood_stairs_right", new ItemStack(IC2Blocks.RUBBERWOOD_STAIRS, 4), "  X", " XX", "XXX", 'X', IC2Blocks.RUBBERWOOD_PLANKS
      );
      recipes.addShapedIC2Recipe(
         "rubberwood_fence", new ItemStack(IC2Blocks.RUBBERWOOD_FENCE, 3), "XSX", "XSX", 'X', IC2Blocks.RUBBERWOOD_PLANKS, 'S', Items.STICK
      );
      recipes.addShapedIC2Recipe(
         "rubberwood_fence_gate", new ItemStack(IC2Blocks.RUBBERWOOD_FENCE_GATE), "SXS", "SXS", 'X', IC2Blocks.RUBBERWOOD_PLANKS, 'S', Items.STICK
      );
      recipes.addShapedIC2Recipe("rubberwood_pressure_plate", new ItemStack(IC2Blocks.RUBBERWOOD_PRESSURE_PLATE), "XX", 'X', IC2Blocks.RUBBERWOOD_PLANKS);
      recipes.addShapedIC2Recipe("rubberwood_trapdoor", new ItemStack(IC2Blocks.RUBBERWOOD_TRAPDOOR, 2), "XXX", "XXX", 'X', IC2Blocks.RUBBERWOOD_PLANKS);
      recipes.addShapedIC2Recipe(
         "rubberwood_sign", new ItemStack(IC2Blocks.RUBBERWOOD_SIGN), "XXX", "XXX", " C ", 'C', Items.STICK, 'X', IC2Blocks.RUBBERWOOD_PLANKS
      );
      recipes.addShapedIC2Recipe("rubberwood_boat", new ItemStack(IC2Items.RUBBERWOOD_BOAT), "X X", "XXX", 'X', IC2Blocks.RUBBERWOOD_PLANKS);
      recipes.addShapelessIC2Recipe("rubberwood_button", new ItemStack(IC2Blocks.RUBBERWOOD_BUTTON), IC2Blocks.RUBBERWOOD_PLANKS);
      recipes.addIC2SmeltingRecipe(
         "rubberwood_charcoal", new ItemStack(Items.CHARCOAL), IC2Blocks.RUBBERWOOD_LOG, IAdvancedCraftingManager.SmeltingType.FURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "rubberwood_bark_charcoal", new ItemStack(Items.CHARCOAL), IC2Blocks.RUBBERWOOD_LOG_BARKED, IAdvancedCraftingManager.SmeltingType.FURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "rubberwood_bark_stripped_charcoal",
         new ItemStack(Items.CHARCOAL),
         IC2Blocks.RUBBER_LOG_BARKED_STRIPPED,
         IAdvancedCraftingManager.SmeltingType.FURNACE
      );
      recipes.addIC2SmeltingRecipe(
         "rubberwood_stripped_charcoal", new ItemStack(Items.CHARCOAL), IC2Blocks.RUBBER_LOG_STRIPPED, IAdvancedCraftingManager.SmeltingType.FURNACE
      );
   }

   private static void addCables(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe("tin_cable", new ItemStack(IC2Items.TIN_CABLE, 9), "XXX", 'X', IC2Tags.INGOT_TIN);
      recipes.addShapedIC2Recipe("copper_cable", new ItemStack(IC2Items.COPPER_CABLE, 6), "XXX", 'X', IC2Tags.INGOT_COPPER);
      recipes.addShapedIC2Recipe(
         "insulated_copper_cable_0", new ItemStack(IC2Items.COPPER_CABLE_1X_INSULATED, 6), "YYY", "XXX", "YYY", 'X', IC2Tags.INGOT_COPPER, 'Y', IC2Tags.RUBBER
      );
      recipes.addShapedIC2Recipe(
         "insulated_copper_cable_1", new ItemStack(IC2Items.COPPER_CABLE_1X_INSULATED, 6), "YXY", "YXY", "YXY", 'X', IC2Tags.INGOT_COPPER, 'Y', IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe("insulated_copper_cable_2", new ItemStack(IC2Items.COPPER_CABLE_1X_INSULATED), IC2Items.COPPER_CABLE, IC2Tags.RUBBER);
      recipes.addShapedIC2Recipe("bronze_cable", new ItemStack(IC2Items.BRONZE_CABLE, 6), "XXX", 'X', IC2Tags.INGOT_BRONZE);
      recipes.addShapedIC2Recipe(
         "insulated_bronze_cable_0", new ItemStack(IC2Items.BRONZE_CABLE_1X_INSULATED, 6), "YYY", "XXX", "YYY", 'X', IC2Tags.INGOT_BRONZE, 'Y', IC2Tags.RUBBER
      );
      recipes.addShapedIC2Recipe(
         "insulated_bronze_cable_1", new ItemStack(IC2Items.BRONZE_CABLE_1X_INSULATED, 6), "YXY", "YXY", "YXY", 'X', IC2Tags.INGOT_BRONZE, 'Y', IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe("insulated_bronze_cable_2", new ItemStack(IC2Items.BRONZE_CABLE_1X_INSULATED), IC2Items.BRONZE_CABLE, IC2Tags.RUBBER);
      recipes.addShapedIC2Recipe(
         "2x_insulated_bronze_cable_0", new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED, 2), "YXY", 'X', IC2Tags.INGOT_BRONZE, 'Y', IC2Tags.RUBBER
      );
      recipes.addShapedIC2Recipe(
         "2x_insulated_bronze_cable_1", new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED, 2), "Y", "X", "Y", 'X', IC2Tags.INGOT_BRONZE, 'Y', IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "2x_insulated_bronze_cable_2", new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED), IC2Items.BRONZE_CABLE, IC2Tags.RUBBER, IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "2x_insulated_bronze_cable_3", new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED), IC2Items.BRONZE_CABLE_1X_INSULATED, IC2Tags.RUBBER
      );
      recipes.addShapedIC2Recipe("gold_cable", new ItemStack(IC2Items.GOLD_CABLE, 12), "XXX", 'X', net.minecraftforge.common.Tags.Items.INGOTS_GOLD);
      recipes.addShapedIC2Recipe(
         "insulated_gold_cable_0",
         new ItemStack(IC2Items.GOLD_CABLE_1X_INSULATED, 4),
         " Y ",
         "YXY",
         " Y ",
         'X',
         net.minecraftforge.common.Tags.Items.INGOTS_GOLD,
         'Y',
         IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe("insulated_gold_cable_1", new ItemStack(IC2Items.GOLD_CABLE_1X_INSULATED), IC2Items.GOLD_CABLE, IC2Tags.RUBBER);
      recipes.addShapelessIC2Recipe(
         "2x_insulated_gold_cable_0", new ItemStack(IC2Items.GOLD_CABLE_2X_INSULATED), IC2Items.GOLD_CABLE, IC2Items.RUBBER, IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "2x_insulated_gold_cable_1", new ItemStack(IC2Items.GOLD_CABLE_2X_INSULATED), IC2Items.GOLD_CABLE_1X_INSULATED, IC2Tags.RUBBER
      );
      recipes.addShapedIC2Recipe(
         "2x_insulated_gold_cable_2",
         new ItemStack(IC2Items.GOLD_CABLE_2X_INSULATED, 4),
         "YYY",
         "YXY",
         "YYY",
         'X',
         net.minecraftforge.common.Tags.Items.INGOTS_GOLD,
         'Y',
         IC2Tags.RUBBER
      );
      recipes.addShapedIC2Recipe("iron_cable", new ItemStack(IC2Items.IRON_CABLE, 12), "XXX", 'X', IC2Tags.INGOT_REFINED_IRON);
      recipes.addShapedIC2Recipe(
         "insulated_iron_cable_0",
         new ItemStack(IC2Items.IRON_CABLE_1X_INSULATED, 4),
         " Y ",
         "YXY",
         " Y ",
         'X',
         IC2Tags.INGOT_REFINED_IRON,
         'Y',
         IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe("insulated_iron_cable_1", new ItemStack(IC2Items.IRON_CABLE_1X_INSULATED), IC2Items.IRON_CABLE, IC2Tags.RUBBER);
      recipes.addShapedIC2Recipe(
         "2x_insulated_iron_cable_0",
         new ItemStack(IC2Items.IRON_CABLE_2X_INSULATED, 4),
         "YYY",
         "YXY",
         "YYY",
         'X',
         IC2Tags.INGOT_REFINED_IRON,
         'Y',
         IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "2x_insulated_iron_cable_1", new ItemStack(IC2Items.IRON_CABLE_2X_INSULATED), IC2Items.IRON_CABLE, IC2Tags.RUBBER, IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "2x_insulated_iron_cable_2", new ItemStack(IC2Items.IRON_CABLE_2X_INSULATED), IC2Items.IRON_CABLE_1X_INSULATED, IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "4x_insulated_iron_cable_0", new ItemStack(IC2Items.IRON_CABLE_4X_INSULATED), IC2Items.IRON_CABLE, IC2Tags.RUBBER, IC2Tags.RUBBER, IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "4x_insulated_iron_cable_1", new ItemStack(IC2Items.IRON_CABLE_4X_INSULATED), IC2Items.IRON_CABLE_1X_INSULATED, IC2Tags.RUBBER, IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "4x_insulated_iron_cable_2", new ItemStack(IC2Items.IRON_CABLE_4X_INSULATED), IC2Items.IRON_CABLE_2X_INSULATED, IC2Tags.RUBBER
      );
      recipes.addShapedIC2Recipe("aluminum_cable", new ItemStack(IC2Items.ALUMINIUM_CABLE, 12), "XXX", 'X', IC2Tags.INGOT_ALUMINIUM);
      recipes.addShapedIC2Recipe(
         "insulated_aluminum_cable",
         new ItemStack(IC2Items.ALUMINIUM_CABLE_1X_INSULATED, 4),
         " Y ",
         "YXY",
         " Y ",
         'X',
         IC2Tags.INGOT_ALUMINIUM,
         'Y',
         IC2Tags.RUBBER
      );
      recipes.addShapedIC2Recipe(
         "2x_insulated_aluminum_cable",
         new ItemStack(IC2Items.ALUMINIUM_CABLE_2X_INSULATED, 4),
         "YYY",
         "YXY",
         "YYY",
         'X',
         IC2Tags.INGOT_ALUMINIUM,
         'Y',
         IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "8x_insulated_iron_cable_4x",
         new ItemStack(IC2Items.ALUMINIUM_CABLE_8X_INSULATED),
         IC2Items.ALUMINIUM_CABLE,
         IC2Tags.RUBBER,
         IC2Tags.RUBBER,
         IC2Tags.RUBBER,
         IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "8x_insulated_iron_cable_3x",
         new ItemStack(IC2Items.ALUMINIUM_CABLE_8X_INSULATED),
         IC2Items.ALUMINIUM_CABLE_1X_INSULATED,
         IC2Tags.RUBBER,
         IC2Tags.RUBBER,
         IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "4x_insulated_iron_cable_3x",
         new ItemStack(IC2Items.ALUMINIUM_CABLE_4X_INSULATED),
         IC2Items.ALUMINIUM_CABLE,
         IC2Tags.RUBBER,
         IC2Tags.RUBBER,
         IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "8x_insulated_iron_cable_2x",
         new ItemStack(IC2Items.ALUMINIUM_CABLE_8X_INSULATED),
         IC2Items.ALUMINIUM_CABLE_2X_INSULATED,
         IC2Tags.RUBBER,
         IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "4x_insulated_iron_cable_2x",
         new ItemStack(IC2Items.ALUMINIUM_CABLE_4X_INSULATED),
         IC2Items.ALUMINIUM_CABLE_1X_INSULATED,
         IC2Tags.RUBBER,
         IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "2x_insulated_iron_cable_2x", new ItemStack(IC2Items.ALUMINIUM_CABLE_2X_INSULATED), IC2Items.ALUMINIUM_CABLE, IC2Tags.RUBBER, IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "8x_insulated_iron_cable_1x", new ItemStack(IC2Items.ALUMINIUM_CABLE_8X_INSULATED), IC2Items.ALUMINIUM_CABLE_4X_INSULATED, IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "4x_insulated_iron_cable_1x", new ItemStack(IC2Items.ALUMINIUM_CABLE_4X_INSULATED), IC2Items.ALUMINIUM_CABLE_2X_INSULATED, IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "2x_insulated_iron_cable_1x", new ItemStack(IC2Items.ALUMINIUM_CABLE_2X_INSULATED), IC2Items.ALUMINIUM_CABLE_1X_INSULATED, IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe(
         "1x_insulated_iron_cable_1x", new ItemStack(IC2Items.ALUMINIUM_CABLE_1X_INSULATED), IC2Items.ALUMINIUM_CABLE, IC2Tags.RUBBER
      );
      recipes.addShapedIC2Recipe(
         "glass_fiber_0",
         new ItemStack(IC2Items.GLASSFIBER_CABLE, 4),
         "XXX",
         "CVC",
         "XXX",
         'X',
         net.minecraftforge.common.Tags.Items.GLASS,
         'C',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'V',
         net.minecraftforge.common.Tags.Items.GEMS_DIAMOND
      );
      recipes.addShapedIC2Recipe(
         "glass_fiber_1",
         new ItemStack(IC2Items.GLASSFIBER_CABLE, 6),
         "XXX",
         "CVC",
         "XXX",
         'X',
         net.minecraftforge.common.Tags.Items.GLASS,
         'C',
         IC2Tags.INGOT_SILVER,
         'V',
         net.minecraftforge.common.Tags.Items.GEMS_DIAMOND
      );
      recipes.addShapedIC2Recipe(
         "detector_cable",
         new ItemStack(IC2Items.DETECTOR_CABLE),
         " C ",
         "RIR",
         " R ",
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'I',
         IC2Items.IRON_CABLE_4X_INSULATED,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "splitter_cable",
         new ItemStack(IC2Items.SPLITTER_CABLE),
         " R ",
         "ILI",
         " R ",
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'I',
         IC2Items.IRON_CABLE_4X_INSULATED,
         'L',
         Items.LEVER
      );
      recipes.addShapedIC2Recipe(
         "comparing_cable",
         new ItemStack(IC2Items.COMPARING_CABLE),
         " C ",
         "RIR",
         " R ",
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'I',
         IC2Items.IRON_CABLE_4X_INSULATED,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "uncompressed_super_cable",
         new ItemStack(IC2Items.UNCOMPRESSED_SUPER_CABLE),
         " X ",
         "YCY",
         " X ",
         'X',
         IC2Items.CARBON_PLATE,
         'C',
         IC2Items.INGOT_POLISHED_GOLD,
         'Y',
         IC2Items.PULSATING_QUARTZ
      );
      recipes.addShapedIC2Recipe(
         "plasma_cable",
         new ItemStack(IC2Items.PLASMA_CABLE, 4),
         "XXX",
         "YCY",
         "XXX",
         'X',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Tags.INGOT_REFINED_IRON,
         'C',
         IC2Items.PLASMA_CORE
      );
   }

   private static void addTransport(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "simple_tube", new ItemStack(IC2Blocks.SIMPLE_TUBE, 8), "XYX", 'Y', net.minecraftforge.common.Tags.Items.GLASS, 'X', IC2Tags.INGOT_TIN
      );
      recipes.addShapedIC2Recipe(
         "extraction_tube",
         new ItemStack(IC2Blocks.EXTRACTOR_TUBE, 8),
         " X ",
         "VYV",
         " C ",
         'X',
         Items.STICKY_PISTON,
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'V',
         IC2Tags.INGOT_TIN,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "extraction_tube_upgrade",
         new ItemStack(IC2Blocks.EXTRACTOR_TUBE, 2),
         "XYC",
         'X',
         Items.STICKY_PISTON,
         'Y',
         new ItemStack(IC2Blocks.SIMPLE_TUBE, 2),
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "filtered_extraction_tube",
         new ItemStack(IC2Blocks.FILTERED_EXTRACTION_TUBE, 8),
         " X ",
         "VYV",
         " C ",
         'X',
         Items.STICKY_PISTON,
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'V',
         IC2Tags.INGOT_TIN,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "filtered_extraction_tube_upgrade",
         new ItemStack(IC2Blocks.FILTERED_EXTRACTION_TUBE, 2),
         "XYC",
         'X',
         Items.STICKY_PISTON,
         'Y',
         new ItemStack(IC2Blocks.SIMPLE_TUBE, 2),
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "directional_tube",
         new ItemStack(IC2Blocks.DIRECTIONAL_TUBE, 8),
         " C ",
         "XYX",
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN,
         'C',
         net.minecraftforge.common.Tags.Items.INGOTS_IRON
      );
      recipes.addShapedIC2Recipe(
         "directional_tube_refined",
         new ItemStack(IC2Blocks.DIRECTIONAL_TUBE, 8),
         " C ",
         "XYX",
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN,
         'C',
         IC2Items.INGOT_REFINED_IRON
      );
      recipes.addShapedIC2Recipe(
         "filter_tube",
         new ItemStack(IC2Blocks.FILTER_TUBE, 8),
         " C ",
         "XYX",
         " C ",
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapelessIC2Recipe("filter_tube_upgrade", new ItemStack(IC2Blocks.FILTER_TUBE, 4), new ItemStack(IC2Blocks.SIMPLE_TUBE, 4), IC2Items.CIRCUIT);
      recipes.addShapelessIC2Recipe(
         "color_filter_tube", new ItemStack(IC2Blocks.COLOR_FILTER_TUBE), net.minecraftforge.common.Tags.Items.DYES, IC2Blocks.FILTER_TUBE
      );
      recipes.addShapedIC2Recipe(
         "pickup_tube",
         new ItemStack(IC2Blocks.PICKUP_TUBE, 8),
         " C ",
         "XYX",
         " C ",
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN,
         'C',
         net.minecraftforge.common.Tags.Items.OBSIDIAN
      );
      recipes.addShapedIC2Recipe(
         "insertion_tube",
         new ItemStack(IC2Blocks.INSERTION_TUBE, 8),
         " X ",
         "YVY",
         " C ",
         'X',
         Items.PISTON,
         'Y',
         IC2Tags.INGOT_TIN,
         'V',
         net.minecraftforge.common.Tags.Items.GLASS,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "insertion_tube_upgrade",
         new ItemStack(IC2Blocks.INSERTION_TUBE, 2),
         "XYC",
         'X',
         Items.PISTON,
         'Y',
         new ItemStack(IC2Blocks.SIMPLE_TUBE, 2),
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "redstone_tube",
         new ItemStack(IC2Blocks.REDSTONE_TUBE, 8),
         " C ",
         "XYX",
         " C ",
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN,
         'C',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE
      );
      recipes.addShapedIC2Recipe(
         "switch_tube",
         new ItemStack(IC2Blocks.SWITCH_TUBE, 8),
         " C ",
         "XYX",
         " C ",
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN,
         'C',
         Items.LEVER
      );
      recipes.addShapedIC2Recipe(
         "round_robin",
         new ItemStack(IC2Blocks.ROUND_ROBIN_TUBE, 8),
         " C ",
         "XYX",
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN,
         'C',
         IC2Items.TURBINE_BLADE
      );
      recipes.addShapedIC2Recipe(
         "speed_tube",
         new ItemStack(IC2Blocks.SPEED_TUBE, 8),
         " G ",
         "XYX",
         " G ",
         'G',
         net.minecraftforge.common.Tags.Items.INGOTS_GOLD,
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN
      );
      recipes.addShapedIC2Recipe(
         "unconnecting_tube",
         new ItemStack(IC2Blocks.TRANSPORT_TUBE, 8),
         " G ",
         "XYX",
         " G ",
         'G',
         net.minecraftforge.common.Tags.Items.GRAVEL,
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN
      );
      recipes.addShapedIC2Recipe(
         "teleport_tube",
         new ItemStack(IC2Blocks.TELEPORT_TUBE, 2),
         " E ",
         "XDX",
         " E ",
         'E',
         Items.ENDER_PEARL,
         'X',
         IC2Blocks.SIMPLE_TUBE,
         'D',
         net.minecraftforge.common.Tags.Items.GEMS_DIAMOND
      );
      recipes.addShapedIC2Recipe(
         "dropping_tube",
         new ItemStack(IC2Blocks.DROPPING_TUBE, 4),
         " C ",
         "XYX",
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN,
         'C',
         Blocks.DROPPER
      );
      recipes.addShapedIC2Recipe(
         "void_tube",
         new ItemStack(IC2Blocks.VOID_TUBE, 8),
         " L ",
         "XYX",
         " O ",
         'L',
         Items.LAVA_BUCKET,
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN,
         'O',
         Blocks.OBSIDIAN
      );
      recipes.addShapedIC2Recipe(
         "stacking_tube", new ItemStack(IC2Blocks.STACKING_TUBE), "CXR", 'C', Blocks.CHEST, 'X', IC2Blocks.SIMPLE_TUBE, 'R', Items.REDSTONE
      );
      recipes.addShapedIC2Recipe(
         "hover_tube",
         new ItemStack(IC2Blocks.HOVER_TUBE, 4),
         " X ",
         "YCY",
         " X ",
         'X',
         net.minecraftforge.common.Tags.Items.GEMS_DIAMOND,
         'Y',
         IC2Tags.INGOT_TIN,
         'C',
         net.minecraftforge.common.Tags.Items.GLASS
      );
      recipes.addShapelessIC2Recipe(
         "request_tube", new ItemStack(IC2Blocks.REQUEST_TUBE, 8), new ItemStack(IC2Blocks.INSERTION_TUBE, 8), IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapelessIC2Recipe(
         "provider_tube", new ItemStack(IC2Blocks.PROVIDER_TUBE, 8), new ItemStack(IC2Blocks.EXTRACTOR_TUBE, 8), IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "dyeing_tube",
         new ItemStack(IC2Blocks.DYEING_TUBE, 4),
         " C ",
         "XYX",
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN,
         'C',
         IC2Items.PAINTER
      );
      recipes.addShapelessIC2Recipe("dyeing_tube_upgrade", new ItemStack(IC2Blocks.DYEING_TUBE, 4), new ItemStack(IC2Blocks.SIMPLE_TUBE, 4), IC2Items.PAINTER);
      recipes.addShapedIC2Recipe(
         "sticky_tube",
         new ItemStack(IC2Blocks.STICKY_TUBE, 8),
         "XYC",
         'X',
         new ItemStack(IC2Blocks.PICKUP_TUBE, 4),
         'Y',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         new ItemStack(IC2Blocks.EXTRACTOR_TUBE, 4)
      );
      recipes.addShapedIC2Recipe(
         "fluid_tube",
         new ItemStack(IC2Blocks.FLUID_TUBE, 8),
         " C ",
         "XYX",
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         IC2Tags.INGOT_TIN,
         'C',
         IC2Items.CELL_EMPTY
      );
      recipes.addShapelessIC2Recipe("fluid_tube_upgrade", new ItemStack(IC2Blocks.FLUID_TUBE), IC2Blocks.PIPE, IC2Blocks.SIMPLE_TUBE);
      recipes.addShapedIC2Recipe(
         "limiter_tube",
         new ItemStack(IC2Blocks.LIMITER_TUBE, 8),
         "XYC",
         'X',
         new ItemStack(IC2Blocks.REDSTONE_TUBE, 4),
         'Y',
         IC2Items.COMPLEX_CIRCUIT,
         'C',
         new ItemStack(IC2Blocks.SWITCH_TUBE, 4)
      );
      recipes.addShapedIC2Recipe(
         "pipe", new ItemStack(IC2Blocks.PIPE, 8), " C ", "XYX", 'Y', net.minecraftforge.common.Tags.Items.GLASS, 'X', IC2Tags.INGOT_TIN, 'C', Items.BUCKET
      );
      recipes.addShapelessIC2Recipe("pipe_upgrade", new ItemStack(IC2Blocks.PIPE, 8), new ItemStack(IC2Blocks.SIMPLE_TUBE, 8), IC2Items.CELL_EMPTY);
      recipes.addShapedIC2Recipe(
         "void_pipe",
         new ItemStack(IC2Blocks.VOID_PIPE, 2),
         " X ",
         "YCY",
         " V ",
         'X',
         Items.LAVA_BUCKET,
         'Y',
         IC2Tags.INGOT_TIN,
         'V',
         Items.OBSIDIAN,
         'C',
         new ItemStack(IC2Blocks.PIPE, 2)
      );
      recipes.addShapedIC2Recipe(
         "detector_pipe",
         new ItemStack(IC2Blocks.DETECTOR_PIPE),
         " C ",
         "RIR",
         " R ",
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'I',
         IC2Blocks.PIPE,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "splitter_pipe",
         new ItemStack(IC2Blocks.SPLITTER_PIPE),
         " R ",
         "ILI",
         " R ",
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'I',
         IC2Blocks.PIPE,
         'L',
         Items.LEVER
      );
      recipes.addShapedIC2Recipe(
         "capacity_pipe", new ItemStack(IC2Blocks.HIGH_CAPACITY_PIPE), " C ", "XYX", 'X', Items.BUCKET, 'Y', IC2Blocks.PIPE, 'C', IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "pressure_pipe",
         new ItemStack(IC2Blocks.HIGH_PRESSURE_PIPE),
         " C ",
         "XYV",
         'X',
         IC2Items.FLUID_IMPORT_UPGRADE_SIMPLE,
         'V',
         IC2Items.FLUID_EXPORT_UPGRADE_SIMPLE,
         'Y',
         IC2Blocks.PIPE,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "electric_pipe_pump",
         new ItemStack(IC2Blocks.ELECTRIC_PIPE_PUMP),
         "XCX",
         "YVY",
         "XBX",
         'X',
         IC2Items.INGOT_REFINED_IRON,
         'Y',
         IC2Items.RE_BATTERY,
         'C',
         IC2Items.FLUID_EXPORT_UPGRADE,
         'V',
         IC2Blocks.HIGH_PRESSURE_PIPE,
         'B',
         IC2Items.FLUID_IMPORT_UPGRADE
      );
   }

   private static void addUpgrades(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "upgrade_base_0",
         new ItemStack(IC2Items.UPGRADE_BASE, 3),
         "X X",
         "YCY",
         "XVX",
         'X',
         IC2Tags.INGOT_COPPER,
         'Y',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'C',
         IC2Items.CIRCUIT,
         'V',
         IC2Blocks.MINING_PIPE_SHAFT
      );
      recipes.addShapedIC2Recipe(
         "upgrade_base_1",
         new ItemStack(IC2Items.UPGRADE_BASE, 5),
         "X X",
         "YCY",
         "XVX",
         'X',
         IC2Tags.INGOT_SILVER,
         'Y',
         IC2Items.BRONZE_CABLE_2X_INSULATED,
         'C',
         IC2Items.CIRCUIT,
         'V',
         IC2Blocks.MINING_PIPE_SHAFT
      );
      recipes.addShapedIC2Recipe(
         "overclocker_upgrade_0",
         new ItemStack(IC2Items.OVERCLOCKER_UPGRADE),
         "CCC",
         "WEW",
         'C',
         IC2Items.COOLANT_CELL_10K,
         'W',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'E',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "overclocker_upgrade_1",
         new ItemStack(IC2Items.OVERCLOCKER_UPGRADE, 3),
         "CCC",
         "WEW",
         'C',
         IC2Items.COOLANT_CELL_30K,
         'W',
         new ItemStack(IC2Items.COPPER_CABLE_1X_INSULATED, 3),
         'E',
         new ItemStack(IC2Items.CIRCUIT, 3)
      );
      recipes.addShapedIC2Recipe(
         "overclocker_upgrade_2",
         new ItemStack(IC2Items.OVERCLOCKER_UPGRADE, 6),
         "CCC",
         "WEW",
         'C',
         IC2Items.COOLANT_CELL_60K,
         'W',
         new ItemStack(IC2Items.COPPER_CABLE_1X_INSULATED, 6),
         'E',
         new ItemStack(IC2Items.CIRCUIT, 6)
      );
      recipes.addShapedIC2Recipe(
         "overclocker_upgrade_3", new ItemStack(IC2Items.OVERCLOCKER_UPGRADE), " X ", "XYX", " X ", 'Y', IC2Items.UPGRADE_BASE, 'X', IC2Items.COOLANT_CELL_10K
      );
      recipes.addShapedIC2Recipe(
         "overclocker_upgrade_4",
         new ItemStack(IC2Items.OVERCLOCKER_UPGRADE, 2),
         "XXX",
         "XYX",
         "XXX",
         'Y',
         new ItemStack(IC2Items.UPGRADE_BASE, 2),
         'X',
         IC2Items.COOLANT_CELL_10K
      );
      recipes.addShapedIC2Recipe(
         "overclocker_upgrade_5",
         new ItemStack(IC2Items.OVERCLOCKER_UPGRADE, 4),
         " X ",
         "XYX",
         " X ",
         'Y',
         new ItemStack(IC2Items.UPGRADE_BASE, 4),
         'X',
         IC2Items.COOLANT_CELL_30K
      );
      recipes.addShapedIC2Recipe(
         "overclocker_upgrade_6",
         new ItemStack(IC2Items.OVERCLOCKER_UPGRADE, 8),
         "XXX",
         "XYX",
         "XXX",
         'Y',
         new ItemStack(IC2Items.UPGRADE_BASE, 8),
         'X',
         IC2Items.COOLANT_CELL_30K
      );
      recipes.addShapedIC2Recipe(
         "overclocker_upgrade_7",
         new ItemStack(IC2Items.OVERCLOCKER_UPGRADE, 8),
         " X ",
         "XYX",
         " X ",
         'Y',
         new ItemStack(IC2Items.UPGRADE_BASE, 8),
         'X',
         IC2Items.COOLANT_CELL_60K
      );
      recipes.addShapedIC2Recipe(
         "overclocker_upgrade_8",
         new ItemStack(IC2Items.OVERCLOCKER_UPGRADE, 16),
         "XXX",
         "XYX",
         "XXX",
         'Y',
         new ItemStack(IC2Items.UPGRADE_BASE, 16),
         'X',
         IC2Items.COOLANT_CELL_60K
      );
      recipes.addShapedIC2Recipe(
         "quantum_overclocker_upgrade",
         new ItemStack(IC2Items.QUANTUM_OVERCLOCKER_UPGRADE),
         "XYX",
         "CVC",
         "XYX",
         'X',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Items.PLASMA_CORE,
         'V',
         IC2Items.OVERCLOCKER_UPGRADE
      );
      recipes.addShapedIC2Recipe(
         "transformer_upgrade_0",
         new ItemStack(IC2Items.TRANSFORMER_UPGRADE),
         "GGG",
         "WTW",
         "GEG",
         'G',
         net.minecraftforge.common.Tags.Items.GLASS,
         'W',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'T',
         IC2Blocks.TRANSFORMER_MV,
         'E',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "transformer_upgrade_1",
         new ItemStack(IC2Items.TRANSFORMER_UPGRADE),
         "GGG",
         "GTG",
         "GEG",
         'G',
         net.minecraftforge.common.Tags.Items.GLASS,
         'T',
         IC2Blocks.TRANSFORMER_MV,
         'E',
         IC2Items.UPGRADE_BASE
      );
      recipes.addShapedIC2Recipe(
         "energy_storage_upgrade_0",
         new ItemStack(IC2Items.ENERGY_STORAGE_UPGRADE),
         "www",
         "WBW",
         "wEw",
         'w',
         ItemTags.PLANKS,
         'W',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'B',
         IC2Items.RE_BATTERY,
         'E',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "energy_storage_upgrade_1",
         new ItemStack(IC2Items.ENERGY_STORAGE_UPGRADE),
         "www",
         "wBw",
         "wEw",
         'w',
         ItemTags.PLANKS,
         'B',
         IC2Items.RE_BATTERY,
         'E',
         IC2Items.UPGRADE_BASE
      );
      recipes.addShapedIC2Recipe(
         "redstone_sensitivity_upgrade_0",
         new ItemStack(IC2Items.REDSTONE_SENSITIVITY_UPGRADE),
         "XAX",
         "YCY",
         "XVX",
         'X',
         IC2Tags.INGOT_BRONZE,
         'Y',
         IC2Tags.RUBBER,
         'A',
         Items.REPEATER,
         'C',
         Items.REDSTONE_TORCH,
         'V',
         Items.COMPARATOR
      );
      recipes.addShapedIC2Recipe(
         "redstone_sensitivity_upgrade_1",
         new ItemStack(IC2Items.REDSTONE_SENSITIVITY_UPGRADE),
         "YCY",
         "ADV",
         'D',
         IC2Items.UPGRADE_BASE,
         'Y',
         IC2Tags.RUBBER,
         'A',
         Items.REPEATER,
         'C',
         Items.REDSTONE_TORCH,
         'V',
         Items.COMPARATOR
      );
      recipes.addShapedIC2Recipe(
         "redstone_inverter_upgrade_0",
         new ItemStack(IC2Items.REDSTONE_INVERTER_UPGRADE),
         "X X",
         " C ",
         "X X",
         'X',
         IC2Tags.INGOT_TIN,
         'C',
         Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "redstone_inverter_upgrade_1", new ItemStack(IC2Items.REDSTONE_INVERTER_UPGRADE), "X", "C", 'X', IC2Items.UPGRADE_BASE, 'C', Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "simple_import_upgrade_0",
         new ItemStack(IC2Items.IMPORT_UPGRADE_SIMPLE),
         "XCX",
         "YVY",
         "XBX",
         'X',
         IC2Tags.INGOT_COPPER,
         'Y',
         IC2Items.CIRCUIT,
         'C',
         Items.STICKY_PISTON,
         'V',
         IC2Blocks.MINING_PIPE_SHAFT,
         'B',
         Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "simple_import_upgrade_1",
         new ItemStack(IC2Items.IMPORT_UPGRADE_SIMPLE),
         "X",
         "Y",
         "V",
         'X',
         Items.STICKY_PISTON,
         'Y',
         IC2Items.UPGRADE_BASE,
         'V',
         Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "simple_export_upgrade_0",
         new ItemStack(IC2Items.EXPORT_UPGRADE_SIMPLE),
         "XCX",
         "YVY",
         "XBX",
         'X',
         IC2Tags.INGOT_COPPER,
         'Y',
         IC2Items.CIRCUIT,
         'C',
         Items.PISTON,
         'V',
         IC2Blocks.MINING_PIPE_SHAFT,
         'B',
         Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "simple_export_upgrade_1",
         new ItemStack(IC2Items.EXPORT_UPGRADE_SIMPLE),
         "X",
         "Y",
         "V",
         'X',
         Items.PISTON,
         'Y',
         IC2Items.UPGRADE_BASE,
         'V',
         Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "import_upgrade_0",
         new ItemStack(IC2Items.IMPORT_UPGRADE),
         "BCB",
         " A ",
         'C',
         IC2Items.IMPORT_UPGRADE_SIMPLE,
         'B',
         IC2Items.ADVANCED_CIRCUIT,
         'A',
         IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "export_upgrade_0",
         new ItemStack(IC2Items.EXPORT_UPGRADE),
         "BCB",
         " A ",
         'C',
         IC2Items.EXPORT_UPGRADE_SIMPLE,
         'B',
         IC2Items.ADVANCED_CIRCUIT,
         'A',
         IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "muffler_upgrade_0", new ItemStack(IC2Items.MUFFLER_UPGRADE), "XXX", "XYX", "XXX", 'X', ItemTags.WOOL, 'Y', IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "muffler_upgrade_1", new ItemStack(IC2Items.MUFFLER_UPGRADE), "XXX", "XYX", "XXX", 'X', ItemTags.WOOL, 'Y', IC2Items.UPGRADE_BASE
      );
      recipes.addShapedIC2Recipe(
         "mute_upgrade", new ItemStack(IC2Items.MUTE_UPGRADE), "XYX", "YCY", "XYX", 'X', ItemTags.WOOL, 'C', Items.STRING, 'Y', IC2Items.MUFFLER_UPGRADE
      );
      recipes.addShapedIC2Recipe(
         "efficiency_upgrade",
         new ItemStack(IC2Items.EFFICIENCY_UPGRADE, 2),
         " X ",
         "CVC",
         'X',
         IC2Items.GLASSFIBER_CABLE,
         'C',
         IC2Items.BRONZE_CABLE_2X_INSULATED,
         'V',
         IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "exp_collector_upgrade_0",
         new ItemStack(IC2Items.EXP_COLLECTOR_UPGRADE),
         "XYX",
         "CVC",
         "XBX",
         'X',
         IC2Items.PLATE_DENSE_COPPER,
         'Y',
         IC2Blocks.MACHINE_BLOCK,
         'C',
         IC2Items.ADVANCED_CIRCUIT,
         'V',
         Items.HOPPER,
         'B',
         Items.GLASS_BOTTLE
      );
      recipes.addShapedIC2Recipe(
         "exp_collector_upgrade_1",
         new ItemStack(IC2Items.EXP_COLLECTOR_UPGRADE),
         " Y ",
         "CVC",
         " B ",
         'Y',
         IC2Items.IMPORT_UPGRADE_SIMPLE,
         'C',
         IC2Items.ADVANCED_CIRCUIT,
         'V',
         IC2Items.UPGRADE_BASE,
         'B',
         Items.GLASS_BOTTLE
      );
      recipes.addShapedIC2Recipe(
         "energy_storage_multiplier_upgrade",
         new ItemStack(IC2Items.ENERGY_STORAGE_MULTIPLIER_UPGRADE),
         "XYX",
         "YCY",
         "XYX",
         'Y',
         IC2Items.ENERGY_STORAGE_UPGRADE,
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "loudness_upgrade", new ItemStack(IC2Items.LOUDNESS_UPGRADE), "X", "Y", 'X', IC2Items.MUFFLER_UPGRADE, 'Y', Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "slowness_upgrade", new ItemStack(IC2Items.SLOWNESS_UPGRADE), "X", "Y", 'X', IC2Items.OVERCLOCKER_UPGRADE, 'Y', Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "upgrade_container",
         new ItemStack(IC2Items.UPGRADE_CONTAINER),
         "XYX",
         "YCY",
         "XYX",
         'X',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Items.PLATE_IRIDIUM,
         'C',
         net.minecraftforge.common.Tags.Items.CHESTS
      );
      recipes.addShapedIC2Recipe(
         "simple_fluid_import_upgrade_0",
         new ItemStack(IC2Items.FLUID_IMPORT_UPGRADE_SIMPLE),
         "XCX",
         "YVY",
         "XBX",
         'X',
         IC2Tags.INGOT_COPPER,
         'Y',
         IC2Items.CIRCUIT,
         'C',
         Items.STICKY_PISTON,
         'V',
         Items.BUCKET,
         'B',
         Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "simple_fluid_import_upgrade_1",
         new ItemStack(IC2Items.FLUID_IMPORT_UPGRADE_SIMPLE),
         " X ",
         "CYC",
         " V ",
         'C',
         Items.BUCKET,
         'X',
         Items.STICKY_PISTON,
         'Y',
         IC2Items.UPGRADE_BASE,
         'V',
         Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "simple_fluid_export_upgrade_0",
         new ItemStack(IC2Items.FLUID_EXPORT_UPGRADE_SIMPLE),
         "XCX",
         "YVY",
         "XBX",
         'X',
         IC2Tags.INGOT_COPPER,
         'Y',
         IC2Items.CIRCUIT,
         'C',
         Items.PISTON,
         'V',
         Items.BUCKET,
         'B',
         Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "simple_fluid_export_upgrade_1",
         new ItemStack(IC2Items.FLUID_EXPORT_UPGRADE_SIMPLE),
         " X ",
         "CYC",
         " V ",
         'C',
         Items.BUCKET,
         'X',
         Items.PISTON,
         'Y',
         IC2Items.UPGRADE_BASE,
         'V',
         Items.REDSTONE_TORCH
      );
      recipes.addShapedIC2Recipe(
         "fluid_import_upgrade",
         new ItemStack(IC2Items.FLUID_IMPORT_UPGRADE),
         "BCB",
         " A ",
         'C',
         IC2Items.FLUID_IMPORT_UPGRADE_SIMPLE,
         'B',
         IC2Items.ADVANCED_CIRCUIT,
         'A',
         IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "fluid_export_upgrade",
         new ItemStack(IC2Items.FLUID_EXPORT_UPGRADE),
         "BCB",
         " A ",
         'C',
         IC2Items.FLUID_EXPORT_UPGRADE_SIMPLE,
         'B',
         IC2Items.ADVANCED_CIRCUIT,
         'A',
         IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "cobblegen_upgrade_0",
         new ItemStack(IC2Items.COBBLEGEN_UPGRADE),
         "XYX",
         "C V",
         "XBX",
         'X',
         net.minecraftforge.common.Tags.Items.GLASS,
         'Y',
         Items.GOLDEN_PICKAXE,
         'V',
         Fluids.WATER,
         'B',
         IC2Items.UPGRADE_BASE,
         'C',
         Items.MAGMA_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "cobblegen_upgrade_1",
         new ItemStack(IC2Items.COBBLEGEN_UPGRADE),
         "XYX",
         "C V",
         "XBX",
         'X',
         net.minecraftforge.common.Tags.Items.GLASS,
         'Y',
         Items.GOLDEN_PICKAXE,
         'V',
         Fluids.WATER,
         'B',
         IC2Items.UPGRADE_BASE,
         'C',
         Fluids.LAVA
      );
      recipes.addShapedIC2Recipe(
         "crafting_upgrade",
         new ItemStack(IC2Items.CRAFTING_UPGRADE),
         " X ",
         "YCV",
         " B ",
         'X',
         Items.REDSTONE_TORCH,
         'Y',
         IC2Items.CIRCUIT,
         'C',
         Items.CRAFTING_TABLE,
         'V',
         Items.HOPPER,
         'B',
         IC2Items.UPGRADE_BASE
      );
      recipes.addShapelessIC2Recipe("crafting_upgrade_reset", new ItemStack(IC2Items.CRAFTING_UPGRADE), IC2Items.CRAFTING_UPGRADE);
      recipes.addShapedIC2Recipe(
         "efficient_sawblade_upgrade",
         new ItemStack(IC2Items.SAWBLADE_EFFICIENT_UPGRADE),
         "XYX",
         "CVC",
         'X',
         Items.STICK,
         'Y',
         IC2Items.SCRAP_METAL_BLADE,
         'C',
         Items.PISTON,
         'V',
         IC2Items.UPGRADE_BASE
      );
      recipes.addShapelessIC2Recipe(
         "efficient_sawblade_repair", new ItemStack(IC2Items.SAWBLADE_EFFICIENT_UPGRADE), IC2Items.SAWBLADE_EFFICIENT_UPGRADE, IC2Items.SCRAP_METAL_BLADE
      );
      recipes.addShapedIC2Recipe(
         "durable_sawblade_upgrade",
         new ItemStack(IC2Items.SAWBLADE_DURABLE_UPGRADE),
         "XYX",
         "CVC",
         'X',
         Items.STICK,
         'Y',
         IC2Items.OBSIDIAN_BLADE,
         'C',
         Items.PISTON,
         'V',
         IC2Items.UPGRADE_BASE
      );
      recipes.addShapelessIC2Recipe(
         "durable_sawblade_repair", new ItemStack(IC2Items.SAWBLADE_DURABLE_UPGRADE), IC2Items.SAWBLADE_DURABLE_UPGRADE, IC2Items.OBSIDIAN_BLADE
      );
      recipes.addShapedIC2Recipe(
         "item_limiter", new ItemStack(IC2Items.ITEM_LIMITER), "X", "Y", "C", 'X', Items.BOOK, 'Y', IC2Items.UPGRADE_BASE, 'C', IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe("mfe_upgrade_kit_direct", new ItemStack(IC2Items.MFE_UPGRADE_KIT), "X", "Y", 'Y', IC2Blocks.MFE, 'X', IC2Items.WRENCH);
      recipes.addShapedIC2Recipe(
         "mfe_upgrade_kit_indirect_gold",
         new ItemStack(IC2Items.MFE_UPGRADE_KIT),
         "XYX",
         "YCY",
         "XYX",
         'C',
         IC2Items.WRENCH,
         'Y',
         IC2Items.ENERGY_CRYSTAL,
         'X',
         IC2Items.GOLD_CABLE_2X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "mfe_upgrade_kit_indirect_bronze",
         new ItemStack(IC2Items.MFE_UPGRADE_KIT),
         "XYX",
         "YCY",
         "XYX",
         'C',
         IC2Items.WRENCH,
         'Y',
         IC2Items.ENERGY_CRYSTAL,
         'X',
         new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED, 4)
      );
      recipes.addShapedIC2Recipe("mfsu_upgrade_kit_direct", new ItemStack(IC2Items.MFSU_UPGRADE_KIT), "X", "Y", 'Y', IC2Blocks.MFSU, 'X', IC2Items.WRENCH);
      recipes.addShapedIC2Recipe(
         "mfsu_upgrade_kit_indirect",
         new ItemStack(IC2Items.MFSU_UPGRADE_KIT),
         "LCL",
         "LML",
         "LAL",
         'M',
         IC2Items.WRENCH,
         'A',
         IC2Blocks.ADVANCED_MACHINE_BLOCK,
         'C',
         IC2Items.ADVANCED_CIRCUIT,
         'L',
         IC2Items.LAPOTRON_CRYSTAL
      );
      recipes.addShapedIC2Recipe("esu_upgrade_kit_direct", new ItemStack(IC2Items.ESU_UPGRADE_KIT), "X", "Y", 'Y', IC2Blocks.ESU, 'X', IC2Items.ELECTRIC_WRENCH);
      recipes.addShapedIC2Recipe(
         "esu_upgrade_kit_indirect",
         new ItemStack(IC2Items.ESU_UPGRADE_KIT),
         "XVX",
         "YCY",
         "XBX",
         'C',
         IC2Items.ELECTRIC_WRENCH,
         'Y',
         IC2Items.GLOWTRONIC_CRYSTAL,
         'X',
         IC2Items.IRON_CABLE_4X_INSULATED,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.STABILIZED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe("isu_upgrade_kit_direct", new ItemStack(IC2Items.ISU_UPGRADE_KIT), "X", "Y", 'Y', IC2Blocks.ISU, 'X', IC2Items.ELECTRIC_WRENCH);
      recipes.addShapedIC2Recipe(
         "isu_upgrade_kit_indirect",
         new ItemStack(IC2Items.ISU_UPGRADE_KIT),
         "LCL",
         "LML",
         "LAL",
         'M',
         IC2Items.ELECTRIC_WRENCH,
         'A',
         IC2Blocks.STABILIZED_MACHINE_BLOCK,
         'C',
         IC2Items.PLATE_IRIDIUM,
         'L',
         IC2Items.UESC
      );
      recipes.addShapedIC2Recipe(
         "pesu_upgrade_kit_direct", new ItemStack(IC2Items.PESU_UPGRADE_KIT), "X", "Y", 'Y', IC2Blocks.PESU, 'X', IC2Items.ELECTRIC_WRENCH
      );
      recipes.addShapedIC2Recipe(
         "mv_charge_bench_kit_indirect_gold",
         new ItemStack(IC2Items.MV_CHARGE_UPGRADE),
         "XYX",
         "CVC",
         "CBC",
         'Y',
         IC2Items.WRENCH,
         'V',
         IC2Items.CIRCUIT,
         'B',
         IC2Blocks.MFE,
         'X',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'C',
         ItemTags.PLANKS
      );
      recipes.addShapedIC2Recipe(
         "mv_charge_bench_kit_indirect_bronze",
         new ItemStack(IC2Items.MV_CHARGE_UPGRADE),
         "XYX",
         "CVC",
         "CBC",
         'Y',
         IC2Items.WRENCH,
         'V',
         IC2Items.CIRCUIT,
         'B',
         IC2Blocks.MFE,
         'X',
         new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED),
         'C',
         ItemTags.PLANKS
      );
      recipes.addShapedIC2Recipe(
         "hv_charge_bench_kit_indirect",
         new ItemStack(IC2Items.HV_CHARGE_UPGRADE),
         "XYX",
         "CVC",
         "CBC",
         'Y',
         IC2Items.WRENCH,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.MFSU,
         'X',
         IC2Items.GLASSFIBER_CABLE,
         'C',
         ItemTags.PLANKS
      );
      recipes.addShapedIC2Recipe(
         "ev_charge_bench_kit_indirect",
         new ItemStack(IC2Items.EV_CHARGE_UPGRADE),
         "XYX",
         "CVC",
         "CBC",
         'Y',
         IC2Items.WRENCH,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.ESU,
         'X',
         IC2Items.IRON_CABLE_4X_INSULATED,
         'C',
         ItemTags.PLANKS
      );
      recipes.addShapedIC2Recipe(
         "iv_charge_bench_kit_indirect",
         new ItemStack(IC2Items.IV_CHARGE_UPGRADE),
         "XYX",
         "CVC",
         "CBC",
         'Y',
         IC2Items.WRENCH,
         'V',
         IC2Items.COMPLEX_CIRCUIT,
         'B',
         IC2Blocks.ISU,
         'X',
         IC2Items.SUPER_CABLE,
         'C',
         ItemTags.PLANKS
      );
      recipes.addShapedIC2Recipe(
         "mv_charge_bench_kit_direct", new ItemStack(IC2Items.MV_CHARGE_UPGRADE), "X", "Y", 'X', IC2Items.WRENCH, 'Y', IC2Blocks.CHARGING_BENCH_MV
      );
      recipes.addShapedIC2Recipe(
         "hv_charge_bench_kit_direct", new ItemStack(IC2Items.HV_CHARGE_UPGRADE), "X", "Y", 'X', IC2Items.WRENCH, 'Y', IC2Blocks.CHARGING_BENCH_HV
      );
      recipes.addShapedIC2Recipe(
         "ev_charge_bench_kit_direct", new ItemStack(IC2Items.EV_CHARGE_UPGRADE), "X", "Y", 'X', IC2Items.WRENCH, 'Y', IC2Blocks.CHARGING_BENCH_EV
      );
      recipes.addShapedIC2Recipe(
         "iv_charge_bench_kit_direct", new ItemStack(IC2Items.IV_CHARGE_UPGRADE), "X", "Y", 'X', IC2Items.WRENCH, 'Y', IC2Blocks.CHARGING_BENCH_IV
      );
      recipes.addShapedIC2Recipe(
         "mv_battery_station_kit_indirect_gold",
         new ItemStack(IC2Items.MV_BATTERY_UPGRADE),
         "XYX",
         "CVC",
         "CBC",
         'Y',
         IC2Items.WRENCH,
         'V',
         IC2Items.CIRCUIT,
         'B',
         IC2Blocks.TRANSFORMER_MV,
         'X',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'C',
         ItemTags.PLANKS
      );
      recipes.addShapedIC2Recipe(
         "mv_battery_station_kit_indirect_bronze",
         new ItemStack(IC2Items.MV_BATTERY_UPGRADE),
         "XYX",
         "CVC",
         "CBC",
         'Y',
         IC2Items.WRENCH,
         'V',
         IC2Items.CIRCUIT,
         'B',
         IC2Blocks.TRANSFORMER_MV,
         'X',
         new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED),
         'C',
         ItemTags.PLANKS
      );
      recipes.addShapedIC2Recipe(
         "hv_battery_station_kit_indirect",
         new ItemStack(IC2Items.HV_BATTERY_UPGRADE),
         "XYX",
         "CVC",
         "CBC",
         'Y',
         IC2Items.WRENCH,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.TRANSFORMER_HV,
         'X',
         IC2Items.GLASSFIBER_CABLE,
         'C',
         ItemTags.PLANKS
      );
      recipes.addShapedIC2Recipe(
         "ev_battery_station_kit_indirect",
         new ItemStack(IC2Items.EV_BATTERY_UPGRADE),
         "XYX",
         "CVC",
         "CBC",
         'Y',
         IC2Items.WRENCH,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.TRANSFORMER_EV,
         'X',
         IC2Items.IRON_CABLE_4X_INSULATED,
         'C',
         ItemTags.PLANKS
      );
      recipes.addShapedIC2Recipe(
         "iv_battery_station_kit_indirect",
         new ItemStack(IC2Items.IV_BATTERY_UPGRADE),
         "XYX",
         "CVC",
         "CBC",
         'Y',
         IC2Items.WRENCH,
         'V',
         IC2Items.COMPLEX_CIRCUIT,
         'B',
         IC2Blocks.TRANSFORMER_IV,
         'X',
         IC2Items.SUPER_CABLE,
         'C',
         ItemTags.PLANKS
      );
      recipes.addShapedIC2Recipe(
         "mv_battery_station_kit_direct", new ItemStack(IC2Items.MV_BATTERY_UPGRADE), "X", "Y", 'X', IC2Items.WRENCH, 'Y', IC2Blocks.BATTERY_STATION_MV
      );
      recipes.addShapedIC2Recipe(
         "hv_battery_station_kit_direct", new ItemStack(IC2Items.HV_BATTERY_UPGRADE), "X", "Y", 'X', IC2Items.WRENCH, 'Y', IC2Blocks.BATTERY_STATION_HV
      );
      recipes.addShapedIC2Recipe(
         "ev_battery_station_kit_direct", new ItemStack(IC2Items.EV_BATTERY_UPGRADE), "X", "Y", 'X', IC2Items.WRENCH, 'Y', IC2Blocks.BATTERY_STATION_EV
      );
      recipes.addShapedIC2Recipe(
         "iv_battery_station_kit_direct", new ItemStack(IC2Items.IV_BATTERY_UPGRADE), "X", "Y", 'X', IC2Items.WRENCH, 'Y', IC2Blocks.BATTERY_STATION_IV
      );
      recipes.addShapedIC2Recipe(
         "crystal_upgrade_kit",
         new ItemStack(IC2Items.CRYSTAL_UPGRADE_KIT),
         "X",
         "Y",
         "C",
         'X',
         IC2Items.ELECTRIC_WRENCH,
         'Y',
         IC2Items.CIRCUIT,
         'C',
         IC2Blocks.MFE
      );
      recipes.addShapedIC2Recipe(
         "lapotronic_upgrade_kit",
         new ItemStack(IC2Items.LAPOTRONIC_UPGRADE_KIT),
         "XYX",
         "CVC",
         'V',
         IC2Items.MFSU_UPGRADE_KIT,
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Items.ELECTRIC_WRENCH
      );
      recipes.addShapedIC2Recipe(
         "fission_upgrade_kit",
         new ItemStack(IC2Items.FISSION_UPGRADE_KIT),
         "XYX",
         "VCV",
         'X',
         IC2Items.PLATE_IRIDIUM,
         'C',
         IC2Blocks.NUCLEAR_REACTOR,
         'V',
         IC2Items.TFBP_CHILLING,
         'Y',
         IC2Items.ELECTRIC_WRENCH
      );
      recipes.addShapedIC2Recipe(
         "proximity_upgrade",
         new ItemStack(IC2Items.PROXIMITY_PAD_UPGRADE, 2),
         "XXX",
         "YCY",
         "VAV",
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'Y',
         IC2Items.OVERCLOCKER_UPGRADE,
         'C',
         IC2Items.SPLITTER_CABLE,
         'V',
         IC2Items.GOLD_CABLE_2X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "wide_band_upgrade",
         new ItemStack(IC2Items.WIDE_BAND_PAD_UPGRADE),
         "XYX",
         "CVC",
         "XYX",
         'A',
         IC2Items.ADVANCED_CIRCUIT,
         'V',
         IC2Items.SPLITTER_CABLE,
         'Y',
         IC2Items.PROXIMITY_PAD_UPGRADE,
         'C',
         IC2Items.ENERGY_CRYSTAL
      );
      recipes.addShapedIC2Recipe(
         "armor_priority_upgrade",
         new ItemStack(IC2Items.ARMOR_PRIORITY_PAD_UPGRADE),
         "XYX",
         "CVC",
         'Y',
         Items.ENDER_PEARL,
         'X',
         Items.GLOWSTONE_DUST,
         'C',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'V',
         IC2Items.PROXIMITY_PAD_UPGRADE
      );
      recipes.addShapedIC2Recipe(
         "basic_field_upgrade",
         new ItemStack(IC2Items.BASIC_FIELD_PAD_UPGRADE),
         "XYX",
         "CCC",
         "VBV",
         'Y',
         IC2Items.FREQUENCY_TRANSMITTER,
         'X',
         IC2Items.REFLECTOR,
         'C',
         IC2Items.ADVANCED_CIRCUIT,
         'V',
         IC2Items.OVERCLOCKER_UPGRADE,
         'B',
         IC2Items.UPGRADE_BASE
      );
      recipes.addShapedIC2Recipe(
         "field_upgrade",
         new ItemStack(IC2Items.FIELD_PAD_UPGRADE),
         "XYX",
         "CVC",
         "XYX",
         'X',
         IC2Items.BASIC_FIELD_PAD_UPGRADE,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'Y',
         IC2Items.EFFICIENCY_UPGRADE,
         'C',
         IC2Items.SPLITTER_CABLE
      );
      recipes.addShapedIC2Recipe(
         "advanced_field_upgrade",
         new ItemStack(IC2Items.ADVANCED_FIELD_PAD_UPGRADE),
         "XYX",
         "CVC",
         "BNB",
         'C',
         IC2Items.FIELD_PAD_UPGRADE,
         'N',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Items.EFFICIENCY_UPGRADE,
         'V',
         IC2Items.SPLITTER_CABLE,
         'X',
         IC2Items.REFLECTOR_THICK,
         'Y',
         IC2Items.OVERCLOCKER_UPGRADE
      );
      recipes.addShapedIC2Recipe(
         "damage_upgrade",
         new ItemStack(IC2Items.DAMAGE_PAD_UPGRADE),
         "XXX",
         "YCY",
         'X',
         IC2Items.URANIUM_ROD_NEAR_DEPLETED,
         'Y',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "drain_upgrade",
         new ItemStack(IC2Items.DRAIN_PAD_UPGRADE),
         "XXX",
         "YCY",
         'X',
         Items.GLOWSTONE_DUST,
         'Y',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
   }

   private static void addComponents(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "circuit_0",
         new ItemStack(IC2Items.CIRCUIT),
         "CCC",
         "RIR",
         "CCC",
         'I',
         IC2Tags.INGOT_REFINED_IRON,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "circuit_1",
         new ItemStack(IC2Items.CIRCUIT),
         "CRC",
         "CIC",
         "CRC",
         'I',
         IC2Tags.INGOT_REFINED_IRON,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "advanced_circuit_0",
         new ItemStack(IC2Items.ADVANCED_CIRCUIT),
         "RGR",
         "LCL",
         "RGR",
         'L',
         Items.LAPIS_LAZULI,
         'G',
         Items.GLOWSTONE_DUST,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "advanced_circuit_1",
         new ItemStack(IC2Items.ADVANCED_CIRCUIT),
         "RLR",
         "GCG",
         "RLR",
         'L',
         Items.LAPIS_LAZULI,
         'G',
         Items.GLOWSTONE_DUST,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "complex_circuit",
         new ItemStack(IC2Items.COMPLEX_CIRCUIT),
         "XYX",
         "CVC",
         "XYX",
         'X',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'Y',
         IC2Items.PULSATING_QUARTZ,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Items.MAGNET
      );
      recipes.addShapedIC2Recipe("carbon_fiber", new ItemStack(IC2Items.CARBON_FIBER), "CC", "CC", 'C', IC2Tags.DUST_COAL);
      recipes.addShapelessIC2Recipe("carbon_mesh", new ItemStack(IC2Items.CARBON_MESH), IC2Items.CARBON_FIBER, IC2Items.CARBON_FIBER);
      recipes.addShapedIC2Recipe("coal_ball", new ItemStack(IC2Items.COAL_BALL), "CCC", "CFC", "CCC", 'C', IC2Tags.DUST_COAL, 'F', Items.FLINT);
      recipes.addShapedIC2Recipe(
         "coal_chunk_0", new ItemStack(IC2Items.COAL_CHUNK), "###", "#O#", "###", '#', IC2Items.COAL_BALL_COMPRESSED, 'O', Items.OBSIDIAN
      );
      recipes.addShapedIC2Recipe(
         "coal_chunk_1",
         new ItemStack(IC2Items.COAL_CHUNK),
         "###",
         "#O#",
         "###",
         '#',
         IC2Items.COAL_BALL_COMPRESSED,
         'O',
         Items.IRON_BLOCK,
         RecipeMods.HIDDEN_RECIPE
      );
      recipes.addShapedIC2Recipe(
         "coal_chunk_2",
         new ItemStack(IC2Items.COAL_CHUNK),
         "###",
         "#O#",
         "###",
         '#',
         IC2Items.COAL_BALL_COMPRESSED,
         'O',
         Items.BRICKS,
         RecipeMods.HIDDEN_RECIPE
      );
      recipes.addShapedIC2Recipe(
         "iron_dust_0", new ItemStack(IC2Items.DUST_IRON), RecipeMods.HIDDEN_RECIPE, "CTC", "TCT", "CTC", 'C', IC2Tags.DUST_COPPER, 'T', IC2Tags.DUST_TIN
      );
      recipes.addShapedIC2Recipe(
         "iron_dust_1", new ItemStack(IC2Items.DUST_IRON), RecipeMods.HIDDEN_RECIPE, "TCT", "CTC", "TCT", 'C', IC2Tags.DUST_COPPER, 'T', IC2Tags.DUST_TIN
      );
      recipes.addShapedIC2Recipe(
         "tin_dust_0", new ItemStack(IC2Items.DUST_TIN), RecipeMods.HIDDEN_RECIPE, "CTC", "TCT", "CTC", 'C', IC2Tags.DUST_COPPER, 'T', IC2Tags.DUST_IRON
      );
      recipes.addShapedIC2Recipe(
         "tin_dust_1", new ItemStack(IC2Items.DUST_TIN), RecipeMods.HIDDEN_RECIPE, "TCT", "CTC", "TCT", 'C', IC2Tags.DUST_COPPER, 'T', IC2Tags.DUST_IRON
      );
      recipes.addShapedIC2Recipe(
         "silver_dust_0", new ItemStack(IC2Items.DUST_SILVER), RecipeMods.HIDDEN_RECIPE, "CTC", "TCT", "CTC", 'C', IC2Tags.DUST_BRONZE, 'T', IC2Tags.DUST_GOLD
      );
      recipes.addShapedIC2Recipe(
         "silver_dust_1", new ItemStack(IC2Items.DUST_SILVER), RecipeMods.HIDDEN_RECIPE, "TCT", "CTC", "TCT", 'C', IC2Tags.DUST_BRONZE, 'T', IC2Tags.DUST_GOLD
      );
      recipes.addShapedIC2Recipe(
         "rubber_sapling",
         new ItemStack(IC2Blocks.RUBBER_SAPLING),
         RecipeMods.HIDDEN_RECIPE,
         "XYX",
         "CXV",
         "XBX",
         'X',
         new ItemStack(IC2Items.PLANT_BALL, 16),
         'Y',
         Blocks.OAK_SAPLING,
         'C',
         Blocks.BIRCH_SAPLING,
         'V',
         Blocks.SPRUCE_SAPLING,
         'B',
         Blocks.JUNGLE_SAPLING
      );
      recipes.addShapedIC2Recipe(
         "iridium_plate",
         new ItemStack(IC2Items.PLATE_IRIDIUM),
         "IAI",
         "ADA",
         "IAI",
         'I',
         IC2Items.ORE_IRIDIUM,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'D',
         net.minecraftforge.common.Tags.Items.GEMS_DIAMOND
      );
      recipes.addShapedIC2Recipe(
         "advanced_alloy",
         new ItemStack(IC2Items.INGOT_ADVANCED_ALLOY, 2),
         "III",
         "BBB",
         "TTT",
         'I',
         IC2Tags.INGOT_REFINED_IRON,
         'B',
         IC2Tags.INGOT_BRONZE,
         'T',
         IC2Tags.INGOT_ALUMINIUM
      );
      recipes.addShapedIC2Recipe(
         "scrap_metal_blade", new ItemStack(IC2Items.SCRAP_METAL_BLADE), "XXX", "XYX", "XXX", 'X', Items.FLINT, 'Y', IC2Items.SCRAP_METAL_CHUNK
      );
      recipes.addShapedIC2Recipe(
         "raw_obsidian_blade", new ItemStack(IC2Items.OBSIDIAN_BLADE_RAW), "XXX", "XYX", "XXX", 'X', IC2Items.DUST_OBSIDIAN, 'Y', Items.FLINT
      );
      recipes.addShapedIC2Recipe("turbine_blade", new ItemStack(IC2Items.TURBINE_BLADE), "X X", " X ", "X X", 'X', IC2Tags.INGOT_REFINED_IRON);
      recipes.addShapedIC2Recipe(
         "rare_earth_chunk",
         new ItemStack(IC2Items.RARE_EARTH_CHUNK),
         " X ",
         "XYX",
         " X ",
         'X',
         IC2Items.DUST_RARE_EARTH,
         'Y',
         net.minecraftforge.common.Tags.Items.INGOTS_IRON
      );
      recipes.addShapedIC2Recipe(
         "plasma_core",
         new ItemStack(IC2Items.PLASMA_CORE),
         "XYX",
         "YCY",
         "XYX",
         'X',
         new ItemStack(IC2Items.MAGNET, 2),
         'Y',
         new ItemStack(IC2Items.PLATE_ADVANCED_ALLOY, 4),
         'C',
         IC2Items.CELL_PLASMA
      );
   }

   private static void addFoodAndDrink(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe("tin_can", new ItemStack(IC2Items.TIN_CAN, 4), "T T", "TTT", 'T', IC2Tags.INGOT_TIN);
      recipes.addShapedIC2Recipe("barrel_0", new ItemStack(IC2Blocks.BARREL), "P", "W", "P", 'P', ItemTags.PLANKS, 'W', IC2Blocks.RUBBERWOOD_LOG);
      recipes.addShapelessIC2Recipe("barrel_1", new ItemStack(IC2Blocks.BARREL), IC2Blocks.BARREL);
      recipes.addShapelessIC2Recipe("coffee_powder", new ItemStack(IC2Items.COFFEE_POWDER), IC2Items.COFFEE_BEANS);
      recipes.addShapedIC2Recipe("mug", new ItemStack(IC2Items.MUG), "SS ", "SSS", "SS ", 'S', net.minecraftforge.common.Tags.Items.STONE);
      recipes.addShapedIC2Recipe("glass", new ItemStack(IC2Items.GLASS), "G G", "G G", "GGG", 'G', net.minecraftforge.common.Tags.Items.GLASS);
      recipes.addIC2SmeltingRecipe(
         "flour_to_bread",
         new ItemStack(Items.BREAD),
         IC2Items.FLOUR,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.SMOKER,
         IAdvancedCraftingManager.SmeltingType.CAMPFIRE
      );
      recipes.addIC2SmeltingRecipe(
         "fries",
         new ItemStack(IC2Items.FRIES),
         IC2Items.POTATO_SLICES,
         0.0F,
         2.0F,
         IAdvancedCraftingManager.SmeltingType.SMOKER,
         IAdvancedCraftingManager.SmeltingType.CAMPFIRE
      );
      recipes.addShapedIC2Recipe(
         "chips",
         new ItemStack(IC2Items.CHIPS_ROLL),
         new CountingModifier(IC2Items.FRIES, 4),
         "XYX",
         "CYC",
         "XYX",
         'X',
         Items.PAPER,
         'C',
         Items.STICK,
         'Y',
         new ItemStack(IC2Items.FRIES, 64)
      );
      recipes.addShapedIC2Recipe(
         "chocolate_cake",
         new ItemStack(IC2Blocks.CAKE_CHOCOLATE),
         "XXX",
         "YCY",
         "VVV",
         'X',
         Items.COCOA_BEANS,
         'Y',
         Items.SUGAR,
         'C',
         Items.EGG,
         'V',
         Items.WHEAT
      );
      recipes.addShapedIC2Recipe(
         "coffee_cake",
         new ItemStack(IC2Blocks.CAKE_COFFEE),
         "XXX",
         "YCY",
         "VVV",
         'X',
         IC2Items.COFFEE_POWDER,
         'Y',
         Items.SUGAR,
         'C',
         Items.EGG,
         'V',
         Items.WHEAT
      );
      recipes.addShapedIC2Recipe(
         "lemon_cake", new ItemStack(IC2Blocks.CAKE_LEMON), "YXY", "YCY", "VVV", 'X', Items.GLOW_BERRIES, 'Y', Items.SUGAR, 'C', Items.EGG, 'V', Items.WHEAT
      );
      recipes.addShapedIC2Recipe(
         "honey_cake", new ItemStack(IC2Blocks.CAKE_HONEY), "XXX", "YCY", "VVV", 'X', Items.HONEY_BOTTLE, 'Y', Items.SUGAR, 'C', Items.EGG, 'V', Items.WHEAT
      );

      for (IC2Drink container : IC2FoodsAndDrinks.CONTAINERS) {
         Map<IDrinkableFluid, Item> filled = container.getFilledContainers();
         String containerName = ForgeRegistryWrapper.getItemKey(container).getPath();
         recipes.addShapelessIC2Recipe(
            containerName + "_cold_dark_coffee",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.COLD_DARK_COFFEE)),
            container,
            IC2Items.COFFEE_POWDER,
            Fluids.WATER
         );
         recipes.addShapelessIC2Recipe(
            containerName + "_coffee",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.COFFEE)),
            filled.get(IC2FoodsAndDrinks.DARK_COFFEE),
            Items.SUGAR,
            Items.MILK_BUCKET
         );
         recipes.addShapelessIC2Recipe(
            containerName + "_cold_black_tea",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.COLD_BLACK_TEA)),
            IC2Items.TEA_LEAF,
            IC2Items.TEA_LEAF,
            container,
            Fluids.WATER
         );
         recipes.addShapelessIC2Recipe(
            containerName + "_cold_fruit_tea",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.COLD_FRUIT_TEA)),
            IC2Items.TEA_LEAF,
            ItemListInput.createItemList(Items.SWEET_BERRIES, Items.APPLE),
            container,
            Fluids.WATER
         );
         recipes.addShapelessIC2Recipe(
            containerName + "_cold_green_tea",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.COLD_GREEN_TEA)),
            IC2Items.TEA_LEAF,
            container,
            Fluids.WATER
         );
         recipes.addShapelessIC2Recipe(
            containerName + "_cold_tea",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.COLD_TEA)),
            filled.get(IC2FoodsAndDrinks.COLD_BLACK_TEA),
            Items.MILK_BUCKET
         );
         recipes.addShapelessIC2Recipe(
            containerName + "_tea_0", new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.TEA)), filled.get(IC2FoodsAndDrinks.BLACK_TEA), Items.MILK_BUCKET
         );
         recipes.addShapelessIC2Recipe(
            containerName + "_iced_tea",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.ICED_TEA)),
            filled.get(IC2FoodsAndDrinks.COLD_BLACK_TEA),
            Items.SUGAR,
            Items.ICE
         );
         recipes.addIC2SmeltingRecipe(
            containerName + "_dark_coffee",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.DARK_COFFEE)),
            filled.get(IC2FoodsAndDrinks.COLD_DARK_COFFEE),
            IAdvancedCraftingManager.SmeltingType.FURNACE,
            IAdvancedCraftingManager.SmeltingType.SMOKER
         );
         recipes.addShapelessIC2Recipe(containerName + "_milk", new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.MILK)), container, Items.MILK_BUCKET);
         recipes.addShapelessIC2Recipe(
            containerName + "_cocoa_0", new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.COCOA)), container, Items.MILK_BUCKET, Items.COCOA_BEANS
         );
         recipes.addShapelessIC2Recipe(
            containerName + "_cocoa_1", new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.COCOA)), filled.get(IC2FoodsAndDrinks.MILK), Items.COCOA_BEANS
         );
         recipes.addIC2SmeltingRecipe(
            containerName + "_tea_1",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.TEA)),
            filled.get(IC2FoodsAndDrinks.COLD_TEA),
            IAdvancedCraftingManager.SmeltingType.FURNACE,
            IAdvancedCraftingManager.SmeltingType.SMOKER
         );
         recipes.addIC2SmeltingRecipe(
            containerName + "_black_tea",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.BLACK_TEA)),
            filled.get(IC2FoodsAndDrinks.COLD_BLACK_TEA),
            IAdvancedCraftingManager.SmeltingType.FURNACE,
            IAdvancedCraftingManager.SmeltingType.SMOKER
         );
         recipes.addIC2SmeltingRecipe(
            containerName + "_fruit_tea",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.FRUIT_TEA)),
            filled.get(IC2FoodsAndDrinks.COLD_FRUIT_TEA),
            IAdvancedCraftingManager.SmeltingType.FURNACE,
            IAdvancedCraftingManager.SmeltingType.SMOKER
         );
         recipes.addIC2SmeltingRecipe(
            containerName + "_green_tea",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.GREEN_TEA)),
            filled.get(IC2FoodsAndDrinks.COLD_GREEN_TEA),
            IAdvancedCraftingManager.SmeltingType.FURNACE,
            IAdvancedCraftingManager.SmeltingType.SMOKER
         );
         recipes.addIC2SmeltingRecipe(
            containerName + "_hot_cocoa",
            new ItemStack((ItemLike)filled.get(IC2FoodsAndDrinks.HOT_COCOA)),
            filled.get(IC2FoodsAndDrinks.COCOA),
            IAdvancedCraftingManager.SmeltingType.FURNACE,
            IAdvancedCraftingManager.SmeltingType.SMOKER
         );
      }
   }

   private static void addCrops(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe("crop_sticks", new ItemStack(IC2Items.CROP_STICKS, 2), "S S", "S S", 'S', Items.STICK);
      recipes.addShapelessIC2Recipe("fertilizer_0", new ItemStack(IC2Items.FERTILIZER, 2), IC2Items.SCRAP, Items.BONE_MEAL);
      recipes.addShapelessIC2Recipe("fertilizer_1", new ItemStack(IC2Items.FERTILIZER, 2), IC2Items.SCRAP, IC2Items.SCRAP, IC2Items.FERTILIZER);
      recipes.addShapedIC2Recipe(
         "weedex",
         new ItemStack(IC2Items.WEEDEX),
         "R",
         "G",
         "C",
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'G',
         IC2Items.GRIN_POWDER,
         'C',
         IC2Items.CELL_EMPTY
      );
      recipes.addShapelessIC2Recipe(
         "overgrowth_fertilizer",
         new ItemStack(IC2Items.OVERGROWTH_FERTILIZER, 3),
         IC2Items.FERTILIZER,
         Fluids.WATER,
         Items.BONE_MEAL,
         IC2Items.GRIN_POWDER,
         IC2Items.SCRAP,
         IC2Items.SCRAP
      );
      recipes.addShapelessIC2Recipe(
         "restoring_fertilizer",
         new ItemStack(IC2Items.RESTORING_FERTILIZER, 3),
         new ItemStack(IC2Items.CELL_WATER, 64),
         new ItemStack(IC2Items.CELL_WATER, 64),
         new ItemStack(IC2Items.FERTILIZER, 64),
         new ItemStack(IC2Items.FERTILIZER, 64),
         new ItemStack(IC2Items.CELL_AIR, 64),
         new ItemStack(IC2Items.CELL_AIR, 64)
      );
      recipes.addShapedIC2Recipe("paper", new ItemStack(Items.PAPER), "X X", " X ", "X X", 'X', IC2Items.HEMP);
      recipes.addShapelessIC2Recipe("string", new ItemStack(Items.STRING), IC2Items.HEMP);
      recipes.addShapelessIC2Recipe("allium", new ItemStack(Items.ALLIUM), CropRegistry.ALLIUM);
      recipes.addShapelessIC2Recipe("blue_orchid", new ItemStack(Items.BLUE_ORCHID), CropRegistry.BLUE_ORCHID);
      recipes.addShapelessIC2Recipe("cornflower", new ItemStack(Items.CORNFLOWER), CropRegistry.CORNFLOWER);
      recipes.addShapelessIC2Recipe("dandelion", new ItemStack(Items.DANDELION), CropRegistry.DANDELION);
      recipes.addShapelessIC2Recipe("orange_tulip", new ItemStack(Items.ORANGE_TULIP), CropRegistry.ORANGE_TULIP);
      recipes.addShapelessIC2Recipe("oxeye_daisy", new ItemStack(Items.OXEYE_DAISY), CropRegistry.OXEYE_DAISY);
      recipes.addShapelessIC2Recipe("pink_tulip", new ItemStack(Items.PINK_TULIP), CropRegistry.PINK_TULIP);
      recipes.addShapelessIC2Recipe("poppy", new ItemStack(Items.POPPY), CropRegistry.POPPY);
      recipes.addShapelessIC2Recipe("red_tulip", new ItemStack(Items.RED_TULIP), CropRegistry.RED_TULIP);
      recipes.addShapelessIC2Recipe("white_tulip", new ItemStack(Items.WHITE_TULIP), CropRegistry.WHITE_TULIP);
      recipes.addShapelessIC2Recipe("azure_bluet", new ItemStack(Items.AZURE_BLUET), CropRegistry.AZURE_BLUET);
      recipes.addShapelessIC2Recipe("lily_of_the_valley", new ItemStack(Items.LILY_OF_THE_VALLEY), CropRegistry.LILY_OF_THE_VALLEY);
      recipes.addShapelessIC2Recipe("beetroot_seeds", new ItemStack(Items.BEETROOT_SEEDS), CropRegistry.BEETROOT);
      recipes.addShapelessIC2Recipe("wheat_seeds", new ItemStack(Items.WHEAT_SEEDS), CropRegistry.WHEAT);
      recipes.addShapelessIC2Recipe("pumpkin_seeds", new ItemStack(Items.PUMPKIN_SEEDS), CropRegistry.PUMPKIN);
      recipes.addShapelessIC2Recipe("melon_seeds", new ItemStack(Items.MELON_SEEDS), CropRegistry.MELON);
      recipes.addShapelessIC2Recipe("oak_sapling", new ItemStack(Items.OAK_SAPLING), CropRegistry.OAK_SAPLING);
      recipes.addShapelessIC2Recipe("spruce_sapling", new ItemStack(Items.SPRUCE_SAPLING), CropRegistry.SPRUCE_SAPLING);
      recipes.addShapelessIC2Recipe("birch_sapling", new ItemStack(Items.BIRCH_SAPLING), CropRegistry.BIRCH_SAPLING);
      recipes.addShapelessIC2Recipe("jungle_sapling", new ItemStack(Items.JUNGLE_SAPLING), CropRegistry.JUNGLE_SAPLING);
      recipes.addShapelessIC2Recipe("acacia_sapling", new ItemStack(Items.ACACIA_SAPLING), CropRegistry.ACACIA_SAPLING);
      recipes.addShapelessIC2Recipe("dark_oak_sapling", new ItemStack(Items.DARK_OAK_SAPLING), CropRegistry.DARK_OAK_SAPLING);
      recipes.addShapelessIC2Recipe("rubberwood_sapling", new ItemStack(IC2Blocks.RUBBER_SAPLING), CropRegistry.RUBBERWOOD_SAPLING);

      for (Block block : CropRegistry.REGISTRY.getAllFarmlands()) {
         if (block != IC2Blocks.PLANTER_POT) {
            ItemStack stack = new ItemStack(IC2Blocks.PLANTER_POT);
            PlanterPotBlock.setId(stack, block);
            recipes.addShapedIC2Recipe("planter_pot_" + ForgeRegistryWrapper.getBlockKey(block).getPath(), stack, "Y", "X", 'X', Items.FLOWER_POT, 'Y', block);
         }
      }
   }

   private static void addMisc(IAdvancedCraftingManager recipes) {
      recipes.addSmithingIC2Recipe("tin_can_cutting", IC2Tags.INGOT_TIN, IC2Items.CUTTER, new ItemStack(IC2Items.TIN_CAN, 2));
      recipes.addShapedIC2Recipe(
         "unprinted_wiki", new ItemStack(IC2Items.UNPRINTED_WIKI), " X ", "YCY", " X ", 'X', Items.INK_SAC, 'Y', IC2Items.CARBON_PLATE, 'C', Items.BOOK
      );
      recipes.addShapedIC2Recipe("empty_cell", new ItemStack(IC2Items.CELL_EMPTY, 16), " T ", "T T", " T ", 'T', IC2Tags.INGOT_TIN);
      recipes.addShapedIC2Recipe("fuel_can", new ItemStack(IC2Items.FUEL_CAN), " TT", "T T", "TTT", 'T', IC2Tags.INGOT_TIN);
      recipes.addShapedIC2Recipe(
         "glass_to_splash", new ItemStack(IC2Items.EMPTY_SPLASH_POTION, 8), "XXX", "XYX", "XXX", 'Y', Items.GUNPOWDER, 'X', Items.GLASS_BOTTLE
      );
      recipes.addShapedIC2Recipe(
         "glass_to_lingering", new ItemStack(IC2Items.EMPTY_LINGERING_POTION, 8), "XXX", "XYX", "XXX", 'Y', Items.DRAGON_BREATH, 'X', Items.GLASS_BOTTLE
      );
      recipes.addShapedIC2Recipe("torch", new ItemStack(Items.TORCH, 4), "R", "S", 'R', IC2Items.STICKY_RESIN, 'S', Items.STICK, RecipeMods.HIDDEN_RECIPE);
      recipes.addShapedIC2Recipe("wooden_scaffold", new ItemStack(IC2Blocks.SCAFFOLD_WOOD, 16), "PPP", " S ", "S S", 'P', ItemTags.PLANKS, 'S', Items.STICK);
      recipes.addShapedIC2Recipe(
         "iron_scaffold", new ItemStack(IC2Blocks.SCAFFOLD_IRON, 16), "PPP", " S ", "S S", 'P', IC2Tags.INGOT_REFINED_IRON, 'S', IC2Blocks.IRON_FENCE
      );
      recipes.addShapelessIC2Recipe("sticky_piston", new ItemStack(Items.STICKY_PISTON), Items.PISTON, IC2Items.STICKY_RESIN, RecipeMods.HIDDEN_RECIPE);
      recipes.addShapedIC2Recipe(
         "mining_pipe", new ItemStack(IC2Blocks.MINING_PIPE_SHAFT, 8), "I I", "I I", "ITI", 'I', IC2Tags.INGOT_REFINED_IRON, 'T', IC2Items.TREETAP
      );
      recipes.addShapedIC2Recipe("lead", new ItemStack(Items.LEAD, 2), "XX ", "XY ", "  X", 'X', Items.STRING, 'Y', IC2Items.STICKY_RESIN);
      recipes.addShapelessIC2Recipe(
         "obsidian_lava", new ItemStack(Items.OBSIDIAN), IC2Items.CELL_WATER, IC2Items.CELL_WATER, IC2Items.CELL_LAVA, IC2Items.CELL_LAVA
      );
      recipes.addShapelessIC2Recipe(
         "obsidian_magma", new ItemStack(Items.OBSIDIAN), IC2Items.CELL_WATER, IC2Items.CELL_WATER, Items.MAGMA_BLOCK, Items.MAGMA_BLOCK
      );
      recipes.addShapelessIC2Recipe("mud", new ItemStack(Items.MUD), Items.DIRT, Items.CLAY_BALL, Fluids.WATER);
      recipes.addShapedIC2Recipe("rubber_boat", new ItemStack(IC2Items.RUBBER_BOAT), "XYX", "XXX", 'X', IC2Tags.RUBBER, 'Y', ItemTags.BOATS);
      recipes.addShapelessIC2Recipe("rubber_boat_repair", new ItemStack(IC2Items.RUBBER_BOAT), IC2Items.RUBBER_BOAT_BROKEN, IC2Tags.RUBBER, IC2Tags.RUBBER);
      recipes.addShapedIC2Recipe("carbon_boat", new ItemStack(IC2Items.CARBON_BOAT), "XYX", "XXX", 'X', IC2Items.CARBON_PLATE, 'Y', ItemTags.BOATS);
      recipes.addShapedIC2Recipe("scrapbox", new ItemStack(IC2Items.SCRAPBOX), "SSS", "SSS", "SSS", 'S', IC2Items.SCRAP);
      recipes.addShapedIC2Recipe(
         "memory_stick",
         new ItemStack(IC2Items.MEMORY_STICK),
         "XYX",
         "CVC",
         "BNB",
         'X',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'Y',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'C',
         IC2Items.ADVANCED_CIRCUIT,
         'V',
         net.minecraftforge.common.Tags.Items.GEMS_DIAMOND,
         'B',
         IC2Items.CARBON_PLATE,
         'N',
         IC2Items.UPGRADE_BASE
      );
      if (!IC2.CONFIG.disableGlowstoneCrafting.get()) {
         recipes.addShapedIC2Recipe(
            "glowstone_dust",
            new ItemStack(Items.GLOWSTONE_DUST, 1),
            "RGR",
            "GRG",
            "RGR",
            'R',
            net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
            'G',
            IC2Tags.DUST_GOLD,
            RecipeMods.HIDDEN_RECIPE
         );
      }

      if (!IC2.CONFIG.disableGunpowderCrafting.get()) {
         recipes.addShapedIC2Recipe(
            "gunpowder",
            new ItemStack(Items.GUNPOWDER, 3),
            "RCR",
            "CRC",
            "RCR",
            'R',
            net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
            'C',
            IC2Tags.DUST_COAL,
            RecipeMods.HIDDEN_RECIPE
         );
      }

      if (!IC2.CONFIG.disableAlternateVanillaCrafting.get()) {
         recipes.addShapedIC2Recipe("bucket", new ItemStack(Items.BUCKET, 1), "T T", " T ", 'T', IC2Tags.INGOT_TIN, RecipeMods.HIDDEN_RECIPE);
         recipes.addShapedIC2Recipe(
            "rail", new ItemStack(Items.RAIL, 8), "B B", "BsB", "B B", 'B', IC2Tags.INGOT_BRONZE, 's', Items.STICK, RecipeMods.HIDDEN_RECIPE
         );
         recipes.addShapedIC2Recipe(
            "piston_silver",
            new ItemStack(Items.PISTON),
            "XXX",
            "YCY",
            "YVY",
            'Y',
            net.minecraftforge.common.Tags.Items.COBBLESTONE,
            'X',
            ItemTags.PLANKS,
            'V',
            net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
            'C',
            IC2Tags.INGOT_SILVER,
            RecipeMods.HIDDEN_RECIPE
         );
         recipes.addShapedIC2Recipe(
            "piston_bronze",
            new ItemStack(Items.PISTON),
            "XXX",
            "YCY",
            "YVY",
            'Y',
            net.minecraftforge.common.Tags.Items.COBBLESTONE,
            'X',
            ItemTags.PLANKS,
            'V',
            net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
            'C',
            IC2Tags.INGOT_BRONZE,
            RecipeMods.HIDDEN_RECIPE
         );
         recipes.addShapedIC2Recipe(
            "hopper",
            new ItemStack(Items.HOPPER),
            "I I",
            "ICI",
            " I ",
            'I',
            IC2Tags.INGOT_BRONZE,
            'C',
            net.minecraftforge.common.Tags.Items.CHESTS_WOODEN,
            RecipeMods.HIDDEN_RECIPE
         );
         recipes.addShapedIC2Recipe("minecart", new ItemStack(Items.MINECART), "I I", "III", 'I', IC2Tags.INGOT_BRONZE, RecipeMods.HIDDEN_RECIPE);
      }

      recipes.addShapelessIC2Recipe("advanced_comparator", new ItemStack(IC2Blocks.ADVANCED_COMPARATOR), Items.COMPARATOR, IC2Items.CIRCUIT);
      recipes.addShapelessIC2Recipe("comparator", new ItemStack(Items.COMPARATOR), IC2Blocks.ADVANCED_COMPARATOR);
      recipes.addShapedIC2Recipe(
         "player_detector",
         new ItemStack(IC2Blocks.PLAYER_DETECTOR, 4),
         "XYX",
         "XCX",
         "XVX",
         'X',
         Items.STONE,
         'Y',
         IC2Items.CIRCUIT,
         'C',
         Items.ENDER_EYE,
         'V',
         Items.REDSTONE
      );
      if (!IC2.CONFIG.disableExplosiveCrafting.get()) {
         recipes.addShapedIC2Recipe("itnt_0", new ItemStack(IC2Blocks.ITNT, 4), "FFF", "TTT", "FFF", 'F', Items.FLINT, 'T', Items.TNT);
         recipes.addShapedIC2Recipe("itnt_1", new ItemStack(IC2Blocks.ITNT, 4), "FTF", "FTF", "FTF", 'F', Items.FLINT, 'T', Items.TNT);
         recipes.addShapedIC2Recipe(
            "nuke", new ItemStack(IC2Blocks.NUKE), "GUG", "UGU", "GUG", 'G', Items.GUNPOWDER, 'U', IC2Tags.INGOT_URANIUM, RecipeMods.HIDDEN_RECIPE
         );
         recipes.addShapedIC2Recipe(
            "sticky_dynamite", new ItemStack(IC2Items.STICKY_DYNAMITE, 8), "DDD", "DRD", "DDD", 'D', IC2Blocks.DYNAMITE, 'R', IC2Items.STICKY_RESIN
         );
         recipes.addShapelessIC2Recipe("dynamite", new ItemStack(IC2Blocks.DYNAMITE, 8), IC2Blocks.ITNT, Items.STRING);
         recipes.addShapedIC2Recipe(
            "dynamite_remote",
            new ItemStack(IC2Items.DYNAMITE_REMOTE),
            " c ",
            "GCG",
            "TTT",
            'c',
            IC2Items.COPPER_CABLE_1X_INSULATED,
            'G',
            Items.GLOWSTONE_DUST,
            'C',
            IC2Items.CIRCUIT,
            'T',
            Items.TNT
         );
         recipes.addShapedIC2Recipe(
            "dynamite_remote_upgrade",
            new ItemStack(IC2Items.DYNAMITE_REMOTE),
            " C ",
            "TLT",
            " F ",
            'C',
            IC2Items.COPPER_CABLE_1X_INSULATED,
            'F',
            IC2Items.FREQUENCY_TRANSMITTER,
            'L',
            Items.LAPIS_LAZULI,
            'T',
            IC2Tags.INGOT_TIN
         );
      }

      recipes.addShapedIC2Recipe(
         "luminator",
         new ItemStack(IC2Blocks.LUMINATOR, 8),
         "ICI",
         "GTG",
         "GGG",
         'G',
         net.minecraftforge.common.Tags.Items.GLASS,
         'I',
         IC2Tags.INGOT_REFINED_IRON,
         'T',
         IC2Items.TIN_CABLE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapelessIC2Recipe(
         "adjustable_luminator", new ItemStack(IC2Blocks.LUMINATOR_ADJUSTABLE, 4), new ItemStack(IC2Blocks.LUMINATOR, 4), IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "construction_light",
         new ItemStack(IC2Blocks.CONSTRUCTION_LIGHT),
         "X",
         "Y",
         "C",
         'X',
         IC2Blocks.LUMINATOR,
         'Y',
         IC2Items.RE_BATTERY,
         'C',
         Items.ARMOR_STAND
      );
      recipes.addShapedIC2Recipe(
         "reinforced_stone",
         new ItemStack(IC2Blocks.REINFORCED_STONE, 8),
         "SSS",
         "SAS",
         "SSS",
         'S',
         net.minecraftforge.common.Tags.Items.STONE,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "reinforced_glass_0",
         new ItemStack(IC2Blocks.REINFORCED_GLASS, 7),
         "GAG",
         "GGG",
         "GAG",
         'G',
         net.minecraftforge.common.Tags.Items.GLASS,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "reinforced_glass_1",
         new ItemStack(IC2Blocks.REINFORCED_GLASS, 7),
         "GGG",
         "AGA",
         "GGG",
         'G',
         net.minecraftforge.common.Tags.Items.GLASS,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe("reinforced_door", new ItemStack(IC2Blocks.REINFORCED_DOOR), "SS", "SS", "SS", 'S', IC2Blocks.REINFORCED_STONE);
      recipes.addShapedIC2Recipe("rubber_sheet", new ItemStack(IC2Blocks.RUBBER_SHEET, 3), "RRR", "RRR", 'R', IC2Tags.RUBBER);
      recipes.addShapedIC2Recipe("iron_fence", new ItemStack(IC2Blocks.IRON_FENCE, 12), "III", "III", 'I', IC2Tags.INGOT_REFINED_IRON);
      recipes.addShapedIC2Recipe("reinforced_brick", new ItemStack(IC2Blocks.REINFORCED_BRICK, 4), "XX", "XX", 'X', IC2Blocks.REINFORCED_STONE);
      recipes.addShapedIC2Recipe(
         "reinforced_cracked_stone",
         new ItemStack(IC2Blocks.REINFORCED_CRACKED_STONE, 8),
         "XXX",
         "XYX",
         "XXX",
         'X',
         IC2Blocks.REINFORCED_STONE,
         'Y',
         ItemListInput.createItemList(IC2Items.SCRAP_METAL_BLADE, IC2Items.OBSIDIAN_BLADE)
      );
      recipes.addShapedIC2Recipe("reinforced_clear_glass", new ItemStack(IC2Blocks.REINFORCED_CLEAR_GLASS, 4), "XX", "XX", 'X', IC2Blocks.REINFORCED_GLASS);
      recipes.addShapedIC2Recipe(
         "iridium_stone", new ItemStack(IC2Blocks.IRIDIUM_STONE, 8), "XXX", "XYX", "XXX", 'X', IC2Blocks.REINFORCED_STONE, 'Y', IC2Items.PLATE_IRIDIUM
      );
      recipes.addShapedIC2Recipe(
         "bio_mesh", new ItemStack(IC2Items.BIO_MESH, 4), "XXX", "XYX", "XXX", 'X', IC2Items.PLANT_BALL_COMPRESSED, 'Y', IC2Items.STICKY_RESIN
      );
      recipes.addShapedIC2Recipe("bio_ball", new ItemStack(IC2Items.BIO_BALL), "XXX", "XYX", "XXX", 'X', IC2Items.BIO_MESH, 'Y', Items.BONE);
      recipes.addShapedIC2Recipe("bio_chunk", new ItemStack(IC2Items.BIO_CHUNK), "XXX", "XYX", "XXX", 'X', IC2Items.BIO_BALL_COMPRESSED, 'Y', Items.COAL);
      recipes.addIC2SmeltingRecipe(
         "bio_fiber_smelting", new ItemStack(IC2Items.CARBON_FIBER), IC2Items.BIO_FIBER_RAW, IAdvancedCraftingManager.SmeltingType.FURNACE
      );
      recipes.addIC2SmeltingRecipe("bio_chunk_smelting", new ItemStack(IC2Items.COAL_CHUNK), IC2Items.BIO_CHUNK, IAdvancedCraftingManager.SmeltingType.FURNACE);
      recipes.addShapedIC2Recipe(
         "flesh_ball", new ItemStack(IC2Items.BIO_FLESH_BALL, 4), "XXX", "XYX", "XXX", 'X', IC2Items.BIO_MESH, 'Y', new ItemStack(Items.ROTTEN_FLESH, 8)
      );
      recipes.addIC2SmeltingRecipe(
         "raw_leather",
         new ItemStack(Items.LEATHER),
         IC2Items.BIO_LEATHER_RAW,
         IAdvancedCraftingManager.SmeltingType.FURNACE,
         IAdvancedCraftingManager.SmeltingType.SMOKER
      );
      recipes.addShapedIC2Recipe(
         "plant_ball_seed",
         new ItemStack(IC2Items.PLANT_BALL),
         "PPP",
         "P P",
         "PPP",
         'P',
         new ArrayInput(
            1,
            false,
            Items.SUGAR_CANE,
            Items.CACTUS,
            Items.VINE,
            Items.GRASS,
            Items.TALL_GRASS,
            ItemTags.LEAVES,
            net.minecraftforge.common.Tags.Items.CROPS,
            IC2Items.HEMP,
            net.minecraftforge.common.Tags.Items.MUSHROOMS,
            net.minecraftforge.common.Tags.Items.SEEDS,
            Items.APPLE,
            IC2Items.STICKY_RESIN,
            IC2Items.CROP_SEED,
            Items.BAMBOO,
            ItemTags.FLOWERS,
            Items.KELP,
            IC2Items.HOPS,
            Items.WARPED_WART_BLOCK,
            Items.NETHER_WART_BLOCK,
            Items.SWEET_BERRIES,
            Items.GLOW_BERRIES,
            Items.COCOA_BEANS
         )
      );
      recipes.addShapedIC2Recipe("plant_ball_sapling", new ItemStack(IC2Items.PLANT_BALL, 2), "PPP", "P P", "PPP", 'P', ItemTags.SAPLINGS);
      recipes.addShapelessIC2Recipe("hydrated_coal_dust", new ItemStack(IC2Items.DUST_HYDRATED_COAL), IC2Tags.DUST_COAL, Fluids.WATER);
      recipes.addShapedIC2Recipe(
         "hydrated_coal_dust_mass", new ItemStack(IC2Items.DUST_HYDRATED_COAL, 8), "CCC", "CWC", "CCC", 'C', IC2Tags.DUST_COAL, 'W', Fluids.WATER
      );
      recipes.addShapelessIC2Recipe("hydrated_charcoal_dust", new ItemStack(IC2Items.DUST_HYDRATED_CHARCOAL), IC2Tags.DUST_CHARCOAL, Fluids.WATER);
      recipes.addShapedIC2Recipe(
         "hydrated_charcoal_dust_mass", new ItemStack(IC2Items.DUST_HYDRATED_CHARCOAL, 8), "CCC", "CWC", "CCC", 'C', IC2Tags.DUST_CHARCOAL, 'W', Fluids.WATER
      );
      recipes.addShapelessIC2Recipe("hydrated_coal_cell", new ItemStack(IC2Items.CELL_HYDRATED_COAL), IC2Items.CELL_EMPTY, IC2Items.HYDRATED_COAL_COMPRESSED);
      recipes.addShapelessIC2Recipe("bio_cell", new ItemStack(IC2Items.CELL_BIO), IC2Items.CELL_EMPTY, IC2Items.PLANT_BALL_COMPRESSED);
      recipes.addShapedIC2Recipe("coin_to_8x", new ItemStack(IC2Items.COIN_8X), "XXX", "X X", "XXX", 'X', IC2Items.COIN);
      recipes.addShapedIC2Recipe("8x_to_64x", new ItemStack(IC2Items.COIN_64X), "XXX", "X X", "XXX", 'X', IC2Items.COIN_8X);
      recipes.addShapedIC2Recipe("64x_to_512x", new ItemStack(IC2Items.COIN_512X), "XXX", "X X", "XXX", 'X', IC2Items.COIN_64X);
      recipes.addShapedIC2Recipe("512x_to_4096x", new ItemStack(IC2Items.COIN_4096X), "XXX", "X X", "XXX", 'X', IC2Items.COIN_512X);
      recipes.addShapedIC2Recipe("4096x_to_32768x", new ItemStack(IC2Items.COIN_32768X), "XXX", "X X", "XXX", 'X', IC2Items.COIN_4096X);
      recipes.addShapelessIC2Recipe("8x_to_coin", new ItemStack(IC2Items.COIN, 8), IC2Items.COIN_8X);
      recipes.addShapelessIC2Recipe("64x_to_8x", new ItemStack(IC2Items.COIN_8X, 8), IC2Items.COIN_64X);
      recipes.addShapelessIC2Recipe("512x_to_64x", new ItemStack(IC2Items.COIN_64X, 8), IC2Items.COIN_512X);
      recipes.addShapelessIC2Recipe("4096x_to_512x", new ItemStack(IC2Items.COIN_512X, 8), IC2Items.COIN_4096X);
      recipes.addShapelessIC2Recipe("32768x_to_4096x", new ItemStack(IC2Items.COIN_4096X, 8), IC2Items.COIN_32768X);
      recipes.addShapedIC2Recipe(
         "tfbp",
         new ItemStack(IC2Items.TFBP),
         " C ",
         " A ",
         "R R",
         'C',
         IC2Items.CIRCUIT,
         'A',
         IC2Items.ADVANCED_CIRCUIT,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE
      );
      recipes.addShapedIC2Recipe("tfbp_cultivation", new ItemStack(IC2Items.TFBP_CULTIVATION), " S ", "S#S", " S ", '#', IC2Items.TFBP, 'S', Items.WHEAT_SEEDS);
      recipes.addShapedIC2Recipe(
         "tfbp_desertification", new ItemStack(IC2Items.TFBP_DESERTIFICATION), " S ", "S#S", " S ", '#', IC2Items.TFBP, 'S', Blocks.SAND
      );
      recipes.addShapedIC2Recipe("tfbp_chilling", new ItemStack(IC2Items.TFBP_CHILLING), " W ", "W#W", " W ", '#', IC2Items.TFBP, 'W', Items.SNOWBALL);
      recipes.addShapedIC2Recipe("tfbp_irrigation", new ItemStack(IC2Items.TFBP_IRRIGATION), " S ", "S#S", " S ", '#', IC2Items.TFBP, 'S', Items.WATER_BUCKET);
      recipes.addShapedIC2Recipe("tfbp_flatificator", new ItemStack(IC2Items.TFBP_FLATIFICATOR), " D ", "D#D", " D ", '#', IC2Items.TFBP, 'D', Blocks.DIRT);
      recipes.addShapedIC2Recipe(
         "tfbp_mushroom", new ItemStack(IC2Items.TFBP_MUSHROOM), "mMm", "M#M", "mMm", '#', IC2Items.TFBP, 'M', Blocks.BROWN_MUSHROOM, 'm', Blocks.MYCELIUM
      );
      recipes.addShapelessIC2Recipe("tfbp_cultivation_uncrafting", new ItemStack(IC2Items.TFBP), IC2Items.TFBP_CULTIVATION);
      recipes.addShapelessIC2Recipe("tfbp_irrigation_uncrafting", new ItemStack(IC2Items.TFBP), IC2Items.TFBP_IRRIGATION);
      recipes.addShapelessIC2Recipe("tfbp_chilling_uncrafting", new ItemStack(IC2Items.TFBP), IC2Items.TFBP_CHILLING);
      recipes.addShapelessIC2Recipe("tfbp_desertification_uncrafting", new ItemStack(IC2Items.TFBP), IC2Items.TFBP_DESERTIFICATION);
      recipes.addShapelessIC2Recipe("tfbp_flatificator_uncrafting", new ItemStack(IC2Items.TFBP), IC2Items.TFBP_FLATIFICATOR);
      recipes.addShapelessIC2Recipe("tfbp_mushroom_uncrafting", new ItemStack(IC2Items.TFBP), IC2Items.TFBP_MUSHROOM);
      recipes.addShapedIC2Recipe(
         "tfbp_biome",
         new ItemStack(IC2Items.TFBP_BIOME),
         " X ",
         " X ",
         "Y Y",
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'Y',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE
      );
      recipes.addShapelessIC2Recipe("tfbp_biome_cultivation", new ItemStack(IC2Items.TFBP_BIOME_CULTIVATION), IC2Items.TFBP_BIOME, IC2Items.TFBP_CULTIVATION);
      recipes.addShapelessIC2Recipe(
         "tfbp_biome_desertification", new ItemStack(IC2Items.TFBP_BIOME_DESERTIFICATION), IC2Items.TFBP_BIOME, IC2Items.TFBP_DESERTIFICATION
      );
      recipes.addShapedIC2Recipe(
         "tfbp_biome_forestification",
         new ItemStack(IC2Items.TFBP_BIOME_FORESTIFICATION),
         "X X",
         " C ",
         "XVX",
         'C',
         IC2Items.TFBP_BIOME,
         'V',
         IC2Items.TFBP_CULTIVATION,
         'X',
         ItemTags.SAPLINGS
      );
      recipes.addShapelessIC2Recipe("tfbp_biome_irrigation", new ItemStack(IC2Items.TFBP_BIOME_IRRIGATION), IC2Items.TFBP_BIOME, IC2Items.TFBP_CHILLING);
      recipes.addShapelessIC2Recipe("tfbp_biome_mushroom", new ItemStack(IC2Items.TFBP_BIOME_MUSHROOM), IC2Items.TFBP_BIOME, IC2Items.TFBP_MUSHROOM);
      recipes.addShapedIC2Recipe(
         "tfbp_biome_undergrowth",
         new ItemStack(IC2Items.TFBP_BIOME_UNDERGROWTH),
         "X X",
         " C ",
         "X X",
         'C',
         IC2Items.TFBP_BIOME_FORESTIFICATION,
         'X',
         Blocks.VINE
      );
      recipes.addShapelessIC2Recipe("tfbp_biome_chilling", new ItemStack(IC2Items.TFBP_BIOME_CHILLING), IC2Items.TFBP_BIOME, IC2Items.TFBP_IRRIGATION);
      recipes.addShapelessIC2Recipe("tfbp_biome_cultivation_uncrafting", new ItemStack(IC2Items.TFBP_BIOME), IC2Items.TFBP_BIOME_CULTIVATION);
      recipes.addShapelessIC2Recipe("tfbp_biome_desertification_uncrafting", new ItemStack(IC2Items.TFBP_BIOME), IC2Items.TFBP_BIOME_DESERTIFICATION);
      recipes.addShapelessIC2Recipe("tfbp_biome_forestification_uncrafting", new ItemStack(IC2Items.TFBP_BIOME), IC2Items.TFBP_BIOME_FORESTIFICATION);
      recipes.addShapelessIC2Recipe("tfbp_biome_irrigation_uncrafting", new ItemStack(IC2Items.TFBP_BIOME), IC2Items.TFBP_BIOME_IRRIGATION);
      recipes.addShapelessIC2Recipe("tfbp_biome_mushroom_uncrafting", new ItemStack(IC2Items.TFBP_BIOME), IC2Items.TFBP_BIOME_MUSHROOM);
      recipes.addShapelessIC2Recipe("tfbp_biome_undergrowth_uncrafting", new ItemStack(IC2Items.TFBP_BIOME), IC2Items.TFBP_BIOME_UNDERGROWTH);
      recipes.addShapelessIC2Recipe("tfbp_biome_chilling_uncrafting", new ItemStack(IC2Items.TFBP_BIOME), IC2Items.TFBP_BIOME_CHILLING);
      recipes.addShapelessIC2Recipe("miner_remote", new ItemStack(IC2Items.MINER_REMOTE), IC2Items.COMPLEX_CIRCUIT, IC2Items.GOLD_CABLE);
      recipes.addShapelessIC2Recipe("miner_remote_bronze", new ItemStack(IC2Items.MINER_REMOTE), IC2Items.COMPLEX_CIRCUIT, IC2Items.BRONZE_CABLE_2X_INSULATED);
      recipes.addShapedIC2Recipe("copper_sign", new ItemStack(IC2Blocks.COPPER_SIGN), "XXX", "XXX", " C ", 'C', Items.STICK, 'X', IC2Tags.INGOT_COPPER);
      recipes.addShapedIC2Recipe("silver_sign", new ItemStack(IC2Blocks.SILVER_SIGN), "XXX", "XXX", " C ", 'C', Items.STICK, 'X', IC2Tags.INGOT_SILVER);
      recipes.addShapedIC2Recipe("tin_sign", new ItemStack(IC2Blocks.TIN_SIGN), "XXX", "XXX", " C ", 'C', Items.STICK, 'X', IC2Tags.INGOT_TIN);
      recipes.addShapedIC2Recipe("bronze_sign", new ItemStack(IC2Blocks.BRONZE_SIGN), "XXX", "XXX", " C ", 'C', Items.STICK, 'X', IC2Tags.INGOT_BRONZE);
      recipes.addShapedIC2Recipe(
         "refined_iron_sign", new ItemStack(IC2Blocks.REFINED_IRON_SIGN), "XXX", "XXX", " C ", 'C', Items.STICK, 'X', IC2Tags.INGOT_REFINED_IRON
      );
      recipes.addShapedIC2Recipe("aluminum_sign", new ItemStack(IC2Blocks.ALUMINIUM_SIGN), "XXX", "XXX", " C ", 'C', Items.STICK, 'X', IC2Tags.INGOT_ALUMINIUM);
      recipes.addShapedIC2Recipe("wooden_rotor", new ItemStack(IC2Items.WOODEN_ROTOR), " S ", "SIS", " S ", 'S', Items.STICK, 'I', IC2Tags.INGOT_REFINED_IRON);
      recipes.addShapedIC2Recipe(
         "wool_rotor", new ItemStack(IC2Items.WOOL_ROTOR), "SWS", "WRW", "SWS", 'S', Items.STRING, 'W', ItemTags.WOOL, 'R', IC2Items.WOODEN_ROTOR
      );
      recipes.addShapedIC2Recipe(
         "iron_rotor", new ItemStack(IC2Items.IRON_ROTOR), "XXX", "XYX", "XXX", 'Y', IC2Blocks.MACHINE_BLOCK, 'X', IC2Tags.INGOT_REFINED_IRON
      );
      recipes.addShapedIC2Recipe(
         "carbon_rotor", new ItemStack(IC2Items.CARBON_ROTOR), "CCC", "CMC", "CCC", 'C', IC2Items.CARBON_PLATE, 'M', IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "alloy_rotor", new ItemStack(IC2Items.MIXED_ALLOY_ROTOR), "AAA", "AMA", "AAA", 'A', IC2Items.INGOT_ADVANCED_ALLOY, 'M', IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "iridium_rotor", new ItemStack(IC2Items.IRIDIUM_ROTOR), "III", "IRI", "III", 'I', IC2Items.PLATE_IRIDIUM, 'R', IC2Items.CARBON_ROTOR
      );
      recipes.addShapedIC2Recipe("batbox_minecart", new ItemStack(IC2Items.BATBOX_MINECART), "X", "Y", 'X', IC2Blocks.BATBOX, 'Y', Items.MINECART);
      recipes.addShapedIC2Recipe("mfe_minecart", new ItemStack(IC2Items.MFE_MINECART), "X", "Y", 'X', IC2Blocks.MFE, 'Y', Items.MINECART);
      recipes.addShapedIC2Recipe("mfsu_minecart", new ItemStack(IC2Items.MFSU_MINECART), "X", "Y", 'X', IC2Blocks.MFSU, 'Y', Items.MINECART);
      recipes.addShapedIC2Recipe("esu_minecart", new ItemStack(IC2Items.ESU_MINECART), "X", "Y", 'X', IC2Blocks.ESU, 'Y', Items.MINECART);
      recipes.addShapedIC2Recipe("isu_minecart", new ItemStack(IC2Items.ISU_MINECART), "X", "Y", 'X', IC2Blocks.ISU, 'Y', Items.MINECART);
      recipes.addShapedIC2Recipe("pesu_minecart", new ItemStack(IC2Items.PESU_MINECART), "X", "Y", 'X', IC2Blocks.PESU, 'Y', Items.MINECART);
   }

   private static void addGenerators(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "generator_0",
         new ItemStack(IC2Blocks.GENERATOR),
         " B ",
         "III",
         " F ",
         'B',
         IC2Items.RE_BATTERY,
         'I',
         IC2Tags.INGOT_REFINED_IRON,
         'F',
         IC2Blocks.IRON_FURNACE
      );
      recipes.addShapedIC2Recipe(
         "generator_1", new ItemStack(IC2Blocks.GENERATOR), " B ", " M ", " F ", 'B', IC2Items.RE_BATTERY, 'M', IC2Blocks.MACHINE_BLOCK, 'F', Items.FURNACE
      );
      recipes.addShapedIC2Recipe(
         "geothermal",
         new ItemStack(IC2Blocks.GEOTHERMAL_GENERATOR),
         "XCX",
         "XCX",
         "IGI",
         'X',
         net.minecraftforge.common.Tags.Items.GLASS,
         'C',
         IC2Items.CELL_EMPTY,
         'I',
         IC2Tags.INGOT_REFINED_IRON,
         'G',
         IC2Blocks.GENERATOR
      );
      recipes.addShapedIC2Recipe(
         "solar_panel",
         new ItemStack(IC2Blocks.SOLAR_PANEL),
         "XYX",
         "YXY",
         "CVC",
         'C',
         IC2Items.CIRCUIT,
         'V',
         IC2Blocks.GENERATOR,
         'X',
         IC2Tags.DUST_COAL_AND_CHARCOAL,
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS
      );
      recipes.addShapedIC2Recipe(
         "water_mill", new ItemStack(IC2Blocks.WATER_MILL, 2), "XYX", "YCY", "XYX", 'X', Items.STICK, 'Y', ItemTags.PLANKS, 'C', IC2Blocks.GENERATOR
      );
      recipes.addShapedIC2Recipe(
         "wind_turbine", new ItemStack(IC2Blocks.WIND_TURBINE), "X X", " Y ", "X X", 'Y', IC2Blocks.GENERATOR, 'X', IC2Tags.INGOT_REFINED_IRON
      );
      recipes.addShapedIC2Recipe(
         "nuclear_reactor",
         new ItemStack(IC2Blocks.NUCLEAR_REACTOR),
         " X ",
         "YYY",
         " G ",
         'G',
         IC2Blocks.GENERATOR,
         'Y',
         IC2Blocks.REACTOR_CHAMBER,
         'X',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "steam_reactor",
         new ItemStack(IC2Blocks.STEAM_REACTOR),
         "AXA",
         "YYY",
         "ACA",
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'Y',
         IC2Blocks.STEAM_REACTOR_CHAMBER,
         'C',
         IC2Blocks.NUCLEAR_REACTOR
      );
      recipes.addShapedIC2Recipe(
         "steam_turbine",
         new ItemStack(IC2Blocks.STEAM_TURBINE),
         " A ",
         "XYC",
         " B ",
         'A',
         IC2Items.CIRCUIT,
         'X',
         IC2Items.TURBINE_BLADE,
         'Y',
         IC2Blocks.GENERATOR,
         'C',
         IC2Blocks.WATER_MILL,
         'B',
         ItemListInput.createItemList(IC2Items.BRONZE_CABLE_2X_INSULATED, IC2Items.GOLD_CABLE_2X_INSULATED)
      );
      recipes.addShapedIC2Recipe(
         "reactor_chamber", new ItemStack(IC2Blocks.REACTOR_CHAMBER), " X ", "XYX", " X ", 'X', IC2Items.PLATE_DENSE_COPPER, 'Y', IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "steam_chamber",
         new ItemStack(IC2Blocks.STEAM_REACTOR_CHAMBER),
         "XYX",
         "YCY",
         "XYX",
         'Y',
         IC2Blocks.REINFORCED_GLASS,
         'C',
         IC2Blocks.REACTOR_CHAMBER,
         'X',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "thermal_generator",
         new ItemStack(IC2Blocks.THERMAL_GENERATOR),
         "XCX",
         "CBC",
         "XNX",
         'X',
         IC2Tags.INGOT_REFINED_IRON,
         'C',
         Items.MAGMA_BLOCK,
         'B',
         IC2Blocks.GEOTHERMAL_GENERATOR,
         'N',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "solar_turbine",
         new ItemStack(IC2Blocks.SOLAR_TURBINE),
         "XCX",
         "VBV",
         "XYX",
         'X',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'C',
         IC2Blocks.SOLAR_PANEL,
         'V',
         IC2Blocks.PUMP,
         'B',
         IC2Items.TURBINE_BLADE,
         'Y',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "liquid_fuel_generator",
         new ItemStack(IC2Blocks.LIQUID_FUEL_GENERATOR),
         "XYX",
         "YCY",
         "XVX",
         'X',
         IC2Tags.INGOT_SILVER,
         'Y',
         net.minecraftforge.common.Tags.Items.GLASS,
         'C',
         IC2Items.TURBINE_BLADE,
         'V',
         IC2Blocks.GENERATOR
      );
      recipes.addShapedIC2Recipe(
         "slag_generator",
         new ItemStack(IC2Blocks.SLAG_GENERATOR),
         "XYX",
         "XCX",
         "XVX",
         'X',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'Y',
         IC2Blocks.GENERATOR,
         'C',
         IC2Blocks.RECYCLER,
         'V',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "wave_generator",
         new ItemStack(IC2Blocks.WAVE_GENERATOR),
         "XYB",
         "XVC",
         "XYB",
         'X',
         IC2Tags.INGOT_REFINED_IRON,
         'Y',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'V',
         IC2Items.TURBINE_BLADE,
         'B',
         IC2Items.CIRCUIT,
         'C',
         IC2Blocks.WATER_MILL
      );
      recipes.addShapedIC2Recipe(
         "ocean_generator",
         new ItemStack(IC2Blocks.OCEAN_GENERATOR),
         "XYX",
         "CVC",
         "XYX",
         'X',
         IC2Tags.INGOT_REFINED_IRON,
         'Y',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'C',
         IC2Items.TURBINE_BLADE,
         'V',
         IC2Blocks.WATER_MILL
      );
      recipes.addShapedIC2Recipe(
         "fuel_boiler",
         new ItemStack(IC2Blocks.FUEL_BOILER),
         "XYX",
         "CVC",
         "BNB",
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'Y',
         IC2Items.FLUID_EXPORT_UPGRADE_SIMPLE,
         'C',
         IC2Items.PLATING_HEAT_CAPACITY,
         'V',
         IC2Blocks.PERSONAL_TANK,
         'B',
         IC2Items.PLATE_IRIDIUM,
         'N',
         IC2Blocks.FUEL_BOILER_MULTIBLOCK
      );
      recipes.addShapedIC2Recipe(
         "fuel_boiler_structure",
         new ItemStack(IC2Blocks.FUEL_BOILER_MULTIBLOCK, 8),
         "XYX",
         "CVC",
         " B ",
         'B',
         new ItemStack(IC2Blocks.IRON_FURNACE, 4),
         'V',
         IC2Blocks.MACHINE_TANK,
         'C',
         IC2Items.PLATING_HEAT_CAPACITY,
         'X',
         new ItemStack(IC2Items.CIRCUIT, 2),
         'Y',
         IC2Items.FLUID_EXPORT_UPGRADE_SIMPLE
      );
      recipes.addShapedIC2Recipe(
         "windmill",
         new ItemStack(IC2Blocks.WINDMILL),
         " X ",
         "YCV",
         " B ",
         'X',
         IC2Items.COMPLEX_CIRCUIT,
         'Y',
         IC2Blocks.TRANSFORMER_HV,
         'C',
         IC2Blocks.WIND_TURBINE,
         'V',
         IC2Blocks.IRON_FENCE,
         'B',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "steam_tunnel",
         new ItemStack(IC2Blocks.STEAM_TUNNEL),
         "XYX",
         "YCY",
         "VYV",
         'X',
         IC2Items.CIRCUIT,
         'Y',
         IC2Blocks.STEAM_TURBINE,
         'C',
         IC2Blocks.STABILIZED_MACHINE_BLOCK,
         'V',
         IC2Blocks.MACHINE_TANK
      );
      recipes.addShapedIC2Recipe(
         "lv_solar_panel", new ItemStack(IC2Blocks.SOLAR_PANEL_LV), "XXX", "XYX", "XXX", 'X', IC2Blocks.SOLAR_PANEL, 'Y', IC2Blocks.TRANSFORMER_LV
      );
      recipes.addShapedIC2Recipe(
         "mv_solar_panel", new ItemStack(IC2Blocks.SOLAR_PANEL_MV), "XXX", "XYX", "XXX", 'X', IC2Blocks.SOLAR_PANEL_LV, 'Y', IC2Blocks.TRANSFORMER_MV
      );
      recipes.addShapedIC2Recipe(
         "hv_solar_panel", new ItemStack(IC2Blocks.SOLAR_PANEL_HV), "XXX", "XYX", "XXX", 'X', IC2Blocks.SOLAR_PANEL_MV, 'Y', IC2Blocks.TRANSFORMER_IV
      );
      recipes.addShapedIC2Recipe(
         "lv_solar_panel_compressed",
         new ItemStack(IC2Blocks.SOLAR_PANEL_LV_COMPRESSED),
         "XXX",
         "XYX",
         "XXX",
         'X',
         IC2Blocks.SOLAR_PANEL_COMPRESSED,
         'Y',
         IC2Blocks.TRANSFORMER_LV
      );
      recipes.addShapedIC2Recipe(
         "mv_solar_panel_compressed",
         new ItemStack(IC2Blocks.SOLAR_PANEL_MV_COMPRESSED),
         "XXX",
         "XYX",
         "XXX",
         'X',
         IC2Blocks.SOLAR_PANEL_LV_COMPRESSED,
         'Y',
         IC2Blocks.TRANSFORMER_MV
      );
      recipes.addShapedIC2Recipe(
         "hv_solar_panel_compressed",
         new ItemStack(IC2Blocks.SOLAR_PANEL_HV_COMPRESSED),
         "XXX",
         "XYX",
         "XXX",
         'X',
         IC2Blocks.SOLAR_PANEL_MV_COMPRESSED,
         'Y',
         IC2Blocks.TRANSFORMER_IV
      );
      recipes.addShapedIC2Recipe(
         "lv_water_mill", new ItemStack(IC2Blocks.WATER_MILL_LV), "XXX", "XYX", "XXX", 'X', IC2Blocks.WATER_MILL, 'Y', IC2Blocks.TRANSFORMER_LV
      );
      recipes.addShapedIC2Recipe(
         "mv_water_mill", new ItemStack(IC2Blocks.WATER_MILL_MV), "XXX", "XYX", "XXX", 'X', IC2Blocks.WATER_MILL_LV, 'Y', IC2Blocks.TRANSFORMER_MV
      );
      recipes.addShapedIC2Recipe(
         "hv_water_mill", new ItemStack(IC2Blocks.WATER_MILL_HV), "XXX", "XYX", "XXX", 'X', IC2Blocks.WATER_MILL_MV, 'Y', IC2Blocks.TRANSFORMER_HV
      );
      recipes.addShapedIC2Recipe(
         "lv_flux_generator",
         new ItemStack(IC2Blocks.FLUX_GENERATOR_LV),
         " X ",
         "YCV",
         " X ",
         'X',
         IC2Tags.INGOT_COPPER,
         'C',
         net.minecraftforge.common.Tags.Items.INGOTS_IRON,
         'Y',
         IC2Blocks.TRANSFORMER_LV,
         'V',
         Items.REDSTONE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "mv_flux_generator_0",
         new ItemStack(IC2Blocks.FLUX_GENERATOR_MV),
         "DXD",
         "YCV",
         "DXD",
         'D',
         IC2Items.CIRCUIT,
         'X',
         IC2Tags.INGOT_COPPER,
         'C',
         net.minecraftforge.common.Tags.Items.INGOTS_IRON,
         'Y',
         IC2Blocks.TRANSFORMER_MV,
         'V',
         Items.REDSTONE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "mv_flux_generator_1",
         new ItemStack(IC2Blocks.FLUX_GENERATOR_MV),
         " X ",
         "YCV",
         " X ",
         'X',
         IC2Items.CIRCUIT,
         'Y',
         IC2Blocks.TRANSFORMER_MV,
         'V',
         Items.REDSTONE_BLOCK,
         'C',
         IC2Blocks.FLUX_GENERATOR_LV
      );
      recipes.addShapedIC2Recipe(
         "hv_flux_generator_0",
         new ItemStack(IC2Blocks.FLUX_GENERATOR_HV),
         "DXD",
         "YCV",
         "DXD",
         'D',
         IC2Items.ADVANCED_CIRCUIT,
         'X',
         IC2Tags.INGOT_COPPER,
         'C',
         net.minecraftforge.common.Tags.Items.INGOTS_IRON,
         'Y',
         IC2Blocks.TRANSFORMER_HV,
         'V',
         Items.REDSTONE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "hv_flux_generator_1",
         new ItemStack(IC2Blocks.FLUX_GENERATOR_HV),
         " X ",
         "YCV",
         " X ",
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'Y',
         IC2Blocks.TRANSFORMER_HV,
         'V',
         Items.REDSTONE_BLOCK,
         'C',
         IC2Blocks.FLUX_GENERATOR_MV
      );
   }

   private static void addNVMachines(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "iron_furnace", new ItemStack(IC2Blocks.IRON_FURNACE), "XXX", "X X", "XXX", 'X', net.minecraftforge.common.Tags.Items.INGOTS_IRON
      );
      recipes.addShapedIC2Recipe(
         "iron_furnace_upgrade",
         new ItemStack(IC2Blocks.IRON_FURNACE),
         " X ",
         "X X",
         "XYX",
         'X',
         net.minecraftforge.common.Tags.Items.INGOTS_IRON,
         'Y',
         Items.FURNACE
      );
      recipes.addShapedIC2Recipe(
         "stone_macerator",
         new ItemStack(IC2Blocks.STONE_MACERATOR),
         "XXX",
         "YCY",
         "YVY",
         'Y',
         net.minecraftforge.common.Tags.Items.COBBLESTONE,
         'X',
         Items.FLINT,
         'C',
         Items.PISTON,
         'V',
         Items.FURNACE
      );
      recipes.addShapedIC2Recipe(
         "industrial_worktable",
         new ItemStack(IC2Blocks.INDUSTRIAL_WORKBENCH),
         new EmptyInventoryModifier(IC2Items.TOOL_BOX_CARBON),
         "AXA",
         "YCY",
         "AVA",
         'A',
         Items.HOPPER,
         'X',
         new ItemStack(IC2Items.CIRCUIT, 4),
         'V',
         IC2Blocks.MACHINE_BLOCK,
         'C',
         new ItemStack(Items.CRAFTING_TABLE, 28),
         'Y',
         IC2Items.TOOL_BOX_CARBON
      );
      recipes.addShapedIC2Recipe(
         "wood_gassifier",
         new ItemStack(IC2Blocks.WOOD_GASSIFIER),
         "XYX",
         "XCX",
         "BVB",
         'B',
         Items.SMOOTH_STONE,
         'X',
         Items.STONE,
         'Y',
         IC2Items.CELL_EMPTY,
         'C',
         IC2Blocks.EXTRACTOR,
         'V',
         IC2Blocks.IRON_FURNACE
      );
      recipes.addShapedIC2Recipe(
         "armor_configurator",
         new ItemStack(IC2Blocks.ARMOR_CONFIGURATOR),
         "XYX",
         "XVX",
         " B ",
         'X',
         IC2Items.CARBON_PLATE,
         'Y',
         Blocks.CRAFTING_TABLE,
         'V',
         IC2Items.CIRCUIT,
         'B',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "stone_canner",
         new ItemStack(IC2Blocks.STONE_CANNER),
         "XCX",
         "XYX",
         "VVV",
         'V',
         Items.SMOOTH_STONE,
         'X',
         Items.STONE,
         'C',
         Items.CRAFTING_TABLE,
         'Y',
         Items.FURNACE
      );
      recipes.addShapedIC2Recipe(
         "storage_expansion",
         new ItemStack(IC2Blocks.STORAGE_EXPANSION),
         " X ",
         "VYV",
         " C ",
         'X',
         Items.HOPPER,
         'V',
         IC2Items.CIRCUIT,
         'Y',
         Items.CHEST,
         'C',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapelessIC2Recipe(
         "trash_expansion", new ItemStack(IC2Blocks.BUFFER_STORAGE_EXPANSION), IC2Blocks.STORAGE_EXPANSION, IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "memory_expansion",
         new ItemStack(IC2Blocks.MEMORY_EXPANSION),
         "XCX",
         "VBV",
         "XYX",
         'X',
         IC2Items.CIRCUIT,
         'C',
         Items.CRAFTING_TABLE,
         'V',
         Items.CHEST,
         'B',
         IC2Items.MEMORY_STICK,
         'Y',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "fluid_expansion",
         new ItemStack(IC2Blocks.FLUID_EXPANSION),
         "XCX",
         "CVC",
         "XCX",
         'X',
         IC2Items.CIRCUIT,
         'C',
         IC2Blocks.MACHINE_TANK,
         'V',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "uu_expansion",
         new ItemStack(IC2Blocks.UU_EXPANSION),
         "XCX",
         "VBV",
         "XYX",
         'X',
         IC2Items.PLATE_IRIDIUM,
         'C',
         IC2Blocks.INDUSTRIAL_WORKBENCH,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.MASS_FABRICATOR,
         'Y',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "tank",
         new ItemStack(IC2Blocks.TANK),
         "XXX",
         "XYX",
         "XXX",
         'X',
         IC2Blocks.REINFORCED_STONE,
         'Y',
         ItemListInput.createItemList(IC2Blocks.MACHINE_TANK, IC2Blocks.PERSONAL_TANK)
      );
      recipes.addShapedIC2Recipe("valve", new ItemStack(IC2Blocks.VALVE), " X ", "XYX", " X ", 'X', IC2Blocks.PIPE, 'Y', IC2Blocks.MACHINE_BLOCK);
      recipes.addShapedIC2Recipe(
         "pushing_valve",
         new ItemStack(IC2Blocks.VALVE_PUSHING),
         "X",
         "Y",
         "C",
         'X',
         IC2Items.CIRCUIT,
         'Y',
         ItemListInput.createItemList(IC2Blocks.VALVE, IC2Blocks.TANK_ANIMATED),
         'C',
         IC2Items.FLUID_EXPORT_UPGRADE_SIMPLE
      );
      recipes.addShapedIC2Recipe(
         "animated_valve",
         new ItemStack(IC2Blocks.TANK_ANIMATED),
         "XXX",
         "XYX",
         "XXX",
         'X',
         net.minecraftforge.common.Tags.Items.GLASS,
         'Y',
         ItemListInput.createItemList(IC2Blocks.VALVE, IC2Blocks.VALVE_PUSHING)
      );
      recipes.addShapedIC2Recipe(
         "teleport_valve",
         new ItemStack(IC2Blocks.VALVE_TELEPORTING, 2),
         " X ",
         "YCY",
         " X ",
         'X',
         net.minecraftforge.common.Tags.Items.GEMS_DIAMOND,
         'Y',
         Items.ENDER_PEARL,
         'C',
         new ItemStack(IC2Blocks.VALVE, 2)
      );
      recipes.addShapedIC2Recipe(
         "sound_beacon",
         new ItemStack(IC2Blocks.SOUND_BEACON, 4),
         "XYX",
         "XCX",
         "XXX",
         'X',
         ItemTags.WOOL,
         'Y',
         IC2Items.FREQUENCY_TRANSMITTER,
         'C',
         IC2Blocks.MACHINE_BUFFER
      );
   }

   private static void addMachinesLV(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "electric_furnace",
         new ItemStack(IC2Blocks.ELECTRIC_FURNACE),
         " X ",
         "YCY",
         'X',
         IC2Items.CIRCUIT,
         'Y',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Blocks.IRON_FURNACE
      );
      recipes.addShapedIC2Recipe(
         "electric_furnace_upgrade",
         new ItemStack(IC2Blocks.ELECTRIC_FURNACE),
         " X ",
         "YCY",
         " V ",
         'X',
         IC2Items.CIRCUIT,
         'Y',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         Blocks.FURNACE,
         'V',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "electric_blast_furnace_upgrade",
         new ItemStack(IC2Blocks.ELECTRIC_BLAST_FURNACE),
         " X ",
         "YCY",
         " V ",
         'X',
         IC2Items.CIRCUIT,
         'Y',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         Blocks.BLAST_FURNACE,
         'V',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "electric_smoker_upgrade",
         new ItemStack(IC2Blocks.ELECTRIC_SMOKER),
         " X ",
         "YCY",
         " V ",
         'X',
         IC2Items.CIRCUIT,
         'Y',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         Blocks.SMOKER,
         'V',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "macerator_upgrade",
         new ItemStack(IC2Blocks.MACERATOR),
         "XXX",
         "XYX",
         "XCX",
         'X',
         IC2Tags.INGOT_REFINED_IRON,
         'Y',
         IC2Blocks.STONE_MACERATOR,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "macerator",
         new ItemStack(IC2Blocks.MACERATOR),
         "XXX",
         "CVC",
         " Y ",
         'Y',
         IC2Items.CIRCUIT,
         'V',
         IC2Blocks.MACHINE_BLOCK,
         'C',
         net.minecraftforge.common.Tags.Items.COBBLESTONE,
         'X',
         Items.FLINT
      );
      recipes.addShapedIC2Recipe(
         "extractor", new ItemStack(IC2Blocks.EXTRACTOR), "TMT", "TCT", 'T', IC2Items.TREETAP, 'M', IC2Blocks.MACHINE_BLOCK, 'C', IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "compressor",
         new ItemStack(IC2Blocks.COMPRESSOR),
         "S S",
         "SMS",
         "SXS",
         'S',
         net.minecraftforge.common.Tags.Items.STONE,
         'M',
         IC2Blocks.MACHINE_BLOCK,
         'X',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "canner", new ItemStack(IC2Blocks.CANNER), "TCT", "TMT", "TTT", 'T', IC2Tags.INGOT_TIN, 'M', IC2Blocks.MACHINE_BLOCK, 'C', IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "canner_upgrade", new ItemStack(IC2Blocks.CANNER), "TCT", "TMT", "TTT", 'T', IC2Tags.INGOT_TIN, 'M', IC2Blocks.STONE_CANNER, 'C', IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "recycler",
         new ItemStack(IC2Blocks.RECYCLER),
         " G ",
         "DMD",
         "IDI",
         'D',
         Items.DIRT,
         'G',
         Items.GLOWSTONE_DUST,
         'M',
         IC2Blocks.COMPRESSOR,
         'I',
         IC2Tags.INGOT_REFINED_IRON
      );
      recipes.addShapedIC2Recipe(
         "electrolyzer",
         new ItemStack(IC2Blocks.ELECTROLYZER),
         "C C",
         "CXC",
         "YBY",
         'Y',
         IC2Items.CELL_EMPTY,
         'B',
         IC2Blocks.MACHINE_BLOCK,
         'X',
         IC2Items.CIRCUIT,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "magnetizer",
         new ItemStack(IC2Blocks.MAGNETIZER),
         "RFR",
         "RMR",
         "RFR",
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'F',
         IC2Blocks.IRON_FENCE,
         'M',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "pump",
         new ItemStack(IC2Blocks.PUMP),
         "XCX",
         "XDX",
         "YVY",
         'X',
         IC2Items.CELL_EMPTY,
         'C',
         IC2Items.CIRCUIT,
         'D',
         IC2Blocks.MACHINE_BLOCK,
         'Y',
         IC2Blocks.MINING_PIPE_SHAFT,
         'V',
         IC2Items.TREETAP
      );
      recipes.addShapedIC2Recipe(
         "miner", new ItemStack(IC2Blocks.MINER), "XYX", " C ", " C ", 'C', IC2Blocks.MINING_PIPE_SHAFT, 'X', IC2Items.CIRCUIT, 'Y', IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "electric_wood_gassifier",
         new ItemStack(IC2Blocks.ELECTRIC_WOOD_GASSIFIER),
         "XYX",
         "BCB",
         "XVX",
         'X',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'B',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Items.CELL_EMPTY,
         'C',
         IC2Blocks.EXTRACTOR,
         'V',
         IC2Blocks.ELECTRIC_FURNACE
      );
      recipes.addShapedIC2Recipe(
         "electric_wood_gassifier_upgrade",
         new ItemStack(IC2Blocks.ELECTRIC_WOOD_GASSIFIER),
         " X ",
         "CVC",
         " B ",
         'X',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'C',
         IC2Items.CARBON_PLATE,
         'V',
         IC2Blocks.WOOD_GASSIFIER,
         'B',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "rare_earth_extractor",
         new ItemStack(IC2Blocks.RARE_EARTH_EXTRACTOR),
         "XYX",
         "XCX",
         'X',
         Items.GLOWSTONE_DUST,
         'Y',
         IC2Blocks.EXTRACTOR,
         'C',
         IC2Blocks.RECYCLER
      );
      recipes.addShapedIC2Recipe(
         "machine_buffer",
         new ItemStack(IC2Blocks.MACHINE_BUFFER),
         new EmptyInventoryModifier(IC2Items.TOOL_BOX),
         " X ",
         "YCY",
         " V ",
         'X',
         IC2Items.UPGRADE_BASE,
         'Y',
         IC2Items.CIRCUIT,
         'C',
         IC2Items.TOOL_BOX,
         'V',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "machine_tank",
         new ItemStack(IC2Blocks.MACHINE_TANK),
         "XXX",
         "XYX",
         "XXX",
         'X',
         net.minecraftforge.common.Tags.Items.GLASS,
         'Y',
         IC2Blocks.MACHINE_BUFFER
      );
      recipes.addShapedIC2Recipe(
         "sawmill",
         new ItemStack(IC2Blocks.SAWMILL),
         "XYX",
         "CVC",
         "CBC",
         'C',
         IC2Tags.INGOT_BRONZE,
         'X',
         Items.STONE_AXE,
         'Y',
         IC2Items.TURBINE_BLADE,
         'V',
         IC2Blocks.MACHINE_BLOCK,
         'B',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "alloy_smelter",
         new ItemStack(IC2Blocks.ALLOY_SMELTER),
         "XYX",
         "XCX",
         'X',
         Blocks.BRICKS,
         'Y',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Blocks.ELECTRIC_FURNACE
      );
      recipes.addShapedIC2Recipe(
         "crop_analyzer_block",
         new ItemStack(IC2Blocks.CROP_ANALYZER),
         " C ",
         "VBV",
         'C',
         IC2Items.CROP_ANALYZER,
         'V',
         IC2Items.CIRCUIT,
         'B',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "simple_crop_library",
         new ItemStack(IC2Blocks.SIMPLE_CROP_LIBRARY),
         new EmptyInventoryModifier(IC2Items.TOOL_BOX),
         "XYX",
         "CVB",
         "NMN",
         'X',
         IC2Items.CROP_STICKS,
         'Y',
         new SubItemInput(IC2Items.CROP_SEED),
         'N',
         IC2Items.CIRCUIT,
         'M',
         IC2Blocks.MACHINE_BLOCK,
         'C',
         IC2Blocks.LUMINATOR,
         'V',
         IC2Items.TOOL_BOX,
         'B',
         Items.HOPPER
      );
   }

   private static void addMachinesMV(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "cropmatron",
         new ItemStack(IC2Blocks.CROP_MATRON),
         "XYX",
         "CVC",
         "CCC",
         'C',
         IC2Items.CROP_STICKS,
         'Y',
         net.minecraftforge.common.Tags.Items.CHESTS_WOODEN,
         'V',
         IC2Blocks.MACHINE_BLOCK,
         'X',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "advanced_machine_block_0",
         new ItemStack(IC2Blocks.ADVANCED_MACHINE_BLOCK),
         " X ",
         "YCY",
         " X ",
         'X',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'C',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "advanced_machine_block_1",
         new ItemStack(IC2Blocks.ADVANCED_MACHINE_BLOCK),
         " X ",
         "YCY",
         " X ",
         'Y',
         IC2Items.CARBON_PLATE,
         'X',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'C',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "induction_furnace",
         new ItemStack(IC2Blocks.INDUCTION_FURNACE),
         "XXX",
         "XYX",
         "XCX",
         'X',
         IC2Tags.INGOT_COPPER,
         'Y',
         IC2Blocks.ELECTRIC_FURNACE,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "induction_blast_furnace",
         new ItemStack(IC2Blocks.INDUCTION_BLAST_FURNACE),
         "XXX",
         "XYX",
         "XCX",
         'X',
         IC2Tags.INGOT_COPPER,
         'Y',
         IC2Blocks.ELECTRIC_BLAST_FURNACE,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "induction_smoker",
         new ItemStack(IC2Blocks.INDUCTION_SMOKER),
         "XXX",
         "XYX",
         "XCX",
         'X',
         IC2Tags.INGOT_COPPER,
         'Y',
         IC2Blocks.ELECTRIC_SMOKER,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "induction_furnace",
         new ItemStack(IC2Blocks.INDUCTION_FURNACE),
         "XXX",
         "XYX",
         "XCX",
         'X',
         IC2Tags.INGOT_COPPER,
         'Y',
         IC2Blocks.ELECTRIC_FURNACE,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "rotary_macerator_scrap",
         new ItemStack(IC2Blocks.ROTARY_MACERATOR),
         "XXX",
         "XYX",
         "XCX",
         'X',
         IC2Items.SCRAP_METAL_BLADE,
         'Y',
         IC2Blocks.MACERATOR,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "rotary_macerator_obsidian",
         new ItemStack(IC2Blocks.ROTARY_MACERATOR),
         "XXX",
         "XYX",
         "XCX",
         'X',
         new ItemStack(IC2Items.OBSIDIAN_BLADE, 2),
         'Y',
         IC2Blocks.MACERATOR,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "centrifugal_extractor",
         new ItemStack(IC2Blocks.CENTRIFUGAL_EXTRACTOR),
         "XXX",
         "XYX",
         "XCX",
         'X',
         IC2Items.CELL_ELECTROLYZED_WATER,
         'Y',
         IC2Blocks.EXTRACTOR,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "centrifugal_rare_earth_extractor",
         new ItemStack(IC2Blocks.CENTRIFUGAL_RARE_EARTH_EXTRACTOR),
         "XXX",
         "XYX",
         "XCX",
         'X',
         IC2Items.CELL_ELECTROLYZED_WATER,
         'Y',
         IC2Blocks.RARE_EARTH_EXTRACTOR,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "singularity_compressor",
         new ItemStack(IC2Blocks.SINGULARITY_COMPRESSOR),
         "XXX",
         "XYX",
         "XCX",
         'X',
         Items.OBSIDIAN,
         'Y',
         IC2Blocks.COMPRESSOR,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "vacuum_canner",
         new ItemStack(IC2Blocks.VACUUM_CANNER),
         "XPX",
         "XYX",
         "XCX",
         'P',
         IC2Blocks.PUMP,
         'X',
         IC2Items.CELL_AIR,
         'Y',
         IC2Blocks.CANNER,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "compacting_recycler",
         new ItemStack(IC2Blocks.COMPACTING_RECYCLER),
         "XXX",
         "XYX",
         "XCX",
         'X',
         Items.PISTON,
         'Y',
         IC2Blocks.RECYCLER,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "charged_electrolyzer",
         new ItemStack(IC2Blocks.CHARGED_ELECTROLYZER),
         "XXX",
         "XYX",
         "XCX",
         'X',
         IC2Blocks.TRANSFORMER_LV,
         'Y',
         IC2Blocks.ELECTROLYZER,
         'C',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "tesla_coil",
         new ItemStack(IC2Blocks.TESLA_COIL),
         "XXX",
         "XYX",
         "VCV",
         'X',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'Y',
         IC2Blocks.TRANSFORMER_MV,
         'C',
         IC2Items.CIRCUIT,
         'V',
         IC2Tags.INGOT_REFINED_IRON
      );
      recipes.addShapedIC2Recipe(
         "reactor_planner",
         new ItemStack(IC2Blocks.REACTOR_PLANNER),
         "XCX",
         "VBV",
         "XNX",
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         net.minecraftforge.common.Tags.Items.CHESTS_WOODEN,
         'B',
         IC2Blocks.NUCLEAR_REACTOR,
         'V',
         IC2Blocks.REACTOR_CHAMBER,
         'N',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "crop_harvester",
         new ItemStack(IC2Blocks.CROP_HARVESTER),
         "YXY",
         "CVC",
         "BNB",
         'X',
         IC2Items.CROP_ANALYZER,
         'Y',
         IC2Items.CIRCUIT,
         'C',
         new ItemStack(Items.HOPPER, 2),
         'V',
         net.minecraftforge.common.Tags.Items.CHESTS_WOODEN,
         'B',
         new ItemStack(IC2Blocks.MINING_PIPE_SHAFT, 8),
         'N',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "overclocked_pump",
         new ItemStack(IC2Blocks.OVERCLOCKED_PUMP),
         "XYX",
         "YCY",
         "XVX",
         'X',
         IC2Items.COOLANT_CELL_10K,
         'Y',
         IC2Items.OVERCLOCKER_UPGRADE,
         'C',
         IC2Blocks.PUMP,
         'V',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "ranged_pump",
         new ItemStack(IC2Blocks.RANGED_PUMP),
         "XYX",
         "CVC",
         "XBX",
         'X',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'C',
         IC2Items.ADVANCED_CIRCUIT,
         'Y',
         IC2Blocks.OVERCLOCKED_PUMP,
         'V',
         IC2Blocks.MINER,
         'B',
         IC2Items.MAGNET
      );
      recipes.addShapedIC2Recipe(
         "base_teleporter",
         new ItemStack(IC2Blocks.BASE_TELEPORTER),
         "XYX",
         "BCB",
         "XVX",
         'B',
         IC2Items.DUST_DIAMOND,
         'X',
         IC2Tags.INGOT_TIN,
         'Y',
         Items.ENDER_PEARL,
         'C',
         IC2Items.ADVANCED_CIRCUIT,
         'V',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "chunkloader",
         new ItemStack(IC2Blocks.CHUNKLOADER),
         " Y ",
         "CMC",
         "VBV",
         'Y',
         Items.ENDER_PEARL,
         'B',
         IC2Blocks.ADVANCED_MACHINE_BLOCK,
         'M',
         IC2Items.ENERGY_CRYSTAL,
         'C',
         IC2Items.PROXIMITY_PAD_UPGRADE,
         'V',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "slow_grinder",
         new ItemStack(IC2Blocks.SLOW_GRINDER),
         " X ",
         "YCY",
         " V ",
         'X',
         IC2Items.SAWBLADE_EFFICIENT_UPGRADE,
         'Y',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Blocks.RECYCLER,
         'V',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "refinery",
         new ItemStack(IC2Blocks.REFINERY),
         " X ",
         "VYV",
         "CBC",
         'X',
         IC2Items.COMPLEX_CIRCUIT,
         'V',
         Blocks.NETHER_BRICKS,
         'Y',
         IC2Blocks.ALLOY_SMELTER,
         'C',
         IC2Blocks.MACHINE_TANK,
         'B',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
   }

   private static void addMachinesHV(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "crop_library",
         new ItemStack(IC2Blocks.CROP_LIBRARY),
         new EmptyInventoryModifier(IC2Items.TOOL_BOX_CARBON),
         "XYX",
         "CVB",
         "NMN",
         'X',
         IC2Items.CROP_STICKS,
         'Y',
         IC2Items.LAPOTRON_CRYSTAL,
         'N',
         IC2Items.ADVANCED_CIRCUIT,
         'M',
         IC2Blocks.STABILIZED_MACHINE_BLOCK,
         'C',
         IC2Blocks.LUMINATOR,
         'V',
         IC2Items.TOOL_BOX_CARBON,
         'B',
         IC2Items.OBSCURATOR
      );
      recipes.addShapedIC2Recipe(
         "ore_scanner",
         new ItemStack(IC2Blocks.ORE_SCANNER),
         "VXV",
         "MYN",
         "BCB",
         'M',
         new ItemStack(IC2Blocks.LUMINATOR, 8),
         'N',
         IC2Blocks.BATBOX,
         'V',
         IC2Items.COOLANT_CELL_10K,
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'Y',
         IC2Blocks.STABILIZED_MACHINE_BLOCK,
         'C',
         new ItemStack(IC2Blocks.MINING_PIPE_SHAFT, 16)
      );
      recipes.addShapedIC2Recipe(
         "terraformer",
         new ItemStack(IC2Blocks.TERRAFORMER),
         "GTG",
         "DMD",
         "GDG",
         'T',
         IC2Items.TFBP,
         'G',
         Items.GLOWSTONE_DUST,
         'D',
         Blocks.DIRT,
         'M',
         IC2Blocks.STABILIZED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "mass_fabricator",
         new ItemStack(IC2Blocks.MASS_FABRICATOR),
         "GCG",
         "ALA",
         "GCG",
         'A',
         IC2Blocks.STABILIZED_MACHINE_BLOCK,
         'L',
         IC2Items.LAPOTRON_CRYSTAL,
         'G',
         Items.GLOWSTONE_DUST,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "teleporter",
         new ItemStack(IC2Blocks.TELEPORTER),
         "GFG",
         "CMC",
         "GDG",
         'M',
         IC2Blocks.STABILIZED_MACHINE_BLOCK,
         'C',
         IC2Items.GLASSFIBER_CABLE,
         'F',
         IC2Items.FREQUENCY_TRANSMITTER,
         'G',
         IC2Items.ADVANCED_CIRCUIT,
         'D',
         net.minecraftforge.common.Tags.Items.GEMS_DIAMOND
      );
      recipes.addShapedIC2Recipe(
         "uranium_enricher",
         new ItemStack(IC2Blocks.URANIUM_ENRICHER),
         "VVV",
         "XYC",
         "VBV",
         'X',
         IC2Blocks.EXTRACTOR,
         'Y',
         IC2Blocks.VACUUM_CANNER,
         'C',
         IC2Items.LAPOTRON_CRYSTAL,
         'B',
         IC2Blocks.STABILIZED_MACHINE_BLOCK,
         'V',
         IC2Tags.INGOT_SILVER
      );
      recipes.addShapedIC2Recipe(
         "electric_enchanter",
         new ItemStack(IC2Blocks.ELECTRIC_ENCHANTER),
         " X ",
         "YCY",
         "VBV",
         'X',
         Items.ANVIL,
         'Y',
         IC2Items.COMPLEX_CIRCUIT,
         'C',
         Items.ENCHANTING_TABLE,
         'V',
         Items.BOOKSHELF,
         'B',
         IC2Blocks.MFSU
      );
      recipes.addShapedIC2Recipe(
         "rocket_miner",
         new ItemStack(IC2Blocks.ROCKET_MINER),
         "YXC",
         "VBV",
         "NMN",
         'Y',
         Items.CHEST,
         'X',
         IC2Items.LAPOTRON_CRYSTAL,
         'C',
         IC2Blocks.PUMP,
         'V',
         IC2Items.COMPLEX_CIRCUIT,
         'B',
         IC2Blocks.MINER,
         'M',
         Items.MINECART,
         'N',
         Items.FIREWORK_ROCKET
      );
      recipes.addShapedIC2Recipe(
         "pressure_alloy_furnace",
         new ItemStack(IC2Blocks.PRESSURE_ALLOY_FURNACE),
         "XYX",
         "XCX",
         "XVX",
         'X',
         Blocks.NETHER_BRICKS,
         'Y',
         IC2Items.COMPLEX_CIRCUIT,
         'C',
         IC2Blocks.ALLOY_SMELTER,
         'V',
         IC2Blocks.PRESSURE_ALLOY_FURNACE_MULTIBLOCK
      );
      recipes.addShapedIC2Recipe(
         "pressure_alloy_furnace_structure",
         new ItemStack(IC2Blocks.PRESSURE_ALLOY_FURNACE_MULTIBLOCK, 4),
         "XYX",
         "XCX",
         "XVX",
         'X',
         Blocks.NETHER_BRICKS,
         'Y',
         IC2Items.PLATING_HEAT_CAPACITY,
         'C',
         IC2Blocks.STABILIZED_MACHINE_BLOCK,
         'V',
         IC2Items.HEAT_PACK
      );
      recipes.addShapedIC2Recipe(
         "hyperclocked_pump",
         new ItemStack(IC2Blocks.HYPERCLOCKED_PUMP),
         "XYX",
         "CVC",
         "XBX",
         'X',
         IC2Items.COOLANT_CELL_60K,
         'Y',
         IC2Items.QUANTUM_OVERCLOCKER_UPGRADE,
         'C',
         IC2Items.VENT_ELECTRIC_OVERCLOCKED,
         'V',
         IC2Blocks.OVERCLOCKED_PUMP,
         'B',
         IC2Blocks.STABILIZED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "villager_o_mat",
         new ItemStack(IC2Blocks.VILLAGER_O_MAT),
         "XYX",
         "CVC",
         "BNB",
         'X',
         Items.HOPPER,
         'Y',
         IC2Blocks.PLAYER_DETECTOR,
         'C',
         IC2Items.COMPLEX_CIRCUIT,
         'V',
         IC2Blocks.TRADE_O_MAT,
         'B',
         Items.CLOCK,
         'N',
         IC2Blocks.STABILIZED_MACHINE_BLOCK
      );
   }

   private static void addMachinesEV(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "plasmafier",
         new ItemStack(IC2Blocks.PLASMAFIER),
         "XYX",
         "YCY",
         "XYX",
         'X',
         IC2Blocks.IRIDIUM_STONE,
         'Y',
         IC2Blocks.REINFORCED_GLASS,
         'C',
         IC2Blocks.INDUCTION_FURNACE
      );
      recipes.addShapedIC2Recipe(
         "uu_crop_library",
         new ItemStack(IC2Blocks.UU_CROP_LIBRARY),
         "XYX",
         "CVC",
         "XBX",
         'X',
         IC2Items.LAPOTRON_CRYSTAL,
         'Y',
         IC2Blocks.MASS_FABRICATOR,
         'V',
         IC2Blocks.INDUSTRIAL_WORKBENCH,
         'B',
         IC2Blocks.CROP_LIBRARY,
         'C',
         IC2Items.PLATE_IRIDIUM
      );
      recipes.addShapedIC2Recipe(
         "colossal_case",
         new ItemStack(IC2Blocks.COLOSSAL_BASE, 4),
         " X ",
         "YCY",
         " V ",
         'X',
         IC2Items.TURBINE_BLADE,
         'Y',
         IC2Items.PLATE_IRIDIUM,
         'C',
         IC2Blocks.STABILIZED_MACHINE_BLOCK,
         'V',
         IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "colossal_furnace",
         new ItemStack(IC2Blocks.COLOSSAL_FURNACE),
         "XXX",
         "XCX",
         "XYX",
         'X',
         Items.COPPER_INGOT,
         'C',
         IC2Blocks.INDUCTION_FURNACE,
         'Y',
         IC2Blocks.COLOSSAL_BASE
      );
      recipes.addShapedIC2Recipe(
         "colossal_macerator",
         new ItemStack(IC2Blocks.COLOSSAL_MACERATOR),
         "XXX",
         "XCX",
         "XYX",
         'X',
         IC2Items.SCRAP_METAL_BLADE,
         'C',
         IC2Blocks.ROTARY_MACERATOR,
         'Y',
         IC2Blocks.COLOSSAL_BASE
      );
      recipes.addShapedIC2Recipe(
         "colossal_extractor",
         new ItemStack(IC2Blocks.COLOSSAL_EXTRACTOR),
         "XXX",
         "XCX",
         "XYX",
         'X',
         IC2Items.CELL_ELECTROLYZED_WATER,
         'C',
         IC2Blocks.CENTRIFUGAL_EXTRACTOR,
         'Y',
         IC2Blocks.COLOSSAL_BASE
      );
      recipes.addShapedIC2Recipe(
         "colossal_compressor",
         new ItemStack(IC2Blocks.COLOSSAL_COMPRESSOR),
         "XXX",
         "XCX",
         "XYX",
         'X',
         Items.CRYING_OBSIDIAN,
         'C',
         IC2Blocks.SINGULARITY_COMPRESSOR,
         'Y',
         IC2Blocks.COLOSSAL_BASE
      );
      recipes.addShapedIC2Recipe(
         "colossal_recycler",
         new ItemStack(IC2Blocks.COLOSSAL_RECYCLER),
         "XXX",
         "XCX",
         "XYX",
         'X',
         Items.STICKY_PISTON,
         'C',
         IC2Blocks.COMPACTING_RECYCLER,
         'Y',
         IC2Blocks.COLOSSAL_BASE
      );
      recipes.addShapedIC2Recipe(
         "fisher",
         new ItemStack(IC2Blocks.FISHER),
         "XYX",
         "CVC",
         "BNB",
         'X',
         IC2Items.COMPLEX_CIRCUIT,
         'Y',
         Items.HOPPER,
         'C',
         Items.FISHING_ROD,
         'V',
         IC2Items.PLASMA_CORE,
         'B',
         IC2Blocks.STABILIZED_MACHINE_BLOCK,
         'N',
         IC2Blocks.RANGED_PUMP
      );
      recipes.addShapedIC2Recipe(
         "crafter",
         new ItemStack(IC2Blocks.CRAFTER),
         "XYX",
         "CVC",
         "BNB",
         'X',
         IC2Items.COMPLEX_CIRCUIT,
         'Y',
         IC2Items.CRAFTING_UPGRADE,
         'C',
         IC2Items.PLATE_IRIDIUM,
         'V',
         IC2Blocks.INDUSTRIAL_WORKBENCH,
         'B',
         Blocks.CHEST,
         'N',
         IC2Blocks.STABILIZED_MACHINE_BLOCK
      );
   }

   private static void addMachinesLuV(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "teleporter_hub",
         new ItemStack(IC2Blocks.TELEPORTER_HUB),
         "XYX",
         "YCY",
         "XYX",
         'C',
         IC2Blocks.PESU,
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'Y',
         IC2Blocks.TELEPORTER
      );
      recipes.addShapedIC2Recipe(
         "fusion_reactor",
         new ItemStack(IC2Blocks.FUSION_REACTOR),
         "RCR",
         "PNP",
         "RFR",
         'R',
         IC2Blocks.REINFORCED_STONE,
         'C',
         IC2Items.COMPLEX_CIRCUIT,
         'P',
         IC2Items.PLASMA_CORE,
         'N',
         IC2Blocks.STEAM_REACTOR,
         'F',
         IC2Blocks.FUEL_BOILER
      );
   }

   private static void addPersonalBlocks(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "trade_o_mat",
         new ItemStack(IC2Blocks.TRADE_O_MAT),
         "RRR",
         "CMC",
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         net.minecraftforge.common.Tags.Items.CHESTS_WOODEN,
         'M',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "fluid_o_mat",
         new ItemStack(IC2Blocks.FLUID_O_MAT),
         "GGG",
         "PMP",
         'G',
         net.minecraftforge.common.Tags.Items.GLASS,
         'P',
         IC2Blocks.PUMP,
         'M',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "energy_o_mat",
         new ItemStack(IC2Blocks.ENERGY_O_MAT),
         "RBR",
         "CMC",
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'M',
         IC2Blocks.MACHINE_BLOCK,
         'B',
         IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "personal_chest",
         new ItemStack(IC2Blocks.PERSONAL_CHEST),
         "c",
         "M",
         "C",
         'c',
         IC2Items.CIRCUIT,
         'C',
         net.minecraftforge.common.Tags.Items.CHESTS_WOODEN,
         'M',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "personal_tank",
         new ItemStack(IC2Blocks.PERSONAL_TANK),
         "XXX",
         "XYX",
         "XXX",
         'X',
         net.minecraftforge.common.Tags.Items.GLASS,
         'Y',
         IC2Blocks.PERSONAL_CHEST
      );
   }

   private static void addTools(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "chainsaw", new ItemStack(IC2Items.CHAINSAW), " II", "ICI", "BI ", 'I', IC2Tags.INGOT_REFINED_IRON, 'B', IC2Items.RE_BATTERY, 'C', IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "advanced_chainsaw",
         new ItemStack(IC2Items.CHAINSAW_ADVANCED),
         "XBX",
         "CYC",
         "BVB",
         'X',
         IC2Items.COMPLEX_CIRCUIT,
         'Y',
         IC2Items.ADVANCED_FIELD_PAD_UPGRADE,
         'C',
         IC2Items.PLATE_IRIDIUM,
         'V',
         IC2Items.CHAINSAW,
         'B',
         Items.NETHERITE_INGOT
      );
      recipes.addShapedIC2Recipe(
         "drill", new ItemStack(IC2Items.DRILL), " I ", "ICI", "IBI", 'I', IC2Tags.INGOT_REFINED_IRON, 'B', IC2Items.RE_BATTERY, 'C', IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "diamond_drill", new ItemStack(IC2Items.DRILL_DIAMOND), " D ", "DdD", 'D', net.minecraftforge.common.Tags.Items.GEMS_DIAMOND, 'd', IC2Items.DRILL
      );
      ItemStack silkTouchDrill = new ItemStack(IC2Items.DRILL_DIAMOND);
      silkTouchDrill.enchant(Enchantments.SILK_TOUCH, Enchantments.SILK_TOUCH.getMaxLevel());
      recipes.addShapedIC2Recipe(
         "diamond_drill_silk",
         silkTouchDrill,
         "XYX",
         "CVC",
         "BNB",
         'X',
         IC2Items.PLATE_IRIDIUM,
         'Y',
         Items.SHEARS,
         'C',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'V',
         IC2Items.DRILL_DIAMOND,
         'B',
         IC2Items.UPGRADE_BASE,
         'N',
         IC2Items.MAGNET
      );
      ItemStack fortuneDrill = new ItemStack(IC2Items.DRILL_DIAMOND);
      fortuneDrill.enchant(Enchantments.BLOCK_FORTUNE, Enchantments.BLOCK_FORTUNE.getMaxLevel());
      recipes.addShapedIC2Recipe(
         "diamond_drill_fortune",
         fortuneDrill,
         "XYX",
         "CVC",
         "BNB",
         'X',
         IC2Items.PLATE_IRIDIUM,
         'Y',
         new ItemStack(IC2Items.ADVANCED_CIRCUIT, 5),
         'C',
         new ItemStack(Items.END_CRYSTAL, 3),
         'V',
         IC2Items.DRILL_DIAMOND,
         'B',
         IC2Items.UPGRADE_BASE,
         'N',
         new ItemStack(IC2Items.MAGNET, 5)
      );
      recipes.addShapedIC2Recipe(
         "diamond_drill_rock_cutter",
         new ItemStack(IC2Items.DRILL_ROCK_CUTTER),
         "XYX",
         "CVC",
         'X',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'Y',
         IC2Items.DRILL_DIAMOND,
         'C',
         IC2Items.UPGRADE_BASE,
         'V',
         Items.PISTON
      );
      recipes.addShapedIC2Recipe(
         "advanced_drill",
         new ItemStack(IC2Items.DRILL_ADVANCED),
         "XYX",
         "CVC",
         "BNB",
         'X',
         IC2Items.COMPLEX_CIRCUIT,
         'Y',
         IC2Items.ADVANCED_FIELD_PAD_UPGRADE,
         'C',
         IC2Items.PLATE_IRIDIUM,
         'V',
         IC2Items.DRILL_DIAMOND,
         'B',
         Items.NETHERITE_INGOT,
         'N',
         IC2Items.PLASMA_CORE
      );
      recipes.addShapedIC2Recipe(
         "electric_hoe",
         new ItemStack(IC2Items.ELECTRIC_HOE),
         "II ",
         " C ",
         " B ",
         'I',
         IC2Tags.INGOT_REFINED_IRON,
         'B',
         IC2Items.RE_BATTERY,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "advanced_hoe",
         new ItemStack(IC2Items.ADVANCED_HOE),
         "XYX",
         "CVC",
         "XBX",
         'X',
         Items.NETHERITE_INGOT,
         'Y',
         IC2Items.ADVANCED_FIELD_PAD_UPGRADE,
         'C',
         IC2Items.COMPLEX_CIRCUIT,
         'V',
         IC2Items.ELECTRIC_HOE,
         'B',
         IC2Items.PLATE_IRIDIUM
      );
      recipes.addShapedIC2Recipe(
         "electric_treetap",
         new ItemStack(IC2Items.ELECTRIC_TREETAP),
         "  W",
         " C ",
         "B  ",
         'W',
         IC2Items.TREETAP,
         'B',
         IC2Items.RE_BATTERY,
         'C',
         IC2Items.CIRCUIT
      );
      ItemStack importTreeTap = new ItemStack(IC2Items.ELECTRIC_TREETAP);
      CompoundTag nbt_import = importTreeTap.getOrCreateTag();
      nbt_import.putBoolean("inv_import", true);
      recipes.addShapelessIC2Recipe("electric_treetap_upgrade", importTreeTap, IC2Items.ELECTRIC_TREETAP, Items.HOPPER, IC2Items.IMPORT_UPGRADE_SIMPLE);
      recipes.addShapedIC2Recipe(
         "advanced_tree_tap",
         new ItemStack(IC2Items.ADVANCED_TREETAP),
         "XYX",
         "CVC",
         "BNB",
         'X',
         IC2Items.COMPLEX_CIRCUIT,
         'Y',
         IC2Items.ADVANCED_FIELD_PAD_UPGRADE,
         'C',
         Items.NETHERITE_INGOT,
         'V',
         IC2Blocks.TREETAP_AND_BUCKET,
         'N',
         IC2Items.ELECTRIC_TREETAP,
         'B',
         IC2Items.PLATE_IRIDIUM
      );
      recipes.addShapedIC2Recipe(
         "electric_cf_sprayer",
         new ItemStack(IC2Items.ELECTRIC_SPRAYER),
         "  W",
         " C ",
         "B  ",
         'W',
         IC2Items.CF_SPRAYER,
         'B',
         IC2Items.RE_BATTERY,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "mining_laser",
         new ItemStack(IC2Items.MINING_LASER),
         "Rcc",
         "AAC",
         " AA",
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'C',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'c',
         IC2Items.ENERGY_CRYSTAL,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE
      );
      recipes.addShapedIC2Recipe(
         "nano_saber",
         new ItemStack(IC2Items.NANO_SABER),
         "GA ",
         "GA ",
         "CcC",
         'C',
         IC2Items.CARBON_PLATE,
         'c',
         IC2Items.ENERGY_CRYSTAL,
         'G',
         Items.GLOWSTONE_DUST,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "obscurator",
         new ItemStack(IC2Items.OBSCURATOR),
         "XCX",
         "YVB",
         "NMN",
         'N',
         Items.STONE_BUTTON,
         'M',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'Y',
         Items.STICKY_PISTON,
         'V',
         IC2Items.ENERGY_CRYSTAL,
         'B',
         Items.PISTON,
         'C',
         net.minecraftforge.common.Tags.Items.GLASS,
         'X',
         Items.PAINTING
      );
      ItemStack electricWrench = new ItemStack(IC2Items.ELECTRIC_WRENCH);
      electricWrench.getOrCreateTag().putBoolean("losslessMode", true);
      recipes.addShapedIC2Recipe("electric_wrench", electricWrench, "  W", " C ", "B  ", 'W', IC2Items.WRENCH, 'B', IC2Items.RE_BATTERY, 'C', IC2Items.CIRCUIT);
      recipes.addShapedIC2Recipe(
         "precision_wrench",
         new ItemStack(IC2Items.PRECISION_WRENCH),
         "XYX",
         "CVC",
         "XYX",
         'X',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'C',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Items.ELECTRIC_WRENCH,
         'V',
         IC2Items.PLATE_IRIDIUM
      );
      ItemStack lossless = new ItemStack(IC2Items.PRECISION_WRENCH);
      CompoundTag nbt = lossless.getOrCreateTag();
      nbt.putBoolean("lossless", true);
      recipes.addShapedIC2Recipe(
         "precision_wrench_lossless",
         lossless,
         "XYX",
         "CVC",
         "BNB",
         'X',
         new ItemStack(IC2Items.EFFICIENCY_UPGRADE, 10),
         'Y',
         IC2Items.OBSCURATOR,
         'C',
         new ItemStack(IC2Items.PLASMA_CORE, 5),
         'V',
         IC2Items.PRECISION_WRENCH,
         'B',
         IC2Items.VENT_ELECTRIC_ADVANCED,
         'N',
         new ItemStack(IC2Items.ADVANCED_CIRCUIT, 10)
      );
      recipes.addShapedIC2Recipe(
         "mowing_tool",
         new ItemStack(IC2Items.MOWING_TOOL),
         " C ",
         "XYX",
         "XVX",
         'X',
         IC2Tags.INGOT_REFINED_IRON,
         'Y',
         IC2Items.CIRCUIT,
         'C',
         IC2Items.TURBINE_BLADE,
         'V',
         IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "portable_teleporter",
         new ItemStack(IC2Items.PORTABLE_TELEPORTER),
         "XYX",
         "CVC",
         "BNB",
         'X',
         new ItemStack(IC2Items.PLATE_IRIDIUM, 2),
         'Y',
         IC2Items.MEMORY_STICK,
         'C',
         new ItemStack(IC2Items.ADVANCED_CIRCUIT, 3),
         'V',
         new ItemStack(IC2Blocks.TELEPORTER, 2),
         'B',
         new ItemStack(IC2Items.PLASMA_CORE, 8),
         'N',
         IC2Items.PESD
      );
      recipes.addShapedIC2Recipe("treetap", new ItemStack(IC2Items.TREETAP), " P ", "PPP", "P  ", 'P', ItemTags.PLANKS);
      recipes.addShapedIC2Recipe("tap_bucket", new ItemStack(IC2Blocks.TREETAP_AND_BUCKET), "X", "Y", 'X', IC2Items.TREETAP, 'Y', Items.BUCKET);
      recipes.addShapedIC2Recipe("tap_pot", new ItemStack(IC2Blocks.TREETAP_AND_BUCKET), "X", "Y", 'X', IC2Items.TREETAP, 'Y', Items.FLOWER_POT);
      recipes.addShapedIC2Recipe(
         "painter", new ItemStack(IC2Items.PAINTER), " CC", " IC", "I  ", 'C', ItemTags.WOOL, 'I', net.minecraftforge.common.Tags.Items.INGOTS_IRON
      );
      recipes.addShapedIC2Recipe("wrench", new ItemStack(IC2Items.WRENCH), "B B", "BBB", " B ", 'B', IC2Tags.INGOT_BRONZE);
      recipes.addShapedIC2Recipe(
         "cutter", new ItemStack(IC2Items.CUTTER), "A A", " A ", "I I", 'A', IC2Tags.INGOT_REFINED_IRON, 'I', net.minecraftforge.common.Tags.Items.INGOTS_IRON
      );
      recipes.addShapedIC2Recipe(
         "toolbox", new ItemStack(IC2Items.TOOL_BOX), "ICI", "III", 'C', net.minecraftforge.common.Tags.Items.CHESTS_WOODEN, 'I', IC2Tags.INGOT_REFINED_IRON
      );
      recipes.addShapedIC2Recipe(
         "nano_toolbox",
         new ItemStack(IC2Items.TOOL_BOX_CARBON),
         new TransferInventoryModifier(IC2Items.TOOL_BOX),
         "XYX",
         "XXX",
         'X',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Items.TOOL_BOX
      );
      recipes.addShapedIC2Recipe(
         "quantum_toolbox",
         new ItemStack(IC2Items.TOOL_BOX_IRIDIUM),
         new TransferInventoryModifier(IC2Items.TOOL_BOX_CARBON),
         "XYX",
         "XXX",
         'X',
         IC2Items.PLATE_IRIDIUM,
         'Y',
         IC2Items.TOOL_BOX_CARBON
      );
      recipes.addShapedIC2Recipe(
         "eu_reader",
         new ItemStack(IC2Items.EU_READER),
         " G ",
         "cCc",
         "c c",
         'G',
         Items.GLOWSTONE_DUST,
         'c',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "copy_tool", new ItemStack(IC2Items.COPY_TOOL), " R ", "CMC", 'R', IC2Items.EU_READER, 'C', IC2Items.ADVANCED_CIRCUIT, 'M', IC2Items.MEMORY_STICK
      );
      recipes.addShapedIC2Recipe(
         "crop_analyzer",
         new ItemStack(IC2Items.CROP_ANALYZER),
         "cc ",
         "RGR",
         "RCR",
         'G',
         net.minecraftforge.common.Tags.Items.GLASS,
         'c',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapelessIC2Recipe(
         "frequency_transmitter", new ItemStack(IC2Items.FREQUENCY_TRANSMITTER), IC2Items.CIRCUIT, IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapelessIC2Recipe(
         "tube_tool", new ItemStack(IC2Items.TUBE_TOOL), IC2Items.FREQUENCY_TRANSMITTER, IC2Items.ADVANCED_CIRCUIT, IC2Blocks.SIMPLE_TUBE
      );
      recipes.addShapedIC2Recipe(
         "tube_lense",
         new ItemStack(IC2Items.TUBE_LENSE),
         "  z",
         " y ",
         "x  ",
         'x',
         IC2Blocks.SIMPLE_TUBE,
         'y',
         IC2Blocks.MINING_PIPE_SHAFT,
         'z',
         net.minecraftforge.common.Tags.Items.GLASS_PANES
      );
      ItemStack damage = new ItemStack(IC2Items.CF_SPRAYER);
      damage.setDamageValue(1600);
      recipes.addShapedIC2Recipe("cf_sprayer", damage, "SS ", "Ss ", "  S", 'S', net.minecraftforge.common.Tags.Items.COBBLESTONE, 's', Items.STICK);
      recipes.addRepairIC2Recipe("cf_refill", (IRepairable)IC2Items.CF_SPRAYER, new ItemInput(IC2Items.CF_PELLET), 100);
      recipes.addShapedIC2Recipe(
         "thermometer",
         new ItemStack(IC2Items.THERMOMETER),
         " XY",
         "XCX",
         "XX ",
         'X',
         net.minecraftforge.common.Tags.Items.GLASS,
         'C',
         IC2Items.CELL_WATER,
         'Y',
         IC2Tags.INGOT_TIN
      );
      if (!IC2.CONFIG.disableBronzeTools.get()) {
         recipes.addShapedIC2Recipe("bronze_pickaxe", new ItemStack(IC2Items.PICKAXE_BRONZE), "BBB", " S ", " S ", 'B', IC2Tags.INGOT_BRONZE, 'S', Items.STICK);
         recipes.addShapedIC2Recipe("bronze_axe", new ItemStack(IC2Items.AXE_BRONZE), "BB", "BS", " S", 'B', IC2Tags.INGOT_BRONZE, 'S', Items.STICK);
         recipes.addShapedIC2Recipe("bronze_hoe", new ItemStack(IC2Items.HOE_BRONZE), "BB", " S", " S", 'B', IC2Tags.INGOT_BRONZE, 'S', Items.STICK);
         recipes.addShapedIC2Recipe("bronze_sword", new ItemStack(IC2Items.SWORD_BRONZE), "B", "B", "S", 'B', IC2Tags.INGOT_BRONZE, 'S', Items.STICK);
         recipes.addShapedIC2Recipe("bronze_shovel", new ItemStack(IC2Items.SHOVEL_BRONZE), " B ", " S ", " S ", 'B', IC2Tags.INGOT_BRONZE, 'S', Items.STICK);
      }

      if (!IC2.CONFIG.disableAluminumTools.get()) {
         recipes.addShapedIC2Recipe(
            "aluminum_pickaxe", new ItemStack(IC2Items.PICKAXE_ALUMINIUM), "AAA", " S ", " S ", 'A', IC2Tags.INGOT_ALUMINIUM, 'S', Items.STICK
         );
         recipes.addShapedIC2Recipe("aluminum_axe", new ItemStack(IC2Items.AXE_ALUMINIUM), "AA", "AS", " S", 'A', IC2Tags.INGOT_ALUMINIUM, 'S', Items.STICK);
         recipes.addShapedIC2Recipe("aluminum_hoe", new ItemStack(IC2Items.HOE_ALUMINIUM), "AA", " S", " S", 'A', IC2Tags.INGOT_ALUMINIUM, 'S', Items.STICK);
         recipes.addShapedIC2Recipe("aluminum_sword", new ItemStack(IC2Items.SWORD_ALUMINIUM), "A", "A", "S", 'A', IC2Tags.INGOT_ALUMINIUM, 'S', Items.STICK);
         recipes.addShapedIC2Recipe(
            "aluminum_shovel", new ItemStack(IC2Items.SHOVEL_ALUMINIUM), " A ", " S ", " S ", 'A', IC2Tags.INGOT_ALUMINIUM, 'S', Items.STICK
         );
      }

      recipes.addShapedIC2Recipe(
         "od_scanner",
         new ItemStack(IC2Items.SCANNER_OD),
         " G ",
         "CBC",
         "ccc",
         'B',
         IC2Items.RE_BATTERY,
         'c',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'G',
         Items.GLOWSTONE_DUST,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "ov_scanner_0",
         new ItemStack(IC2Items.SCANNER_OV),
         " G ",
         "GCG",
         "cSc",
         'S',
         IC2Items.SCANNER_OD,
         'c',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'G',
         Items.GLOWSTONE_DUST,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "ov_scanner_1",
         new ItemStack(IC2Items.SCANNER_OV),
         " G ",
         "GCG",
         "cSc",
         'S',
         IC2Items.SCANNER_OD,
         'c',
         new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED, 3),
         'G',
         Items.GLOWSTONE_DUST,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "ov_scanner_2",
         new ItemStack(IC2Items.SCANNER_OV),
         " G ",
         "GCG",
         "cSc",
         'S',
         IC2Items.RE_BATTERY,
         'c',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'G',
         Items.GLOWSTONE_DUST,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "ov_scanner_3",
         new ItemStack(IC2Items.SCANNER_OV),
         " G ",
         "GCG",
         "cSc",
         'S',
         IC2Items.RE_BATTERY,
         'c',
         new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED, 3),
         'G',
         Items.GLOWSTONE_DUST,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "ranged_od_scanner_0",
         new ItemStack(IC2Items.SCANNER_OD_RANGED),
         "VVV",
         "XYX",
         "CCC",
         'X',
         IC2Items.SCANNER_OD,
         'Y',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'V',
         Items.GLOWSTONE_DUST
      );
      recipes.addShapedIC2Recipe(
         "ranged_od_scanner_1",
         new ItemStack(IC2Items.SCANNER_OD_RANGED),
         "VVV",
         "XYX",
         "CCC",
         'X',
         IC2Items.SCANNER_OD,
         'Y',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED, 3),
         'V',
         Items.GLOWSTONE_DUST
      );
      recipes.addShapedIC2Recipe(
         "ranged_ov_scanner_0",
         new ItemStack(IC2Items.SCANNER_OV_RANGED),
         "VVV",
         "XYX",
         "CCC",
         'X',
         IC2Items.SCANNER_OV,
         'Y',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'V',
         Items.GLOWSTONE_DUST
      );
      recipes.addShapedIC2Recipe(
         "ranged_ov_scanner_1",
         new ItemStack(IC2Items.SCANNER_OV_RANGED),
         "VVV",
         "XYX",
         "CCC",
         'X',
         IC2Items.SCANNER_OV,
         'Y',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED, 3),
         'V',
         Items.GLOWSTONE_DUST
      );
      recipes.addShapelessIC2Recipe("ranged_od_scanner_2", new ItemStack(IC2Items.SCANNER_OD_RANGED), IC2Items.SCANNER_OV_RANGED, IC2Items.CIRCUIT);
      recipes.addShapelessIC2Recipe("ranged_ov_scanner_2", new ItemStack(IC2Items.SCANNER_OV_RANGED), IC2Items.SCANNER_OD_RANGED, IC2Items.ADVANCED_CIRCUIT);
      recipes.addShapedIC2Recipe(
         "big_ranged_od_scanner_0",
         new ItemStack(IC2Items.SCANNER_OD_BIG_RANGED),
         "XYX",
         "CVC",
         "XYX",
         'X',
         Items.GLOWSTONE_DUST,
         'Y',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Items.SCANNER_OD,
         'V',
         IC2Items.SCANNER_OD_RANGED
      );
      recipes.addShapedIC2Recipe(
         "big_ranged_ov_scanner_0",
         new ItemStack(IC2Items.SCANNER_OV_BIG_RANGED),
         "XYX",
         "CVC",
         "XYX",
         'X',
         Items.GLOWSTONE_DUST,
         'Y',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Items.SCANNER_OV,
         'V',
         IC2Items.SCANNER_OV_RANGED
      );
      recipes.addShapelessIC2Recipe("big_ranged_od_scanner_1", new ItemStack(IC2Items.SCANNER_OD_BIG_RANGED), IC2Items.SCANNER_OV_BIG_RANGED, IC2Items.CIRCUIT);
      recipes.addShapelessIC2Recipe(
         "big_ranged_ov_scanner_1", new ItemStack(IC2Items.SCANNER_OV_BIG_RANGED), IC2Items.SCANNER_OD_BIG_RANGED, IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "quarry_scanner_0",
         new ItemStack(IC2Items.SCANNER_QUARRY),
         " X ",
         "XYX",
         " X ",
         'X',
         IC2Items.SCANNER_OD,
         'Y',
         new ItemTagInput(net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE, 5)
      );
      ItemStack upgradedQuarryScanner = new ItemStack(IC2Items.SCANNER_QUARRY);
      CompoundTag nbt_upgraded = upgradedQuarryScanner.getOrCreateTag();
      nbt_upgraded.putByte("upgraded", (byte)1);
      recipes.addShapelessIC2Recipe("quarry_scanner_upgraded", upgradedQuarryScanner, IC2Items.SCANNER_QUARRY, IC2Items.ADVANCED_CIRCUIT);
      recipes.addShapelessIC2Recipe("fluid_scanner", new ItemStack(IC2Items.SCANNER_FLUID), IC2Items.SCANNER_OV_RANGED, Items.BUCKET);
      recipes.addShapedIC2Recipe(
         "filtered_fluid_scanner",
         new ItemStack(IC2Items.SCANNER_FLUID_FILTERED),
         "X",
         "Y",
         "C",
         'X',
         IC2Items.SCANNER_FLUID,
         'Y',
         net.minecraftforge.common.Tags.Items.CHESTS_WOODEN,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "filtered_scanner",
         new ItemStack(IC2Items.SCANNER_FILTERED),
         "X",
         "Y",
         "C",
         'X',
         IC2Items.SCANNER_QUARRY,
         'Y',
         net.minecraftforge.common.Tags.Items.CHESTS_WOODEN,
         'C',
         IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "su_battery_0",
         new ItemStack(IC2Items.SU_BATTERY, 6),
         "C",
         "R",
         "D",
         'D',
         IC2Tags.DUST_COAL,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "su_battery_1",
         new ItemStack(IC2Items.SU_BATTERY, 6),
         "C",
         "D",
         "R",
         'D',
         IC2Tags.DUST_COAL,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "su_battery_2",
         new ItemStack(IC2Items.SU_BATTERY, 8),
         "C",
         "R",
         "D",
         'D',
         IC2Items.DUST_HYDRATED_COAL,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "su_battery_3",
         new ItemStack(IC2Items.SU_BATTERY, 8),
         "C",
         "D",
         "R",
         'D',
         IC2Items.DUST_HYDRATED_COAL,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "su_battery_4",
         new ItemStack(IC2Items.SU_BATTERY, 4),
         "C",
         "R",
         "D",
         'D',
         IC2Tags.DUST_CHARCOAL,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "su_battery_5",
         new ItemStack(IC2Items.SU_BATTERY, 4),
         "C",
         "D",
         "R",
         'D',
         IC2Tags.DUST_CHARCOAL,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "su_battery_6",
         new ItemStack(IC2Items.SU_BATTERY, 6),
         "C",
         "R",
         "D",
         'D',
         IC2Items.DUST_HYDRATED_CHARCOAL,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "su_battery_7",
         new ItemStack(IC2Items.SU_BATTERY, 6),
         "C",
         "D",
         "R",
         'D',
         IC2Items.DUST_HYDRATED_CHARCOAL,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "re_battery_0",
         new ItemStack(IC2Items.RE_BATTERY),
         " C ",
         "TRT",
         "TRT",
         'T',
         IC2Tags.INGOT_TIN,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "energy_crystal",
         new ItemStack(IC2Items.ENERGY_CRYSTAL),
         "RRR",
         "RDR",
         "RRR",
         'D',
         net.minecraftforge.common.Tags.Items.GEMS_DIAMOND,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE
      );
      recipes.addShapedIC2Recipe(
         "lapatron_crystal",
         new ItemStack(IC2Items.LAPOTRON_CRYSTAL),
         "LCL",
         "LDL",
         "LCL",
         'D',
         IC2Items.ENERGY_CRYSTAL,
         'C',
         IC2Items.CIRCUIT,
         'L',
         Items.LAPIS_LAZULI
      );
      recipes.addShapedIC2Recipe(
         "glowtronic_crystal",
         new ItemStack(IC2Items.GLOWTRONIC_CRYSTAL),
         "LCL",
         "LDL",
         "LCL",
         'D',
         IC2Items.LAPOTRON_CRYSTAL,
         'C',
         IC2Items.COMPLEX_CIRCUIT,
         'L',
         Items.GLOWSTONE_DUST
      );
      recipes.addShapedIC2Recipe(
         "uesc",
         new ItemStack(IC2Items.UESC),
         "XYX",
         "CVC",
         "XYX",
         'X',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'C',
         IC2Items.PLATE_IRIDIUM,
         'V',
         IC2Items.GLOWTRONIC_CRYSTAL
      );
      recipes.addShapedIC2Recipe(
         "pesd",
         new ItemStack(IC2Items.PESD),
         "XYX",
         "CVC",
         "XYX",
         'X',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Items.PLATE_ADVANCED_ALLOY,
         'C',
         IC2Items.PLATE_IRIDIUM,
         'V',
         IC2Items.PLASMA_CORE
      );
      recipes.addShapedIC2Recipe(
         "quantum_accumulator",
         new ItemStack(IC2Items.QUANTUM_ACCUMULATOR),
         "XYX",
         "CVC",
         "CBC",
         'C',
         IC2Tags.RUBBER,
         'B',
         IC2Items.PLATE_IRIDIUM,
         'V',
         IC2Items.LAPOTRON_CRYSTAL,
         'X',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Items.GLASSFIBER_CABLE
      );
      recipes.addShapedIC2Recipe(
         "big_quantum_accumulator",
         new ItemStack(IC2Items.QUANTUM_ACCUMULATOR_BIG),
         "XYX",
         "CCC",
         "CCC",
         'C',
         IC2Items.QUANTUM_ACCUMULATOR,
         'Y',
         IC2Items.CARBON_PLATE,
         'X',
         IC2Items.PLATE_IRIDIUM
      );
      recipes.addShapelessIC2Recipe("monitor_card", new ItemStack(IC2Items.MONITOR_CARD, 6), IC2Items.FREQUENCY_TRANSMITTER, Items.ENDER_PEARL);
      recipes.addShapelessIC2Recipe("activity_card", new ItemStack(IC2Items.ACTIVITY_CARD), IC2Items.MONITOR_CARD, Items.LEVER);
      recipes.addShapelessIC2Recipe("progress_card", new ItemStack(IC2Items.PROGRESS_CARD), IC2Items.MONITOR_CARD, IC2Items.CIRCUIT);
      recipes.addShapelessIC2Recipe("energy_card", new ItemStack(IC2Items.ENERGY_CARD), IC2Items.MONITOR_CARD, IC2Items.EU_READER);
      recipes.addShapelessIC2Recipe("time_card", new ItemStack(IC2Items.TIME_CARD), IC2Items.MONITOR_CARD, Items.CLOCK);
      recipes.addShapelessIC2Recipe("lag_card", new ItemStack(IC2Items.LAG_CARD), IC2Items.MONITOR_CARD, IC2Items.STICKY_RESIN);
      recipes.addShapelessIC2Recipe("reactor_card", new ItemStack(IC2Items.REACTOR_CARD), IC2Items.MONITOR_CARD, IC2Items.PLATING);
      recipes.addShapelessIC2Recipe("text_card", new ItemStack(IC2Items.TEXT_CARD), IC2Items.MONITOR_CARD, Items.WRITABLE_BOOK);
      recipes.addShapelessIC2Recipe("detector_card", new ItemStack(IC2Items.DETECTOR_CARD), IC2Items.MONITOR_CARD, IC2Blocks.PLAYER_DETECTOR);
      recipes.addShapelessIC2Recipe("inventory_card", new ItemStack(IC2Items.INV_CARD), IC2Items.MONITOR_CARD, Items.CHEST);
      recipes.addShapelessIC2Recipe("activity_to_monitor_card", new ItemStack(IC2Items.MONITOR_CARD), IC2Items.ACTIVITY_CARD);
      recipes.addShapelessIC2Recipe("progress_to_monitor_card", new ItemStack(IC2Items.MONITOR_CARD), IC2Items.PROGRESS_CARD);
      recipes.addShapelessIC2Recipe("energy_to_monitor_card", new ItemStack(IC2Items.MONITOR_CARD), IC2Items.ENERGY_CARD);
      recipes.addShapelessIC2Recipe("time_to_monitor_card", new ItemStack(IC2Items.MONITOR_CARD), IC2Items.TIME_CARD);
      recipes.addShapelessIC2Recipe("lag_to_monitor_card", new ItemStack(IC2Items.MONITOR_CARD), IC2Items.LAG_CARD);
      recipes.addShapelessIC2Recipe("reactor_to_monitor_card", new ItemStack(IC2Items.MONITOR_CARD), IC2Items.REACTOR_CARD);
      recipes.addShapelessIC2Recipe("text_to_monitor_card", new ItemStack(IC2Items.MONITOR_CARD), IC2Items.TEXT_CARD);
      recipes.addShapelessIC2Recipe("detector_to_monitor_card", new ItemStack(IC2Items.MONITOR_CARD), IC2Items.DETECTOR_CARD);
      recipes.addShapelessIC2Recipe("inventory_to_monitor_card", new ItemStack(IC2Items.MONITOR_CARD), IC2Items.INV_CARD);
   }

   private static void addArmor(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "composite_helmet_0",
         new ItemStack(IC2Items.COMPOSITE_HELMET),
         "AAA",
         "AIA",
         " L ",
         'L',
         Items.LEATHER_HELMET,
         'I',
         Items.IRON_HELMET,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "composite_helmet_1",
         new ItemStack(IC2Items.COMPOSITE_HELMET),
         "AAA",
         "ALA",
         " I ",
         'L',
         Items.LEATHER_HELMET,
         'I',
         Items.IRON_HELMET,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "composite_chest_0",
         new ItemStack(IC2Items.COMPOSITE_CHEST),
         "A A",
         "ALA",
         "AIA",
         'L',
         Items.LEATHER_CHESTPLATE,
         'I',
         Items.IRON_CHESTPLATE,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "composite_chest_1",
         new ItemStack(IC2Items.COMPOSITE_CHEST),
         "A A",
         "AIA",
         "ALA",
         'L',
         Items.LEATHER_CHESTPLATE,
         'I',
         Items.IRON_CHESTPLATE,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "composite_leggings_0",
         new ItemStack(IC2Items.COMPOSITE_LEGGINGS),
         "AAA",
         "AIA",
         "ALA",
         'L',
         Items.LEATHER_LEGGINGS,
         'I',
         Items.IRON_LEGGINGS,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "composite_leggings_1",
         new ItemStack(IC2Items.COMPOSITE_LEGGINGS),
         "AAA",
         "ALA",
         "AIA",
         'L',
         Items.LEATHER_LEGGINGS,
         'I',
         Items.IRON_LEGGINGS,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "composite_boots_0",
         new ItemStack(IC2Items.COMPOSITE_BOOTS),
         "ALA",
         "AIA",
         'L',
         Items.LEATHER_BOOTS,
         'I',
         Items.IRON_BOOTS,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "composite_boots_1",
         new ItemStack(IC2Items.COMPOSITE_BOOTS),
         "AIA",
         "ALA",
         'L',
         Items.LEATHER_BOOTS,
         'I',
         Items.IRON_BOOTS,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      if (!IC2.CONFIG.disableBronzeArmor.get()) {
         recipes.addShapedIC2Recipe("bronze_helmet", new ItemStack(IC2Items.BRONZE_HELMET), "BBB", "B B", 'B', IC2Tags.INGOT_BRONZE);
         recipes.addShapedIC2Recipe("bronze_chest", new ItemStack(IC2Items.BRONZE_CHEST), "B B", "BBB", "BBB", 'B', IC2Tags.INGOT_BRONZE);
         recipes.addShapedIC2Recipe("bronze_leggings", new ItemStack(IC2Items.BRONZE_LEGGINGS), "BBB", "B B", "B B", 'B', IC2Tags.INGOT_BRONZE);
         recipes.addShapedIC2Recipe("bronze_boots", new ItemStack(IC2Items.BRONZE_BOOTS), "B B", "B B", 'B', IC2Tags.INGOT_BRONZE);
      }

      recipes.addShapedIC2Recipe(
         "hazmat_helmet",
         new ItemStack(IC2Items.HAZMAT_HELMET),
         " O ",
         "RGR",
         "R#R",
         'R',
         IC2Tags.RUBBER,
         'G',
         net.minecraftforge.common.Tags.Items.GLASS,
         '#',
         Items.IRON_BARS,
         'O',
         Items.ORANGE_DYE
      );
      recipes.addShapedIC2Recipe("hazmat_chest", new ItemStack(IC2Items.HAZMAT_CHEST), "R R", "ROR", "ROR", 'R', IC2Tags.RUBBER, 'O', Items.ORANGE_DYE);
      recipes.addShapedIC2Recipe("hazmat_leggings", new ItemStack(IC2Items.HAZMAT_LEGGINGS), "ROR", "R R", "R R", 'R', IC2Tags.RUBBER, 'O', Items.ORANGE_DYE);
      recipes.addShapedIC2Recipe("hazmat_boots", new ItemStack(IC2Items.HAZMAT_BOOTS), "R R", "R R", "RWR", 'R', IC2Tags.RUBBER, 'W', ItemTags.WOOL);
      recipes.addShapedIC2Recipe("bronze_shield", new ItemStack(IC2Items.SHIELD_BRONZE), "XXX", "XYX", "XXX", 'X', IC2Tags.INGOT_BRONZE, 'Y', Items.SHIELD);
      recipes.addShapedIC2Recipe(
         "aluminum_shield", new ItemStack(IC2Items.SHIELD_ALUMINUM), "XXX", "XYX", "XXX", 'X', IC2Tags.INGOT_ALUMINIUM, 'Y', Items.SHIELD
      );
      recipes.addShapedIC2Recipe(
         "plated_shield", new ItemStack(IC2Items.SHIELD_PLATED), "XXX", "XYX", "XXX", 'X', IC2Items.PLATE_ADVANCED_ALLOY, 'Y', IC2Items.SHIELD_BRONZE
      );
      recipes.addShapedIC2Recipe(
         "nano_shield",
         new ItemStack(IC2Items.SHIELD_NANO),
         "XCX",
         "XYX",
         "XXX",
         'C',
         IC2Items.ENERGY_CRYSTAL,
         'X',
         IC2Items.CARBON_PLATE,
         'Y',
         IC2Items.SHIELD_PLATED
      );
      recipes.addShapedIC2Recipe(
         "battery_belt", new ItemStack(IC2Items.BATTERY_BELT), "XYY", "CCC", 'C', IC2Items.RE_BATTERY, 'Y', Items.LEATHER, 'X', IC2Tags.RUBBER
      );
      recipes.addShapelessIC2Recipe("re_battery_1", new ItemStack(IC2Items.RE_BATTERY, 3), IC2Items.BATTERY_BELT);
      recipes.addShapedIC2Recipe(
         "bat_pack", new ItemStack(IC2Items.BAT_PACK), "BCB", "BTB", "B B", 'T', IC2Tags.INGOT_TIN, 'C', IC2Items.CIRCUIT, 'B', IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "lap_pack", new ItemStack(IC2Items.LAP_PACK), "LAL", "LBL", "L L", 'L', Items.LAPIS_BLOCK, 'A', IC2Items.ADVANCED_CIRCUIT, 'B', IC2Items.BAT_PACK
      );
      recipes.addShapedIC2Recipe(
         "quantum_pack",
         new ItemStack(IC2Items.QUANTUM_PACK),
         " X ",
         "YCY",
         " V ",
         'Y',
         IC2Items.PLATE_IRIDIUM,
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Items.LAP_PACK,
         'V',
         IC2Items.LAPOTRON_CRYSTAL
      );
      ItemStack stack = new ItemStack(IC2Items.CF_PACK);
      stack.setDamageValue(stack.getMaxDamage());
      recipes.addShapedIC2Recipe(
         "cf_pack", stack, "SCS", "FTF", "F F", 'T', IC2Tags.INGOT_TIN, 'C', IC2Items.CIRCUIT, 'F', IC2Items.FUEL_CAN, 'S', IC2Items.CF_SPRAYER
      );
      recipes.addShapedIC2Recipe(
         "solar_helmet_0",
         new ItemStack(IC2Items.SOLAR_HELMET),
         "III",
         "ISI",
         "CCC",
         'I',
         net.minecraftforge.common.Tags.Items.INGOTS_IRON,
         'S',
         IC2Blocks.SOLAR_PANEL,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "solar_helmet_1",
         new ItemStack(IC2Items.SOLAR_HELMET),
         " H ",
         " S ",
         "CCC",
         'H',
         Items.IRON_HELMET,
         'S',
         IC2Blocks.SOLAR_PANEL,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "advanced_solar_helmet",
         new ItemStack(IC2Items.SOLAR_HELMET_ADVANCED),
         "XXX",
         "XYX",
         "CVC",
         'Y',
         IC2Items.SOLAR_HELMET,
         'X',
         IC2Blocks.SOLAR_PANEL,
         'C',
         IC2Items.CIRCUIT,
         'V',
         IC2Items.BRONZE_CABLE_2X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "static_boots_0",
         new ItemStack(IC2Items.STATIC_BOOTS),
         "I I",
         "ISI",
         "CCC",
         'I',
         net.minecraftforge.common.Tags.Items.INGOTS_IRON,
         'S',
         ItemTags.WOOL,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "static_boots_1",
         new ItemStack(IC2Items.STATIC_BOOTS),
         " H ",
         " S ",
         "CCC",
         'H',
         Items.IRON_BOOTS,
         'S',
         ItemTags.WOOL,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED
      );
      stack = new ItemStack(IC2Items.JETPACK_FUEL);
      stack.setDamageValue(stack.getMaxDamage());
      recipes.addShapedIC2Recipe(
         "fuel_jetpack",
         stack,
         "ICI",
         "IFI",
         "R R",
         'I',
         IC2Tags.INGOT_REFINED_IRON,
         'C',
         IC2Items.CIRCUIT,
         'F',
         IC2Items.FUEL_CAN,
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE
      );
      stack = new ItemStack(IC2Items.JETPACK_FUEL_COMPACT);
      stack.setDamageValue(stack.getMaxDamage());
      recipes.addShapedIC2Recipe(
         "compacted_fuel_jetpack",
         stack,
         " C ",
         "XCX",
         "ICI",
         'I',
         IC2Tags.INGOT_REFINED_IRON,
         'C',
         IC2Items.FUEL_CAN,
         'I',
         IC2Items.JETPACK_FUEL,
         'X',
         new ItemStack(IC2Items.CIRCUIT, 4)
      );
      recipes.addShapedIC2Recipe(
         "electric_jetpack",
         new ItemStack(IC2Items.JETPACK_ELECTRIC),
         "ICI",
         "IBI",
         "G G",
         'I',
         IC2Tags.INGOT_REFINED_IRON,
         'C',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.BATBOX,
         'G',
         Items.GLOWSTONE_DUST
      );
      recipes.addShapelessIC2Recipe(
         "electric_jetpack_repair",
         new ItemStack(IC2Items.JETPACK_ELECTRIC),
         IC2Items.JETPACK_ELECTRIC,
         IC2Items.MAGNET,
         new FlagModifier(IC2Items.JETPACK_ELECTRIC, "SpecialMode", false).setUsesInput(),
         RecipeMods.HIDDEN_RECIPE
      );
      recipes.addShapedIC2Recipe(
         "nuclear_jetpack",
         new ItemStack(IC2Items.JETPACK_NUCLEAR),
         "XCX",
         "DVD",
         "XAX",
         'A',
         IC2Items.JETPACK_ELECTRIC,
         'V',
         IC2Blocks.NUCLEAR_REACTOR,
         'D',
         IC2Blocks.REACTOR_CHAMBER,
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Blocks.TRANSFORMER_EV
      );
      recipes.addShapelessIC2Recipe(
         "nuclear_jetpack_repair",
         new ItemStack(IC2Items.JETPACK_NUCLEAR),
         IC2Items.JETPACK_NUCLEAR,
         IC2Items.MAGNET,
         new FlagModifier(IC2Items.JETPACK_NUCLEAR, "SpecialMode", false).setUsesInput(),
         RecipeMods.HIDDEN_RECIPE
      );
      recipes.addShapedIC2Recipe(
         "compacted_nuclear_jetpack_0",
         new ItemStack(IC2Items.JETPACK_NUCLEAR_COMPACT),
         "XCX",
         "DVD",
         "XAX",
         'A',
         IC2Items.JETPACK_ELECTRIC_COMPACT,
         'V',
         IC2Blocks.NUCLEAR_REACTOR,
         'D',
         IC2Blocks.REACTOR_CHAMBER,
         'X',
         IC2Items.ADVANCED_CIRCUIT,
         'C',
         IC2Blocks.TRANSFORMER_EV
      );
      recipes.addShapedIC2Recipe(
         "compacted_nuclear_jetpack_1",
         new ItemStack(IC2Items.JETPACK_NUCLEAR_COMPACT),
         " C ",
         "XYX",
         "VBV",
         'Y',
         IC2Items.JETPACK_NUCLEAR,
         'C',
         IC2Items.BAT_PACK,
         'X',
         new ItemStack(IC2Items.ADVANCED_CIRCUIT, 4),
         'V',
         IC2Items.JETPACK_ELECTRIC,
         'B',
         IC2Items.LAP_PACK
      );
      recipes.addShapedIC2Recipe(
         "compacted_electric_jetpack",
         new ItemStack(IC2Items.JETPACK_ELECTRIC_COMPACT),
         " C ",
         "XYX",
         "VBV",
         'C',
         IC2Items.TRANSFORMER_UPGRADE,
         'Y',
         IC2Items.BAT_PACK,
         'X',
         new ItemStack(IC2Items.ADVANCED_CIRCUIT, 4),
         'V',
         IC2Items.JETPACK_ELECTRIC,
         'B',
         IC2Items.LAP_PACK
      );
      recipes.addShapedIC2Recipe(
         "nightvision_goggles",
         new ItemStack(IC2Items.NIGHT_VISION_HELMET),
         "XYX",
         "CVC",
         "BNB",
         'X',
         IC2Items.HEAT_EXCHANGER_SPREAD,
         'Y',
         IC2Items.NANOSUIT_HELMET,
         'C',
         IC2Blocks.LUMINATOR,
         'V',
         IC2Blocks.REINFORCED_CLEAR_GLASS,
         'B',
         IC2Items.RUBBER,
         'N',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "normal_nano_armor_helmet",
         IC2Items.NANOSUIT_HELMET.createDefaultArmor(),
         "CcC",
         "CGC",
         'C',
         IC2Items.CARBON_PLATE,
         'c',
         IC2Items.ENERGY_CRYSTAL,
         'G',
         net.minecraftforge.common.Tags.Items.GLASS
      );
      recipes.addShapedIC2Recipe(
         "normal_nano_armor_chestplate",
         IC2Items.NANOSUIT_CHESTPLATE.createDefaultArmor(),
         "C C",
         "CcC",
         "CCC",
         'C',
         IC2Items.CARBON_PLATE,
         'c',
         IC2Items.ENERGY_CRYSTAL
      );
      recipes.addShapedIC2Recipe(
         "normal_nano_armor_leggings",
         IC2Items.NANOSUIT_LEGGINGS.createDefaultArmor(),
         "CcC",
         "C C",
         "C C",
         'C',
         IC2Items.CARBON_PLATE,
         'c',
         IC2Items.ENERGY_CRYSTAL
      );
      recipes.addShapedIC2Recipe(
         "normal_nano_armor_boots", IC2Items.NANOSUIT_BOOTS.createDefaultArmor(), "C C", "CcC", 'C', IC2Items.CARBON_PLATE, 'c', IC2Items.ENERGY_CRYSTAL
      );
      recipes.addShapedIC2Recipe(
         "upgraded_nano_armor_helmet",
         IC2Items.NANOSUIT_HELMET.createDefaultArmor(),
         new FlagModifier(IC2Items.NANOSUIT_HELMET, "upgraded", true)
            .setUsesInput()
            .setTooltip(Component.literal("Upgrades the Module Slots to Quantum Level")),
         " X ",
         " C ",
         "Y Y",
         'X',
         IC2Items.PLATE_IRIDIUM,
         'C',
         IC2Items.NANOSUIT_HELMET,
         'Y',
         IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "upgraded_nano_armor_chestplate",
         IC2Items.NANOSUIT_CHESTPLATE.createDefaultArmor(),
         new FlagModifier(IC2Items.NANOSUIT_CHESTPLATE, "upgraded", true)
            .setUsesInput()
            .setTooltip(Component.literal("Upgrades the Module Slots to Quantum Level")),
         " X ",
         " C ",
         "Y Y",
         'X',
         IC2Items.PLATE_IRIDIUM,
         'C',
         IC2Items.NANOSUIT_CHESTPLATE,
         'Y',
         IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "upgraded_nano_armor_leggings",
         IC2Items.NANOSUIT_LEGGINGS.createDefaultArmor(),
         new FlagModifier(IC2Items.NANOSUIT_LEGGINGS, "upgraded", true)
            .setUsesInput()
            .setTooltip(Component.literal("Upgrades the Module Slots to Quantum Level")),
         " X ",
         " C ",
         "Y Y",
         'X',
         IC2Items.PLATE_IRIDIUM,
         'C',
         IC2Items.NANOSUIT_LEGGINGS,
         'Y',
         IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "upgraded_nano_armor_boots",
         IC2Items.NANOSUIT_BOOTS.createDefaultArmor(),
         new FlagModifier(IC2Items.NANOSUIT_BOOTS, "upgraded", true).setUsesInput().setTooltip(Component.literal("Upgrades the Module Slots to Quantum Level")),
         " X ",
         " C ",
         "Y Y",
         'X',
         IC2Items.PLATE_IRIDIUM,
         'C',
         IC2Items.NANOSUIT_BOOTS,
         'Y',
         IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "empty_quantum_armor_helmet",
         new ItemStack(IC2Items.QUANTUM_SUIT_HELMET),
         new ModularArmorModifier(),
         " n ",
         "ILI",
         "CGC",
         'n',
         IC2Items.NANOSUIT_HELMET,
         'I',
         IC2Items.PLATE_IRIDIUM,
         'L',
         IC2Items.BASE_MODULE,
         'G',
         IC2Blocks.REINFORCED_GLASS,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "empty_quantum_armor_chestplate",
         new ItemStack(IC2Items.QUANTUM_SUIT_CHESTPLATE),
         new ModularArmorModifier(),
         "AnA",
         "ILI",
         "IAI",
         'n',
         IC2Items.NANOSUIT_CHESTPLATE,
         'I',
         IC2Items.PLATE_IRIDIUM,
         'L',
         IC2Items.BASE_MODULE,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "empty_quantum_armor_leggings",
         new ItemStack(IC2Items.QUANTUM_SUIT_LEGGINGS),
         new ModularArmorModifier(),
         "MLM",
         "InI",
         "G G",
         'n',
         IC2Items.NANOSUIT_LEGGINGS,
         'I',
         IC2Items.PLATE_IRIDIUM,
         'L',
         IC2Items.BASE_MODULE,
         'G',
         Items.GLOWSTONE_DUST,
         'M',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "empty_quantum_armor_boots",
         new ItemStack(IC2Items.QUANTUM_SUIT_BOOTS),
         new ModularArmorModifier(),
         "InI",
         "RLR",
         'n',
         IC2Items.NANOSUIT_BOOTS,
         'I',
         IC2Items.PLATE_IRIDIUM,
         'L',
         IC2Items.BASE_MODULE,
         'R',
         IC2Items.HAZMAT_BOOTS
      );
      recipes.addShapedIC2Recipe(
         "normal_quantum_armor_helmet",
         IC2Items.QUANTUM_SUIT_HELMET.createDefaultArmor(),
         new InfoModifier("Removes all installed Modules and installs the Default Functionality"),
         "XnY",
         "ILI",
         "CGC",
         'X',
         IC2Items.AUTO_FEED_MODULE,
         'Y',
         IC2Items.AIR_REFILL_MODULE,
         'n',
         IC2Items.NANOSUIT_HELMET,
         'I',
         IC2Items.PLATE_IRIDIUM,
         'L',
         IC2Items.LAPOTRON_CRYSTAL,
         'G',
         IC2Blocks.REINFORCED_GLASS,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "normal_quantum_armor_chestplate",
         IC2Items.QUANTUM_SUIT_CHESTPLATE.createDefaultArmor(),
         new InfoModifier("Removes all installed Modules and installs the Default Functionality"),
         "AnA",
         "ILI",
         "IXI",
         'X',
         IC2Items.EXTINGUISHING_MODULE,
         'n',
         IC2Items.NANOSUIT_CHESTPLATE,
         'I',
         IC2Items.PLATE_IRIDIUM,
         'L',
         IC2Items.LAPOTRON_CRYSTAL,
         'A',
         IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "normal_quantum_armor_leggings",
         IC2Items.QUANTUM_SUIT_LEGGINGS.createDefaultArmor(),
         new InfoModifier("Removes all installed Modules and installs the Default Functionality"),
         "MLM",
         "InI",
         "GXG",
         'X',
         IC2Items.SPEED_MODULE,
         'n',
         IC2Items.NANOSUIT_LEGGINGS,
         'I',
         IC2Items.PLATE_IRIDIUM,
         'L',
         IC2Items.LAPOTRON_CRYSTAL,
         'G',
         Items.GLOWSTONE_DUST,
         'M',
         IC2Blocks.MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "normal_quantum_armor_boots",
         IC2Items.QUANTUM_SUIT_BOOTS.createDefaultArmor(),
         new InfoModifier("Removes all installed Modules and installs the Default Functionality"),
         " X ",
         "InI",
         "RLR",
         'X',
         IC2Items.JUMP_BOOST_MODULE,
         'n',
         IC2Items.NANOSUIT_BOOTS,
         'I',
         IC2Items.PLATE_IRIDIUM,
         'L',
         IC2Items.LAPOTRON_CRYSTAL,
         'R',
         IC2Items.HAZMAT_BOOTS
      );
      recipes.addShapedIC2Recipe(
         "base_module",
         new ItemStack(IC2Items.BASE_MODULE, 2),
         "XXX",
         "XYX",
         "XXX",
         'X',
         IC2Blocks.REINFORCED_STONE,
         'Y',
         new ItemStack(IC2Items.UPGRADE_BASE, 2)
      );
      recipes.addShapedIC2Recipe(
         "air_refill_module",
         new ItemStack(IC2Items.AIR_REFILL_MODULE),
         "XY",
         "CV",
         'V',
         IC2Items.BASE_MODULE,
         'Y',
         IC2Items.HAZMAT_HELMET,
         'X',
         new ItemStack(IC2Items.CELL_AIR, 64),
         'C',
         IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "feeder_module",
         new ItemStack(IC2Items.AUTO_FEED_MODULE),
         "XY",
         "CV",
         'Y',
         Blocks.PISTON,
         'V',
         IC2Items.BASE_MODULE,
         'X',
         IC2Items.ENERGY_CRYSTAL,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapelessIC2Recipe("charger_module", new ItemStack(IC2Items.CHARGER_MODULE), IC2Items.BASE_MODULE, IC2Blocks.CHARGING_BENCH_LV);
      recipes.addShapelessIC2Recipe("food_storage_module", new ItemStack(IC2Items.FOOD_STORAGE_MODULE), IC2Items.TIN_CAN, IC2Items.BASE_MODULE, Items.CHEST);
      recipes.addShapelessIC2Recipe(
         "jump_boost_module", new ItemStack(IC2Items.JUMP_BOOST_MODULE), IC2Items.BASE_MODULE, Items.PISTON, IC2Items.OVERCLOCKER_UPGRADE
      );
      recipes.addShapedIC2Recipe(
         "jump_boost_module",
         new ItemStack(IC2Items.JUMP_BOOST_MODULE),
         " X ",
         "CVC",
         "BBB",
         'V',
         IC2Items.BASE_MODULE,
         'B',
         Items.PISTON,
         'C',
         IC2Items.PLATE_IRIDIUM,
         'X',
         IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "light_module",
         new ItemStack(IC2Items.FLASH_LIGHT_MODULE),
         "XY",
         "CV",
         'Y',
         IC2Blocks.LUMINATOR_ADJUSTABLE,
         'V',
         IC2Items.BASE_MODULE,
         'X',
         IC2Items.ENERGY_CRYSTAL,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "parachute_module",
         new ItemStack(IC2Items.PARACHUTE_MODULE),
         "XXX",
         "YVY",
         " C ",
         'X',
         ItemTags.WOOL_CARPETS,
         'Y',
         Items.STICK,
         'C',
         IC2Items.BASE_MODULE,
         'V',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "protective_module",
         new ItemStack(IC2Items.PROTECTION_MODULE),
         "XCX",
         "VYV",
         "XCX",
         'X',
         IC2Items.COMPLEX_CIRCUIT,
         'Y',
         IC2Items.BASE_MODULE,
         'V',
         IC2Items.PLATE_IRIDIUM,
         'C',
         Items.MILK_BUCKET
      );
      recipes.addShapedIC2Recipe(
         "speed_module",
         new ItemStack(IC2Items.SPEED_MODULE),
         "XCX",
         "VYV",
         "XCX",
         'Y',
         IC2Items.BASE_MODULE,
         'X',
         IC2Items.OVERCLOCKER_UPGRADE,
         'C',
         IC2Items.COMPLEX_CIRCUIT,
         'V',
         IC2Items.PLATE_IRIDIUM
      );
      recipes.addShapedIC2Recipe(
         "step_module",
         new ItemStack(IC2Items.STEP_ASSIST_MODULE),
         "XY",
         "CV",
         'Y',
         ItemTags.STAIRS,
         'V',
         IC2Items.BASE_MODULE,
         'X',
         Blocks.PISTON,
         'C',
         IC2Items.COMPLEX_CIRCUIT
      );
      recipes.addShapelessIC2Recipe("tank_module", new ItemStack(IC2Items.TANK_MODULE), IC2Items.BASE_MODULE, IC2Blocks.MACHINE_TANK);
      recipes.addShapedIC2Recipe(
         "water_walking",
         new ItemStack(IC2Items.WATER_WALKER_MODULE),
         "XY",
         "CV",
         'Y',
         IC2Items.BASE_MODULE,
         'X',
         IC2Items.ENERGY_CRYSTAL,
         'C',
         IC2Items.COMPLEX_CIRCUIT,
         'V',
         IC2Items.CARBON_BOAT
      );
      recipes.addShapelessIC2Recipe("extinguish_module", new ItemStack(IC2Items.EXTINGUISHING_MODULE), IC2Items.BASE_MODULE, Fluids.WATER);
      recipes.addShapelessIC2Recipe("energy_hud_module", new ItemStack(IC2Items.ENERGY_HUD_MODULE), IC2Items.BASE_MODULE, IC2Blocks.MONITOR);
      recipes.addShapedIC2Recipe(
         "lava_module",
         new ItemStack(IC2Items.LAVA_PROTECT_MODULE),
         "XY",
         "CV",
         'X',
         IC2Items.TANK_MODULE,
         'Y',
         IC2Items.EXTINGUISHING_MODULE,
         'C',
         IC2Items.COMPLEX_CIRCUIT,
         'V',
         IC2Items.PLATE_IRIDIUM
      );
      recipes.addShapedIC2Recipe(
         "sonar_module",
         new ItemStack(IC2Items.SONAR_MODULE),
         " X ",
         "YCV",
         " B ",
         'X',
         IC2Items.FREQUENCY_TRANSMITTER,
         'Y',
         IC2Items.COMPLEX_CIRCUIT,
         'C',
         IC2Items.ENERGY_CRYSTAL,
         'V',
         IC2Blocks.MONITOR,
         'B',
         IC2Items.BASE_MODULE
      );
      recipes.addShapelessIC2Recipe("energy_shield_overlay_module", new ItemStack(IC2Items.ENERGY_SHIELD_MODULE), IC2Items.BASE_MODULE, IC2Blocks.TESLA_COIL);
      recipes.addShapedIC2Recipe(
         "gold_module", new ItemStack(IC2Items.GOLDEN_MODULE), "X", "Y", "C", 'X', IC2Items.ADVANCED_CIRCUIT, 'Y', IC2Items.BASE_MODULE, 'C', Items.GOLD_BLOCK
      );
   }

   private static void addCFoam(IAdvancedCraftingManager recipes) {
      recipes.addShapelessIC2Recipe(
         "wet_cfoam_0",
         new ItemStack(IC2Blocks.CFOAM_WET, 3),
         IC2Items.DUST_CLAY,
         Fluids.WATER,
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         IC2Tags.DUST_COAL
      );
      recipes.addShapelessIC2Recipe(
         "wet_cfoam_1",
         new ItemStack(IC2Blocks.CFOAM_WET, 3),
         IC2Items.DUST_CLAY,
         Fluids.WATER,
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         IC2Tags.DUST_CHARCOAL
      );
      recipes.addShapelessIC2Recipe("cfoam_0", new ItemStack(IC2Blocks.CFOAM_BLOCK_LIGHT_GRAY), IC2Blocks.CFOAM_WET, Items.SAND);
      recipes.addShapedIC2Recipe("cfoam_1", new ItemStack(IC2Blocks.CFOAM_BLOCK_LIGHT_GRAY, 8), "XXX", "XYX", "XXX", 'X', IC2Blocks.CFOAM_WET, 'Y', Items.SAND);

      for (DyeColor color : DyeColor.values()) {
         recipes.addShapelessIC2Recipe(
            "painter_" + color.getSerializedName(), new ItemStack((ItemLike)ColorMaps.PAINTER.get(color)), ColorMaps.getTagForDye(color), IC2Items.PAINTER
         );
         recipes.addShapedIC2Recipe(
            "cfoam_" + color.getSerializedName() + "_0",
            new ItemStack(ColorMaps.CFOAM_BLOCKS.getBlock(color), 8),
            "XXX",
            "XYX",
            "XXX",
            'X',
            IC2Tags.CFOAM_BLOCK,
            'Y',
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapelessIC2Recipe(
            "cfoam_" + color.getSerializedName() + "_1",
            new ItemStack(ColorMaps.CFOAM_BLOCKS.getBlock(color)),
            IC2Tags.CFOAM_BLOCK,
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_wool_" + color.getSerializedName() + "_0",
            new ItemStack(ColorMaps.CFOAM_WOOL.getBlock(color), 8),
            "XXX",
            "XYX",
            "XXX",
            'X',
            IC2Tags.CFOAM_WOOL,
            'Y',
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapelessIC2Recipe(
            "cfoam_wool_" + color.getSerializedName() + "_1",
            new ItemStack(ColorMaps.CFOAM_WOOL.getBlock(color)),
            IC2Tags.CFOAM_WOOL,
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapelessIC2Recipe(
            "cfoam_wool_" + color.getSerializedName() + "_2",
            new ItemStack(ColorMaps.CFOAM_WOOL.getBlock(color)),
            IC2Blocks.CFOAM_WET,
            PainterHelper.WOOL.getBlock(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_wool_" + color.getSerializedName() + "_3",
            new ItemStack(ColorMaps.CFOAM_WOOL.getBlock(color), 8),
            "XXX",
            "XYX",
            "XXX",
            'X',
            IC2Blocks.CFOAM_WET,
            'Y',
            PainterHelper.WOOL.getBlock(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_wool_carpet_" + color.getSerializedName() + "_0",
            new ItemStack(ColorMaps.CFOAM_CARPET.getBlock(color), 3),
            "XX",
            'X',
            ColorMaps.CFOAM_WOOL.getBlock(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_wool_carpet_" + color.getSerializedName() + "_1",
            new ItemStack(ColorMaps.CFOAM_CARPET.getBlock(color), 8),
            "XXX",
            "XYX",
            "XXX",
            'X',
            IC2Tags.CFOAM_WOOL_CARPET,
            'Y',
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapelessIC2Recipe(
            "cfoam_carpet_" + color.getSerializedName() + "_3",
            new ItemStack(ColorMaps.CFOAM_CARPET.getBlock(color)),
            IC2Tags.CFOAM_WOOL_CARPET,
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_stairs_" + color.getSerializedName() + "_0",
            new ItemStack(ColorMaps.CFOAM_STAIRS.getBlock(color), 4),
            "X  ",
            "XX ",
            "XXX",
            'X',
            ColorMaps.CFOAM_BLOCKS.getBlock(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_stairs_" + color.getSerializedName() + "_1",
            new ItemStack(ColorMaps.CFOAM_STAIRS.getBlock(color), 8),
            "XXX",
            "XYX",
            "XXX",
            'X',
            IC2Tags.CFOAM_STAIRS,
            'Y',
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapelessIC2Recipe(
            "cfoam_stairs_" + color.getSerializedName() + "_2",
            new ItemStack(ColorMaps.CFOAM_STAIRS.getBlock(color)),
            IC2Tags.CFOAM_STAIRS,
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_slabs_" + color.getSerializedName() + "_0",
            new ItemStack(ColorMaps.CFOAM_SLABS.getBlock(color), 6),
            "XXX",
            'X',
            ColorMaps.CFOAM_BLOCKS.getBlock(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_slabs_" + color.getSerializedName() + "_1",
            new ItemStack(ColorMaps.CFOAM_SLABS.getBlock(color), 8),
            "XXX",
            "XYX",
            "XXX",
            'X',
            IC2Tags.CFOAM_SLABS,
            'Y',
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapelessIC2Recipe(
            "cfoam_slabs_" + color.getSerializedName() + "_2",
            new ItemStack(ColorMaps.CFOAM_SLABS.getBlock(color)),
            IC2Tags.CFOAM_SLABS,
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_surface_" + color.getSerializedName() + "_0",
            new ItemStack(ColorMaps.CFOAM_SURFACE.getBlock(color), 8),
            "XXX",
            "XYX",
            "XXX",
            'X',
            IC2Tags.CFOAM_SURFACE,
            'Y',
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapelessIC2Recipe(
            "cfoam_surface_" + color.getSerializedName() + "_1",
            new ItemStack(ColorMaps.CFOAM_SURFACE.getBlock(color)),
            IC2Tags.CFOAM_SURFACE,
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_panel_" + color.getSerializedName() + "_0",
            new ItemStack(ColorMaps.CFOAM_PANELS.getBlock(color), 8),
            "XXX",
            "XYX",
            "XXX",
            'X',
            IC2Tags.CFOAM_PANEL,
            'Y',
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapelessIC2Recipe(
            "cfoam_panel_" + color.getSerializedName() + "_1",
            new ItemStack(ColorMaps.CFOAM_PANELS.getBlock(color)),
            IC2Tags.CFOAM_PANEL,
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_wall_" + color.getSerializedName() + "_0",
            new ItemStack(ColorMaps.CFOAM_WALLS.getBlock(color), 6),
            "XXX",
            "XXX",
            'X',
            ColorMaps.CFOAM_BLOCKS.getBlock(color)
         );
         recipes.addShapelessIC2Recipe(
            "cfoam_wall_" + color.getSerializedName() + "_1",
            new ItemStack(ColorMaps.CFOAM_WALLS.getBlock(color)),
            IC2Tags.CFOAM_WALLS,
            ColorMaps.getTagForDye(color)
         );
         recipes.addShapedIC2Recipe(
            "cfoam_wall_" + color.getSerializedName() + "_2",
            new ItemStack(ColorMaps.CFOAM_WALLS.getBlock(color), 8),
            "XXX",
            "XYX",
            "XXX",
            'X',
            IC2Tags.CFOAM_WALLS,
            'Y',
            ColorMaps.getTagForDye(color)
         );
         recipes.addStoneCutterIC2Recipe(
            "stonecutter_cfoam_stairs_" + color.getSerializedName(),
            new ItemStack(ColorMaps.CFOAM_STAIRS.getBlock(color)),
            ColorMaps.CFOAM_BLOCKS.getBlock(color)
         );
         recipes.addStoneCutterIC2Recipe(
            "stonecutter_cfoam_slabs_" + color.getSerializedName(),
            new ItemStack(ColorMaps.CFOAM_SLABS.getBlock(color), 2),
            ColorMaps.CFOAM_BLOCKS.getBlock(color)
         );
         recipes.addStoneCutterIC2Recipe(
            "stonecutter_cfoam_wall_" + color.getSerializedName(), new ItemStack(ColorMaps.CFOAM_WALLS.getBlock(color)), ColorMaps.CFOAM_BLOCKS.getBlock(color)
         );
         recipes.addStoneCutterIC2Recipe(
            "stonecutter_cfoam_surface_" + color.getSerializedName(),
            new ItemStack(ColorMaps.CFOAM_SURFACE.getBlock(color), 8),
            ColorMaps.CFOAM_BLOCKS.getBlock(color)
         );
         recipes.addStoneCutterIC2Recipe(
            "stonecutter_cfoam_railing_" + color.getSerializedName(),
            new ItemStack(ColorMaps.CFOAM_PANELS.getBlock(color), 8),
            ColorMaps.CFOAM_BLOCKS.getBlock(color)
         );
      }

      recipes.addShapelessIC2Recipe(
         "pixelfoam_bolt",
         new ItemStack(IC2Blocks.PIXELBLOCK_BOLT),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.GRAY),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.RED)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_crown",
         new ItemStack(IC2Blocks.PIXELBLOCK_CROWN),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.PURPLE),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.YELLOW)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_fire",
         new ItemStack(IC2Blocks.PIXELBLOCK_FIRE),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.RED),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.YELLOW)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_greg",
         new ItemStack(IC2Blocks.PIXELBLOCK_GREG),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.BLUE),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.YELLOW)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_half",
         new ItemStack(IC2Blocks.PIXELBLOCK_HALF_LIFE),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.BLACK),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.ORANGE)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_heart",
         new ItemStack(IC2Blocks.PIXELBLOCK_HEART),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.PINK),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.RED)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_ic",
         new ItemStack(IC2Blocks.PIXELBLOCK_IC2),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.LIGHT_BLUE),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.BLUE)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_muffin",
         new ItemStack(IC2Blocks.PIXELBLOCK_MUFFIN_BUTTON),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.MAGENTA),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.BROWN)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_nuclear",
         new ItemStack(IC2Blocks.PIXELBLOCK_NUCLEAR),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.YELLOW),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.BLACK)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_portal",
         new ItemStack(IC2Blocks.PIXELBLOCK_PORTAL),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.WHITE),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.LIGHT_GRAY)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_rose",
         new ItemStack(IC2Blocks.PIXELBLOCK_ROSE),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.GREEN),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.PINK)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_triforce",
         new ItemStack(IC2Blocks.PIXELBLOCK_TRIFORCE),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.GREEN),
         ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.YELLOW)
      );
      recipes.addShapelessIC2Recipe(
         "pixelfoam_space_bean",
         new ItemStack(IC2Blocks.PIXELBLOCK_SPACE_BEAN, 4),
         new ItemStack(ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.GRAY), 4),
         Items.GOLDEN_SWORD
      );
   }

   private static void addEnergyStorage(IAdvancedCraftingManager recipes) {
      recipes.addShapedIC2Recipe(
         "batbox",
         new ItemStack(IC2Blocks.BATBOX),
         "XCX",
         "YYY",
         "XXX",
         'X',
         ItemTags.PLANKS,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'Y',
         IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "mfe_0",
         new ItemStack(IC2Blocks.MFE),
         "XYX",
         "YCY",
         "XYX",
         'C',
         IC2Blocks.MACHINE_BLOCK,
         'Y',
         IC2Items.ENERGY_CRYSTAL,
         'X',
         IC2Items.GOLD_CABLE_2X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "mfe_1",
         new ItemStack(IC2Blocks.MFE),
         "XYX",
         "YCY",
         "XYX",
         'C',
         IC2Blocks.MACHINE_BLOCK,
         'Y',
         IC2Items.ENERGY_CRYSTAL,
         'X',
         new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED, 4)
      );
      recipes.addShapedIC2Recipe(
         "mfsu",
         new ItemStack(IC2Blocks.MFSU),
         "LCL",
         "LML",
         "LAL",
         'M',
         IC2Blocks.MFE,
         'A',
         IC2Blocks.ADVANCED_MACHINE_BLOCK,
         'C',
         IC2Items.ADVANCED_CIRCUIT,
         'L',
         IC2Items.LAPOTRON_CRYSTAL
      );
      recipes.addShapedIC2Recipe(
         "esu",
         new ItemStack(IC2Blocks.ESU),
         "XVX",
         "YCY",
         "XBX",
         'C',
         IC2Blocks.MACHINE_BLOCK,
         'Y',
         IC2Items.GLOWTRONIC_CRYSTAL,
         'X',
         IC2Items.IRON_CABLE_4X_INSULATED,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.STABILIZED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "isu",
         new ItemStack(IC2Blocks.ISU),
         "LCL",
         "LML",
         "LAL",
         'M',
         IC2Blocks.ESU,
         'A',
         IC2Blocks.STABILIZED_MACHINE_BLOCK,
         'C',
         IC2Items.PLATE_IRIDIUM,
         'L',
         IC2Items.UESC
      );
      recipes.addShapedIC2Recipe(
         "pesu", new ItemStack(IC2Blocks.PESU), "XYX", "CCC", "XYX", 'X', IC2Blocks.IRIDIUM_STONE, 'Y', IC2Items.PLATE_IRIDIUM, 'C', IC2Items.PESD
      );
      recipes.addShapedIC2Recipe(
         "lv_charging_bench",
         new ItemStack(IC2Blocks.CHARGING_BENCH_LV),
         "XYX",
         "C C",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'C',
         ItemTags.PLANKS,
         'B',
         IC2Blocks.BATBOX
      );
      recipes.addShapedIC2Recipe(
         "mv_charging_bench",
         new ItemStack(IC2Blocks.CHARGING_BENCH_MV),
         "XYX",
         "CVC",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'C',
         ItemTags.PLANKS,
         'V',
         IC2Items.CIRCUIT,
         'B',
         IC2Blocks.MFE
      );
      recipes.addShapedIC2Recipe(
         "mv_charging_bench_bronze",
         new ItemStack(IC2Blocks.CHARGING_BENCH_MV),
         "XYX",
         "CVC",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED, 2),
         'C',
         ItemTags.PLANKS,
         'V',
         IC2Items.CIRCUIT,
         'B',
         IC2Blocks.MFE
      );
      recipes.addShapedIC2Recipe(
         "hv_charging_bench",
         new ItemStack(IC2Blocks.CHARGING_BENCH_HV),
         "XYX",
         "CVC",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         IC2Items.GLASSFIBER_CABLE,
         'C',
         ItemTags.PLANKS,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.MFSU
      );
      recipes.addShapedIC2Recipe(
         "ev_charging_bench",
         new ItemStack(IC2Blocks.CHARGING_BENCH_EV),
         "XYX",
         "CVC",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         IC2Items.IRON_CABLE_4X_INSULATED,
         'C',
         ItemTags.PLANKS,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.ESU
      );
      recipes.addShapedIC2Recipe(
         "iv_charging_bench",
         new ItemStack(IC2Blocks.CHARGING_BENCH_IV),
         "XYX",
         "CVC",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         IC2Items.SUPER_CABLE,
         'C',
         ItemTags.PLANKS,
         'V',
         IC2Items.COMPLEX_CIRCUIT,
         'B',
         IC2Blocks.ISU
      );
      recipes.addShapedIC2Recipe(
         "lv_battery_station",
         new ItemStack(IC2Blocks.BATTERY_STATION_LV),
         "XYX",
         "C C",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'C',
         ItemTags.PLANKS,
         'B',
         IC2Blocks.TRANSFORMER_LV
      );
      recipes.addShapedIC2Recipe(
         "mv_battery_station",
         new ItemStack(IC2Blocks.BATTERY_STATION_MV),
         "XYX",
         "CVC",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'C',
         ItemTags.PLANKS,
         'V',
         IC2Items.CIRCUIT,
         'B',
         IC2Blocks.TRANSFORMER_MV
      );
      recipes.addShapedIC2Recipe(
         "mv_battery_station_bronze",
         new ItemStack(IC2Blocks.BATTERY_STATION_MV),
         "XYX",
         "CVC",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED, 2),
         'C',
         ItemTags.PLANKS,
         'V',
         IC2Items.CIRCUIT,
         'B',
         IC2Blocks.TRANSFORMER_MV
      );
      recipes.addShapedIC2Recipe(
         "hv_battery_station",
         new ItemStack(IC2Blocks.BATTERY_STATION_HV),
         "XYX",
         "CVC",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         IC2Items.GLASSFIBER_CABLE,
         'C',
         ItemTags.PLANKS,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.TRANSFORMER_HV
      );
      recipes.addShapedIC2Recipe(
         "ev_battery_station",
         new ItemStack(IC2Blocks.BATTERY_STATION_EV),
         "XYX",
         "CVC",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         IC2Items.IRON_CABLE_4X_INSULATED,
         'C',
         ItemTags.PLANKS,
         'V',
         IC2Items.ADVANCED_CIRCUIT,
         'B',
         IC2Blocks.TRANSFORMER_EV
      );
      recipes.addShapedIC2Recipe(
         "iv_battery_station",
         new ItemStack(IC2Blocks.BATTERY_STATION_IV),
         "XYX",
         "CVC",
         "CBC",
         'X',
         IC2Tags.RUBBER,
         'Y',
         IC2Items.SUPER_CABLE,
         'C',
         ItemTags.PLANKS,
         'V',
         IC2Items.COMPLEX_CIRCUIT,
         'B',
         IC2Blocks.TRANSFORMER_IV
      );
      recipes.addShapedIC2Recipe(
         "lv_transformer",
         new ItemStack(IC2Blocks.TRANSFORMER_LV),
         "PCP",
         "ccc",
         "PCP",
         'P',
         ItemTags.PLANKS,
         'C',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'c',
         IC2Tags.INGOT_COPPER
      );
      recipes.addShapedIC2Recipe(
         "mv_transformer_0", new ItemStack(IC2Blocks.TRANSFORMER_MV), " C ", " M ", " C ", 'M', IC2Blocks.MACHINE_BLOCK, 'C', IC2Items.GOLD_CABLE_2X_INSULATED
      );
      recipes.addShapedIC2Recipe(
         "mv_transformer_1",
         new ItemStack(IC2Blocks.TRANSFORMER_MV),
         " C ",
         " M ",
         " C ",
         'M',
         IC2Blocks.MACHINE_BLOCK,
         'C',
         new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED, 2)
      );
      recipes.addShapedIC2Recipe(
         "hv_transformer",
         new ItemStack(IC2Blocks.TRANSFORMER_HV),
         " c ",
         "CED",
         " c ",
         'E',
         IC2Blocks.TRANSFORMER_MV,
         'c',
         IC2Items.IRON_CABLE_4X_INSULATED,
         'D',
         IC2Items.ENERGY_CRYSTAL,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "ev_transformer",
         new ItemStack(IC2Blocks.TRANSFORMER_EV),
         " c ",
         "CED",
         " c ",
         'E',
         IC2Blocks.TRANSFORMER_HV,
         'c',
         IC2Tags.INGOT_SILVER,
         'D',
         IC2Items.LAPOTRON_CRYSTAL,
         'C',
         IC2Items.ADVANCED_CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "iv_transformer",
         new ItemStack(IC2Blocks.TRANSFORMER_IV),
         "XYX",
         "CVB",
         "XYX",
         'X',
         IC2Blocks.IRIDIUM_STONE,
         'Y',
         IC2Items.PLASMA_CABLE,
         'C',
         IC2Items.ADVANCED_CIRCUIT,
         'V',
         IC2Blocks.TRANSFORMER_EV,
         'B',
         IC2Items.PESD
      );
      recipes.addShapedIC2Recipe(
         "adjustable_transformer",
         new ItemStack(IC2Blocks.TRANSFORMER_ADJUSTABLE),
         "X",
         "Y",
         "C",
         'X',
         IC2Blocks.TRANSFORMER_LV,
         'Y',
         IC2Items.COMPLEX_CIRCUIT,
         'C',
         IC2Blocks.TRANSFORMER_EV
      );
      recipes.addShapedIC2Recipe(
         "redirector_master",
         new ItemStack(IC2Blocks.REDIRECTOR_MASTER),
         " X ",
         "YCY",
         " V ",
         'X',
         IC2Items.DETECTOR_CABLE,
         'Y',
         IC2Items.SPLITTER_CABLE,
         'C',
         IC2Blocks.TRANSFORMER_MV,
         'V',
         IC2Blocks.ADVANCED_MACHINE_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "redirector_slave",
         new ItemStack(IC2Blocks.REDIRECTOR_SLAVE),
         "XXX",
         "XYX",
         "XCX",
         'X',
         IC2Items.GOLD_CABLE_2X_INSULATED,
         'Y',
         IC2Blocks.TRANSFORMER_MV,
         'C',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "monitor",
         new ItemStack(IC2Blocks.MONITOR, 6),
         "XXX",
         "YCV",
         "YBV",
         'X',
         IC2Items.COPPER_CABLE_1X_INSULATED,
         'Y',
         IC2Items.CIRCUIT,
         'V',
         IC2Blocks.LUMINATOR_ADJUSTABLE,
         'C',
         IC2Blocks.BATBOX,
         'B',
         IC2Blocks.REFINED_IRON_BLOCK
      );
      recipes.addShapedIC2Recipe(
         "lv_chargepad",
         new ItemStack(IC2Blocks.LV_CHARGEPAD),
         "XYX",
         "CVC",
         'Y',
         Items.STONE_PRESSURE_PLATE,
         'X',
         IC2Tags.RUBBER,
         'C',
         IC2Items.CIRCUIT,
         'V',
         IC2Blocks.BATBOX
      );
      recipes.addShapedIC2Recipe(
         "mv_chargepad",
         new ItemStack(IC2Blocks.MV_CHARGEPAD),
         "XYX",
         "CVC",
         'Y',
         Items.STONE_PRESSURE_PLATE,
         'X',
         IC2Tags.RUBBER,
         'C',
         IC2Items.CIRCUIT,
         'V',
         IC2Blocks.MFE
      );
      recipes.addShapedIC2Recipe(
         "hv_chargepad",
         new ItemStack(IC2Blocks.HV_CHARGEPAD),
         "XYX",
         "CVC",
         'Y',
         Items.STONE_PRESSURE_PLATE,
         'X',
         IC2Items.CARBON_PLATE,
         'C',
         IC2Items.ADVANCED_CIRCUIT,
         'V',
         IC2Blocks.MFSU
      );
      recipes.addShapedIC2Recipe(
         "nuclear_chargepad",
         new ItemStack(IC2Blocks.FISSION_CHARGEPAD),
         "XYX",
         "CVC",
         'Y',
         IC2Blocks.HV_CHARGEPAD,
         'X',
         IC2Items.PLATE_IRIDIUM,
         'C',
         IC2Items.TFBP_IRRIGATION,
         'V',
         IC2Blocks.NUCLEAR_REACTOR
      );
      recipes.addShapedIC2Recipe(
         "lv_loader",
         new ItemStack(IC2Blocks.LOADER_LV),
         "XYX",
         "XCX",
         "XVX",
         'X',
         ItemTags.PLANKS,
         'Y',
         Items.RAIL,
         'C',
         IC2Items.COPPER_CABLE,
         'V',
         IC2Blocks.BATBOX
      );
      recipes.addShapedIC2Recipe(
         "mv_loader",
         new ItemStack(IC2Blocks.LOADER_MV),
         "XYX",
         "XCX",
         "XVX",
         'X',
         IC2Tags.INGOT_REFINED_IRON,
         'Y',
         Items.RAIL,
         'C',
         IC2Items.GOLD_CABLE,
         'V',
         IC2Blocks.MFE
      );
      recipes.addShapedIC2Recipe(
         "hv_loader",
         new ItemStack(IC2Blocks.LOADER_HV),
         "XYX",
         "XCX",
         "XVX",
         'X',
         IC2Items.CARBON_PLATE,
         'Y',
         Items.RAIL,
         'C',
         IC2Items.GLASSFIBER_CABLE,
         'V',
         IC2Blocks.MFSU
      );
      recipes.addShapedIC2Recipe(
         "ev_loader",
         new ItemStack(IC2Blocks.LOADER_EV),
         "XYX",
         "XCX",
         "XVX",
         'X',
         IC2Tags.INGOT_SILVER,
         'Y',
         Items.RAIL,
         'C',
         IC2Items.SUPER_CABLE,
         'V',
         IC2Blocks.ESU
      );
      recipes.addShapedIC2Recipe(
         "lv_unloader",
         new ItemStack(IC2Blocks.UNLOADER_LV),
         "XYX",
         "XCX",
         "VVV",
         'X',
         ItemTags.PLANKS,
         'V',
         IC2Blocks.TRANSFORMER_LV,
         'C',
         IC2Items.COPPER_CABLE,
         'Y',
         Items.RAIL
      );
      recipes.addShapedIC2Recipe(
         "mv_unloader",
         new ItemStack(IC2Blocks.UNLOADER_MV),
         "XYX",
         "XCX",
         "VVV",
         'X',
         IC2Tags.INGOT_REFINED_IRON,
         'V',
         IC2Blocks.TRANSFORMER_MV,
         'C',
         IC2Items.GOLD_CABLE,
         'Y',
         Items.RAIL
      );
      recipes.addShapedIC2Recipe(
         "hv_unloader",
         new ItemStack(IC2Blocks.UNLOADER_HV),
         "XYX",
         "XCX",
         "VVV",
         'X',
         IC2Items.CARBON_PLATE,
         'V',
         IC2Blocks.TRANSFORMER_HV,
         'C',
         IC2Items.GLASSFIBER_CABLE,
         'Y',
         Items.RAIL
      );
      recipes.addShapedIC2Recipe(
         "ev_unloader",
         new ItemStack(IC2Blocks.UNLOADER_EV),
         "XYX",
         "XCX",
         "VVV",
         'X',
         IC2Tags.INGOT_SILVER,
         'V',
         IC2Blocks.TRANSFORMER_EV,
         'C',
         IC2Items.SUPER_CABLE,
         'Y',
         Items.RAIL
      );
   }

   private static void addReactorComponents(IAdvancedCraftingManager recipes) {
      for (IUraniumRod uranium : ReactorUraniumRod.TYPES) {
         recipes.addShapedIC2Recipe(
            "uranium_" + uranium.getName() + "_near_depleted",
            uranium.createNearDepletedRod(8),
            "XXX",
            "XYX",
            "XXX",
            'Y',
            uranium.getBaseIngot(),
            'X',
            IC2Items.CELL_EMPTY
         );
         recipes.addShapelessIC2Recipe(
            "uranium_" + uranium.getName() + "_isotopic_0", uranium.createIsotopicRod(), uranium.createNearDepletedRod(1), IC2Tags.DUST_COAL
         );
         recipes.addShapelessIC2Recipe(
            "uranium_" + uranium.getName() + "_isotopic_1", uranium.createIsotopicRod(), uranium.createNearDepletedRod(), IC2Tags.DUST_CHARCOAL
         );
         recipes.addShapelessIC2Recipe(
            "uranium_" + uranium.getName() + "_single_0", uranium.createSingleRod(), IC2Tags.DUST_COAL, uranium.createReEnrichedRod()
         );
         recipes.addShapelessIC2Recipe(
            "uranium_" + uranium.getName() + "_single_1", uranium.createSingleRod(), IC2Tags.DUST_CHARCOAL, uranium.createReEnrichedRod()
         );
         recipes.addShapedIC2Recipe(
            "uranium_" + uranium.getName() + "_dual", uranium.createDualRod(), "UCU", 'U', uranium.createSingleRod(), 'C', IC2Items.PLATE_DENSE_COPPER
         );
         recipes.addShapedIC2Recipe(
            "uranium_" + uranium.getName() + "_quad_0",
            uranium.createQuadRod(),
            " U ",
            "CCC",
            " U ",
            'U',
            uranium.createDualRod(),
            'C',
            IC2Items.PLATE_DENSE_COPPER
         );
         recipes.addShapedIC2Recipe(
            "uranium_" + uranium.getName() + "_quad_1",
            uranium.createQuadRod(),
            "UCU",
            "CCC",
            "UCU",
            'U',
            uranium.createSingleRod(),
            'C',
            IC2Items.PLATE_DENSE_COPPER
         );
         if (!uranium.isEnrichedUranium() || !IC2.CONFIG.enableHardEnrichedUranium.get()) {
            recipes.addShapelessIC2Recipe("uranium_" + uranium.getName() + "_single_2", uranium.createSingleRod(), IC2Items.CELL_EMPTY, uranium.getBaseIngot());
         }
      }

      recipes.addShapelessIC2Recipe("uranium__single_2_tag", StandardUranium.INSTANCE.createSingleRod(), IC2Items.CELL_EMPTY, IC2Tags.INGOT_URANIUM);
      recipes.addShapedIC2Recipe(
         "uranium__near_depleted_tag",
         StandardUranium.INSTANCE.createNearDepletedRod(8),
         "XXX",
         "XYX",
         "XXX",
         'Y',
         IC2Tags.INGOT_URANIUM,
         'X',
         IC2Items.CELL_EMPTY
      );
      ItemStack fuelCell = new ItemStack(IC2Items.REACTOR_FUEL_CELL);
      fuelCell.setDamageValue(fuelCell.getMaxDamage());
      recipes.addShapelessIC2Recipe("fuel_cell", fuelCell, IC2Items.CELL_EMPTY, new ItemStack(IC2Items.DUST_NETHERRACK, 8), IC2Items.DUST_CLAY);
      recipes.addShapedIC2Recipe("coolant_cell_10", new ItemStack(IC2Items.COOLANT_CELL_10K), " T ", "TWT", " T ", 'W', Fluids.WATER, 'T', IC2Tags.INGOT_TIN);
      recipes.addShapedIC2Recipe(
         "coolant_cell_30_0", new ItemStack(IC2Items.COOLANT_CELL_30K), "TTT", "CCC", "TTT", 'C', IC2Items.COOLANT_CELL_10K, 'T', IC2Tags.INGOT_TIN
      );
      recipes.addShapedIC2Recipe(
         "coolant_cell_30_1",
         new ItemStack(IC2Items.COOLANT_CELL_30K),
         "TTT",
         "CCC",
         "TTT",
         'C',
         new ItemStack(IC2Items.CELL_WATER, 2),
         'T',
         new ItemTagInput(IC2Tags.INGOT_TIN, 2)
      );
      recipes.addShapedIC2Recipe(
         "coolant_cell_60",
         new ItemStack(IC2Items.COOLANT_CELL_60K),
         "TCT",
         "TcT",
         "TCT",
         'C',
         IC2Items.COOLANT_CELL_30K,
         'T',
         IC2Tags.INGOT_TIN,
         'c',
         IC2Items.PLATE_DENSE_COPPER
      );
      recipes.addShapelessIC2Recipe("plating", new ItemStack(IC2Items.PLATING), IC2Items.PLATE_DENSE_COPPER, IC2Items.PLATE_ADVANCED_ALLOY);
      recipes.addShapelessIC2Recipe(
         "plating_heat", new ItemStack(IC2Items.PLATING_HEAT_CAPACITY), IC2Items.PLATING, IC2Items.PLATE_DENSE_COPPER, IC2Items.PLATE_DENSE_COPPER
      );
      recipes.addShapelessIC2Recipe(
         "plating_containment", new ItemStack(IC2Items.PLATING_CONTAINMENT), IC2Items.PLATING, IC2Items.PLATE_ADVANCED_ALLOY, IC2Items.PLATE_ADVANCED_ALLOY
      );
      recipes.addShapedIC2Recipe(
         "reflector_0", new ItemStack(IC2Items.REFLECTOR), "TcT", "cCc", "TcT", 'c', IC2Tags.DUST_COAL, 'C', IC2Items.PLATE_DENSE_COPPER, 'T', IC2Tags.DUST_TIN
      );
      recipes.addShapedIC2Recipe(
         "reflector_1",
         new ItemStack(IC2Items.REFLECTOR),
         "TcT",
         "cCc",
         "TcT",
         'c',
         IC2Tags.DUST_CHARCOAL,
         'C',
         IC2Items.PLATE_DENSE_COPPER,
         'T',
         IC2Tags.DUST_TIN
      );
      recipes.addShapedIC2Recipe(
         "reflector_thick", new ItemStack(IC2Items.REFLECTOR_THICK), " R ", "RCR", " R ", 'C', IC2Items.PLATE_DENSE_COPPER, 'R', IC2Items.REFLECTOR
      );
      recipes.addShapedIC2Recipe(
         "reflector_iridium",
         new ItemStack(IC2Items.REFLECTOR_IRIDIUM),
         "XYX",
         "YCY",
         "XYX",
         'C',
         IC2Items.PLATE_IRIDIUM,
         'X',
         IC2Items.REFLECTOR_THICK,
         'Y',
         IC2Items.MAGNET
      );
      recipes.addRepairIC2Recipe("reflector_repair_coal", (IRepairable)IC2Items.REFLECTOR_IRIDIUM, new ItemTagInput(IC2Tags.DUST_COAL, 4), 10000);
      recipes.addRepairIC2Recipe("reflector_repair_char_coal", (IRepairable)IC2Items.REFLECTOR_IRIDIUM, new ItemTagInput(IC2Tags.DUST_CHARCOAL, 4), 5000);
      recipes.addShapedIC2Recipe(
         "heat_spreader", new ItemStack(IC2Items.HEAT_SPREADER), "#T#", "TVT", "#T#", 'V', IC2Items.VENT_HEAT, '#', Items.IRON_BARS, 'T', IC2Tags.INGOT_TIN
      );
      recipes.addShapedIC2Recipe(
         "heat_pack", new ItemStack(IC2Items.HEAT_PACK), "c", "L", "C", 'c', IC2Items.CIRCUIT, 'C', IC2Items.PLATE_DENSE_COPPER, 'L', IC2Items.CELL_LAVA
      );
      recipes.addShapedIC2Recipe(
         "condensator",
         new ItemStack(IC2Items.CONDENSATOR),
         "RRR",
         "RVR",
         "RSR",
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'V',
         IC2Items.VENT_HEAT,
         'S',
         IC2Items.HEAT_EXCHANGER
      );
      recipes.addShapedIC2Recipe(
         "condensator_lap",
         new ItemStack(IC2Items.CONDENSATOR_LAP),
         "RVR",
         "CLC",
         "RSR",
         'R',
         net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE,
         'V',
         IC2Items.VENT_HEAT_CORE,
         'S',
         IC2Items.HEAT_EXCHANGER_CORE,
         'C',
         IC2Items.CONDENSATOR,
         'L',
         net.minecraftforge.common.Tags.Items.STORAGE_BLOCKS_LAPIS
      );
      recipes.addRepairIC2Recipe(
         "condensator_repair_redstone", (IRepairable)IC2Items.CONDENSATOR, new ItemTagInput(net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE), 10000
      );
      recipes.addRepairIC2Recipe(
         "condensator_lap_repair_redstone", (IRepairable)IC2Items.CONDENSATOR_LAP, new ItemTagInput(net.minecraftforge.common.Tags.Items.DUSTS_REDSTONE), 4000
      );
      recipes.addRepairIC2Recipe(
         "condensator_lap_repair_lapis", (IRepairable)IC2Items.CONDENSATOR_LAP, new ItemTagInput(net.minecraftforge.common.Tags.Items.GEMS_LAPIS), 40000
      );
      recipes.addShapedIC2Recipe(
         "heat_exchanger",
         new ItemStack(IC2Items.HEAT_EXCHANGER),
         " c ",
         "TCT",
         " T ",
         'c',
         IC2Items.CIRCUIT,
         'T',
         IC2Tags.INGOT_TIN,
         'C',
         IC2Items.PLATE_DENSE_COPPER
      );
      recipes.addShapedIC2Recipe(
         "heat_exchanger_core", new ItemStack(IC2Items.HEAT_EXCHANGER_CORE), "C", "S", "C", 'S', IC2Items.HEAT_EXCHANGER, 'C', IC2Items.PLATE_DENSE_COPPER
      );
      recipes.addShapedIC2Recipe(
         "heat_exchanger_spread",
         new ItemStack(IC2Items.HEAT_EXCHANGER_SPREAD),
         " G ",
         "GSG",
         " G ",
         'S',
         IC2Items.HEAT_EXCHANGER,
         'G',
         net.minecraftforge.common.Tags.Items.INGOTS_GOLD
      );
      recipes.addShapedIC2Recipe(
         "heat_exchanger_adv",
         new ItemStack(IC2Items.HEAT_EXCHANGER_ADVANCED),
         "GcG",
         "SCS",
         "GcG",
         'S',
         IC2Items.HEAT_EXCHANGER,
         'C',
         IC2Items.PLATE_DENSE_COPPER,
         'G',
         IC2Items.GLASSFIBER_CABLE,
         'c',
         IC2Items.CIRCUIT
      );
      recipes.addShapedIC2Recipe(
         "heat_balancer",
         new ItemStack(IC2Items.HEAT_BALANCER),
         "XYX",
         "YCY",
         "XYX",
         'Y',
         IC2Items.HEAT_EXCHANGER,
         'X',
         IC2Tags.INGOT_SILVER,
         'C',
         IC2Items.PLATE_DENSE_COPPER
      );
      recipes.addShapedIC2Recipe(
         "heat_balancer_core",
         new ItemStack(IC2Items.HEAT_BALANCER_CORE),
         "XYX",
         "YCY",
         "XYX",
         'Y',
         IC2Items.HEAT_EXCHANGER_CORE,
         'X',
         IC2Tags.INGOT_SILVER,
         'C',
         IC2Items.PLATE_DENSE_COPPER
      );
      recipes.addShapedIC2Recipe(
         "heat_balancer_spread",
         new ItemStack(IC2Items.HEAT_BALANCER_SPREAD),
         "XYX",
         "YCY",
         "XYX",
         'Y',
         IC2Items.HEAT_EXCHANGER_SPREAD,
         'X',
         IC2Tags.INGOT_SILVER,
         'C',
         IC2Items.PLATE_DENSE_COPPER
      );
      recipes.addShapedIC2Recipe(
         "heat_balancer_adv",
         new ItemStack(IC2Items.HEAT_BALANCER_ADVANCED),
         "XYX",
         "YCY",
         "XYX",
         'Y',
         IC2Items.HEAT_EXCHANGER_ADVANCED,
         'X',
         IC2Tags.INGOT_SILVER,
         'C',
         IC2Items.PLATE_DENSE_COPPER
      );
      recipes.addShapedIC2Recipe(
         "heat_pump",
         new ItemStack(IC2Items.HEAT_PUMP),
         "XYX",
         "YCY",
         "XYX",
         'Y',
         IC2Items.HEAT_EXCHANGER_SPREAD,
         'X',
         Items.REDSTONE_TORCH,
         'C',
         IC2Blocks.HIGH_PRESSURE_PIPE
      );
      recipes.addShapedIC2Recipe("vent_heat_0", new ItemStack(IC2Items.VENT_HEAT), "I#I", "# #", "I#I", 'I', IC2Tags.INGOT_REFINED_IRON, '#', Items.IRON_BARS);
      recipes.addShapedIC2Recipe(
         "vent_heat_core_0", new ItemStack(IC2Items.VENT_HEAT_CORE), "C", "V", "C", 'V', IC2Items.VENT_HEAT, 'C', IC2Items.PLATE_DENSE_COPPER
      );
      recipes.addShapedIC2Recipe(
         "vent_heat_overclocked_0",
         new ItemStack(IC2Items.VENT_HEAT_OVERCLOCKED),
         "G",
         "V",
         "G",
         'V',
         IC2Items.VENT_HEAT_CORE,
         'G',
         net.minecraftforge.common.Tags.Items.INGOTS_GOLD
      );
      recipes.addShapedIC2Recipe(
         "vent_heat_adv_0",
         new ItemStack(IC2Items.VENT_HEAT_ADVANCED),
         "#V#",
         "#D#",
         "#V#",
         'V',
         IC2Items.VENT_HEAT,
         '#',
         Items.IRON_BARS,
         'D',
         net.minecraftforge.common.Tags.Items.GEMS_DIAMOND
      );
      recipes.addShapedIC2Recipe(
         "vent_steam",
         new ItemStack(IC2Items.VENT_STEAM),
         "I#I",
         "#X#",
         "I#I",
         'X',
         IC2Items.VENT_HEAT,
         'I',
         net.minecraftforge.common.Tags.Items.GLASS,
         '#',
         Items.IRON_BARS
      );
      recipes.addShapedIC2Recipe(
         "vent_steam_core_0", new ItemStack(IC2Items.VENT_STEAM_CORE), "C", "V", "C", 'V', IC2Items.VENT_STEAM, 'C', IC2Items.PLATE_DENSE_COPPER
      );
      recipes.addShapedIC2Recipe(
         "vent_steam_core_1",
         new ItemStack(IC2Items.VENT_STEAM_CORE),
         "I#I",
         "#X#",
         "I#I",
         'X',
         IC2Items.VENT_HEAT_CORE,
         'I',
         net.minecraftforge.common.Tags.Items.GLASS,
         '#',
         Items.IRON_BARS
      );
      recipes.addShapedIC2Recipe(
         "vent_steam_overclocked_0",
         new ItemStack(IC2Items.VENT_STEAM_OVERCLOCKED),
         "G",
         "V",
         "G",
         'V',
         IC2Items.VENT_STEAM_CORE,
         'G',
         net.minecraftforge.common.Tags.Items.INGOTS_GOLD
      );
      recipes.addShapedIC2Recipe(
         "vent_steam_overclocked_1",
         new ItemStack(IC2Items.VENT_STEAM_OVERCLOCKED),
         "I#I",
         "#X#",
         "I#I",
         'X',
         IC2Items.VENT_HEAT_OVERCLOCKED,
         'I',
         net.minecraftforge.common.Tags.Items.GLASS,
         '#',
         Items.IRON_BARS
      );
      recipes.addShapedIC2Recipe(
         "vent_steam_adv_0",
         new ItemStack(IC2Items.VENT_STEAM_ADVANCED),
         "#V#",
         "#D#",
         "#V#",
         'V',
         IC2Items.VENT_STEAM,
         '#',
         Items.IRON_BARS,
         'D',
         net.minecraftforge.common.Tags.Items.GEMS_DIAMOND
      );
      recipes.addShapedIC2Recipe(
         "vent_steam_adv_1",
         new ItemStack(IC2Items.VENT_STEAM_ADVANCED),
         "I#I",
         "#X#",
         "I#I",
         'X',
         IC2Items.VENT_HEAT_ADVANCED,
         'I',
         net.minecraftforge.common.Tags.Items.GLASS,
         '#',
         Items.IRON_BARS
      );
      recipes.addShapelessIC2Recipe("vent_heat_1", new ItemStack(IC2Items.VENT_HEAT), IC2Items.VENT_STEAM);
      recipes.addShapelessIC2Recipe("vent_heat_core_1", new ItemStack(IC2Items.VENT_HEAT_CORE), IC2Items.VENT_STEAM_CORE);
      recipes.addShapelessIC2Recipe("vent_heat_overclocked_1", new ItemStack(IC2Items.VENT_HEAT_OVERCLOCKED), IC2Items.VENT_STEAM_OVERCLOCKED);
      recipes.addShapelessIC2Recipe("vent_heat_adv_1", new ItemStack(IC2Items.VENT_HEAT_ADVANCED), IC2Items.VENT_STEAM_ADVANCED);
      recipes.addShapedIC2Recipe(
         "vent_electric",
         new ItemStack(IC2Items.VENT_ELECTRIC),
         "BCB",
         " A ",
         'C',
         IC2Items.VENT_HEAT,
         'B',
         IC2Items.ADVANCED_CIRCUIT,
         'A',
         IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "vent_electric_core",
         new ItemStack(IC2Items.VENT_ELECTRIC_CORE),
         "BCB",
         " A ",
         'C',
         IC2Items.VENT_HEAT_CORE,
         'B',
         IC2Items.ADVANCED_CIRCUIT,
         'A',
         IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "vent_electric_overclocked",
         new ItemStack(IC2Items.VENT_ELECTRIC_OVERCLOCKED),
         "BCB",
         " A ",
         'C',
         IC2Items.VENT_HEAT_OVERCLOCKED,
         'B',
         IC2Items.ADVANCED_CIRCUIT,
         'A',
         IC2Items.RE_BATTERY
      );
      recipes.addShapedIC2Recipe(
         "vent_electric_adv",
         new ItemStack(IC2Items.VENT_ELECTRIC_ADVANCED),
         "BCB",
         " A ",
         'C',
         IC2Items.VENT_HEAT_ADVANCED,
         'B',
         IC2Items.ADVANCED_CIRCUIT,
         'A',
         IC2Items.RE_BATTERY
      );
      recipes.addShapelessIC2Recipe("vent_heat_2", new ItemStack(IC2Items.VENT_HEAT), IC2Items.VENT_ELECTRIC);
      recipes.addShapelessIC2Recipe("vent_heat_core_2", new ItemStack(IC2Items.VENT_HEAT_CORE), IC2Items.VENT_ELECTRIC_CORE);
      recipes.addShapelessIC2Recipe("vent_heat_overclocked_2", new ItemStack(IC2Items.VENT_HEAT_OVERCLOCKED), IC2Items.VENT_ELECTRIC_OVERCLOCKED);
      recipes.addShapelessIC2Recipe("vent_heat_adv_2", new ItemStack(IC2Items.VENT_HEAT_ADVANCED), IC2Items.VENT_ELECTRIC_ADVANCED);
   }
}
