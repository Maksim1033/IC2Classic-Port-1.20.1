package ic2.core.item.upgrades.io.item;

import ic2.api.items.IUpgradeItem;
import ic2.api.recipes.ingridients.queue.IStackOutput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.tiles.IMachine;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.upgrades.base.BaseDirectionalUpgrade;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SimpleExportUpgradeItem extends BaseDirectionalUpgrade {
   public SimpleExportUpgradeItem() {
      super("simple_export");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      helper.addSimpleToolTip("tooltip.item.ic2.upgrade.stacking");
   }

   @Override
   public IUpgradeItem.UpgradeType getType(ItemStack stack) {
      return IUpgradeItem.UpgradeType.TRANSPORT_MOD;
   }

   @Override
   public void onTick(ItemStack stack, IMachine machine) {
      if (!machine.isMachineWorking() && machine.getWorldObj().getGameTime() % 20L == 0L) {
         this.sendItems(stack, machine);
      }
   }

   @Override
   public void onMachineProcessed(ItemStack stack, IMachine machine) {
      this.sendItems(stack, machine);
   }

   @Override
   public void onMachineFinishedRecipePost(ItemStack stack, IMachine machine, IMachineRecipeList.RecipeEntry entry, List<IStackOutput> drops) {
      if (!drops.isEmpty()) {
         Direction dir = this.getFacing(stack);
         if (dir != null) {
            IItemTransporter target = TransporterManager.getTransporter(machine.getConnectedInventory(dir));
            if (target == null) {
               target = TransporterManager.getTransporter(machine.getConnectedTube(dir));
               if (target == null) {
                  return;
               }
            }

            int count = Math.min(stack.getCount(), 9);
            int i = 0;

            for (int m = drops.size(); i < m && count > 0; i++) {
               IStackOutput output = drops.get(i);
               ItemStack out = output.getStack().copy().split(count);
               int added = target.addItem(out, dir.getOpposite(), false);
               if (added > 0) {
                  count -= added;
                  output.getStack().shrink(added);
                  if (output.getStack().isEmpty()) {
                     drops.remove(i--);
                     m--;
                  }
               }
            }

            if (count > 0) {
               IItemTransporter source = TransporterManager.getTransporter(machine);
               if (source == null) {
                  return;
               }

               this.sendItems(count, source, target, dir);
            }
         }
      }
   }

   protected void sendItems(ItemStack stack, IMachine machine) {
      Direction dir = this.getFacing(stack);
      if (dir != null) {
         IItemTransporter target = TransporterManager.getTransporter(machine.getConnectedInventory(dir));
         if (target == null) {
            target = TransporterManager.getTransporter(machine.getConnectedTube(dir));
            if (target == null) {
               return;
            }
         }

         IItemTransporter source = TransporterManager.getTransporter(machine);
         if (source != null) {
            this.sendItems(Math.min(stack.getCount(), 9), source, target, dir);
         }
      }
   }

   protected void sendItems(int left, IItemTransporter source, IItemTransporter target, Direction dir) {
      while (left > 0) {
         ItemStack found = source.removeItem(SpecialFilters.ANY_ITEM, dir, left, true);
         if (!found.isEmpty()) {
            int added = target.addItem(found, dir.getOpposite(), false);
            if (added > 0) {
               source.removeItem(StackFilter.defaultCompare(found), dir, added, false);
               left -= added;
               continue;
            }
         }
         break;
      }
   }
}
