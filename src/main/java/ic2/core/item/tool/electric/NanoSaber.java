package ic2.core.item.tool.electric;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ic2.api.items.electric.ElectricItem;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.item.base.ElectricToolItem;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.wearable.base.IC2ModularElectricArmor;
import ic2.core.platform.player.KeyHelper;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.IItemModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class NanoSaber extends ElectricToolItem implements IItemModel {
   public int soundTicker = 0;
   public static ResourceLocation saberIdle = new ResourceLocation("ic2", "sounds/tools/nanosaber/nanosaber_idle.ogg");
   public static ResourceLocation saberPowerUp = new ResourceLocation("ic2", "sounds/tools/nanosaber/nanosaber_powerup.ogg");
   public static ResourceLocation saberSwingOne = new ResourceLocation("ic2", "sounds/tools/nanosaber/nanosaber_swing_1.ogg");
   public static ResourceLocation saberSwingTwo = new ResourceLocation("ic2", "sounds/tools/nanosaber/nanosaber_swing_2.ogg");
   public static ResourceLocation saberSwingThree = new ResourceLocation("ic2", "sounds/tools/nanosaber/nanosaber_swing_3.ogg");
   public static final RandomSource RANDOM = RandomSource.create();

   public NanoSaber() {
      super("nano_saber", 0.0F, 1.0F, 2.0F, new PropertiesBuilder().maxDamage(0).maxStackSize(1).setNoRepair());
   }

   @Override
   public List<ItemStack> getModelTypes() {
      List<ItemStack> types = CollectionUtils.<ItemStack>createList();
      ItemStack inactive = new ItemStack(this);
      ItemStack active_0 = new ItemStack(this);
      ItemStack active_1 = new ItemStack(this);
      inactive.getOrCreateTag().putBoolean("active", false);
      CompoundTag tag_0 = active_0.getOrCreateTag();
      CompoundTag tag_1 = active_1.getOrCreateTag();
      tag_0.putBoolean("active", true);
      tag_0.putBoolean("0", true);
      tag_1.putBoolean("active", true);
      tag_1.putBoolean("0", false);
      types.add(inactive);
      types.add(active_0);
      types.add(active_1);
      return types;
   }

   public boolean canAttackBlock(BlockState p_43291_, Level p_43292_, BlockPos p_43293_, Player p_43294_) {
      return !p_43294_.isCreative();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addDataTooltip("tooltip.item.ic2.nano_saber");
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, "tooltip.item.ic2.nano_saber.enable", new Object[0]));
      helper.addKeybindingTooltip(this.buildKeyDescription(KeyHelper.RIGHT_CLICK, KeyHelper.ALT_KEY, "tooltip.item.ic2.nano_saber.disable", new Object[0]));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSprite(ItemStack input) {
      CompoundTag nbt = input.getOrCreateTag();
      return nbt.getBoolean("active")
         ? IC2Textures.getMappedEntriesItemIC2("tools/nano_saber").get("active_" + (nbt.getBoolean("0") ? 0 : 1))
         : IC2Textures.getMappedEntriesItemIC2("tools/nano_saber").get("off");
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, @Nullable LivingEntity entity) {
      return stack.getOrCreateTag().getBoolean("active") ? RANDOM.nextInt(2) + 1 : 0;
   }

   @Override
   public int getCapacity(ItemStack stack) {
      return 40000;
   }

   @Override
   public int getTransferLimit(ItemStack stack) {
      return 128;
   }

   @Override
   public int getTier(ItemStack stack) {
      return 2;
   }

   @Override
   public int getEnergyCost(ItemStack stack) {
      return 10;
   }

   public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
      return toolAction == ToolActions.SWORD_SWEEP && StackUtil.getNbtData(stack).getBoolean("active");
   }

   @Override
   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (!StackUtil.getNbtData(stack).getBoolean("active")) {
         return true;
      } else {
         if (IC2.PLATFORM.isSimulating() && (IC2.PLATFORM.isPVP() || !(attacker instanceof Player))) {
            if (target instanceof Player player) {
               for (EquipmentSlot slot : EquipmentSlot.values()) {
                  if (slot.getType() == Type.ARMOR) {
                     ItemStack armor = player.getItemBySlot(slot);
                     Item usage = armor.getItem();
                     if (usage instanceof IC2ModularElectricArmor) {
                        IC2ModularElectricArmor modular = (IC2ModularElectricArmor)usage;
                        int usagex = (int)(3000.0 * Math.pow(6.25, (double)(modular.getTier(armor) - 1)));
                        ElectricItem.MANAGER.discharge(armor, usagex, 2147483647, true, false, false);
                        if (ElectricItem.MANAGER.discharge(armor, 1, 2147483647, true, false, true) < 1) {
                           player.setItemSlot(slot, ItemStack.EMPTY);
                        }

                        drainSaber(stack, 16, attacker);
                     }
                  }
               }
            }

            drainSaber(stack, 40, attacker);
         }

         return true;
      }
   }

   public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
      return IC2Items.SWORD_BRONZE.isCorrectToolForDrops(stack, state);
   }

   public static void drainSaber(ItemStack stack, int needed, LivingEntity entity) {
      if (IC2.PLATFORM.isSimulating()) {
         if (!ElectricItem.MANAGER.use(stack, needed, entity)) {
            stack.getOrCreateTag().putBoolean("active", false);
         }
      }
   }

   @Override
   public float getDestroySpeed(ItemStack stack, BlockState state) {
      if (StackUtil.getNbtData(stack).getBoolean("active")) {
         if (IC2.PLATFORM.isRendering()) {
            this.soundTicker++;
            if (this.soundTicker % 4 == 0) {
               IC2.AUDIO.playSound(IC2.PLATFORM.getClientPlayerInstance(), this.getRandomSwingSound(), AudioManager.SoundType.ITEM);
            }
         }

         return IC2Items.SWORD_BRONZE.getDestroySpeed(stack, state);
      } else {
         return 1.0F;
      }
   }

   @Override
   public boolean isCorrectToolForDrops(BlockState blockIn) {
      return blockIn.getBlock() == Blocks.COBWEB;
   }

   public ResourceLocation getRandomSwingSound() {
      switch (RANDOM.nextInt(3)) {
         case 1:
            return saberSwingTwo;
         case 2:
            return saberSwingThree;
         default:
            return saberSwingOne;
      }
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      CompoundTag nbt = stack.getOrCreateTag();
      if (nbt.getBoolean("active") && PlayerHandler.getHandler(playerIn).altKeyDown) {
         if (IC2.PLATFORM.isSimulating()) {
            nbt.putBoolean("active", false);
         }

         return InteractionResultHolder.success(stack);
      } else if (!nbt.getBoolean("active") && ElectricItem.MANAGER.canUse(stack, 16)) {
         if (IC2.PLATFORM.isSimulating()) {
            nbt.putBoolean("active", true);
         }

         IC2.AUDIO.playSound(playerIn, saberPowerUp, AudioManager.SoundType.ITEM, IC2.AUDIO.getVolumeForType(AudioManager.SoundType.ITEM), 1.0F);
         return InteractionResultHolder.success(stack);
      } else {
         return InteractionResultHolder.pass(stack);
      }
   }

   public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
      if (entityIn instanceof Player && StackUtil.getNbtData(stack).getBoolean("active")) {
         CompoundTag nbt = stack.getOrCreateTag();
         int delay = nbt.getInt("delay");
         delay++;
         boolean drain = isSelected ? delay >= 16 : delay >= 64;
         if (drain) {
            delay = 0;
         }

         nbt.putInt("delay", delay);
         if (drain) {
            drainSaber(stack, 16, (LivingEntity)entityIn);
         }
      }
   }

   @Override
   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot, ItemStack stack) {
      Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
      if (equipmentSlot == EquipmentSlot.MAINHAND) {
         map.put(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", StackUtil.getNbtData(stack).getBoolean("active") ? 19.0 : 3.0, Operation.ADDITION)
         );
      }

      return map;
   }

   @Override
   public void setDamage(ItemStack stack, int damage) {
   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      return StackUtil.getNbtData(oldStack).getBoolean("active") != StackUtil.getNbtData(newStack).getBoolean("active") || slotChanged;
   }

   public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
      return oldStack.getItem() != newStack.getItem();
   }

   @Override
   public EnchantmentCategory getEnchantmentType(ItemStack stack) {
      return EnchantmentCategory.WEAPON;
   }
}
