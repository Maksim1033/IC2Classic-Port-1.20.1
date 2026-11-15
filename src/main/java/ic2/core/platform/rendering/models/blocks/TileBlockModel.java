package ic2.core.platform.rendering.models.blocks;

import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.ITileParticleTexture;
import ic2.core.platform.rendering.models.BaseModel;
import net.minecraft.world.level.block.state.BlockState;

public class TileBlockModel extends BaseModel {
   BlockState state;

   public TileBlockModel(BlockState state) {
      this.state = state;
   }

   @Override
   public void init() {
      if (this.state.getBlock() instanceof ITileParticleTexture) {
         this.setParticleTexture(((ITileParticleTexture)this.state.getBlock()).getParticleTexture(this.state));
      } else if (this.state.getBlock() instanceof IBlockModel) {
         this.setParticleTexture(((IBlockModel)this.state.getBlock()).getSpriteForParticle(this.state));
      } else {
         this.setParticleTexture(null);
      }
   }

   @Override
   public boolean isCustomRenderer() {
      return true;
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }
}
