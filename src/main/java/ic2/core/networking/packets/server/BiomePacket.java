package ic2.core.networking.packets.server;

import ic2.core.IC2;
import ic2.core.networking.packets.IC2Packet;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BiomePacket extends IC2Packet {
   BlockPos pos;
   ResourceLocation biomeKey;

   public BiomePacket() {
   }

   public BiomePacket(BlockPos pos, ResourceLocation biomeKey) {
      this.pos = pos;
      this.biomeKey = biomeKey;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeLong(this.pos.asLong());
      buffer.writeResourceLocation(this.biomeKey);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.pos = BlockPos.of(buffer.readLong());
      this.biomeKey = buffer.readResourceLocation();
   }

   @Override
   public void handlePacket(Player source) {
      this.handleClient(source);
   }

   @OnlyIn(Dist.CLIENT)
   public void handleClient(Player source) {
      ClientLevel world = (ClientLevel)source.level();
      ChunkAccess chunk = world.getChunk(this.pos);
      if (chunk != null) {
         Optional<Holder.Reference<Biome>> biomeRef = IC2.PLATFORM.getBiomeRegistry(world).getHolder(ResourceKey.create(Registries.BIOME, this.biomeKey));
         if (!biomeRef.isEmpty()) {
            Holder<Biome> biome = biomeRef.get();
            int i = 0;

            for (int m = world.getSectionsCount(); i < m; i++) {
               LevelChunkSection section = chunk.getSection(i);
               PalettedContainerRO<Holder<Biome>> palette = section.getBiomes();
               if (palette instanceof PalettedContainer) {
                  PalettedContainer<Holder<Biome>> chunkPalette = (PalettedContainer<Holder<Biome>>)palette;

                  for (int y = 0; y < 4; y++) {
                     chunkPalette.getAndSet(this.pos.getX() & 3, y, this.pos.getZ() & 3, biome);
                  }
               }
            }

            Minecraft.getInstance()
               .levelRenderer
               .setBlocksDirty(
                  this.pos.getX() - 5, world.getMinBuildHeight(), this.pos.getZ() - 5, this.pos.getX() + 5, world.getMaxBuildHeight(), this.pos.getZ() + 5
               );
         }
      }
   }
}
