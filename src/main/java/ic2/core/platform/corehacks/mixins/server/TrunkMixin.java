package ic2.core.platform.corehacks.mixins.server;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({TrunkPlacerType.class})
public interface TrunkMixin {
   @Invoker("register")
   static <P extends TrunkPlacer> TrunkPlacerType<P> register(String name, Codec<P> codec) {
      throw new AssertionError();
   }
}
