package ic2.core.audio.providers;

import ic2.core.audio.AudioManager;
import ic2.core.audio.IAudioPosition;
import ic2.core.audio.ISoundProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class SimplePosition implements IAudioPosition {
   Level world;
   Vec3 pos;

   public SimplePosition(Level world, BlockPos pos) {
      this(world, Vec3.atLowerCornerOf(pos));
   }

   public SimplePosition(Level world, Vec3 pos) {
      this.world = world;
      this.pos = pos;
   }

   @Override
   public Level getWorld() {
      return this.world;
   }

   @Override
   public Vec3 getPosition() {
      return this.pos;
   }

   public static IAudioPosition getFrom(Object obj, AudioManager.SoundType spec) {
      if (obj instanceof ISoundProvider) {
         return ((ISoundProvider)obj).getPosition();
      } else if (obj instanceof IAudioPosition) {
         return (IAudioPosition)obj;
      } else if (obj instanceof Entity e) {
         return (IAudioPosition)(spec == AudioManager.SoundType.STATIC
            ? new SimplePosition(e.getCommandSenderWorld(), e.position())
            : new MovingEntityPosition(e));
      } else {
         return obj instanceof BlockEntity te ? new SimplePosition(te.getLevel(), te.getBlockPos()) : null;
      }
   }
}
