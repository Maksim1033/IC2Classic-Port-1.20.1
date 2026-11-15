package ic2.core.block.rendering.camouflage;

import ic2.api.events.RetextureEvent;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.platform.registries.ColorMaps;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ColorQuadCache implements IQuadCache {
   static final int[] COLOR = new int[]{-1};
   static final RetextureEvent.Rotation[] ROTATION = new RetextureEvent.Rotation[]{RetextureEvent.Rotation.ROTATION_0};
   final Direction dir;
   DyeColor color;
   BlockState state;

   public ColorQuadCache(Direction dir) {
      this.dir = dir;
   }

   public void setColor(DyeColor color) {
      this.color = color;
      if (color != null) {
         this.state = ColorMaps.CFOAM_BLOCKS.getBlock(color, Blocks.AIR).defaultBlockState();
      }
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeByte((byte)(this.color == null ? -1 : this.color.getId()));
   }

   @Override
   public void read(IInputBuffer buffer) {
      byte value = buffer.readByte();
      this.setColor(value == -1 ? null : DyeColor.byId(value));
   }

   @Override
   public CompoundTag write(CompoundTag nbt) {
      nbt.putByte("color", (byte)(this.color == null ? -1 : this.color.getId()));
      return nbt;
   }

   @Override
   public void read(CompoundTag nbt) {
      int value = nbt.getInt("color");
      this.setColor(value == -1 ? null : DyeColor.byId(value));
   }

   @Override
   public BlockState getDisplayBlock() {
      return this.state;
   }

   @Override
   public int[] getColors() {
      return COLOR;
   }

   @Override
   public RetextureEvent.Rotation[] getRotations() {
      return ROTATION;
   }

   @Override
   public Direction getSide() {
      return this.dir;
   }

   public DyeColor getColor() {
      return this.color;
   }
}
