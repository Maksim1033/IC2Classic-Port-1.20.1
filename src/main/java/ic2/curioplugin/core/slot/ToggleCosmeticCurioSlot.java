package ic2.curioplugin.core.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.inventory.CosmeticCurioSlot;

public class ToggleCosmeticCurioSlot extends CosmeticCurioSlot {
   MutableBoolean visible;

   public ToggleCosmeticCurioSlot(
      Player player, IDynamicStackHandler handler, int index, String identifier, int xPosition, int yPosition, MutableBoolean visible
   ) {
      super(player, handler, index, identifier, xPosition, yPosition);
      this.visible = visible;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isActive() {
      return this.visible.getValue();
   }

   public boolean mayPlace(ItemStack stack) {
      return this.visible.getValue() && super.mayPlace(stack);
   }

   public boolean mayPickup(Player playerIn) {
      return this.visible.getValue() && super.mayPickup(playerIn);
   }
}
