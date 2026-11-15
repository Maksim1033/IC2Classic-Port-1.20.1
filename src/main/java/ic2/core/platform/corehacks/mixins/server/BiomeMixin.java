package ic2.core.platform.corehacks.mixins.server;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({Biome.class})
public interface BiomeMixin {
   @Invoker("getTemperature")
   float getTemp(BlockPos var1);
}
