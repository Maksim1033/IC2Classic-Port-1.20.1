package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import ic2.core.IC2;
import ic2.core.block.misc.LightBlock;
import ic2.core.item.wearable.base.IArmorToolTip;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LightModuleItem extends BaseModuleItem implements IArmorToolTip {
   public static final String LIGHT = "light";

   public LightModuleItem(String textureFolder, String textureName) {
      super("light_module", null, textureFolder, textureName, IArmorModule.ModuleType.GENERIC);
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.HEAD;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      this.handleToolTip(stack, helper::addSimpleToolTip);
      helper.addKeybindingTooltip(
         this.buildKeyDescription(new KeyHelper[]{KeyHelper.SNEAK_KEY, KeyHelper.HUD_KEY, KeyHelper.MODE_KEY}, "tooltip.item.ic2.light.name", new Object[0])
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack armor, ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(
         this.buildKeyDescription(new KeyHelper[]{KeyHelper.SNEAK_KEY, KeyHelper.HUD_KEY, KeyHelper.MODE_KEY}, "tooltip.item.ic2.light.name", new Object[0])
      );
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      armor.removeTagKey("light");
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      CompoundTag nbt = armor.getOrCreateTagElement("light");
      boolean enabled = nbt.getBoolean("enabled");
      PlayerHandler handler = PlayerHandler.getHandler(player);
      if (handler.hudModeKeyDown && handler.modeSwitchKeyDown && IC2.KEYBOARD.isSneakKeyDown(player) && world.getGameTime() - nbt.getLong("last") >= 5L) {
         nbt.putBoolean("enabled", !enabled);
         nbt.putLong("last", world.getGameTime());
         if (!world.isClientSide) {
            if (enabled) {
               player.displayClientMessage(this.translate("tooltip.item.ic2.light.disable"), false);
            } else {
               player.displayClientMessage(this.translate("tooltip.item.ic2.light.enable"), false);
            }
         }
      }

      if (enabled && LightBlock.placeLightBlock(world, player.blockPosition().above(), 5, () -> this.canUseEnergy(armor, 10))) {
         this.useEnergy(armor, 10, player);
      }
   }
}
