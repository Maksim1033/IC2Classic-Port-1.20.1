package ic2.core.audio;

import ic2.core.audio.providers.SimplePosition;
import ic2.core.audio.tracker.IAudioTracker;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.server.AudioPacket;
import ic2.core.utils.collection.CollectionUtils;
import java.util.EnumMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AudioManager {
   public void init() {
   }

   public void playSound(Object obj, ResourceLocation location) {
      this.playSound(obj, location, AudioManager.SoundType.STATIC);
   }

   public void playSound(Object obj, ResourceLocation location, AudioManager.SoundType type) {
      this.playSound(obj, location, type, 1.0F, 1.0F);
   }

   public void playSound(Object obj, ResourceLocation location, AudioManager.SoundType type, float volume, float pitch) {
      if (location != null) {
         if (obj instanceof Entity entity) {
            PacketManager.INSTANCE.sendToAllEntityWatchers(entity, new AudioPacket.EntityAudioPacket(entity, location, type, volume, pitch));
         } else if (obj instanceof BlockEntity tile) {
            PacketManager.INSTANCE
               .sendToAllChunkWatchers(tile.getLevel().getChunkAt(tile.getBlockPos()), new AudioPacket.TileAudioPacket(tile, location, type, volume, pitch));
         } else if (obj instanceof SimplePosition position) {
            PacketManager.INSTANCE
               .sendToAllChunkWatchers(
                  position.getWorld().getChunkAt(BlockPos.containing(position.getPosition())),
                  new AudioPacket.SimpleAudioPacket(position.getPosition(), location, type, volume, pitch)
               );
         }
      }
   }

   public IAudioSource createSource(Object obj, ResourceLocation location) {
      return null;
   }

   public IAudioSource createSource(Object obj, ResourceLocation location, AudioManager.SoundType type, float volume, boolean loop, boolean priority) {
      return null;
   }

   public void removeSource(Object obj) {
   }

   public float getMasterVolume() {
      return 0.0F;
   }

   public float getVolumeForType(AudioManager.SoundType type) {
      return 0.0F;
   }

   public void addTracker(IAudioTracker tracker) {
   }

   public void addModifier(BlockPos pos, EnumMap<AudioManager.SoundType, AudioEffect> modifiers) {
   }

   public void removeModifier(BlockPos pos) {
   }

   public void resetAll() {
   }

   public static enum SoundType implements CollectionUtils.IIndexEnum {
      STATIC(0),
      ITEM(1),
      BACKPACK(2);

      static final AudioManager.SoundType[] TYPES = CollectionUtils.createSortedArray(values());
      final int index;

      private SoundType(int index) {
         this.index = index;
      }

      @Override
      public int getIndex() {
         return this.index;
      }

      public static AudioManager.SoundType byIndex(int index) {
         return TYPES[index % TYPES.length];
      }
   }
}
