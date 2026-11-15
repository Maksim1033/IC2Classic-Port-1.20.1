package ic2.core.wiki.helper;

import ic2.core.wiki.base.book.WikiChapter;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class PageVisit {
   final WikiChapter chapter;
   final int page;
   boolean wasSearching = false;
   Component cache = null;

   public PageVisit(WikiChapter chapter, int page) {
      this.chapter = chapter;
      this.page = page;
   }

   public PageVisit withSearching(boolean value) {
      this.wasSearching = value;
      return this;
   }

   public boolean wasSearching() {
      return this.wasSearching;
   }

   public WikiChapter getChapter() {
      return this.chapter;
   }

   public int getPage() {
      return this.page;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.chapter, this.page);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof PageVisit visit && visit.chapter == this.chapter && visit.page == this.page) {
         return true;
      }

      return false;
   }

   public boolean isValid() {
      return this.chapter != null && this.page >= 0 && this.page < this.chapter.size();
   }

   @OnlyIn(Dist.CLIENT)
   public Component getSearch(Font font) {
      if (this.cache != null) {
         return this.cache;
      } else {
         MutableComponent comp = this.getHeader().copy().withStyle(ChatFormatting.BOLD);
         Component hover = Component.literal("Open: ")
            .append(this.getHeader().copy().setStyle(Style.EMPTY))
            .append("\n")
            .append("Chapter: ")
            .append(this.chapter.getHeader(0).copy().setStyle(Style.EMPTY))
            .append("\nPage: " + this.page);
         return this.cache = comp.withStyle(
            Style.EMPTY.withBold(true).withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.literal("Open: ").append(hover)))
         );
      }
   }

   public Component getHeader() {
      return this.chapter.getHeader(this.page);
   }
}
