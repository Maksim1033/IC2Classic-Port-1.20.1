package ic2.probeplugin.base;

import java.util.function.Function;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class BaseFactory implements IElementFactory {
   ResourceLocation id;
   Function<FriendlyByteBuf, IElement> builder;

   public BaseFactory(ResourceLocation id, Function<FriendlyByteBuf, IElement> builder) {
      this.id = id;
      this.builder = builder;
   }

   public IElement createElement(FriendlyByteBuf buffer) {
      return this.builder.apply(buffer);
   }

   public ResourceLocation getId() {
      return this.id;
   }
}
