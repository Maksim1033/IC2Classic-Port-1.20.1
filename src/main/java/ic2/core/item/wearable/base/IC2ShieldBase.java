package ic2.core.item.wearable.base;

import ic2.core.IC2;
import ic2.core.item.renders.models.ShieldModel;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.platform.rendering.features.item.ICustomItemModel;
import ic2.core.platform.rendering.features.item.IToolModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.plugins.IRegistryProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.GameData;

public abstract class IC2ShieldBase extends ShieldItem implements ICustomItemModel, IToolModel, IRegistryProvider {
   ResourceLocation id;

   public IC2ShieldBase(String name, int maxDamage) {
      this(name, new Properties().durability(maxDamage));
   }

   public IC2ShieldBase(String name, Properties properties) {
      super(properties);
      this.id = GameData.checkPrefix(name, false);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   public void setDamage(ItemStack stack, int damage) {
      if (damage <= 0) {
         stack.removeTagKey("Damage");
      } else {
         stack.getOrCreateTag().putInt("Damage", damage);
      }
   }

   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(IC2.PLATFORM.getTileRenderer());
   }

   public abstract void damageShield(ItemStack var1, int var2, LivingEntity var3);

   public abstract ResourceLocation getTexture(ItemStack var1);

   public boolean canBlock(ItemStack item, LivingEntity entity) {
      return true;
   }

   public boolean isBlocking(ItemStack stack) {
      return StackUtil.getNbtData(stack).getBoolean("Blocking");
   }

   public ItemStack setBlocking(ItemStack stack, boolean blocking) {
      stack.getOrCreateTag().putBoolean("Blocking", blocking);
      return stack;
   }

   public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      if (this.canBlock(stack, playerIn)) {
         playerIn.startUsingItem(handIn);
         return new InteractionResultHolder(InteractionResult.CONSUME, stack);
      } else {
         return new InteractionResultHolder(InteractionResult.FAIL, stack);
      }
   }

   @Override
   public List<ItemStack> getCustomTypes() {
      List<ItemStack> list = new ArrayList<>();
      list.add(new ItemStack(this));
      list.add(this.setBlocking(new ItemStack(this), true));
      return list;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getModel(ItemStack stack) {
      boolean blocking = this.isBlocking(stack);
      return new ShieldModel(blocking).setOther(this.createResourceLocationForStack(this.setBlocking(stack.copy(), !blocking)));
   }

   private ModelResourceLocation createResourceLocationForStack(ItemStack stack) {
      boolean blocking = this.isBlocking(stack);
      String name = stack.getDescriptionId();
      name = name.substring(name.lastIndexOf(".") + 1);
      ResourceLocation resource = ForgeRegistryWrapper.getItemKey(stack.getItem());
      return new ModelResourceLocation(new ResourceLocation(resource.getNamespace(), name + (blocking ? "_blocking" : "")), "inventory");
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, @Nullable LivingEntity entity) {
      return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0;
   }

   @Override
   public ResourceLocation getCustomTransform(ItemStack stack) {
      return new ResourceLocation("minecraft:models/item/shield" + (this.isBlocking(stack) ? "_blocking" : ""));
   }

   public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
      return toolAction == ToolActions.SHIELD_BLOCK;
   }
}
