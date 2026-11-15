package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICrop;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.tooltips.ILangHelper;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class BaseCrop extends ICrop implements ILangHelper {
   public static final Component ALBLAKA = Component.literal("Alblaka");
   public static final Component NOTCH = Component.literal("Notch");
   public static final Component SPEIGER = Component.literal("Speiger");
   public static final Component RAZZOKK = Component.literal("Razzokk");
   public static final Component DAENARA = Component.literal("Daenara");
   public static final Component MOJANG = Component.literal("Mojang");
   public static final Component NANO_HEART = Component.literal("NanoHeart");
   public static final Component CROSS_VAS = Component.literal("CrossVas");
   private String name = "";
   private ResourceLocation id;
   private CropProperties props;
   private String[] attributes;
   public static final Object2IntLinkedOpenHashMap<String> TAG_MAP = new Object2IntLinkedOpenHashMap();

   public BaseCrop(String name, CropProperties props, String... attributes) {
      this(new ResourceLocation(IC2.MOD_ID + ":" + name), props, attributes);
      this.name = name;
   }

   public BaseCrop(ResourceLocation id, CropProperties props, String... attributes) {
      this.id = id;
      this.props = props;
      this.attributes = attributes;

      for (String attribute : attributes) {
         TAG_MAP.addTo(attribute, 1);
      }
   }

   @Override
   public ResourceLocation id() {
      return this.id;
   }

   @Override
   public Component getCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return this.string();
   }

   @Override
   public Component getName() {
      if (this.name.isEmpty()) {
         IC2.LOGGER.error("When using BaseCrop Constructor with a ResourceLocation overwrite getName()!");
         throw new RuntimeException();
      } else {
         return this.translate("crop.ic2." + this.name);
      }
   }

   @Override
   public CropProperties getProperties() {
      return this.props;
   }

   @Override
   public String[] getAttributes() {
      return this.attributes;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<ResourceLocation> getTextures() {
      List<ResourceLocation> locations = CollectionUtils.<ResourceLocation>createList();

      for (int i = 0; i < this.getGrowthSteps(); i++) {
         locations.add(this.getTexture(i).contents().name());
      }

      return locations;
   }

   @OnlyIn(Dist.CLIENT)
   public abstract TextureAtlasSprite getTexture(int var1);

   public static RandomSource getRandom(ICropTile cropTile) {
      return cropTile.getWorldObj().getRandom();
   }
}
