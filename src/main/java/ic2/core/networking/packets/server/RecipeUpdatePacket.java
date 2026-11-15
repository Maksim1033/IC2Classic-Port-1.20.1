package ic2.core.networking.packets.server;

import ic2.core.IC2;
import ic2.core.block.machines.recipes.IRecipeList;
import ic2.core.networking.packets.IC2Packet;
import ic2.core.platform.recipes.misc.GlobalRecipes;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RecipeUpdatePacket extends IC2Packet {
   byte[] data;

   public RecipeUpdatePacket() {
   }

   public RecipeUpdatePacket(Map<ResourceLocation, IRecipeList> recipes) {
      this.data = this.toByteArray(recipes);
   }

   private byte[] toByteArray(Map<ResourceLocation, IRecipeList> recipes) {
      FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
      buffer.writeVarInt(recipes.size());

      for (Entry<ResourceLocation, IRecipeList> entry : recipes.entrySet()) {
         buffer.writeResourceLocation(entry.getKey());
         entry.getValue().writeRecipes(buffer);
      }

      byte[] data = new byte[buffer.writerIndex()];
      buffer.readBytes(data);
      return data;
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.writeByteArray(this.data);
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.data = buffer.readByteArray();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void handlePacket(Player source) {
      FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(this.data));
      GlobalRecipes recipes = IC2.RECIPES.get(false);
      int left = buffer.readVarInt();

      for (int i = 0; i < left; i++) {
         recipes.getList(buffer.readResourceLocation()).readRecipes(buffer);
      }
   }
}
