package ic2.core.item.tool.electric;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IDamagelessElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.core.IC2;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.tool.WrenchTool;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.function.Consumer;
import javax.annotation.Nullable;
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

public class ElectricWrenchTool extends WrenchTool implements IDamagelessElectricItem, IElectricEnchantable {
   protected int capacity = 12000;
   protected int tier = 1;
   protected int transferLimit = 250;
   protected int toolDamage = 50;
   protected double chanceModifier = 1.1;
   protected int losslessUses = 15;

   public ElectricWrenchTool() {
      this("electric_wrench", null);
   }

   public ElectricWrenchTool(String itemName, @Nullable PropertiesBuilder properties) {
      super(itemName, (properties == null ? new PropertiesBuilder() : properties).group(IC2.IC2_MAIN_GROUP).setNoRepair().maxDamage(0).maxStackSize(1));
   }

   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/wrench").get("base");
   }

   @Override
   public boolean canProvideEnergy(ItemStack stack) {
      return false;
   }

   @Override
   public int getCapacity(ItemStack stack) {
      return this.capacity;
   }

   @Override
   public int getTier(ItemStack stack) {
      return this.tier;
   }

   @Override
   public int getTransferLimit(ItemStack stack) {
      return this.transferLimit;
   }

   @Override
   public boolean canOverrideLoss(ItemStack stack) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      return nbt.getBoolean("losslessMode") && nbt.getInt("LossUses") < this.losslessUses;
   }

   @Override
   public void onLossPrevented(Player player, ItemStack stack) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      int uses = nbt.getInt("LossUses") + 1;
      nbt.putInt("LossUses", uses);
      if (!player.level().isClientSide()) {
         player.displayClientMessage(this.translate("tooltip.item.ic2.electric_wrench.machine_saved", new Object[]{this.losslessUses - uses}), true);
      }

      if (uses >= this.losslessUses) {
         nbt.putBoolean("losslessMode", false);
         player.displayClientMessage(this.translate("tooltip.item.ic2.electric_wrench.losslessWrenchModeOff"), false);
      }
   }

   @Override
   public boolean canTakeDamage(ItemStack stack, int amount) {
      return ElectricItem.MANAGER.canUse(stack, amount * this.toolDamage);
   }

   public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
      ElectricItem.MANAGER.use(stack, amount * this.toolDamage, entity);
      return 0;
   }

   @Override
   public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
      return IC2.KEYBOARD.isModeSwitchKeyDown(context.getPlayer()) ? InteractionResult.PASS : super.onItemUseFirst(stack, context);
   }

   @Override
   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (IC2.PLATFORM.isSimulating() && IC2.KEYBOARD.isModeSwitchKeyDown(playerIn)) {
         CompoundTag nbt = stack.getOrCreateTag();
         if (nbt.getInt("LossUses") < this.losslessUses) {
            boolean newValue = !nbt.getBoolean("losslessMode");
            nbt.putBoolean("losslessMode", newValue);
            playerIn.displayClientMessage(
               this.translate(newValue ? "tooltip.item.ic2.electric_wrench.losslessWrenchModeOn" : "tooltip.item.ic2.electric_wrench.losslessWrenchModeOff"),
               false
            );
         }

         return InteractionResultHolder.success(stack);
      } else {
         return super.use(worldIn, playerIn, handIn);
      }
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         IC2ElectricItem.addEmptyAndFullToGroup(this, items);
      }
   }

   @Override
   public double getChanceModifier(ItemStack stack) {
      return this.chanceModifier;
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

   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      helper.addSimpleToolTip("tooltip.item.ic2.electric_wrench.losslessWrenchUses", this.losslessUses - nbt.getInt("LossUses"));
      helper.addSimpleToolTip(
         nbt.getBoolean("losslessMode") ? "tooltip.item.ic2.electric_wrench.losslessWrenchModeOn" : "tooltip.item.ic2.electric_wrench.losslessWrenchModeOff"
      );
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.MODE_KEY, "tooltip.item.ic2.electric_wrench.toggleLosslessMode", new Object[0]));
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment == Enchantments.BLOCK_FORTUNE
         ? InteractionResult.SUCCESS
         : (enchantment == Enchantments.MENDING ? InteractionResult.FAIL : InteractionResult.PASS);
   }

   @Override
   public EnchantmentCategory getEnchantmentType(ItemStack stack) {
      return EnchantmentCategory.BREAKABLE;
   }

   public static class PrecisionWrenchTool extends ElectricWrenchTool {
      public PrecisionWrenchTool() {
         super("precision_wrench", new PropertiesBuilder().group(IC2.IC2_MAIN_GROUP).setNoRepair().maxStackSize(1));
         this.capacity = 40000;
         this.tier = 2;
         this.transferLimit = 350;
         this.toolDamage = 100;
         this.chanceModifier = 2.0;
         this.losslessUses = 30;
      }

      @Override
      public TextureAtlasSprite getTexture() {
         return IC2Textures.getMappedEntriesItemIC2("tools/wrench").get("precision");
      }

      @Override
      public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
         CompoundTag nbt = StackUtil.getNbtData(stack);
         if (nbt.getBoolean("lossless")) {
            helper.addSimpleToolTip("tooltip.item.ic2.electric_wrench.losslessWrenchInfiniteUses");
         } else {
            helper.addSimpleToolTip("tooltip.item.ic2.electric_wrench.losslessWrenchUses", this.losslessUses - nbt.getInt("LossUses"));
            helper.addSimpleToolTip("tooltip.item.ic2.electric_wrench.losslessVanilla");
         }
      }

      @Override
      public boolean canOverrideLoss(ItemStack stack) {
         CompoundTag nbt = StackUtil.getNbtData(stack);
         return nbt.getBoolean("lossless") || nbt.getInt("LossUses") < this.losslessUses;
      }

      @Override
      public void onLossPrevented(Player player, ItemStack stack) {
         CompoundTag nbt = StackUtil.getNbtData(stack);
         if (!nbt.getBoolean("lossless")) {
            int uses = nbt.getInt("LossUses") + 1;
            nbt.putInt("LossUses", uses);
         }
      }

      @Override
      public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
         return IC2Items.WRENCH.use(worldIn, playerIn, handIn);
      }

      @Override
      public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
         if (this.m_220152_(group)) {
            IC2ElectricItem.addEmptyAndFullToGroup(this, items);
            ItemStack lossless = new ItemStack(this);
            lossless.getOrCreateTag().putBoolean("lossless", true);
            IC2ElectricItem.addEmptyAndFullToGroup(lossless, items);
         }
      }
   }
}
