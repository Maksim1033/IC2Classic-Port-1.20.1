package ic2.api.network;

import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.tile.INetworkFieldProvider;
import ic2.api.network.tile.PacketRange;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface INetworkManager {
   void registerDataBuffer(ResourceLocation var1, Class<? extends INetworkDataBuffer> var2, Supplier<INetworkDataBuffer> var3);

   void startGuiTracking(BlockEntity var1, Player var2);

   void sendInitialGuiData(INetworkFieldProvider var1, Player var2);

   void updateGuiData(BlockEntity var1, Player var2);

   void updateTileField(BlockEntity var1, String var2);

   void updateTileFields(BlockEntity var1, String... var2);

   void updateGuiField(BlockEntity var1, String var2);

   void updateGuiFields(BlockEntity var1, String... var2);

   void sendInitialData(INetworkFieldProvider var1, CompoundTag var2);

   void handleInitialChange(BlockEntity var1, CompoundTag var2);

   void requestInitialData(INetworkFieldProvider var1);

   void sendTileEvent(BlockEntity var1, int var2, int var3, PacketRange var4);

   void sendTileDataBufferEvent(BlockEntity var1, String var2, INetworkDataBuffer var3, PacketRange var4);

   void sendClientTileEvent(BlockEntity var1, int var2, int var3);

   void sendClientTileDataBufferEvent(BlockEntity var1, String var2, INetworkDataBuffer var3);

   void sendItemEvent(Player var1, ItemStack var2, int var3, int var4);

   void sendItemBuffer(Player var1, ItemStack var2, String var3, INetworkDataBuffer var4);

   void sendClientItemEvent(ItemStack var1, int var2, int var3);

   void sendClientItemBuffer(ItemStack var1, String var2, INetworkDataBuffer var3);
}
