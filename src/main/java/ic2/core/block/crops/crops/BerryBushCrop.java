package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
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

public class BerryBushCrop extends BaseCrop {
   public BerryBushCrop() {
      super("sweet_berry_bush", new CropProperties(2, 0, 4, 3, 3, 0), "Red", "Green", "Food", "Thorns");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/berry_bush").get("growing_" + stage);
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.MOJANG;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Items.SWEET_BERRIES);
   }

   @Override
   public int getGrowthSteps() {
      return 5;
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return cropTile.getGrowthStage() >= 2 ? 200 : super.getGrowthDuration(cropTile);
   }

   @Override
   public int getOptimalHarvestStep(ICropTile cropTile) {
      return this.getGrowthSteps();
   }

   @Override
   public boolean canBeHarvested(ICropTile cropTile) {
      return cropTile.getGrowthStage() >= 4;
   }

   @Override
   public int getAfterHarvestStage(ICropTile cropTile) {
      return 2;
   }

   @Override
   public void onEntityCollision(ICropTile cropTile, Entity entity) {
      if (!(entity instanceof ItemEntity) && !cropTile.getWorldObj().isClientSide && !entity.isShiftKeyDown()) {
         entity.hurt(cropTile.getWorldObj().damageSources().sweetBerryBush(), 1.0F);
      }

      super.onEntityCollision(cropTile, entity);
   }

   @Override
   public double getDropChance(ICropTile cropTile) {
      return cropTile.getGrowthStage() == 4 ? super.getDropChance(cropTile) / 2.0 : super.getDropChance(cropTile);
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(Items.SWEET_BERRIES)};
   }
}
