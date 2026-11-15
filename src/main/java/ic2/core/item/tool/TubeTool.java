package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.block.transport.item.TubeAction;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.item.base.IC2SimpleItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.player.KeyHelper;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TubeTool extends IC2SimpleItem {
   public TubeTool() {
      super("tube_tool", new PropertiesBuilder().maxStackSize(1), "tools", "tube_tool");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.MODE_KEY, "tooltip.item.ic2.tube_tool.switch_mode", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.BLOCK_CLICK, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.tube_tool.use", new Object[0]));
      helper.addSimpleToolTip(this.getAction(stack).getName());
      helper.addSimpleToolTip(this.getAction(stack).getDesc());
      helper.addSimpleToolTip(this.getAction(stack).getApplicable());
      helper.addDataTooltip(TubeAction.getModeList());
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (IC2.KEYBOARD.isModeSwitchKeyDown(player)) {
         ItemStack stack = player.getItemInHand(hand);
         CompoundTag data = stack.getOrCreateTag();
         TubeAction action = TubeAction.getNextAction(data.getString("action"));
         data.putString("action", action.getId());
         if (IC2.PLATFORM.isSimulating()) {
            player.displayClientMessage(action.getName(), false);
         }

         return InteractionResultHolder.success(stack);
      } else {
         return super.use(world, player, hand);
      }
   }

   public InteractionResult useOn(UseOnContext context) {
      if (context.getPlayer() != null && context.isSecondaryUseActive()) {
         BlockEntity tile = context.getPlayer().level().getBlockEntity(context.getClickedPos());
         if (tile instanceof TubeTileEntity
            && ((TubeTileEntity)tile)
               .doTubeAction(
                  context.getClickedFace(),
                  context.getClickLocation().subtract(Vec3.atLowerCornerOf(context.getClickedPos())),
                  context.getPlayer(),
                  this.getAction(context.getItemInHand())
               )) {
            return InteractionResult.SUCCESS;
         }
      }

      return super.useOn(context);
   }

   public TubeAction getAction(ItemStack stack) {
      return TubeAction.byIdOrDefault(StackUtil.getNbtData(stack).getString("action"));
   }
}
