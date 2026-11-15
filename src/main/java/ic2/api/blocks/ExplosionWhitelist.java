package ic2.api.blocks;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import net.minecraft.world.level.block.Block;

public class ExplosionWhitelist {
   static final Set<Block> WHITELIST = new ObjectOpenHashSet();

   public static void addWhitelist(Block... blocks) {
      WHITELIST.addAll(ObjectArrayList.wrap(blocks));
   }

   public static void removeWhitelist(Block... blocks) {
      ObjectArrayList.wrap(blocks).forEach(WHITELIST::remove);
   }

   public static boolean isWhitelisted(Block block) {
      return WHITELIST.contains(block);
   }
}
