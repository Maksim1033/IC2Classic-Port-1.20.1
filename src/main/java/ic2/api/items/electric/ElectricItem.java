package ic2.api.items.electric;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.items.IItemHandler;

public final class ElectricItem {
   static final Map<Item, IElectricItemManager> BACKUPS = new Object2ObjectOpenHashMap();
   static Function<Player, IItemHandler> CURIO_ACCESSOR;
   public static IElectricItemManager MANAGER;
   public static IElectricItemManager DIRECT_MANAGER;

   public static void registerBackupManager(IElectricItemManager manager, ItemLike... items) {
      for (ItemLike prov : items) {
         BACKUPS.put(prov.asItem(), manager);
      }
   }

   public static void setCurioSupport(Function<Player, IItemHandler> curio) {
      CURIO_ACCESSOR = curio;
   }

   public static IElectricItemManager getBackupManager(Item item) {
      return BACKUPS.get(item);
   }

   public static int chargeArmor(Player player, int provided) {
      return chargeArmor(player, provided, true, EquipmentSlot.values());
   }

   public static int chargeArmor(Player player, int provided, boolean whitelist, EquipmentSlot... slots) {
      return chargeArmor(player, provided, whitelist, true, slots);
   }

   public static int chargeArmor(Player player, int provided, boolean whitelist, boolean allowCurio, EquipmentSlot... slots) {
      if (whitelist) {
         for (EquipmentSlot slot : slots) {
            if (provided <= 0) {
               break;
            }

            if (slot.getType() != Type.HAND) {
               provided -= MANAGER.charge(player.getItemBySlot(slot), provided, 2147483647, false, false);
            }
         }

         if (provided > 0 && allowCurio && CURIO_ACCESSOR != null) {
            provided -= chargeCurio(CURIO_ACCESSOR.apply(player), provided);
         }

         return provided;
      } else {
         Set<EquipmentSlot> notAllowed = EnumSet.copyOf(ObjectArrayList.wrap(slots));

         for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (provided <= 0) {
               break;
            }

            if (slot.getType() != Type.HAND && !notAllowed.contains(slot)) {
               provided -= MANAGER.charge(player.getItemBySlot(slot), provided, 2147483647, false, false);
            }
         }

         if (provided > 0 && allowCurio && CURIO_ACCESSOR != null) {
            provided -= chargeCurio(CURIO_ACCESSOR.apply(player), provided);
         }

         return provided;
      }
   }

   public static int chargeCurio(IItemHandler handler, int energyProvided) {
      if (handler == null) {
         return 0;
      } else {
         int used = 0;
         int i = 0;

         for (int m = handler.getSlots(); i < m && used < energyProvided; i++) {
            used += MANAGER.charge(handler.getStackInSlot(i), energyProvided - used, 2147483647, false, false);
         }

         return used;
      }
   }

   public static int applyEnchantmentEffect(ItemStack item, int amount) {
      amount *= (int)(1.0 + 0.15 * (double)EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, item));
      return amount * (int)(1.0 - 0.1 * (double)EnchantmentHelper.getTagEnchantmentLevel(Enchantments.UNBREAKING, item));
   }
}
