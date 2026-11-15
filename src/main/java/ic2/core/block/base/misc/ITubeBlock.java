package ic2.core.block.base.misc;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ITubeBlock {
   float getRadius(BlockState var1);

   @OnlyIn(Dist.CLIENT)
   TextureAtlasSprite getTexture(BlockState var1, boolean var2, Direction var3, Direction var4);

   VoxelShape[] getRealShapes(BlockState var1, BlockGetter var2, BlockPos var3);

   int getHighlightColor(BlockState var1);

   DyeColor getBlockColor(BlockState var1);

   static Direction isClickingAt(Vec3 subHit, Direction base, float radius) {
      float min = 0.5F - radius;
      float max = 0.5F + radius;
      if (subHit.x >= (double)min
         && subHit.x <= (double)max
         && subHit.y >= (double)min
         && subHit.y <= (double)max
         && subHit.z >= (double)min
         && subHit.z <= (double)max) {
         return base;
      } else if (subHit.x >= 0.0
         && subHit.x <= (double)min
         && subHit.y >= (double)min
         && subHit.y <= (double)max
         && subHit.z >= (double)min
         && subHit.z <= (double)max) {
         return Direction.WEST;
      } else if (subHit.x >= (double)max
         && subHit.x <= 1.0
         && subHit.y >= (double)min
         && subHit.y <= (double)max
         && subHit.z >= (double)min
         && subHit.z <= (double)max) {
         return Direction.EAST;
      } else if (subHit.x >= (double)min
         && subHit.x <= (double)max
         && subHit.y >= 0.0
         && subHit.y <= (double)min
         && subHit.z >= (double)min
         && subHit.z <= (double)max) {
         return Direction.DOWN;
      } else if (subHit.x >= (double)min
         && subHit.x <= (double)max
         && subHit.y >= (double)max
         && subHit.y <= 1.0
         && subHit.z >= (double)min
         && subHit.z <= (double)max) {
         return Direction.UP;
      } else if (subHit.x >= (double)min
         && subHit.x <= (double)max
         && subHit.y >= (double)min
         && subHit.y <= (double)max
         && subHit.z >= 0.0
         && subHit.z <= (double)min) {
         return Direction.NORTH;
      } else {
         return subHit.x >= (double)min
               && subHit.x <= (double)max
               && subHit.y >= (double)min
               && subHit.y <= (double)max
               && subHit.z >= (double)max
               && subHit.z <= 1.0
            ? Direction.SOUTH
            : null;
      }
   }

   static AABB getHitBox(Vec3 subHit, Direction base, float radius) {
      float min = 0.5F - radius;
      float max = 0.5F + radius;
      if (subHit.x >= (double)min
         && subHit.x <= (double)max
         && subHit.y >= (double)min
         && subHit.y <= (double)max
         && subHit.z >= (double)min
         && subHit.z <= (double)max) {
         return new AABB((double)min, (double)min, (double)min, (double)max, (double)max, (double)max);
      } else if (subHit.x >= 0.0
         && subHit.x <= (double)min
         && subHit.y >= (double)min
         && subHit.y <= (double)max
         && subHit.z >= (double)min
         && subHit.z <= (double)max) {
         return new AABB(0.0, (double)min, (double)min, (double)min, (double)max, (double)max);
      } else if (subHit.x >= (double)max
         && subHit.x <= 1.0
         && subHit.y >= (double)min
         && subHit.y <= (double)max
         && subHit.z >= (double)min
         && subHit.z <= (double)max) {
         return new AABB((double)max, (double)min, (double)min, 1.0, (double)max, (double)max);
      } else if (subHit.x >= (double)min
         && subHit.x <= (double)max
         && subHit.y >= 0.0
         && subHit.y <= (double)min
         && subHit.z >= (double)min
         && subHit.z <= (double)max) {
         return new AABB((double)min, 0.0, (double)min, (double)max, (double)min, (double)max);
      } else if (subHit.x >= (double)min
         && subHit.x <= (double)max
         && subHit.y >= (double)max
         && subHit.y <= 1.0
         && subHit.z >= (double)min
         && subHit.z <= (double)max) {
         return new AABB((double)min, (double)max, (double)min, (double)max, 1.0, (double)max);
      } else if (subHit.x >= (double)min
         && subHit.x <= (double)max
         && subHit.y >= (double)min
         && subHit.y <= (double)max
         && subHit.z >= 0.0
         && subHit.z <= (double)min) {
         return new AABB((double)min, (double)min, 0.0, (double)max, (double)max, (double)min);
      } else {
         return subHit.x >= (double)min
               && subHit.x <= (double)max
               && subHit.y >= (double)min
               && subHit.y <= (double)max
               && subHit.z >= (double)max
               && subHit.z <= 1.0
            ? new AABB((double)min, (double)min, (double)max, (double)max, (double)max, 1.0)
            : null;
      }
   }
}
