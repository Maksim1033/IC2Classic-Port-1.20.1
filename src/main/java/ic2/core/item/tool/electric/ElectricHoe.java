package ic2.core.item.tool.electric;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ic2.api.crops.ICropModifier;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IDamagelessElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.core.IC2;
import ic2.core.item.base.IC2ElectricItem;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.platform.rendering.features.item.IToolModel;
import ic2.core.utils.plugins.IRegistryProvider;
import ic2.core.utils.tooltips.IAdvancedTooltip;
import ic2.core.utils.tooltips.ILangHelper;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.function.Consumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class ElectricHoe
   extends HoeItem
   implements ic2.core.item.base.IItemCategoryFiller,
   ISimpleItemModel,
   IDamagelessElectricItem,
   IToolModel,
   IElectricEnchantable,
   ICropModifier,
   IAdvancedTooltip,
   ILangHelper,
   IRegistryProvider {
   protected int energyCost = 50;
   protected ResourceLocation id = GameData.checkPrefix("electric_hoe", false);

   public ElectricHoe() {
      super(Tiers.IRON, (int)Tiers.IRON.getAttackDamageBonus(), -3.0F, new Properties().setNoRepair().stacksTo(1));
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.BLOCK_CLICK, "tooltip.item.ic2.hoe.seedmode", new Object[0]));
   }

   public boolean isDamageable(ItemStack stack) {
      return false;
   }

   public InteractionResult useOn(UseOnContext context) {
      InteractionResult result = super.useOn(context);
      if (result.consumesAction()) {
         ElectricItem.MANAGER.use(context.getPlayer().getItemInHand(context.getHand()), this.energyCost, context.getPlayer());
      }

      return result;
   }

   public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entityLiving) {
      boolean result = super.mineBlock(stack, world, state, pos, entityLiving);
      if (entityLiving instanceof Player) {
         ElectricItem.MANAGER.use(stack, this.energyCost, entityLiving);
      } else {
         ElectricItem.MANAGER.discharge(stack, this.energyCost, 1, true, false, false);
      }

      return result;
   }

   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot, ItemStack stack) {
      Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
      if (equipmentSlot == EquipmentSlot.MAINHAND) {
         multimap.put(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)Tiers.IRON.getAttackDamageBonus(), Operation.ADDITION)
         );
         multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.0, Operation.ADDITION));
      }

      return multimap;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/hoe").get("base");
   }

   @Override
   public boolean canProvideEnergy(ItemStack stack) {
      return false;
   }

   @Override
   public boolean canChangeSeedMode(ItemStack input) {
      return true;
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

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         IC2ElectricItem.addEmptyAndFullToGroup(this, items);
      }
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
      ElectricItem.MANAGER.discharge(stack, ElectricItem.applyEnchantmentEffect(stack, this.energyCost * amount), 2147483647, true, false, false);
      return 0;
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment != Enchantments.BLOCK_FORTUNE && enchantment != Enchantments.UNBREAKING && enchantment != Enchantments.BLOCK_EFFICIENCY
         ? (enchantment == Enchantments.MENDING ? InteractionResult.FAIL : InteractionResult.PASS)
         : InteractionResult.SUCCESS;
   }

   @Override
   public EnchantmentCategory getEnchantmentType(ItemStack stack) {
      return EnchantmentCategory.BREAKABLE;
   }
}
