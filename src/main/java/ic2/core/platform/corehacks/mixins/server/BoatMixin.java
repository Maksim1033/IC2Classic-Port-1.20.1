package ic2.core.platform.corehacks.mixins.server;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Boat.Status;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Boat.class})
public interface BoatMixin {
   @Accessor("status")
   Status getActualStatus();

   @Accessor("lastYd")
   void setLastYDelta(double var1);
}
