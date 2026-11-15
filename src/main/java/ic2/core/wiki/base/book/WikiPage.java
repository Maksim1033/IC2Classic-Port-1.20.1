package ic2.core.wiki.base.book;

import ic2.core.utils.collection.CollectionUtils;
import ic2.core.wiki.components.HeaderComponent;
import ic2.core.wiki.components.IWikiComponent;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WikiPage {
   WikiChapter owner;
   ResourceLocation link;
   List<IWikiComponent> components = CollectionUtils.<IWikiComponent>createList();
   boolean isSearchable = true;

   public void addComponent(IWikiComponent component) {
      this.components.add(component);
   }

   public WikiPage setOwner(WikiChapter owner) {
      this.owner = owner;
      return this;
   }

   public WikiChapter getOwner() {
      return this.owner;
   }

   public void setSearchable(boolean value) {
      this.isSearchable = value;
   }

   public WikiPage setLink(ResourceLocation link) {
      this.link = link;
      return this;
   }

   public ResourceLocation getLink() {
      return this.link;
   }

   public List<IWikiComponent> getComponents() {
      return this.components;
   }

   public Component getHeader() {
      for (int i = 0; i < this.components.size(); i++) {
         if (this.components.get(i) instanceof HeaderComponent) {
            return ((HeaderComponent)this.components.get(i)).getHeader();
         }
      }

      return null;
   }

   public String getSearchString(Map<ResourceLocation, WikiChapter> provider) {
      if (!this.isSearchable) {
         return "";
      } else {
         StringBuilder builder = new StringBuilder();
         int i = 0;

         for (int m = this.components.size(); i < m; i++) {
            this.components.get(i).addSearchString(T -> builder.append(T.getString()));
         }

         if (this.link != null) {
            WikiChapter chapter = provider.get(this.link);
            if (chapter != null) {
               chapter.buildSearchTree((P, S) -> builder.append(S), provider);
            }
         }

         return builder.toString();
      }
   }
}
