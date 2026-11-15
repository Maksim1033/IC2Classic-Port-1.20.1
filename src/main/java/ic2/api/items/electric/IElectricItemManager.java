package ic2.api.items.electric;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IElectricItemManager {
   int charge(ItemStack var1, int var2, int var3, boolean var4, boolean var5);

   int discharge(ItemStack var1, int var2, int var3, boolean var4, boolean var5, boolean var6);

   int getCharge(ItemStack var1);

   int getCapacity(ItemStack var1);

   boolean canUse(ItemStack var1, int var2);

   boolean use(ItemStack var1, int var2, LivingEntity var3);

   void chargeFromArmor(ItemStack var1, LivingEntity var2);

   int getTier(ItemStack var1);

   int getTransferLimit(ItemStack var1);

   Component getToolTip(ItemStack var1);
}
