package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.block.crops.CropRegistry;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TerraWartCrop extends BaseCrop {
   public TerraWartCrop() {
      super("terrawart", new CropProperties(5, 2, 4, 0, 3, 0), "Blue", "Aether", "Food", "Snow", "Wart");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/shroom/terrawart").get("growing_" + stage);
   }

   @Override
   public int getGrowthSteps() {
      return 3;
   }

   @Override
   public double getDropChance(ICropTile cropTile) {
      return 0.8;
   }

   @Override
   public void onTick(ICropTile cropTile) {
      if (cropTile.isBlockBelow(Blocks.SNOW_BLOCK)) {
         if (this.canGrow(cropTile) && cropTile.getGrowthStage() < cropTile.getCrop().getGrowthSteps()) {
            cropTile.setGrowthPoints(cropTile.getGrowthPoints() + cropTile.calculateGrowthSpeed() / 2);
         }
      } else if (cropTile.isBlockBelow(Blocks.SOUL_SAND) && BaseCrop.getRandom(cropTile).nextInt(300) == 0) {
         cropTile.setCrop(CropRegistry.NETHERWART);
      }
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(IC2Items.TERRA_WART);
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(IC2Items.TERRA_WART)};
   }
}
