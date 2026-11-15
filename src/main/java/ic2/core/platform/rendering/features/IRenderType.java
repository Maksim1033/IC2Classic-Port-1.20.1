package ic2.core.platform.rendering.features;

import com.google.common.base.Objects;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IRenderType {
   @OnlyIn(Dist.CLIENT)
   RenderType getType();

   @OnlyIn(Dist.CLIENT)
   default boolean canRenderInLayer(BlockState state, RenderType type) {
      return Objects.equal(type, this.getType());
   }
}
