package ic2.core.platform.corehacks.mixins.client;

import net.minecraft.client.KeyboardHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin({KeyboardHandler.class})
public interface DebugMixin {
   @Accessor("handledDebugKey")
   void setDebugHandled(boolean var1);
}
