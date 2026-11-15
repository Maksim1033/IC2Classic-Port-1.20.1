package ic2.core.block.machines.logic.scanner;

import ic2.core.block.rendering.world.impl.BlockHighlighter;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ScannerHighlighter implements BlockHighlighter.IHighlightController {
   LongSet present = new LongOpenHashSet();
   BlockEntity tile;

   public ScannerHighlighter(BlockEntity tile) {
      this.tile = tile;
   }

   public void clearOrSet(LongList list, int color) {
      if (!this.present.isEmpty()) {
         this.present.clear();
      } else {
         this.present.addAll(list);
         int i = 0;

         for (int m = list.size(); i < m; i++) {
            BlockHighlighter.INSTANCE.addHighlight(BlockPos.of(list.getLong(i)), 5, color, false, this);
         }
      }
   }

   @Override
   public boolean keepAlive(BlockHighlighter.Highlighter owner) {
      if (this.present.contains(owner.pos.asLong()) && !this.tile.isRemoved()) {
         owner.ticksLeft = 2;
         return true;
      } else {
         return false;
      }
   }
}
