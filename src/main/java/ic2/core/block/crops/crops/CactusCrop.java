package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.corehacks.mixins.server.BiomeMixin;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CactusCrop extends BaseCrop {
   public CactusCrop() {
      super("cactus", new CropProperties(2, 0, 0, 4, 4, 0), "Green", "Cactus", "Thorns");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/cactus").get("growing_" + stage);
   }

   @Override
   public Component discoveredBy() {
      return this.string("SpwnX");
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Items.CACTUS);
   }

   @Override
   public int getGrowthSteps() {
      return 4;
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return 300;
   }

   @Override
   public int getStatInfluence(ICropTile cropTile, int humidity, int nutrients, int air) {
      int count = super.getStatInfluence(cropTile, humidity, nutrients, air);
      return count + (int)(10.0F * this.getBiomeTemperature(cropTile));
   }

   private float getBiomeTemperature(ICropTile cropTile) {
      return ((BiomeMixin)(Object)cropTile.getWorldObj().getBiome(cropTile.getPosition()).value()).getTemp(cropTile.getPosition()) - 0.5F;
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return super.canGrow(cropTile) && this.secondaryGrowthCondition(cropTile);
   }

   private boolean secondaryGrowthCondition(ICropTile cropTile) {
      return cropTile.getEnvironmentQuality() >= 2;
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      int count = 1;
      if (BaseCrop.getRandom(cropTile).nextInt(3) == 0) {
         count = 2;
      }

      return new ItemStack[]{new ItemStack(Items.CACTUS, count)};
   }

   @Override
   public void onEntityCollision(ICropTile cropTile, Entity entity) {
      if (!(entity instanceof ItemEntity) && !cropTile.getWorldObj().isClientSide) {
         entity.hurt(cropTile.getWorldObj().damageSources().cactus(), 1.0F);
      }

      super.onEntityCollision(cropTile, entity);
   }

   @Override
   public boolean hasCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return true;
   }

   @Override
   public Component getCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return (Component)(!this.secondaryGrowthCondition(cropTile)
         ? this.translate(
            "info.crop.ic2.plant.placement_error.quality",
            new Object[]{this.translate("info.crop.ic2.plant.placement_error.better"), this.translate("info.crop.ic2.plant.placement_error.air")}
         )
         : super.getCustomCropPlaceFailedMessage(cropTile));
   }
}
