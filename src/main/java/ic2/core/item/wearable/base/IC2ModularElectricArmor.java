package ic2.core.item.wearable.base;

import ic2.api.core.IC2Classic;
import ic2.api.items.armor.IArmorHud;
import ic2.api.items.armor.IArmorModule;
import ic2.api.items.armor.IEnergyShieldArmor;
import ic2.api.items.armor.IMetalArmor;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.readers.ICropReader;
import ic2.api.items.readers.IEUReader;
import ic2.api.items.readers.IThermometer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.item.INetworkItemBufferEvent;
import ic2.api.recipes.misc.RecipeMods;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.wearable.modules.IModuleUseAction;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public abstract class IC2ModularElectricArmor
   extends IC2ElectricArmorBase
   implements IArmorModule.IArmorModuleHolder,
   IHasHeldSlotInventory,
   IEUReader,
   IThermometer,
   ICropReader,
   IC2JetpackBase.IIndirectJetpack,
   IEnergyShieldArmor,
   IArmorHud,
   INetworkItemBufferEvent<INetworkDataBuffer>,
   IMetalArmor {
   private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
   public static final String UPGRADE_LIST = "armor_upgrades";
   public static final String UPGRADE_TYPE = "upgrade_type";
   protected Object2IntMap<IArmorModule.ModuleType> types = new Object2IntOpenHashMap();

   public IC2ModularElectricArmor(String itemName, EquipmentSlot slot, PropertiesBuilder props) {
      super(itemName, slot, props);
      if (slot == EquipmentSlot.HEAD) {
         this.types.put(IArmorModule.ModuleType.HUD, 9);
      }
   }

   public abstract ItemStack createDefaultArmor();

   protected void addSlotType(IArmorModule.ModuleType type, int slots) {
      this.types.put(type, slots);
   }

   @Override
   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> subItems) {
      if (this.m_220152_(group)) {
         subItems.add(new ItemStack(this));
         IC2ElectricItem.addEmptyAndFullToGroup(this.createDefaultArmor(), subItems);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      for (ItemStack entry : this.getInstalledModules(stack)) {
         if (entry.getItem() instanceof IArmorToolTip) {
            ((IArmorToolTip)entry.getItem()).addToolTip(stack, entry, player, type, helper);
         }
      }
   }

   @Override
   public Object2IntMap<IArmorModule.ModuleType> getModuleLimits(ItemStack stack) {
      return Object2IntMaps.unmodifiable(this.types);
   }

   public void onArmorTick(ItemStack stack, Level world, Player player) {
      for (ItemStack module : this.getInstalledModules(stack)) {
         if (module.getItem() instanceof IArmorModule) {
            ((IArmorModule)module.getItem()).onTick(module, stack, world, player);
         }
      }
   }

   @Override
   public int getCapacity(ItemStack stack) {
      return getValue(stack, IArmorModule.ArmorMod.ENERGY_STORAGE, this.capacity);
   }

   @Override
   public int getTier(ItemStack stack) {
      return getValue(stack, IArmorModule.ArmorMod.ENERGY_TIER, this.tier);
   }

   @Override
   public int getTransferLimit(ItemStack stack) {
      return getValue(stack, IArmorModule.ArmorMod.ENERGY_TRANSFER, this.transferLimit);
   }

   @Override
   public boolean canProvideEnergy(ItemStack stack) {
      return getValue(stack, IArmorModule.ArmorMod.ENERGY_PROVIDER, 0) > 0;
   }

   @Override
   public boolean isCropReader(ItemStack stack) {
      return getValue(stack, IArmorModule.ArmorMod.CROP_SCANNER, 0) > 0;
   }

   @Override
   public boolean isThermometer(ItemStack stack) {
      return getValue(stack, IArmorModule.ArmorMod.THERMOMETER, 0) > 0;
   }

   @Override
   public boolean isEUReader(ItemStack stack) {
      return getValue(stack, IArmorModule.ArmorMod.EU_READER, 0) > 0;
   }

   @Override
   public boolean isHudEnabled(ItemStack stack) {
      return getValue(stack, IArmorModule.ArmorMod.HUD_ENERGY, 0) > 0;
   }

   public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
      return getValue(stack, IArmorModule.ArmorMod.GOLDEN, 0) > 0;
   }

   public boolean isFullyAbsorbingFallDamage(ItemStack stack) {
      return false;
   }

   @Override
   public boolean isBlockingEverything(ItemStack stack, DamageSource source, LivingEntity living) {
      if ("sonic_boom".equals(source.getMsgId())) {
         for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!(living.getItemBySlot(slot).getItem() instanceof IC2ModularElectricArmor)) {
               return false;
            }
         }

         return true;
      } else {
         return !source.is(DamageTypeTags.BYPASSES_ARMOR)
            && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
            && !source.is(DamageTypeTags.BYPASSES_EFFECTS)
            && !source.is(DamageTypes.MAGIC)
            && !source.is(DamageTypes.INDIRECT_MAGIC);
      }
   }

   @Override
   public boolean isMetalArmor(ItemStack stack, Player player, EquipmentSlot targetSlot) {
      return targetSlot == this.getEquipmentSlot();
   }

   @Override
   public boolean addsShieldEffect(EquipmentSlot type, LivingEntity entity, ItemStack stack) {
      return type == this.getEquipmentSlot()
         && getValue(stack, IArmorModule.ArmorMod.ENERGY_SHIELD, 0) + getValue(stack, IArmorModule.ArmorMod.ENERGY_SHIELD_ALWAYS, 0) > 0;
   }

   @Override
   public boolean isEffectAlwaysOn(EquipmentSlot type, LivingEntity entity, ItemStack stack) {
      return type == this.getEquipmentSlot() && getValue(stack, IArmorModule.ArmorMod.ENERGY_SHIELD_ALWAYS, 0) > 0;
   }

   @Override
   public void onDataBufferReceived(ItemStack stack, Player player, String id, INetworkDataBuffer buffer, Dist targetSide) {
      for (CompoundTag nbt : NBTListWrapper.wrap(StackUtil.getNbtData(stack).getList("armor_upgrades", 10), CompoundTag.class)) {
         ItemStack item = ItemStack.of(nbt);
         if (item.getItem() instanceof IArmorModule && ((IArmorModule)item.getItem()).handlePacket(player, item, stack, id, buffer, targetSide)) {
            return;
         }
      }
   }

   @Override
   public IC2JetpackBase getJetpack(ItemStack stack) {
      if (this.getEquipmentSlot() != EquipmentSlot.CHEST) {
         return null;
      } else {
         for (CompoundTag nbt : NBTListWrapper.wrap(StackUtil.getNbtData(stack).getList("armor_upgrades", 10), CompoundTag.class)) {
            ItemStack jetpack = ItemStack.of(nbt);
            if (jetpack.getItem() instanceof IC2JetpackBase) {
               return (IC2JetpackBase)jetpack.getItem();
            }
         }

         return null;
      }
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (IC2.KEYBOARD.isSideInventoryKeyDown(playerIn) && this.hasInventory(stack)) {
         IC2.PLATFORM.launchGui(playerIn, handIn, Direction.NORTH, this.getInventory(playerIn, handIn, stack));
         return InteractionResultHolder.success(stack);
      } else {
         IC2ModularElectricArmor armorItem = (IC2ModularElectricArmor)stack.getItem();

         for (ItemStack module : this.getInstalledModules(stack)) {
            if (module.getItem() instanceof IModuleUseAction && ((IModuleUseAction)module.getItem()).use(worldIn, playerIn, module, stack, armorItem)) {
               return InteractionResultHolder.success(stack);
            }
         }

         return super.use(worldIn, playerIn, handIn);
      }
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      ItemStack result = this.findInventory(stack);
      return result.isEmpty() ? null : ((IHasArmorInventory)result.getItem()).getInventory(player, hand, stack);
   }

   @Override
   public boolean hasInventory(ItemStack stack) {
      ItemStack result = this.findInventory(stack);
      return !result.isEmpty();
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      ItemStack result = this.findInventory(stack);
      return result.isEmpty() ? null : ((IHasArmorInventory)result.getItem()).getInventory(player, stack, slot);
   }

   protected ItemStack findInventory(ItemStack stack) {
      for (ItemStack entry : this.getInstalledModules(stack)) {
         if (entry.getItem() instanceof IHasArmorInventory && ((IHasArmorInventory)entry.getItem()).hasArmorInventory(stack, entry)) {
            return entry;
         }
      }

      return ItemStack.EMPTY;
   }

   public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      return new IC2ModularElectricArmor.ArmorCapability(this, stack);
   }

   @Override
   public List<ItemStack> getInstalledModules(ItemStack stack) {
      List<ItemStack> items = CollectionUtils.<ItemStack>createList();

      for (CompoundTag nbt : NBTListWrapper.wrap(StackUtil.getNbtData(stack).getList("armor_upgrades", 10), CompoundTag.class)) {
         ItemStack item = ItemStack.of(nbt);
         if (!item.isEmpty()) {
            items.add(item);
         }
      }

      return items;
   }

   @Override
   public void onEquipmentStateChanged(ItemStack stack, boolean equip, Player player) {
      for (CompoundTag nbt : NBTListWrapper.wrap(StackUtil.getNbtData(stack).getList("armor_upgrades", 10), CompoundTag.class)) {
         ItemStack item = ItemStack.of(nbt);
         if (item.getItem() instanceof IArmorModule) {
            if (equip) {
               ((IArmorModule)item.getItem()).onEquipped(item, stack, player);
            } else {
               ((IArmorModule)item.getItem()).onUnequipped(item, stack, player);
            }
         }
      }
   }

   public static Object2ObjectMap<IArmorModule.ModuleType, List<ItemStack>> getModuleTypes(ItemStack stack) {
      Object2ObjectMap<IArmorModule.ModuleType, List<ItemStack>> maps = CollectionUtils.<IArmorModule.ModuleType, List<ItemStack>>createLinkedMap();

      for (CompoundTag nbt : NBTListWrapper.wrap(StackUtil.getNbtData(stack).getList("armor_upgrades", 10), CompoundTag.class)) {
         ItemStack item = ItemStack.of(nbt);
         if (!item.isEmpty()) {
            IArmorModule.ModuleType type = IArmorModule.ModuleType.getType(nbt.getString("upgrade_type"));
            if (type != null) {
               List<ItemStack> list = (List<ItemStack>)maps.get(type);
               if (list == null) {
                  list = CollectionUtils.<ItemStack>createList();
                  maps.put(type, list);
               }

               list.add(item);
            }
         }
      }

      return maps;
   }

   public static void setAndInstallTypes(ItemStack stack, Map<IArmorModule.ModuleType, List<ItemStack>> types) {
      IArmorModule.IArmorModuleHolder holder = (IArmorModule.IArmorModuleHolder)stack.getItem();
      ListTag list = new ListTag();

      for (Entry<IArmorModule.ModuleType, List<ItemStack>> entry : types.entrySet()) {
         String s = entry.getKey().getId();
         List<ItemStack> itemList = entry.getValue();
         int i = 0;

         for (int m = itemList.size(); i < m; i++) {
            ItemStack module = itemList.get(i);
            if (!module.isEmpty()) {
               if (module.getItem() instanceof IArmorModule) {
                  ((IArmorModule)module.getItem()).onInstall(module, stack, holder);
               }

               CompoundTag nbt = module.save(new CompoundTag());
               nbt.putString("upgrade_type", s);
               list.add(nbt);
            }
         }
      }

      stack.getOrCreateTag().put("armor_upgrades", list);
      ((IHasHeldGui)stack.getItem()).setGuiID(stack, -1);
      int stored = ElectricItem.MANAGER.discharge(stack, 2147483647, 2147483647, true, false, false);
      if (stored > 0) {
         ElectricItem.MANAGER.charge(stack, stored, 2147483647, true, false);
      }
   }

   public static void setModuleTypes(ItemStack stack, Map<IArmorModule.ModuleType, List<ItemStack>> types) {
      ListTag list = new ListTag();

      for (Entry<IArmorModule.ModuleType, List<ItemStack>> entry : types.entrySet()) {
         String s = entry.getKey().getId();
         List<ItemStack> itemList = entry.getValue();
         int i = 0;

         for (int m = itemList.size(); i < m; i++) {
            ItemStack module = itemList.get(i);
            if (!module.isEmpty()) {
               CompoundTag nbt = module.save(new CompoundTag());
               nbt.putString("upgrade_type", s);
               list.add(nbt);
            }
         }
      }

      ((IHasHeldGui)stack.getItem()).setGuiID(stack, -1);
      stack.getOrCreateTag().put("armor_upgrades", list);
      int stored = ElectricItem.MANAGER.discharge(stack, 2147483647, 2147483647, true, false, false);
      if (stored > 0) {
         ElectricItem.MANAGER.charge(stack, stored, 2147483647, true, false);
      }
   }

   @Override
   public void addAddModifier(ItemStack stack, IArmorModule.ArmorMod type, int amount) {
      if (type.getAdd() != null) {
         CompoundTag nbt = stack.getOrCreateTagElement("mods");
         nbt.putInt(type.getAdd(), nbt.getInt(type.getAdd()) + amount);
      }
   }

   @Override
   public void addMulModifier(ItemStack stack, IArmorModule.ArmorMod type, float amount) {
      if (type.getMul() != null) {
         CompoundTag nbt = stack.getOrCreateTagElement("mods");
         nbt.putFloat(type.getMul(), nbt.getFloat(type.getMul()) + amount);
      }
   }

   @Override
   public void removeAddModifier(ItemStack stack, IArmorModule.ArmorMod type, int amount) {
      if (type.getAdd() != null) {
         CompoundTag nbt = stack.getTagElement("mods");
         if (nbt != null) {
            int add = nbt.getInt(type.getAdd()) - amount;
            if (add == 0) {
               nbt.remove(type.getAdd());
               if (nbt.isEmpty()) {
                  stack.removeTagKey("mods");
               }
            } else {
               nbt.putInt(type.getAdd(), add);
            }
         }
      }
   }

   @Override
   public void removeMulModifier(ItemStack stack, IArmorModule.ArmorMod type, float amount) {
      if (type.getMul() != null) {
         CompoundTag nbt = stack.getTagElement("mods");
         if (nbt != null) {
            float mul = nbt.getFloat(type.getMul()) - amount;
            if (mul == 0.0F) {
               nbt.remove(type.getMul());
               if (nbt.isEmpty()) {
                  stack.removeTagKey("mods");
               }
            } else {
               nbt.putFloat(type.getMul(), mul);
            }
         }
      }
   }

   public static int getValue(ItemStack stack, IArmorModule.ArmorMod type, int base) {
      CompoundTag nbt = stack.getTagElement("mods");
      return nbt == null
         ? base
         : RecipeMods.apply(
            0, base, type.getAdd() == null ? 0 : nbt.getInt(type.getAdd()), type.getMul() == null ? 1.0 : (double)(1.0F + nbt.getFloat(type.getMul()))
         );
   }

   public static class ArmorCapability implements IArmorModule.IArmorCapability, ICapabilityProvider, INBTSerializable<CompoundTag> {
      LazyOptional<IArmorModule.IArmorCapability> own = LazyOptional.of(() -> this);
      IC2ModularElectricArmor module;
      ItemStack stack;
      Object2ObjectMap<Capability<?>, ICapabilityProvider> caps = CollectionUtils.<Capability<?>, ICapabilityProvider>createLinkedMap();

      public ArmorCapability(IC2ModularElectricArmor module, ItemStack stack) {
         this.module = module;
         this.stack = stack;
      }

      public CompoundTag serializeNBT() {
         CompoundTag nbt = new CompoundTag();
         ObjectIterator var2 = Object2ObjectMaps.fastIterable(this.caps).iterator();

         while (var2.hasNext()) {
            Entry<Capability<?>, ICapabilityProvider> entry = (Entry<Capability<?>, ICapabilityProvider>)var2.next();
            if (entry.getValue() instanceof INBTSerializable) {
               nbt.put(entry.getKey().getName(), ((INBTSerializable)entry.getValue()).serializeNBT());
            }
         }

         return nbt;
      }

      public void deserializeNBT(CompoundTag nbt) {
         for (ItemStack stack : this.module.getInstalledModules(this.stack)) {
            if (stack.getItem() instanceof IArmorModule) {
               ((IArmorModule)stack.getItem()).provideCapabilities(stack, this.stack);
            }
         }

         ObjectIterator var4 = Object2ObjectMaps.fastIterable(this.caps).iterator();

         while (var4.hasNext()) {
            Entry<Capability<?>, ICapabilityProvider> entry = (Entry<Capability<?>, ICapabilityProvider>)var4.next();
            if (entry.getValue() instanceof INBTSerializable) {
               ((INBTSerializable)entry.getValue()).deserializeNBT(nbt.get(entry.getKey().getName()));
            }
         }
      }

      public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
         if (cap == IC2Classic.ARMOR_CAPABILITY) {
            return this.own.cast();
         } else {
            ICapabilityProvider lazy = (ICapabilityProvider)this.caps.get(cap);
            return lazy == null ? LazyOptional.empty() : lazy.getCapability(cap, side);
         }
      }

      @Override
      public <T> void registerArmorCapability(Capability<T> cap, ICapabilityProvider value) {
         this.caps.put(cap, value);
      }

      @Override
      public <T> ICapabilityProvider removeArmorCapability(Capability<T> cap) {
         return (ICapabilityProvider)this.caps.remove(cap);
      }

      @Override
      public <T> LazyOptional<T> getArmorCapability(Capability<T> cap) {
         ICapabilityProvider lazy = (ICapabilityProvider)this.caps.get(cap);
         return lazy == null ? LazyOptional.empty() : lazy.getCapability(cap);
      }
   }
}
