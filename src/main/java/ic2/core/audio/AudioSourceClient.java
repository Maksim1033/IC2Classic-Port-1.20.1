package ic2.core.audio;

import com.mojang.blaze3d.audio.Channel;
import ic2.core.utils.math.MathUtils;
import net.minecraft.client.sounds.ChannelAccess.ChannelHandle;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AudioSourceClient implements IAudioSource {
   private static final float FADING_DISTANCE = 16.0F;
   public static final int VALID = 1;
   public static final int AUDIO_UPDATE = 2;
   public static final int LOOPING = 4;
   public static final int PRIORITY = 8;
   public static final int PLAYING = 16;
   public static final int PAUSED = 32;
   public static final int ENABLED = 64;
   public static final int ACCEPTING = 128;
   AudioManagerClient manager;
   ResourceLocation location;
   IAudioPosition pos;
   ChannelHandle audio;
   Vec3 currentPosition;
   AudioManager.SoundType type;
   float modifier = 1.0F;
   float volume;
   float realVolume;
   float pitch;
   int flags = 65;

   public AudioSourceClient(
      AudioManagerClient manager,
      ResourceLocation location,
      IAudioPosition pos,
      AudioManager.SoundType type,
      float volume,
      float pitch,
      boolean looping,
      boolean priority
   ) {
      this.manager = manager;
      this.location = location;
      this.pos = pos;
      this.currentPosition = pos.getPosition();
      this.type = type;
      this.volume = volume;
      this.pitch = pitch;
      if (looping) {
         this.setFlag(4);
      }

      if (priority) {
         this.setFlag(8);
      }
   }

   public AudioSourceClient(AudioManagerClient manager) {
      this.manager = manager;
   }

   protected void setFlag(int flag) {
      this.flags |= flag;
   }

   protected boolean hasFlag(int flag) {
      return (this.flags & flag) == flag;
   }

   protected boolean hasNotFlag(int flag) {
      return (this.flags & flag) != flag;
   }

   protected void clearFlag(int flag) {
      this.flags &= ~flag;
   }

   @Override
   public boolean isValid() {
      return this.hasFlag(1);
   }

   @Override
   public boolean isEnabled() {
      return this.hasFlag(65);
   }

   @Override
   public boolean isPlaying() {
      return this.hasFlag(16) && this.hasNotFlag(32);
   }

   @Override
   public boolean isPaused() {
      return this.hasFlag(32);
   }

   @Override
   public boolean isPriority() {
      return this.hasFlag(8);
   }

   @Override
   public float getVolume() {
      return this.volume;
   }

   @Override
   public float getRealVolume() {
      return this.realVolume;
   }

   @Override
   public float getPitch() {
      return this.pitch;
   }

   @Override
   public void setVolume(float volume) {
      if (!this.hasNotFlag(1)) {
         this.volume = volume;
         this.setFlag(2);
      }
   }

   @Override
   public void setPitch(float pitch) {
      if (!this.hasNotFlag(1)) {
         this.pitch = pitch;
         this.setFlag(2);
      }
   }

   @Override
   public void play() {
      if (!this.hasNotFlag(1)) {
         this.clearFlag(32);
         this.setFlag(16);
         if (this.audio != null && this.hasFlag(64)) {
            this.audio.execute(Channel::play);
         }
      }
   }

   @Override
   public void pause() {
      if (!this.hasNotFlag(17)) {
         this.setFlag(32);
         if (this.audio != null && this.hasFlag(64)) {
            this.audio.execute(Channel::pause);
         }
      }
   }

   @Override
   public void stop() {
      if (!this.hasNotFlag(1)) {
         this.clearFlag(48);
         if (this.audio != null && this.hasFlag(64)) {
            this.audio.execute(Channel::stop);
         }
      }
   }

   @Override
   public void remove() {
      this.stop();
      this.clearFlag(1);
   }

   @Override
   public void disable() {
      if (!this.hasNotFlag(65)) {
         if (this.hasFlag(16)) {
            this.stop();
            this.setFlag(16);
         }

         this.clearFlag(64);
      }
   }

   @Override
   public void enable() {
      if (!this.hasNotFlag(1) && !this.hasFlag(64)) {
         this.setFlag(64);
         this.ensureValid();
      }
   }

   public void ensureValid() {
      if (!this.hasNotFlag(65)) {
         if (this.audio != null && this.audio.isStopped()) {
            this.audio = null;
            if (this.hasNotFlag(4)) {
               this.clearFlag(48);
            }
         }

         if (this.audio == null && this.hasNotFlag(128)) {
            this.setFlag(128);
            this.manager.createEntry().thenAccept(T -> {
               this.audio = T;
               this.clearFlag(128);
               this.audio.execute(i -> {
                  i.attachStaticBuffer(this.manager.createBuffer(this.location));
                  this.syncWithSource(i, true);
                  if (this.hasFlag(16) && this.hasNotFlag(32)) {
                     i.play();
                  }
               });
            });
         }
      }
   }

   @Override
   public IAudioPosition getPosition() {
      return this.pos;
   }

   @Override
   public void updatePosition() {
      if (!this.hasNotFlag(1)) {
         this.currentPosition = this.pos.getPosition();
         if (this.audio != null) {
            this.audio.execute(i -> i.setSelfPosition(this.currentPosition));
         }
      }
   }

   @Override
   public void updateVolume(Player player) {
      if (this.hasNotFlag(1)) {
         this.realVolume = 0.0F;
      } else {
         if (this.manager.didModifiersChange()) {
            this.modifier = this.manager.getModifier(this.type, this.currentPosition);
         }

         float multiplier = this.volume;
         multiplier *= this.manager.getVolumeForType(this.type);
         multiplier *= this.manager.getMasterVolume();
         multiplier *= this.modifier;
         multiplier /= this.volume;
         double maxDistance = multiplier > 1.0F ? (double)(16.0F * multiplier * Math.max(this.volume, 0.2F)) : (double)(16.0F * Math.max(this.volume, 0.2F));
         double distance = 1.0;
         Vec3 playerPos = player.position();
         if (this.pos.isInSameWorld(player.level())) {
            distance = Math.max(1.0, this.pos.getPosition().distanceTo(playerPos));
         }

         if (distance > maxDistance) {
            this.realVolume = 0.0F;
            this.disable();
         } else {
            this.setFlag(64);
            double newRealVolume = (double)this.volume * (1.0 - distance / maxDistance);
            newRealVolume *= (double)multiplier;
            if (newRealVolume > 0.10000000149011612) {
               Vec3 perStep = MathUtils.divide(this.pos.getPosition().subtract(playerPos), distance);

               for (int i = 0; (double)i < distance; i++) {
                  BlockPos pos = BlockPos.containing(playerPos);
                  BlockState state = player.level().getBlockState(pos);
                  if (state.getBlock() != Blocks.AIR) {
                     newRealVolume *= state.isSolidRender(player.level(), pos) ? 0.6 : 0.8;
                  }

                  playerPos = playerPos.add(perStep);
               }
            }

            this.realVolume = (float)newRealVolume;
            if (this.realVolume <= 1.0E-4F) {
               this.realVolume = 0.0F;
               this.disable();
            } else {
               if (this.audio != null) {
                  if (this.hasFlag(2)) {
                     this.clearFlag(2);
                     this.audio.execute(ix -> this.syncWithSource(ix, false));
                  } else {
                     this.audio.execute(ix -> ix.setVolume(this.realVolume));
                  }
               }
            }
         }
      }
   }

   public void syncWithSource(Channel source, boolean init) {
      if (init) {
         source.setLooping(this.hasFlag(4));
         source.disableAttenuation();
         source.setRelative(false);
         source.setSelfPosition(this.currentPosition);
      }

      source.setVolume(this.realVolume);
      source.setPitch(this.pitch);
   }
}
