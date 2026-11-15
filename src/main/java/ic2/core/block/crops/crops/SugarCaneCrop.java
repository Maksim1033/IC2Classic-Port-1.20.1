package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SugarCaneCrop extends BaseCrop {
   public SugarCaneCrop() {
      super("sugarcane", new CropProperties(2, 0, 0, 1, 0, 2), "Reed", "Green");
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
      }

      return IC2Textures.getMappedEntriesBlockIC2("crops/reed").get(stageTexture);
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.NOTCH;
   }

   @Override
   public int getGrowthSteps() {
      return 3;
   }

   @Override
   public boolean canBeHarvested(ICropTile cropTile) {
      return cropTile.getGrowthStage() > 1;
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
      return 200;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Items.SUGAR_CANE);
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(Items.SUGAR_CANE, cropTile.getGrowthStage() - 1)};
   }
}
