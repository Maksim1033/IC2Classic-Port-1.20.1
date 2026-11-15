package ic2.core.utils;

import com.mojang.util.UUIDTypeAdapter;
import java.util.UUID;

public class MinecraftPlayer {
   private UUID UUID;
   private String name;

   public MinecraftPlayer(String UUID, String name) {
      this.UUID = UUIDTypeAdapter.fromString(UUID);
      this.name = name;
   }

   public UUID getUUID() {
      return this.UUID;
   }

   public String getName() {
      return this.name;
   }
}
