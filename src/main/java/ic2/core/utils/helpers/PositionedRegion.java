package ic2.core.utils.helpers;

import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class PositionedRegion {
   int chunkX;
   int chunkY;
   int chunkZ;
   LevelChunkSection section;

   public PositionedRegion(ChunkPos pos, int chunkY, LevelChunkSection section) {
      this.chunkX = pos.x;
      this.chunkY = chunkY;
      this.chunkZ = pos.z;
      this.section = section;
   }

   public PositionedRegion(int chunkX, int chunkY, int chunkZ, LevelChunkSection section) {
      this.chunkX = chunkX;
      this.chunkY = chunkY;
      this.chunkZ = chunkZ;
      this.section = section;
   }

   public static List<PositionedRegion> create(Level level, Box box) {
      int chunkX = box.getMinX() >> 4;
      int chunkZ = box.getMinZ() >> 4;
      int width = (box.getMaxX() >> 4) + 1 - chunkX;
      int height = (box.getMaxZ() >> 4) + 1 - chunkZ;
      List<PositionedRegion> regions = CollectionUtils.<PositionedRegion>createList();

      for (int x = 0; x < width; x++) {
         for (int z = 0; z < height; z++) {
            LevelChunk chunk = level.getChunk(chunkX + x, chunkZ + z);
            LevelChunkSection[] sections = chunk.getSections();
            for (int i = 0; i < sections.length; i++) {
               LevelChunkSection section = sections[i];
               if (section != null && !section.hasOnlyAir()) {
                  regions.add(new PositionedRegion(chunkX + x, chunk.getSectionYFromSectionIndex(i), chunkZ + z, section));
               }
            }
         }
      }

      return regions;
   }

   public static List<PositionedRegion> create(LevelChunk chunk) {
      List<PositionedRegion> regions = CollectionUtils.<PositionedRegion>createList();
      LevelChunkSection[] sections = chunk.getSections();
      for (int i = 0; i < sections.length; i++) {
         LevelChunkSection section = sections[i];
         if (section != null && !section.hasOnlyAir()) {
            regions.add(new PositionedRegion(chunk.getPos(), chunk.getSectionYFromSectionIndex(i), section));
         }
      }

      return regions;
   }

   public Map<BlockState, LongList> getStatePositions() {
      Map<BlockState, LongList> maps = CollectionUtils.<BlockState, LongList>createLinkedMap();
      int posX = this.chunkX << 4;
      int posY = this.chunkY << 4;
      int posZ = this.chunkZ << 4;

      for (int x = 0; x < 16; x++) {
         for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
               maps.computeIfAbsent(this.section.getBlockState(x, y, z), this::create).add(BlockPos.asLong(x + posX, y + posY, z + posZ));
            }
         }
      }

      return maps;
   }

   public Map<Block, LongList> getBlockPositions() {
      Map<Block, LongList> maps = CollectionUtils.<Block, LongList>createLinkedMap();
      int posX = this.chunkX << 4;
      int posY = this.chunkY << 4;
      int posZ = this.chunkZ << 4;

      for (int x = 0; x < 16; x++) {
         for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
               maps.computeIfAbsent(this.section.getBlockState(x, y, z).getBlock(), this::create).add(BlockPos.asLong(x + posX, y + posY, z + posZ));
            }
         }
      }

      return maps;
   }

   private LongList create(Block ignored) {
      return new LongArrayList();
   }

   private LongList create(BlockState ignored) {
      return new LongArrayList();
   }
}
