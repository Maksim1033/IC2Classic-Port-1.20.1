package ic2.core.item.tool.electric;

import ic2.api.items.electric.ElectricItem;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.item.base.features.IMultiTargetTool;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.utils.collection.IterableWrapper;
import ic2.core.utils.helpers.AABBUtil;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AdvancedChainsaw extends ChainsawTool implements IMultiTargetTool {
   public AdvancedChainsaw() {
      super("advanced_chainsaw", 2, 100, 16.0F);
      this.tier = 3;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/chainsaw").get("advanced");
   }

   @Override
   public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
      if (this.isMultiMining(stack) && this.canMultiMine(stack)) {
         Level world = player.level();
         BlockHitResult ray = getPlayerPOVHitResult(world, player, Fluid.NONE);
         int removed = 0;

         for (BlockPos offset : IterableWrapper.wrap(this.getHitPositions(stack, player, pos, ray.getDirection()))) {
            BlockState state = world.getBlockState(offset);
            if (!state.isAir() && state.canHarvestBlock(world, offset, player)) {
               BlockEntity tile = world.getBlockEntity(offset);
               if (state.onDestroyedByPlayer(world, offset, player, true, state.getFluidState())) {
                  Block block = state.getBlock();
                  block.destroy(world, offset, state);
                  block.playerDestroy(world, player, offset, state, tile, stack);
                  removed++;
               }
            }
         }

         if (removed > 0) {
            IC2.AUDIO.playSound(player, player.level().random.nextBoolean() ? USE_1 : USE_2, AudioManager.SoundType.ITEM, 1.0F, 1.0F);
            player.awardStat(IC2Stats.BLOCKS_SAWED);
            ElectricItem.MANAGER.use(stack, this.getEnergyCost(stack) * removed, player);
            return true;
         }
      }

      return super.onBlockStartBreak(stack, pos, player);
   }

   @Override
   public boolean isMultiMining(ItemStack stack) {
      return true;
   }

   @Override
   public boolean canMultiMine(ItemStack stack) {
      return true;
   }

   @Override
   public Iterator<BlockPos> getHitPositions(ItemStack stack, Player player, BlockPos pos, Direction dir) {
      final CompoundTag nbt = stack.getOrCreateTag();
      if (pos.asLong() != nbt.getLong("lastPos")) {
         long[] toCheck = nbt.getLongArray("positions");
         boolean found = false;
         if (toCheck.length > 0) {
            long toFind = pos.asLong();

            for (long l : toCheck) {
               if (toFind == l) {
                  found = true;
                  break;
               }
            }
         }

         if (!found) {
            boolean remove = true;
            int limit = ElectricItem.MANAGER.getCharge(stack) / this.getEnergyCost(stack);
            if (limit > 0) {
               BlockState state = player.level().getBlockState(pos);
               Block block = state.getBlock();
               if (state.is(BlockTags.LOGS)) {
                  if (player.isSecondaryUseActive()) {
                     long[] result = new long[]{pos.asLong()};
                     nbt.putLongArray("positions", result);
                     remove = false;
                  } else {
                     Map<Block, List<BlockPos>> positions = AABBUtil.getValidBlocks(
                           player.level(), pos, 20, new AdvancedChainsaw.TreeChopper(block), 32, DirectionList.ALL, 200
                        )
                        .getMappedPositions();
                     List<BlockPos> logs = positions.remove(block);
                     if (!positions.isEmpty()) {
                        long[] result = new long[Math.min(limit, logs.size())];

                        for (int i = 0; i < result.length; i++) {
                           result[i] = logs.get(i).asLong();
                        }

                        nbt.putLongArray("positions", result);
                        remove = false;
                     }
                  }
               }
            }

            if (remove) {
               nbt.remove("positions");
            }
         }

         nbt.putLong("lastPos", pos.asLong());
      }

      return new Iterator<BlockPos>() {
         long[] positions = nbt.getLongArray("positions");
         int index = 0;
         MutableBlockPos position = new MutableBlockPos();

         @Override
         public boolean hasNext() {
            return this.index < this.positions.length;
         }

         public BlockPos next() {
            return this.position.set(this.positions[this.index++]);
         }
      };
   }

   public static class TreeChopper implements AABBUtil.IBlockFilter {
      Block baseBlock;

      public TreeChopper(Block baseBlock) {
         this.baseBlock = baseBlock;
      }

      @Override
      public boolean isValid(BlockState state) {
         return state.getBlock() == this.baseBlock || state.getBlock() instanceof LeavesBlock && (Integer)state.getValue(LeavesBlock.DISTANCE) == 1;
      }
   }
}
