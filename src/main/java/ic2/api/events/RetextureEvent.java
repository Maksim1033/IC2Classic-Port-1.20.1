package ic2.api.events;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.LevelEvent;

public class RetextureEvent extends LevelEvent {
   BlockPos pos;
   Direction side;
   Player applyingPlayer;
   RetextureEvent.TextureContainer container;
   boolean applied = false;

   public RetextureEvent(LevelAccessor world, BlockPos pos, Direction side, Player applyingPlayer, RetextureEvent.TextureContainer container) {
      super(world);
      this.pos = pos;
      this.side = side;
      this.applyingPlayer = applyingPlayer;
      this.container = container;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Direction getSide() {
      return this.side;
   }

   public BlockState getBlockState() {
      return this.getLevel().getBlockState(this.pos);
   }

   public BlockEntity getBlockEntity() {
      return this.getLevel().getBlockEntity(this.pos);
   }

   public Player getApplyingPlayer() {
      return this.applyingPlayer;
   }

   public RetextureEvent.TextureContainer getContainer() {
      return this.container;
   }

   public boolean isApplied() {
      return this.applied;
   }

   public void setApplied(boolean apply) {
      this.applied = apply;
   }

   public static enum Rotation {
      ROTATION_0(0),
      ROTATION_90(1),
      ROTATION_180(2),
      ROTATION_270(3);

      public static final RetextureEvent.Rotation[] ROTATIONS;
      final int rotation;

      private Rotation(int rotation) {
         this.rotation = rotation;
      }

      public static RetextureEvent.Rotation byIndex(int index) {
         return ROTATIONS[index % ROTATIONS.length];
      }

      public RetextureEvent.Rotation getNext() {
         return byIndex(this.rotation + 1);
      }

      public int getIndex() {
         return this.rotation;
      }

      public int getRotation() {
         return this.rotation * 90;
      }

      static {
         RetextureEvent.Rotation[] values = values();
         ROTATIONS = new RetextureEvent.Rotation[values.length];

         for (RetextureEvent.Rotation rotation : values) {
            ROTATIONS[rotation.getIndex()] = rotation;
         }
      }
   }

   public static class TextureContainer {
      BlockState state;
      Direction side;
      RetextureEvent.Rotation[] rotations;
      int[] colors;

      public TextureContainer(CompoundTag nbt) {
         this(
            NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("block")),
            Direction.from3DDataValue(nbt.getInt("side")),
            decode(nbt.getByteArray("rotations")),
            nbt.getIntArray("colors")
         );
      }

      public TextureContainer(BlockState state, Direction side, RetextureEvent.Rotation[] rotations, int[] colors) {
         this.state = state;
         this.side = side;
         this.rotations = rotations;
         this.colors = colors;
      }

      public BlockState getState() {
         return this.state;
      }

      public Direction getSide() {
         return this.side;
      }

      public int[] getColors() {
         return this.colors;
      }

      public RetextureEvent.Rotation[] getRotations() {
         return this.rotations;
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof RetextureEvent.TextureContainer other)
            ? false
            : other.state.equals(this.state) && other.side == this.side && Arrays.equals(other.colors, this.colors);
      }

      public CompoundTag save() {
         CompoundTag nbt = new CompoundTag();
         nbt.put("block", NbtUtils.writeBlockState(this.state));
         nbt.putByte("side", (byte)this.side.get3DDataValue());
         nbt.putByteArray("rotations", encode(this.rotations));
         nbt.putIntArray("colors", this.colors);
         return nbt.copy();
      }

      private static RetextureEvent.Rotation[] decode(byte[] data) {
         RetextureEvent.Rotation[] rotation = new RetextureEvent.Rotation[data.length];

         for (int i = 0; i < data.length; i++) {
            rotation[i] = RetextureEvent.Rotation.byIndex(data[i]);
         }

         return rotation;
      }

      private static byte[] encode(RetextureEvent.Rotation[] rotations) {
         byte[] data = new byte[rotations.length];

         for (int i = 0; i < data.length; i++) {
            data[i] = (byte)rotations[i].getIndex();
         }

         return data;
      }
   }
}
