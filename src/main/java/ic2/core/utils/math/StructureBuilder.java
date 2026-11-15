package ic2.core.utils.math;

import ic2.core.platform.registries.IC2Properties;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class StructureBuilder {
   LongSet finalPositions = new LongLinkedOpenHashSet();
   Level world;
   BlockPos defaultPos;
   MutableBlockPos pos = new MutableBlockPos();
   Direction facing;
   Long2ObjectMap<LevelChunk> chunks = new Long2ObjectOpenHashMap();
   Long2ObjectFunction<LevelChunk> getter = T -> this.world.getChunk(ChunkPos.getX(T), ChunkPos.getZ(T));

   public StructureBuilder(Level world, BlockPos start, Direction facing) {
      this.world = world;
      this.pos.set(start);
      this.defaultPos = BlockPos.of(start.asLong());
      this.facing = facing;
   }

   public StructureBuilder(Level world, int x, int y, int z, Direction facing) {
      this.world = world;
      this.pos.set(x, y, z);
      this.defaultPos = new BlockPos(x, y, z);
      this.facing = facing;
   }

   public long[] getPositions() {
      return this.finalPositions.toLongArray();
   }

   public Direction getFacing() {
      return this.facing;
   }

   private BlockPos mark(BlockPos pos) {
      this.finalPositions.add(pos.asLong());
      return pos;
   }

   public StructureBuilder addMark() {
      this.finalPositions.add(this.pos.asLong());
      return this;
   }

   public StructureBuilder removeMark() {
      this.finalPositions.remove(this.pos.asLong());
      return this;
   }

   public StructureBuilder resetPosition() {
      this.pos.set(this.defaultPos);
      return this;
   }

   public boolean isDefaultPos() {
      return this.pos.equals(this.defaultPos);
   }

   public StructureBuilder left() {
      return this.offset(this.facing.getCounterClockWise());
   }

   public StructureBuilder left(int steps) {
      return this.offset(this.facing.getCounterClockWise(), steps);
   }

   public StructureBuilder right() {
      return this.offset(this.facing.getClockWise());
   }

   public StructureBuilder right(int steps) {
      return this.offset(this.facing.getClockWise(), steps);
   }

   public StructureBuilder forward() {
      return this.offset(this.facing);
   }

   public StructureBuilder forward(int steps) {
      return this.offset(this.facing, steps);
   }

   public StructureBuilder back() {
      return this.offset(this.facing.getOpposite());
   }

   public StructureBuilder back(int steps) {
      return this.offset(this.facing.getOpposite(), steps);
   }

   public StructureBuilder up() {
      return this.offset(Direction.UP);
   }

   public StructureBuilder up(int steps) {
      return this.offset(Direction.UP, steps);
   }

   public StructureBuilder down() {
      return this.offset(Direction.DOWN);
   }

   public StructureBuilder down(int steps) {
      return this.offset(Direction.DOWN, steps);
   }

   public StructureBuilder move(Rotation rotation) {
      return this.move(rotation, 1);
   }

   public StructureBuilder move(Rotation rotation, int steps) {
      return this.offset(rotation.rotate(this.facing), steps);
   }

   public StructureBuilder set(BlockPos pos) {
      this.pos.set(pos);
      return this;
   }

   public StructureBuilder setOffset(BlockPos pos) {
      this.pos.set(this.defaultPos.getX() + pos.getX(), this.defaultPos.getY() + pos.getY(), this.defaultPos.getZ() + pos.getZ());
      return this;
   }

   public StructureBuilder offset(Direction facing) {
      return this.offset(facing, 1);
   }

   public StructureBuilder offset(Direction facing, int steps) {
      if (steps != 0 && facing != null) {
         this.pos.move(facing, steps);
      }

      return this;
   }

   public boolean isBlock(Block block) {
      return this.getChunk().getBlockState(this.mark(this.pos)).is(block);
   }

   public boolean isBlock(BlockState state) {
      return this.isBlock(state.getBlock());
   }

   public boolean isBlock(Predicate<Block> test) {
      return test.test(this.getChunk().getBlockState(this.mark(this.pos)).getBlock());
   }

   public boolean isBlock(TagKey<Block> test) {
      return this.getChunk().getBlockState(this.mark(this.pos)).is(test);
   }

   public boolean isState(BlockState state) {
      return this.getChunk().getBlockState(this.mark(this.pos)) == state;
   }

   public boolean isState(Predicate<BlockState> state) {
      return state.test(this.getChunk().getBlockState(this.mark(this.pos)));
   }

   public boolean isAir() {
      return this.getChunk().getBlockState(this.pos).isAir();
   }

   public boolean isWaterlogged() {
      BlockState state = this.getChunk().getBlockState(this.pos);
      if (state.hasProperty(BlockStateProperties.WATERLOGGED) && (Boolean)state.getValue(BlockStateProperties.WATERLOGGED)) {
         return true;
      } else {
         FluidState fluid = state.getFluidState();
         return fluid.isSource() && fluid.getType() == Fluids.WATER;
      }
   }

   public boolean isLavaLogged() {
      BlockState state = this.getChunk().getBlockState(this.pos);
      if (state.hasProperty(IC2Properties.LAVA_LOGGED) && (Boolean)state.getValue(IC2Properties.LAVA_LOGGED)) {
         return true;
      } else {
         FluidState fluid = state.getFluidState();
         return fluid.isSource() && fluid.getType() == Fluids.LAVA;
      }
   }

   public long getCurrentPos() {
      return this.mark(this.pos).asLong();
   }

   public BlockState getState() {
      return this.getChunk().getBlockState(this.mark(this.pos));
   }

   public BlockEntity getTile() {
      return this.getChunk().getBlockEntity(this.mark(this.pos));
   }

   public <T> T getTile(Class<T> clz) {
      BlockEntity tile = this.getTile();
      return clz.isInstance(tile) ? clz.cast(tile) : null;
   }

   public boolean cubicStructure(int width, int height, int depth, BooleanSupplier run) {
      int total = width * height * depth;
      int yOffset = width * depth;

      for (int i = 1; i < total; i++) {
         this.resetPosition().right(i % width).forward(i / width % depth).down(i / yOffset);
         if (!run.getAsBoolean()) {
            return false;
         }
      }

      return true;
   }

   public boolean circleSquareStructure(int width, int depth, BooleanSupplier run) {
      for (int i = 0; i < width; i++) {
         if (!run.getAsBoolean()) {
            return false;
         }

         this.right();
      }

      this.left();

      for (int i = 0; i < depth; i++) {
         if (!run.getAsBoolean()) {
            return false;
         }

         this.back();
      }

      this.forward();

      for (int i = 0; i < width; i++) {
         if (!run.getAsBoolean()) {
            return false;
         }

         this.left();
      }

      this.right();

      for (int i = 0; i < depth; i++) {
         if (!run.getAsBoolean()) {
            return false;
         }

         this.forward();
      }

      this.back();
      return true;
   }

   public boolean cubicStructure(int index, int width, int height, int depth) {
      this.resetPosition().right(index % width).forward(index / width % depth).down(index / (width * depth));
      return index < width * height * depth;
   }

   public boolean cubicStructure(int index, int width, int height, int depth, MutableBlockPos pos) {
      int x = index % width;
      int y = index / (width * depth);
      int z = index / width % depth;
      this.resetPosition().right(x).forward(z).down(y);
      pos.set(x, y, z);
      return index < width * height * depth;
   }

   public int findLength(int maxRadius, boolean reset, Direction dir, Predicate<StructureBuilder> builder) {
      this.resetPosition();
      int walk = 0;

      while (builder.test(this.offset(dir)) && walk < maxRadius) {
         walk++;
      }

      this.resetPosition();
      if (reset) {
         this.finalPositions.clear();
      }

      return walk;
   }

   public int findRadius(int maxRadius, boolean reset, Predicate<StructureBuilder> builder) {
      this.resetPosition();
      int walk = 0;

      while (builder.test(this.right()) && walk < maxRadius) {
         walk++;
      }

      this.resetPosition();
      walk = 0;

      while (builder.test(this.forward()) && walk < maxRadius) {
         walk++;
      }

      int var8 = Math.min(walk, walk);
      this.resetPosition();
      walk = 0;

      while (builder.test(this.down()) && walk < maxRadius) {
         walk++;
      }

      var8 = Math.min(walk, var8);
      this.resetPosition();
      if (reset) {
         this.finalPositions.clear();
      }

      return var8;
   }

   private LevelChunk getChunk() {
      return (LevelChunk)this.chunks.computeIfAbsent(ChunkPos.asLong(this.pos.getX() >> 4, this.pos.getZ() >> 4), this.getter);
   }
}
