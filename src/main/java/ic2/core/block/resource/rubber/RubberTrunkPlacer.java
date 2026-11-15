package ic2.core.block.resource.rubber;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ic2.api.util.DirectionList;
import ic2.core.block.resource.RubberwoodLogBlock;
import ic2.core.platform.events.WorldGenerator;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.common.Tags.Biomes;

public class RubberTrunkPlacer extends TrunkPlacer {
   public static final Codec<RubberTrunkPlacer> SERIALIZER = RecordCodecBuilder.create(
      p_236891_0_ -> trunkPlacerParts(p_236891_0_).apply(p_236891_0_, RubberTrunkPlacer::new)
   );

   public RubberTrunkPlacer(int p_i232060_1_, int p_i232060_2_, int p_i232060_3_) {
      super(p_i232060_1_, p_i232060_2_, p_i232060_3_);
   }

   protected TrunkPlacerType<?> type() {
      return WorldGenerator.RUBBER_TRUNK;
   }

   public List<FoliageAttachment> placeTrunk(
      LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> placer, RandomSource random, int maxHeight, BlockPos pos, TreeConfiguration config
   ) {
      int max = 0;
      if (!(world instanceof CommonLevelAccessor)) {
         if (!config.forceDirt) {
            return ObjectLists.emptyList();
         }
      } else {
         Holder<Biome> cat = ((CommonLevelAccessor)world).getBiome(pos);
         if (cat.is(BiomeTags.IS_FOREST) || cat.is(BiomeTags.IS_JUNGLE)) {
            max += 6;
         }

         if (cat.is(Biomes.IS_SWAMP)) {
            max += 15;
         }

         if (!config.forceDirt && (max <= 0 || random.nextInt(100) <= 100 - max * 2)) {
            return ObjectLists.emptyList();
         }
      }

      setDirtAt(world, placer, random, pos.below(), config);
      int hole_Chance = 25;
      List<FoliageAttachment> spawningPlaces = CollectionUtils.<FoliageAttachment>createList();

      for (int i = 0; i < maxHeight; i++) {
         BlockState state = IC2Blocks.RUBBERWOOD_LOG.defaultBlockState();
         if (random.nextInt(100) <= hole_Chance) {
            hole_Chance -= 10;
            state = (BlockState)((BlockState)((BlockState)state.setValue(RubberwoodLogBlock.RESIN, true)).setValue(RubberwoodLogBlock.COLLECTABLE, true))
               .setValue(RubberwoodLogBlock.RESIN_FACING, DirectionList.HORIZONTAL.getRandomFacing());
         }

         placeBlocks(world, random, pos.above(i), placer, state);
         if (maxHeight < 4 || maxHeight < 7 && i > 1 || i > 2) {
            int x = pos.getX();
            int z = pos.getZ();

            for (int a = x - 2; a <= x + 2; a++) {
               for (int b = z - 2; b <= z + 2; b++) {
                  int c = i + 4 - maxHeight;
                  if (c < 1) {
                     c = 1;
                  }

                  boolean gen = a > x - 2 && a < x + 2 && b > z - 2 && b < z + 2
                     || a > x - 2 && a < x + 2 && random.nextInt(c) == 0
                     || b > z - 2 && b < z + 2 && random.nextInt(c) == 0;
                  BlockPos leavePos = new BlockPos(a, pos.getY() + i, b);
                  if (gen && TreeFeature.isAirOrLeaves(world, leavePos)) {
                     spawningPlaces.add(new FoliageAttachment(leavePos, 0, true));
                  }
               }
            }
         }
      }

      for (int i = 0; i <= 2; i++) {
         BlockPos leavePos = new BlockPos(pos.getX(), pos.getY() + maxHeight + i, pos.getZ());
         if (TreeFeature.isAirOrLeaves(world, leavePos)) {
            spawningPlaces.add(new FoliageAttachment(leavePos, 0, true));
         }
      }

      return ImmutableList.copyOf(spawningPlaces);
   }

   protected static boolean placeBlocks(LevelSimulatedReader world, RandomSource rand, BlockPos pos, BiConsumer<BlockPos, BlockState> placer, BlockState state) {
      if (TreeFeature.validTreePos(world, pos)) {
         placer.accept(pos, state);
         return true;
      } else {
         return false;
      }
   }
}
