package ic2.api.recipes.ingridients.queue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface IStackOutput {
   boolean addToInventory(IInputter var1);

   ItemStack getStack();

   void save(CompoundTag var1);
}
