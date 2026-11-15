package ic2.core.platform.registries;

import ic2.api.blocks.DyeableMap;
import ic2.core.utils.collection.CollectionUtils;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public class ColorMaps {
   public static final DyeableMap CFOAM_BLOCKS = new DyeableMap();
   public static final DyeableMap CFOAM_STAIRS = new DyeableMap();
   public static final DyeableMap CFOAM_SLABS = new DyeableMap();
   public static final DyeableMap CFOAM_SURFACE = new DyeableMap();
   public static final DyeableMap CFOAM_PANELS = new DyeableMap();
   public static final DyeableMap CFOAM_WALLS = new DyeableMap();
   public static final DyeableMap CFOAM_WOOL = new DyeableMap();
   public static final DyeableMap CFOAM_CARPET = new DyeableMap();
   public static final Map<DyeColor, Item> PAINTER = CollectionUtils.<DyeColor, Item>createMap();
   static TagKey<Item>[] COLOR_TAGS = null;

   public static TagKey<Item> getTagForDye(DyeColor myColor) {
      if (COLOR_TAGS == null) {
         COLOR_TAGS = new TagKey[16];

         for (DyeColor color : DyeColor.values()) {
            COLOR_TAGS[color.getId()] = ItemTags.create(new ResourceLocation("forge", "dyes/" + color.getName()));
         }
      }

      return COLOR_TAGS[myColor.getId()];
   }
}
