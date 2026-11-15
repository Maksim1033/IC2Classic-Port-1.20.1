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

public class CocoaCrop extends BaseCrop {
   public CocoaCrop() {
      super("cocoa", new CropProperties(3, 1, 3, 0, 4, 0), "Brown", "Food", "Stem", "Ingredient");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/cocoa").get("growing_" + stage);
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.NOTCH;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Items.COCOA_BEANS);
   }

   @Override
   public int getGrowthSteps() {
      return 4;
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return cropTile.getGrowthStage() == 3 ? 900 : 400;
   }

   @Override
   public int getStatInfluence(ICropTile cropTile, int humidity, int nutrients, int air) {
      return (int)((float)humidity * 0.8F + (float)nutrients * 1.3F + (float)air * 0.9F);
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return super.canGrow(cropTile) && this.secondaryGrowthCondition(cropTile);
   }

   private boolean secondaryGrowthCondition(ICropTile cropTile) {
      return cropTile.getNutrients() >= 3;
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(Items.COCOA_BEANS)};
   }

   @Override
   public boolean hasCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return true;
   }

   @Override
   public Component getCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return (Component)(!this.secondaryGrowthCondition(cropTile)
         ? this.translate("info.crop.ic2.plant.placement_error.nutrients", new Object[]{this.translate("info.crop.ic2.plant.placement_error.more")})
         : super.getCustomCropPlaceFailedMessage(cropTile));
   }
}
