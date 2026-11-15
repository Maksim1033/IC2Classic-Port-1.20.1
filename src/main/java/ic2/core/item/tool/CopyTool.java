package ic2.core.item.tool;

import ic2.api.tiles.ICopyableSettings;
import ic2.core.IC2;
import ic2.core.inventory.handler.IHasInventoryHandler;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.item.base.IC2SimpleItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.core.BlockPos;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CopyTool extends IC2SimpleItem {
   public CopyTool() {
      super("copy_tool", new PropertiesBuilder().maxStackSize(1), "tools", "copy_tool");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, "tooltip.item.ic2.copy_tool.mode_key", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.ALT_KEY, "tooltip.item.ic2.copy_tool.toClear", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.BLOCK_CLICK, "tooltip.item.ic2.copy_tool.toApply", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.BLOCK_CLICK, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.copy_tool.toCopy", new Object[0]));
      helper.addSimpleToolTip(
         "tooltip.item.ic2.copy_tool.mode",
         this.translate(this.isInvSettings(stack) ? "tooltip.item.ic2.copy_tool.mode.settings" : "tooltip.item.ic2.copy_tool.mode.inv")
      );
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      if (IC2.KEYBOARD.isAltKeyDown(player)) {
         ItemStack stack = player.getItemInHand(hand);
         stack.setTag(null);
         return InteractionResultHolder.success(stack);
      } else if (IC2.KEYBOARD.isModeSwitchKeyDown(player)) {
         ItemStack stack = player.getItemInHand(hand);
         CompoundTag tag = stack.getOrCreateTag();
         tag.putBoolean("inv_mode", !tag.getBoolean("inv_mode"));
         if (!level.isClientSide()) {
            player.sendSystemMessage(
               this.translate(
                  "tooltip.item.ic2.copy_tool.mode_switch",
                  new Object[]{this.translate(this.isInvSettings(stack) ? "tooltip.item.ic2.copy_tool.mode.settings" : "tooltip.item.ic2.copy_tool.mode.inv")}
               )
            );
         }

         return InteractionResultHolder.success(stack);
      } else {
         return super.use(level, player, hand);
      }
   }

   public boolean isInvSettings(ItemStack stack) {
      return !StackUtil.getNbtData(stack).getBoolean("inv_mode");
   }

   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockEntity entity = level.getBlockEntity(pos);
      if (this.isInvSettings(stack)) {
         if (entity instanceof IHasInventoryHandler invHandler && invHandler.allowsUI() && invHandler.getInventoryHandler() != null) {
            InventoryHandler handler = invHandler.getInventoryHandler();
            if (context.isSecondaryUseActive()) {
               CompoundTag tag = new CompoundTag();
               handler.saveSettings(tag);
               stack.addTagElement(ForgeRegistryWrapper.getBlockEntityTypeKey(entity.getType()).toString() + ".inv", tag);
               if (!level.isClientSide()) {
                  context.getPlayer().sendSystemMessage(this.translate("tooltip.item.ic2.copy_tool.copied"));
               }

               return InteractionResult.SUCCESS;
            }

            String id = ForgeRegistryWrapper.getBlockEntityTypeKey(entity.getType()).toString() + ".inv";
            if (stack.getTag() != null && stack.getTag().contains(id)) {
               handler.loadSettings(stack.getTagElement(id));
               if (!level.isClientSide()) {
                  context.getPlayer().sendSystemMessage(this.translate("tooltip.item.ic2.copy_tool.applied"));
               }

               return InteractionResult.SUCCESS;
            }

            if (!level.isClientSide()) {
               context.getPlayer().sendSystemMessage(this.translate("tooltip.item.ic2.copy_tool.nothing_to_applied"));
            }

            return InteractionResult.FAIL;
         }

         if (context.getPlayer() != null) {
            if (!level.isClientSide()) {
               context.getPlayer()
                  .sendSystemMessage(
                     this.translate(
                        context.isSecondaryUseActive() ? "tooltip.item.ic2.copy_tool.nothing_to_copy" : "tooltip.item.ic2.copy_tool.nothing_to_applied"
                     )
                  );
            }

            return InteractionResult.FAIL;
         }
      } else {
         if (entity instanceof ICopyableSettings settings) {
            if (context.isSecondaryUseActive()) {
               CompoundTag tag = new CompoundTag();
               settings.saveSettings(tag);
               stack.addTagElement(ForgeRegistryWrapper.getBlockEntityTypeKey(entity.getType()).toString() + ".settings", tag);
               if (!level.isClientSide()) {
                  context.getPlayer().sendSystemMessage(this.translate("tooltip.item.ic2.copy_tool.copied"));
               }

               return InteractionResult.SUCCESS;
            }

            String idx = ForgeRegistryWrapper.getBlockEntityTypeKey(entity.getType()).toString() + ".settings";
            if (stack.getTag() != null && stack.getTag().contains(idx)) {
               settings.loadSettings(stack.getTagElement(idx));
               if (!level.isClientSide()) {
                  context.getPlayer().sendSystemMessage(this.translate("tooltip.item.ic2.copy_tool.applied"));
               }

               return InteractionResult.SUCCESS;
            }

            if (!level.isClientSide()) {
               context.getPlayer().sendSystemMessage(this.translate("tooltip.item.ic2.copy_tool.nothing_to_applied"));
            }

            return InteractionResult.FAIL;
         }

         if (context.getPlayer() != null) {
            if (!level.isClientSide()) {
               context.getPlayer()
                  .sendSystemMessage(
                     this.translate(
                        context.isSecondaryUseActive() ? "tooltip.item.ic2.copy_tool.nothing_to_copy" : "tooltip.item.ic2.copy_tool.nothing_to_applied"
                     )
                  );
            }

            return InteractionResult.FAIL;
         }
      }

      return InteractionResult.PASS;
   }
}
