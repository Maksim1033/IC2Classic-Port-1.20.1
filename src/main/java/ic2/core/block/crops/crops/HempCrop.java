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

public class HempCrop extends BaseSeedCrop {
   public HempCrop() {
      super("hemp", new CropProperties(3, 2, 2, 0, 2, 0), "Leaves", "Ingredient", "Green");
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.SPEIGER;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/hemp").get("growing_" + stage);
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(IC2Items.HEMP);
   }

   @Override
   public int getGrowthSteps() {
      return 6;
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return cropTile.getGrowthStage() >= 4 ? 100 : super.getGrowthDuration(cropTile);
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return super.canGrow(cropTile) && this.secondaryGrowthCondition(cropTile);
   }

   private boolean secondaryGrowthCondition(ICropTile cropTile) {
      return cropTile.getLightLevel() >= 7;
   }

   @Override
   public int getAfterHarvestStage(ICropTile cropTile) {
      return 4;
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(IC2Items.HEMP)};
   }

   @Override
   public ItemStack[] getSeedDrops(ICropTile tile) {
      return new ItemStack[]{new ItemStack(IC2Items.HEMP_SEEDS)};
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
