package ic2.core.platform.corehacks.mixins.client;

import java.util.Map;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockModelShaper.class)
public interface BlockModelShaperAccessor {
   @Accessor("modelByStateCache")
   Map<BlockState, BakedModel> getModelByStateCache();
}
