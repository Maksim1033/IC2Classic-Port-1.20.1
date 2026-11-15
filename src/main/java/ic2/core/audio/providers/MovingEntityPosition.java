package ic2.core.audio.providers;

import ic2.core.audio.IAudioPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MovingEntityPosition implements IAudioPosition {
   Entity entity;

   public MovingEntityPosition(Entity entity) {
      this.entity = entity;
   }

   @Override
   public Level getWorld() {
      return this.entity.level();
   }

   @Override
   public Vec3 getPosition() {
      return this.entity.position();
   }
}
