package ic2.core.networking.packets.server;

import ic2.core.networking.packets.IC2Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RocketModePacket extends IC2Packet {
   boolean elytra;
   float power;
   int entityId;

   public RocketModePacket() {
   }

   public RocketModePacket(boolean elytra, float power, int entityId) {
      this.elytra = elytra;
      this.power = power;
      this.entityId = entityId;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeBoolean(this.elytra);
      buffer.writeFloat(this.power);
      buffer.writeInt(this.entityId);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.elytra = buffer.readBoolean();
      this.power = buffer.readFloat();
      this.entityId = buffer.readInt();
   }

   @Override
   public void handlePacket(Player source) {
      Entity entity = source.level().getEntity(this.entityId);
      if (entity != null) {
         if (this.elytra) {
            entity.moveRelative(10.0F, new Vec3(0.0, 0.0, 0.4 * (double)this.power));
         } else {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, (double)this.power, 0.0));
         }
      }
   }
}
