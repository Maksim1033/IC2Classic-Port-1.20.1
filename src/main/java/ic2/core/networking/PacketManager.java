package ic2.core.networking;

import ic2.core.IC2;
import ic2.core.networking.packets.IC2Packet;
import ic2.core.networking.packets.client.ChunkRequestPacket;
import ic2.core.networking.packets.client.ClientTileEventPacket;
import ic2.core.networking.packets.client.OpenSlotPacket;
import ic2.core.networking.packets.client.PollContainerPacket;
import ic2.core.networking.packets.client.RequestFieldUpdate;
import ic2.core.networking.packets.client.TileDataBufferEventPacket;
import ic2.core.networking.packets.client.custom.CacheRequestPacket;
import ic2.core.networking.packets.client.custom.FilterComponentPacket;
import ic2.core.networking.packets.client.custom.PlayerConfigSetting;
import ic2.core.networking.packets.client.custom.ScrollPacket;
import ic2.core.networking.packets.client.friend.FriendSyncRequest;
import ic2.core.networking.packets.client.friend.FriendUpdatePacket;
import ic2.core.networking.packets.common.ItemDataBufferPacket;
import ic2.core.networking.packets.common.ItemEventPacket;
import ic2.core.networking.packets.debug.AnswerServerData;
import ic2.core.networking.packets.debug.RequestServerData;
import ic2.core.networking.packets.server.AudioPacket;
import ic2.core.networking.packets.server.BiomePacket;
import ic2.core.networking.packets.server.ChunkAnswerPacket;
import ic2.core.networking.packets.server.DebugPacket;
import ic2.core.networking.packets.server.ExplosionPacket;
import ic2.core.networking.packets.server.FriendPacket;
import ic2.core.networking.packets.server.HighlightPacket;
import ic2.core.networking.packets.server.KeyPacket;
import ic2.core.networking.packets.server.MultiHighlightPacket;
import ic2.core.networking.packets.server.PollGuiPacket;
import ic2.core.networking.packets.server.RecipeUpdatePacket;
import ic2.core.networking.packets.server.SpawnSonarPacket;
import ic2.core.networking.packets.server.custom.CacheAnswerPacket;
import ic2.core.networking.packets.server.custom.MonitorSyncPacket;
import ic2.core.networking.packets.server.custom.TubeSyncPacket;
import ic2.core.networking.packets.server.gui.open.CloseCustomGuiPacket;
import ic2.core.networking.packets.server.gui.open.OpenCustomGuiPacket;
import ic2.core.networking.packets.server.gui.open.OpenEntityGuiPacket;
import ic2.core.networking.packets.server.gui.open.OpenItemGuiPacket;
import ic2.core.networking.packets.server.gui.open.OpenTileGuiPacket;
import ic2.core.networking.packets.server.gui.open.SpecialOpenItemGuiPacket;
import ic2.core.networking.packets.server.gui.sync.ContainerSyncPacket;
import ic2.core.networking.packets.server.gui.sync.CustomNetworkPacket;
import ic2.core.networking.packets.server.gui.sync.GuiFieldUpdatePacket;
import ic2.core.networking.packets.server.gui.sync.NetworkFieldUpdatePacket;
import ic2.core.networking.packets.server.gui.sync.ServerTileEventPacket;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PacketDistributor.TargetPoint;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketManager {
   public static PacketManager INSTANCE = new PacketManager();
   public final String version = "1.0.0";
   public SimpleChannel channel;

   public void init() {
      this.channel = NetworkRegistry.newSimpleChannel(new ResourceLocation("ic2", "networking"), () -> "1.0.0", "1.0.0"::equals, "1.0.0"::equals);
      this.registerPacket(0, NetworkFieldUpdatePacket.class, NetworkFieldUpdatePacket::new);
      this.registerPacket(1, GuiFieldUpdatePacket.class, GuiFieldUpdatePacket::new);
      this.registerPacket(2, ServerTileEventPacket.class, ServerTileEventPacket::new);
      this.registerPacket(3, ClientTileEventPacket.class, ClientTileEventPacket::new);
      this.registerPacket(4, TileDataBufferEventPacket.class, TileDataBufferEventPacket::new);
      this.registerPacket(5, OpenTileGuiPacket.class, OpenTileGuiPacket::new);
      this.registerPacket(6, RequestFieldUpdate.class, RequestFieldUpdate::new);
      this.registerPacket(7, DebugPacket.class, DebugPacket::new);
      this.registerPacket(8, AudioPacket.TileAudioPacket.class, AudioPacket.TileAudioPacket::new);
      this.registerPacket(9, AudioPacket.EntityAudioPacket.class, AudioPacket.EntityAudioPacket::new);
      this.registerPacket(10, KeyPacket.class, KeyPacket::new);
      this.registerPacket(11, OpenItemGuiPacket.class, OpenItemGuiPacket::new);
      this.registerPacket(12, ScrollPacket.class, ScrollPacket::new);
      this.registerPacket(13, RecipeUpdatePacket.class, RecipeUpdatePacket::new);
      this.registerPacket(14, ItemEventPacket.class, ItemEventPacket::new);
      this.registerPacket(15, ItemDataBufferPacket.class, ItemDataBufferPacket::new);
      this.registerPacket(16, ExplosionPacket.class, ExplosionPacket::new);
      this.registerPacket(17, FilterComponentPacket.class, FilterComponentPacket::new);
      this.registerPacket(18, HighlightPacket.class, HighlightPacket::new);
      this.registerPacket(19, SpecialOpenItemGuiPacket.class, SpecialOpenItemGuiPacket::new);
      this.registerPacket(20, OpenSlotPacket.class, OpenSlotPacket::new);
      this.registerPacket(21, MultiHighlightPacket.class, MultiHighlightPacket::new);
      this.registerPacket(22, FriendPacket.class, FriendPacket::new);
      this.registerPacket(23, CacheRequestPacket.class, CacheRequestPacket::new);
      this.registerPacket(24, CacheAnswerPacket.class, CacheAnswerPacket::new);
      this.registerPacket(25, TubeSyncPacket.class, TubeSyncPacket::new);
      this.registerPacket(26, BiomePacket.class, BiomePacket::new);
      this.registerPacket(27, OpenCustomGuiPacket.class, OpenCustomGuiPacket::new);
      this.registerPacket(28, CustomNetworkPacket.class, CustomNetworkPacket::new);
      this.registerPacket(29, CloseCustomGuiPacket.class, CloseCustomGuiPacket::new);
      this.registerPacket(30, FriendSyncRequest.class, FriendSyncRequest::new);
      this.registerPacket(31, FriendUpdatePacket.class, FriendUpdatePacket::new);
      this.registerPacket(32, PollContainerPacket.class, PollContainerPacket::new);
      this.registerPacket(33, PollGuiPacket.class, PollGuiPacket::new);
      this.registerPacket(35, ChunkRequestPacket.class, ChunkRequestPacket::new);
      this.registerPacket(36, ChunkAnswerPacket.class, ChunkAnswerPacket::new);
      this.registerPacket(37, SpawnSonarPacket.class, SpawnSonarPacket::new);
      this.registerPacket(38, AudioPacket.SimpleAudioPacket.class, AudioPacket.SimpleAudioPacket::new);
      this.registerPacket(39, RequestServerData.class, RequestServerData::new);
      this.registerPacket(40, AnswerServerData.class, AnswerServerData::new);
      this.registerPacket(41, PlayerConfigSetting.class, PlayerConfigSetting::new);
      this.registerPacket(42, MonitorSyncPacket.class, MonitorSyncPacket::new);
      this.registerPacket(46, OpenEntityGuiPacket.class, OpenEntityGuiPacket::new);
      this.registerPacket(47, ContainerSyncPacket.class, ContainerSyncPacket::new);
   }

   public <T extends IC2Packet> void registerPacket(int index, Class<T> packet, Supplier<T> creator) {
      this.channel.registerMessage(index, packet, this::writePacket, K -> this.readPacket(K, creator), this::handlePacket);
   }

   protected void writePacket(IC2Packet packet, FriendlyByteBuf buffer) {
      try {
         packet.write(buffer);
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   protected <T extends IC2Packet> T readPacket(FriendlyByteBuf buffer, Supplier<T> values) {
      try {
         T packet = (T)values.get();
         packet.read(buffer);
         return packet;
      } catch (Exception var4) {
         var4.printStackTrace();
         return null;
      }
   }

   protected void handlePacket(IC2Packet packet, Supplier<Context> provider) {
      try {
         Context context = provider.get();
         Player player = this.getPlayer(context);
         if (packet.needsMainThread()) {
            context.enqueueWork(() -> packet.handlePacket(player));
         } else {
            packet.handlePacket(player);
         }

         context.setPacketHandled(true);
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   protected Player getPlayer(Context cont) {
      Player entity = cont.getSender();
      return entity != null ? entity : IC2.PLATFORM.getClientPlayerInstance();
   }

   public void sendToServer(IC2Packet packet) {
      this.channel.send(PacketDistributor.SERVER.noArg(), packet);
   }

   public void sendToPlayer(IC2Packet packet, Player player) {
      if (!(player instanceof ServerPlayer)) {
         throw new RuntimeException("Sending a Packet to a Player from client is not allowed");
      } else {
         this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), packet);
      }
   }

   public void sendToAllPlayers(IC2Packet packet) {
      this.channel.send(PacketDistributor.ALL.noArg(), packet);
   }

   public void sendToAllDim(Level level, IC2Packet packet) {
      this.channel.send(PacketDistributor.DIMENSION.with(level::dimension), packet);
   }

   public void sendToAllChunkWatchers(LevelChunk chunk, IC2Packet packet) {
      this.channel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), packet);
   }

   public void sendToAllEntityWatchers(Entity entity, IC2Packet packet) {
      this.channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
   }

   public void sendToNearby(Level level, BlockPos pos, int radius, IC2Packet packet) {
      this.sendToNearby(
         new TargetPoint((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, (double)radius, level.dimension()), packet
      );
   }

   public void sendToNearby(TargetPoint point, IC2Packet packet) {
      this.channel.send(PacketDistributor.NEAR.with(() -> point), packet);
   }
}
