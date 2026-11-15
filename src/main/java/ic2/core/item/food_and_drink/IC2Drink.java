package ic2.core.item.food_and_drink;

import ic2.api.items.IDrinkContainer;
import ic2.api.items.IDrinkableFluid;
import ic2.core.IC2;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.rendering.features.item.IItemModel;
import ic2.core.utils.helpers.StackUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class IC2Drink extends IC2Item implements IDrinkContainer, IItemModel {
   protected IDrinkableFluid fluid;

   public IC2Drink(String itemName) {
      super(itemName, new PropertiesBuilder().group(IC2.FOOD_AND_DRINK_GROUP));
   }

   public IC2Drink(String itemName, IDrinkableFluid fluid) {
      super(itemName, new PropertiesBuilder().group(IC2.FOOD_AND_DRINK_GROUP));
      this.fluid = fluid;
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (player.canEat(true) && this.fluid != null) {
         player.startUsingItem(hand);
         return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
      } else {
         return new InteractionResultHolder(InteractionResult.FAIL, stack);
      }
   }

   public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity livingEntity) {
      if (livingEntity instanceof Player player && !world.isClientSide()) {
         this.fluid.drink(stack, world, player);
         StackUtil.addOrDrop(player, this.getEmptyContainer());
         stack.shrink(1);
      }

      return stack;
   }

   public abstract String getBasePath(ItemStack var1);

   @Override
   public IDrinkableFluid getContent(ItemStack stack) {
      return this.fluid;
   }

   @Override
   public boolean hasContent(ItemStack stack) {
      return this.fluid != null;
   }

   @Override
   public int getCapacity() {
      return 1000;
   }

   public int getUseDuration(ItemStack stack) {
      return 32;
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.DRINK;
   }

   public Component getName(ItemStack stack) {
      return this.fluid != null && this.fluid.hasSpecialName() ? this.fluid.getSpecialName(stack) : super.getName(stack);
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, @Nullable LivingEntity entity) {
      return this.fluid != null ? this.fluid.getTextureIndex(stack) : 0;
   }

   @Override
   public List<ItemStack> getModelTypes() {
      return this.fluid != null ? this.fluid.generateSubStates(new ItemStack(this), true) : Collections.singletonList(new ItemStack(this));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSprite(ItemStack input) {
      return (TextureAtlasSprite)Minecraft.getInstance()
         .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
         .apply(
            this.fluid == null
               ? new ResourceLocation(this.getRegistryName().getNamespace(), this.getBasePath(input) + "/empty")
               : this.fluid.getTexture(input, this.getRegistryName().getNamespace() + ":" + this.getBasePath(input))
         );
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         if (this.fluid == null) {
            items.add(new ItemStack(this));
         } else {
            items.addAll(this.fluid.generateSubStates(new ItemStack(this), false));
         }
      }
   }

   public abstract Map<IDrinkableFluid, Item> getFilledContainers();
}
