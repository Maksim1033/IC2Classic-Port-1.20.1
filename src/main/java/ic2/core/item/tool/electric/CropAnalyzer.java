package ic2.core.item.tool.electric;

import ic2.api.items.armor.IArmorModule;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.api.items.readers.ICropReader;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.item.INetworkItemEvent;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldSlotInventory;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.inv.container.CropBreederContainer;
import ic2.core.item.inv.inventory.CropAnalyzerInventory;
import ic2.core.item.inv.inventory.CropBreederInventory;
import ic2.core.item.wearable.base.IBaseArmorModule;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CropAnalyzer
   extends IC2ElectricItem
   implements ISimpleItemModel,
   IHasHeldSlotInventory,
   INetworkItemEvent,
   ICropReader,
   IBaseArmorModule,
   IElectricEnchantable {
   public CropAnalyzer() {
      super("crop_analyzer");
      this.capacity = 50000;
      this.transferLimit = 100;
      this.tier = 1;
   }

   @Override
   public boolean isCropReader(ItemStack stack) {
      return true;
   }

   @Override
   protected int getEnergyCost(ItemStack stack) {
      return 100;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools").get("crop_analyzer");
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (IC2.PLATFORM.isSimulating() && !playerIn.isShiftKeyDown()) {
         if (IC2.KEYBOARD.isModeSwitchKeyDown(playerIn)) {
            CompoundTag nbt = stack.getOrCreateTag();
            boolean breed = !nbt.getBoolean("breed");
            nbt.putBoolean("breed", breed);
            if (!breed) {
               nbt.remove("results");
               nbt.remove("crops");
            }

            playerIn.displayClientMessage(
               this.translate(
                  "tooltip.item.ic2.crop_analyzer.mode",
                  new Object[]{this.translate("tooltip.item.ic2.crop_analyzer." + (nbt.getBoolean("breed") ? "breeding" : "analyzing"))}
               ),
               false
            );
         } else {
            IC2.PLATFORM.launchGui(playerIn, handIn, Direction.NORTH, this.getInventory(playerIn, handIn, stack));
         }
      }

      return InteractionResultHolder.success(stack);
   }

   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      helper.addSimpleToolTip(
         "tooltip.item.ic2.crop_analyzer.mode", this.translate("tooltip.item.ic2.crop_analyzer." + (nbt.getBoolean("breed") ? "breeding" : "analyzing"))
      );
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, "tooltip.item.ic2.crop_analyzer.toggleMode", new Object[0]));
      this.handleToolTip(stack, helper::addSimpleToolTip);
   }

   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return true;
   }

   @Override
   public IPortableInventory getInventory(Player player, InteractionHand hand, ItemStack stack) {
      return StackUtil.getNbtData(stack).getBoolean("breed")
         ? new CropBreederInventory(player, this, stack, null).load(stack)
         : new CropAnalyzerInventory(player, this, stack, null, IC2.PLATFORM.isSimulating()).load(stack);
   }

   @Override
   public IPortableInventory getInventory(Player player, ItemStack stack, Slot slot) {
      return StackUtil.getNbtData(stack).getBoolean("breed")
         ? new CropBreederInventory(player, this, stack, slot).load(stack)
         : new CropAnalyzerInventory(player, this, stack, slot, IC2.PLATFORM.isSimulating()).load(stack);
   }

   @Override
   public void onEventReceived(ItemStack stack, Player player, int key, int value, Dist target) {
      if (target.isDedicatedServer() && player.containerMenu instanceof CropBreederContainer) {
         CropBreederInventory inventory = ((CropBreederContainer)player.containerMenu).getHolder();
         switch (key) {
            case 0:
               inventory.allCrops.offset(value);
               break;
            case 1:
               inventory.results.offset(value);
               break;
            case 2:
               inventory.results.clear();
               break;
            case 3:
               inventory.breed();
               break;
            case 4:
               inventory.waterlogged = !inventory.waterlogged;
         }
      }
   }

   @Override
   public IArmorModule.ModuleType getType(ItemStack stack) {
      return IArmorModule.ModuleType.HUD;
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.HEAD;
   }

   @Override
   public void onInstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      holder.addAddModifier(armor, IArmorModule.ArmorMod.CROP_SCANNER, 1);
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      holder.removeAddModifier(armor, IArmorModule.ArmorMod.CROP_SCANNER, 1);
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment == Enchantments.UNBREAKING
         ? InteractionResult.SUCCESS
         : (enchantment == Enchantments.MENDING ? InteractionResult.FAIL : InteractionResult.PASS);
   }

   @Override
   public EnchantmentCategory getEnchantmentType(ItemStack stack) {
      return EnchantmentCategory.BREAKABLE;
   }

   @Override
   public boolean handlePacket(Player player, ItemStack module, ItemStack armor, String id, INetworkDataBuffer buffer, Dist targetSide) {
      return false;
   }
}
