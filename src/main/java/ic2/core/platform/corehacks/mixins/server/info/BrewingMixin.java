package ic2.core.platform.corehacks.mixins.server.info;

import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({BrewingStandBlockEntity.class})
public interface BrewingMixin {
   @Accessor("fuel")
   int getFuel();

   @Accessor("brewTime")
   int getProgress();
}
