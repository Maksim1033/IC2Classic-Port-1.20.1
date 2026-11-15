package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IModuleUseAction {
   boolean use(Level var1, Player var2, ItemStack var3, ItemStack var4, IArmorModule.IArmorModuleHolder var5);
}
