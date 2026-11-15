package ic2.core.platform.corehacks.mixins.server;

import ic2.core.entity.potion.ShakyPredicate;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin({ProjectileUtil.class})
public class ProjectileMixin {
   @Overwrite
   public static EntityHitResult getEntityHitResult(Level worldIn, Entity projectile, Vec3 startVec, Vec3 endVec, AABB boundingBox, Predicate<Entity> filter) {
      double d0 = 1.7976931348623157E308;
      Entity entity = null;

      for (Entity entity1 : worldIn.getEntities(projectile, boundingBox, filter.and(ShakyPredicate.INSTANCE))) {
         AABB axisAlignedBB = entity1.getBoundingBox().inflate(0.30000001192092896);
         Optional<Vec3> optional = axisAlignedBB.clip(startVec, endVec);
         if (optional.isPresent()) {
            double d1 = startVec.distanceToSqr(optional.get());
            if (d1 < d0) {
               entity = entity1;
               d0 = d1;
            }
         }
      }

      return entity == null ? null : new EntityHitResult(entity);
   }
}
