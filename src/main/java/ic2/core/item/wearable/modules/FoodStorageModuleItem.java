package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.food_and_drink.TinCanItem;
import ic2.core.item.inv.inventory.FoodStorageModuleInventory;
import ic2.core.item.wearable.base.IHasArmorInventory;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class FoodStorageModuleItem extends BaseModuleItem implements IHasArmorInventory {
   public static final String DATA = "food_storage";

   public FoodStorageModuleItem(String textureFolder, String textureName) {
      super("food_storage_module", new PropertiesBuilder().maxStackSize(1), textureFolder, textureName, IArmorModule.ModuleType.STORAGE);
   }

   @Override
   public void onInstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      CompoundTag nbt = armor.getOrCreateTagElement("food_storage");
      nbt.putByte("listener", (byte)(nbt.getByte("listener") + 1));
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.CHEST;
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      CompoundTag nbt = StackUtil.getNbtData(armor).getCompound("food_storage");
      byte value = (byte)(nbt.getByte("listener") - 1);
      if (value <= 0) {
         armor.removeTagKey("food_storage");
      } else {
         nbt.putByte("listener", value);
      }
   }

   @Override
   public void transferToArmor(ItemStack stack, ItemStack oldArmor, ItemStack newArmor) {
      CompoundTag nbt = StackUtil.getNbtData(oldArmor).getCompound("food_storage").copy();
      nbt.remove("listener");
      nbt.putByte("listener", StackUtil.getNbtData(newArmor).getCompound("food_storage").getByte("listener"));
      newArmor.addTagElement("food_storage", nbt);
   }

   @Override
   public boolean hasInventory(ItemStack stack) {
      return false;
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return new FoodStorageModuleInventory(player, this, stack, null).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return new FoodStorageModuleInventory(player, this, stack, slot).load(stack);
   }

   static {
      AutoFeedModuleItem.addFoodSource((t, u) -> {
         for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != Type.HAND) {
               CompoundTag nbt = StackUtil.getNbtData(t.getItemBySlot(slot));
               if (nbt.contains("food_storage")) {
                  return TransporterManager.getTransporter(new FoodStorageModuleItem.CustomHandler(nbt.getCompound("food_storage")));
               }
            }
         }

         return null;
      });
   }

   public static class CustomHandler extends ItemStackHandler {
      CompoundTag nbt;

      public CustomHandler(CompoundTag nbt) {
         super(4);
         this.nbt = nbt;
         NBTUtils.loadItems(nbt.getCompound("inventory"), this.stacks);
      }

      protected void onContentsChanged(int slot) {
         CompoundTag data = new CompoundTag();
         NBTUtils.saveItems(data, this.stacks);
         this.nbt.put("inventory", data);
      }

      public boolean isItemValid(int slot, ItemStack stack) {
         return stack.getItem() instanceof TinCanItem && (slot == 0 || slot == 1) || (slot == 2 || slot == 3) && stack.getItem() == IC2Items.TIN_CAN;
      }
   }
}
