package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import ic2.core.platform.registries.IC2Potions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ProtectiveModuleItem extends BaseModuleItem {
   public ProtectiveModuleItem(String textureFolder, String textureName) {
      super("protective_module", null, textureFolder, textureName, IArmorModule.ModuleType.ANY);
   }

   public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
      return armorType == EquipmentSlot.HEAD;
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      MobEffectInstance effect = player.getEffect(MobEffects.POISON);
      if (effect != null && this.canUseEnergy(armor, 10000 * (effect.getAmplifier() + 1))) {
         this.useEnergy(armor, 10000 * (effect.getAmplifier() + 1), player);
         player.removeEffect(MobEffects.POISON);
      }

      effect = player.getEffect(IC2Potions.RADIATION);
      if (effect != null && this.canUseEnergy(armor, 20000 * (effect.getAmplifier() + 1))) {
         this.useEnergy(armor, 20000 * (effect.getAmplifier() + 1), player);
         player.removeEffect(IC2Potions.RADIATION);
      }

      effect = player.getEffect(MobEffects.WITHER);
      if (effect != null && this.canUseEnergy(armor, 25000 * (effect.getAmplifier() + 1))) {
         this.useEnergy(armor, 25000 * (effect.getAmplifier() + 1), player);
         player.removeEffect(MobEffects.WITHER);
      }
   }
}
