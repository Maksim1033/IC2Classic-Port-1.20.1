package ic2.api.tiles.display;

import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IDisplayRegistry {
   void register(ResourceLocation var1, Class<? extends IDisplayInfo> var2, Function<FriendlyByteBuf, IDisplayInfo> var3);

   void serialize(IDisplayInfo var1, FriendlyByteBuf var2);

   IDisplayInfo deserialize(FriendlyByteBuf var1);

   public static class DelegateRegistry implements IDisplayRegistry {
      IDisplayRegistry registry;

      public void setRegistry(IDisplayRegistry registry) {
         this.registry = registry;
      }

      @Override
      public void register(ResourceLocation id, Class<? extends IDisplayInfo> info, Function<FriendlyByteBuf, IDisplayInfo> creator) {
         if (this.registry == null) {
            throw new UnsupportedOperationException();
         } else {
            this.registry.register(id, info, creator);
         }
      }

      @Override
      public void serialize(IDisplayInfo info, FriendlyByteBuf buffer) {
         if (this.registry == null) {
            throw new UnsupportedOperationException();
         } else {
            this.registry.serialize(info, buffer);
         }
      }

      @Override
      public IDisplayInfo deserialize(FriendlyByteBuf buffer) {
         if (this.registry == null) {
            throw new UnsupportedOperationException();
         } else {
            return this.registry.deserialize(buffer);
         }
      }
   }
}
