package ic2.core.inventory.slot;

import java.util.function.BooleanSupplier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LockablePlayerSlot extends Slot {
   BooleanSupplier locked;

   public LockablePlayerSlot(Container p_i1824_1_, int slot, int x, int y, BooleanSupplier locked) {
      super(p_i1824_1_, slot, x, y);
      this.locked = locked;
   }

   public boolean mayPickup(Player p_82869_1_) {
      return !this.locked.getAsBoolean() && super.mayPickup(p_82869_1_);
   }

   public boolean mayPlace(ItemStack p_75214_1_) {
      return !this.locked.getAsBoolean() && super.mayPlace(p_75214_1_);
   }
}
