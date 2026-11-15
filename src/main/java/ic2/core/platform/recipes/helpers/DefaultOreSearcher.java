package ic2.core.platform.recipes.helpers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import ic2.api.blocks.BlockRegistries;
import ic2.core.utils.collection.CollectionUtils;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags.Blocks;

public class DefaultOreSearcher implements Runnable {
   public static final Set<TagKey<Block>> IGNORED = CollectionUtils.<TagKey<Block>>createSet();
   final Set<TagKey<Block>> TEMP_IGNORED = CollectionUtils.<TagKey<Block>>createSet();

   @Override
   public void run() {
      this.TEMP_IGNORED.clear();
      this.add(BlockTags.COPPER_ORES, 3);
      this.add(TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "ores/tin")), 3);
      this.add(TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "ores/silver")), 5);
      this.add(TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "ores/uranium")), 6);
      this.add(TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "ores/aluminium")), 4);
      this.add(Blocks.ORES_COAL, 2);
      this.add(Blocks.ORES_DIAMOND, 6);
      this.add(Blocks.ORES_EMERALD, 6);
      this.add(Blocks.ORES_NETHERITE_SCRAP, 6);
      this.add(Blocks.ORES_GOLD, 5);
      this.add(Blocks.ORES_IRON, 3);
      this.add(Blocks.ORES_LAPIS, 4);
      this.add(Blocks.ORES_QUARTZ, 4);
      this.add(Blocks.ORES_REDSTONE, 5);
      BuiltInRegistries.BLOCK.getTagNames().forEach(T -> {
         if (T.location().getPath().startsWith("ores/")) {
            this.add(T, 1);
         }
      });
   }

   public void add(TagKey<Block> location, int level) {
      if (!IGNORED.contains(location) && this.TEMP_IGNORED.add(location)) {
         BlockRegistries.registerOre(level, location);
      }
   }
}
