package ic2.core.item.tool.electric;

import ic2.api.items.electric.ElectricItem;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.block.resource.RubberwoodLogBlock;
import ic2.core.item.base.features.IMultiTargetTool;
import ic2.core.item.tool.TreeTapTool;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.utils.collection.IterableWrapper;
import ic2.core.utils.helpers.AABBUtil;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AdvancedTreeTap extends ElectricTreeTap implements IMultiTargetTool {
   public AdvancedTreeTap() {
      super("advanced_tree_tap");
   }

   @Override
   public boolean canMultiMine(ItemStack stack) {
      return ElectricItem.MANAGER.canUse(stack, this.energyCost * Math.max(1, StackUtil.getNbtData(stack).getLongArray("positions").length));
   }

   @Override
   public boolean isMultiMining(ItemStack stack) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("tools/treetap").get("advanced");
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Player player = context.getPlayer();

      assert player != null;

      ItemStack stack = player.getItemInHand(context.getHand());
      BlockState state = player.level().getBlockState(pos);
      Direction side = context.getClickedFace();
      if (!(state.getBlock() instanceof RubberwoodLogBlock)
         || !(Boolean)state.getValue(RubberwoodLogBlock.RESIN)
         || state.getValue(RubberwoodLogBlock.RESIN_FACING) != side) {
         return InteractionResult.PASS;
      } else if (!this.canMultiMine(stack)) {
         return InteractionResult.PASS;
      } else {
         boolean server = IC2.PLATFORM.isSimulating();
         if (server) {
            int count = 0;
            List<ItemStack> list = new ObjectArrayList();

            for (BlockPos subPos : IterableWrapper.wrap(this.getHitPositions(stack, player, pos, side))) {
               TreeTapTool.extractRubber(world.getBlockState(subPos), world, subPos, side, stack, list);
               list.add(new ItemStack(IC2Items.STICKY_RESIN, TreeTapTool.getResinAmount(stack, 5, 10)));
               count++;
               world.removeBlock(subPos, false);
            }

            IC2.AUDIO.playSound(context.getPlayer(), IC2Sounds.TREE_TAP, AudioManager.SoundType.ITEM);
            ElectricItem.MANAGER.use(stack, this.energyCost * count, player);

            for (ItemStack item : list) {
               StackUtil.addOrDrop(player, item);
            }
         }

         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public Iterator<BlockPos> getHitPositions(final ItemStack stack, Player player, BlockPos pos, Direction dir) {
      BlockState state = player.level().getBlockState(pos);
      if (state.getBlock() instanceof RubberwoodLogBlock
         && (Boolean)state.getValue(RubberwoodLogBlock.RESIN)
         && state.getValue(RubberwoodLogBlock.RESIN_FACING) == dir) {
         CompoundTag data = stack.getOrCreateTag();
         if (data.getLong("lastPos") != pos.asLong()) {
            int limit = ElectricItem.MANAGER.getCharge(stack) / this.energyCost;
            List<BlockPos> result = AABBUtil.getValidBlocks(player.level(), pos, 20, AdvancedTreeTap.LogFinder.INSTANCE, 0, DirectionList.ALL, 20).getPositions();
            long[] positions = new long[Math.min(limit, result.size())];
            int i = 0;

            for (int m = result.size(); i < m; i++) {
               positions[i] = result.get(i).asLong();
            }

            data.putLong("lastPos", pos.asLong());
            data.putLongArray("positions", positions);
         }

         return new Iterator<BlockPos>() {
            long[] positions = StackUtil.getNbtData(stack).getLongArray("positions");
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
      } else {
         if (stack.hasTag()) {
            CompoundTag data = StackUtil.getNbtData(stack);
            data.remove("positions");
            data.remove("lastPos");
            if (data.isEmpty()) {
               stack.setTag(null);
            }
         }

         return Collections.emptyIterator();
      }
   }

   public static class LogFinder implements AABBUtil.IBlockFilter {
      public static final AABBUtil.IBlockFilter INSTANCE = new AdvancedTreeTap.LogFinder();

      @Override
      public boolean isValid(BlockState state) {
         return state.getBlock() instanceof RubberwoodLogBlock;
      }
   }
}
