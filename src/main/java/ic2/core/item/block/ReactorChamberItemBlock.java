package ic2.core.item.block;

import ic2.core.item.base.IC2BlockItem;
import ic2.core.utils.helpers.StackUtil;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class ReactorChamberItemBlock extends IC2BlockItem {
   Supplier<Block> reactor;

   public ReactorChamberItemBlock(Block block, Supplier<Block> reactor) {
      super(block);
      this.reactor = reactor;
   }

   public InteractionResult place(BlockPlaceContext context) {
      if (!context.canPlace()) {
         return InteractionResult.FAIL;
      } else {
         BlockPlaceContext placeContext = this.updatePlacementContext(context);
         if (placeContext == null) {
            return InteractionResult.FAIL;
         } else {
            BlockState state = this.getPlacementState(placeContext);
            if (state == null) {
               if (!this.placeReactor(placeContext.getLevel(), placeContext.getClickedPos().above(), placeContext.getPlayer(), placeContext)) {
                  return InteractionResult.FAIL;
               }

               state = this.getPlacementState(placeContext);
               if (state == null) {
                  return InteractionResult.FAIL;
               }
            }

            if (!this.placeBlock(placeContext, state)) {
               return InteractionResult.FAIL;
            } else {
               BlockPos pos = placeContext.getClickedPos();
               Level level = placeContext.getLevel();
               Player player = placeContext.getPlayer();
               ItemStack stack = placeContext.getItemInHand();
               BlockState existing = level.getBlockState(pos);
               if (existing.is(state.getBlock())) {
                  existing = this.updateBlockStateFromTag(pos, level, stack, existing);
                  this.updateCustomBlockEntityTag(pos, level, player, stack, existing);
                  existing.getBlock().setPlacedBy(level, pos, existing, player, stack);
                  if (player instanceof ServerPlayer) {
                     CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, pos, stack);
                  }
               }

               level.gameEvent(GameEvent.BLOCK_PLACE, pos, Context.of(player, existing));
               SoundType soundtype = existing.getSoundType(level, pos, context.getPlayer());
               level.playSound(
                  player,
                  pos,
                  this.getPlaceSound(existing, level, pos, context.getPlayer()),
                  SoundSource.BLOCKS,
                  (soundtype.getVolume() + 1.0F) / 2.0F,
                  soundtype.getPitch() * 0.8F
               );
               if (player == null || !player.getAbilities().instabuild) {
                  stack.shrink(1);
               }

               return InteractionResult.sidedSuccess(level.isClientSide);
            }
         }
      }
   }

   protected boolean placeReactor(Level level, BlockPos tryPlacePosition, Player player, BlockPlaceContext context) {
      if (player != null && this.reactor != null) {
         Block reactor = this.reactor.get();
         if (reactor == null) {
            return false;
         } else {
            int reactorSlot = this.findReactor(player, reactor);
            if (reactorSlot == -1) {
               return false;
            } else if (!this.canPlace(
               player,
               level,
               tryPlacePosition,
               reactor.defaultBlockState(),
               this.createContext(context, StackUtil.copyWithSize((ItemStack)player.getInventory().items.get(reactorSlot), 1), tryPlacePosition)
            )) {
               return false;
            } else if (!level.setBlock(tryPlacePosition, reactor.defaultBlockState(), 11)) {
               return false;
            } else {
               if (!player.getAbilities().instabuild) {
                  player.getInventory().removeItem(reactorSlot, 1);
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   protected int findReactor(Player player, Block reactor) {
      Item item = reactor.asItem();
      Inventory inv = player.getInventory();

      for (int i = 0; i < inv.items.size(); i++) {
         if (((ItemStack)inv.items.get(i)).is(item)) {
            return i;
         }
      }

      return -1;
   }

   private BlockPlaceContext createContext(BlockPlaceContext existing, ItemStack stack, BlockPos newPos) {
      Direction dir = existing.getClickedFace();
      BlockHitResult hit = new BlockHitResult(
         new Vec3(
            (double)newPos.getX() + 0.5 + (double)dir.getStepX() * 0.5,
            (double)newPos.getY() + 0.5 + (double)dir.getStepY() * 0.5,
            (double)newPos.getZ() + 0.5 + (double)dir.getStepZ() * 0.5
         ),
         dir,
         newPos,
         existing.isInside()
      );
      return BlockPlaceContext.at(new BlockPlaceContext(existing.getPlayer(), existing.getHand(), stack, hit), newPos, existing.getClickedFace());
   }

   protected boolean canPlace(Player player, Level level, BlockPos pos, BlockState state, BlockPlaceContext context) {
      return (!this.mustSurvive() || state.canSurvive(level, pos))
         && level.getBlockState(pos).canBeReplaced(context)
         && level.isUnobstructed(state, pos, player == null ? CollisionContext.empty() : CollisionContext.of(player));
   }

   private BlockState updateBlockStateFromTag(BlockPos pos, Level level, ItemStack stack, BlockState state) {
      BlockState blockstate = state;
      CompoundTag compoundtag = stack.getTag();
      if (compoundtag != null) {
         CompoundTag compoundTag1 = compoundtag.getCompound("BlockStateTag");
         StateDefinition<Block, BlockState> statedefinition = state.getBlock().getStateDefinition();

         for (String s : compoundTag1.getAllKeys()) {
            Property<?> property = statedefinition.getProperty(s);
            if (property != null) {
               String s1 = compoundTag1.get(s).getAsString();
               blockstate = updateState(blockstate, property, s1);
            }
         }
      }

      if (blockstate != state) {
         level.setBlock(pos, blockstate, 2);
      }

      return blockstate;
   }

   private static <T extends Comparable<T>> BlockState updateState(BlockState state, Property<T> property, String value) {
      return property.getValue(value).map(actualValue -> (BlockState)state.setValue(property, actualValue)).orElse(state);
   }
}
