package ic2.core.utils.helpers;

import ic2.core.platform.corehacks.mixins.server.ChunkMixin;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WorldRegion implements LevelReader {
   @Override
   public net.minecraft.world.flag.FeatureFlagSet enabledFeatures() {
      return this.world.enabledFeatures();
   }

   @Override
   public net.minecraft.core.RegistryAccess registryAccess() {
      return this.world.registryAccess();
   }

   protected final int chunkX;
   protected final int chunkZ;
   protected final int height;
   protected final Long2ObjectMap<LevelChunk> chunks;
   protected final Level world;
   protected final boolean loadChunks;
   protected final boolean offThread;

   public WorldRegion(Level worldIn, Box box, boolean loadChunks) {
      this(worldIn, box, loadChunks, false);
   }

   public WorldRegion(Level worldIn, Box box, boolean loadChunks, boolean offThread) {
      this.world = worldIn;
      this.chunkX = box.getMinX() >> 4;
      this.chunkZ = box.getMinZ() >> 4;
      int width = (box.getMaxX() >> 4) + 1 - this.chunkX;
      this.height = (box.getMaxZ() >> 4) + 1 - this.chunkZ;
      this.loadChunks = loadChunks;
      this.offThread = offThread;
      this.chunks = new Long2ObjectLinkedOpenHashMap();

      for (int x = 0; x < width; x++) {
         for (int z = 0; z < this.height; z++) {
            this.chunks.put(ChunkPos.asLong(this.chunkX + x, this.chunkZ + z), this.world.getChunk(this.chunkX + x, this.chunkZ + z));
         }
      }
   }

   public int getRawBrightness(BlockPos pos, int amount) {
      return this.world.getRawBrightness(pos, amount);
   }

   public ChunkAccess getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
      long pos = ChunkPos.asLong(x, z);
      LevelChunk chunk = (LevelChunk)this.chunks.get(pos);
      if (chunk == null && this.loadChunks && !this.offThread) {
         chunk = this.world.getChunk(x, z);
         this.chunks.put(pos, chunk);
      }

      return chunk;
   }

   public boolean hasChunk(int x, int z) {
      return this.loadChunks && !this.offThread ? true : this.chunks.containsKey(ChunkPos.asLong(x, z));
   }

   public BlockPos getHeightmapPos(Types heightMapType, BlockPos pos) {
      return new BlockPos(pos.getX(), this.getHeight(heightMapType, pos.getX(), pos.getZ()), pos.getZ());
   }

   public int getHeight(Types heightMapType, int x, int z) {
      ChunkAccess chunk = this.getChunk(x >> 4, z >> 4);
      return chunk == null ? 0 : chunk.getHeight(heightMapType, x & 15, z & 15) + 1;
   }

   public int getSkyDarken() {
      return this.world.getSkyDarken();
   }

   public WorldBorder getWorldBorder() {
      return this.world.getWorldBorder();
   }

   public boolean isClientSide() {
      return false;
   }

   public int getSeaLevel() {
      return this.world.getSeaLevel();
   }

   public DimensionType dimensionType() {
      return this.world.dimensionType();
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      ChunkAccess chunk = this.getChunk(pos);
      return chunk == null ? null : chunk.getBlockEntity(pos);
   }

   public BlockState getBlockState(BlockPos pos) {
      if (this.world.isOutsideBuildHeight(pos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         ChunkAccess chunk = this.getChunk(pos);
         return chunk == null ? Blocks.AIR.defaultBlockState() : chunk.getBlockState(pos);
      }
   }

   public FluidState getFluidState(BlockPos pos) {
      if (this.world.isOutsideBuildHeight(pos)) {
         return Fluids.EMPTY.defaultFluidState();
      } else {
         ChunkAccess chunk = this.getChunk(pos);
         return chunk == null ? Fluids.EMPTY.defaultFluidState() : chunk.getFluidState(pos);
      }
   }

   public List<VoxelShape> getEntityCollisions(Entity p_186427_, AABB p_186428_) {
      return this.world.getEntityCollisions(p_186427_, p_186428_);
   }

   @OnlyIn(Dist.CLIENT)
   public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
      return 1.0F;
   }

   public LevelLightEngine getLightEngine() {
      return this.world.getLightEngine();
   }

   public BiomeManager getBiomeManager() {
      return this.world.getBiomeManager();
   }

   public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
      return this.world.getNoiseBiome(x, y, z);
   }

   public int getBrightness(LightLayer type, BlockPos pos) {
      return this.world.getBrightness(type, pos);
   }

   public void scanBlockEntities(BiPredicate<BlockPos, BlockEntity> filter) {
      ObjectIterator var2 = this.chunks.values().iterator();

      while (var2.hasNext()) {
         LevelChunk chunk = (LevelChunk)var2.next();

         for (Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            if (!filter.test(entry.getKey(), entry.getValue())) {
               return;
            }
         }
      }
   }

   public boolean hasInvalidChunks() {
      ObjectIterator var1 = this.chunks.values().iterator();

      while (var1.hasNext()) {
         LevelChunk chunk = (LevelChunk)var1.next();
         if (!((ChunkMixin)chunk).isLoaded()) {
            return true;
         }
      }

      return false;
   }

   public boolean isBlockPresent(BlockPos pos) {
      return !this.world.isOutsideBuildHeight(pos) && this.hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
   }
}
