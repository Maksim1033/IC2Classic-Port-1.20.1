package ic2.api.addons;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;

public interface IModule {
   boolean canLoad(Dist var1);

   default void loadConfigs() {
   }

   default void preInit(IEventBus modBus) {
   }

   default void postInit() {
   }
}
