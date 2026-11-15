package ic2.core.audio;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IAudioPosition {
   Level getWorld();

   Vec3 getPosition();

   default boolean isInSameWorld(Level world) {
      return isInSameWorld(world, this.getWorld());
   }

   static boolean isInSameWorld(Level world, Level otherWorld) {
      return getId(world) == getId(otherWorld);
   }

   static ResourceKey<Level> getId(Level world) {
      return world == null ? null : world.dimension();
   }
}
