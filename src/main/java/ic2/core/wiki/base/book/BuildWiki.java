package ic2.core.wiki.base.book;

import ic2.core.IC2;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.math.MathUtils;
import ic2.core.wiki.base.managers.IWikiProvider;
import ic2.core.wiki.helper.BookMark;
import ic2.core.wiki.helper.PageVisit;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;

public class BuildWiki implements IWikiProvider {
   Map<ResourceLocation, WikiChapter> chapters;
   Map<ResourceLocation, PageVisit> subChapter;
   Map<PageVisit, String> searchTree = CollectionUtils.<PageVisit, String>createLinkedMap();
   WikiChapter defaultChapter;
   WikiChapter currentChapter;
   PageVisit mainChapter;
   PageVisit controls;
   int page;
   String location;
   List<BookMark> bookMarks = CollectionUtils.<BookMark>createList();
   ObjectArrayFIFOQueue<PageVisit> memory = new ObjectArrayFIFOQueue();
   String searchKey = "";

   public BuildWiki(
      Map<ResourceLocation, WikiChapter> chapters,
      Map<ResourceLocation, PageVisit> subChapter,
      ResourceLocation defaultPage,
      ResourceLocation mainPage,
      Entry<ResourceLocation> control,
      String location
   ) {
      this.chapters = chapters;
      this.subChapter = subChapter;
      this.defaultChapter = chapters.get(defaultPage);
      this.currentChapter = chapters.get(defaultPage);
      this.mainChapter = subChapter.getOrDefault(mainPage, new PageVisit(this.defaultChapter, 0));
      if (this.currentChapter == null) {
         throw new IllegalStateException("Default Chapter is required");
      } else {
         this.location = location;
         if (control != null) {
            WikiChapter chapter = chapters.get(control.getKey());
            if (chapter != null) {
               this.controls = new PageVisit(chapter, control.getIntValue());
            }
         }

         this.loadLocations();
         long time = System.nanoTime();
         Set<WikiChapter> checkedChapters = CollectionUtils.<WikiChapter>createSet();

         for (WikiChapter current : this.chapters.values()) {
            for (WikiChapter entry : current.iteratePages()) {
               if (checkedChapters.add(entry)) {
                  entry.buildSearchTree(this.searchTree::put, chapters);
               }
            }
         }

         IC2.LOGGER.info(MathUtils.toLagString("Search Tree Loading Time", System.nanoTime() - time));
      }
   }

   public List<BookMark> getBookMarks() {
      return this.bookMarks;
   }

   private void loadLocations() {
      if (this.location != null) {
         Path path = IC2.PLATFORM.getConfigFolder().resolve(this.location);
         if (Files.exists(path)) {
            this.bookMarks.clear();

            try (InputStream stream = Files.newInputStream(path)) {
               CompoundTag nbt = NbtIo.readCompressed(stream);

               for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("marks", 10), CompoundTag.class)) {
                  BookMark mark = BookMark.read(data, this.chapters);
                  if (mark != null) {
                     this.bookMarks.add(mark);
                  }
               }
            } catch (Exception var9) {
            }
         }
      }
   }

   public void updateLocations(List<BookMark> marks) {
      this.bookMarks.clear();
      this.bookMarks.addAll(marks);
      if (this.location != null) {
         ListTag list = new ListTag();

         for (BookMark mark : this.bookMarks) {
            list.add(mark.save());
         }

         CompoundTag nbt = new CompoundTag();
         nbt.put("marks", list);
         Path path = IC2.PLATFORM.getConfigFolder().resolve(this.location);
         if (Files.notExists(path.getParent())) {
            try {
               Files.createDirectories(path.getParent());
            } catch (Exception var9) {
            }
         }

         try (OutputStream stream = Files.newOutputStream(path)) {
            NbtIo.writeCompressed(nbt, stream);
         } catch (Exception var11) {
         }
      }
   }

   public void setToDefault() {
      this.currentChapter = this.defaultChapter;
      this.page = 0;
   }

   public WikiChapter getCurrentChapter() {
      return this.currentChapter;
   }

   public Component getCurrentHeader() {
      WikiPage page = this.getCurrentPage();
      if (page != null) {
         Component header = page.getHeader();
         if (header != null) {
            return header;
         }
      }

      for (int i = this.page; i >= 0; i--) {
         page = this.currentChapter.getPage(i);
         if (page != null) {
            Component header = page.getHeader();
            if (header != null) {
               return header;
            }
         }
      }

      return null;
   }

   public List<PageVisit> getPages(String searchText) {
      if (searchText.isEmpty()) {
         return Collections.emptyList();
      } else {
         String lowerSearch = searchText.toLowerCase(Locale.ROOT);
         Set<PageVisit> foundEntries = CollectionUtils.<PageVisit>createLinkedSet();

         for (java.util.Map.Entry<PageVisit, String> entry : this.searchTree.entrySet()) {
            if (entry.getValue().contains(lowerSearch)) {
               foundEntries.add(entry.getKey());
            }
         }

         List<PageVisit> result = new ObjectArrayList(foundEntries);
         result.sort(Comparator.comparing(T -> this.getWorth(T, lowerSearch)));
         return result;
      }
   }

   private int getWorth(PageVisit visit, String search) {
      String s = visit.getHeader().getString().toLowerCase(Locale.ROOT);
      return StringUtils.getLevenshteinDistance(search, s) + (s.contains(search) ? 0 : 200) + (s.startsWith(search) ? 0 : 100);
   }

   public String getSearchKey() {
      return this.searchKey;
   }

   public void setSearchKey(String searchKey) {
      this.searchKey = searchKey;
   }

   public PageVisit getControls() {
      return this.controls;
   }

   public int getPage() {
      return this.page;
   }

   public PageVisit getMainPage() {
      return this.mainChapter;
   }

   public PageVisit getDefaultPage() {
      return new PageVisit(this.defaultChapter, 0);
   }

   public PageVisit getCurrentVisit() {
      return new PageVisit(this.currentChapter, this.page);
   }

   public WikiPage getCurrentPage() {
      return this.getPage(this.page);
   }

   public WikiPage getNextPage() {
      return this.getPage(this.page + 1);
   }

   public WikiPage getPage(int index) {
      return this.currentChapter.getPage(index);
   }

   public boolean hasPrevPage() {
      return this.page > 0 || this.currentChapter.getPrev() != null;
   }

   public boolean hasNextPage() {
      return this.page + 2 < this.currentChapter.size() || this.currentChapter.getNext() != null;
   }

   public boolean hasPageLink(boolean prev) {
      WikiPage page = prev ? this.getCurrentPage() : this.getNextPage();
      return page != null && page.getLink() != null && this.hasPageLink(page.getLink(), 0);
   }

   protected int evenOut(int count) {
      return (count & 1) != 0 ? count + 1 : count;
   }

   public int getPageIndex() {
      int count = this.page;

      for (WikiChapter chapter = this.currentChapter.getPrev(); chapter != null; chapter = chapter.getPrev()) {
         count += this.evenOut(chapter.size());
      }

      return count;
   }

   public int getPageCount() {
      int count = 0;

      for (WikiChapter chapter = this.currentChapter.getPrev(); chapter != null; chapter = chapter.getPrev()) {
         count += this.evenOut(chapter.size());
      }

      for (WikiChapter chapter = this.currentChapter.getNext(); chapter != null; chapter = chapter.getNext()) {
         count += this.evenOut(chapter.size());
      }

      return count + this.evenOut(this.currentChapter.size());
   }

   @Override
   public boolean handleClickEvent(ClickEvent event) {
      if (event != null) {
         String[] action = event.getValue().split(";");
         if (action.length < 2) {
            return false;
         }

         if (action[0].equalsIgnoreCase("redirect")) {
            WikiChapter chapter = this.chapters.get(new ResourceLocation(action[1]));
            int offset = 0;
            if (chapter == null) {
               PageVisit visit = this.subChapter.get(new ResourceLocation(action[1]));
               if (visit == null) {
                  return false;
               }

               chapter = visit.getChapter();
               offset = visit.getPage();
               if (chapter == null) {
                  return false;
               }
            } else {
               try {
                  if (action.length > 2) {
                     offset = Integer.parseInt(action[2]);
                  }
               } catch (Exception var6) {
               }
            }

            return this.openPage(new PageVisit(chapter, offset), true);
         }
      }

      return false;
   }

   public boolean hasPageLink(ResourceLocation location, int page) {
      WikiChapter chapter = this.chapters.get(location);
      return chapter != null && chapter.size() > page ? true : this.subChapter.get(location) != null;
   }

   public boolean tryOpenPage(ResourceLocation location, int page) {
      WikiChapter chapter = this.chapters.get(location);
      if (chapter != null && chapter.size() > page) {
         return this.openPage(new PageVisit(chapter, page), true);
      } else {
         PageVisit visit = this.subChapter.get(location);
         return visit != null ? this.openPage(visit, true) : false;
      }
   }

   @Override
   public boolean openPage(PageVisit visit, boolean save) {
      return this.openPage(visit, save, false);
   }

   public boolean openPage(PageVisit visit, boolean save, boolean wasSearching) {
      if (!visit.isValid()) {
         return false;
      } else {
         if (save) {
            this.memory.enqueue(new PageVisit(this.currentChapter, this.page).withSearching(wasSearching));
            if (this.memory.size() > 256) {
               this.memory.dequeue();
            }
         }

         this.currentChapter = visit.getChapter();
         this.setPageIndex(visit.getPage());
         return true;
      }
   }

   protected void setPageIndex(int requestedIndex) {
      requestedIndex = Mth.clamp(requestedIndex, 0, this.currentChapter.size());
      this.page = requestedIndex - (requestedIndex & 1);
   }

   public void flipForward(boolean shift) {
      if (this.page + 2 < this.currentChapter.size() && !shift) {
         this.openPage(new PageVisit(this.currentChapter, Math.min(this.currentChapter.size() - 1, this.page + 2)), this.memory.isEmpty());
      } else {
         WikiChapter next = this.currentChapter.getNext();
         if (next != null) {
            this.openPage(new PageVisit(next, 0), this.memory.isEmpty());
         } else if (shift) {
            this.openPage(new PageVisit(this.currentChapter, this.currentChapter.size() - 1), this.memory.isEmpty());
         }
      }
   }

   public void flipBackwards(boolean shift) {
      if (this.page > 0 && !shift) {
         this.openPage(new PageVisit(this.currentChapter, Math.max(this.page - 2, 0)), this.memory.isEmpty());
      } else if (shift) {
         if (this.page > 0) {
            this.openPage(new PageVisit(this.currentChapter, 0), this.memory.isEmpty());
         } else {
            WikiChapter prev = this.currentChapter.getPrev();
            if (prev != null) {
               this.openPage(new PageVisit(prev, 0), this.memory.isEmpty());
            }
         }
      } else {
         WikiChapter prev = this.currentChapter.getPrev();
         if (prev != null) {
            this.openPage(new PageVisit(prev, Math.max(0, this.evenOut(prev.size()) - 2)), this.memory.isEmpty());
         }
      }
   }

   public int pollHistory() {
      if (this.memory.isEmpty()) {
         return 0;
      } else {
         PageVisit visit = (PageVisit)this.memory.dequeue();
         if (!this.openPage(visit, false)) {
            return 0;
         } else {
            return visit.wasSearching() ? 2 : 1;
         }
      }
   }
}
