package ic2.core.platform.registries;

import ic2.api.recipes.registries.IPotionBrewRegistry;
import ic2.core.entity.potion.RadiationEffect;
import ic2.core.entity.potion.SimpleEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.GameData;

public class IC2Potions {
   public static MobEffect RADIATION;
   public static MobEffect SHAKY;

   public static <T extends MobEffect> T registerEffect(String name, T effect) {
      ForgeRegistries.MOB_EFFECTS.register(GameData.checkPrefix(name, false), effect);
      return effect;
   }

   public static void init() {
      RADIATION = registerEffect("radiation", new RadiationEffect());
      SHAKY = registerEffect("shaky", new SimpleEffect(MobEffectCategory.NEUTRAL, 5578058));
   }

   public static void registerBrewing(IPotionBrewRegistry registry) {
      registry.registerPotionContainer(Items.GLASS_BOTTLE, new IPotionBrewRegistry.PotionContainer(Items.POTION, 250, 1.0F));
      registry.registerPotionContainer(IC2Items.EMPTY_SPLASH_POTION, new IPotionBrewRegistry.PotionContainer(Items.SPLASH_POTION, 250, 1.0F));
      registry.registerPotionContainer(IC2Items.EMPTY_LINGERING_POTION, new IPotionBrewRegistry.PotionContainer(Items.LINGERING_POTION, 250, 1.0F));
      registry.registerPotionContainer(Items.ARROW, new IPotionBrewRegistry.PotionContainer(Items.TIPPED_ARROW, 25, 0.125F));
      registry.registerName(MobEffects.NIGHT_VISION, "night_vision");
      registry.registerName(MobEffects.FIRE_RESISTANCE, "fire_resistance");
      registry.registerName(MobEffects.JUMP, "leaping");
      registry.registerName(MobEffects.WEAKNESS, "weakness");
      registry.registerName(MobEffects.MOVEMENT_SPEED, "swiftness");
      registry.registerName(MobEffects.WATER_BREATHING, "water_breathing");
      registry.registerName(MobEffects.HEAL, "healing");
      registry.registerName(MobEffects.POISON, "poison");
      registry.registerName(MobEffects.DAMAGE_BOOST, "strength");
      registry.registerName(MobEffects.SLOW_FALLING, "slow_falling");
      registry.registerName(MobEffects.INVISIBILITY, "invisibility");
      registry.registerName(MobEffects.MOVEMENT_SLOWDOWN, "slowness");
      registry.registerName(MobEffects.HARM, "harming");
      registry.registerBrew(Items.GOLDEN_CARROT, MobEffects.NIGHT_VISION);
      registry.registerBrew(Items.MAGMA_CREAM, MobEffects.FIRE_RESISTANCE);
      registry.registerBrew(Items.RABBIT_FOOT, MobEffects.JUMP);
      registry.registerBrew(Items.FERMENTED_SPIDER_EYE, MobEffects.WEAKNESS);
      registry.registerBrew(Items.SUGAR, MobEffects.MOVEMENT_SPEED);
      registry.registerBrew(Items.PUFFERFISH, MobEffects.WATER_BREATHING);
      registry.registerBrew(Items.GLISTERING_MELON_SLICE, MobEffects.HEAL);
      registry.registerBrew(Items.SPIDER_EYE, MobEffects.POISON);
      registry.registerBrew(Items.BLAZE_POWDER, MobEffects.DAMAGE_BOOST);
      registry.registerBrew(Items.PHANTOM_MEMBRANE, MobEffects.SLOW_FALLING);
      registry.registerBrew(Items.FERMENTED_SPIDER_EYE, MobEffects.NIGHT_VISION, MobEffects.INVISIBILITY);
      registry.registerBrew(Items.FERMENTED_SPIDER_EYE, MobEffects.JUMP, MobEffects.MOVEMENT_SLOWDOWN);
      registry.registerBrew(Items.FERMENTED_SPIDER_EYE, MobEffects.MOVEMENT_SPEED, MobEffects.MOVEMENT_SLOWDOWN);
      registry.registerBrew(Items.FERMENTED_SPIDER_EYE, MobEffects.HEAL, MobEffects.HARM);
      registry.registerBrew(Items.FERMENTED_SPIDER_EYE, MobEffects.POISON, MobEffects.HARM);
   }
}
