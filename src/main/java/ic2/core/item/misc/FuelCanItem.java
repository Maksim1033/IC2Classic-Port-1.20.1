package ic2.core.item.misc;

import ic2.api.items.IFuelableItem;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.IItemModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FuelCanItem extends IC2Item implements IFuelableItem, IItemModel {
   public FuelCanItem() {
      super("fuel_can", new PropertiesBuilder().maxStackSize(1));
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      tooltip.add(this.translate("tooltip.item.ic2.fuel_can.release", new ChatFormatting[]{ChatFormatting.GRAY}));
      if (this.hasFuel(stack)) {
         CompoundTag nbt = StackUtil.getNbtData(stack);
         tooltip.add(this.translate("tooltip.item.ic2.fuel_can.stored", ChatFormatting.GRAY, new Object[]{nbt.getInt("fuel"), nbt.getInt("max_fuel")}));
      }
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         items.add(new ItemStack(this));
         items.add(this.fill(new ItemStack(this), 10000));
         items.add(this.fill(new ItemStack(this), 1000000));
      }
   }

   @Override
   public ItemStack fill(ItemStack stack, int amount) {
      ItemStack newStack = stack.copy();
      CompoundTag nbt = newStack.getOrCreateTag();
      amount += Math.max(0, nbt.getInt("fuel"));
      nbt.putInt("fuel", amount);
      nbt.putInt("max_fuel", amount);
      return newStack;
   }

   @Override
   public boolean canFuel(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("fuel") == 0;
   }

   @Override
   public boolean hasFuel(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("fuel") > 0;
   }

   @Override
   public int getFuel(ItemStack stack, int requested, boolean doDrain) {
      CompoundTag nbt = stack.getOrCreateTag();
      int stored = nbt.getInt("fuel");
      int removed = Math.min(requested, stored);
      if (doDrain) {
         nbt.putInt("fuel", stored - removed);
         if (stored - removed <= 0) {
            nbt.remove("fuel");
            nbt.remove("max_fuel");
            if (nbt.isEmpty()) {
               stack.setTag(null);
            }
         }
      }

      return removed;
   }

   public boolean isBarVisible(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("fuel") > 0;
   }

   public int getBarColor(ItemStack stack) {
      float f = (float)this.getBarWidth(stack) / 13.0F;
      return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
   }

   public int getBarWidth(ItemStack stack) {
      CompoundTag nbt = StackUtil.getNbtData(stack);
      return (int)Math.round(nbt.getDouble("fuel") / nbt.getDouble("max_fuel") * 13.0);
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, LivingEntity entity) {
      return StackUtil.getNbtData(stack).getInt("fuel") > 0 ? 1 : 0;
   }

   @Override
   public List<ItemStack> getModelTypes() {
      List<ItemStack> list = CollectionUtils.<ItemStack>createList();
      list.add(new ItemStack(this));
      ItemStack stack = new ItemStack(this);
      stack.getOrCreateTag().putInt("fuel", 1);
      return list;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSprite(ItemStack input) {
      return IC2Textures.getMappedEntriesItemIC2("tools").get(StackUtil.getNbtData(input).getInt("fuel") > 0 ? "filled_fuel_can" : "empty_fuel_can");
   }

   public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
      return Math.min(StackUtil.getNbtData(itemStack).getInt("fuel"), 500);
   }

   public boolean hasCraftingRemainingItem(ItemStack stack) {
      return true;
   }

   public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
      itemStack = itemStack.copy();
      CompoundTag nbt = itemStack.getOrCreateTag();
      nbt.putInt("fuel", Math.max(0, nbt.getInt("fuel") - 500));
      return itemStack;
   }
}
