package ic2.core.audio;

import net.minecraft.world.entity.player.Player;

public interface IAudioSource {
   boolean isValid();

   boolean isEnabled();

   boolean isPlaying();

   boolean isPaused();

   boolean isPriority();

   float getVolume();

   float getRealVolume();

   float getPitch();

   void setVolume(float var1);

   void setPitch(float var1);

   default void playStop(boolean play) {
      if (play) {
         this.play();
      } else {
         this.stop();
      }
   }

   void play();

   void pause();

   void stop();

   void remove();

   void enable();

   void disable();

   IAudioPosition getPosition();

   void updatePosition();

   void updateVolume(Player var1);
}
