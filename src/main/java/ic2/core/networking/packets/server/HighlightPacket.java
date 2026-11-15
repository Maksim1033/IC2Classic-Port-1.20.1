package ic2.core.networking.packets.server;

import ic2.core.block.rendering.world.impl.BlockHighlighter;
import ic2.core.networking.packets.IC2Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class HighlightPacket extends IC2Packet {
   BlockPos pos;
   int ticks;
   int color;
   boolean doubleSided;

   public HighlightPacket() {
   }

   public HighlightPacket(BlockPos pos, int ticks, int color, boolean doubleSided) {
      this.pos = pos;
      this.ticks = ticks;
      this.color = color;
      this.doubleSided = doubleSided;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeLong(this.pos.asLong());
      buffer.writeVarInt(this.ticks);
      buffer.writeInt(this.color);
      buffer.writeBoolean(this.doubleSided);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.pos = BlockPos.of(buffer.readLong());
      this.ticks = buffer.readVarInt();
      this.color = buffer.readInt();
      this.doubleSided = buffer.readBoolean();
   }

   @Override
   public void handlePacket(Player source) {
      BlockHighlighter.INSTANCE.addHighlight(this.pos, this.ticks, this.color, this.doubleSided);
   }
}
