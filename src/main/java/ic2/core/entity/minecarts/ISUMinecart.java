package ic2.core.entity.minecarts;

import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Items;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ISUMinecart extends ElectricMinecart {
   public ISUMinecart(EntityType<?> type, Level level) {
      super(type, level);
   }

   public ISUMinecart(EntityType<?> type, Level level, double x, double y, double z) {
      super(type, level, x, y, z);
   }

   @Override
   public int getMaxEU() {
      return 500000000;
   }

   @Override
   public int getTier() {
      return 5;
   }

   @Override
   public int getGuiOffset() {
      return -18;
   }

   @Override
   public BlockState getDefaultDisplayBlockState() {
      return IC2Blocks.ISU.defaultBlockState();
   }

   @Override
   protected Item getDropItem() {
      return IC2Items.ISU_MINECART;
   }
}
