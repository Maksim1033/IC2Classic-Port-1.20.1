package ic2.core.audio.tracker;

import net.minecraft.world.level.Level;

public interface IAudioTracker {
   void onTick();

   boolean isValid(Level var1);
}
