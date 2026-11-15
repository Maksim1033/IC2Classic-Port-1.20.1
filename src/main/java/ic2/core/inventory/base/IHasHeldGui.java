package ic2.core.inventory.base;

import ic2.core.utils.helpers.StackUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IHasHeldGui {
   String DEFAULT_GUI_ID = "GUI_ID";

   IPortableInventory getInventory(Player var1, InteractionHand var2, ItemStack var3);

   default int getGuiId(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("GUI_ID");
   }

   default void setGuiID(ItemStack stack, int id) {
      if (id == -1) {
         stack.removeTagKey("GUI_ID");
      } else {
         stack.getOrCreateTag().putInt("GUI_ID", id);
      }
   }
}
