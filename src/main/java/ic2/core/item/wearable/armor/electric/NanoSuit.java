package ic2.core.item.wearable.armor.electric;

import ic2.api.events.ArmorSlotEvent;
import ic2.api.items.armor.IArmorModule;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.wearable.base.IC2ModularElectricArmor;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.MinecraftForge;

public class NanoSuit extends IC2ModularElectricArmor {
   public static final String UPGRADED = "upgraded";
   protected Object2IntMap<IArmorModule.ModuleType> alternative = new Object2IntOpenHashMap();

   public NanoSuit(String itemName, EquipmentSlot slot) {
      super(itemName, slot, new PropertiesBuilder().rarity(Rarity.UNCOMMON));
      this.addSlotType(IArmorModule.ModuleType.BATTERY, 1);
      this.addSlotType(IArmorModule.ModuleType.CHARGER, 1);
      switch (slot) {
         case CHEST:
            this.addSlotType(IArmorModule.ModuleType.STORAGE, 1);
            this.addSlotType(IArmorModule.ModuleType.BACK_SLOT, 1);
            break;
         case FEET:
            this.addSlotType(IArmorModule.ModuleType.MOVEMENT, 2);
            break;
         case HEAD:
            this.addSlotType(IArmorModule.ModuleType.GENERIC, 2);
            break;
         case LEGS:
            this.addSlotType(IArmorModule.ModuleType.STORAGE, 1);
            this.addSlotType(IArmorModule.ModuleType.MOVEMENT, 1);
      }

      if (slot == EquipmentSlot.HEAD) {
         this.alternative.put(IArmorModule.ModuleType.HUD, 9);
      }

      this.alternative.mergeInt(IArmorModule.ModuleType.BATTERY, 1, Integer::sum);
      this.alternative.mergeInt(IArmorModule.ModuleType.CHARGER, 1, Integer::sum);
      switch (slot) {
         case CHEST:
            this.alternative.mergeInt(IArmorModule.ModuleType.GENERIC, 1, Integer::sum);
            this.alternative.mergeInt(IArmorModule.ModuleType.STORAGE, 2, Integer::sum);
            this.alternative.mergeInt(IArmorModule.ModuleType.BACK_SLOT, 1, Integer::sum);
            break;
         case FEET:
            this.alternative.mergeInt(IArmorModule.ModuleType.GENERIC, 1, Integer::sum);
            this.alternative.mergeInt(IArmorModule.ModuleType.MOVEMENT, 2, Integer::sum);
            break;
         case HEAD:
            this.alternative.mergeInt(IArmorModule.ModuleType.GENERIC, 3, Integer::sum);
            break;
         case LEGS:
            this.alternative.mergeInt(IArmorModule.ModuleType.GENERIC, 1, Integer::sum);
            this.alternative.mergeInt(IArmorModule.ModuleType.STORAGE, 2, Integer::sum);
            this.alternative.mergeInt(IArmorModule.ModuleType.MOVEMENT, 1, Integer::sum);
      }

      MinecraftForge.EVENT_BUS.post(new ArmorSlotEvent(this, "Nano Suit", slot, this.types));
      MinecraftForge.EVENT_BUS.post(new ArmorSlotEvent(this, "Nano Suit Alternative", slot, this.alternative));
   }

   @Override
   public ItemStack createDefaultArmor() {
      ItemStack stack = new ItemStack(this);
      Map<IArmorModule.ModuleType, List<ItemStack>> types = CollectionUtils.<IArmorModule.ModuleType, List<ItemStack>>createLinkedMap();
      types.put(IArmorModule.ModuleType.BATTERY, ObjectArrayList.wrap(new ItemStack[]{new ItemStack(IC2Items.ENERGY_CRYSTAL)}));
      setAndInstallTypes(stack, types);
      return stack;
   }

   @Override
   public Object2IntMap<IArmorModule.ModuleType> getModuleLimits(ItemStack stack) {
      return StackUtil.getNbtData(stack).getBoolean("upgraded") ? Object2IntMaps.unmodifiable(this.alternative) : Object2IntMaps.unmodifiable(this.types);
   }

   @Override
   public double getDamageAbsorptionRatio(ItemStack stack) {
      return 0.9;
   }

   @Override
   public int getEnergyPerDamage(ItemStack stack) {
      return 800;
   }

   @Override
   public boolean isFullyAbsorbingFallDamage(ItemStack stack) {
      return StackUtil.getNbtData(stack).getBoolean("upgraded");
   }

   @Override
   public String getTextureFolder() {
      return "armor/nano";
   }

   @Override
   public String getTextureName() {
      switch (this.getEquipmentSlot()) {
         case CHEST:
            return "bodyarmor";
         case FEET:
            return "boots";
         case HEAD:
            return "helmet";
         case LEGS:
            return "leggings";
         default:
            return "";
      }
   }

   @Override
   public String getArmorTexture() {
      return "ic2:textures/models/armor/nano";
   }
}
