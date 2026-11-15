package ic2.core.platform.rendering.features.providers;

import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VanillaProvider implements ITextureProvider {
   ResourceLocation location;

   public VanillaProvider(ResourceLocation location) {
      this.location = location;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(BlockState state, Direction dir) {
      return (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(this.location);
   }
}
