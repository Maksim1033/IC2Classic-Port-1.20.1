package ic2.core.platform.rendering.features.block;

import ic2.core.utils.plugins.IRegistryProvider;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IBlockModel extends IRegistryProvider {
   AABB DEFAULT_BOUNDS = new AABB(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

   default List<BlockState> getModelStates() {
      return ((Block)this).getStateDefinition().getPossibleStates();
   }

   @OnlyIn(Dist.CLIENT)
   TextureAtlasSprite getSpriteForState(BlockState var1, Direction var2);

   @OnlyIn(Dist.CLIENT)
   default TextureAtlasSprite getSpriteForParticle(BlockState state) {
      return this.getSpriteForState(state, Direction.NORTH);
   }

   default AABB getModelBounds(BlockState state) {
      return DEFAULT_BOUNDS;
   }

   default boolean isFullCube(BlockState state) {
      return true;
   }
}
