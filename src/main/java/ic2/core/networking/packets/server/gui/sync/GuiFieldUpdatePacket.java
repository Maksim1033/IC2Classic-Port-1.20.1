package ic2.core.networking.packets.server.gui.sync;

import ic2.core.networking.NetworkManager;
import ic2.core.networking.buffers.InputBuffer;
import ic2.core.networking.buffers.OutputBuffer;
import ic2.core.networking.misc.NetworkUtils;
import ic2.core.networking.packets.IC2Packet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GuiFieldUpdatePacket extends IC2Packet {
   List<NetworkManager.FieldData> fields;
   BlockPos position;

   public GuiFieldUpdatePacket() {
   }

   public GuiFieldUpdatePacket(BlockEntity tile, List<NetworkManager.FieldData> fields) {
      this.position = tile.getBlockPos();
      this.fields = fields;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeLong(this.position.asLong());
      NetworkUtils.writeEntries(new OutputBuffer(buffer), this.fields);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.position = BlockPos.of(buffer.readLong());
      this.fields = NetworkUtils.readEntries(new InputBuffer(buffer));
   }

   @Override
   public void handlePacket(Player source) {
      BlockEntity tile = source.level().getBlockEntity(this.position);
      if (tile != null) {
         NetworkUtils.applyFields(tile, this.fields, true, source);
      }
   }
}
