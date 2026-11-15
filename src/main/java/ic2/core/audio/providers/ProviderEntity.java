package ic2.core.audio.providers;

import ic2.core.audio.AudioManager;
import ic2.core.audio.IAudioPosition;
import ic2.core.audio.ISoundProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ProviderEntity implements ISoundProvider {
   Entity entity;
   AudioManager.SoundType type;

   public ProviderEntity(Entity entity, AudioManager.SoundType type) {
      this.entity = entity;
      this.type = type;
   }

   @Override
   public IAudioPosition getPosition() {
      return SimplePosition.getFrom(this.entity, this.type);
   }

   @Override
   public boolean isValid(Level world) {
      return this.entity.isAlive() && IAudioPosition.isInSameWorld(world, this.entity.getCommandSenderWorld());
   }

   @Override
   public int hashCode() {
      return this.entity.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof ProviderEntity ? ((ProviderEntity)obj).entity == this.entity : false;
   }
}
