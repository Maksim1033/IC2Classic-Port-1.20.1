package ic2.core.platform.corehacks.mixins.server;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({SmithingMenu.class})
public class SmithingMixin {
   @Inject(
      method = {"shrinkStackInSlot"},
      at = {@At("RETURN")},
      locals = LocalCapture.CAPTURE_FAILEXCEPTION
   )
   public void shrinkStack(int slot, CallbackInfo info, ItemStack stack) {
      stack.grow(1);
      if (stack.hasCraftingRemainingItem()) {
         ((Slot)((SmithingMenu)(Object)this).slots.get(slot)).set(stack.getCraftingRemainingItem());
      } else {
         stack.shrink(1);
      }
   }
}
