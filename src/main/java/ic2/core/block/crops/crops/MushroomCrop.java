package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MushroomCrop extends BaseCrop {
   private boolean isRed;
   private Item drop;

   public MushroomCrop(boolean isRed) {
      super((isRed ? "red" : "brown") + "_mushroom", new CropProperties(2, 0, 4, 0, 0, 4), isRed ? "Red" : "Brown", "Food", "Mushroom");
      this.isRed = isRed;
      this.drop = isRed ? Items.RED_MUSHROOM : Items.BROWN_MUSHROOM;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      String color = this.isRed ? "red" : "brown";
      return IC2Textures.getMappedEntriesBlockIC2("crops/shroom/" + color + "_mushroom").get("growing_" + stage);
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.NOTCH;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(this.drop);
   }

   @Override
   public int getGrowthSteps() {
      return 3;
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return super.canGrow(cropTile) && this.secondaryGrowthCondition(cropTile);
   }

   private boolean secondaryGrowthCondition(ICropTile cropTile) {
      return cropTile.getHumidity() > 0;
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(this.drop)};
   }

   @Override
   public boolean hasCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return true;
   }

   @Override
   public Component getCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return (Component)(!this.secondaryGrowthCondition(cropTile)
         ? this.translate("info.crop.ic2.plant.placement_error.humidity", new Object[]{this.translate("info.crop.ic2.plant.placement_error.more")})
         : super.getCustomCropPlaceFailedMessage(cropTile));
   }
}
