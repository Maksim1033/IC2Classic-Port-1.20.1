package ic2.api.tiles.tubes;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.api.util.DirectionList;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public final class TransportedItem implements INetworkDataBuffer {
   static int GLOBAL_COUNTER = 0;
   public static final int MIN_SPEED = 1;
   public static final int MAX_SPEED = 100;
   int id;
   UUID logistics = null;
   ItemStack serverStack = ItemStack.EMPTY;
   Supplier<ItemStack> clientStack;
   byte color = 0;
   byte speed = 5;
   byte progress;
   byte lastProgress;
   boolean centering;
   byte currentDirection;
   byte triedDirections;
   private byte timeOut = 0;
   private boolean invalid = false;

   private TransportedItem() {
   }

   public TransportedItem(ItemStack stack) {
      this.serverStack = stack.copy();
      this.id = GLOBAL_COUNTER++;
      if (GLOBAL_COUNTER < 0) {
         GLOBAL_COUNTER = 0;
      }
   }

   public static TransportedItem readFromNetwork(IInputBuffer buffer) {
      TransportedItem item = new TransportedItem();
      item.read(buffer);
      return item;
   }

   public static TransportedItem read(CompoundTag nbt) {
      TransportedItem item = new TransportedItem();
      item.readFromNBT(nbt);
      return item.isInvalid() ? null : item;
   }

   public void readFromNBT(CompoundTag nbt) {
      this.id = nbt.getInt("id");
      this.serverStack = ItemStack.of(nbt.getCompound("item"));
      this.speed = nbt.getByte("speed");
      this.progress = nbt.getByte("progress");
      this.centering = nbt.getBoolean("centering");
      this.color = nbt.getByte("color");
      this.currentDirection = nbt.getByte("dir");
      this.triedDirections = nbt.getByte("tried");
      if (nbt.contains("logistics")) {
         this.logistics = nbt.getUUID("logistics");
      }
   }

   public CompoundTag writeToNBT(CompoundTag nbt) {
      nbt.putInt("id", this.id);
      nbt.put("item", this.serverStack.save(new CompoundTag()));
      nbt.putByte("speed", this.speed);
      nbt.putByte("progress", this.progress);
      nbt.putBoolean("centering", this.centering);
      nbt.putByte("color", this.color);
      nbt.putByte("dir", this.currentDirection);
      nbt.putByte("tried", this.triedDirections);
      if (this.logistics != null) {
         nbt.putUUID("logistics", this.logistics);
      }

      return nbt;
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.id = buffer.readVarInt();
      this.serverStack = ItemStack.EMPTY;
      this.clientStack = IItemCache.getCache().getItem(buffer.readVarInt());
      this.speed = buffer.readByte();
      this.progress = buffer.readByte();
      this.deserializeEssentials(buffer.readShort());
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeVarInt(this.id);
      buffer.writeVarInt(IItemCache.getCache().registerItem(this.serverStack));
      buffer.writeByte(this.speed);
      buffer.writeByte(this.progress);
      buffer.writeShort(this.serializeEssentials());
   }

   public short serializeEssentials() {
      return (short)((this.centering ? 1 : 0) << 14 | (this.color & 31) << 9 | (this.currentDirection & 7) << 6 | this.triedDirections & 63);
   }

   public void deserializeEssentials(short data) {
      this.triedDirections = (byte)(data & 63);
      this.currentDirection = (byte)(data >> 6 & 7);
      this.color = (byte)(data >> 9 & 31);
      this.centering = data >> 14 > 0;
   }

   public int getId() {
      return this.id;
   }

   public UUID getRequestId() {
      return this.logistics;
   }

   public boolean isCentering() {
      return this.centering;
   }

   public ItemStack getServerStack() {
      return this.serverStack;
   }

   public ItemStack getStack() {
      return this.clientStack != null ? this.clientStack.get() : this.serverStack;
   }

   public byte getProgress() {
      return this.progress;
   }

   public boolean isInvalid() {
      return this.invalid || this.clientStack == null && this.serverStack.isEmpty();
   }

   public DyeColor getColor() {
      return this.color == 0 ? null : DyeColor.byId(this.color - 1);
   }

   public TransportedItem setColor(DyeColor color) {
      this.color = (byte)(color == null ? 0 : color.getId() + 1);
      return this;
   }

   public void setAttemptedDirections(DirectionList list) {
      this.triedDirections = (byte)list.getCode();
   }

   public TransportedItem setRequestId(UUID id) {
      this.logistics = id;
      return this;
   }

   public void invalidate() {
      this.invalid = true;
   }

   public boolean hasReachedCenter() {
      return this.centering && this.progress >= 0;
   }

   public boolean hasReachedEnd() {
      return !this.centering && this.progress >= 100;
   }

   public Direction getTransferDirection() {
      return Direction.from3DDataValue(this.currentDirection);
   }

   public Direction getTravelingDirection() {
      return this.isCentering() ? this.getTransferDirection().getOpposite() : this.getTransferDirection();
   }

   public DirectionList getAttemptedDirections() {
      return DirectionList.ofNumber(this.triedDirections);
   }

   public void clearDirections() {
      this.triedDirections = 0;
   }

   public void setCentering() {
      this.centering = true;
   }

   public void setInsertionDirection(Direction dir) {
      if (dir == null) {
         this.progress = 0;
         this.centering = false;
      } else {
         this.centering = true;
         this.progress = -100;
         this.currentDirection = (byte)dir.get3DDataValue();
         this.triedDirections = (byte)(this.triedDirections | (byte)(1 << dir.get3DDataValue()));
      }
   }

   public void setExportDirection(Direction dir) {
      this.centering = false;
      this.currentDirection = (byte)dir.get3DDataValue();
   }

   public void update() {
      this.progress = (byte)Math.min(this.centering ? 0 : 100, this.progress + this.speed);
      if (this.progress == this.lastProgress) {
         if (++this.timeOut > 4) {
            this.invalid = true;
            return;
         }
      } else {
         this.timeOut = 0;
      }

      this.lastProgress = this.progress;
   }

   public TransportedItem setStartSpeed(int speed) {
      this.speed = (byte)Mth.clamp(speed, 1, 100);
      return this;
   }

   public boolean updateSpeed(int newSpeed, int change) {
      if (newSpeed != this.speed && change != 0) {
         this.speed = (byte)Mth.clamp(newSpeed < this.speed ? this.speed - change : this.speed + change, 1, 100);
         return true;
      } else {
         return false;
      }
   }
}
