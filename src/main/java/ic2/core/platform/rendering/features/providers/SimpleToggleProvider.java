package ic2.core.platform.rendering.features.providers;

import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SimpleToggleProvider implements ITextureProvider {
   String mod;
   String path;
   String on;
   String off;

   public SimpleToggleProvider(String mod, String path, String on, String off) {
      this.mod = mod;
      this.path = path;
      this.on = on;
      this.off = off;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlock(this.mod, this.path).get(state.getValue(IC2Properties.ACTIVE) ? this.on : this.off);
   }
}
