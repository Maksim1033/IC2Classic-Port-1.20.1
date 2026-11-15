package ic2.api.reactor;

import ic2.api.util.ILocation;
import net.minecraft.world.item.ItemStack;

public interface IReactor extends ILocation {
   int getHeat();

   void setHeat(int var1);

   void addHeat(int var1);

   int getMaxHeat();

   void setMaxHeat(int var1);

   float getHeatEffectModifier();

   void setHeatEffectModifier(float var1);

   double getEnergyOutput();

   void addOutput(float var1);

   ItemStack getStackInReactor(int var1, int var2);

   void setStackInReactor(int var1, int var2, ItemStack var3);

   void explode();

   int getTickRate();

   boolean isProducingEnergy();
}
