package ic2.api.tiles.tubes;

import java.util.UUID;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public interface IRequestTube extends ITube {
   void provideRequests(IRequestTube.ITubeRequester var1);

   void onRequestsReset();

   void onRequestFulfilled(ItemStack var1, int var2);

   UUID getRequestId();

   void onRequestLost(ItemStack var1, int var2);

   long getRequestSource();

   public interface ITubeRequester {
      void requestItems(ItemStack var1, int var2, DyeColor var3, UUID var4);

      int validateRequest(ItemStack var1, int var2, DyeColor var3);
   }
}
