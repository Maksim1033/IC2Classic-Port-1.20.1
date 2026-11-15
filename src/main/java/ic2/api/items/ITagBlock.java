package ic2.api.items;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public interface ITagBlock {
   boolean matches(ItemStack var1, Block var2);

   List<Block> getBlocks(ItemStack var1);
}
