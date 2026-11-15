package ic2.core.item.reactor.base;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public interface IUraniumRod {
   int getRodDurability();

   float getPulseEU();

   int getUraniumPulses();

   int getPulsesForConnection();

   List<int[]> getPulseArea();

   List<int[]> getHeatArea();

   float getPulseHeatModifier();

   float getExplosionModifier();

   boolean isEnrichedUranium();

   int getFusionHeat();

   ItemStack getBaseIngot();

   String getName();

   int getColor();

   ItemStack createNearDepletedRod(int var1);

   ItemStack createNearDepletedRod();

   ItemStack createReEnrichedRod();

   ItemStack createIsotopicRod();

   ItemStack createSingleRod();

   ItemStack createDualRod();

   ItemStack createQuadRod();
}
