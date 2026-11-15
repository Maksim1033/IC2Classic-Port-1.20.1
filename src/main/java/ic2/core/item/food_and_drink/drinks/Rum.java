package ic2.core.item.food_and_drink.drinks;

import ic2.api.items.IDrinkableFluid;
import ic2.core.platform.registries.IC2Potions;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Rum extends IDrinkableFluid {
   public static final MobEffectInstance[] EFFECTS = new MobEffectInstance[]{
      new MobEffectInstance(MobEffects.CONFUSION, 1200, 0),
      new MobEffectInstance(MobEffects.BLINDNESS, 2400, 0),
      new MobEffectInstance(MobEffects.POISON, 2400, 0),
      new MobEffectInstance(MobEffects.POISON, 200, 2)
   };

   public Rum(ResourceLocation id) {
      super(id);
   }

   @Override
   public boolean drink(ItemStack stack, Level world, Player player) {
      int progress = StackUtil.getNbtData(stack).getInt("progress");
      if (progress < 100) {
         int rand = world.random.nextInt(6);
         if (rand == 5) {
            MobEffects.HARM.applyInstantenousEffect(player, player, player, world.random.nextInt(4), 0.25);
         } else if (rand != 0) {
            player.addEffect(new MobEffectInstance(EFFECTS[rand - 1]));
         }

         return true;
      } else {
         amplifyEffect(player, MobEffects.FIRE_RESISTANCE, 0, 2.0F, 2400);
         MobEffectInstance effect = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
         int lvl = effect != null ? effect.getAmplifier() : -1;
         amplifyEffect(player, MobEffects.DAMAGE_RESISTANCE, 2, 2.0F, 2400);
         if (lvl >= 0) {
            amplifyEffect(player, MobEffects.BLINDNESS, 0, 1.0F, 2400);
            amplifyEffect(player, IC2Potions.SHAKY, 0, 2.0F, 2400);
         }

         if (lvl >= 1) {
            amplifyEffect(player, MobEffects.CONFUSION, 0, 0.5F, 2400);
         }

         return true;
      }
   }

   @Override
   public List<ItemStack> generateSubStates(ItemStack base, boolean textures) {
      List<ItemStack> list = CollectionUtils.<ItemStack>createList();
      ItemStack rum = base.copy();
      rum.getOrCreateTag().putByte("progress", (byte)100);
      list.add(rum);
      rum = base.copy();
      rum.getOrCreateTag().putByte("progress", (byte)0);
      list.add(rum);
      return list;
   }

   @Override
   public int getTextureIndex(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("progress") >= 100 ? 0 : 1;
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack, String baseFolder) {
      return new ResourceLocation(baseFolder + (StackUtil.getNbtData(stack).getInt("progress") < 100 ? "/black_stuff" : "/rum"));
   }

   public static ItemStack createData(ItemStack base, int progress) {
      if (!base.isEmpty()) {
         base.getOrCreateTag().putByte("progress", (byte)Mth.clamp(progress, 0, 100));
      }

      return base;
   }

   public static void amplifyEffect(Player player, MobEffect potion, int max, float intensity, int duration) {
      MobEffectInstance eff = player.getEffect(potion);
      if (eff == null) {
         player.addEffect(new MobEffectInstance(potion, duration, 0));
      } else {
         int newDuration = eff.getDuration();
         player.addEffect(
            new MobEffectInstance(
               potion,
               newDuration + (int)Math.min(Math.max(0.0F, ((float)duration * (1.0F + intensity * 2.0F) - (float)newDuration) / 2.0F), (float)duration),
               Math.min(max, eff.getAmplifier() + 1)
            )
         );
      }
   }
}
