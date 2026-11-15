package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GlowshroomCrop extends BaseCrop {
   public GlowshroomCrop() {
      super("glowshroom", new CropProperties(5, 2, 4, 0, 3, 0), "Yellow", "Nether", "Light", "Soulsand");
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(int stage) {
      return IC2Textures.getMappedEntriesBlockIC2("crops/shroom/glowshroom").get("growing_" + stage);
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return cropTile.getGrowthStage() < 1 || cropTile.getGrowthStage() < 3 && this.hasGlowStone(cropTile);
   }

   @Override
   public Component discoveredBy() {
      return SPEIGER;
   }

   @Override
   public int getGrowthSteps() {
      return 3;
   }

   @Override
   public int getAfterHarvestStage(ICropTile cropTile) {
      return 2;
   }

   @Override
   public double getDropChance(ICropTile cropTile) {
      return 0.8;
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Items.GLOWSTONE_DUST);
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(Items.GLOWSTONE_DUST)};
   }

   private boolean hasGlowStone(ICropTile crop) {
      Level world = crop.getWorldObj();

      for (Direction facing : DirectionList.DOWN.invert()) {
         BlockPos pos = crop.getPosition().relative(facing);
         if (world.isLoaded(pos) && world.getBlockState(pos).getBlock() == Blocks.GLOWSTONE) {
            return true;
         }
      }

      return false;
   }
}
