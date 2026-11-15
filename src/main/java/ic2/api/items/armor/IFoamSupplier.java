package ic2.api.items.armor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IFoamSupplier {
   boolean canProvideFoam(Player var1, ItemStack var2, IFoamSupplier.InventoryType var3, int var4);

   void useFoam(Player var1, ItemStack var2, int var3);

   int getFreeFoamSpace(ItemStack var1);

   void fillFoam(ItemStack var1, int var2);

   public static enum InventoryType {
      HOTBAR,
      OFFHAND,
      ARMOR,
      CURIO;
   }
}
