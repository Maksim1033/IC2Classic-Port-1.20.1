package ic2.api.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ICutterItem {
   void cutInsulation(Player var1, ItemStack var2, Level var3, BlockPos var4);
}
