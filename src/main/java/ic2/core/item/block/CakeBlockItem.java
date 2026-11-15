package ic2.core.item.block;

import ic2.core.IC2;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.IC2TexturedBlockItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CakeBlockItem extends IC2TexturedBlockItem {
   public CakeBlockItem(Block block, String textureFolder, String textureName, FoodProperties props) {
      super(block, new Properties().durability(7).food(props), textureFolder, textureName);
   }

   public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
      if (this.isEdible()) {
         if (!(entity instanceof Player player) || !player.isCreative()) {
            stack.grow(1);
         }

         entity.eat(level, stack);
         stack.hurtAndBreak(1, entity, IC2Item.get(entity.getUsedItemHand()));
      }

      return stack;
   }
}
