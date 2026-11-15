package ic2.core.utils;

import java.util.function.Supplier;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.thread.EffectiveSide;

public class SidedGateway<T> {
   private final T clientInstance;
   private final T serverInstance;

   public SidedGateway(Class<? extends T> server, Class<? extends T> client) {
      try {
         if (FMLEnvironment.dist.isClient()) {
            this.clientInstance = (T)client.newInstance();
         } else {
            this.clientInstance = null;
         }

         this.serverInstance = (T)server.newInstance();
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public SidedGateway(Supplier<? extends T> server, Supplier<? extends T> client) {
      this.serverInstance = (T)server.get();
      if (FMLEnvironment.dist.isClient()) {
         this.clientInstance = (T)client.get();
      } else {
         this.clientInstance = null;
      }
   }

   public SidedGateway(String server, String client) {
      try {
         if (FMLEnvironment.dist.isClient()) {
            this.clientInstance = (T)Class.forName(client).newInstance();
         } else {
            this.clientInstance = null;
         }

         this.serverInstance = (T)Class.forName(server).newInstance();
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public T get() {
      return EffectiveSide.get().isClient() ? this.clientInstance : this.serverInstance;
   }

   public T get(boolean simulating) {
      return simulating ? this.serverInstance : this.clientInstance;
   }
}
