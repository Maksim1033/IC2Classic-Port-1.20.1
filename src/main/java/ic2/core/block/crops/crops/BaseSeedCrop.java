package ic2.core.block.crops.crops;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropModifier;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ISeedCrop;
import ic2.core.IC2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class BaseSeedCrop extends BaseCrop implements ISeedCrop {
   public BaseSeedCrop(ResourceLocation id, CropProperties props, String... attributes) {
      super(id, props, attributes);
   }

   public BaseSeedCrop(String name, CropProperties props, String... attributes) {
      super(name, props, attributes);
   }

   @Override
   public boolean onRightClick(ICropTile cropTile, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (ICropModifier.canToggleSeedMode(stack)) {
         CompoundTag data = cropTile.getCustomData();
         boolean newSeed = !data.getBoolean("seed");
         data.putBoolean("seed", newSeed);
         if (IC2.PLATFORM.isSimulating()) {
            player.displayClientMessage(this.translate(newSeed ? "info.crop.ic2.seed_mode.enable" : "info.crop.ic2.seed_mode.disable"), false);
         }

         return true;
      } else {
         return super.onRightClick(cropTile, player, hand);
      }
   }

   @Override
   public boolean isDroppingSeeds(ICropTile tile) {
      return tile.getCustomData().getBoolean("seed");
   }
}
