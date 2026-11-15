package ic2.core.utils.math.geometry;

import ic2.api.util.DirectionList;
import ic2.core.utils.collection.IterableWrapper;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public class Box implements Iterable<BlockPos> {
   int minX;
   int minY;
   int minZ;
   int maxX;
   int maxY;
   int maxZ;

   public Box() {
      this(0, 0, 0, 0, 0, 0);
   }

   public Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      this.minX = Math.min(minX, maxX);
      this.minY = Math.min(minY, maxY);
      this.minZ = Math.min(minZ, maxZ);
      this.maxX = Math.max(minX, maxX);
      this.maxY = Math.max(minY, maxY);
      this.maxZ = Math.max(minZ, maxZ);
   }

   public static Box fromNBT(IntArrayTag nbt) {
      return fromArray(nbt.getAsIntArray());
   }

   public static Box relative(byte[] array) {
      if (array.length != 6) {
         throw new IllegalStateException("Array has to be size of 6");
      } else {
         return new Box(array[0], array[1], array[2], array[3], array[4], array[5]);
      }
   }

   public static Box fromArray(int[] array) {
      if (array.length != 6) {
         throw new IllegalStateException("Array has to be size of 6");
      } else {
         return new Box(array[0], array[1], array[2], array[3], array[4], array[5]);
      }
   }

   public static Box fromPos(BlockPos pos) {
      return fromPos(pos, pos.offset(1, 1, 1));
   }

   public static Box fromPos(BlockPos pos, boolean center) {
      return fromPos(pos, center ? pos : pos.offset(1, 1, 1));
   }

   public static Box fromPos(BlockPos from, BlockPos to) {
      return new Box(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
   }

   public static Box withRange(BlockPos pos, int range) {
      return fromPos(pos.offset(-range, -range, -range), pos.offset(range, range, range));
   }

   public static Box fromAABB(AABB box) {
      return new Box(
         (int)Math.floor(box.minX),
         (int)Math.floor(box.minY),
         (int)Math.floor(box.minZ),
         (int)Math.floor(box.maxX),
         (int)Math.floor(box.maxY),
         (int)Math.floor(box.maxZ)
      );
   }

   public static Box fromPosArray(long[] position) {
      int xMin = 2147483647;
      int yMin = 2147483647;
      int zMin = 2147483647;
      int xMax = -2147483648;
      int yMax = -2147483648;
      int zMax = -2147483648;

      for (long l : position) {
         int x = BlockPos.getX(l);
         int y = BlockPos.getY(l);
         int z = BlockPos.getZ(l);
         xMin = Math.min(x, xMin);
         yMin = Math.min(y, yMin);
         zMin = Math.min(z, zMin);
         xMax = Math.max(x, xMax);
         yMax = Math.max(y, yMax);
         zMax = Math.max(z, zMax);
      }

      return new Box(xMin, yMin, zMin, xMax, yMax, zMax);
   }

   public static Box fromPosArray(LongIterator iter) {
      int xMin = 2147483647;
      int yMin = 2147483647;
      int zMin = 2147483647;
      int xMax = -2147483648;
      int yMax = -2147483648;
      int zMax = -2147483648;

      while (iter.hasNext()) {
         long pos = iter.nextLong();
         int x = BlockPos.getX(pos);
         int y = BlockPos.getY(pos);
         int z = BlockPos.getZ(pos);
         xMin = Math.min(x, xMin);
         yMin = Math.min(y, yMin);
         zMin = Math.min(z, zMin);
         xMax = Math.max(x, xMax);
         yMax = Math.max(y, yMax);
         zMax = Math.max(z, zMax);
      }

      return new Box(xMin, yMin, zMin, xMax, yMax, zMax);
   }

   public static Box fromList(Iterable<BlockPos> set) {
      int xMin = 2147483647;
      int yMin = 2147483647;
      int zMin = 2147483647;
      int xMax = -2147483648;
      int yMax = -2147483648;
      int zMax = -2147483648;

      for (BlockPos pos : set) {
         xMin = Math.min(pos.getX(), xMin);
         yMin = Math.min(pos.getY(), yMin);
         zMin = Math.min(pos.getZ(), zMin);
         xMax = Math.max(pos.getX(), xMax);
         yMax = Math.max(pos.getY(), yMax);
         zMax = Math.max(pos.getZ(), zMax);
      }

      return new Box(xMin, yMin, zMin, xMax, yMax, zMax);
   }

   public Box copy() {
      return new Box(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
   }

   public AABB toAxis() {
      return new AABB((double)this.minX, (double)this.minY, (double)this.minZ, (double)this.maxX, (double)this.maxY, (double)this.maxZ);
   }

   public Box toInclusiveBox() {
      return new Box(this.minX, this.minY, this.minZ, this.maxX + 1, this.maxY + 1, this.maxZ + 1);
   }

   public AABB toInclusiveAxis() {
      return new AABB((double)this.minX, (double)this.minY, (double)this.minZ, (double)(this.maxX + 1), (double)(this.maxY + 1), (double)(this.maxZ + 1));
   }

   public Box expandSide(Axis axis, int amount) {
      return this.expand(DirectionList.ofAxis(axis).invert(), amount);
   }

   public Box expand(Axis axis, int amount) {
      return this.expand(DirectionList.ofAxis(axis), amount);
   }

   public Box expand(Direction dir, int amount) {
      return this.expand(DirectionList.ofFacing(dir), amount);
   }

   public Box expand(DirectionList dir, int amount) {
      this.minX = this.minX - (dir.contains(Direction.WEST) ? amount : 0);
      this.minY = this.minY - (dir.contains(Direction.DOWN) ? amount : 0);
      this.minZ = this.minZ - (dir.contains(Direction.NORTH) ? amount : 0);
      this.maxX = this.maxX + (dir.contains(Direction.EAST) ? amount : 0);
      this.maxY = this.maxY + (dir.contains(Direction.UP) ? amount : 0);
      this.maxZ = this.maxZ + (dir.contains(Direction.SOUTH) ? amount : 0);
      return this;
   }

   public Box setMaxValue(BlockPos pos) {
      this.maxX = Math.min(pos.getX(), this.maxX);
      this.maxY = Math.min(pos.getY(), this.maxY);
      this.maxZ = Math.min(pos.getZ(), this.maxZ);
      return this;
   }

   public Box setMinValue(BlockPos pos) {
      this.minX = Math.max(pos.getX(), this.minX);
      this.minY = Math.max(pos.getY(), this.minY);
      this.minZ = Math.max(pos.getZ(), this.minZ);
      return this;
   }

   public Box setValue(DirectionList dir, int value) {
      this.minX = dir.contains(Direction.WEST) ? value : this.minX;
      this.minY = dir.contains(Direction.DOWN) ? value : this.minY;
      this.minZ = dir.contains(Direction.NORTH) ? value : this.minZ;
      this.maxX = dir.contains(Direction.EAST) ? value : this.maxX;
      this.maxY = dir.contains(Direction.UP) ? value : this.maxY;
      this.maxZ = dir.contains(Direction.SOUTH) ? value : this.maxZ;
      if (this.maxX < this.minX) {
         value = this.minX;
         this.minX = this.maxX;
         this.maxX = value;
      }

      if (this.maxY < this.minY) {
         value = this.minY;
         this.minY = this.maxY;
         this.maxY = value;
      }

      if (this.maxZ < this.minZ) {
         value = this.minZ;
         this.minZ = this.maxZ;
         this.maxZ = value;
      }

      return this;
   }

   public Box offset(Vec3i pos) {
      return this.offset(pos.getX(), pos.getY(), pos.getZ());
   }

   public Box offset(Direction dir) {
      return this.offset(dir.getNormal());
   }

   public Box offset(int x, int y, int z) {
      this.minX += x;
      this.minY += y;
      this.minZ += z;
      this.maxX += x;
      this.maxY += y;
      this.maxZ += z;
      return this;
   }

   public int size() {
      int x = Math.abs(this.maxX - this.minX) + 1;
      int y = Math.abs(this.maxY - this.minY) + 1;
      int z = Math.abs(this.maxZ - this.minZ) + 1;
      return x * y * z;
   }

   @Override
   public String toString() {
      return "BoundingBox[minX="
         + this.minX
         + ", minY="
         + this.minY
         + ", minZ="
         + this.minZ
         + ", maxX="
         + this.maxX
         + ", maxY="
         + this.maxY
         + ", maxZ="
         + this.maxZ
         + "]";
   }

   public int get(Direction side) {
      switch (side) {
         case DOWN:
            return this.minY;
         case UP:
            return this.maxY;
         case EAST:
            return this.minX;
         case WEST:
            return this.maxX;
         case NORTH:
            return this.minZ;
         case SOUTH:
            return this.maxZ;
         default:
            return 0;
      }
   }

   public int get(Axis axis) {
      switch (axis) {
         case X:
            return this.getWidth();
         case Y:
            return this.getHeight();
         case Z:
            return this.getDepth();
         default:
            return 0;
      }
   }

   public int getMin(Axis axis) {
      switch (axis) {
         case X:
            return this.minX;
         case Y:
            return this.minY;
         case Z:
            return this.minZ;
         default:
            return 0;
      }
   }

   public int getMax(Axis axis) {
      switch (axis) {
         case X:
            return this.maxX;
         case Y:
            return this.maxY;
         case Z:
            return this.maxZ;
         default:
            return 0;
      }
   }

   public int getMinX() {
      return this.minX;
   }

   public int getMinY() {
      return this.minY;
   }

   public int getMinZ() {
      return this.minZ;
   }

   public BlockPos getMin() {
      return new BlockPos(this.minX, this.minY, this.minZ);
   }

   public int getMaxX() {
      return this.maxX;
   }

   public int getMaxY() {
      return this.maxY;
   }

   public int getMaxZ() {
      return this.maxZ;
   }

   public BlockPos getMax() {
      return new BlockPos(this.maxX, this.maxY, this.maxZ);
   }

   public BlockPos get(int x, int y, int z) {
      return new BlockPos(this.minX + x, this.minY + y, this.minZ + z);
   }

   public BlockPos getCenter() {
      return new BlockPos(this.minX + (this.maxX - this.minX) / 2, this.minY + (this.maxY - this.minY) / 2, this.minZ + (this.maxZ - this.minZ) / 2);
   }

   public int getWidth() {
      return Math.abs(this.maxX - this.minX);
   }

   public int getHeight() {
      return Math.abs(this.maxY - this.minY);
   }

   public int getDepth() {
      return Math.abs(this.maxZ - this.minZ);
   }

   public boolean isCube(int size) {
      return this.getWidth() > size && this.getHeight() > size && this.getDepth() > size;
   }

   public int hollowSize() {
      this.expand(DirectionList.ALL, -1);
      int smallerSize = this.size();
      this.expand(DirectionList.ALL, 1);
      return this.size() - smallerSize;
   }

   public boolean intersectsWith(BlockPos pos) {
      return pos.getX() >= this.minX
         && pos.getX() <= this.maxX
         && pos.getY() >= this.minY
         && pos.getY() <= this.maxY
         && pos.getZ() >= this.minZ
         && pos.getZ() <= this.maxZ;
   }

   public boolean intersectsWith(BlockEntity tile) {
      return this.intersectsWith(tile.getBlockPos());
   }

   public boolean intersectsWith(Entity entity) {
      return this.intersectsWith(entity.blockPosition());
   }

   public boolean intersectsWith(Box other) {
      return (other.minX >= this.minX && other.minX <= this.maxX || other.maxX >= this.minX && other.maxX <= this.maxX)
         && (other.minY >= this.minY && other.minY <= this.maxY || other.maxY >= this.minY && other.maxY <= this.maxY)
         && (other.minZ >= this.minZ && other.minZ <= this.maxZ || other.maxZ >= this.minZ && other.maxZ <= this.maxZ);
   }

   public boolean isInSingleChunk() {
      return this.minX >> 4 == this.maxX >> 4 && this.minZ >> 4 == this.maxZ >> 4;
   }

   public boolean isLoaded(Level world) {
      int xMin = this.minX >> 4;

      for (int xMax = this.maxX >> 4; xMin <= xMax; xMin++) {
         int zMin = this.minZ >> 4;

         for (int zMax = this.maxZ >> 4; zMin <= zMax; zMin++) {
            if (!world.hasChunk(xMin, zMin)) {
               return false;
            }
         }
      }

      return true;
   }

   public Direction getBorderSide(BlockPos pos) {
      if (this.isEdge(pos)) {
         return null;
      } else if (pos.getX() == this.minX) {
         return Direction.WEST;
      } else if (pos.getX() == this.maxX) {
         return Direction.EAST;
      } else if (pos.getY() == this.minY) {
         return Direction.DOWN;
      } else if (pos.getY() == this.maxY) {
         return Direction.UP;
      } else if (pos.getX() == this.minZ) {
         return Direction.NORTH;
      } else {
         return pos.getX() == this.maxZ ? Direction.SOUTH : null;
      }
   }

   public boolean isBorder(BlockPos pos) {
      return pos.getX() == this.minX
         || pos.getX() == this.maxX
         || pos.getY() == this.minY
         || pos.getY() == this.maxY
         || pos.getZ() == this.minZ
         || pos.getZ() == this.maxZ;
   }

   public boolean isEdge(BlockPos pos) {
      if (pos.getX() != this.minX && pos.getX() != this.maxX) {
         if (pos.getY() == this.minY || pos.getY() == this.maxY) {
            return pos.getX() == this.minX || pos.getX() == this.maxX || pos.getZ() == this.minZ || pos.getZ() == this.maxZ;
         } else {
            return pos.getZ() != this.minZ && pos.getZ() != this.maxZ
               ? false
               : pos.getX() == this.minX || pos.getX() == this.maxX || pos.getY() == this.minY || pos.getY() == this.maxY;
         }
      } else {
         return pos.getY() == this.minY || pos.getY() == this.maxY || pos.getZ() == this.minZ || pos.getZ() == this.maxZ;
      }
   }

   public boolean isCorner(BlockPos pos) {
      return (pos.getX() == this.minX || pos.getX() == this.maxX)
         && (pos.getY() == this.minY || pos.getY() == this.maxY)
         && (pos.getZ() == this.minZ || pos.getZ() == this.maxZ);
   }

   public IntArrayTag save() {
      return new IntArrayTag(new int[]{this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ});
   }

   public ByteArrayTag saveRelative() {
      return new ByteArrayTag(new byte[]{(byte)this.minX, (byte)this.minY, (byte)this.minZ, (byte)this.maxX, (byte)this.maxY, (byte)this.maxZ});
   }

   public long[] asLongArray() {
      long[] data = new long[this.size()];
      int i = 0;

      for (int y = this.minY; y <= this.maxY; y++) {
         for (int z = this.minZ; z <= this.maxZ; z++) {
            for (int x = this.minX; x <= this.maxX; x++) {
               data[i++] = BlockPos.asLong(x, y, z);
            }
         }
      }

      return data;
   }

   public long[] asLongArray(BlockPos offset) {
      this.offset(offset);
      long[] data = new long[this.size() - 1];
      int i = 0;

      for (int y = this.minY; y <= this.maxY; y++) {
         for (int z = this.minZ; z <= this.maxZ; z++) {
            for (int x = this.minX; x <= this.maxX; x++) {
               if (x != offset.getX() || y != offset.getY() || z != offset.getZ()) {
                  data[i++] = BlockPos.asLong(x, y, z);
               }
            }
         }
      }

      this.offset(-offset.getX(), -offset.getY(), -offset.getZ());
      return data;
   }

   public long[] asHollowArray() {
      long[] data = new long[this.hollowSize()];
      int i = 0;
      int add = this.maxX - this.minX - 1;

      for (int y = this.minY; y <= this.maxY; y++) {
         for (int z = this.minZ; z <= this.maxZ; z++) {
            for (int x = this.minX; x <= this.maxX; x++) {
               data[i++] = BlockPos.asLong(x, y, z);
               if (z != this.minZ && z != this.maxZ && y != this.minY && y != this.maxY && x == this.minX) {
                  x += add;
               }
            }
         }
      }

      return data;
   }

   public long[] asHollowArray(BlockPos offset) {
      this.offset(offset);
      long[] data = new long[this.hollowSize() - 1];
      int i = 0;

      for (int z = this.minZ; z <= this.maxZ; z++) {
         for (int y = this.minY; y <= this.maxY; y++) {
            for (int x = this.minX; x <= this.maxX; x++) {
               if ((x != offset.getX() || y != offset.getY() || z != offset.getZ())
                  && (x == this.minX || x == this.maxX || y == this.minY || y == this.maxY || z == this.minZ || z == this.maxZ)) {
                  data[i++] = BlockPos.asLong(x, y, z);
               }
            }
         }
      }

      this.offset(-offset.getX(), -offset.getY(), -offset.getZ());
      return data;
   }

   @Override
   public Iterator<BlockPos> iterator() {
      return new Iterator<BlockPos>() {
         int x = Box.this.minX;
         int y = Box.this.minY;
         int z = Box.this.minZ;
         boolean hasNext = true;
         MutableBlockPos pos = new MutableBlockPos(this.x, this.y, this.z);

         @Override
         public boolean hasNext() {
            return this.hasNext;
         }

         public BlockPos next() {
            this.pos.set(this.x, this.y, this.z);
            if (++this.x > Box.this.maxX) {
               this.x = Box.this.minX;
               if (++this.z > Box.this.maxZ) {
                  this.z = Box.this.minZ;
                  if (++this.y > Box.this.maxY) {
                     this.y = Box.this.minY;
                     this.hasNext = false;
                  }
               }
            }

            return this.pos;
         }
      };
   }

   public Iterator<BlockPos> asInfiniteIterator() {
      return new Iterator<BlockPos>() {
         int x = Box.this.minX;
         int y = Box.this.minY;
         int z = Box.this.minZ;
         MutableBlockPos pos = new MutableBlockPos(this.x, this.y, this.z);

         @Override
         public boolean hasNext() {
            return true;
         }

         public BlockPos next() {
            this.pos.set(this.x, this.y, this.z);
            if (++this.x > Box.this.maxX) {
               this.x = Box.this.minX;
               if (++this.z > Box.this.maxZ) {
                  this.z = Box.this.minZ;
                  if (++this.y > Box.this.maxY) {
                     this.y = Box.this.minY;
                  }
               }
            }

            return this.pos;
         }
      };
   }

   public Iterable<BlockPos> getHollowHorizontalIterator() {
      return IterableWrapper.wrap(new Iterator<BlockPos>() {
         boolean hasNext = true;
         int add = Box.this.maxX - Box.this.minX - 1;
         int x = Box.this.minX;
         int y = Box.this.minY;
         int z = Box.this.minZ;
         MutableBlockPos pos = new MutableBlockPos();

         @Override
         public boolean hasNext() {
            return this.hasNext;
         }

         public BlockPos next() {
            this.pos.set(this.x, this.y, this.z);
            if (this.z != Box.this.minZ && this.z != Box.this.maxZ && this.x == Box.this.minX) {
               this.x = this.x + this.add;
            }

            if (++this.x > Box.this.maxX) {
               this.x = Box.this.minX;
               if (++this.z > Box.this.maxZ) {
                  this.z = Box.this.minZ;
                  this.hasNext = false;
               }
            }

            return this.pos;
         }
      });
   }

   public Iterable<BlockPos> getHollowIterator() {
      return IterableWrapper.wrap(new Iterator<BlockPos>() {
         boolean hasNext = true;
         int add = Box.this.maxX - Box.this.minX - 1;
         int x = Box.this.minX;
         int y = Box.this.minY;
         int z = Box.this.minZ;
         MutableBlockPos pos = new MutableBlockPos();

         @Override
         public boolean hasNext() {
            return this.hasNext;
         }

         public BlockPos next() {
            this.pos.set(this.x, this.y, this.z);
            if (this.z != Box.this.minZ && this.z != Box.this.maxZ && this.y != Box.this.minY && this.y != Box.this.maxY && this.x == Box.this.minX) {
               this.x = this.x + this.add;
            }

            if (++this.x > Box.this.maxX) {
               this.x = Box.this.minX;
               if (++this.z > Box.this.maxZ) {
                  this.z = Box.this.minZ;
                  if (++this.y > Box.this.maxY) {
                     this.y = Box.this.minY;
                     this.hasNext = false;
                  }
               }
            }

            return this.pos;
         }
      });
   }
}
