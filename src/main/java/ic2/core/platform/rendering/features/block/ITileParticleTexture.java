package ic2.core.platform.rendering.features.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ITileParticleTexture {
   @OnlyIn(Dist.CLIENT)
   TextureAtlasSprite getParticleTexture(BlockState var1);
}
