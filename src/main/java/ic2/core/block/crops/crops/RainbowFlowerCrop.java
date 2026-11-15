package ic2.core.block.crops.crops;

import ic2.api.crops.ICropTile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class RainbowFlowerCrop extends DyeFlowerCrop {
   public RainbowFlowerCrop() {
      super("rainbow_flower", null, BaseCrop.DAENARA, "Rainbow", "Tulip");
   }

   @Override
   public ItemStack getDisplayItem() {
      return new ItemStack(Blocks.CAULDRON);
   }

   @Override
   public ItemStack[] getDrops(ICropTile cropTile) {
      return new ItemStack[]{new ItemStack(DyeItem.byColor(DyeColor.byId(BaseCrop.getRandom(cropTile).nextInt(16))))};
   }
}
