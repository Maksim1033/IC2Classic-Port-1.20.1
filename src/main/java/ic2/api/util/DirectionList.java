package ic2.api.util;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public final class DirectionList implements Iterable<Direction>, Predicate<Direction> {
   static final Random RAND = new Random();
   static final DirectionList[] DIRECTIONS = generateArray();
   public static final DirectionList DOWN = ofFacing(Direction.DOWN);
   public static final DirectionList UP = ofFacing(Direction.UP);
   public static final DirectionList NORTH = ofFacing(Direction.NORTH);
   public static final DirectionList SOUTH = ofFacing(Direction.SOUTH);
   public static final DirectionList EAST = ofFacing(Direction.EAST);
   public static final DirectionList WEST = ofFacing(Direction.WEST);
   public static final DirectionList VERTICAL = DOWN.add(UP);
   public static final DirectionList HORIZONTAL = ofFacings(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
   public static final DirectionList POSITIVE = ofFacings(Direction.UP, Direction.SOUTH, Direction.EAST);
   public static final DirectionList NEGATIVE = ofFacings(Direction.DOWN, Direction.NORTH, Direction.WEST);
   public static final DirectionList X_AXIS = EAST.add(WEST);
   public static final DirectionList Z_AXIS = NORTH.add(SOUTH);
   public static final DirectionList XY_AXIS = X_AXIS.add(VERTICAL);
   public static final DirectionList YZ_AXIS = Z_AXIS.add(VERTICAL);
   public static final DirectionList P_CORNER = SOUTH.add(EAST);
   public static final DirectionList N_CORNER = NORTH.add(WEST);
   public static final DirectionList ALL = ofNumber(63);
   public static final DirectionList EMPTY = ofNumber(0);
   final int code;
   final int size;
   BlockPos offset;
   Direction[] array;
   Map<Direction, Direction> forwardHelper;
   Map<Direction, Direction> backwardHelper;

   private DirectionList() {
      throw new RuntimeException("NOT ALLOWED!");
   }

   private DirectionList(int index) {
      this.code = (byte)Mth.clamp(index, 0, 63);
      MutableBlockPos helper = new MutableBlockPos();
      ObjectList<Direction> list = new ObjectArrayList();

      for (int i = 0; i < 6; i++) {
         if ((this.code & 1 << i) != 0) {
            Direction dir = Direction.from3DDataValue(i);
            helper.move(dir);
            list.add(dir);
         }
      }

      this.offset = helper.immutable();
      this.size = list.size();
      this.array = (Direction[])list.toArray(new Direction[this.size]);
      if (this.size == 0) {
         this.forwardHelper = ImmutableMap.of();
      } else {
         Map<Direction, Direction> forward = new Object2ObjectOpenHashMap();
         Map<Direction, Direction> backward = new Object2ObjectOpenHashMap();

         for (int ix = 0; ix < this.array.length; ix++) {
            forward.put(this.array[ix], this.array[(ix + 1) % this.array.length]);
            backward.put(this.array[(ix + 1) % this.array.length], this.array[ix]);
         }

         this.forwardHelper = ImmutableMap.copyOf(forward);
         this.backwardHelper = ImmutableMap.copyOf(backward);
      }
   }

   public static DirectionList ofFacing(Direction facing) {
      return DIRECTIONS[1 << facing.get3DDataValue()];
   }

   public static DirectionList ofFacings(Direction... facings) {
      return DIRECTIONS[toNumber(facings)];
   }

   public static DirectionList ofFacings(Collection<Direction> facings) {
      return DIRECTIONS[toNumber(facings)];
   }

   public static DirectionList ofAxis(Axis axis) {
      int value = 0;

      for (Direction dir : ALL) {
         if (axis.test(dir)) {
            value |= 1 << dir.get3DDataValue();
         }
      }

      return DIRECTIONS[value];
   }

   public static DirectionList ofFlags(boolean... flags) {
      return DIRECTIONS[toNumber(flags)];
   }

   public static DirectionList ofNumber(int index) {
      return DIRECTIONS[Mth.clamp(index, 0, 63)];
   }

   public static MutableComponent getName(Direction dir) {
      return Component.translatable("misc.ic2.side." + dir.getName());
   }

   public DirectionList rotate(int amount) {
      int value = 0;

      for (int i = 0; i < 6; i++) {
         if ((this.code & 1 << i) != 0) {
            value |= 1 << Direction.from3DDataValue(i + amount).get3DDataValue();
         }
      }

      return DIRECTIONS[value & 63];
   }

   public DirectionList invert() {
      return DIRECTIONS[63 - this.code];
   }

   public DirectionList opposite() {
      int value = 0;

      for (int i = 0; i < 6; i++) {
         if ((this.code & 1 << i) != 0) {
            value |= 1 << Direction.from3DDataValue(i).getOpposite().get3DDataValue();
         }
      }

      return DIRECTIONS[value & 63];
   }

   public DirectionList add(Direction facing) {
      return DIRECTIONS[this.code | 1 << facing.get3DDataValue()];
   }

   public DirectionList add(DirectionList facings) {
      return DIRECTIONS[this.code | facings.code];
   }

   public DirectionList remove(Direction facing) {
      return DIRECTIONS[this.code & ~(1 << facing.get3DDataValue())];
   }

   public DirectionList remove(DirectionList facings) {
      return DIRECTIONS[this.code & ~facings.code];
   }

   public DirectionList set(Direction facing, boolean value) {
      return DIRECTIONS[value ? this.code | 1 << facing.get3DDataValue() : this.code & ~(1 << facing.get3DDataValue())];
   }

   public DirectionList keep(DirectionList facings) {
      return DIRECTIONS[this.code & facings.code];
   }

   public DirectionList flip(Direction dir) {
      return DIRECTIONS[this.code ^ 1 << dir.get3DDataValue()];
   }

   public boolean contains(Direction direction) {
      return (this.code & 1 << direction.get3DDataValue()) != 0;
   }

   public boolean contains(DirectionList facings) {
      return (this.code & facings.code) == facings.code;
   }

   public boolean containsAny(DirectionList facings) {
      return (this.code & facings.code) != 0;
   }

   public boolean notContains(Direction direction) {
      return (this.code & 1 << direction.get3DDataValue()) == 0;
   }

   public boolean containsNot(DirectionList facings) {
      return (this.code & facings.code) == 0;
   }

   public DirectionList invertFacing(Direction facing) {
      return this.contains(facing) ? this.remove(facing).add(facing.getOpposite()) : this;
   }

   public DirectionList replace(Direction oldFacing, Direction newFacing) {
      return this.contains(oldFacing) ? this.remove(oldFacing).add(newFacing) : this;
   }

   public Direction getNextFacing(Direction facing) {
      if (this.contains(facing)) {
         return this.forwardHelper.get(facing);
      } else {
         return this.code == 0 ? facing : this.array[0];
      }
   }

   public Direction getPrevFacing(Direction facing) {
      if (this.contains(facing)) {
         return this.backwardHelper.get(facing);
      } else {
         return this.code == 0 ? facing : this.array[this.array.length - 1];
      }
   }

   public BlockPos getOffset() {
      return this.offset;
   }

   public int getCode() {
      return this.code;
   }

   public int size() {
      return this.size;
   }

   public boolean isEmpty() {
      return this.code == 0;
   }

   public boolean isFull() {
      return this.code == 63;
   }

   @Override
   public String toString() {
      return ObjectArrayList.wrap(this.array).toString();
   }

   public EnumSet<Direction> toFacings() {
      return this.code == 0 ? EnumSet.noneOf(Direction.class) : EnumSet.copyOf(ObjectArrayList.wrap(this.array));
   }

   public Direction getRandomFacing() {
      return this.getRandomFacing(Direction.NORTH);
   }

   public Direction getRandomFacing(Direction defaultValue) {
      return this.code == 0 ? defaultValue : this.array[RAND.nextInt(this.size)];
   }

   public Direction getDefaultFacing() {
      return this.code == 0 ? Direction.NORTH : this.array[0];
   }

   public boolean[] toFlags() {
      boolean[] flags = new boolean[6];

      for (int i = 0; i < 6; i++) {
         flags[i] = (this.code & 1 << i) != 0;
      }

      return flags;
   }

   public static BlockState getNeighborState(BlockEntity tile, Direction dir) {
      return getNeighborState(tile.getLevel(), tile.getBlockPos(), dir);
   }

   public static BlockState getNeighborState(Level world, BlockPos pos, Direction dir) {
      pos = pos.relative(dir);
      return world.isLoaded(pos) ? world.getBlockState(pos) : Blocks.AIR.defaultBlockState();
   }

   public static BlockEntity getNeighborTile(BlockEntity tile, Direction dir) {
      return getNeighborTile(tile.getLevel(), tile.getBlockPos(), dir);
   }

   public static BlockEntity getNeighborTile(Level world, BlockPos pos, Direction dir) {
      pos = pos.relative(dir);
      return world.isLoaded(pos) ? world.getBlockEntity(pos) : null;
   }

   public static <T> LazyOptional<T> getNeighborCapability(BlockEntity tile, Direction dir, Capability<T> capability) {
      BlockEntity neighbor = getNeighborTile(tile, dir);
      return neighbor == null ? LazyOptional.empty() : neighbor.getCapability(capability, dir.getOpposite());
   }

   public static <T> LazyOptional<T> getNeighborCapability(Level world, BlockPos pos, Direction dir, Capability<T> capability) {
      BlockEntity neighbor = getNeighborTile(world, pos, dir);
      return neighbor == null ? LazyOptional.empty() : neighbor.getCapability(capability, dir.getOpposite());
   }

   public static <T> T getNeighborInterface(BlockEntity tile, Direction dir, Class<T> clazz) {
      BlockEntity neighbor = getNeighborTile(tile, dir);
      return clazz.isInstance(neighbor) ? clazz.cast(neighbor) : null;
   }

   public static <T> T getNeighborInterface(Level world, BlockPos pos, Direction dir, Class<T> clazz) {
      BlockEntity neighbor = getNeighborTile(world, pos, dir);
      return clazz.isInstance(neighbor) ? clazz.cast(neighbor) : null;
   }

   public static Rotation getRotation(Direction source, Direction other) {
      int index = source.get2DDataValue();
      int otherIndex = other.get2DDataValue();
      if (index == -1 || otherIndex == -1) {
         throw new IllegalStateException("Horizontal Only!");
      } else if (source == other) {
         return Rotation.NONE;
      } else if (source == other.getOpposite()) {
         return Rotation.CLOCKWISE_180;
      } else {
         return source == other.getClockWise() ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
      }
   }

   public static Direction rotateAround(Direction dir, Axis axis) {
      switch (axis) {
         case X:
            if (dir != Direction.WEST && dir != Direction.EAST) {
               return rotateX(dir);
            }

            return dir;
         case Y:
            if (dir != Direction.UP && dir != Direction.DOWN) {
               return rotateY(dir);
            }

            return dir;
         case Z:
            if (dir != Direction.NORTH && dir != Direction.SOUTH) {
               return rotateZ(dir);
            }

            return dir;
         default:
            throw new IllegalStateException("Unable to get CW facing for axis " + axis);
      }
   }

   public static Direction rotateY(Direction dir) {
      switch (dir) {
         case NORTH:
            return Direction.EAST;
         case EAST:
            return Direction.SOUTH;
         case SOUTH:
            return Direction.WEST;
         case WEST:
            return Direction.NORTH;
         default:
            throw new IllegalStateException("Unable to get Y-rotated facing of " + dir);
      }
   }

   private static Direction rotateX(Direction dir) {
      switch (dir) {
         case NORTH:
            return Direction.DOWN;
         case EAST:
         case WEST:
         default:
            throw new IllegalStateException("Unable to get X-rotated facing of " + dir);
         case SOUTH:
            return Direction.UP;
         case UP:
            return Direction.NORTH;
         case DOWN:
            return Direction.SOUTH;
      }
   }

   private static Direction rotateZ(Direction dir) {
      switch (dir) {
         case EAST:
            return Direction.DOWN;
         case SOUTH:
         default:
            throw new IllegalStateException("Unable to get Z-rotated facing of " + dir);
         case WEST:
            return Direction.UP;
         case UP:
            return Direction.EAST;
         case DOWN:
            return Direction.WEST;
      }
   }

   public static int toNumber(Direction... facings) {
      int value = 0;

      for (Direction facing : facings) {
         value |= 1 << facing.get3DDataValue();
      }

      return value & 63;
   }

   public static int toNumber(Collection<Direction> facings) {
      int value = 0;

      for (Direction facing : facings) {
         value |= 1 << facing.get3DDataValue();
      }

      return value & 63;
   }

   public static int toNumber(boolean... facings) {
      return (facings[0] ? 1 : 0)
         | (facings[1] ? 1 : 0) << 1
         | (facings[2] ? 1 : 0) << 2
         | (facings[3] ? 1 : 0) << 3
         | (facings[4] ? 1 : 0) << 4
         | (facings[5] ? 1 : 0) << 5;
   }

   public static boolean[] toFlags(Direction... facings) {
      boolean[] array = new boolean[6];

      for (Direction face : facings) {
         array[face.get3DDataValue()] = true;
      }

      return array;
   }

   public boolean test(Direction t) {
      return this.contains(t);
   }

   @Override
   public Iterator<Direction> iterator() {
      return new Iterator<Direction>() {
         int index = 0;

         @Override
         public boolean hasNext() {
            return this.index < DirectionList.this.size;
         }

         public Direction next() {
            return DirectionList.this.array[this.index++];
         }
      };
   }

   public Iterable<Direction> getRandomIterator() {
      return () -> new Iterator<Direction>() {
            Direction[] data = (Direction[])ObjectArrays.shuffle((Direction[])ObjectArrays.copy(DirectionList.this.array), DirectionList.RAND);
            int index = 0;

            @Override
            public boolean hasNext() {
               return this.index < this.data.length;
            }

            public Direction next() {
               return this.data[this.index++];
            }
         };
   }

   static DirectionList[] generateArray() {
      DirectionList[] rotations = new DirectionList[64];

      for (int i = 0; i < 64; i++) {
         rotations[i] = new DirectionList(i);
      }

      return rotations;
   }
}
