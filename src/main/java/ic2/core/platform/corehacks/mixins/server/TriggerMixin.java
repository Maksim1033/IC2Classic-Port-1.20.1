package ic2.core.platform.corehacks.mixins.server;

import ic2.core.platform.registries.IC2Advancements;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InventoryChangeTrigger.class})
public class TriggerMixin {
   @Inject(
      method = {"trigger"},
      at = {@At("HEAD")}
   )
   private void onTrigger(ServerPlayer player, Inventory inventory, ItemStack stack, CallbackInfo info) {
      if (!stack.isEmpty()) {
         IC2Advancements.RECIPE_ITEM_TRIGGER.onTrigger(player, stack);
      }
   }
}
