package ic2.core.platform.events;

import com.mojang.blaze3d.vertex.VertexConsumer;
import ic2.core.item.base.IC2Item;
import ic2.core.item.base.features.IMultiTargetTool;
import ic2.core.platform.corehacks.mixins.client.PlayerControllerMixin;
import ic2.core.platform.rendering.RenderShapes;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.IterableWrapper;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent.Block;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MultiMineToolHandler {
   public static final MultiMineToolHandler INSTANCE = new MultiMineToolHandler();
   Map<UUID, Entry<BlockPos, Direction>> positionMap = CollectionUtils.<UUID, Entry<BlockPos, Direction>>createMap();
   IntSet positions = new IntLinkedOpenHashSet();
   IntSet lastPositions = new IntLinkedOpenHashSet();

   @SubscribeEvent
   public void onBlockBreakSpeed(BreakSpeed event) {
      Player player = event.getEntity();
      ItemStack stack = player.getMainHandItem();
      Optional<BlockPos> position = event.getPosition();
      if (position.isPresent() && stack.getItem() instanceof IMultiTargetTool tool) {
         if (!tool.isMultiMining(stack) || !tool.canMultiMine(stack)) {
            return;
         }

         if (IC2Item.rayTrace(player.level(), player, false, player.getAttribute((Attribute)ForgeMod.BLOCK_REACH.get()).getValue()) instanceof BlockHitResult blockHit
            )
          {
            float totalHardness = 0.0F;
            int found = 0;

            for (BlockPos pos : IterableWrapper.wrap(tool.getHitPositions(stack, player, position.get(), blockHit.getDirection()))) {
               BlockState state = player.level().getBlockState(pos);
               if (!state.isAir()
                  && !state.liquid()
                  && state.canHarvestBlock(player.level(), pos, player)
                  && state.getDestroySpeed(player.level(), pos) >= 0.0F) {
                  found++;
                  totalHardness += Math.max(1.0F, state.getDestroySpeed(player.level(), pos));
               }
            }

            if (found > 0) {
               event.setNewSpeed(event.getNewSpeed() / totalHardness * Math.max(1.0F, event.getState().getDestroySpeed(player.level(), position.get())));
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent
   public void onLeftClick(LeftClickBlock event) {
      if (!event.getSide().isServer()) {
         this.positionMap.put(event.getEntity().getUUID(), new SimpleEntry<>(event.getPos(), event.getFace()));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent
   public void onDestroyProgress(PlayerTickEvent event) {
      if (!event.side.isServer() && event.phase != Phase.START) {
         MultiPlayerGameMode controller = Minecraft.getInstance().gameMode;
         LevelRenderer renderer = Minecraft.getInstance().levelRenderer;
         if (!controller.isDestroying()) {
            this.positionMap.remove(event.player.getUUID());
            if (!this.positions.isEmpty()) {
               IntIterator var11 = this.positions.iterator();

               while (var11.hasNext()) {
                  int id = (Integer)var11.next();
                  renderer.destroyBlockProgress(id, BlockPos.ZERO, -1);
               }

               this.positions.clear();
            }
         } else {
            Entry<BlockPos, Direction> entry = this.positionMap.get(event.player.getUUID());
            if (entry != null) {
               ItemStack stack = event.player.getMainHandItem();
               if (stack.getItem() instanceof IMultiTargetTool target) {
                  if (!target.isMultiMining(stack) || !target.canMultiMine(stack)) {
                     return;
                  }

                  this.lastPositions.addAll(this.positions);
                  this.positions.clear();
                  int progress = (int)(((PlayerControllerMixin)controller).getDestroyProgress() * 10.0F) - 1;

                  for (BlockPos pos : IterableWrapper.wrap(target.getHitPositions(stack, event.player, entry.getKey(), entry.getValue()))) {
                     int id = Math.abs(pos.equals(entry.getKey()) ? event.player.getId() : pos.hashCode());
                     renderer.destroyBlockProgress(id, pos.immutable(), progress);
                     this.positions.add(id);
                  }

                  this.lastPositions.removeAll(this.positions);
                  if (!this.lastPositions.isEmpty()) {
                     IntIterator var14 = this.lastPositions.iterator();

                     while (var14.hasNext()) {
                        int id = (Integer)var14.next();
                        renderer.destroyBlockProgress(id, BlockPos.ZERO, -1);
                     }

                     this.lastPositions.clear();
                  }
               }
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent
   public void onBlockHighlightEvent(Block event) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         ItemStack stack = player.getMainHandItem();
         if (stack.getItem() instanceof IMultiTargetTool target) {
            if (!target.isMultiMining(stack) || !target.canMultiMine(stack)) {
               return;
            }

            WorldBorder border = player.level().getWorldBorder();
            Vec3 view = event.getCamera().getPosition();
            BlockHitResult result = event.getTarget();
            VertexConsumer builder = event.getMultiBufferSource().getBuffer(RenderType.lines());
            int count = 0;

            for (BlockPos pos : IterableWrapper.wrap(target.getHitPositions(stack, player, result.getBlockPos(), result.getDirection()))) {
               BlockState state = player.level().getBlockState(pos);
               if (!state.isAir() && state.getDestroySpeed(player.level(), pos) != -1.0F && border.isWithinBounds(pos)) {
                  RenderShapes.drawSelectionBox(event.getPoseStack(), builder, player, view.x(), view.y(), view.z(), pos, state);
                  count++;
               }
            }

            if (count > 0) {
               event.setCanceled(true);
            }
         }
      }
   }
}
