package ic2.core.item.misc.tfbp.bp;

import net.minecraft.core.registries.BuiltInRegistries;
import ic2.api.items.ITerraformerBP;
import ic2.api.tiles.ITerraformer;
import ic2.core.platform.registries.IC2Advancements;
import ic2.core.platform.registries.IC2Tags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CultivatedBluePrint implements ITerraformerBP {
   public static final ITerraformerBP INSTANCE = new CultivatedBluePrint();

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
      if (world.dimension() == Level.END) {
         IC2Advancements.ID_TRIGGER.onTrigger(player, new ResourceLocation("ic2:cultivate_end"));
      }
   }

   @Override
   public boolean terraform(ItemStack stack, Level world, BlockPos position, ITerraformer terraformer) {
      BlockPos target = terraformer.getFirstSolidBlockFrom(world, position.above(10));
      if (target.getY() == -1) {
         return false;
      } else if (terraformer.switchGround(world, target, Blocks.SAND.defaultBlockState(), Blocks.DIRT.defaultBlockState(), true, false)) {
         return true;
      } else {
         BlockState state = world.getBlockState(target);
         if (state.getBlock() == Blocks.DIRT) {
            world.setBlockAndUpdate(target, Blocks.GRASS_BLOCK.defaultBlockState());
            return true;
         } else {
            return state.getBlock() == Blocks.GRASS_BLOCK && this.growPlantsOn(world, target.above());
         }
      }
   }

   public boolean growPlantsOn(Level world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.TALL_GRASS && world.random.nextInt(4) == 0) {
         BlockState plant = BuiltInRegistries.BLOCK.getOrCreateTag(IC2Tags.TERRAFORMER_FLOWERS).getRandomElement(world.getRandom()).orElseThrow()
            .value()
            .defaultBlockState();
         if (plant.getBlock() instanceof CropBlock) {
            world.setBlockAndUpdate(pos.below(), (BlockState)Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
            world.setBlockAndUpdate(pos, plant);
            return true;
         } else if (plant.getBlock() instanceof DoublePlantBlock) {
            DoublePlantBlock.placeAt(world, plant, pos, 3);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
