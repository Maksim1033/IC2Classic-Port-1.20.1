package ic2.api.tiles.display;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IDisplayInfo {
   IDisplayRegistry REGISTRY = new IDisplayRegistry.DelegateRegistry();

   @OnlyIn(Dist.CLIENT)
   void render(PoseStack var1, int var2, int var3, int var4, int var5, IDisplayInfo.Alignment var6, IMonitorRenderer var7);

   @OnlyIn(Dist.CLIENT)
   int getHeight(int var1, IDisplayInfo.Alignment var2);

   boolean isValid();

   void serialize(FriendlyByteBuf var1);

   Tag getServerData();

   public static enum Alignment {
      LEFT,
      CENTER,
      RIGHT;

      public int getXOffset(int width) {
         return this == CENTER ? width >> 1 : (this == RIGHT ? width : 0);
      }
   }
}
