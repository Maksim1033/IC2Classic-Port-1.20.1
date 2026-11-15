package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WeedCrop extends BaseCrop {
   public WeedCrop() {
      super("weed", new CropProperties(0, 0, 0, 1, 0, 5), "Weed", "Green");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/weed").get("growing_" + stage);
   }

   @Override
   public int getGrowthSteps() {
      return 3;
   }

   @Override
   public int getOptimalHarvestStep(ICropTile cropTile) {
      return 0;
   }

   @Override
   public int getGrowthDuration(ICropTile cropTile) {
      return 300;
   }

   @Override
   public boolean canBeHarvested(ICropTile cropTile) {
      return false;
   }

   @Override
   public boolean onLeftClick(ICropTile cropTile, Player player) {
      cropTile.removeCrop();
      return true;
   }

   @Override
   public void onEntityCollision(ICropTile cropTile, Entity entity) {
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return EMPTY_DROPS;
   }

   @Override
   public ItemStack getSeeds(ICropTile cropTile) {
      return ItemStack.EMPTY;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(IC2Items.WEEDEX);
   }
}
