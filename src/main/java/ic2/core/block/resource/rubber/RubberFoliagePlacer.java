package ic2.core.block.resource.rubber;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ic2.core.platform.events.WorldGenerator;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;

public class RubberFoliagePlacer extends FoliagePlacer {
   public static final Codec<RubberFoliagePlacer> ENCODER = RecordCodecBuilder.create(
      p_236737_0_ -> foliagePlacerParts(p_236737_0_).apply(p_236737_0_, RubberFoliagePlacer::new)
   );

   public RubberFoliagePlacer() {
      super(ConstantInt.of(0), ConstantInt.of(0));
   }

   public RubberFoliagePlacer(IntProvider radius, IntProvider offset) {
      super(radius, offset);
   }

   protected FoliagePlacerType<?> type() {
      return WorldGenerator.RUBBER_FOLIAGE;
   }

   @Override
   protected void createFoliage(
      LevelSimulatedReader world,
      FoliagePlacer.FoliageSetter placer,
      RandomSource rand,
      TreeConfiguration config,
      int p_225617_,
      FoliageAttachment position,
      int p_225619_,
      int p_225620_,
      int p_225621_
   ) {
      BlockPos pos = position.pos();
      if (TreeFeature.validTreePos(world, pos)) {
         placer.set(pos, config.foliageProvider.getState(rand, pos));
      }
   }

   public int foliageHeight(RandomSource random, int p_230374_2_, TreeConfiguration p_230374_3_) {
      int h = 8;
      int height = h / 2;
      h -= height;
      return height + random.nextInt(h + 1);
   }

   protected boolean shouldSkipLocation(RandomSource p_225595_, int p_225596_, int p_225597_, int p_225598_, int p_225599_, boolean p_225600_) {
      return false;
   }
}
