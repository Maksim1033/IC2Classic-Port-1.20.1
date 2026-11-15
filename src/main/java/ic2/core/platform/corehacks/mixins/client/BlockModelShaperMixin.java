package ic2.core.platform.corehacks.mixins.client;

import ic2.core.platform.rendering.IC2Models;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {
   @Shadow
   @Final
   private ModelManager modelManager;

   @Inject(method = "getBlockModel", at = @At("HEAD"), cancellable = true)
   private void ic2$getBlockModel(BlockState state, CallbackInfoReturnable<BakedModel> cir) {
      BakedModel custom = IC2Models.INSTANCE.getBlockModel(state);
      if (custom != null) {
         cir.setReturnValue(custom);
      }
   }

   @Inject(method = "getBlockModel", at = @At("RETURN"), cancellable = true)
   private void ic2$fallbackMissingModel(BlockState state, CallbackInfoReturnable<BakedModel> cir) {
      if (cir.getReturnValue() == this.modelManager.getMissingModel()) {
         BakedModel custom = this.modelManager.getModel(BlockModelShaper.stateToModelLocation(state));
         if (custom != null && custom != this.modelManager.getMissingModel()) {
            cir.setReturnValue(custom);
         }
      }
   }
}
