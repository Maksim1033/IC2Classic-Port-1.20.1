package ic2.core.inventory.slot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class ArmorSlot extends Slot {
   public static final String[] ARMOR_SLOT_TEXTURES = new String[]{
      "item/empty_armor_slot_boots", "item/empty_armor_slot_leggings", "item/empty_armor_slot_chestplate", "item/empty_armor_slot_helmet"
   };
   Player player;
   EquipmentSlot slot;

   public ArmorSlot(Player inventoryIn, EquipmentSlot slot, int index, int xPosition, int yPosition) {
      super(inventoryIn.getInventory(), index, xPosition, yPosition);
      this.player = inventoryIn;
      this.slot = slot;
      this.setBackground(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(ARMOR_SLOT_TEXTURES[slot.getIndex()]));
   }

   public int getMaxStackSize() {
      return 1;
   }

   public boolean mayPlace(ItemStack stack) {
      return stack.canEquip(this.slot, this.player);
   }

   public boolean mayPickup(Player playerIn) {
      ItemStack itemstack = this.getItem();
      return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(playerIn);
   }
}
