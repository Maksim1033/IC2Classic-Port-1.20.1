package ic2.core.platform.rendering.features.providers;

import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NoStateProvider implements ITextureProvider {
   String mod;
   String path;

   public NoStateProvider(String mod, String path) {
      this.mod = mod;
      this.path = path;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(BlockState state, Direction dir) {
      return IC2Textures.getMappedEntriesBlock(this.mod, this.path).get(dir.getName());
   }
}
