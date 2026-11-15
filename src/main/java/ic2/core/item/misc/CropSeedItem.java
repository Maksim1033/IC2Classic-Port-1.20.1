package ic2.core.item.misc;

import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRegistry;
import ic2.api.crops.ICropSeed;
import ic2.core.block.base.misc.color.IItemColorListener;
import ic2.core.block.crops.CropRegistry;
import ic2.core.item.base.IC2Item;
import ic2.core.item.renders.models.CropItemModel;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.features.item.ICustomItemModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.OptionalLong;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.util.Strings;

public class CropSeedItem extends IC2Item implements ICropSeed, ICustomItemModel, IItemColorListener {
   public static final String BREED_RESULT = "breed";

   public CropSeedItem() {
      super("crop_seed");
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      int level = this.getScanLevel(stack);
      if (level >= 4) {
         tooltip.add(
            this.translate("gui.ic2.crop_analyzer.growth", new ChatFormatting[]{ChatFormatting.DARK_GREEN})
               .append(this.string(" " + this.getGrowth(stack), new ChatFormatting[]{ChatFormatting.WHITE}))
         );
         tooltip.add(
            this.translate("gui.ic2.crop_analyzer.gain", new ChatFormatting[]{ChatFormatting.GOLD})
               .append(this.string(" " + this.getGain(stack), new ChatFormatting[]{ChatFormatting.WHITE}))
         );
         tooltip.add(
            this.translate("gui.ic2.crop_analyzer.resistance", new ChatFormatting[]{ChatFormatting.DARK_AQUA})
               .append(this.string(" " + this.getResistance(stack), new ChatFormatting[]{ChatFormatting.WHITE}))
         );
      }

      if (level > 0 && !isVisible(stack) && !Screen.hasControlDown()) {
         tooltip.add(this.translate("tooltip.item.ic2.crop.item", new ChatFormatting[]{ChatFormatting.GRAY}));
      }

      int[] result = StackUtil.getNbtData(stack).getIntArray("breed");
      if (result.length == 5) {
         double total = (double)result[0];
         tooltip.add(this.translate("info.crop.ic2.data.points", new Object[]{result[1]}));
         tooltip.add(this.translate("info.crop.ic2.data.points.props", new Object[]{result[2]}));
         tooltip.add(this.translate("info.crop.ic2.data.points.attr", new Object[]{result[3]}));
         tooltip.add(this.translate("info.crop.ic2.data.points.tier", new Object[]{result[4]}));
         tooltip.add(this.translate("info.crop.ic2.data.chance", new Object[]{ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)result[1] / total * 100.0)}));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getItemColor(ItemStack stack, int tintIndex) {
      ItemStack entry = getDisplayItem(stack);
      return entry.isEmpty() ? -1 : Minecraft.getInstance().getItemColors().getColor(entry, tintIndex);
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, @Nullable LivingEntity entity) {
      return 0;
   }

   public Component getName(ItemStack stack) {
      CompoundTag tag = StackUtil.getNbtData(stack);
      if ("ic2:random".equals(tag.getString("crop"))) {
         return ((MutableComponent)super.getName(stack)).append(": Random Crop");
      } else {
         ICrop crop = this.getCrop(stack);
         return ((MutableComponent)super.getName(stack))
            .append(": ")
            .append((Component)(crop != null && this.getScanLevel(stack) >= 1 ? crop.getName() : this.translate("info.crop.ic2.data.unknown")));
      }
   }

   @Override
   public List<ItemStack> getCustomTypes() {
      return ObjectLists.singleton(new ItemStack(this));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getModel(ItemStack stack) {
      return new CropItemModel();
   }

   @Override
   public ICrop getCrop(ItemStack stack) {
      CompoundTag tag = StackUtil.getNbtData(stack);
      String id = tag.getString("crop");
      if (Strings.isEmpty(id)) {
         return null;
      } else {
         return id.equalsIgnoreCase("ic2:random")
            ? ICropRegistry.INSTANCE.getRandomCrop(tag.contains("seed") ? OptionalLong.of(tag.getLong("seed")) : OptionalLong.empty())
            : ICropRegistry.INSTANCE.getCrop(new ResourceLocation(id));
      }
   }

   @Override
   public int getScanLevel(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("scan");
   }

   @Override
   public void setScanLevel(ItemStack stack, int scan) {
      stack.getOrCreateTag().putByte("scan", (byte)scan);
   }

   @Override
   public int getGrowth(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("growth");
   }

   @Override
   public void setGrowth(ItemStack stack, int growth) {
      stack.getOrCreateTag().putByte("growth", (byte)growth);
   }

   @Override
   public int getGain(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("gain");
   }

   @Override
   public void setGain(ItemStack stack, int gain) {
      stack.getOrCreateTag().putByte("gain", (byte)gain);
   }

   @Override
   public int getResistance(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("resistance");
   }

   @Override
   public void setResistance(ItemStack stack, int resistance) {
      stack.getOrCreateTag().putByte("resistance", (byte)resistance);
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         for (ICrop crop : ICropRegistry.INSTANCE.getCrops()) {
            if (crop != ICropRegistry.WEED && crop != CropRegistry.SEA_WEED) {
               ItemStack stack = createStack(crop, 1, 1, 1, 4);
               if (!stack.isEmpty()) {
                  items.add(stack);
               }
            }
         }

         items.add(createRandomSeed(OptionalLong.empty(), 4));
         if (group != null && group.getType() == CreativeModeTab.Type.SEARCH) {
            for (ICrop cropx : ICropRegistry.INSTANCE.getCrops()) {
               if (cropx != ICropRegistry.WEED && cropx != CropRegistry.SEA_WEED) {
                  ItemStack stack = createStack(cropx, 1, 1, 1, 0);
                  if (!stack.isEmpty()) {
                     items.add(stack);
                  }
               }
            }

            items.add(createRandomSeed(OptionalLong.empty(), 0));
         }
      }
   }

   public static void setVisible(ItemStack stack, boolean visible) {
      stack.getOrCreateTag().putBoolean("visible", visible);
   }

   public static boolean isVisible(ItemStack stack) {
      return StackUtil.getNbtData(stack).getBoolean("visible");
   }

   public static ItemStack createStack(ICrop crop, int growth, int gain, int resistance, int scan) {
      if (crop == null) {
         return ItemStack.EMPTY;
      } else {
         ItemStack stack = new ItemStack(IC2Items.CROP_SEED);
         CompoundTag nbt = stack.getOrCreateTag();
         nbt.putString("crop", crop.id().toString());
         nbt.putByte("growth", (byte)growth);
         nbt.putByte("gain", (byte)gain);
         nbt.putByte("resistance", (byte)resistance);
         nbt.putByte("scan", (byte)scan);
         return stack;
      }
   }

   public static ItemStack createRandomSeed(OptionalLong seed, int scan) {
      ItemStack stack = new ItemStack(IC2Items.CROP_SEED);
      CompoundTag nbt = stack.getOrCreateTag();
      nbt.putString("crop", "ic2:random");
      seed.ifPresent(T -> nbt.putLong("seed", T));
      nbt.putByte("scan", (byte)scan);
      return stack;
   }

   @OnlyIn(Dist.CLIENT)
   public static ItemStack getDisplayItem(ItemStack base) {
      if (!Screen.hasControlDown() && !isVisible(base) || !(base.getItem() instanceof CropSeedItem item)) {
         return ItemStack.EMPTY;
      } else if (item.getScanLevel(base) < 1 && !isVisible(base)) {
         return ItemStack.EMPTY;
      } else {
         ICrop crop = item.getCrop(base);
         return crop == null ? ItemStack.EMPTY : ICropRegistry.INSTANCE.getDisplayItem(crop.id());
      }
   }
}
