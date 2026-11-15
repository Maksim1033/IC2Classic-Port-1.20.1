package ic2.core.item.misc;

import ic2.api.items.armor.IArmorModule;
import ic2.api.items.electric.ElectricItem;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.base.features.ISoundBattery;
import ic2.core.item.wearable.base.IBaseArmorModule;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.IItemModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BatteryItem extends IC2ElectricItem implements IItemModel, ISoundBattery, IBaseArmorModule {
   public static final ResourceLocation AUDIO_FILE = new ResourceLocation("ic2", "sounds/tools/battery.ogg");
   private String textureName;
   private boolean hasStates;
   private int armor_transfer;

   public BatteryItem(
      String itemName,
      int capacity,
      int transferLimit,
      int tier,
      boolean provider,
      String textureName,
      boolean hasStates,
      int armor_transfer,
      @Nullable PropertiesBuilder props
   ) {
      super(itemName, props);
      this.capacity = capacity;
      this.tier = tier;
      this.transferLimit = transferLimit;
      this.provider = provider;
      this.textureName = textureName;
      this.hasStates = hasStates;
      this.armor_transfer = armor_transfer;
   }

   public BatteryItem(String itemName, int capacity, int transferLimit, int tier, boolean provider, String textureName, boolean hasStates, int armor_transfer) {
      super(itemName);
      this.capacity = capacity;
      this.tier = tier;
      this.transferLimit = transferLimit;
      this.provider = provider;
      this.textureName = textureName;
      this.hasStates = hasStates;
      this.armor_transfer = armor_transfer;
   }

   public BatteryItem(String itemName, int capacity, int transferLimit, int tier, boolean provider, String textureName, boolean hasStates) {
      super(itemName);
      this.capacity = capacity;
      this.tier = tier;
      this.transferLimit = transferLimit;
      this.provider = provider;
      this.textureName = textureName;
      this.hasStates = hasStates;
      this.armor_transfer = -1;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, "tooltip.item.ic2.battery.recharge", new Object[0]));
      if (this.armor_transfer > 0) {
         this.handleToolTip(stack, helper::addSimpleToolTip);
      }
   }

   @Override
   public List<ItemStack> getModelTypes() {
      if (!this.hasStates) {
         return ObjectLists.singleton(new ItemStack(this));
      } else {
         List<ItemStack> subTypes = CollectionUtils.<ItemStack>createList();

         for (int i = 0; i < 5; i++) {
            ItemStack battery = new ItemStack(this);
            ElectricItem.MANAGER.charge(battery, i * this.capacity / 4, 2147483647, true, false);
            subTypes.add(battery);
         }

         return subTypes;
      }
   }

   @Override
   protected int getEnergyCost(ItemStack stack) {
      return 0;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSprite(ItemStack input) {
      if (this.hasStates) {
         int percentage = ElectricItem.MANAGER.getCharge(input) * 4 / this.capacity;
         return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "battery").get(this.textureName + "_" + percentage);
      } else {
         return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), "battery").get(this.textureName);
      }
   }

   @Override
   public boolean isBarVisible(ItemStack stack) {
      return ElectricItem.MANAGER.getCharge(stack) > 0 && super.isBarVisible(stack);
   }

   public int getMaxStackSize(ItemStack stack) {
      return ElectricItem.MANAGER.getCharge(stack) > 0 ? 1 : 16;
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, @Nullable LivingEntity entity) {
      return this.hasStates ? ElectricItem.MANAGER.getCharge(stack) * 5 / this.capacity : 0;
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (ElectricItem.MANAGER.getCharge(stack) > 0) {
         playerIn.startUsingItem(handIn);
         return InteractionResultHolder.success(stack);
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   public void onUsingTick(ItemStack itemStack, LivingEntity entity, int count) {
      if (entity instanceof Player) {
         shareEnergy((Player)entity, itemStack, this.transferLimit, count);
      }
   }

   public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
      return oldStack.getItem() == newStack.getItem();
   }

   @Override
   public boolean wantsToPlay(ItemStack stack) {
      return true;
   }

   @Override
   public ResourceLocation getSound(ItemStack stack) {
      return AUDIO_FILE;
   }

   public int getUseDuration(ItemStack stack) {
      return 20000;
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      return oldStack.getItem() != newStack.getItem() || slotChanged;
   }

   public static void shareEnergy(Player player, ItemStack itemStack, int transferLimit, int tier) {
      int rate = ElectricItem.MANAGER.discharge(itemStack, 2 * transferLimit, tier, true, false, true);
      if (rate <= 0) {
         player.stopUsingItem();
      } else {
         boolean mainHand = player.getUsedItemHand() == InteractionHand.MAIN_HAND;
         Inventory inv = player.getInventory();
         boolean done = false;
         int i = 0;

         for (int m = Inventory.getSelectionSize(); i < m; i++) {
            if (!mainHand || i != inv.selected) {
               ItemStack stack = inv.getItem(i);
               if (!stack.isEmpty() && chargeItem(stack, itemStack, rate, tier)) {
                  done = true;
                  break;
               }
            }
         }

         if (mainHand && !done) {
            ItemStack stack = player.getItemBySlot(EquipmentSlot.OFFHAND);
            if (!stack.isEmpty() && chargeItem(stack, itemStack, rate, tier)) {
               done = true;
            }
         }

         if (!done) {
            player.stopUsingItem();
         }
      }
   }

   private static boolean chargeItem(ItemStack other, ItemStack self, int rate, int myTier) {
      rate = ElectricItem.MANAGER.charge(other, rate, myTier, true, false);
      return rate <= 0 ? false : ElectricItem.MANAGER.discharge(self, rate, myTier, true, false, false) > 0;
   }

   @Override
   public IArmorModule.ModuleType getType(ItemStack stack) {
      return IArmorModule.ModuleType.BATTERY;
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return this.armor_transfer > 0;
   }

   @Override
   public void onInstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      holder.addAddModifier(armor, IArmorModule.ArmorMod.ENERGY_STORAGE, this.capacity);
      holder.addAddModifier(armor, IArmorModule.ArmorMod.ENERGY_TIER, this.tier);
      holder.addAddModifier(armor, IArmorModule.ArmorMod.ENERGY_TRANSFER, this.armor_transfer);
      ElectricItem.MANAGER
         .discharge(stack, ElectricItem.MANAGER.charge(armor, ElectricItem.MANAGER.getCharge(stack), 2147483647, true, false), 2147483647, true, false, false);
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      ElectricItem.MANAGER
         .discharge(armor, ElectricItem.MANAGER.charge(stack, ElectricItem.MANAGER.getCharge(armor), 2147483647, true, false), 2147483647, true, false, false);
      holder.removeAddModifier(armor, IArmorModule.ArmorMod.ENERGY_STORAGE, this.capacity);
      holder.removeAddModifier(armor, IArmorModule.ArmorMod.ENERGY_TIER, this.tier);
      holder.removeAddModifier(armor, IArmorModule.ArmorMod.ENERGY_TRANSFER, this.armor_transfer);
   }

   @Override
   public boolean handlePacket(Player player, ItemStack module, ItemStack armor, String id, INetworkDataBuffer buffer, Dist targetSide) {
      return false;
   }
}
