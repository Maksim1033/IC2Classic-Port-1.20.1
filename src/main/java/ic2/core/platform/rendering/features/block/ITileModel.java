package ic2.core.platform.rendering.features.block;

import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.blocks.TileBlockModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ITileModel extends ICustomBlockModel {
   @OnlyIn(Dist.CLIENT)
   @Override
   default BaseModel getForCustomState(BlockState state) {
      return new TileBlockModel(state);
   }
}
