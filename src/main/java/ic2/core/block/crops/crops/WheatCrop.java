package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WheatCrop extends BaseSeedCrop {
   public WheatCrop() {
      super("wheat", new CropProperties(1, 0, 4, 0, 0, 2), "Yellow", "Food", "Wheat");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/wheat").get("growing_" + (stage < 4 ? "" : "wheat_") + stage);
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Items.WHEAT);
   }

   @Override
   public Component discoveredBy() {
      return NOTCH;
   }

   @Override
   public int getGrowthSteps() {
      return 7;
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
      return 2;
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(Items.WHEAT)};
   }

   @Override
   public ItemStack[] getSeedDrops(ICropTile tile) {
      return new ItemStack[]{new ItemStack(Items.WHEAT_SEEDS)};
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
