package ic2.core.platform.rendering.features.providers;

import ic2.core.platform.rendering.IC2Models;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EmptyProvider implements ITextureProvider {
   public static final ITextureProvider INSTANCE = new EmptyProvider();

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(BlockState state, Direction dir) {
      return IC2Models.getSafeIcon(null);
   }
}
