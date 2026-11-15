package ic2.core.audio;

import net.minecraft.world.level.Level;

public interface ISoundProvider {
   IAudioPosition getPosition();

   boolean isValid(Level var1);
}
