package ic2.core.item.misc;

import ic2.core.item.base.IC2Item;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.IItemModel;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IconItem extends IC2Item implements IItemModel {
   List<Tuple<ResourceLocation, String>> textures = this.createIcons();

   public IconItem() {
      super("item_icons");
   }

   public IconItem(String name) {
      super(name);
   }

   public ItemStack create(int index) {
      ItemStack stack = new ItemStack(this);
      stack.addTagElement("index", ByteTag.valueOf((byte)Mth.clamp(index, 0, this.textures.size() - 1)));
      return stack;
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
   }

   @Override
   public int getModelIndexForStack(ItemStack stack, LivingEntity entity) {
      return Mth.clamp(StackUtil.getNbtData(stack).getInt("index"), 0, this.textures.size() - 1);
   }

   @Override
   public List<ItemStack> getModelTypes() {
      List<ItemStack> list = CollectionUtils.<ItemStack>createList();
      int i = 0;

      for (int m = this.textures.size(); i < m; i++) {
         ItemStack stack = new ItemStack(this);
         stack.addTagElement("index", ByteTag.valueOf((byte)i));
         list.add(stack);
      }

      return list;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSprite(ItemStack input) {
      Tuple<ResourceLocation, String> entry = this.textures.get(Mth.clamp(StackUtil.getNbtData(input).getInt("index"), 0, this.textures.size() - 1));
      return IC2Textures.getMappedEntries((ResourceLocation)entry.getA()).get(entry.getB());
   }

   public List<Tuple<ResourceLocation, String>> createIcons() {
      List<Tuple<ResourceLocation, String>> icons = CollectionUtils.<Tuple<ResourceLocation, String>>createList();
      icons.add(new Tuple(new ResourceLocation("ic2", "misc/gui"), "yes"));
      icons.add(new Tuple(new ResourceLocation("ic2", "misc/gui"), "no"));
      icons.add(new Tuple(new ResourceLocation("ic2", "misc/gui"), "memory_stick"));
      icons.add(new Tuple(new ResourceLocation("ic2", "misc/gui"), "add"));
      icons.add(new Tuple(new ResourceLocation("ic2", "misc/gui"), "remove"));
      icons.add(new Tuple(new ResourceLocation("ic2", "misc/gui"), "swap"));
      icons.add(new Tuple(new ResourceLocation("ic2", "item/upgrades/inventory"), "slot_diversity"));
      icons.add(new Tuple(new ResourceLocation("ic2", "item/upgrades/inventory"), "stack_diversity"));
      return icons;
   }
}
