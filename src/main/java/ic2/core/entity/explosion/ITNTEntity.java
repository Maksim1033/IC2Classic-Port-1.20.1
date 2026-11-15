package ic2.core.entity.explosion;

import ic2.core.platform.registries.IC2Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ITNTEntity extends IC2ExplosiveEntity {
   public ITNTEntity(EntityType<?> entityTypeIn, Level worldIn) {
      this(entityTypeIn, worldIn, 0.0, 0.0, 0.0);
   }

   public ITNTEntity(EntityType<?> entityTypeIn, Level worldIn, double x, double y, double z) {
      super(entityTypeIn, worldIn, x, y, z, 60, 5.5F, 0.9F, IC2Blocks.ITNT.defaultBlockState(), null);
   }
}
