package ic2.core.item.upgrades.io.item;

import ic2.api.recipes.ingridients.queue.IStackOutput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.tiles.IMachine;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.inv.inventory.ImportExportInventory;
import ic2.core.item.upgrades.base.BaseTransportUpgrade;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ExportUpgradeItem extends BaseTransportUpgrade implements IHasHeldSlotInventory {
   public ExportUpgradeItem() {
      super("export");
   }

   @Override
   public void onTick(ItemStack stack, IMachine machine) {
      if (machine.getWorldObj().getGameTime() % 20L == 0L) {
         this.exportItems(stack, machine, null);
      }
   }

   @Override
   public void onMachineFinishedRecipePost(ItemStack stack, IMachine machine, IMachineRecipeList.RecipeEntry entry, List<IStackOutput> drops) {
      this.exportItems(stack, machine, drops);
   }

   @Override
   public void onMachineProcessed(ItemStack stack, IMachine machine) {
      this.exportItems(stack, machine, null);
   }

   public void exportItems(ItemStack stack, IMachine machine, List<IStackOutput> drops) {
      Direction dir = this.getFacing(stack);
      if (dir != null) {
         IItemTransporter target = TransporterManager.getTransporter(machine.getConnectedInventory(dir));
         if (target == null) {
            target = TransporterManager.getTransporter(machine.getConnectedTube(dir));
            if (target == null) {
               return;
            }
         }

         int cap = Math.min(machine.getAvailableEnergy() * 3, this.getTransferSize(stack));
         if (cap > 0) {
            if (drops == null) {
               IItemTransporter source = TransporterManager.getTransporter(machine);
               if (source != null) {
                  int size = Math.min(machine.getAvailableEnergy() * 3, this.getTransferSize(stack));
                  if (size > 0) {
                     IFilter filter = this.getFilter(stack);
                     int totalAdded = 0;

                     while (size > totalAdded) {
                        ItemStack found = source.removeItem(filter, dir, cap, true);
                        if (found.isEmpty()) {
                           break;
                        }

                        int added = target.addItem(found, dir.getOpposite(), false);
                        if (added <= 0) {
                           break;
                        }

                        source.removeItem(StackFilter.defaultCompare(found), dir, added, false);
                        totalAdded += added;
                     }

                     if (totalAdded > 0) {
                        machine.useEnergy(Math.max(1, totalAdded / 3), true);
                     }
                  }
               }
            } else if (!drops.isEmpty() && (drops.size() != 1 || drops.get(0).getStack().getCount() >= 4)) {
               for (int i = 0; i < drops.size(); i++) {
                  IStackOutput output = drops.get(i);
                  ItemStack drop = output.getStack();
                  if (drop.isEmpty()) {
                     drops.remove(i--);
                  } else {
                     int added = target.addItem(StackUtil.copyWithSize(drop, Math.min(drop.getCount(), cap)), dir.getOpposite(), false);
                     if (added > 0) {
                        drop.shrink(added);
                        if (drop.isEmpty()) {
                           drops.remove(i--);
                        }

                        cap -= added;
                        machine.useEnergy(added / 3, true);
                     }
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
