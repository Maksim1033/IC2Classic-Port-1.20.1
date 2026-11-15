package ic2.api.crops;

import net.minecraft.world.item.ItemStack;

public interface ISeedCrop {
   boolean isDroppingSeeds(ICropTile var1);

   ItemStack[] getSeedDrops(ICropTile var1);
}
