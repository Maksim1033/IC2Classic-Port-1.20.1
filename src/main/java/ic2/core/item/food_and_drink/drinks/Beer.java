package ic2.core.item.food_and_drink.drinks;

import ic2.api.items.IDrinkableFluid;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Beer extends IDrinkableFluid {
   public static final MobEffect[] EFFECTS = new MobEffect[]{
      MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DAMAGE_RESISTANCE, MobEffects.CONFUSION
   };
   public static final int[] DURATIONS = new int[]{600, 900, 1200, 1600, 2000, 2400, 2800};
   public static final float[] INTENSITY = new float[]{0.4F, 0.75F, 1.0F, 1.5F, 2.0F};
   public static final String[] TEXTURES = new String[]{"brew", "youngster", "beer", "ale", "dragonblood", "black_stuff"};
   public static final String[] SOLIDS = new String[]{
      "subitem.ic2.beer_solid_watery",
      "subitem.ic2.beer_solid_clear",
      "subitem.ic2.beer_solid_lite",
      "",
      "subitem.ic2.beer_solid_strong",
      "subitem.ic2.beer_solid_thick",
      "subitem.ic2.beer_solid_stodge"
   };
   public static final String[] HOPS = new String[]{
      "subitem.ic2.beer_hops_soup",
      "subitem.ic2.beer_hops_alcfree",
      "subitem.ic2.beer_hops_white",
      "",
      "subitem.ic2.beer_hops_dark",
      "subitem.ic2.beer_hops_full",
      "subitem.ic2.beer_hops_black"
   };
   public static final String[] TIME = new String[]{
      "subitem.ic2.beer_time_brew",
      "subitem.ic2.beer_time_youngster",
      "subitem.ic2.beer_time_beer",
      "subitem.ic2.beer_time_ale",
      "subitem.ic2.beer_time_dragonblood",
      "subitem.ic2.beer_time_black_stuff"
   };

   public Beer(ResourceLocation id) {
      super(id);
   }

   @Override
   public boolean drink(ItemStack stack, Level world, Player player) {
      int data = StackUtil.getNbtData(stack).getInt("data");
      int time = getTime(data);
      if (time >= 5) {
         int rand = world.random.nextInt(6);
         if (rand == 5) {
            MobEffects.HARM.applyInstantenousEffect(player, player, player, world.random.nextInt(4), 0.25);
         } else if (rand != 0) {
            player.addEffect(new MobEffectInstance(Rum.EFFECTS[rand - 1]));
         }

         return true;
      } else {
         int hops = getHops(data);
         int solid = getSolid(data);
         int duration = DURATIONS[solid];
         float intensity = INTENSITY[time];
         player.getFoodData().eat(6 - hops, (float)solid * 0.15F);
         int max = (int)(intensity * (float)hops * 0.5F);
         MobEffectInstance effect = player.getEffect(MobEffects.DIG_SLOWDOWN);
         int lvl = effect != null ? effect.getAmplifier() : -1;
         Rum.amplifyEffect(player, MobEffects.DIG_SLOWDOWN, max, intensity, duration);

         for (int i = 0; lvl > -1; i++) {
            if (i > 3) {
               MobEffects.HARM.applyInstantenousEffect(player, player, player, world.random.nextInt(3), 0.25);
            } else {
               Rum.amplifyEffect(player, EFFECTS[i], this.getMax(i, max), intensity, duration);
            }

            lvl--;
         }

         return true;
      }
   }

   @Override
   public boolean hasSpecialName() {
      return true;
   }

   @Override
   public Component getSpecialName(ItemStack stack) {
      int data = StackUtil.getNbtData(stack).getInt("data");
      int time = getTime(data);
      int hops = getHops(data);
      int solid = getSolid(data);
      MutableComponent comp = Component.empty();
      String s = SOLIDS[Mth.clamp(solid, 0, SOLIDS.length - 1)];
      if (!s.isEmpty()) {
         comp.append(Component.translatable(s)).append(Component.literal(" "));
      }

      s = HOPS[Mth.clamp(hops, 0, HOPS.length - 1)];
      if (!s.isEmpty()) {
         comp.append(Component.translatable(s)).append(Component.literal(" "));
      }

      s = TIME[Mth.clamp(time, 0, TIME.length - 1)];
      if (!s.isEmpty()) {
         comp.append(Component.translatable(s));
      }

      return comp;
   }

   @Override
   public int getTextureIndex(ItemStack stack) {
      return getTime(StackUtil.getNbtData(stack).getInt("data"));
   }

   @Override
   public List<ItemStack> generateSubStates(ItemStack base, boolean textures) {
      if (textures) {
         List<ItemStack> list = CollectionUtils.<ItemStack>createList();

         for (int i = 0; i < 6; i++) {
            list.add(createData(base.copy(), i, 0, 0));
         }

         return list;
      } else {
         List<ItemStack> list = CollectionUtils.<ItemStack>createList();

         for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 7; y++) {
               for (int z = 0; z < 7; z++) {
                  list.add(createData(base.copy(), x, y, z));
               }
            }
         }

         return list;
      }
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack, String baseFolder) {
      return new ResourceLocation(baseFolder + "/" + TEXTURES[getTime(StackUtil.getNbtData(stack).getInt("data")) % TEXTURES.length]);
   }

   public static int getTime(int data) {
      return data >> 6 & 7;
   }

   public static int getSolid(int data) {
      return data >> 3 & 7;
   }

   public static int getHops(int data) {
      return data & 7;
   }

   public int getMax(int lvl, int max) {
      switch (lvl) {
         case 1:
            return max / 2;
         case 2:
            return max - 1;
         case 3:
            return 0;
         default:
            return max;
      }
   }

   public static int createData(int time, int solid, int hops) {
      return (time & 7) << 6 | (solid & 7) << 3 | hops & 7;
   }

   public static ItemStack createData(ItemStack stack, int time, int solid, int hops) {
      if (!stack.isEmpty()) {
         stack.getOrCreateTag().putInt("data", (time & 7) << 6 | (solid & 7) << 3 | hops & 7);
      }

      return stack;
   }
}
