package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StickReedCrop extends BaseCrop {
   public StickReedCrop() {
      super("stickreed", new CropProperties(4, 2, 0, 1, 0, 1), "Reed", "Resin", "Green");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      String stageTexture = "";
      switch (stage) {
         case 0:
         case 1:
            stageTexture = "growing_" + stage;
            break;
         case 2:
            stageTexture = "growing_reed_" + stage;
            break;
         case 3:
            stageTexture = "growing_sticky_reed_" + stage;
      }

      return IC2Textures.getMappedEntriesBlockIC2("crops/reed").get(stageTexture);
   }

   @Override
   public Component discoveredBy() {
      return Component.literal("raa1337");
   }

   @Override
   public int getGrowthSteps() {
      return 4;
   }

   @Override
   public boolean canBeHarvested(ICropTile cropTile) {
      return cropTile.getGrowthStage() > 1;
   }

   @Override
   public int getAfterHarvestStage(ICropTile cropTile) {
      return cropTile.getGrowthStage() == this.getGrowthSteps() ? 3 - BaseCrop.getRandom(cropTile).nextInt(3) : 1;
   }

   @Override
   public void onEntityCollision(ICropTile cropTile, Entity entity) {
   }

   @Override
   public int getStatInfluence(ICropTile cropTile, int humidity, int nutrients, int air) {
      return (int)((float)humidity * 1.2F + (float)nutrients + (float)air * 0.8F);
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return cropTile.getGrowthStage() == 3 ? 400 : 100;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(IC2Items.STICKY_RESIN);
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return cropTile.getGrowthStage() == this.getGrowthSteps()
         ? new ItemStack[]{new ItemStack(IC2Items.STICKY_RESIN)}
         : new ItemStack[]{new ItemStack(Items.SUGAR_CANE, cropTile.getGrowthStage() - 1)};
   }
}
