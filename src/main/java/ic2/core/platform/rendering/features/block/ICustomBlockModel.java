package ic2.core.platform.rendering.features.block;

import ic2.core.platform.rendering.models.BaseModel;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICustomBlockModel {
   default List<BlockState> getCustomStates() {
      return ((Block)this).getStateDefinition().getPossibleStates();
   }

   @OnlyIn(Dist.CLIENT)
   BaseModel getForCustomState(BlockState var1);
}
