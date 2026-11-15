package ic2.core.networking.packets.server;

import ic2.core.networking.packets.IC2Packet;
import ic2.core.platform.events.DebugHighlighter;
import ic2.core.utils.helpers.NBTUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class DebugPacket extends IC2Packet {
   BlockState state;
   CompoundTag tileData;
   long[] data;

   public DebugPacket() {
   }

   public DebugPacket(BlockState state, CompoundTag tileData, long[] data) {
      this.state = state;
      this.tileData = tileData;
      this.data = data;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeNbt(NbtUtils.writeBlockState(this.state));
      buffer.writeBoolean(this.tileData != null);
      if (this.tileData != null) {
         buffer.writeNbt(this.tileData);
      }

      buffer.writeLongArray(this.data);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), buffer.readNbt());
      if (buffer.readBoolean()) {
         this.tileData = buffer.readNbt();
      }

      this.data = buffer.readLongArray(this.data);
   }

   @Override
   public void handlePacket(Player source) {
      List<Component> debugTest = new ArrayList<>();
      debugTest.add(Component.literal("Debug Data:"));
      debugTest.add(Component.literal(""));
      debugTest.add(Component.literal("Block: ").append(this.state.getBlock().getName()));
      debugTest.addAll(NBTUtils.convertState(this.state));
      if (this.tileData != null) {
         debugTest.add(Component.literal(""));
         debugTest.add(Component.literal("TileData: "));
         ByteArrayOutputStream out = new ByteArrayOutputStream();

         try {
            NbtIo.write(this.tileData, new DataOutputStream(out));
         } catch (Exception var5) {
         }

         this.addBytes("NBTSize: ", (long)out.size(), debugTest);
         this.addBytes("All Network Bytes: ", this.data[0], debugTest);
         this.addBytes("Initial Network Bytes: ", this.data[1], debugTest);
         this.addBytes("Initial Gui Bytes: ", this.data[2], debugTest);
         debugTest.addAll(NBTUtils.convertNBT(this.tileData));
      }

      DebugHighlighter.INSTANCE.storeDebug(debugTest, this.tileData);
   }

   private void addBytes(String prefix, long bytes, List<Component> list) {
      if (bytes > 10000L) {
         list.add(Component.literal(prefix + bytes / 1000L + " KB").withStyle(ChatFormatting.GOLD));
      } else {
         list.add(Component.literal(prefix + bytes + " Bytes").withStyle(ChatFormatting.GOLD));
      }
   }
}
