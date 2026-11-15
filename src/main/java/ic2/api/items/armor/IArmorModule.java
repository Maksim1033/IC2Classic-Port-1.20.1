package ic2.api.items.armor;

import ic2.api.network.buffer.INetworkDataBuffer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public interface IArmorModule {
   IArmorModule.ModuleType getType(ItemStack var1);

   boolean canInstallInArmor(ItemStack var1, ItemStack var2, EquipmentSlot var3);

   void onInstall(ItemStack var1, ItemStack var2, IArmorModule.IArmorModuleHolder var3);

   void onUninstall(ItemStack var1, ItemStack var2, IArmorModule.IArmorModuleHolder var3);

   void transferToArmor(ItemStack var1, ItemStack var2, ItemStack var3);

   void onTick(ItemStack var1, ItemStack var2, Level var3, Player var4);

   void onEquipped(ItemStack var1, ItemStack var2, Player var3);

   void onUnequipped(ItemStack var1, ItemStack var2, Player var3);

   void provideCapabilities(ItemStack var1, ItemStack var2);

   boolean handlePacket(Player var1, ItemStack var2, ItemStack var3, String var4, INetworkDataBuffer var5, Dist var6);

   default void handleToolTip(ItemStack stack, Consumer<MutableComponent> results) {
      results.accept(Component.translatable("misc.ic2.module.slot_type", new Object[]{this.getType(stack).getName()}));
      MutableComponent adding = Component.literal("[");
      Component next = null;
      boolean first = true;

      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (slot.getType() == Type.ARMOR && this.canInstallInArmor(stack, stack, slot)) {
            if (next != null) {
               if (!first) {
                  adding.append(", ");
               }

               adding.append(next);
               first = false;
            }

            next = Component.translatable("misc.ic2.module.valid_armor." + slot.getName());
         }
      }

      if (next != null) {
         if (!first) {
            adding.append(", ");
         }

         results.accept(Component.translatable("misc.ic2.module.valid_armor", new Object[]{adding.append(next).append("]")}));
      }
   }

   public static class ArmorMod {
      public static final IArmorModule.ArmorMod ENERGY_STORAGE = new IArmorModule.ArmorMod("storage_add", "storage_mul");
      public static final IArmorModule.ArmorMod ENERGY_TIER = new IArmorModule.ArmorMod("tier");
      public static final IArmorModule.ArmorMod ENERGY_TRANSFER = new IArmorModule.ArmorMod("transfer_add", "transfer_mul");
      public static final IArmorModule.ArmorMod ENERGY_PROVIDER = new IArmorModule.ArmorMod("provider");
      public static final IArmorModule.ArmorMod NO_LAVA_DAMAGE = new IArmorModule.ArmorMod("lava");
      public static final IArmorModule.ArmorMod ENERGY_SHIELD = new IArmorModule.ArmorMod("shield");
      public static final IArmorModule.ArmorMod ENERGY_SHIELD_ALWAYS = new IArmorModule.ArmorMod("shield_always");
      public static final IArmorModule.ArmorMod EU_READER = new IArmorModule.ArmorMod("eu_reader");
      public static final IArmorModule.ArmorMod THERMOMETER = new IArmorModule.ArmorMod("thermo");
      public static final IArmorModule.ArmorMod HAZMAT_PROTECTION = new IArmorModule.ArmorMod("hazmat");
      public static final IArmorModule.ArmorMod CROP_SCANNER = new IArmorModule.ArmorMod("crop");
      public static final IArmorModule.ArmorMod HUD_ENERGY = new IArmorModule.ArmorMod("armor_hud");
      public static final IArmorModule.ArmorMod GOLDEN = new IArmorModule.ArmorMod("golden");
      String add_mod;
      String mul_mod;

      public ArmorMod(String add_mod) {
         this(add_mod, null);
      }

      public ArmorMod(String add_mod, String mul_mod) {
         this.add_mod = add_mod;
         this.mul_mod = mul_mod;
      }

      public String getAdd() {
         return this.add_mod;
      }

      public String getMul() {
         return this.mul_mod;
      }
   }

   public interface IArmorCapability {
      <T> void registerArmorCapability(Capability<T> var1, ICapabilityProvider var2);

      <T> ICapabilityProvider removeArmorCapability(Capability<T> var1);

      <T> LazyOptional<T> getArmorCapability(Capability<T> var1);
   }

   public interface IArmorModuleHolder {
      String DISABLE_MODULES = "no_modules";

      List<ItemStack> getInstalledModules(ItemStack var1);

      Object2IntMap<IArmorModule.ModuleType> getModuleLimits(ItemStack var1);

      void addAddModifier(ItemStack var1, IArmorModule.ArmorMod var2, int var3);

      void addMulModifier(ItemStack var1, IArmorModule.ArmorMod var2, float var3);

      void removeAddModifier(ItemStack var1, IArmorModule.ArmorMod var2, int var3);

      void removeMulModifier(ItemStack var1, IArmorModule.ArmorMod var2, float var3);

      void onEquipmentStateChanged(ItemStack var1, boolean var2, Player var3);

      static boolean isValidModularArmor(ItemStack stack) {
         if (!(stack.getItem() instanceof IArmorModule.IArmorModuleHolder)) {
            return false;
         } else {
            CompoundTag tag = stack.getTag();
            return tag == null || !tag.getBoolean("no_modules");
         }
      }
   }

   public static final class ModuleType implements Comparable<IArmorModule.ModuleType> {
      private static final Map<String, IArmorModule.ModuleType> TYPES = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap());
      private static final AtomicInteger IDS = new AtomicInteger(1);
      public static final IArmorModule.ModuleType BATTERY = getOrCreateType("battery", "misc.ic2.module.battery");
      public static final IArmorModule.ModuleType HUD = getOrCreateType("hud", "misc.ic2.module.hud");
      public static final IArmorModule.ModuleType ANY = getOrCreateType("any", "misc.ic2.module.any");
      public static final IArmorModule.ModuleType GENERIC = getOrCreateType("generic", "misc.ic2.module.generic");
      public static final IArmorModule.ModuleType CHARGER = getOrCreateType("charge", "misc.ic2.module.charger");
      public static final IArmorModule.ModuleType STORAGE = getOrCreateType("storage", "misc.ic2.module.storage");
      public static final IArmorModule.ModuleType BACK_SLOT = getOrCreateType("back_slot", "misc.ic2.module.back_slot");
      public static final IArmorModule.ModuleType MOVEMENT = getOrCreateType("movement", "misc.ic2.module.movement");
      final int index = IDS.getAndIncrement();
      final String id;
      final String name;

      ModuleType(String id, String name) {
         this.id = id;
         this.name = name;
      }

      public Component getName() {
         return Component.translatable(this.name);
      }

      public String getId() {
         return this.id;
      }

      public static IArmorModule.ModuleType getType(String id) {
         return TYPES.get(id);
      }

      public static synchronized IArmorModule.ModuleType getOrCreateType(String id, String name) {
         IArmorModule.ModuleType type = TYPES.get(id);
         if (type == null) {
            type = new IArmorModule.ModuleType(id, name);
            TYPES.put(id, type);
         }

         return type;
      }

      public static List<IArmorModule.ModuleType> getAllTypes() {
         return new ObjectArrayList(TYPES.values());
      }

      public int compareTo(IArmorModule.ModuleType o) {
         return Integer.compareUnsigned(this.index, o.index);
      }
   }
}
