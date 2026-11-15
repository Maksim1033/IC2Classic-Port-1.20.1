package ic2.core.wiki.base;

import ic2.core.utils.collection.CollectionUtils;
import ic2.core.wiki.components.builders.IWikiObj;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public interface IChapterBuilder {
   default void startBuildChapter(String id, String name) {
      this.startBuildChapter(new ResourceLocation(id, name));
   }

   void startBuildChapter(ResourceLocation var1);

   default void addSubChapter(String id, String name) {
      this.addSubChapter(new ResourceLocation(id, name));
   }

   void addSubChapter(ResourceLocation var1);

   default void addSimplePage(IWikiObj... objects) {
      this.addSimplePage(CollectionUtils.<IWikiObj>asList(objects));
   }

   void addSimplePage(List<IWikiObj> var1);

   void addSubPages(ItemLike... var1);

   void addSubPages(ResourceLocation... var1);

   void addPageRedirector(ResourceLocation var1, ItemLike... var2);

   void addPageRedirector(ResourceLocation var1, ResourceLocation... var2);

   default void addPreviewBookMark(String mod, String name) {
      this.addPreviewBookMark(new ResourceLocation(mod, name));
   }

   void addPreviewBookMark(ResourceLocation var1);

   void finishBuildChapter(boolean var1);

   void finishSubChapter();
}
