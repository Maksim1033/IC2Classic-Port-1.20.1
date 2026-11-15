package ic2.core.wiki.helper;

import ic2.core.wiki.base.book.WikiChapter;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;

public class BookMark {
   PageVisit location;
   int color;
   Component name;
   boolean permanent = false;

   public BookMark(PageVisit location, int color, Component name) {
      this.location = location;
      this.color = color;
      this.name = name;
   }

   public boolean isPermanent() {
      return this.permanent;
   }

   public int getColor() {
      return this.color;
   }

   public PageVisit getLocation() {
      return this.location;
   }

   public Component getName() {
      return this.name;
   }

   public BookMark setPermanent() {
      this.permanent = true;
      return this;
   }

   public CompoundTag save() {
      CompoundTag nbt = new CompoundTag();
      nbt.putString("page", this.location.getChapter().getId().toString());
      nbt.putInt("pageIndex", this.location.getPage());
      nbt.putInt("color", this.color);
      nbt.putString("name", Serializer.toJson(this.name));
      return nbt;
   }

   public static BookMark read(CompoundTag nbt, Map<ResourceLocation, WikiChapter> registry) {
      WikiChapter chapter = registry.get(new ResourceLocation(nbt.getString("page")));
      return chapter == null
         ? null
         : new BookMark(new PageVisit(chapter, nbt.getInt("pageIndex")), nbt.getInt("color"), Serializer.fromJson(nbt.getString("name")));
   }
}
