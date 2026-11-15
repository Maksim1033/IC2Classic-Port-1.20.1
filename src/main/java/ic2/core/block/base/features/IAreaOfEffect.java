package ic2.core.block.base.features;

import net.minecraft.world.phys.AABB;

public interface IAreaOfEffect {
   AABB getAreaOfEffect();

   int getAreaOfEffectColor();

   void setVisualizationId(int var1);

   int getVisualizationId();
}
