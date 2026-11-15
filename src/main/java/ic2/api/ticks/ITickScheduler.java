package ic2.api.ticks;

import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public interface ITickScheduler {
   void addWorldCallback(Level var1, ToIntFunction<Level> var2);

   void addWorldCallback(Level var1, ToIntFunction<Level> var2, int var3);

   void addServerCallback(ToIntFunction<MinecraftServer> var1);

   void addServerCallback(ToIntFunction<MinecraftServer> var1, int var2);

   void addClientCallback(IntSupplier var1);

   void addClientCallback(IntSupplier var1, int var2);
}
