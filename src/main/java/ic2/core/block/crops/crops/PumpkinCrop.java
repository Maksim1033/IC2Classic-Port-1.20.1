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

public class PumpkinCrop extends BaseSeedCrop {
   public PumpkinCrop() {
      super("pumpkin", new CropProperties(1, 0, 1, 0, 3, 1), "Orange", "Decoration", "Stem", "Food");
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
            stageTexture = "growing_pumpkin_" + stage;
      }

      return IC2Textures.getMappedEntriesBlockIC2("crops/stem").get(stageTexture);
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.NOTCH;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Items.PUMPKIN);
   }

   @Override
   public int getGrowthSteps() {
      return 4;
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return cropTile.getGrowthStage() == 3 ? 600 : 200;
   }

   @Override
   public int getStatInfluence(ICropTile cropTile, int humidity, int nutrients, int air) {
      return (int)((float)humidity * 1.1F + (float)nutrients * 0.9F + (float)air);
   }

   @Override
   public int getAfterHarvestStage(ICropTile cropTile) {
      return 3;
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(Items.PUMPKIN)};
   }

   @Override
   public ItemStack[] getSeedDrops(ICropTile tile) {
      return new ItemStack[]{new ItemStack(Items.PUMPKIN_SEEDS)};
   }
}
