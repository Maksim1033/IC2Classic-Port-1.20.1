package ic2.core.item.tool.simple;

import ic2.core.IC2;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.platform.rendering.features.item.IToolModel;
import ic2.core.utils.plugins.IRegistryProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class ShovelTool extends ShovelItem implements ISimpleItemModel, IToolModel, IRegistryProvider {
   private String textureName;
   private String textureFolder;
   private ResourceLocation id;

   public ShovelTool(String itemName, Properties properties, String textureFolder, String textureName, Tier tier) {
      super(tier, 1.5F, -3.0F, properties);
      this.id = GameData.checkPrefix(itemName, false);
      this.textureFolder = textureFolder;
      this.textureName = textureName;
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   public ShovelTool(String itemName, String textureFolder, String textureName, Tier tier) {
      this(itemName, new Properties(), textureFolder, textureName, tier);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), this.textureFolder).get(this.textureName);
   }
}
