package ic2.core.platform.corehacks.mixins.server.info;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({AbstractFurnaceBlockEntity.class})
public interface FurnaceMixin {
   @Accessor("litTime")
   int getCurrentFuel();

   @Accessor("litDuration")
   int getMaxFuel();

   @Accessor("cookingProgress")
   int getProgress();

   @Accessor("cookingTotalTime")
   int getMaxProgress();
}
