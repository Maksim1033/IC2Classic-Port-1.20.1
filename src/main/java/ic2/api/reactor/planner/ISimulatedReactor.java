package ic2.api.reactor.planner;

public interface ISimulatedReactor {
   SimulatedStack getItem(int var1, int var2);

   void markBroken(int var1, int var2);

   void addBreedingPulse();

   void addFuelPulse();

   int getHeat();

   void setHeat(int var1);

   void addHeat(int var1);

   int getMaxHeat();

   void setMaxHeat(int var1);

   float getHeatEffectModifier();

   void setHeatEffectModifier(float var1);

   void addOutput(float var1);

   float getEnergyOutput();

   void addSteam(int var1);

   int consumeWater(int var1);

   boolean isProducingEnergy();

   boolean isSteamReactor();

   boolean isSimulatingPulses();

   long getGameTime();
}
