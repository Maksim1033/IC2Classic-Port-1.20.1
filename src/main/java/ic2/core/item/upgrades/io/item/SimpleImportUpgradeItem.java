package ic2.core.item.upgrades.io.item;

import ic2.api.items.IUpgradeItem;
import ic2.api.recipes.ingridients.queue.IStackOutput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.tiles.IMachine;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.filter.special.MachineFilter;
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

public class SimpleImportUpgradeItem extends BaseDirectionalUpgrade {
   public SimpleImportUpgradeItem() {
      super("simple_import");
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
         this.importItems(stack, machine);
      }
   }

   @Override
   public void onMachineFinishedRecipePost(ItemStack stack, IMachine machine, IMachineRecipeList.RecipeEntry entry, List<IStackOutput> drops) {
      this.importItems(stack, machine);
   }

   @Override
   public void onMachineProcessed(ItemStack stack, IMachine machine) {
      this.importItems(stack, machine);
   }

   protected void importItems(ItemStack stack, IMachine machine) {
      Direction dir = this.getFacing(stack);
      if (dir != null) {
         IItemTransporter source = TransporterManager.getTransporter(machine.getConnectedInventory(dir));
         if (source != null) {
            IItemTransporter target = TransporterManager.getTransporter(machine);
            if (target != null) {
               int limit = Math.min(9, stack.getCount());
               IFilter filter = new MachineFilter(machine);

               for (int i = 0; i < limit; i++) {
                  ItemStack result = source.removeItem(filter, dir.getOpposite(), 1, true);
                  if (result.isEmpty() || target.addItem(result, dir, false) <= 0) {
                     break;
                  }

                  source.removeItem(StackFilter.defaultCompare(result), dir.getOpposite(), 1, false);
               }
            }
         }
      }
   }
}
