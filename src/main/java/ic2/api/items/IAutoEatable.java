package ic2.api.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IAutoEatable {
   boolean canAutoEat(ItemStack var1);

   int getFoodValue(ItemStack var1);

   ItemStack onEaten(ItemStack var1, Level var2, Player var3);
}
