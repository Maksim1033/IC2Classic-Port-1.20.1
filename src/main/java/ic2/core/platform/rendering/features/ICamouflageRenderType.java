package ic2.core.platform.rendering.features;

import java.util.Objects;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICamouflageRenderType extends IRenderType {
   @OnlyIn(Dist.CLIENT)
   @Override
   default RenderType getType() {
      return null;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   default boolean canRenderInLayer(BlockState state, RenderType type) {
      return Objects.equals(type, RenderType.cutout()) || Objects.equals(type, RenderType.translucent());
   }
}
