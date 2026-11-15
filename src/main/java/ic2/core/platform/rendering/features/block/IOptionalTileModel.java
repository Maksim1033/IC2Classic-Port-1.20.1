package ic2.core.platform.rendering.features.block;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;

public interface IOptionalTileModel extends ITileModel, IBlockModel {
   boolean shouldTileRender();

   @Override
   default List<BlockState> getCustomStates() {
      return (List<BlockState>)(this.shouldTileRender() ? ITileModel.super.getCustomStates() : ObjectLists.emptyList());
   }

   @Override
   default List<BlockState> getModelStates() {
      return (List<BlockState>)(this.shouldTileRender() ? ObjectLists.emptyList() : IBlockModel.super.getModelStates());
   }
}
