package ic2.core.item.manager;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricItem;
import ic2.api.items.electric.IElectricItemManager;
import ic2.core.IC2;
import ic2.core.utils.helpers.Formatters;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

public class ElectricItemManager implements IElectricItemManager {
   @Override
   public int charge(ItemStack stack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
      IElectricItem item = (IElectricItem)stack.getItem();
      if (amount >= 0 && stack.getCount() <= 1 && item.getTier(stack) <= tier) {
         if (!ignoreTransferLimit) {
            amount = Math.min(amount, item.getTransferLimit(stack));
         }

         CompoundTag nbt = stack.getOrCreateTag();
         int charge = nbt.getInt("charge");
         amount = Math.min(amount, item.getCapacity(stack) - charge);
         if (!simulate) {
            charge += amount;
            if (charge > 0) {
               nbt.putInt("charge", charge);
            } else {
               nbt.remove("charge");
            }

            stack.setDamageValue(this.calculateDamage(stack, charge));
         }

         return amount;
      } else {
         return 0;
      }
   }

   @Override
   public int discharge(ItemStack stack, int amount, int tier, boolean ignoreTransferLimit, boolean externalUse, boolean simulate) {
      if (stack.hasTag() && amount > 0 && stack.getCount() <= 1) {
         IElectricItem item = (IElectricItem)stack.getItem();
         if (item.getTier(stack) <= tier && (!externalUse || item.canProvideEnergy(stack))) {
            if (!ignoreTransferLimit) {
               amount = Math.min(amount, item.getTransferLimit(stack));
            }

            CompoundTag nbt = stack.getOrCreateTag();
            int charge = nbt.getInt("charge");
            if (amount > charge) {
               amount = charge;
            }

            if (!simulate) {
               charge -= amount;
               if (charge > 0) {
                  nbt.putInt("charge", charge);
               } else {
                  nbt.remove("charge");
               }

               stack.setDamageValue(this.calculateDamage(stack, charge));
            }

            return amount;
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   @Override
   public int getCharge(ItemStack stack) {
      return this.discharge(stack, 2147483647, 2147483647, true, false, true);
   }

   @Override
   public boolean canUse(ItemStack stack, int amount) {
      return this.getCharge(stack) >= this.applyEnchantmentEffect(stack, amount);
   }

   @Override
   public boolean use(ItemStack stack, int amount, LivingEntity entity) {
      amount = this.applyEnchantmentEffect(stack, amount);
      this.chargeFromArmor(stack, entity);
      int transfer = this.discharge(stack, amount, 2147483647, true, false, true);
      if (transfer == amount) {
         this.discharge(stack, amount, 2147483647, true, false, false);
         this.chargeFromArmor(stack, entity);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void chargeFromArmor(ItemStack stack, LivingEntity entity) {
      if (entity != null) {
         boolean inventoryChanged = false;
         int needed = this.charge(stack, 2147483647, 2147483647, true, true);
         if (needed > 0) {
            if (entity instanceof Player && IC2.CURIO_PLUGIN != null) {
               int added = IC2.CURIO_PLUGIN.chargeFromArmor((Player)entity, stack, needed);
               needed -= added;
               inventoryChanged = added > 0;
            }

            if (needed > 0) {
               for (EquipmentSlot slot : EquipmentSlot.values()) {
                  if (needed <= 0) {
                     break;
                  }

                  if (slot.getType() != Type.HAND) {
                     int provided = ElectricItem.MANAGER.discharge(entity.getItemBySlot(slot), needed, 2147483647, true, true, false);
                     if (provided > 0) {
                        this.charge(stack, provided, 2147483647, true, false);
                        inventoryChanged = true;
                        needed -= provided;
                     }
                  }
               }
            }

            if (inventoryChanged && entity instanceof Player && IC2.PLATFORM.isSimulating()) {
               ((Player)entity).containerMenu.broadcastChanges();
            }
         }
      }
   }

   public int applyEnchantmentEffect(ItemStack item, int amount) {
      amount = (int)((double)amount * (1.0 + 0.15 * (double)item.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY)));
      return (int)((double)amount * (1.0 - 0.1 * (double)item.getEnchantmentLevel(Enchantments.UNBREAKING)));
   }

   @Override
   public int getCapacity(ItemStack stack) {
      return ((IElectricItem)stack.getItem()).getCapacity(stack);
   }

   @Override
   public int getTier(ItemStack stack) {
      return ((IElectricItem)stack.getItem()).getTier(stack);
   }

   @Override
   public int getTransferLimit(ItemStack stack) {
      return ((IElectricItem)stack.getItem()).getTransferLimit(stack);
   }

   private int calculateDamage(ItemStack stack, int newCharge) {
      return !stack.isEmpty() && stack.getMaxDamage() >= 2
         ? (int)((double)newCharge / (double)this.getCapacity(stack) * (double)(stack.getMaxDamage() - 1))
         : 0;
   }

   @Override
   public Component getToolTip(ItemStack stack) {
      return Component.translatable(
            "misc.ic2.eu_data",
            new Object[]{Formatters.EU_FORMAT.format((long)this.getCharge(stack)), Formatters.EU_FORMAT.format((long)this.getCapacity(stack))}
         )
         .withStyle(ChatFormatting.AQUA);
   }
}
