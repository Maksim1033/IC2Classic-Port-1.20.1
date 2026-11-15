package ic2.core.item.misc.tfbp.bp;

import ic2.api.items.ITerraformerBP;
import ic2.api.tiles.ITerraformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MushroomBluePrint implements ITerraformerBP {
   public static final ITerraformerBP INSTANCE = new MushroomBluePrint();

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
      return 8000;
   }

   @Override
   public int getRadius(ItemStack stack) {
      return 25;
   }

   @Override
   public void onInsert(ItemStack stack, Player player, Level world, BlockPos pos) {
   }

   @Override
   public boolean terraform(ItemStack stack, Level world, BlockPos position, ITerraformer terraformer) {
      BlockPos target = terraformer.getFirstSolidBlockFrom(world, position.above(20));
      return target.getY() != -1 && this.growBlockWithDependency((ServerLevel)world, target, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM, terraformer);
   }

   public boolean growBlockWithDependency(ServerLevel world, BlockPos pos, Block id, Block dependency, ITerraformer terraformer) {
      if (dependency != Blocks.AIR) {
         MutableBlockPos mutable = new MutableBlockPos();

         for (int x = -1; x <= 1; x++) {
            int z = -1;

            while (z <= 1) {
               int y = 5;

               while (true) {
                  label148: {
                     label124:
                     if (y > -2) {
                        mutable.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                        Block block = world.getBlockState(mutable).getBlock();
                        if (dependency == Blocks.MYCELIUM) {
                           if (block == dependency || block == Blocks.BROWN_MUSHROOM_BLOCK || block == Blocks.RED_MUSHROOM_BLOCK) {
                              break label124;
                           }

                           if (block == Blocks.AIR) {
                              break label148;
                           }

                           if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK) {
                              BlockPos targetPos = mutable.immutable();
                              world.setBlockAndUpdate(targetPos, dependency.defaultBlockState());
                              terraformer.setBiome(world, targetPos, Biomes.MUSHROOM_FIELDS.location());
                              return true;
                           }
                        }

                        if (dependency != Blocks.BROWN_MUSHROOM) {
                           break label148;
                        }

                        if (block != Blocks.BROWN_MUSHROOM && block != Blocks.RED_MUSHROOM) {
                           if (block != Blocks.AIR
                              && this.growBlockWithDependency(world, mutable.immutable(), Blocks.BROWN_MUSHROOM, Blocks.MYCELIUM, terraformer)) {
                              return true;
                           }
                           break label148;
                        }
                     }

                     z++;
                     break;
                  }

                  y--;
               }
            }
         }
      }

      if (id == Blocks.BROWN_MUSHROOM) {
         Block base = world.getBlockState(pos).getBlock();
         if (base != Blocks.MYCELIUM) {
            if (base != Blocks.BROWN_MUSHROOM_BLOCK && base != Blocks.RED_MUSHROOM_BLOCK) {
               return false;
            }

            world.setBlockAndUpdate(pos, Blocks.MYCELIUM.defaultBlockState());
         }

         Block above = world.getBlockState(pos.above()).getBlock();
         if (above != Blocks.AIR && above != Blocks.TALL_GRASS) {
            return false;
         } else {
            Block shroom = Blocks.BROWN_MUSHROOM;
            if (world.random.nextBoolean()) {
               shroom = Blocks.RED_MUSHROOM;
            }

            world.setBlockAndUpdate(pos.above(), shroom.defaultBlockState());
            return true;
         }
      } else {
         if (id == Blocks.BROWN_MUSHROOM_BLOCK) {
            BlockPos up = pos.above();
            BlockState state = world.getBlockState(up);
            if (state.getBlock() != Blocks.BROWN_MUSHROOM && state.getBlock() != Blocks.RED_MUSHROOM) {
               return false;
            }

            if (((MushroomBlock)state.getBlock()).growMushroom(world, up, state, world.random)) {
               MutableBlockPos mutable = new MutableBlockPos();

               for (int x = -1; x <= 1; x++) {
                  for (int z = -1; z <= 1; z++) {
                     mutable.set(up.getX() + x, up.getY(), up.getZ() + z);
                     Block blockx = world.getBlockState(mutable).getBlock();
                     if (blockx == Blocks.BROWN_MUSHROOM || blockx == Blocks.RED_MUSHROOM) {
                        world.removeBlock(mutable.immutable(), false);
                     }
                  }
               }

               return true;
            }
         }

         return false;
      }
   }
}
