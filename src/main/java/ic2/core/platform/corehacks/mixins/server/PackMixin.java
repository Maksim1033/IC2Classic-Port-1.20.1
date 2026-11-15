package ic2.core.platform.corehacks.mixins.server;

import java.util.List;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ForgeHooks.class})
public class PackMixin {
   @Inject(
      method = {"getModPacks"},
      at = {@At("RETURN")},
      cancellable = true,
      remap = false
   )
   private static void onModpackLoad(CallbackInfoReturnable<List<String>> callback) {
      List<String> list = (List<String>)callback.getReturnValue();
      list.add("mod:ic2c");
      callback.setReturnValue(list);
   }
}
