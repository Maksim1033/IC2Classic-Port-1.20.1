package ic2.core.item.food_and_drink;

import ic2.api.items.IAutoEatable;
import ic2.api.recipes.misc.ICanEffect;
import ic2.core.IC2;
import ic2.core.inventory.filter.IFilter;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TinCanItem extends IC2Item implements ISimpleItemModel, IAutoEatable {
   public static final IFilter FOOD_CAN_FILTER = new TinCanItem.FoodCanFilter();
   private final int value = 2;
   private final float saturation = 0.95F;
   private final int eatTime = 20;
   private final boolean canEatWhenFull = true;
   private ICanEffect effect;

   public TinCanItem() {
      super("filled_tin_can", new PropertiesBuilder().group(IC2.FOOD_AND_DRINK_GROUP));
   }

   public TinCanItem(ICanEffect effect) {
      super("filled_tin_can_" + effect.getID().getPath(), new PropertiesBuilder().group(IC2.FOOD_AND_DRINK_GROUP));
      this.effect = effect;
   }

   protected String getOrCreateDescriptionId() {
      return "item.ic2.filled_tin_can";
   }

   public ICanEffect getEffect() {
      return this.effect;
   }

   @Override
   public int getFoodValue(ItemStack stack) {
      return this.value;
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.canEat(this.canEatWhenFull)) {
         player.startUsingItem(hand);
         return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
      } else {
         return new InteractionResultHolder(InteractionResult.FAIL, stack);
      }
   }

   public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity livingEntity) {
      if (livingEntity instanceof Player player && !world.isClientSide()) {
         StackUtil.addOrDrop(player, this.onEaten(stack, world, player));
         stack.shrink(1);
      }

      return stack;
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.EAT;
   }

   public int getUseDuration(ItemStack stack) {
      return this.eatTime;
   }

   public float getSaturation() {
      return this.saturation;
   }

   public int getHealing() {
      return this.value;
   }

   @Override
   public boolean canAutoEat(ItemStack stack) {
      return true;
   }

   @Override
   public ItemStack onEaten(ItemStack stack, Level level, Player player) {
      player.awardStat(IC2Stats.FOOD_CANS_EATEN);
      player.heal(2.0F);
      if (this.effect != null) {
         this.effect.onFoodEaten(stack, player);
      }

      player.getFoodData().eat(this.value, this.saturation);
      level.playSound(
         player, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F
      );
      player.awardStat(Stats.ITEM_USED.get(this));
      return new ItemStack(IC2Items.TIN_CAN);
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
      if (this.effect == null) {
         tooltip.add(this.translate("tooltip.item.ic2.filled_tin_can"));
      } else {
         Component comp = this.effect.getTooltip();
         if (comp != null) {
            tooltip.add(comp);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      if (this.effect != null) {
         TextureAtlasSprite override = this.effect.getOverrideIcon();
         if (override != null) {
            return override;
         }
      }

      return IC2Textures.getMappedEntriesItemIC2("food_and_drink/tincan").get("filled_tin_can");
   }

   public static class FoodCanFilter implements IFilter {
      @Override
      public boolean matches(ItemStack stack) {
         return stack.getItem() instanceof TinCanItem;
      }
   }
}
