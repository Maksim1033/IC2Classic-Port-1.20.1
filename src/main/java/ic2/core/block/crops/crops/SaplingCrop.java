package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SaplingCrop extends BaseSeedCrop {
   private String name;
   private ItemLike drop;
   private ItemLike seed;

   public SaplingCrop(String name, ItemLike drop, ItemLike seed) {
      super("sapling_" + name, new CropProperties(3, 1, 0, 4, 4, 0), "Leaves", "Sapling", "Green");
      this.name = name;
      this.drop = drop;
      this.seed = seed;
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
         case 4:
            stageTexture = "growing_" + this.name + "_" + stage;
      }

      return IC2Textures.getMappedEntriesBlockIC2("crops/sapling").get(stageTexture);
   }

   @Override
   public int getGrowthSteps() {
      return 5;
   }

   @Override
   public int getAfterHarvestStage(ICropTile cropTile) {
      return 4;
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return super.canGrow(cropTile) && this.secondaryGrowthCondition(cropTile);
   }

   private boolean secondaryGrowthCondition(ICropTile cropTile) {
      return cropTile.getLightLevel() >= 9;
   }

   @Override
   public Component discoveredBy() {
      return "rubberwood".equals(this.name) ? BaseCrop.CROSS_VAS : BaseCrop.NOTCH;
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return cropTile.getGrowthStage() == 4 ? 150 : super.getGrowthDuration(cropTile);
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(this.drop)};
   }

   @Override
   public ItemStack[] getSeedDrops(ICropTile tile) {
      return new ItemStack[]{new ItemStack(this.seed)};
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(this.drop);
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
