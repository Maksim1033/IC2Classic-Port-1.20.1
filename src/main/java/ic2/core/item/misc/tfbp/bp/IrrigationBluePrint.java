package ic2.core.item.misc.tfbp.bp;

import ic2.api.items.ITerraformerBP;
import ic2.api.tiles.ITerraformer;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.utils.collection.CollectionUtils;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;

public class IrrigationBluePrint implements ITerraformerBP {
   public static final IrrigationBluePrint INSTANCE = new IrrigationBluePrint();
   Map<Block, Block> logToLeaves = CollectionUtils.<Block, Block>createMap();

   public void init() {
      this.logToLeaves.clear();
      this.logToLeaves.put(Blocks.OAK_LOG, Blocks.OAK_LEAVES);
      this.logToLeaves.put(Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES);
      this.logToLeaves.put(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES);
      this.logToLeaves.put(Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES);
      this.logToLeaves.put(Blocks.ACACIA_LOG, Blocks.ACACIA_LEAVES);
      this.logToLeaves.put(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES);
      this.logToLeaves.put(Blocks.MANGROVE_LOG, Blocks.MANGROVE_LEAVES);
      this.logToLeaves.put(Blocks.CHERRY_LOG, Blocks.CHERRY_LEAVES);
      if (IC2Blocks.RUBBERWOOD_LOG != null && IC2Blocks.RUBBER_LEAVES != null) {
         this.logToLeaves.put(IC2Blocks.RUBBERWOOD_LOG, IC2Blocks.RUBBER_LEAVES);
      }
   }

   @Override
   public void onInsert(ItemStack stack, Player player, Level world, BlockPos pos) {
   }

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
      return 3000;
   }

   @Override
   public int getRadius(ItemStack stack) {
      return 60;
   }

   @Override
   public boolean terraform(ItemStack stack, Level world, BlockPos position, ITerraformer terraformer) {
      if (world.random.nextInt(48000) == 0) {
         world.getLevelData().setRaining(true);
         return true;
      } else {
         BlockPos target = terraformer.getFirstBlockFrom(world, position.above(10));
         if (target.getY() == -1) {
            return false;
         } else if (terraformer.switchGround(world, target, Blocks.SAND.defaultBlockState(), Blocks.DIRT.defaultBlockState(), true, false)) {
            terraformer.switchGround(world, target, Blocks.SAND.defaultBlockState(), Blocks.DIRT.defaultBlockState(), true, false);
            return true;
         } else {
            BlockState state = world.getBlockState(target);
            if (state.getBlock() != Blocks.TALL_GRASS) {
               if (state.getBlock() instanceof BonemealableBlock && !(state.getBlock() instanceof CropBlock)) {
                  ((BonemealableBlock)state.getBlock()).performBonemeal((ServerLevel)world, world.random, target, state);
                  return true;
               } else if (state.is(BlockTags.LOGS)) {
                  BlockPos logPos = target.above();
                  world.setBlockAndUpdate(logPos, state);
                  this.spawnLeaves(world, target.above(), state);
                  this.spawnLeaves(world, target.east(), state);
                  this.spawnLeaves(world, target.west(), state);
                  this.spawnLeaves(world, target.north(), state);
                  this.spawnLeaves(world, target.west(), state);
                  return true;
               } else if (state.getBlock() instanceof CropBlock crop) {
                  if (crop.isMaxAge(state)) {
                     return false;
                  } else {
                     world.setBlockAndUpdate(target, crop.getStateForAge(crop.getAge(state) + 1));
                     return true;
                  }
               } else if (state.getBlock() == Blocks.FIRE) {
                  world.removeBlock(target, false);
                  return true;
               } else {
                  return false;
               }
            } else {
               return this.spreadGrass(world, position.east(), terraformer)
                  || this.spreadGrass(world, position.west(), terraformer)
                  || this.spreadGrass(world, position.north(), terraformer)
                  || this.spreadGrass(world, position.south(), terraformer);
            }
         }
      }
   }

   public void spawnLeaves(Level world, BlockPos pos, BlockState state) {
      Block leave = this.logToLeaves.get(state.getBlock());
      if (leave != null) {
         world.setBlockAndUpdate(pos, leave.defaultBlockState());
      }
   }

   public boolean spreadGrass(Level world, BlockPos pos, ITerraformer terraformer) {
      if (world.random.nextBoolean()) {
         return false;
      } else {
         BlockPos target = terraformer.getFirstBlockFrom(world, pos);
         BlockState state = world.getBlockState(target);
         if (state.getBlock() == Blocks.DIRT) {
            world.setBlockAndUpdate(target, Blocks.GRASS_BLOCK.defaultBlockState());
            return true;
         } else if (state.getBlock() == Blocks.GRASS) {
            world.setBlockAndUpdate(target, Blocks.TALL_GRASS.defaultBlockState());
            return true;
         } else {
            return false;
         }
      }
   }
}
