package ic2.core.block.machines.logic.armor;

import ic2.api.items.armor.IArmorModule;
import ic2.core.block.machines.tiles.nv.ArmorConfiguratorTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.item.wearable.base.IC2ModularElectricArmor;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public class ArmorInventory {
   public static final IHasInventory EMPTY = new OverflowInventory(0);
   protected ArmorConfiguratorTileEntity owner;
   protected SimpleInventory armor = new SimpleInventory(1);
   protected Map<IArmorModule.ModuleType, SimpleInventory> modules = CollectionUtils.<IArmorModule.ModuleType, SimpleInventory>createLinkedMap();
   protected List<IArmorModule.ModuleType> activeTypes = CollectionUtils.<IArmorModule.ModuleType>createList();
   protected Object2IntMap<IArmorModule.ModuleType> invSizes = new Object2IntOpenHashMap();

   public ArmorInventory(ArmorConfiguratorTileEntity owner) {
      this.owner = owner;

      for (IArmorModule.ModuleType type : IArmorModule.ModuleType.getAllTypes()) {
         this.modules.put(type, new OverflowInventory(9));
      }
   }

   public IHasInventory getArmor() {
      return this.armor;
   }

   public int getTypes() {
      return this.activeTypes.size();
   }

   public IHasInventory getSlotInventory(int index) {
      index += this.owner.offset;
      return index >= 0 && index < this.activeTypes.size() ? this.modules.get(this.activeTypes.get(index)) : EMPTY;
   }

   public int getSize(int index) {
      index += this.owner.offset;
      return index >= 0 && index < this.activeTypes.size() ? this.invSizes.getInt(this.activeTypes.get(index)) : 0;
   }

   public IArmorModule.ModuleType getType(int index) {
      index += this.owner.offset;
      return index >= 0 && index < this.activeTypes.size() ? this.activeTypes.get(index) : null;
   }

   public CompoundTag writeToNBT(CompoundTag nbt) {
      nbt.put("armor", this.armor.save(new CompoundTag()));
      return nbt;
   }

   public void readFromNBT(CompoundTag nbt) {
      this.armor.load(nbt.getCompound("armor"));
      this.onSlotSet();
   }

   public void onSlotSet() {
      this.reset();
      ItemStack armorStack = this.armor.getStackInSlot(0);
      if (armorStack.getItem() instanceof IArmorModule.IArmorModuleHolder holder) {
         ObjectIterator var11 = holder.getModuleLimits(armorStack).object2IntEntrySet().iterator();

         while (var11.hasNext()) {
            Entry<IArmorModule.ModuleType> entry = (Entry<IArmorModule.ModuleType>)var11.next();
            this.activeTypes.add((IArmorModule.ModuleType)entry.getKey());
            this.invSizes.put((IArmorModule.ModuleType)entry.getKey(), entry.getIntValue());
         }

         var11 = IC2ModularElectricArmor.getModuleTypes(armorStack).object2ObjectEntrySet().iterator();

         while (var11.hasNext()) {
            it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<IArmorModule.ModuleType, List<ItemStack>> entry = (it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<IArmorModule.ModuleType, List<ItemStack>>)var11.next();
            List<ItemStack> list = (List<ItemStack>)entry.getValue();
            IHasInventory inv = this.modules.get(entry.getKey());
            if (inv != null) {
               int size = Math.min(this.invSizes.getInt(entry.getKey()), 9);
               int i = 0;

               for (int m = Math.min(list.size(), size); i < m; i++) {
                  ItemStack subStack = list.get(i);
                  inv.setStackInSlot(i, subStack);
               }
            }
         }
      }

      this.activeTypes.sort(null);
      this.owner.syncArmor();
   }

   public void reset() {
      this.activeTypes.clear();
      this.invSizes.clear();

      for (SimpleInventory inv : this.modules.values()) {
         inv.clear();
      }

      this.owner.syncArmor();
   }

   public void save() {
      if (!this.armor.getStackInSlot(0).isEmpty()) {
         Map<IArmorModule.ModuleType, List<ItemStack>> maps = CollectionUtils.<IArmorModule.ModuleType, List<ItemStack>>createLinkedMap();
         int i = 0;

         for (int m = this.activeTypes.size(); i < m; i++) {
            SimpleInventory inv = this.modules.get(this.activeTypes.get(i));
            if (inv != null) {
               List<ItemStack> list = CollectionUtils.<ItemStack>createList();
               int j = 0;

               for (int n = inv.getSlotCount(); j < n; j++) {
                  ItemStack stack = inv.getStackInSlot(j);
                  if (!stack.isEmpty()) {
                     list.add(stack);
                  }
               }

               maps.put(this.activeTypes.get(i), list);
            }
         }

         IC2ModularElectricArmor.setModuleTypes(this.armor.getStackInSlot(0), maps);
         this.owner.syncArmor();
      }
   }

   public boolean canInsert(ItemStack stack, int index) {
      ItemStack armorSlot = this.armor.getStackInSlot(0);
      if (!(stack.getItem() instanceof IArmorModule module) || !(armorSlot.getItem() instanceof IArmorModule.IArmorModuleHolder)) {
         return false;
      }

      if (!module.canInstallInArmor(stack, armorSlot, Mob.getEquipmentSlotForItem(armorSlot))) {
         return false;
      } else {
         IArmorModule.ModuleType type = module.getType(stack);
         IArmorModule.ModuleType other = this.getType(index);
         return type == IArmorModule.ModuleType.ANY && other != IArmorModule.ModuleType.HUD || type == other;
      }
   }

   public void onInserted(ItemStack stack) {
      ItemStack armorStack = this.armor.getStackInSlot(0);
      if (stack.getItem() instanceof IArmorModule module && !armorStack.isEmpty()) {
         module.onInstall(stack, armorStack, (IArmorModule.IArmorModuleHolder)armorStack.getItem());
         this.save();
         return;
      }
   }

   public void onExtracted(ItemStack stack) {
      ItemStack armorStack = this.armor.getStackInSlot(0);
      if (stack.getItem() instanceof IArmorModule module && !armorStack.isEmpty()) {
         module.onUninstall(stack, armorStack, (IArmorModule.IArmorModuleHolder)armorStack.getItem());
         this.save();
         return;
      }
   }
}
