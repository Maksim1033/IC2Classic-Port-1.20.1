package ic2.core.item.misc;

import ic2.core.IC2;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.utils.plugins.IRegistryProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.GameData;

public class IC2SignItem extends SignItem implements ISimpleItemModel, IRegistryProvider {
   private String folder;
   private String texture;
   private ResourceLocation id;

   public IC2SignItem(String name, Block sign, Block wallSign, String folder, String texture) {
      super(new Properties().stacksTo(16), sign, wallSign);
      this.id = GameData.checkPrefix(name, false);
      this.folder = folder;
      this.texture = texture;
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), this.folder).get(this.texture);
   }
}
