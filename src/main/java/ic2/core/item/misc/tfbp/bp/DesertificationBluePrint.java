package ic2.core.item.misc.tfbp.bp;

import net.minecraft.core.registries.BuiltInRegistries;
import ic2.api.items.ITerraformerBP;
import ic2.api.tiles.ITerraformer;
import ic2.core.platform.registries.IC2Tags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.IPlantable;

public class DesertificationBluePrint implements ITerraformerBP {
   public static final ITerraformerBP INSTANCE = new DesertificationBluePrint();

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
      return 2500;
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
      BlockPos target = terraformer.getFirstBlockFrom(world, position.above(10));
      if (target.getY() == -1) {
         return false;
      } else {
         for (Holder<Block> block : BuiltInRegistries.BLOCK.getTagOrEmpty(IC2Tags.TERRAFORMER_GROUND)) {
            if (terraformer.switchGround(world, target, ((Block)block.value()).defaultBlockState(), Blocks.SAND.defaultBlockState(), false, false)) {
               if (world instanceof ServerLevel) {
                  ((ServerLevel)world).getFluidTicks().clearArea(BoundingBox.fromCorners(position.offset(-2, -2, -2), position.offset(2, 2, 2)));
               }

               return true;
            }
         }

         BlockState state = world.getBlockState(target);
         Block blockx = state.getBlock();
         if (state.getFluidState().getType() == Fluids.WATER) {
            world.setBlock(target, Blocks.AIR.defaultBlockState(), 2);
            if (world instanceof ServerLevel) {
               ((ServerLevel)world).getFluidTicks().clearArea(BoundingBox.fromCorners(position.offset(-2, -2, -2), position.offset(2, 2, 2)));
            }

            return true;
         } else if (state.getFluidState().getType() == Fluids.FLOWING_WATER
            || blockx == Blocks.SNOW_BLOCK
            || state.is(BlockTags.LEAVES)
            || blockx instanceof IPlantable) {
            world.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
            if (world instanceof ServerLevel) {
               ((ServerLevel)world).getFluidTicks().clearArea(BoundingBox.fromCorners(position.offset(-2, -2, -2), position.offset(2, 2, 2)));
            }

            return true;
         } else if (blockx == Blocks.ICE) {
            world.setBlockAndUpdate(target, Fluids.WATER.defaultFluidState().createLegacyBlock());
            if (world instanceof ServerLevel) {
               ((ServerLevel)world).getFluidTicks().clearArea(BoundingBox.fromCorners(position.offset(-2, -2, -2), position.offset(2, 2, 2)));
            }

            return true;
         } else if (!state.is(BlockTags.PLANKS) && !state.is(BlockTags.LOGS_THAT_BURN)) {
            return false;
         } else {
            world.setBlockAndUpdate(target, Blocks.FIRE.defaultBlockState());
            if (world instanceof ServerLevel) {
               ((ServerLevel)world).getFluidTicks().clearArea(BoundingBox.fromCorners(position.offset(-2, -2, -2), position.offset(2, 2, 2)));
            }

            return true;
         }
      }
   }
}
