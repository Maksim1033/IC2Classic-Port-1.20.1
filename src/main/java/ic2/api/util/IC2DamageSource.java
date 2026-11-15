package ic2.api.util;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public final class IC2DamageSource extends DamageSource {
   public static final IC2DamageSource ELECTRICITY = new IC2DamageSource("electricity");
   public static final IC2DamageSource NUKE = new IC2DamageSource("nuke");
   public static final IC2DamageSource RADIATION = new IC2DamageSource("radiation");
   Entity source;

   private IC2DamageSource(String damageTypeIn) {
      super(Holder.direct(new DamageType(damageTypeIn, 0.0F)));
   }

   private IC2DamageSource(String damageTypeIn, Entity source) {
      super(Holder.direct(new DamageType(damageTypeIn, 0.0F)), source);
      this.source = source;
   }

   public static IC2DamageSource newShockDamage(Entity entity) {
      return new IC2DamageSource("electricity", entity);
   }

   @Override
   public Entity getEntity() {
      return this.source != null ? this.source : super.getEntity();
   }
}
