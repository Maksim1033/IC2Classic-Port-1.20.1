package ic2.core.networking.packets.server;

import ic2.core.block.rendering.world.impl.SonarOverlay;
import ic2.core.item.base.features.ISonarProvider;
import ic2.core.networking.packets.IC2Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SpawnSonarPacket extends IC2Packet {
   int color;
   int radius;
   long pos;

   public SpawnSonarPacket() {
   }

   public SpawnSonarPacket(int color, int radius, BlockPos pos) {
      this.color = color;
      this.radius = radius;
      this.pos = pos.asLong();
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeInt(this.color);
      buffer.writeVarInt(this.radius);
      buffer.writeLong(this.pos);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.color = buffer.readInt();
      this.radius = buffer.readVarInt();
      this.pos = buffer.readLong();
   }

   @Override
   public void handlePacket(Player source) {
      SonarOverlay.INSTANCE.addSonar(ISonarProvider.simple(this.color, BlockPos.of(this.pos), this.radius), () -> ItemStack.EMPTY);
   }
}
