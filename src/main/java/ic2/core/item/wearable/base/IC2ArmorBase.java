package ic2.core.item.wearable.base;

import com.google.common.base.Strings;
import ic2.api.items.armor.ICustomArmor;
import ic2.core.IC2;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.IItemCategoryFiller;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.plugins.IRegistryProvider;
import ic2.core.utils.tooltips.IAdvancedTooltip;
import ic2.core.utils.tooltips.ILangHelper;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.ints.Int2DoubleLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public abstract class IC2ArmorBase extends ArmorItem implements ISimpleItemModel, ILangHelper, IAdvancedTooltip, IRegistryProvider, IItemCategoryFiller {
   private ResourceLocation id;

   public IC2ArmorBase(String itemName, ArmorMaterial material, EquipmentSlot slot, @Nullable PropertiesBuilder props) {
      super(material, getType(slot), (props == null ? new PropertiesBuilder() : props).group(IC2.IC2_MAIN_GROUP).build());
      this.id = GameData.checkPrefix(itemName.toLowerCase(), false);
   }

   public static ArmorItem.Type getType(EquipmentSlot slot) {
      switch (slot) {
         case HEAD: return ArmorItem.Type.HELMET;
         case CHEST: return ArmorItem.Type.CHESTPLATE;
         case LEGS: return ArmorItem.Type.LEGGINGS;
         case FEET: return ArmorItem.Type.BOOTS;
         default: throw new IllegalArgumentException("Invalid armor slot: " + slot);
      }
   }

   public IC2ArmorBase(String itemName, ArmorMaterial material, EquipmentSlot slot) {
      this(itemName, material, slot, null);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
   }

   public void setDamage(ItemStack stack, int damage) {
      if (damage <= 0) {
         stack.removeTagKey("Damage");
      } else {
         stack.getOrCreateTag().putInt("Damage", damage);
      }
   }

   public abstract String getTextureFolder();

   public abstract String getTextureName();

   public abstract String getArmorTexture();

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), this.getTextureFolder()).get(this.getTextureName());
   }

   @Nullable
   public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
      return this.getArmorTexture() + "_" + (slot == EquipmentSlot.LEGS ? "2" : "1") + (!Strings.isNullOrEmpty(type) ? "_" + type : "") + ".png";
   }

   public static float applyArmor(LivingEntity entity, DamageSource source, double damage) {
      ObjectArrayPriorityQueue<ICustomArmor.AbsorptionProperties> properties = new ObjectArrayPriorityQueue(4);
      Int2DoubleLinkedOpenHashMap map = new Int2DoubleLinkedOpenHashMap(4);

      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            ItemStack stack = entity.getItemBySlot(slot);
            Item prop = stack.getItem();
            if (prop instanceof ICustomArmor) {
               ICustomArmor armor = (ICustomArmor)prop;
               if (armor.canBlockDamageSource(entity, stack, source, slot)) {
                  ICustomArmor.AbsorptionProperties propx = armor.getProperties(entity, stack, source, damage, slot).copy();
                  propx.slot = slot;
                  if (propx.absorbRatio > 0.0 && propx.absorbMax > 0) {
                     properties.enqueue(propx);
                     map.addTo(propx.priority, propx.absorbRatio);
                  }
               }
            }
         }
      }

      if (!properties.isEmpty()) {
         double startDamage = damage;

         while (!properties.isEmpty()) {
            ICustomArmor.AbsorptionProperties entry = (ICustomArmor.AbsorptionProperties)properties.dequeue();
            double percent = map.get(entry.priority);
            if (percent > 1.0) {
               entry.absorbRatio /= percent;
            }

            double damageAbsorbed = Math.min(damage, Math.min(startDamage * entry.absorbRatio, (double)entry.absorbMax));
            damage -= damageAbsorbed;
            if (damageAbsorbed > 0.0) {
               ItemStack stack = entity.getItemBySlot(entry.slot);
               int itemDamage = (int)Math.max(1.0, damageAbsorbed);
               if (stack.getItem() instanceof ICustomArmor) {
                  ((ICustomArmor)stack.getItem()).damageArmor(entity, stack, source, itemDamage, entry.slot, ICustomArmor.DamageType.MODDED);
               }
            }

            if (damage <= 0.0) {
               return 0.0F;
            }
         }
      }

      double totalArmor = (double)entity.getArmorValue();
      double totalToughness = entity.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue();
      if (damage > 0.0 && (totalArmor > 0.0 || totalToughness > 0.0) && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
         double armorDamage = Math.max(1.0, damage / 4.0);

         for (EquipmentSlot slotx : EquipmentSlot.values()) {
            if (slotx.getType() == EquipmentSlot.Type.ARMOR) {
               ItemStack stack = entity.getItemBySlot(slotx);
               if (stack.getItem() instanceof ICustomArmor) {
                  ((ICustomArmor)stack.getItem()).damageArmor(entity, stack, source, (int)armorDamage, slotx, ICustomArmor.DamageType.VANILLA);
               } else if (stack.getItem() instanceof ArmorItem) {
                  stack.hurtAndBreak((int)armorDamage, entity, IC2Item.get(slotx));
               }
            }
         }

         damage = (double)CombatRules.getDamageAfterAbsorb((float)damage, (float)totalArmor, (float)totalToughness);
      }

      return (float)damage;
   }
}
