package ic2.core.platform.rendering.features.block;

import ic2.core.platform.rendering.models.ShapeBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockModifiers {
   boolean hasTextureRotation(BlockState var1, Direction var2);

   int getTextureRotation(BlockState var1, Direction var2);

   boolean hasCustomTextureUVs(BlockState var1, Direction var2);

   float[] getCustomTextureUVs(BlockState var1, Direction var2);

   public interface IAutoUV extends IBlockModifiers {
      @Override
      default boolean hasTextureRotation(BlockState state, Direction side) {
         return false;
      }

      @Override
      default int getTextureRotation(BlockState state, Direction side) {
         return 0;
      }

      @Override
      default boolean hasCustomTextureUVs(BlockState state, Direction side) {
         return this instanceof IBlockModel;
      }

      @Override
      default float[] getCustomTextureUVs(BlockState state, Direction side) {
         return ShapeBuilder.uvsByFace(side, ((IBlockModel)this).getModelBounds(state));
      }
   }
}
