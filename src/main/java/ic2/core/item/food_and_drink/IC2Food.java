package ic2.core.item.food_and_drink;

import ic2.api.items.IAutoEatable;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.IC2SimpleItem;
import ic2.core.item.base.PropertiesBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class IC2Food extends IC2SimpleItem implements IAutoEatable {
   boolean autoEatable;

   public IC2Food(String itemName, PropertiesBuilder properties, String textureFolder, String textureName) {
      super(itemName, properties, textureFolder, textureName);
   }

   public static IC2Food simpleFood(String name, String textureName, FoodProperties food) {
      return new IC2Food(name, new PropertiesBuilder().food(food), "food_and_drink", textureName);
   }

   public static IC2Food simpleFood(String name, String textureFolder, String textureName, FoodProperties food) {
      return new IC2Food(name, new PropertiesBuilder().food(food), textureFolder, textureName);
   }

   public static IC2Food multiFood(String name, String textureName, int maxEats, FoodProperties food) {
      return new IC2Food(name, new PropertiesBuilder().food(food).maxDamage(maxEats).canRepair(false), "food_and_drink", textureName);
   }

   public static IC2Food multiFood(String name, String textureFolder, String textureName, int maxEats, FoodProperties food) {
      return new IC2Food(name, new PropertiesBuilder().food(food).maxDamage(maxEats).canRepair(false), textureFolder, textureName);
   }

   public IC2Food setAutoEatable() {
      this.autoEatable = true;
      return this;
   }

   @Override
   public boolean canAutoEat(ItemStack stack) {
      return this.autoEatable;
   }

   public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
      if (this.isEdible()) {
         if (this.getMaxDamage(stack) > 1) {
            if (!(entity instanceof Player player) || !player.isCreative()) {
               stack.grow(1);
            }

            entity.eat(level, stack);
            stack.hurtAndBreak(1, entity, IC2Item.get(entity.getUsedItemHand()));
         } else {
            entity.eat(level, stack);
         }
      }

      return stack;
   }

   @Override
   public int getFoodValue(ItemStack stack) {
      FoodProperties props = this.getFoodProperties(stack, null);
      return props == null ? 0 : props.getNutrition();
   }

   @Override
   public ItemStack onEaten(ItemStack stack, Level level, Player player) {
      if (this.getMaxDamage(stack) > 1) {
         if (!player.getAbilities().instabuild) {
            stack.grow(1);
         }

         player.eat(level, stack);
         stack.hurtAndBreak(1, player, IC2Item.empty());
         return stack;
      } else {
         return this.getCraftingRemainingItem(stack);
      }
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return enchantment == Enchantments.MENDING ? false : super.canApplyAtEnchantingTable(stack, enchantment);
   }
}
