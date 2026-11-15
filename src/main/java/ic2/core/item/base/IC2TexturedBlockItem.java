package ic2.core.item.base;

import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2TexturedBlockItem extends IC2BlockItem implements ISimpleItemModel {
   private String textureName;
   private String textureFolder;

   public IC2TexturedBlockItem(Block block, Properties properties, String textureFolder, String textureName) {
      super(block, properties);
      this.textureName = textureName;
      this.textureFolder = textureFolder;
   }

   public IC2TexturedBlockItem(Block block, String textureFolder, String textureName) {
      super(block);
      this.textureFolder = textureFolder;
      this.textureName = textureName;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), this.textureFolder).get(this.textureName);
   }
}
