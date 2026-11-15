package ic2.core.item.upgrades.inv;

import ic2.core.IC2;
import ic2.core.item.base.IC2SimpleItem;
import ic2.core.item.base.features.IHandlerItem;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemLimiterUpgradeItem extends IC2SimpleItem implements IHandlerItem {
   public ItemLimiterUpgradeItem() {
      super("item_limiter", "upgrades/inventory", "item_limiter");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addSimpleToolTip("Max Stacksize: " + getMax(stack));
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (IC2.PLATFORM.isSimulating() && player.isShiftKeyDown()) {
         setMax(stack, Mth.clamp(getMax(stack) + (PlayerHandler.getHandler(player).altKeyDown ? -1 : 1), 0, 512));
         return InteractionResultHolder.success(stack);
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   @Override
   public void handleInventory(ItemStack stack, IHandlerItem.IConfigurableInventory inv) {
      inv.setDefaultMaxStackSize(getMax(stack));
   }

   public static int getMax(ItemStack stack) {
      CompoundTag data = StackUtil.getNbtData(stack);
      return data.contains("max") ? data.getInt("max") : 64;
   }

   public static void setMax(ItemStack stack, int max) {
      stack.getOrCreateTag().putInt("max", max);
   }
}
