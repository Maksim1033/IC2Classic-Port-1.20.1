package ic2.api.tiles.teleporter;

import com.google.common.base.Objects;
import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

public class TeleporterTarget implements INetworkDataBuffer {
   ResourceKey<Level> dimension;
   BlockPos targetPosition;

   private TeleporterTarget() {
   }

   public TeleporterTarget(TeleporterTarget target) {
      this.dimension = target.getDimension();
      this.targetPosition = target.getTargetPosition().immutable();
   }

   public TeleporterTarget(BlockEntity tile) {
      this(tile.getLevel(), tile.getBlockPos());
   }

   public TeleporterTarget(Level world, BlockPos pos) {
      this.dimension = world.dimension();
      this.targetPosition = pos.immutable();
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeRegistryKey(this.dimension);
      buffer.writeLong(this.targetPosition.asLong());
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.dimension = buffer.readRegistryKey(Registries.DIMENSION);
      this.targetPosition = BlockPos.of(buffer.readLong());
   }

   public ResourceKey<Level> getDimension() {
      return this.dimension;
   }

   public BlockPos getTargetPosition() {
      return this.targetPosition;
   }

   public ServerLevel getWorld() {
      return ServerLifecycleHooks.getCurrentServer().getLevel(this.dimension);
   }

   public BlockEntity getTile() {
      return this.getWorld().getBlockEntity(this.getTargetPosition());
   }

   public boolean isSame(BlockEntity tile) {
      return this.isSame(tile.getLevel().dimension(), tile.getBlockPos());
   }

   public boolean isSame(ResourceKey<Level> type, BlockPos pos) {
      return this.dimension == type && this.targetPosition.equals(pos);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(new Object[]{this.dimension.location().hashCode(), this.targetPosition.hashCode()});
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof TeleporterTarget target) ? false : target.dimension == this.dimension && target.targetPosition.equals(this.targetPosition);
   }

   public static TeleporterTarget readFromBuffer(IInputBuffer buffer) {
      TeleporterTarget target = new TeleporterTarget();
      target.read(buffer);
      return target.dimension == null ? null : target;
   }

   public static TeleporterTarget read(CompoundTag nbt) {
      if (nbt.isEmpty()) {
         return null;
      } else {
         TeleporterTarget target = new TeleporterTarget();
         target.dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("id")));
         target.targetPosition = BlockPos.of(nbt.getLong("pos"));
         return target;
      }
   }

   public CompoundTag write() {
      return this.write(new CompoundTag());
   }

   public CompoundTag write(CompoundTag nbt) {
      nbt.putString("id", this.dimension.location().toString());
      nbt.putLong("pos", this.targetPosition.asLong());
      return nbt;
   }
}
