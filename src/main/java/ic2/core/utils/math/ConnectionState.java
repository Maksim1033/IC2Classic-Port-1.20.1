package ic2.core.utils.math;

import ic2.api.util.DirectionList;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;

public final class ConnectionState {
   static final ConnectionState[] STATES = createArray();
   int code;
   int size;
   ConnectionState.Connection[] array;

   private ConnectionState() {
      throw new RuntimeException("NOT ALLOWED!");
   }

   private ConnectionState(int code) {
      this.code = Mth.clamp(code, 0, 15);
      ObjectList<ConnectionState.Connection> list = CollectionUtils.createList();

      for (int i = 0; i < 4; i++) {
         if ((code & 1 << i) != 0) {
            list.add(ConnectionState.Connection.byIndex(i));
         }
      }

      this.size = list.size();
      this.array = (ConnectionState.Connection[])list.toArray(ConnectionState.Connection[]::new);
   }

   public static ConnectionState fromList(DirectionList list, Direction facing) {
      int index = 0;

      for (Direction dir : list.remove(DirectionList.ofAxis(facing.getAxis()))) {
         index |= 1 << ConnectionState.Connection.byFacing(dir, facing).getIndex();
      }

      return STATES[index];
   }

   public static ConnectionState fromList(DirectionList list, Axis axis) {
      int index = 0;

      for (Direction dir : list.remove(DirectionList.ofAxis(axis))) {
         index |= 1 << ConnectionState.Connection.byFacing(dir, axis).getIndex();
      }

      return STATES[index];
   }

   public static ConnectionState fromNumber(int index) {
      return STATES[index];
   }

   public int size() {
      return this.size;
   }

   public int getCode() {
      return this.code;
   }

   public ConnectionState inverse() {
      return STATES[15 - this.code];
   }

   public ConnectionState opposite() {
      int value = 0;

      for (ConnectionState.Connection con : this.array) {
         value |= 1 << con.getOpposite().getIndex();
      }

      return STATES[value];
   }

   @Override
   public String toString() {
      return "ConnectionState: " + ObjectArrayList.wrap(this.array);
   }

   public String getTypeName() {
      switch (this.size) {
         case 0:
            return "full";
         case 1:
            return "end_" + this.array[0].getName();
         case 2:
            return this.array[0].getOpposite() == this.array[1]
               ? (this.array[0].isVertical() ? "straight_vertical" : "straight_horizontal")
               : this.array[0].getCornerName(this.array[1]);
         case 3:
            return "side_" + STATES[15 - this.code].array[0].getName();
         default:
            return "empty";
      }
   }

   public int getIndex() {
      switch (this.size) {
         case 1:
            return this.array[0].getIndex();
         case 2:
            return this.array[0].getOpposite() == this.array[1] ? (this.array[0].isVertical() ? 0 : 1) : this.array[0].getCornerIndex(this.array[1]);
         case 3:
            return STATES[15 - this.code].array[0].getIndex();
         default:
            return 0;
      }
   }

   public float[] get3x3UVs() {
      switch (this.size) {
         case 1:
            return this.array[0].getUVs();
         case 2:
            return this.array[0].getOpposite() == this.array[1] ? this.array[0].getUVs() : this.array[0].getCornerUVs(this.array[1]);
         case 3:
            return STATES[15 - this.code].array[0].getUVs();
         default:
            return new float[]{0.0F, 0.0F, 16.0F, 16.0F};
      }
   }

   static ConnectionState[] createArray() {
      ConnectionState[] states = new ConnectionState[16];

      for (int i = 0; i < 16; i++) {
         states[i] = new ConnectionState(i);
      }

      return states;
   }

   public static float[] createQuads(float[][][] uvs, Direction side, int offset, int width, int height, int depth) {
      int x = --offset % width;
      int y = offset / (width * depth) % height;
      int z = height - 1 - offset / width % depth;
      switch (side) {
         case DOWN:
            return uvs[x][depth - 1 - z];
         case UP:
            return uvs[x][z];
         case NORTH:
            return uvs[width - 1 - x][y];
         case SOUTH:
            return uvs[x][y];
         case EAST:
            return uvs[depth - 1 - z][y];
         case WEST:
            return uvs[z][y];
         default:
            return new float[]{0.0F, 0.0F, 16.0F, 16.0F};
      }
   }

   public static float[][][] createTextureCutting(int xSize, int ySize) {
      float width = 16.0F / (float)xSize;
      float height = 16.0F / (float)ySize;
      float[][][] array = new float[xSize][ySize][4];

      for (int x = 0; x < xSize; x++) {
         for (int y = 0; y < ySize; y++) {
            array[x][y] = new float[]{(float)x * width, (float)y * height, (float)(x + 1) * width, (float)(y + 1) * height};
         }
      }

      return array;
   }

   public static enum Connection implements CollectionUtils.IIndexEnum {
      UP(0, 2, "bottom"),
      EAST(1, 3, "left"),
      DOWN(2, 0, "top"),
      WEST(3, 1, "right");

      static final ConnectionState.Connection[] MAPPED = CollectionUtils.createSortedArray(values());
      final int index;
      final int opposite;
      final String name;

      private Connection(int index, int opposite, String name) {
         this.index = index;
         this.opposite = opposite;
         this.name = name;
      }

      public ConnectionState.Connection getOpposite() {
         return MAPPED[this.opposite];
      }

      public String getCornerName(ConnectionState.Connection other) {
         if (other.getIndex() != this.index && other.getIndex() != this.opposite) {
            return "corner_" + (this.isVertical() ? this.name + "_" + other.getName() : other.getName() + "_" + this.name);
         } else {
            throw new IllegalStateException("Illegal State");
         }
      }

      public int getCornerIndex(ConnectionState.Connection other) {
         if (other.getIndex() == this.index || other.getIndex() == this.opposite) {
            throw new IllegalStateException("Illegal State");
         } else if (this.isVertical()) {
            return other == WEST ? (this == UP ? 3 : 2) : (this == UP ? 0 : 1);
         } else {
            return this == WEST ? (other == UP ? 3 : 2) : (other == UP ? 0 : 1);
         }
      }

      public boolean isVertical() {
         return this.index == 0 || this.index == 2;
      }

      @Override
      public int getIndex() {
         return this.index;
      }

      @Override
      public String toString() {
         return this.name;
      }

      public float[] getCornerUVs(ConnectionState.Connection other) {
         if (!this.isVertical() && other.isVertical()) {
            return other.getCornerUVs(this);
         } else {
            float[] result = new float[4];
            switch (other) {
               case EAST:
                  result[0] = 0.0F;
                  result[2] = 5.3333335F;
                  break;
               case WEST:
                  result[0] = 10.666667F;
                  result[2] = 16.0F;
                  break;
               default:
                  result[0] = 0.0F;
                  result[2] = 16.0F;
            }

            switch (this) {
               case DOWN:
                  result[1] = 0.0F;
                  result[3] = 5.3333335F;
                  break;
               case UP:
                  result[1] = 10.666667F;
                  result[3] = 16.0F;
                  break;
               default:
                  result[0] = 0.0F;
                  result[2] = 16.0F;
            }

            return result;
         }
      }

      public float[] getUVs() {
         switch (this) {
            case EAST:
               return new float[]{10.666667F, 5.3333335F, 16.0F, 10.666667F};
            case WEST:
               return new float[]{0.0F, 5.3333335F, 5.3333335F, 10.666667F};
            case DOWN:
               return new float[]{5.3333335F, 10.666667F, 10.666667F, 16.0F};
            case UP:
               return new float[]{5.3333335F, 0.0F, 10.666667F, 5.3333335F};
            default:
               throw new IllegalStateException();
         }
      }

      public String getName() {
         return this.name;
      }

      public static ConnectionState.Connection byFacing(Direction dir, Direction facing) {
         switch (facing.getAxis()) {
            case X:
               switch (dir) {
                  case DOWN:
                     return DOWN;
                  case UP:
                     return UP;
                  case NORTH:
                     return facing.getAxisDirection() == AxisDirection.POSITIVE ? EAST : WEST;
                  case SOUTH:
                     return facing.getAxisDirection() == AxisDirection.POSITIVE ? WEST : EAST;
                  default:
                     throw new IllegalStateException("Not a Valid Facing");
               }
            case Y:
               return MAPPED[(facing.getAxisDirection() != AxisDirection.POSITIVE && dir.getAxis() != Axis.X ? dir : dir.getOpposite()).get2DDataValue()];
            case Z:
               switch (dir) {
                  case DOWN:
                     return DOWN;
                  case UP:
                     return UP;
                  case NORTH:
                  case SOUTH:
                  default:
                     throw new IllegalStateException("Not a Valid Facing");
                  case EAST:
                     return facing.getAxisDirection() == AxisDirection.POSITIVE ? EAST : WEST;
                  case WEST:
                     return facing.getAxisDirection() == AxisDirection.POSITIVE ? WEST : EAST;
               }
            default:
               throw new IllegalStateException("Not a Valid Axis");
         }
      }

      public static ConnectionState.Connection byFacing(Direction dir, Axis axis) {
         switch (axis) {
            case X:
               switch (dir) {
                  case DOWN:
                     return DOWN;
                  case UP:
                     return UP;
                  case NORTH:
                     return WEST;
                  case SOUTH:
                     return EAST;
                  default:
                     throw new IllegalStateException("Not a Valid Facing");
               }
            case Y:
               return MAPPED[dir.get2DDataValue()];
            case Z:
               switch (dir) {
                  case DOWN:
                     return DOWN;
                  case UP:
                     return UP;
                  case NORTH:
                  case SOUTH:
                  default:
                     throw new IllegalStateException("Not a Valid Facing");
                  case EAST:
                     return EAST;
                  case WEST:
                     return WEST;
               }
            default:
               throw new IllegalStateException("Not a Valid Axis");
         }
      }

      public static ConnectionState.Connection byIndex(int index) {
         return MAPPED[index & 3];
      }
   }
}
