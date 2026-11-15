package ic2.core.item.upgrades.base;

import ic2.api.items.IUpgradeItem;
import ic2.api.util.DirectionList;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.platform.player.KeyHelper;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public abstract class BaseDirectionalUpgrade extends BaseUpgradeItem.SimpleUpgradeItem {
   public static final String FACING_TAG = "facing";

   public BaseDirectionalUpgrade(String itemName) {
      super(itemName);
      this.functions.add(IUpgradeItem.Functions.TICK);
      this.functions.add(IUpgradeItem.Functions.RECIPE);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      if (this instanceof IHasHeldGui) {
         helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SIDE_INV_KEY, "tooltip.ic2.open_item_inventory", new Object[0]));
      }

      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.BLOCK_CLICK, "tooltip.item.ic2.upgrade.set_side", new Object[0]));
      helper.addKeybindingTooltip(
         this.buildKeyDescription(KeyHelper.BLOCK_CLICK, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.upgrade.set_opposite_side", new Object[0])
      );
   }

   public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      Direction facing = this.getFacing(stack);
      if (facing != null) {
         tooltip.add(this.translate("tooltip.item.ic2.direction", new Object[]{DirectionList.getName(facing)}));
      }
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      if (context.getPlayer() != null) {
         this.setFacing(stack, context.isSecondaryUseActive() ? context.getClickedFace().getOpposite() : context.getClickedFace());
         if (!context.getLevel().isClientSide) {
            context.getPlayer()
               .displayClientMessage(this.translate("tooltip.item.ic2.set_direction", new Object[]{DirectionList.getName(this.getFacing(stack))}), false);
         }

         return InteractionResult.SUCCESS;
      } else {
         return super.onItemUseFirst(stack, context);
      }
   }

   public void setFacing(ItemStack stack, Direction facing) {
      if (!stack.isEmpty()) {
         if (facing == null) {
            if (stack.getTag() != null) {
               stack.getTag().remove("facing");
               if (stack.getTag().isEmpty()) {
                  stack.setTag(null);
               }
            }
         } else {
            stack.getOrCreateTag().putByte("facing", (byte)facing.get3DDataValue());
         }
      }
   }

   public Direction getFacing(ItemStack stack) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      return nbt.contains("facing") ? Direction.from3DDataValue(nbt.getInt("facing")) : null;
   }

   public <T> T getCapability(Object obj, Direction side, Capability<T> cap) {
      return (T)(obj instanceof ICapabilityProvider ? ((ICapabilityProvider)obj).getCapability(cap, side).orElse(null) : null);
   }
}
