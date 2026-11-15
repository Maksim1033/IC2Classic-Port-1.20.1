package ic2.core.block.crops.crops;

import com.google.common.collect.ObjectArrays;
import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DyeFlowerCrop extends BaseCrop {
   private String name;
   private DyeColor color;
   private Component discovered;

   public DyeFlowerCrop(String name, DyeColor color, Component discovered, CropProperties properties, String... attributes) {
      super(name, properties, (String[])ObjectArrays.concat("Flower", attributes));
      this.name = name;
      this.color = color;
      this.discovered = discovered;
   }

   public DyeFlowerCrop(String name, DyeColor color, Component discovered, String... attributes) {
      this(name, color, discovered, new CropProperties(2, 1, 1, 0, 5, 1), attributes);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      String stageTexture = "";
      switch (stage) {
         case 0:
         case 1:
         case 2:
            stageTexture = "growing_" + stage;
            break;
         case 3:
            stageTexture = "growing_" + this.name + "_" + stage;
      }

      return IC2Textures.getMappedEntriesBlockIC2("crops/flowers").get(stageTexture);
   }

   @Override
   public int getGrowthSteps() {
      return 4;
   }

   @Override
   public int getAfterHarvestStage(ICropTile cropTile) {
      return 3;
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return super.canGrow(cropTile) && this.secondaryGrowthCondition(cropTile);
   }

   private boolean secondaryGrowthCondition(ICropTile cropTile) {
      return cropTile.getLightLevel() >= 12;
   }

   @Override
   public Component discoveredBy() {
      return this.discovered;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(DyeItem.byColor(this.color));
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(DyeItem.byColor(this.color))};
   }

   @Override
   public boolean hasCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return true;
   }

   @Override
   public Component getCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return (Component)(!this.secondaryGrowthCondition(cropTile)
         ? this.translate("info.crop.ic2.plant.placement_error.light", new Object[]{this.translate("info.crop.ic2.plant.placement_error.more")})
         : super.getCustomCropPlaceFailedMessage(cropTile));
   }
}
