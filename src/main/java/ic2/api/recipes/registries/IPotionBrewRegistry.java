package ic2.api.recipes.registries;

import java.util.List;
import java.util.Map;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public interface IPotionBrewRegistry extends IListenableRegistry<IPotionBrewRegistry> {
   default void registerBrew(Item item, MobEffect potion) {
      this.registerBrew(item, null, potion);
   }

   void registerBrew(Item var1, MobEffect var2, MobEffect var3);

   void removeBrew(Item var1);

   MobEffect getEffect(Item var1, MobEffect var2);

   Map<Item, Map<MobEffect, MobEffect>> getEffects();

   List<Item> getRequiredItems(MobEffect var1);

   void registerName(MobEffect var1, String var2);

   String getName(MobEffect var1);

   void registerPotionContainer(Item var1, IPotionBrewRegistry.PotionContainer var2);

   IPotionBrewRegistry.PotionContainer getPotionContainer(Item var1);

   Map<Item, IPotionBrewRegistry.PotionContainer> getContainers();

   public static final class PotionContainer {
      final Item output;
      final int fluidUsage;
      final float durationEffectiveness;

      public PotionContainer(Item output, int fluidUsage, float durationEffectiveness) {
         if (output != Items.AIR && output != null && fluidUsage > 0 && !(durationEffectiveness <= 0.0F)) {
            this.output = output;
            this.fluidUsage = fluidUsage;
            this.durationEffectiveness = durationEffectiveness;
         } else {
            throw new IllegalStateException("Potion Brew Result is invalid");
         }
      }

      public float getDurationEffectiveness() {
         return this.durationEffectiveness;
      }

      public int getFluidUsage() {
         return this.fluidUsage;
      }

      public Item getOutput() {
         return this.output;
      }
   }
}
