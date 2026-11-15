package ic2.core.item.misc;

import net.minecraft.core.registries.BuiltInRegistries;
import ic2.api.items.ITagBlock;
import ic2.core.item.base.IC2Item;
import ic2.core.item.renders.models.TagModel;
import ic2.core.platform.rendering.features.item.ICustomItemModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TagBlock extends IC2Item implements ICustomItemModel, ITagBlock {
   public TagBlock() {
      super("tag_block");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      super.addToolTip(stack, player, type, helper);
      ResourceLocation location = getTag(stack);
      helper.addSimpleToolTip("tooltip.item.ic2.full_tag", location == null ? "minecraft:empty" : location.toString());
   }

   public Component getName(ItemStack stack) {
      ResourceLocation location = getTag(stack);
      return Component.translatable("item.ic2.tag_block", new Object[]{location == null ? this.string("empty") : this.string(location.getPath())});
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, LivingEntity entity) {
      return 0;
   }

   @Override
   public List<ItemStack> getCustomTypes() {
      return ObjectLists.singleton(new ItemStack(this));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getModel(ItemStack stack) {
      return new TagModel();
   }

   @Override
   public boolean matches(ItemStack self, Block block) {
      ResourceLocation location = getTag(self);
      return location != null && block.builtInRegistryHolder().containsTag(BlockTags.create(location));
   }

   @Override
   public List<Block> getBlocks(ItemStack self) {
      ResourceLocation location = getTag(self);
      if (location == null) {
         return Collections.emptyList();
      } else {
         HolderSet<Block> set = BuiltInRegistries.BLOCK.getTag(BlockTags.create(location)).orElse(null);
         return set == null ? Collections.emptyList() : set.stream().<Block>map(Holder::value).collect(Collectors.toList());
      }
   }

   public static void setTag(ItemStack stack, ResourceLocation tag) {
      stack.addTagElement("tag", StringTag.valueOf(tag.toString()));
   }

   public static ResourceLocation getTag(ItemStack stack) {
      return ResourceLocation.tryParse(StackUtil.getNbtData(stack).getString("tag"));
   }
}
