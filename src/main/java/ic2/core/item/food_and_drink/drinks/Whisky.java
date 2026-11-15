package ic2.core.item.food_and_drink.drinks;

import ic2.api.items.IDrinkableFluid;
import ic2.core.platform.registries.IC2Potions;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Whisky extends IDrinkableFluid {
   public static final String[] TEXTURES = new String[]{"black_stuff", "whisky_10y", "whisky_12y", "whisky_15y", "whisky_25y", "whisky_50y"};
   public static final String[] YEARS = new String[]{"0", "10", "12", "15", "25", "50"};
   public static final MobEffect[] EFFECTS = new MobEffect[]{MobEffects.ABSORPTION, MobEffects.DAMAGE_RESISTANCE, MobEffects.CONFUSION};
   public static final int[] DURATIONS = new int[]{0, 6000, 7200, 9000, 15000, 30000};
   public static final int[] LEVELS = new int[]{0, 1, 1, 2, 3, 4};
   public static final int[] YEAR_NUMBERS = new int[]{0, 10, 12, 15, 25, 50};

   public Whisky() {
      super(new ResourceLocation("ic2", "whisky"));
   }

   @Override
   public boolean drink(ItemStack stack, Level world, Player player) {
      int year = getYear(stack);
      if (year == 0) {
         int rand = world.random.nextInt(6);
         if (rand == 5) {
            MobEffects.HARM.applyInstantenousEffect(player, player, player, world.random.nextInt(4), 0.25);
         } else if (rand != 0) {
            player.addEffect(new MobEffectInstance(Rum.EFFECTS[rand - 1]));
         }

         return true;
      } else {
         player.getFoodData().eat(4, (float)(year / 2));
         amplifyEffect(player, IC2Potions.SHAKY, DURATIONS[year] * 5, LEVELS[year], DURATIONS[year]);
         amplifyEffect(player, EFFECTS[world.random.nextInt(256) % EFFECTS.length], DURATIONS[year] * 5, LEVELS[year], DURATIONS[year]);
         return true;
      }
   }

   public static void amplifyEffect(Player player, MobEffect potion, int max, int level, int duration) {
      MobEffectInstance eff = player.getEffect(potion);
      if (eff == null) {
         player.addEffect(new MobEffectInstance(potion, duration));
      } else {
         int newDuration = eff.getDuration();
         player.addEffect(new MobEffectInstance(potion, Math.min(newDuration + duration, max), level));
      }
   }

   @Override
   public boolean hasSpecialName() {
      return true;
   }

   @Override
   public Component getSpecialName(ItemStack stack) {
      return Component.translatable("item.ic2.glass_whisky", new Object[]{YEARS[getYear(stack)]});
   }

   @Override
   public int getTextureIndex(ItemStack stack) {
      return getYear(stack);
   }

   @Override
   public List<ItemStack> generateSubStates(ItemStack base, boolean textures) {
      List<ItemStack> list = CollectionUtils.<ItemStack>createList();

      for (int i = 0; i < 6; i++) {
         list.add(createYear(base.copy(), i));
      }

      return list;
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack, String baseFolder) {
      return new ResourceLocation(baseFolder + "/" + TEXTURES[getYear(stack)]);
   }

   public static int getYear(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("age");
   }

   public static ItemStack createYear(ItemStack stack, int year) {
      if (!stack.isEmpty()) {
         stack.getOrCreateTag().putInt("age", year);
      }

      return stack;
   }
}
