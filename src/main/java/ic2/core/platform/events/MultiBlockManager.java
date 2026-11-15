package ic2.core.platform.events;

import ic2.core.block.base.features.multiblock.IMultiBlockClickable;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.LevelEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class MultiBlockManager {
   public static final MultiBlockManager INSTANCE = new MultiBlockManager();
   Map<LevelAccessor, MultiBlockManager.MultiBlockListeners> listeners = CollectionUtils.<LevelAccessor, MultiBlockManager.MultiBlockListeners>createMap();

   public IMultiBlockClickable getMultiBlock(Level world, BlockPos pos) {
      return this.listeners.get(world).getPos(pos);
   }

   @SubscribeEvent
   public void onBlockClick(RightClickBlock event) {
      if (event.getEntity() != null && !event.getEntity().isShiftKeyDown()) {
         Level world = event.getLevel();
         BlockPos pos = event.getPos();
         IMultiBlockClickable listener = this.getListeners(world).getPos(pos);
         if (listener != null && listener.getRequiredActions().canDoRightClick()) {
            BlockState state = world.getBlockState(pos);
            if (listener.shouldLinkToBlock(pos, state)) {
               if (world.getBlockState(listener.getOrigin())
                     .use(
                        world,
                        event.getEntity(),
                        event.getHand(),
                        new BlockHitResult(Vec3.atLowerCornerOf(listener.getOrigin()).add(0.5, 0.5, 0.5), event.getFace(), listener.getOrigin(), false)
                     )
                  != InteractionResult.PASS) {
                  event.setCanceled(true);
                  event.setCancellationResult(InteractionResult.SUCCESS);
               }
            } else if (listener.onRightClick(
               event.getEntity(), event.getHand(), event.getFace(), new BlockHitResult(Vec3.atCenterOf(event.getPos()), event.getFace(), event.getPos(), false)
            )) {
               event.setCanceled(true);
               event.setCancellationResult(InteractionResult.SUCCESS);
            }
         }
      }
   }

   @SubscribeEvent
   public void onBlockLeftClick(LeftClickBlock event) {
      if (event.getEntity() != null) {
         Level world = event.getLevel();
         BlockPos pos = event.getPos();
         IMultiBlockClickable listener = this.getListeners(world).getPos(pos);
         if (listener != null && listener.getRequiredActions().canDoLeftClick()) {
            BlockState state = world.getBlockState(pos);
            if (listener.shouldAbsorbClick(pos, state)) {
               if (listener.shouldLinkToBlock(pos, state)) {
                  world.getBlockState(listener.getOrigin()).attack(world, listener.getOrigin(), event.getEntity());
                  event.setCanceled(true);
                  event.setCancellationResult(InteractionResult.SUCCESS);
               } else if (listener.onLeftClick(event.getEntity(), pos)) {
                  event.setCanceled(true);
                  event.setCancellationResult(InteractionResult.SUCCESS);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onWorldUnload(Unload event) {
      MultiBlockManager.MultiBlockListeners listener = this.listeners.remove(event.getLevel());
      if (listener != null) {
         listener.clear();
      }
   }

   public void onServerStopped() {
      this.listeners.clear();
      StructureManager.INSTANCE.onServerStopped();
   }

   public void addListeners(Level world, BlockPos pos, long[] listeners, IMultiBlockClickable listener) {
      if (listeners != null && listeners.length != 0 && listener != null) {
         this.getListeners(world).addListeners(pos, listeners, listener);
      }
   }

   public void removeListeners(Level world, BlockPos positions) {
      this.getListeners(world).removeListeners(positions);
   }

   private MultiBlockManager.MultiBlockListeners getListeners(LevelAccessor world) {
      MultiBlockManager.MultiBlockListeners listener = this.listeners.get(world);
      if (listener == null) {
         listener = new MultiBlockManager.MultiBlockListeners();
         this.listeners.put(world, listener);
      }

      return listener;
   }

   public static class MultiBlockListeners {
      Long2ObjectMap<IMultiBlockClickable> listeners = new Long2ObjectOpenHashMap();
      Long2ObjectMap<long[]> mappedPositions = new Long2ObjectOpenHashMap();

      public void addListeners(BlockPos pos, long[] positions, IMultiBlockClickable listener) {
         this.mappedPositions.put(pos.asLong(), positions);

         for (long position : positions) {
            this.listeners.put(position, listener);
         }
      }

      public void removeListeners(BlockPos pos) {
         long[] mapped = (long[])this.mappedPositions.remove(pos.asLong());
         if (mapped != null) {
            for (long l : mapped) {
               this.listeners.remove(l);
            }
         }
      }

      public void clear() {
         this.listeners.clear();
         this.mappedPositions.clear();
         this.listeners = null;
         this.mappedPositions = null;
      }

      public IMultiBlockClickable getPos(BlockPos pos) {
         return (IMultiBlockClickable)this.listeners.get(pos.asLong());
      }
   }
}
