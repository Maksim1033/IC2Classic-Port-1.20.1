package ic2.core.platform.rendering.features.providers;

import ic2.core.platform.rendering.features.ITextureProvider;
import java.util.function.IntSupplier;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StateProvider implements ITextureProvider {
   ITextureProvider[] providers;
   IntSupplier decider;

   public StateProvider(IntSupplier decider, ITextureProvider... providers) {
      this.providers = providers;
      this.decider = decider;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(BlockState state, Direction dir) {
      return this.providers[this.decider.getAsInt()].getTexture(state, dir);
   }
}
