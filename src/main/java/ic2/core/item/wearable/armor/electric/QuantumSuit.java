package ic2.core.item.wearable.armor.electric;

import ic2.api.events.ArmorSlotEvent;
import ic2.api.items.armor.IArmorModule;
import ic2.core.block.base.misc.color.IItemColorListener;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.wearable.base.IC2ModularElectricArmor;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.features.item.IColoredItemModel;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.MinecraftForge;

public class QuantumSuit extends IC2ModularElectricArmor implements DyeableLeatherItem, IItemColorListener, IColoredItemModel {
   public QuantumSuit(String itemName, EquipmentSlot slot) {
      super(itemName, slot, new PropertiesBuilder().rarity(Rarity.RARE));
      this.addSlotType(IArmorModule.ModuleType.BATTERY, 1);
      this.addSlotType(IArmorModule.ModuleType.CHARGER, 1);
      switch (slot) {
         case CHEST:
            this.addSlotType(IArmorModule.ModuleType.GENERIC, 1);
            this.addSlotType(IArmorModule.ModuleType.STORAGE, 2);
            this.addSlotType(IArmorModule.ModuleType.BACK_SLOT, 1);
            break;
         case FEET:
            this.addSlotType(IArmorModule.ModuleType.GENERIC, 1);
            this.addSlotType(IArmorModule.ModuleType.MOVEMENT, 2);
            break;
         case HEAD:
            this.addSlotType(IArmorModule.ModuleType.GENERIC, 3);
            break;
         case LEGS:
            this.addSlotType(IArmorModule.ModuleType.GENERIC, 1);
            this.addSlotType(IArmorModule.ModuleType.STORAGE, 2);
            this.addSlotType(IArmorModule.ModuleType.MOVEMENT, 1);
      }

      MinecraftForge.EVENT_BUS.post(new ArmorSlotEvent(this, "Quantum Suit", slot, this.types));
      CauldronInteraction.WATER.put(this, CauldronInteraction.DYED_ITEM);
   }

   @Override
   public ItemStack createDefaultArmor() {
      ItemStack stack = new ItemStack(this);
      Map<IArmorModule.ModuleType, List<ItemStack>> types = CollectionUtils.<IArmorModule.ModuleType, List<ItemStack>>createLinkedMap();
      types.put(IArmorModule.ModuleType.BATTERY, ObjectArrayList.wrap(new ItemStack[]{new ItemStack(IC2Items.LAPOTRON_CRYSTAL)}));
      switch (this.getEquipmentSlot()) {
         case CHEST:
            types.put(IArmorModule.ModuleType.GENERIC, ObjectArrayList.wrap(new ItemStack[]{new ItemStack(IC2Items.EXTINGUISHING_MODULE)}));
            break;
         case FEET:
            types.put(IArmorModule.ModuleType.MOVEMENT, ObjectArrayList.wrap(new ItemStack[]{new ItemStack(IC2Items.JUMP_BOOST_MODULE)}));
            break;
         case HEAD:
            types.put(
               IArmorModule.ModuleType.GENERIC,
               ObjectArrayList.wrap(
                  new ItemStack[]{
                     new ItemStack(IC2Items.AUTO_FEED_MODULE), new ItemStack(IC2Items.AIR_REFILL_MODULE), new ItemStack(IC2Items.PROTECTION_MODULE)
                  }
               )
            );
            break;
         case LEGS:
            types.put(IArmorModule.ModuleType.MOVEMENT, ObjectArrayList.wrap(new ItemStack[]{new ItemStack(IC2Items.SPEED_MODULE)}));
      }

      setAndInstallTypes(stack, types);
      return stack;
   }

   @Override
   public double getDamageAbsorptionRatio(ItemStack stack) {
      return this.getEquipmentSlot() == EquipmentSlot.FEET ? 1.1 : 1.0;
   }

   @Override
   public int getEnergyPerDamage(ItemStack stack) {
      return 900;
   }

   @Override
   public boolean isFullyAbsorbingFallDamage(ItemStack stack) {
      return true;
   }

   @Override
   public String getTextureFolder() {
      return "armor/quantum";
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
      return "ic2:textures/models/armor/quantum";
   }

   @Override
   public int getItemColor(ItemStack stack, int tintIndex) {
      return tintIndex == 1 && this.hasCustomColor(stack) ? DyeableLeatherItem.super.getColor(stack) : -1;
   }

   public int getColor(ItemStack stack) {
      return this.getItemColor(stack, 1);
   }

   @Override
   public int getTintedIndexForModel(ItemStack stack, int layer) {
      return 1;
   }
}
