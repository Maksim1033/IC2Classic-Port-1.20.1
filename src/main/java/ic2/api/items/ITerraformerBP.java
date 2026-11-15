package ic2.api.items;

import ic2.api.tiles.ITerraformer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ITerraformerBP {
   boolean canInsert(ItemStack var1, Player var2, Level var3, BlockPos var4);

   void onInsert(ItemStack var1, Player var2, Level var3, BlockPos var4);

   boolean isRandomized(ItemStack var1);

   int getEnergyUsage(ItemStack var1);

   int getRadius(ItemStack var1);

   boolean terraform(ItemStack var1, Level var2, BlockPos var3, ITerraformer var4);
}
