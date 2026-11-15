package ic2.core.platform.corehacks.mixins.client;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(
   targets = {"net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$CustomCreativeSlot"}
)
public interface SlotMixin {
}
