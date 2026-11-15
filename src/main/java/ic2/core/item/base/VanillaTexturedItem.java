package ic2.core.item.base;

import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VanillaTexturedItem extends IC2Item implements ISimpleItemModel {
   ResourceLocation texture;

   public VanillaTexturedItem(String itemName, ResourceLocation texture) {
      super(itemName);
      this.texture = texture;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(this.texture);
   }
}
