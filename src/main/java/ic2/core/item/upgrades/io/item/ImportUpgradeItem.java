package ic2.core.item.upgrades.io.item;

import ic2.api.recipes.ingridients.queue.IStackOutput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.tiles.IMachine;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.inventory.filter.ArrayAndFilter;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.filter.special.MachineFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.inv.inventory.ImportExportInventory;
import ic2.core.item.upgrades.base.BaseTransportUpgrade;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ImportUpgradeItem extends BaseTransportUpgrade implements IHasHeldSlotInventory {
   public ImportUpgradeItem() {
      super("import");
   }

   @Override
   public void onTick(ItemStack stack, IMachine machine) {
      if (machine.getWorldObj().getGameTime() % 20L == 0L) {
         this.importItems(stack, machine);
      }
   }

   @Override
   public void onMachineFinishedRecipePost(ItemStack stack, IMachine machine, IMachineRecipeList.RecipeEntry entry, List<IStackOutput> drops) {
      if (!drops.isEmpty() && (entry == null || entry.getInputs().length != 1 || entry.getInputs()[0].getInputSize() >= 4)) {
         this.importItems(stack, machine);
      }
   }

   @Override
   public void onMachineProcessed(ItemStack stack, IMachine machine) {
      this.importItems(stack, machine);
   }

   public void importItems(ItemStack stack, IMachine machine) {
      Direction dir = this.getFacing(stack);
      if (dir != null) {
         IItemTransporter source = TransporterManager.getTransporter(machine.getConnectedInventory(dir));
         if (source != null) {
            IItemTransporter target = TransporterManager.getTransporter(machine);
            if (target != null) {
               int size = Math.min(machine.getAvailableEnergy() * 3, this.getTransferSize(stack));
               if (size > 0) {
                  int totalAdded = 0;
                  IFilter filter = new ArrayAndFilter(new MachineFilter(machine), this.getFilter(stack));

                  while (size > totalAdded) {
                     ItemStack found = source.removeItem(filter, dir.getOpposite(), size, true);
                     if (found.isEmpty()) {
                        break;
                     }

                     int added = target.addItem(found, dir, false);
                     if (added <= 0) {
                        break;
                     }

                     source.removeItem(StackFilter.defaultCompare(found), dir.getOpposite(), added, false);
                     totalAdded += added;
                  }

                  if (totalAdded > 0) {
                     machine.useEnergy(Math.max(1, totalAdded / 3), true);
                  }
               }
            }
         }
      }
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new ImportExportInventory(player, this, stack, null).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return new ImportExportInventory(player, this, stack, slot).load(stack);
   }
}
