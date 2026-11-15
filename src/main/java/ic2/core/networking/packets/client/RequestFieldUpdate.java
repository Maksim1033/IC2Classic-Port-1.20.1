package ic2.core.networking.packets.client;

import ic2.api.network.tile.INetworkFieldProvider;
import ic2.core.IC2;
import ic2.core.networking.packets.IC2Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RequestFieldUpdate extends IC2Packet {
   boolean gui;
   BlockPos pos;

   public RequestFieldUpdate() {
   }

   public RequestFieldUpdate(BlockEntity tile, boolean gui) {
      this.pos = tile.getBlockPos();
      this.gui = gui;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeBoolean(this.gui);
      buffer.writeLong(this.pos.asLong());
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.gui = buffer.readBoolean();
      this.pos = BlockPos.of(buffer.readLong());
   }

   @Override
   public void handlePacket(Player source) {
      BlockEntity tile = source.level().getBlockEntity(this.pos);
      if (tile instanceof INetworkFieldProvider) {
         if (this.gui) {
            IC2.NETWORKING.get().sendInitialGuiData((INetworkFieldProvider)tile, source);
         } else {
            IC2.NETWORKING.get().requestInitialData((INetworkFieldProvider)tile);
         }
      }
   }
}
