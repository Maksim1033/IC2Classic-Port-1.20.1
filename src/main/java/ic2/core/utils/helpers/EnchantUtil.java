package ic2.core.utils.helpers;

import com.google.common.math.DoubleMath;
import ic2.api.items.electric.IDamagelessElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.core.IC2;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.ForgeRegistries;

public class EnchantUtil {
   public static int getEnchantmentCount(ItemStack stack) {
      return (stack.getItem() == Items.ENCHANTED_BOOK ? EnchantedBookItem.getEnchantments(stack) : stack.getEnchantmentTags()).size()
         + stack.getBaseRepairCost();
   }

   public static List<EnchantmentInstance> createElectricEnchantments(int level, ItemStack stack, boolean treasure, RandomSource rand) {
      if (!stack.isEmpty() && level >= 0) {
         int i = stack.getItem() == Items.ENCHANTED_BOOK ? 1 : stack.getEnchantmentValue();
         if (i <= 0) {
            return ObjectLists.emptyList();
         } else {
            level = level + 1 + rand.nextInt(i / 4 + 1) + rand.nextInt(i / 4 + 1);
            float f = (rand.nextFloat() + rand.nextFloat() - 1.0F) * 0.15F;
            level = Mth.clamp(Math.round((float)level + (float)level * f), 1, 2147483647);
            return getElectricEnchantmentData(level, stack, treasure);
         }
      } else {
         return ObjectLists.emptyList();
      }
   }

   public static List<EnchantmentInstance> getElectricEnchantmentData(int level, ItemStack stack, boolean allowTreasure) {
      List<EnchantmentInstance> result = CollectionUtils.<EnchantmentInstance>createList();
      boolean book = stack.getItem() == Items.BOOK || stack.getItem() == Items.ENCHANTED_BOOK;
      EnchantmentCategory type = stack.getItem() instanceof IElectricEnchantable ? ((IElectricEnchantable)stack.getItem()).getEnchantmentType(stack) : null;

      for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
         if ((!enchantment.isTreasureOnly() || allowTreasure)
            && enchantment.isDiscoverable()
            && !enchantment.isCurse()
            && (isValidElectricEnchantment(stack, enchantment, type, false) || book && enchantment.isAllowedOnBooks())) {
            for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; i--) {
               if (level >= enchantment.getMinCost(i) && level <= enchantment.getMaxCost(i)) {
                  result.add(new EnchantmentInstance(enchantment, i));
                  break;
               }
            }
         }
      }

      return result;
   }

   public static boolean isValidElectricEnchantment(ItemStack stack, Enchantment enchantment, boolean anvil) {
      return isValidElectricEnchantment(
         stack, enchantment, stack.getItem() instanceof IElectricEnchantable ? ((IElectricEnchantable)stack.getItem()).getEnchantmentType(stack) : null, anvil
      );
   }

   public static boolean isValidElectricEnchantment(ItemStack stack, Enchantment enchantment, EnchantmentCategory type, boolean anvil) {
      if (type != null && stack.getItem() instanceof IElectricEnchantable tool) {
         switch (tool.getEnchantmentCompatibility(stack, enchantment)) {
            case SUCCESS:
               return true;
            case PASS:
               return type == EnchantmentCategory.BREAKABLE && (stack.isDamageableItem() || stack.getItem() instanceof IDamagelessElectricItem)
                  || (ignoreCanApplyAtEnchantingTable(enchantment) || enchantment.canApplyAtEnchantingTable(stack))
                     && (
                        enchantment.category == type
                           || (
                                 type == EnchantmentCategory.ARMOR_FEET
                                    || type == EnchantmentCategory.ARMOR_CHEST
                                    || type == EnchantmentCategory.ARMOR_HEAD
                                    || type == EnchantmentCategory.ARMOR_LEGS
                              )
                              && enchantment.category == EnchantmentCategory.ARMOR
                     );
            default:
               return false;
         }
      } else {
         return anvil ? enchantment.canEnchant(stack) : enchantment.canApplyAtEnchantingTable(stack);
      }
   }

   private static boolean ignoreCanApplyAtEnchantingTable(Enchantment enchant) {
      ResourceLocation location = ForgeRegistryWrapper.getEnchantmentKey(enchant);
      return location.getNamespace().equals("minecraft")
         || IC2.CONFIG.ignoreEnchantabilityCheck.contains(location.getNamespace())
         || IC2.CONFIG.ignoreEnchantabilityCheck.contains(location.toString());
   }

   public static void mergeEnchantments(Object2IntMap<Enchantment> enchantments, List<EnchantmentInstance> data, ItemStack stack, boolean anvil) {
      EnchantmentCategory type = stack.getItem() instanceof IElectricEnchantable ? ((IElectricEnchantable)stack.getItem()).getEnchantmentType(stack) : null;
      boolean book = stack.getItem() == Items.BOOK || stack.getItem() == Items.ENCHANTED_BOOK;
      int i = 0;

      for (int m = data.size(); i < m; i++) {
         EnchantmentInstance entry = data.get(i);
         if (isValidElectricEnchantment(stack, entry.enchantment, type, anvil) || book && entry.enchantment.isAllowedOnBooks()) {
            int oldLvl = enchantments.getInt(entry.enchantment);
            if (oldLvl == 0) {
               if (book || canInsertEnchantment(entry.enchantment, enchantments.keySet())) {
                  enchantments.put(entry.enchantment, entry.level);
               }
            } else if (oldLvl < entry.level) {
               enchantments.put(entry.enchantment, entry.level);
            } else if (oldLvl == entry.level && entry.enchantment.getMaxLevel() > oldLvl) {
               enchantments.put(entry.enchantment, entry.level + 1);
            }
         }
      }
   }

   public static boolean canInsertEnchantment(EnchantmentInstance enchantmentInstance, List<EnchantmentInstance> list) {
      int i = 0;

      for (int m = list.size(); i < m; i++) {
         Enchantment entry = list.get(i).enchantment;
         if (enchantmentInstance.enchantment != entry && !enchantmentInstance.enchantment.isCompatibleWith(entry)) {
            return false;
         }
      }

      return true;
   }

   public static boolean canInsertEnchantment(Enchantment enchantment, Set<Enchantment> set) {
      for (Enchantment entry : set) {
         if (entry != enchantment && !entry.isCompatibleWith(enchantment)) {
            return false;
         }
      }

      return true;
   }

   public static int drainExperience(Player player, int points) {
      if (player.isCreative()) {
         player.onEnchantmentPerformed(ItemStack.EMPTY, 0);
         return points;
      } else {
         int totalXP = getXP(player);
         if (totalXP != player.totalExperience) {
            player.totalExperience = totalXP;
         }

         int change = Math.min(getXP(player), points);
         player.totalExperience -= change;
         player.experienceLevel = getLvlForXP(player.totalExperience);
         player.experienceProgress = (float)(player.totalExperience - getXPForLvl(player.experienceLevel)) / (float)player.getXpNeededForNextLevel();
         player.onEnchantmentPerformed(ItemStack.EMPTY, 0);
         return change;
      }
   }

   public static int getXP(Player player) {
      return getXPForLvl(player.experienceLevel)
         + DoubleMath.roundToInt((double)(player.experienceProgress * (float)player.getXpNeededForNextLevel()), RoundingMode.HALF_UP);
   }

   public static int getXPForLvl(int level) {
      if (level < 0) {
         return 2147483647;
      } else if (level <= 15) {
         return level * level + 6 * level;
      } else {
         return level <= 30
            ? (int)((double)(level * level) * 2.5 - 40.5 * (double)level + 360.0)
            : (int)((double)(level * level) * 4.5 - 162.5 * (double)level + 2220.0);
      }
   }

   public static int getLvlForXP(int totalXP) {
      int result = 0;

      while (getXPForLvl(result) <= totalXP) {
         result++;
      }

      return result - 1;
   }
}
