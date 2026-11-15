package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BoneFlowerCrop extends BaseCrop {
   public BoneFlowerCrop() {
      super("bone_flower", new CropProperties(6, 2, 0, 0, 0, 0), "Flower", "BoneMeal", "White");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/bone_flower").get("growing_" + stage);
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.NANO_HEART;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Items.BONE_MEAL);
   }

   @Override
   public int getGrowthSteps() {
      return 7;
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return cropTile.getGrowthStage() < 3 ? true : cropTile.isBlockBelow(Blocks.BONE_BLOCK);
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(Items.BONE)};
   }
}
