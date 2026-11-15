package ic2.core.platform.corehacks.mixins.server.info;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({BaseSpawner.class})
public interface SpawnerMixin {
   @Invoker("isNearPlayer")
   boolean isActive(Level var1, BlockPos var2);

   @Accessor("spawnDelay")
   int getDelay();

   @Accessor("maxSpawnDelay")
   int getMaxDelay();
}
