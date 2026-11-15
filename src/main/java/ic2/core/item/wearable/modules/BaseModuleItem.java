package ic2.core.item.wearable.modules;

import ic2.api.core.IC2Classic;
import ic2.api.items.armor.IArmorModule;
import ic2.api.items.electric.ElectricItem;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.core.item.base.IC2SimpleItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.utils.helpers.capabilities.CapabilitySupplier;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class BaseModuleItem extends IC2SimpleItem implements IArmorModule {
   protected IArmorModule.ModuleType type;

   public BaseModuleItem(String itemName, PropertiesBuilder properties, String textureFolder, String textureName, IArmorModule.ModuleType type) {
      super(itemName, properties, textureFolder, textureName);
      this.type = type;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      this.handleToolTip(stack, helper::addSimpleToolTip);
   }

   @Override
   public IArmorModule.ModuleType getType(ItemStack stack) {
      return this.type;
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return true;
   }

   @Override
   public void onInstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
   }

   @Override
   public void transferToArmor(ItemStack stack, ItemStack oldArmor, ItemStack newArmor) {
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
   }

   @Override
   public void onEquipped(ItemStack stack, ItemStack armor, Player entity) {
   }

   @Override
   public void onUnequipped(ItemStack stack, ItemStack armor, Player entity) {
   }

   @Override
   public void provideCapabilities(ItemStack stack, ItemStack armor) {
   }

   @Override
   public boolean handlePacket(Player player, ItemStack module, ItemStack armor, String id, INetworkDataBuffer buffer, Dist targetSide) {
      return false;
   }

   protected EquipmentSlot findPlayerSlot(Player player, ItemStack send) {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (ItemStack.matches(player.getItemBySlot(slot), send)) {
            return slot;
         }
      }

      return null;
   }

   protected ItemStack findPlayerStack(Player player, ItemStack send) {
      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (ItemStack.matches(player.getItemBySlot(slot), send)) {
            return player.getItemBySlot(slot);
         }
      }

      return ItemStack.EMPTY;
   }

   public boolean canUseEnergy(ItemStack stack, int energy) {
      return ElectricItem.MANAGER.discharge(stack, energy, 2147483647, true, false, true) >= energy;
   }

   public boolean useEnergy(ItemStack stack, int energy, LivingEntity living) {
      return ElectricItem.MANAGER.discharge(stack, energy, 2147483647, true, false, false) >= energy;
   }

   public <T> void removeCapability(ItemStack armor, Capability<T> cap) {
      if (cap != null && !armor.isEmpty()) {
         IArmorModule.IArmorCapability storage = (IArmorModule.IArmorCapability)armor.getCapability(IC2Classic.ARMOR_CAPABILITY).orElse(null);
         if (storage != null) {
            storage.removeArmorCapability(cap);
         }
      }
   }

   public <T> void registerCapability(ItemStack armor, Capability<T> cap, T data) {
      if (cap != null && data != null && !armor.isEmpty()) {
         IArmorModule.IArmorCapability storage = (IArmorModule.IArmorCapability)armor.getCapability(IC2Classic.ARMOR_CAPABILITY).orElse(null);
         if (storage != null) {
            if (data instanceof ICapabilityProvider) {
               storage.registerArmorCapability(cap, (ICapabilityProvider)data);
            } else {
               storage.registerArmorCapability(cap, new CapabilitySupplier<>(cap, data));
            }
         }
      }
   }
}
