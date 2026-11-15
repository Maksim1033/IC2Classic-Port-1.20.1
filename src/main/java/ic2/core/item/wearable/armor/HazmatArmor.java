package ic2.core.item.wearable.armor;

import ic2.api.items.armor.ICustomArmor;
import ic2.api.util.IC2DamageSource;
import ic2.core.inventory.filter.IFilter;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.base.IC2Item;
import ic2.core.item.wearable.base.IC2ArmorBase;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Materials;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HazmatArmor extends IC2ArmorBase implements ICustomArmor {
   private static IFilter AIR_CELL_FILTER = i -> i.getItem() == IC2Items.CELL_AIR;

   public HazmatArmor(String itemName, EquipmentSlot slot) {
      super(itemName, IC2Materials.HAZMAT_ARMOR, slot);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addDataTooltip("tooltip.item.ic2.armor_hazmat.prevent_burn");
      if (this.getEquipmentSlot() == EquipmentSlot.HEAD) {
         helper.addDataTooltip("tooltip.item.ic2.armor_hazmat_helmet.provide_air");
      }
   }

   public void onArmorTick(ItemStack stack, Level world, Player player) {
      if (this.getEquipmentSlot() == EquipmentSlot.HEAD) {
         if (isFullHazmatSuit(player)) {
            if (player.isInLava()
               || world.getBlockStates(player.getBoundingBox().expandTowards(-0.10000000149011612, -0.4000000059604645, -0.10000000149011612))
                  .anyMatch(T -> T.is(BlockTags.FIRE))) {
               player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 1));
            }

            player.clearFire();
         }

         if (player.getAirSupply() <= 100 && player.getInventory().contains(new ItemStack(IC2Items.CELL_AIR))) {
            IItemTransporter trans = TransporterManager.getTransporter(player);
            ItemStack tmp = trans.removeItem(AIR_CELL_FILTER, Direction.DOWN, 1, false);
            if (!tmp.isEmpty()) {
               trans.addItem(new ItemStack(IC2Items.CELL_EMPTY), Direction.DOWN, false);
               player.awardStat(IC2Stats.AIR_CELLS_USED);
               player.setAirSupply(player.getAirSupply() + 150);
               player.containerMenu.broadcastChanges();
            }
         }
      }
   }

   public static boolean isFullHazmatSuit(LivingEntity base) {
      if (!(base instanceof Player player)) {
         return false;
      } else {
         for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HAND) {
               ItemStack stack = player.getItemBySlot(slot);
               if (stack.isEmpty() || !(stack.getItem() instanceof HazmatArmor)) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public boolean hazmatAbsorbs(DamageSource source) {
      return source.is(DamageTypeTags.IS_FIRE)
         || source.is(DamageTypes.LIGHTNING_BOLT)
         || source.getMsgId().equals(IC2DamageSource.ELECTRICITY.getMsgId())
         || source == IC2DamageSource.RADIATION;
   }

   @Override
   public String getTextureFolder() {
      return "armor/hazmat";
   }

   @Override
   public String getTextureName() {
      switch (this.getEquipmentSlot()) {
         case HEAD:
            return "scuba_helmet";
         case CHEST:
            return "suit";
         case LEGS:
            return "leggings";
         case FEET:
            return "rubber_boots";
         default:
            return "";
      }
   }

   @Override
   public String getArmorTexture() {
      return "ic2:textures/models/armor/hazmat";
   }

   @Override
   public ICustomArmor.AbsorptionProperties getProperties(LivingEntity entity, ItemStack armor, DamageSource source, double damage, EquipmentSlot slot) {
      if (slot == EquipmentSlot.HEAD && this.hazmatAbsorbs(source) && isFullHazmatSuit(entity)) {
         if (source.is(DamageTypeTags.IS_FIRE)) {
            entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 1));
         }

         return new ICustomArmor.AbsorptionProperties(10, 1.0, 2147483647);
      } else {
         return slot == EquipmentSlot.FEET && source.is(DamageTypes.HOT_FLOOR)
            ? new ICustomArmor.AbsorptionProperties(10, damage < 8.0 ? 1.0 : 0.875, (armor.getMaxDamage() - armor.getDamageValue() + 1) * 2)
            : new ICustomArmor.AbsorptionProperties(0, 0.2, (armor.getMaxDamage() - armor.getDamageValue() + 1) / 2);
      }
   }

   @Override
   public void damageArmor(LivingEntity player, ItemStack stack, DamageSource source, int damage, EquipmentSlot slot, ICustomArmor.DamageType type) {
      if (!this.hazmatAbsorbs(source) || !isFullHazmatSuit(player)) {
         if (!source.is(DamageTypes.IN_FIRE) && (slot == EquipmentSlot.FEET || !source.is(DamageTypes.HOT_FLOOR))) {
            int damageTotal = damage * 2;
            if (slot == EquipmentSlot.FEET && source.is(DamageTypes.HOT_FLOOR)) {
               damageTotal = (damage + 1) / 2;
            }

            stack.hurtAndBreak(damageTotal, player, IC2Item.get(slot));
         }
      }
   }

   @Override
   public boolean canBlockDamageSource(LivingEntity player, ItemStack stack, DamageSource source, EquipmentSlot slot) {
      if (slot == EquipmentSlot.FEET && source.is(DamageTypes.HOT_FLOOR)) {
         return true;
      } else {
         return slot == EquipmentSlot.HEAD && this.hazmatAbsorbs(source) ? true : !source.is(DamageTypeTags.BYPASSES_ARMOR) || source.is(DamageTypes.DROWN);
      }
   }
}
