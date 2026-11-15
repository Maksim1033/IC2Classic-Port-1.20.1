package ic2.core.item.tool.electric;

import ic2.api.items.armor.IFoamSupplier;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IDamagelessElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.core.IC2;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.tool.SprayerTool;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.function.Consumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class ElectricCFoamSprayerTool extends SprayerTool implements ISimpleItemModel, IDamagelessElectricItem, IFoamSupplier, IElectricEnchantable {
   public static final int[] SPRAYER_MODES = new int[]{1, 2, 4, 8, 16, 32};

   public ElectricCFoamSprayerTool() {
      super("electric_sprayer", new PropertiesBuilder().group(IC2.IC2_MAIN_GROUP).setNoRepair().maxStackSize(1));
   }

   @Override
   public int getSprayLimit(ItemStack stack) {
      CompoundTag nbt = stack.getOrCreateTag();
      int amount = SPRAYER_MODES[nbt.getInt("mode")];
      return ElectricItem.MANAGER.canUse(stack, amount * 5) ? amount : 0;
   }

   @Override
   public int getStoredFoam(ItemStack stack) {
      CompoundTag nbt = stack.getOrCreateTag();
      return nbt.getInt("stored");
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (IC2.KEYBOARD.isModeSwitchKeyDown(player)) {
         if (world.isClientSide) {
            return InteractionResultHolder.pass(stack);
         } else {
            CompoundTag nbt = stack.getOrCreateTag();
            int newMode = nbt.getInt("mode") + 1;
            if (newMode >= SPRAYER_MODES.length) {
               newMode = 0;
            }

            nbt.putInt("mode", newMode);
            player.displayClientMessage(this.translate("info.item.ic2.electric_sprayer.range", new Object[]{SPRAYER_MODES[newMode]}), false);
            return InteractionResultHolder.success(stack);
         }
      } else {
         return super.use(world, player, hand);
      }
   }

   public InteractionResult useOn(UseOnContext context) {
      return IC2.KEYBOARD.isModeSwitchKeyDown(context.getPlayer()) ? InteractionResult.PASS : super.useOn(context);
   }

   @Override
   public void useFoam(ItemStack stack, Player player, int amount) {
      if (!player.isCreative()) {
         CompoundTag nbt = stack.getOrCreateTag();
         int newAmount = Math.max(0, nbt.getInt("stored") - amount);
         nbt.putInt("stored", newAmount);
      }
   }

   @Override
   public void onFoamUsed(ItemStack stack, Player player, int amount) {
      super.onFoamUsed(stack, player, amount);
      ElectricItem.MANAGER.use(stack, amount * 5, player);
   }

   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/cf_sprayer").get("base");
   }

   @Override
   public boolean canProvideEnergy(ItemStack stack) {
      return false;
   }

   @Override
   public int getCapacity(ItemStack stack) {
      return 10000;
   }

   @Override
   public int getTier(ItemStack stack) {
      return 1;
   }

   @Override
   public int getTransferLimit(ItemStack stack) {
      return 100;
   }

   @Override
   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         IC2ElectricItem.addEmptyAndFullToGroup(this, items);
         ItemStack stored = new ItemStack(this);
         stored.getOrCreateTag().putInt("stored", 1000);
         IC2ElectricItem.addEmptyAndFullToGroup(stored, items);
      }
   }

   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      CompoundTag nbt = stack.getOrCreateTag();
      helper.addSimpleToolTip("tooltip.item.ic2.electric_sprayer.range", SPRAYER_MODES[nbt.getInt("mode")]);
      helper.addSimpleToolTip("tooltip.item.ic2.electric_sprayer.stored", nbt.getInt("stored"));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, "tooltip.item.ic2.electric_sprayer.change_range", new Object[0]));
   }

   public int getBarWidth(ItemStack stack) {
      return IC2ElectricItem.getElectricWidth(stack);
   }

   public int getBarColor(ItemStack stack) {
      return IC2ElectricItem.getRGBDurability(stack);
   }

   public boolean isBarVisible(ItemStack stack) {
      return true;
   }

   public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
      ElectricItem.MANAGER.discharge(stack, ElectricItem.applyEnchantmentEffect(stack, 5 * amount), 2147483647, true, false, false);
      return 0;
   }

   @Override
   public boolean canProvideFoam(Player player, ItemStack stack, IFoamSupplier.InventoryType inv, int amount) {
      return false;
   }

   @Override
   public void useFoam(Player player, ItemStack stack, int amount) {
   }

   @Override
   public int getFreeFoamSpace(ItemStack stack) {
      return 1000 - StackUtil.getNbtData(stack).getInt("stored");
   }

   @Override
   public void fillFoam(ItemStack stack, int amount) {
      CompoundTag data = stack.getOrCreateTag();
      data.putInt("stored", Math.min(1000, data.getInt("stored") + amount));
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
}
