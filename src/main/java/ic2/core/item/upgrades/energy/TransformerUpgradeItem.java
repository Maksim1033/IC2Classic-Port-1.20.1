package ic2.core.item.upgrades.energy;

import ic2.api.items.IUpgradeItem;
import ic2.api.tiles.IMachine;
import ic2.core.IC2;
import ic2.core.item.upgrades.base.BaseUpgradeItem;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TransformerUpgradeItem extends BaseUpgradeItem.SimpleUpgradeItem {
   public TransformerUpgradeItem() {
      super("transformer");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      if (IC2.CONFIG.energyEasyMode.get()) {
         helper.addSimpleToolTip("tooltip.item.ic2.upgrade_transformer.disabled");
      }
   }

   @Override
   public IUpgradeItem.UpgradeType getType(ItemStack stack) {
      return IUpgradeItem.UpgradeType.MACHINE_MOD;
   }

   @Override
   public int getExtraTier(ItemStack stack, IMachine machine) {
      return IC2.CONFIG.energyEasyMode.get() ? 0 : 1;
   }
}
