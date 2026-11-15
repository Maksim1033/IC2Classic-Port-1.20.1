package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

public class LumiliaCrop extends BaseCrop {
   public LumiliaCrop() {
      super("lumilia", new CropProperties(6, 3, 0, 0, 1, 0), "Yellow", "Leaves", "Flower", "Light");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/lumilia").get("growing_" + stage);
   }

   @Override
   public int getEmittedLight(ICropTile cropTile) {
      return cropTile.getGrowthStage() == this.getGrowthSteps() ? 15 : 0;
   }

   @Override
   public int getGrowthSteps() {
      return 4;
   }

   @Override
   public int getAfterHarvestStage(ICropTile cropTile) {
      return 3;
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return cropTile.getGrowthStage() < 3 || cropTile.getGrowthStage() == 3 && cropTile.getWorldObj().getMoonBrightness() >= 0.15F;
   }

   @Override
   public int getStatInfluence(ICropTile cropTile, int humidity, int nutrients, int air) {
      return (int)(0.4F * (float)humidity + 1.4F * (float)nutrients + 1.2F * (float)air);
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return cropTile.getGrowthStage() == 3 ? 50 : 800;
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.DAENARA;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Blocks.OAK_LEAVES);
   }

   @Override
   public double getDropChance(ICropTile cropTile) {
      return super.getDropChance(cropTile) * 2.0;
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
      return tags != null
         ? new ItemStack[]{new ItemStack((ItemLike)tags.getTag(ItemTags.LEAVES).getRandomElement(cropTile.getWorldObj().getRandom()).orElse(Items.AIR))}
         : new ItemStack[0];
   }
}
