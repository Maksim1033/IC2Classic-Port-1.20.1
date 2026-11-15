package ic2.core.item.misc.tfbp.bp;

import ic2.api.items.ITerraformerBP;
import ic2.api.tiles.ITerraformer;
import ic2.core.platform.registries.IC2Tags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FlatificationBluePrint implements ITerraformerBP {
   public static final ITerraformerBP INSTANCE = new FlatificationBluePrint();

   @Override
   public boolean canInsert(ItemStack stack, Player player, Level world, BlockPos pos) {
      return true;
   }

   @Override
   public boolean isRandomized(ItemStack stack) {
      return true;
   }

   @Override
   public int getEnergyUsage(ItemStack stack) {
      return 4000;
   }

   @Override
   public int getRadius(ItemStack stack) {
      return 40;
   }

   @Override
   public void onInsert(ItemStack stack, Player player, Level world, BlockPos pos) {
   }

   @Override
   public boolean terraform(ItemStack stack, Level world, BlockPos position, ITerraformer terraformer) {
      BlockPos target = terraformer.getFirstBlockFrom(world, position.above(20));
      if (target.getY() == -1) {
         return false;
      } else {
         BlockState state = world.getBlockState(target);
         if (state.getBlock() == Blocks.SNOW) {
            target = target.below();
            state = world.getBlockState(target);
         }

         if (target.getY() == position.getY()) {
            return false;
         } else if (target.getY() < position.getY()) {
            world.setBlockAndUpdate(target.above(), Blocks.DIRT.defaultBlockState());
            return true;
         } else if (state.is(IC2Tags.TERRAFORMER_FLAT)) {
            world.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
            return true;
         } else {
            return false;
         }
      }
   }
}
