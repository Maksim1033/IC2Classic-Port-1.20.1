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

public class MelonCrop extends BaseSeedCrop {
   public MelonCrop() {
      super("melon", new CropProperties(2, 0, 4, 0, 2, 0), "Green", "Food", "Stem", "Red");
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
            stageTexture = "growing_melon_" + stage;
      }

      return IC2Textures.getMappedEntriesBlockIC2("crops/stem").get(stageTexture);
   }

   @Override
   public Component discoveredBy() {
      return this.string("Chao");
   }

   @Override
   public int getGrowthSteps() {
      return 4;
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return cropTile.getGrowthStage() == 3 ? 700 : 250;
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
      return BaseCrop.getRandom(cropTile).nextInt(3) == 0
         ? new ItemStack[]{new ItemStack(Items.MELON)}
         : new ItemStack[]{new ItemStack(Items.MELON_SLICE, BaseCrop.getRandom(cropTile).nextInt(4) + 2)};
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Items.MELON_SLICE);
   }

   @Override
   public ItemStack[] getSeedDrops(ICropTile tile) {
      return new ItemStack[]{new ItemStack(Items.MELON_SEEDS)};
   }
}
