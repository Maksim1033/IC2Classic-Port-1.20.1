package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import ic2.core.IC2;
import ic2.core.item.wearable.base.IArmorToolTip;
import ic2.core.platform.player.KeyHelper;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnergyShieldModuleItem extends BaseModuleItem implements IArmorToolTip, IModuleUseAction {
   public static final String ALWAYS = "always";

   public EnergyShieldModuleItem(String textureFolder, String textureName) {
      super("energy_shield_overlay_module", null, textureFolder, textureName, IArmorModule.ModuleType.HUD);
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.HEAD;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.module_energy_shield", new Object[0]));
      helper.addSimpleToolTip(isAlways(stack) ? "tooltip.item.ic2.module_energy_shield.enable" : "tooltip.item.ic2.module_energy_shield.disable");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack armor, ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.module_energy_shield", new Object[0]));
      helper.addSimpleToolTip(isAlways(armor) ? "tooltip.item.ic2.module_energy_shield.enable" : "tooltip.item.ic2.module_energy_shield.disable");
   }

   @Override
   public boolean use(Level world, Player player, ItemStack module, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      if (module.getItem() != this || !player.isShiftKeyDown()) {
         return false;
      } else if (world.isClientSide) {
         return true;
      } else {
         this.onUninstall(module, armor, holder);
         boolean result = !isAlways(module);
         setAlways(module, result);
         if (IC2.PLATFORM.isSimulating()) {
            player.displayClientMessage(
               this.translate(result ? "tooltip.item.ic2.module_energy_shield.enable" : "tooltip.item.ic2.module_energy_shield.disable"), false
            );
         }

         this.onInstall(module, armor, holder);
         return true;
      }
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      if (player.isShiftKeyDown()) {
         ItemStack stack = player.getItemInHand(hand);
         boolean result = !isAlways(stack);
         setAlways(stack, result);
         if (IC2.PLATFORM.isSimulating()) {
            player.displayClientMessage(
               this.translate(result ? "tooltip.item.ic2.module_energy_shield.enable" : "tooltip.item.ic2.module_energy_shield.disable"), false
            );
         }

         return InteractionResultHolder.success(stack);
      } else {
         return super.use(world, player, hand);
      }
   }

   @Override
   public void onInstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      boolean isAlways = isAlways(stack);
      setAlways(armor, isAlways);
      holder.addAddModifier(armor, isAlways ? IArmorModule.ArmorMod.ENERGY_SHIELD_ALWAYS : IArmorModule.ArmorMod.ENERGY_SHIELD, 1);
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      boolean isAlways = isAlways(armor);
      setAlways(stack, isAlways);
      armor.removeTagKey("always");
      holder.removeAddModifier(armor, isAlways ? IArmorModule.ArmorMod.ENERGY_SHIELD_ALWAYS : IArmorModule.ArmorMod.ENERGY_SHIELD, 1);
   }

   public static void setAlways(ItemStack stack, boolean value) {
      stack.getOrCreateTag().putBoolean("always", value);
   }

   public static boolean isAlways(ItemStack stack) {
      return StackUtil.getNbtData(stack).getBoolean("always");
   }
}
