package ic2.core.platform.rendering.models.items;

import ic2.core.platform.rendering.QuadBaker;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleItemModel extends BaseItemModel<SimpleItemModel> {
   ItemStack stack;

   public SimpleItemModel(ItemStack stack, TextureAtlasSprite sprite) {
      this.stack = stack;
      this.setParticleTexture(sprite);
   }

   private SimpleItemModel() {
   }

   @Override
   public void init() {
      this.quads.addAll(QuadBaker.createQuadsFromTexture(this.getTintedIndex(this.stack, 0), this.getParticleIcon(), this.getTransformMap().gui));
      this.initOther(new SimpleItemModel());
   }
}
