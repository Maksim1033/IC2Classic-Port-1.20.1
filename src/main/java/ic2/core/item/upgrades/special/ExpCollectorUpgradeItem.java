package ic2.core.item.upgrades.special;

import ic2.api.items.IUpgradeItem;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.tiles.IMachine;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.upgrades.base.BaseUpgradeItem;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class ExpCollectorUpgradeItem extends BaseUpgradeItem.SimpleUpgradeItem {
   public ExpCollectorUpgradeItem() {
      super("exp_collector", new PropertiesBuilder().maxStackSize(1));
      this.functions.add(IUpgradeItem.Functions.RECIPE);
   }

   @Override
   public int getExtraEnergyDemand(ItemStack stack, IMachine machine) {
      return 5;
   }

   @Override
   public void onMachineFinishedRecipePre(ItemStack stack, IMachine machine, IRecipeOutput output, CompoundTag recipeFlags) {
      float exp = output.getExperience();
      if (exp > 0.0F) {
         this.addExp(stack, exp * 0.5F);
      }
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack upgradeStack = player.getItemInHand(hand);
      int exp = (int)this.getExp(upgradeStack);
      if (exp > 0) {
         world.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            SoundSource.PLAYERS,
            0.1F,
            0.5F * ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1.8F)
         );
         Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(Enchantments.MENDING, player);
         int used = 0;
         if (entry != null) {
            ItemStack toolStack = entry.getValue();
            if (!toolStack.isEmpty() && toolStack.isDamaged()) {
               int i = Math.min(exp * 2, toolStack.getDamageValue());
               used += i * 2;
               toolStack.setDamageValue(toolStack.getDamageValue() - i);
            }
         }

         int left = exp - used;
         if (left > 0) {
            player.giveExperiencePoints(left);
         }

         this.removeExp(upgradeStack, (float)exp);
         return new InteractionResultHolder(InteractionResult.SUCCESS, upgradeStack);
      } else {
         return new InteractionResultHolder(InteractionResult.PASS, upgradeStack);
      }
   }

   public void addExp(ItemStack item, float exp) {
      CompoundTag nbt = item.getOrCreateTag();
      float stuff = nbt.getFloat("exp");
      stuff += exp;
      if (stuff > 5000.0F) {
         stuff = 5000.0F;
      }

      nbt.putFloat("exp", stuff);
   }

   public void removeExp(ItemStack stack, float exp) {
      CompoundTag nbt = stack.getOrCreateTag();
      float amount = nbt.getFloat("exp");
      amount -= exp;
      if (amount < 0.0F) {
         amount = 0.0F;
      }

      nbt.putFloat("exp", amount);
   }

   public float getExp(ItemStack item) {
      return StackUtil.getNbtData(item).getFloat("exp");
   }

   @Override
   public IUpgradeItem.UpgradeType getType(ItemStack stack) {
      return IUpgradeItem.UpgradeType.PROCESSING_MOD;
   }

   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addSimpleToolTip("tooltip.item.ic2.exp_collector", Formatters.XP_FORMAT.format((double)this.getExp(stack)));
   }
}
