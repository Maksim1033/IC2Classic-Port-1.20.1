package ic2.core.platform.registries;

import ic2.api.blocks.BlockRegistries;
import ic2.api.blocks.PainterHelper;
import ic2.core.platform.recipes.helpers.DefaultOreSearcher;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.SimpleRegistry;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class IC2Tags {
   public static final SimpleRegistry<Set<String>> TAGS_TO_LOAD = CollectionUtils.createRegistry();
   public static TagKey<Item> TOOLBOX = createItemTag("ic2", "toolbox");
   public static TagKey<Item> LOGS = ItemTags.LOGS;
   public static TagKey<Item> RUBBER = createItemTag("ic2", "rubber");
   public static TagKey<Item> ORE_COPPER = createForgeItemTag("ores/copper");
   public static TagKey<Item> ORE_TIN = createForgeItemTag("ores/tin");
   public static TagKey<Item> ORE_SILVER = createForgeItemTag("ores/silver");
   public static TagKey<Item> ORE_URANIUM = createForgeItemTag("ores/uranium");
   public static TagKey<Item> ORE_ALUMINIUM = createForgeItemTag("ores/aluminium");
   public static TagKey<Block> ORE_TIN_BLOCK = createForgeBlockTag("ores/tin");
   public static TagKey<Block> ORE_SILVER_BLOCK = createForgeBlockTag("ores/silver");
   public static TagKey<Block> ORE_ALUMINIUM_BLOCK = createForgeBlockTag("ores/aluminium");
   public static TagKey<Item> RAW_TIN = createForgeItemTag("raw_materials/tin");
   public static TagKey<Item> RAW_SILVER = createForgeItemTag("raw_materials/silver");
   public static TagKey<Item> RAW_URANIUM = createForgeItemTag("raw_materials/uranium");
   public static TagKey<Item> RAW_ALUMINIUM = createForgeItemTag("raw_materials/aluminium");
   public static TagKey<Item> DUST_COPPER = createForgeItemTag("dusts/copper");
   public static TagKey<Item> DUST_TIN = createForgeItemTag("dusts/tin");
   public static TagKey<Item> DUST_SILVER = createForgeItemTag("dusts/silver");
   public static TagKey<Item> DUST_BRONZE = createForgeItemTag("dusts/bronze");
   public static TagKey<Item> DUST_ALUMINIUM = createForgeItemTag("dusts/aluminium");
   public static TagKey<Item> DUST_IRON = createForgeItemTag("dusts/iron");
   public static TagKey<Item> DUST_GOLD = createForgeItemTag("dusts/gold");
   public static TagKey<Item> DUST_COAL = createForgeItemTag("dusts/coal");
   public static TagKey<Item> DUST_CHARCOAL = createForgeItemTag("dusts/charcoal");
   public static TagKey<Item> INGOT_COPPER = createForgeItemTag("ingots/copper");
   public static TagKey<Item> INGOT_TIN = createForgeItemTag("ingots/tin");
   public static TagKey<Item> INGOT_SILVER = createForgeItemTag("ingots/silver");
   public static TagKey<Item> INGOT_BRONZE = createForgeItemTag("ingots/bronze");
   public static TagKey<Item> INGOT_ALUMINIUM = createForgeItemTag("ingots/aluminium");
   public static TagKey<Item> INGOT_REFINED_IRON = createForgeItemTag("ingots/refined_iron");
   public static TagKey<Item> INGOT_URANIUM = createForgeItemTag("ingots/uranium");
   public static TagKey<Item> STORAGE_COPPER = createForgeItemTag("storage_blocks/copper");
   public static TagKey<Item> STORAGE_TIN = createForgeItemTag("storage_blocks/tin");
   public static TagKey<Item> STORAGE_SILVER = createForgeItemTag("storage_blocks/silver");
   public static TagKey<Item> STORAGE_BRONZE = createForgeItemTag("storage_blocks/bronze");
   public static TagKey<Item> STORAGE_ALUMINIUM = createForgeItemTag("storage_blocks/aluminium");
   public static TagKey<Item> STORAGE_REFINED_IRON = createForgeItemTag("storage_blocks/refined_iron");
   public static TagKey<Block> STORAGE_COPPER_BLOCK = createForgeBlockTag("storage_blocks/copper_all");
   public static TagKey<Block> STORAGE_TIN_BLOCK = createForgeBlockTag("storage_blocks/tin");
   public static TagKey<Block> STORAGE_SILVER_BLOCK = createForgeBlockTag("storage_blocks/silver");
   public static TagKey<Block> STORAGE_ALUMINIUM_BLOCK = createForgeBlockTag("storage_blocks/aluminium");
   public static TagKey<Item> STORAGE_RAW_TIN = createForgeItemTag("storage_blocks/raw_tin");
   public static TagKey<Item> STORAGE_RAW_SILVER = createForgeItemTag("storage_blocks/raw_silver");
   public static TagKey<Item> STORAGE_RAW_ALUMINIUM = createForgeItemTag("storage_blocks/raw_aluminium");
   public static TagKey<Item> STORAGE_RAW_URANIUM = createForgeItemTag("storage_blocks/raw_uranium");
   public static TagKey<Item> CFOAM_BLOCK = createItemTag("ic2", "cfoam/block");
   public static TagKey<Item> CFOAM_WOOL = createItemTag("ic2", "cfoam/wool");
   public static TagKey<Item> CFOAM_WOOL_CARPET = createItemTag("ic2", "cfoam/carpet");
   public static TagKey<Item> CFOAM_STAIRS = createItemTag("ic2", "cfoam/stairs");
   public static TagKey<Item> CFOAM_SLABS = createItemTag("ic2", "cfoam/slabs");
   public static TagKey<Item> CFOAM_SURFACE = createItemTag("ic2", "cfoam/surface");
   public static TagKey<Item> CFOAM_PANEL = createItemTag("ic2", "cfoam/panel");
   public static TagKey<Item> CFOAM_WALLS = createItemTag("ic2", "cfoam/walls");
   public static TagKey<Block> ROCK_CUTTER_BLOCKS = createBlockTag("ic2", "rock_cutter");
   public static TagKey<Block> MINING_LASER_UNBREAKABLE = createBlockTag("ic2", "mining_laser_unbreakable");
   public static TagKey<Block> TANK_STRUCTURE = createBlockTag("ic2", "tank_structure_blocks");
   public static TagKey<Block> TERRAFORMER_FLOWERS = createBlockTag("ic2", "flowers");
   public static TagKey<Block> TERRAFORMER_GROUND = createBlockTag("ic2", "ground");
   public static TagKey<Block> TERRAFORMER_FLAT = createBlockTag("ic2", "flat");
   public static TagKey<Block> CAKES = createBlockTag("cakes");
   public static TagKey<Item> DUST_COAL_AND_CHARCOAL = createItemTag("ic2", "coal_and_charcoal");
   public static TagKey<Fluid> STEAM = createFluidTag("forge", "steam");
   public static TagKey<Item> WIKI_SPAWN = createItemTag("ic2", "spawn_wiki");

   public static void initTags() {
      BlockRegistries.registerListener(new DefaultOreSearcher());
      registerMinecraftTag(
         "logs", IC2Blocks.RUBBERWOOD_LOG, IC2Blocks.RUBBER_LOG_STRIPPED, IC2Blocks.RUBBER_LOG_BARKED_STRIPPED, IC2Blocks.RUBBERWOOD_LOG_BARKED
      );
      registerMinecraftTag("planks", IC2Blocks.RUBBERWOOD_PLANKS);
      registerMinecraftTag("leaves", IC2Blocks.RUBBER_LEAVES);
      registerMinecraftTag("saplings", IC2Blocks.RUBBER_SAPLING);
      registerMinecraftTag("wooden_slabs", IC2Blocks.RUBBERWOOD_SLAB);
      registerMinecraftTag("wooden_stairs", IC2Blocks.RUBBERWOOD_STAIRS);
      registerMinecraftTag("wooden_doors", IC2Blocks.RUBBERWOOD_DOOR);
      registerMinecraftTag("wooden_trapdoors", IC2Blocks.RUBBERWOOD_TRAPDOOR);
      registerMinecraftTag("wooden_fences", IC2Blocks.RUBBERWOOD_FENCE);
      registerMinecraftTag("wooden_pressure_plates", IC2Blocks.RUBBERWOOD_PRESSURE_PLATE);
      registerMinecraftTag("wooden_buttons", IC2Blocks.RUBBERWOOD_BUTTON);
      registerMinecraftTag("fences", IC2Blocks.IRON_FENCE);
      registerMinecraftTag("boats", IC2Items.RUBBER_BOAT, IC2Items.RUBBERWOOD_BOAT, IC2Items.CARBON_BOAT, IC2Items.RUBBER_BOAT_BROKEN);
      registerMinecraftTag("signs", IC2Items.RUBBERWOOD_SIGN);
      registerTags(
         new ResourceLocation("tags/blocks/candle_cakes"),
         ForgeRegistries.BLOCKS,
         IC2Blocks.CAKE_CANDLE_CHOCOLATE,
         IC2Blocks.CAKE_CANDLE_COFFEE,
         IC2Blocks.CAKE_CANDLE_HONEY,
         IC2Blocks.CAKE_CANDLE_LEMON
      );
      registerMinecraftTag("cakes", Blocks.CAKE, IC2Blocks.CAKE_CHOCOLATE, IC2Blocks.CAKE_COFFEE, IC2Blocks.CAKE_HONEY, IC2Blocks.CAKE_LEMON);
      registerBlockTag("minecraft", "cakes", BlockTags.CANDLE_CAKES);
      registerSimpleTag("forge", "steam", IC2Fluids.STEAM);
      registerTag("forge", "seeds", "hemp", IC2Items.HEMP_SEEDS);
      registerSimpleTag("ic2", "rubber", IC2Items.RUBBER);

      for (DyeColor color : DyeColor.values()) {
         registerTag("ic2", "cfoam", "block", ColorMaps.CFOAM_BLOCKS.getBlock(color));
         registerTag("ic2", "cfoam", "wool", ColorMaps.CFOAM_WOOL.getBlock(color));
         registerTag("ic2", "cfoam", "carpet", ColorMaps.CFOAM_CARPET.getBlock(color));
         registerTag("ic2", "cfoam", "stairs", ColorMaps.CFOAM_STAIRS.getBlock(color));
         registerTag("ic2", "cfoam", "slabs", ColorMaps.CFOAM_SLABS.getBlock(color));
         registerTag("ic2", "cfoam", "surface", ColorMaps.CFOAM_SURFACE.getBlock(color));
         registerTag("ic2", "cfoam", "panel", ColorMaps.CFOAM_PANELS.getBlock(color));
         registerTag("ic2", "cfoam", "walls", ColorMaps.CFOAM_WALLS.getBlock(color));
         registerMinecraftTag("walls", ColorMaps.CFOAM_WALLS.getBlock(color));
         registerMinecraftTag("stairs", ColorMaps.CFOAM_STAIRS.getBlock(color));
         registerMinecraftTag("slabs", ColorMaps.CFOAM_SLABS.getBlock(color));
         registerMinecraftTag("carpet", ColorMaps.CFOAM_CARPET.getBlock(color));
      }

      registerMinecraftTag("walls", IC2Blocks.TEXTURED_WALL);
      registerTag("forge", "gems", "diamond", IC2Items.INDUSTRIAL_DIAMOND);
      registerTag("forge", "ores", "tin", IC2Blocks.TIN_ORE, IC2Blocks.DEEPSLATE_TIN_ORE);
      registerTag("forge", "ores", "silver", IC2Blocks.SILVER_ORE, IC2Blocks.SILVER_ORE_NETHER, IC2Blocks.DEEPSLATE_SILVER_ORE);
      registerTag("forge", "ores", "uranium", IC2Blocks.URANIUM_ORE, IC2Blocks.DEEPSLATE_URANIUM_ORE);
      registerTag("forge", "ores", "aluminium", IC2Blocks.ALUMINUM_ORE_NETHER);
      registerTag("forge", "raw_materials", "tin", IC2Items.RAW_TIN);
      registerTag("forge", "raw_materials", "silver", IC2Items.RAW_SILVER);
      registerTag("forge", "raw_materials", "aluminium", IC2Items.RAW_ALUMINIUM);
      registerTag("forge", "raw_materials", "uranium", IC2Items.ORE_URANIUM_DROP);
      registerTag("forge", "dusts", "copper", IC2Items.DUST_COPPER);
      registerTag("forge", "dusts", "tin", IC2Items.DUST_TIN);
      registerTag("forge", "dusts", "silver", IC2Items.DUST_SILVER);
      registerTag("forge", "dusts", "bronze", IC2Items.DUST_BRONZE);
      registerTag("forge", "dusts", "aluminium", IC2Items.DUST_ALUMINIUM);
      registerTag("forge", "dusts", "iron", IC2Items.DUST_IRON);
      registerTag("forge", "dusts", "gold", IC2Items.DUST_GOLD);
      registerTag("forge", "dusts", "coal", IC2Items.DUST_COAL);
      registerTag("forge", "dusts", "charcoal", IC2Items.DUST_CHARCOAL);
      registerTag("forge", "ingots", "tin", IC2Items.INGOT_TIN);
      registerTag("forge", "ingots", "silver", IC2Items.INGOT_SILVER);
      registerTag("forge", "ingots", "bronze", IC2Items.INGOT_BRONZE);
      registerTag("forge", "ingots", "aluminium", IC2Items.INGOT_ALUMINIUM);
      registerTag("forge", "ingots", "refined_iron", IC2Items.INGOT_REFINED_IRON);
      registerTag("forge", "ingots", "uranium", IC2Items.INGOT_URANIUM);
      registerTag(
         "forge",
         "storage_blocks",
         "copper_all",
         Blocks.COPPER_ORE,
         Blocks.DEEPSLATE_COPPER_ORE,
         Blocks.COPPER_BLOCK,
         Blocks.EXPOSED_COPPER,
         Blocks.WEATHERED_COPPER,
         Blocks.OXIDIZED_COPPER,
         Blocks.CUT_COPPER,
         Blocks.EXPOSED_CUT_COPPER,
         Blocks.WEATHERED_CUT_COPPER,
         Blocks.OXIDIZED_CUT_COPPER,
         Blocks.WAXED_COPPER_BLOCK,
         Blocks.WAXED_EXPOSED_COPPER,
         Blocks.WAXED_WEATHERED_COPPER,
         Blocks.WAXED_OXIDIZED_COPPER,
         Blocks.WAXED_CUT_COPPER,
         Blocks.WAXED_WEATHERED_CUT_COPPER,
         Blocks.WAXED_EXPOSED_CUT_COPPER,
         Blocks.WAXED_OXIDIZED_CUT_COPPER
      );
      registerTag("forge", "storage_blocks", "tin", IC2Blocks.TIN_BLOCK);
      registerTag("forge", "storage_blocks", "silver", IC2Blocks.SILVER_BLOCK);
      registerTag("forge", "storage_blocks", "bronze", IC2Blocks.BRONZE_BLOCK);
      registerTag("forge", "storage_blocks", "aluminium", IC2Blocks.ALUMINIUM_BLOCK);
      registerTag("forge", "storage_blocks", "refined_iron", IC2Blocks.REFINED_IRON_BLOCK);
      registerTag("forge", "storage_blocks", "raw_tin", IC2Blocks.RAW_TIN_BLOCK);
      registerTag("forge", "storage_blocks", "raw_silver", IC2Blocks.RAW_SILVER_BLOCK);
      registerTag("forge", "storage_blocks", "raw_aluminium", IC2Blocks.RAW_ALUMINIUM_BLOCK);
      registerTag("forge", "storage_blocks", "raw_uranium", IC2Blocks.URANIUM_DROP_BLOCK);
      registerSimpleTag("forge", "gaseous", IC2Fluids.WOOD_GAS, IC2Fluids.STEAM);
      registerSimpleTag(
         "ic2",
         "rock_cutter",
         Blocks.STONE,
         Blocks.DIORITE,
         Blocks.ANDESITE,
         Blocks.GRANITE,
         Blocks.OBSIDIAN,
         Blocks.CRYING_OBSIDIAN,
         Blocks.BLACKSTONE,
         Blocks.BASALT,
         Blocks.DEEPSLATE
      );
      registerSimpleTag("ic2", "mining_laser_unbreakable", Blocks.BRICKS, Blocks.OBSIDIAN, Blocks.LAVA, Blocks.WATER, Blocks.BEDROCK);
      registerBlockTag(
         "ic2",
         "tank_structure_blocks",
         net.minecraftforge.common.Tags.Blocks.STORAGE_BLOCKS,
         net.minecraftforge.common.Tags.Blocks.GLASS,
         net.minecraftforge.common.Tags.Blocks.STONE,
         net.minecraftforge.common.Tags.Blocks.COBBLESTONE,
         BlockTags.STONE_BRICKS,
         CFOAM_BLOCK,
         net.minecraftforge.common.Tags.Blocks.END_STONES,
         net.minecraftforge.common.Tags.Blocks.SANDSTONE
      );
      ObjectSet<Block> blocks = CollectionUtils.<Block>createLinkedSet();
      blocks.addAll(PainterHelper.CONCRETE.getBlocks());
      blocks.addAll(PainterHelper.GLAZED_TERRACOTTA.getBlocks());
      blocks.addAll(PainterHelper.TERRACOTTA.getBlocks());
      blocks.add(Blocks.TERRACOTTA);
      blocks.add(Blocks.BRICKS);
      blocks.add(Blocks.OBSIDIAN);
      blocks.addAll(
         CollectionUtils.<Block>asList(
            IC2Blocks.REINFORCED_BRICK,
            IC2Blocks.REINFORCED_CLEAR_GLASS,
            IC2Blocks.REINFORCED_CRACKED_STONE,
            IC2Blocks.REINFORCED_GLASS,
            IC2Blocks.REINFORCED_STONE
         )
      );
      registerSimpleTag("ic2", "tank_structure_blocks", (Block[])blocks.toArray(Block[]::new));
      registerSimpleTag("forge", "glass", IC2Blocks.REINFORCED_CLEAR_GLASS, IC2Blocks.REINFORCED_GLASS);
      registerBlockTag("ic2", "flowers", BlockTags.CROPS, BlockTags.FLOWERS);
      registerBlockTag("ic2", "ground", BlockTags.DIRT);
      registerSimpleTag("ic2", "ground", Blocks.FARMLAND, Blocks.TERRACOTTA);
      registerSimpleTag("ic2", "ground", PainterHelper.TERRACOTTA.getBlockArray());
      registerBlockTag(
         "ic2",
         "flat",
         BlockTags.FLOWERS,
         BlockTags.CROPS,
         BlockTags.DIRT,
         BlockTags.LOGS,
         BlockTags.SAPLINGS,
         BlockTags.SAND,
         net.minecraftforge.common.Tags.Blocks.GRAVEL
      );
      registerSimpleTag("ic2", "flat", Blocks.MELON, Blocks.PUMPKIN, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.MUSHROOM_STEM, Blocks.RED_MUSHROOM_BLOCK);
      registerItemTag("ic2", "coal_and_charcoal", DUST_COAL, DUST_CHARCOAL);
      registerSimpleTag("forge", "wrenches", IC2Items.WRENCH, IC2Items.ELECTRIC_WRENCH, IC2Items.PRECISION_WRENCH);
      registerTag("forge", "tools", "wrench", IC2Items.WRENCH, IC2Items.ELECTRIC_WRENCH, IC2Items.PRECISION_WRENCH);

      for (PaintingVariant variant : IC2Paintings.PAINTINGS) {
         registerTags(new ResourceLocation("minecraft", "tags/painting_variant/placeable"), ForgeRegistries.PAINTING_VARIANTS, variant);
      }
   }

   public static TagKey<Item> createItemTag(String name) {
      return createItemTag("minecraft", name);
   }

   public static TagKey<Item> createForgeItemTag(String name) {
      return createItemTag("forge", name);
   }

   public static TagKey<Item> createItemTag(String owner, String name) {
      return ItemTags.create(new ResourceLocation(owner, name));
   }

   public static TagKey<Block> createBlockTag(String name) {
      return createBlockTag("minecraft", name);
   }

   public static TagKey<Block> createForgeBlockTag(String name) {
      return createBlockTag("forge", name);
   }

   public static TagKey<Block> createBlockTag(String owner, String name) {
      return BlockTags.create(new ResourceLocation(owner, name));
   }

   public static TagKey<Fluid> createFluidTag(String name) {
      return createFluidTag("minecraft", name);
   }

   public static TagKey<Fluid> createForgeFluidTag(String name) {
      return createFluidTag("forge", name);
   }

   public static TagKey<Fluid> createFluidTag(String owner, String name) {
      return FluidTags.create(new ResourceLocation(owner, name));
   }

   public static void registerTag(String owner, String category, String name, Item... items) {
      registerTags(new ResourceLocation(owner, "tags/items/" + category + "/" + name), ForgeRegistries.ITEMS, items);
      registerTags(new ResourceLocation(owner, "tags/items/" + category), ForgeRegistries.ITEMS, items);
   }

   public static void registerTag(String owner, String category, String name, Block... blocks) {
      registerTags(new ResourceLocation(owner, "tags/blocks/" + category + "/" + name), ForgeRegistries.BLOCKS, blocks);
      registerTags(new ResourceLocation(owner, "tags/blocks/" + category), ForgeRegistries.BLOCKS, blocks);
      registerTags(new ResourceLocation(owner, "tags/items/" + category + "/" + name), ForgeRegistries.BLOCKS, blocks);
      registerTags(new ResourceLocation(owner, "tags/items/" + category), ForgeRegistries.BLOCKS, blocks);
   }

   public static void registerTag(String owner, String category, String name, Fluid... fluids) {
      registerTags(new ResourceLocation(owner, "tags/fluids/" + category + "/" + name), ForgeRegistries.FLUIDS, fluids);
      registerTags(new ResourceLocation(owner, "tags/fluids/" + category), ForgeRegistries.FLUIDS, fluids);
   }

   public static void registerSimpleTag(String owner, String name, Item... items) {
      registerTags(new ResourceLocation(owner, "tags/items/" + name), ForgeRegistries.ITEMS, items);
   }

   public static void registerSimpleTag(String owner, String name, Block... blocks) {
      registerTags(new ResourceLocation(owner, "tags/blocks/" + name), ForgeRegistries.BLOCKS, blocks);
      registerTags(new ResourceLocation(owner, "tags/items/" + name), ForgeRegistries.BLOCKS, blocks);
   }

   public static void registerSimpleTag(String owner, String name, Fluid... fluids) {
      registerTags(new ResourceLocation(owner, "tags/fluids/" + name), ForgeRegistries.FLUIDS, fluids);
   }

   public static void registerMinecraftTag(String name, Item... items) {
      registerTags(new ResourceLocation("tags/items/" + name), ForgeRegistries.ITEMS, items);
   }

   public static void registerMinecraftTag(String name, Block... blocks) {
      registerTags(new ResourceLocation("tags/blocks/" + name), ForgeRegistries.BLOCKS, blocks);
      registerTags(new ResourceLocation("tags/items/" + name), ForgeRegistries.BLOCKS, blocks);
   }

   public static void registerMinecraftTag(String name, Fluid... fluids) {
      registerTags(new ResourceLocation("tags/fluids/" + name), ForgeRegistries.FLUIDS, fluids);
   }

   public static void registerItemTag(String key, String name, TagKey<?>... tags) {
      registerTag(key, "tags/items/" + name, tags);
   }

   public static void registerBlockTag(String key, String name, TagKey<?>... tags) {
      registerTag(key, "tags/blocks/" + name, tags);
   }

   public static void registerTag(String key, String value, TagKey<?>... tags) {
      registerTag(new ResourceLocation(key, value), tags);
   }

   public static void registerTag(ResourceLocation name, TagKey<?>... tags) {
      Set<String> set = TAGS_TO_LOAD.getValue(name);
      if (set == null) {
         set = CollectionUtils.<String>createSet();
         TAGS_TO_LOAD.register(name, set);
      }

      for (TagKey<?> tag : tags) {
         set.add("#" + tag.location());
      }
   }

   @SafeVarargs
   public static <T> void registerTags(ResourceLocation key, IForgeRegistry<T> registry, T... entries) {
      Set<String> set = TAGS_TO_LOAD.getValue(key);
      if (set == null) {
         set = CollectionUtils.<String>createSet();
         TAGS_TO_LOAD.register(key, set);
      }

      for (T entry : entries) {
         set.add(registry.getKey(entry).toString());
      }
   }
}
