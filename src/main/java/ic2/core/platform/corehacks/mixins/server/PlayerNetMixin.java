package ic2.core.platform.corehacks.mixins.server;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ServerGamePacketListenerImpl.class})
public interface PlayerNetMixin {
   @Accessor("aboveGroundTickCount")
   void setAboveGroundTickCount(int var1);

   @Accessor("aboveGroundVehicleTickCount")
   void setAboveGroundVehicleTickCount(int var1);
}
