package ic2.core.item.upgrades.special;

import ic2.api.items.IUpgradeItem;
import ic2.api.tiles.IMachine;
import ic2.core.block.base.features.IInventoryMachine;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.upgrades.base.BaseUpgradeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CobblegenUpgradeItem extends BaseUpgradeItem.SimpleUpgradeItem {
   public CobblegenUpgradeItem() {
      super("cobble_generator");
      this.functions.add(IUpgradeItem.Functions.TICK);
   }

   @Override
   public IUpgradeItem.UpgradeType getType(ItemStack stack) {
      return IUpgradeItem.UpgradeType.CUSTOM_MOD;
   }

   @Override
   public void onTick(ItemStack stack, IMachine machine) {
      if (machine.getWorldObj().getGameTime() % 20L == 0L) {
         IItemTransporter trans = TransporterManager.getTransporter(
            machine instanceof IInventoryMachine ? ((IInventoryMachine)machine).getInputInventory() : machine
         );
         if (trans != null) {
            trans.addItem(new ItemStack(Items.COBBLESTONE, stack.getCount()), null, false);
         }
      }
   }
}
