package ic2.core.platform.rendering.models.blocks;

import ic2.core.platform.rendering.features.block.IBlockModel;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class NonTranslucentBlockModel extends SimpleBlockModel {
   public NonTranslucentBlockModel(BlockState state, IBlockModel model) {
      super(state, model);
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType type) {
      return this.isTranslucent(type) ? empty() : this.quads[side == null ? 6 : side.get3DDataValue()];
   }
}
