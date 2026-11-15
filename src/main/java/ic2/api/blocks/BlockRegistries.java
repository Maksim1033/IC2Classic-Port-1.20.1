package ic2.api.blocks;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;
import java.util.Set;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

public class BlockRegistries {
   static final Object2IntMap<Block> ORE_VALUES = new Object2IntOpenHashMap();
   static final Int2ObjectMap<ObjectSet<Block>> VALUES_TO_ORES = new Int2ObjectOpenHashMap();
   static final List<Runnable> LISTENERS = ObjectLists.synchronize(new ObjectArrayList());

   public static void registerListener(Runnable run) {
      LISTENERS.add(run);
   }

   public static void reload() {
      ORE_VALUES.clear();
      VALUES_TO_ORES.clear();
      LISTENERS.forEach(Runnable::run);
      ObjectIterator var0 = ORE_VALUES.object2IntEntrySet().iterator();

      while (var0.hasNext()) {
         Entry<Block> entry = (Entry<Block>)var0.next();
         ObjectSet<Block> sets = (ObjectSet<Block>)VALUES_TO_ORES.get(entry.getIntValue());
         if (sets == null) {
            sets = new ObjectLinkedOpenHashSet();
            VALUES_TO_ORES.put(entry.getIntValue(), sets);
         }

         sets.add((Block)entry.getKey());
      }
   }

   public static void registerOre(int value, TagKey<Block> tag) {
      ITagManager<Block> tags = ForgeRegistries.BLOCKS.tags();
      if (tags != null) {
         for (Block entry : tags.getTag(tag)) {
            ORE_VALUES.put(entry, value);
         }
      }
   }

   public static void registerOre(int value, Block... blocks) {
      for (Block block : blocks) {
         ORE_VALUES.put(block, value);
      }
   }

   public static Set<Block> getBlocksForOreValue(int value) {
      return ObjectSets.unmodifiable((ObjectSet)VALUES_TO_ORES.getOrDefault(value, ObjectSets.emptySet()));
   }

   public static int getOreValue(BlockState state) {
      return ORE_VALUES.getInt(state.getBlock());
   }

   public static int getOreValue(Block block) {
      return ORE_VALUES.getInt(block);
   }

   public static List<Block> getAllValueBlocks() {
      return new ObjectArrayList(ORE_VALUES.keySet());
   }
}
