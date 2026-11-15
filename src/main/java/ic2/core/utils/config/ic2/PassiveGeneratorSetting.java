package ic2.core.utils.config.ic2;

import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.config.ConfigEntry.IntValue;

public class PassiveGeneratorSetting {
   public IntValue base;
   public IntValue passive;

   public PassiveGeneratorSetting(ConfigSection parent, String prefix, int baseValue, int passiveValue) {
      ConfigSection section = parent.addSubSection(prefix);
      this.base = (IntValue)section.addInt("base", baseValue, new String[]{"Base Production of the Generator"}).setServerSynced();
      this.passive = (IntValue)section.addInt(
            "passive", passiveValue, new String[]{"How much passive energy should be required per fuel. Lower => more Production"}
         )
         .setServerSynced();
   }

   public int getProduction() {
      return this.base.get();
   }

   public int getPassiveProduction() {
      return this.passive.get();
   }
}
