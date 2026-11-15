package ic2.core.platform.events;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import ic2.core.IC2;
import ic2.core.block.resource.rubber.RubberFoliagePlacer;
import ic2.core.block.resource.rubber.RubberTrunkPlacer;
import ic2.core.platform.corehacks.mixins.server.TrunkMixin;
import ic2.core.platform.registries.IC2Blocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.TargetBlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration.TreeConfigurationBuilder;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraftforge.common.Tags.Biomes;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.BiomeModifier.Phase;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;
import net.minecraftforge.registries.ForgeRegistries;

public class WorldGenerator {
   static ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> TIN_FEATURE;
   static ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> URANIUM_FEATURE;
   static ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> URANIUM_LARGE_FEATURE;
   static ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> SILVER_FEATURE;
   static ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> NETHER_ALUMINUM_FEATURE;
   static ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> NETHER_SILVER_FEATURE;
   static Holder<PlacedFeature> ORE_TIN;
   static Holder<PlacedFeature> ORE_URANIUM;
   static Holder<PlacedFeature> ORE_URANIUM_DEEP;
   static Holder<PlacedFeature> ORE_SILVER;
   static Holder<PlacedFeature> NETHER_ORE_ALUMINUM;
   static Holder<PlacedFeature> NETHER_ORE_SILVER;
   static Holder<PlacedFeature> RUBBER_TREE;
   static ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>> RUBBER_TREE_FEATURE;
   public static ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>> RUBBER_TREE_SAPLING;
   public static final TrunkPlacerType<RubberTrunkPlacer> RUBBER_TRUNK = new TrunkPlacerType<>(RubberTrunkPlacer.SERIALIZER);
   public static final FoliagePlacerType<RubberFoliagePlacer> RUBBER_FOLIAGE = new FoliagePlacerType<>(RubberFoliagePlacer.ENCODER);

   public static void generateOres() {
      TIN_FEATURE = createOreFeature(
         "ore_tin", 6, 0.0F, target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), IC2Blocks.TIN_ORE), target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), IC2Blocks.DEEPSLATE_TIN_ORE)
      );
      URANIUM_FEATURE = createOreFeature(
         "ore_uranium", 3, 0.0F, target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), IC2Blocks.URANIUM_ORE), target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), IC2Blocks.DEEPSLATE_URANIUM_ORE)
      );
      URANIUM_LARGE_FEATURE = createOreFeature(
         "ore_uranium_large", 15, 0.0F, target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), IC2Blocks.URANIUM_ORE), target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), IC2Blocks.DEEPSLATE_URANIUM_ORE)
      );
      SILVER_FEATURE = createOreFeature(
         "ore_silver", 5, 0.0F, target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), IC2Blocks.SILVER_ORE), target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), IC2Blocks.DEEPSLATE_SILVER_ORE)
      );
      NETHER_ALUMINUM_FEATURE = createOreFeature("nether_ore_aluminum", 3, 0.5F, target(new BlockMatchTest(Blocks.NETHERRACK), IC2Blocks.ALUMINUM_ORE_NETHER));
      NETHER_SILVER_FEATURE = createOreFeature("nether_ore_silver", 3, 0.1F, target(new BlockMatchTest(Blocks.NETHERRACK), IC2Blocks.SILVER_ORE_NETHER));
      RUBBER_TREE_FEATURE = register(new ResourceLocation("ic2", "rubber_tree"), generateTree(false));
      RUBBER_TREE_SAPLING = generateTree(true);
      ORE_TIN = feature(
         "ore_tin",
         TIN_FEATURE,
         List.of(
            CountPlacement.of(25),
            InSquarePlacement.spread(),
            HeightRangePlacement.triangle(VerticalAnchor.absolute(-6), VerticalAnchor.absolute(122)),
            BiomeFilter.biome()
         )
      );
      ORE_URANIUM = feature(
         "ore_uranium",
         URANIUM_FEATURE,
         List.of(
            CountPlacement.of(20),
            InSquarePlacement.spread(),
            HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(16)),
            BiomeFilter.biome()
         )
      );
      ORE_URANIUM_DEEP = feature(
         "ore_uranium_deep",
         URANIUM_LARGE_FEATURE,
         List.of(
            RarityFilter.onAverageOnceEvery(128),
            CountPlacement.of(20),
            InSquarePlacement.spread(),
            HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-48)),
            BiomeFilter.biome()
         )
      );
      ORE_SILVER = feature(
         "ore_silver",
         SILVER_FEATURE,
         List.of(
            CountPlacement.of(10),
            InSquarePlacement.spread(),
            HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(22)),
            BiomeFilter.biome()
         )
      );
      NETHER_ORE_ALUMINUM = feature(
         "ore_aluminium_nether",
         NETHER_ALUMINUM_FEATURE,
         List.of(
            CountPlacement.of(10),
            InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(128)),
            BiomeFilter.biome()
         )
      );
      NETHER_ORE_SILVER = feature(
         "ore_silver_nether",
         NETHER_SILVER_FEATURE,
         List.of(
            CountPlacement.of(10),
            InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(128)),
            BiomeFilter.biome()
         )
      );
      RUBBER_TREE = Holder.direct(new PlacedFeature(Holder.direct(RUBBER_TREE_FEATURE), VegetationPlacements.treePlacement(CountPlacement.of(10), IC2Blocks.RUBBER_SAPLING)));
   }

   public static void registerBiome(Holder<Biome> biome, BiomeGenerationSettingsBuilder builder) {
      if (IC2.CONFIG.oreTin.get()) {
         builder.addFeature(Decoration.UNDERGROUND_ORES, ORE_TIN);
      }

      if (IC2.CONFIG.oreUranium.get()) {
         builder.addFeature(Decoration.UNDERGROUND_ORES, ORE_URANIUM);
      }

      if (IC2.CONFIG.oreSilver.get()) {
         builder.addFeature(Decoration.UNDERGROUND_ORES, ORE_SILVER);
      }

      if (IC2.CONFIG.oreUranium.get()) {
         builder.addFeature(Decoration.UNDERGROUND_ORES, ORE_URANIUM_DEEP);
      }

      if (biome.is(BiomeTags.IS_NETHER)) {
         if (IC2.CONFIG.oreNetherAluminium.get()) {
            builder.addFeature(Decoration.UNDERGROUND_ORES, NETHER_ORE_ALUMINUM);
         }

         if (IC2.CONFIG.oreNetherSilver.get()) {
            builder.addFeature(Decoration.UNDERGROUND_ORES, NETHER_ORE_SILVER);
         }
      }

      if ((biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_FOREST) || biome.is(Biomes.IS_SWAMP)) && IC2.CONFIG.oreRubberTree.get()) {
         builder.addFeature(Decoration.VEGETAL_DECORATION, RUBBER_TREE);
      }
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> feature(
      String id, ConfiguredFeature<FC, F> feature, List<PlacementModifier> modifiers
   ) {
      return feature(new ResourceLocation("ic2", id), feature, modifiers);
   }

   public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> feature(
      ResourceLocation id, ConfiguredFeature<FC, F> feature, List<PlacementModifier> modifiers
   ) {
      return Holder.direct(new PlacedFeature(Holder.direct(feature), modifiers));
   }

   public static ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> createOreFeature(
      String id, int size, float invisibility, TargetBlockState... targets
   ) {
      return createOreFeature(new ResourceLocation("ic2", id), size, invisibility, targets);
   }

   public static ConfiguredFeature<OreConfiguration, Feature<OreConfiguration>> createOreFeature(
      ResourceLocation id, int size, float invisibility, TargetBlockState... targets
   ) {
      return new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(ImmutableList.copyOf(targets), size, invisibility));
   }

   public static TargetBlockState target(RuleTest test, Block block) {
      return OreConfiguration.target(test, block.defaultBlockState());
   }

   public static ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>> generateTree(boolean force) {
      TreeConfigurationBuilder builder = new TreeConfigurationBuilder(
            BlockStateProvider.simple(IC2Blocks.RUBBERWOOD_LOG.defaultBlockState()),
            new RubberTrunkPlacer(5, 2, 0),
            BlockStateProvider.simple(IC2Blocks.RUBBER_LEAVES.defaultBlockState()),
            new RubberFoliagePlacer(),
            new TwoLayersFeatureSize(1, 0, 1)
         )
         .ignoreVines();
      return new ConfiguredFeature<>(Feature.TREE, (force ? builder.forceDirt() : builder).build());
   }

   public static ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>> register(
      ResourceLocation id, ConfiguredFeature<TreeConfiguration, Feature<TreeConfiguration>> config
   ) {
      return config;
   }

   public static class IC2Modifier implements BiomeModifier {
      public static final Codec<WorldGenerator.IC2Modifier> CODEC = Codec.unit(WorldGenerator.IC2Modifier::new);

      public void modify(Holder<Biome> biome, Phase phase, Builder builder) {
         if (phase == Phase.ADD) {
            WorldGenerator.registerBiome(biome, builder.getGenerationSettings());
         }
      }

      public Codec<? extends BiomeModifier> codec() {
         return CODEC;
      }
   }
}
