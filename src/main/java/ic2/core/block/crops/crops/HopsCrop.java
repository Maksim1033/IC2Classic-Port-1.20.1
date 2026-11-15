package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HopsCrop extends BaseCrop {
   public HopsCrop() {
      super("hops", new CropProperties(5, 2, 2, 0, 1, 1), "Green", "Ingredient", "Wheat");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      String stageTexture = "";
      switch (stage) {
         case 0:
         case 1:
         case 2:
         case 3:
            stageTexture = "growing_" + stage;
            break;
         case 4:
         case 5:
         case 6:
            stageTexture = "growing_hops_" + stage;
      }

      return IC2Textures.getMappedEntriesBlockIC2("crops/wheat").get(stageTexture);
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.ALBLAKA;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(IC2Items.HOPS);
   }

   @Override
   public int getGrowthSteps() {
      return 7;
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return 600;
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return super.canGrow(cropTile) && this.secondaryGrowthCondition(cropTile);
   }

   private boolean secondaryGrowthCondition(ICropTile cropTile) {
      return cropTile.getLightLevel() >= 9;
   }

   @Override
   public int getAfterHarvestStage(ICropTile cropTile) {
      return 3;
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(IC2Items.HOPS)};
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
