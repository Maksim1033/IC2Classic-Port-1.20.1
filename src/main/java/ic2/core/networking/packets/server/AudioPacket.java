package ic2.core.networking.packets.server;

import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.audio.providers.SimplePosition;
import ic2.core.networking.packets.IC2Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public abstract class AudioPacket extends IC2Packet {
   ResourceLocation location;
   AudioManager.SoundType type;
   float volume;
   float pitch;

   public AudioPacket() {
   }

   public AudioPacket(ResourceLocation location, AudioManager.SoundType type, float volume, float pitch) {
      this.location = location;
      this.type = type;
      this.volume = volume;
      this.pitch = pitch;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeResourceLocation(this.location);
      buffer.writeByte(this.type.getIndex());
      buffer.writeFloat(this.volume);
      buffer.writeFloat(this.pitch);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.location = buffer.readResourceLocation();
      this.type = AudioManager.SoundType.byIndex(buffer.readByte());
      this.volume = buffer.readFloat();
      this.pitch = buffer.readFloat();
   }

   public static class EntityAudioPacket extends AudioPacket {
      int id;

      public EntityAudioPacket() {
      }

      public EntityAudioPacket(Entity entity, ResourceLocation location, AudioManager.SoundType type, float volume, float pitch) {
         super(location, type, volume, pitch);
         this.id = entity.getId();
      }

      @Override
      public void write(FriendlyByteBuf buffer) {
         buffer.writeInt(this.id);
         super.write(buffer);
      }

      @Override
      public void read(FriendlyByteBuf buffer) {
         this.id = buffer.readInt();
         super.read(buffer);
      }

      @Override
      public void handlePacket(Player source) {
         Entity entity = source.level().getEntity(this.id);
         if (entity != null) {
            IC2.AUDIO.playSound(entity, this.location, this.type, this.volume, this.pitch);
         }
      }
   }

   public static class SimpleAudioPacket extends AudioPacket {
      Vec3 position;

      public SimpleAudioPacket() {
      }

      public SimpleAudioPacket(Vec3 position, ResourceLocation location, AudioManager.SoundType type, float volume, float pitch) {
         super(location, type, volume, pitch);
         this.position = position;
      }

      @Override
      public void write(FriendlyByteBuf buffer) {
         super.write(buffer);
         buffer.writeDouble(this.position.x());
         buffer.writeDouble(this.position.y());
         buffer.writeDouble(this.position.z());
      }

      @Override
      public void read(FriendlyByteBuf buffer) {
         super.read(buffer);
         this.position = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
      }

      @Override
      public void handlePacket(Player source) {
         if (source != null) {
            IC2.AUDIO.playSound(new SimplePosition(source.level(), this.position), this.location, this.type, this.volume, this.pitch);
         }
      }
   }

   public static class TileAudioPacket extends AudioPacket {
      BlockPos pos;

      public TileAudioPacket() {
      }

      public TileAudioPacket(BlockEntity tile, ResourceLocation location, AudioManager.SoundType type, float volume, float pitch) {
         super(location, type, volume, pitch);
         this.pos = tile.getBlockPos();
      }

      @Override
      public void write(FriendlyByteBuf buffer) {
         buffer.writeLong(this.pos.asLong());
         super.write(buffer);
      }

      @Override
      public void read(FriendlyByteBuf buffer) {
         this.pos = BlockPos.of(buffer.readLong());
         super.read(buffer);
      }

      @Override
      public void handlePacket(Player source) {
         BlockEntity tile = source.level().getBlockEntity(this.pos);
         if (tile != null) {
            IC2.AUDIO.playSound(tile, this.location, this.type, this.volume, this.pitch);
         }
      }
   }
}
