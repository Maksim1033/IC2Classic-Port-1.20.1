package ic2.core.item.misc.tfbp.bp;

import ic2.api.items.ITerraformerBP;
import ic2.api.tiles.ITerraformer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class ChillingBluePrint implements ITerraformerBP {
   public static final ITerraformerBP INSTANCE = new ChillingBluePrint();

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
      return 2000;
   }

   @Override
   public int getRadius(ItemStack stack) {
      return 50;
   }

   @Override
   public boolean terraform(ItemStack stack, Level world, BlockPos position, ITerraformer terraformer) {
      BlockPos target = terraformer.getFirstBlockFrom(world, position.above(10));
      if (target.getY() < -1) {
         return false;
      } else {
         BlockState state = world.getBlockState(target);
         if (state.getFluidState().getType() != Fluids.WATER && state.getFluidState().getType() != Fluids.FLOWING_WATER) {
            if (state.getBlock() == Blocks.ICE) {
               BlockState below = world.getBlockState(target.below());
               if (below.getFluidState().getType() == Fluids.WATER || below.getFluidState().getType() == Fluids.FLOWING_WATER) {
                  world.setBlockAndUpdate(target.below(), Blocks.ICE.defaultBlockState());
                  return true;
               }
            }

            if (state.getBlock() == Blocks.SNOW && this.isSurroundedBySnow(world, target, terraformer)) {
               world.setBlockAndUpdate(target, Blocks.SNOW_BLOCK.defaultBlockState());
               return true;
            } else if (!Blocks.SNOW.canSurvive(world.getBlockState(target.above()), world, target.above()) && state.getBlock() != Blocks.ICE) {
               return false;
            } else {
               world.setBlockAndUpdate(target.above(), Blocks.SNOW.defaultBlockState());
               return true;
            }
         } else {
            world.setBlockAndUpdate(target, Blocks.ICE.defaultBlockState());
            return true;
         }
      }
   }

   @Override
   public void onInsert(ItemStack stack, Player player, Level world, BlockPos pos) {
   }

   public boolean isSurroundedBySnow(Level world, BlockPos pos, ITerraformer terraformer) {
      return this.isSnowHere(world, pos.east(), terraformer)
         && this.isSnowHere(world, pos.west(), terraformer)
         && this.isSnowHere(world, pos.north(), terraformer)
         && this.isSnowHere(world, pos.south(), terraformer);
   }

   public boolean isSnowHere(Level world, BlockPos pos, ITerraformer terraformer) {
      BlockPos target = terraformer.getFirstBlockFrom(world, pos.above(16));
      if (pos.getY() > target.getY()) {
         return false;
      } else {
         BlockState state = world.getBlockState(target);
         if (state.getBlock() != Blocks.SNOW && state.getBlock() != Blocks.SNOW_BLOCK) {
            if (!Blocks.SNOW.canSurvive(world.getBlockState(target.above()), world, target.above()) && state.getBlock() != Blocks.ICE) {
               return false;
            } else {
               world.setBlockAndUpdate(target.above(), Blocks.SNOW.defaultBlockState());
               return true;
            }
         } else {
            return true;
         }
      }
   }
}
