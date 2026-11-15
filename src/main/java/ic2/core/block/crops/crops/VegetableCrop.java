package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.utils.helpers.SanityHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VegetableCrop extends BaseCrop {
   private String name;
   private ItemLike drop;

   public VegetableCrop(String name, String color, ItemLike drop) {
      super(name, new CropProperties(2, 0, 4, 0, 0, 2), "Food", SanityHelper.firstLetterUppercase(name), color);
      this.name = name;
      this.drop = drop;
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
            stageTexture = "growing_" + this.name + "_" + stage;
      }

      return IC2Textures.getMappedEntriesBlockIC2("crops/vegetable").get(stageTexture);
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(this.drop);
   }

   @Override
   public Component discoveredBy() {
      return BaseCrop.NOTCH;
   }

   @Override
   public int getGrowthSteps() {
      return 4;
   }

   @Override
   public boolean canGrow(ICropTile cropTile) {
      return super.canGrow(cropTile) && this.secondaryGrowthCondition(cropTile);
   }

   private boolean secondaryGrowthCondition(ICropTile cropTile) {
      return cropTile.getLightLevel() >= 9;
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return this.name.equals("potato") && BaseCrop.getRandom(cropTile).nextInt(50) == 0
         ? new ItemStack[]{new ItemStack(Items.POISONOUS_POTATO)}
         : new ItemStack[]{new ItemStack(this.drop)};
   }

   @Override
   public boolean hasCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return true;
   }

   @Override
   public Component getCustomCropPlaceFailedMessage(ICropTile cropTile) {
      return (Component)(!this.secondaryGrowthCondition(cropTile)
         ? this.translate("info.crop.ic2.plant.placement_error.light", new Object[]{this.translate("info.crop.ic2.plant.placement_error.more")})
         : super.getCustomCropPlaceFailedMessage(cropTile));
   }
}
