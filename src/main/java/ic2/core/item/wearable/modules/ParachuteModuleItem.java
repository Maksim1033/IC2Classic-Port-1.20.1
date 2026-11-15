package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import ic2.core.IC2;
import ic2.core.item.wearable.base.IArmorToolTip;
import ic2.core.platform.player.KeyHelper;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ParachuteModuleItem extends BaseModuleItem implements IArmorToolTip {
   public static final String GLIDING = "gliding";

   public ParachuteModuleItem(String textureFolder, String textureName) {
      super("parachute_module", null, textureFolder, textureName, IArmorModule.ModuleType.BACK_SLOT);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      this.handleToolTip(stack, helper::addSimpleToolTip);
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.JUMP_KEY, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.parachute.name", new Object[0]));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack armor, ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.JUMP_KEY, KeyHelper.SNEAK_KEY, "tooltip.item.ic2.parachute.name", new Object[0]));
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      CompoundTag nbt = armor.getOrCreateTagElement("para");
      boolean gliding = nbt.getBoolean("gliding");
      if (!gliding
         && IC2.KEYBOARD.isJumpKeyDown(player)
         && IC2.KEYBOARD.isSneakKeyDown(player)
         && !player.onGround()
         && player.getDeltaMovement().y < 0.0
         && !player.isFallFlying()
         && !player.isInWater()) {
         nbt.putBoolean("gliding", true);
         nbt.putLong("lastPress", world.getGameTime());
      } else if (gliding) {
         if (player.onGround()
            || player.isInWater()
            || IC2.KEYBOARD.isJumpKeyDown(player) && IC2.KEYBOARD.isSneakKeyDown(player) && world.getGameTime() - nbt.getLong("lastPress") >= 5L) {
            nbt.putBoolean("gliding", false);
            return;
         }

         Vec3 motion = player.getDeltaMovement();
         player.setDeltaMovement(motion.x(), Math.max(-0.2, motion.y()), motion.z());
         player.moveRelative(0.02F, new Vec3(0.0, 0.0, 1.0));
         IC2.PLATFORM.resetPlayerInAirTime(player);
         player.fallDistance = 0.0F;
      }
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      armor.removeTagKey("para");
   }
}
