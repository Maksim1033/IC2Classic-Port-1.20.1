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

public class BambooCrop extends BaseCrop {
   public BambooCrop() {
      super("bamboo", new CropProperties(2, 0, 0, 0, 2, 0), "Green", "Reed", "Leaves");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/bamboo").get("growing_" + stage);
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.MOJANG;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Items.BAMBOO);
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return super.canGrow(cropTile) && this.secondaryGrowthCondition(cropTile);
   }

   private boolean secondaryGrowthCondition(ICropTile cropTile) {
      return cropTile.getHumidity() <= 4;
   }

   @Override
   public int getStatInfluence(ICropTile cropTile, int humidity, int nutrients, int air) {
      return (int)((float)humidity * 0.8F + (float)nutrients + (float)air * 1.2F);
   }

   @Override
   public int getGrowthSteps() {
      return 6;
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return 125;
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(Items.BAMBOO)};
   }

   @Override
   public boolean hasCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return true;
   }

   @Override
   public Component getCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return (Component)(!this.secondaryGrowthCondition(cropTile)
         ? this.translate("info.crop.ic2.plant.placement_error.humidity", new Object[]{this.translate("info.crop.ic2.plant.placement_error.less")})
         : super.getCustomCropPlaceFailedMessage(cropTile));
   }
}
