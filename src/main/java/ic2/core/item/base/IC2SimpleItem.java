package ic2.core.item.base;

import ic2.core.IC2;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2SimpleItem extends IC2Item implements ISimpleItemModel {
   protected String textureName;
   protected String textureFolder;
   private boolean disableEnchantments = false;

   public IC2SimpleItem(String itemName, @Nullable PropertiesBuilder properties, String textureFolder, String textureName) {
      super(itemName, (properties == null ? new PropertiesBuilder() : properties).group(IC2.IC2_MAIN_GROUP));
      this.textureFolder = textureFolder;
      this.textureName = textureName;
   }

   public IC2SimpleItem(String itemName, String textureFolder, String textureName) {
      this(itemName, null, textureFolder, textureName);
   }

   public IC2SimpleItem disableEnchantments() {
      this.disableEnchantments = true;
      return this;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return this.textureName.isEmpty() ? null : IC2Textures.getMappedEntriesItem(this.id.getNamespace(), this.textureFolder).get(this.textureName);
   }

   public boolean isEnchantable(ItemStack p_77616_1_) {
      return super.isEnchantable(p_77616_1_) && !this.disableEnchantments;
   }
}
