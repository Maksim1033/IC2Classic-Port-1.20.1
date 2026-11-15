package ic2.api.tiles.teleporter;

import ic2.api.util.ILocation;
import net.minecraft.core.Direction;

public interface ITeleporterTarget extends ILocation {
   Direction getFacing();

   ITeleporterTarget.TeleportType getSendType();

   boolean canReceive(ITeleporterTarget.TeleportType var1);

   boolean setTarget(TeleporterTarget var1);

   boolean hasTarget(TeleporterTarget var1);

   public static enum TeleportType {
      ENTITY,
      SPAWNER,
      ITEM,
      FLUID,
      ENERGY,
      NOTHING;

      public boolean matches(ITeleporterTarget.TeleportType other) {
         if (other == SPAWNER) {
            return this == ENTITY;
         } else {
            return this == SPAWNER ? other == ENTITY : this == other;
         }
      }
   }
}
