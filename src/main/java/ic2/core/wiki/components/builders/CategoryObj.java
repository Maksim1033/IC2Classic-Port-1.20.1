package ic2.core.wiki.components.builders;

import ic2.core.utils.collection.CollectionUtils;
import ic2.core.wiki.base.IPageBuilder;
import ic2.core.wiki.components.CategoryComponent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class CategoryObj implements IWikiObj {
   CategoryObj.Link headerLink;
   Component header;
   List<CategoryObj.Link> links = CollectionUtils.<CategoryObj.Link>createList();

   public CategoryObj(String s, CategoryObj.Link... links) {
      this(Component.translatable(s), links);
   }

   public CategoryObj(String s, Object... args) {
      this(Component.translatable(s, args));
   }

   public CategoryObj(MutableComponent header, CategoryObj.Link... links) {
      this.header = header.withStyle(ChatFormatting.BOLD);
      this.addLinks(links);
   }

   public CategoryObj addLinks(CategoryObj.Link... links) {
      this.links.addAll(CollectionUtils.asList(links));
      return this;
   }

   public CategoryObj addHeaderLink(CategoryObj.Link headerLink) {
      this.headerLink = headerLink;
      return this;
   }

   @Override
   public void buildComponents(IPageBuilder builder) {
      builder.addComponent(new CategoryComponent(this.header, this.headerLink, this.links));
   }

   public static class Link {
      ResourceLocation link;
      ItemStack item;
      Component tooltip;

      public Link(ItemLike stack, String id) {
         this(new ItemStack(stack), new ResourceLocation(id));
      }

      public Link(ItemLike stack, String mod, String id) {
         this(new ItemStack(stack), new ResourceLocation(mod, id));
      }

      public Link(ItemLike stack, ResourceLocation location) {
         this(new ItemStack(stack), location);
      }

      public Link(ItemStack stack, String id) {
         this(stack, new ResourceLocation(id));
      }

      public Link(ItemStack stack, String mod, String id) {
         this(stack, new ResourceLocation(mod, id));
      }

      public Link(ItemStack stack, ResourceLocation location) {
         this.item = stack;
         this.link = location;
      }

      public CategoryObj.Link with(String s) {
         return this.with(Component.translatable(s));
      }

      public CategoryObj.Link with(String s, Object... obj) {
         return this.with(Component.translatable(s));
      }

      public CategoryObj.Link with(Component tooltip) {
         this.tooltip = tooltip;
         return this;
      }

      public ResourceLocation getLink() {
         return this.link;
      }

      public ItemStack getItem() {
         return this.item;
      }

      public Component getTooltip() {
         return this.tooltip;
      }
   }
}
