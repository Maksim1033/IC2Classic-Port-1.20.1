package ic2.core.block.rendering.world.impl;

import com.mojang.blaze3d.platform.InputConstants.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ic2.core.IC2;
import ic2.core.block.rendering.world.IWorldOverlay;
import ic2.core.platform.player.KeyboardClient;
import ic2.core.platform.player.PlayerHandler;
import ic2.core.platform.rendering.RenderUtils;
import ic2.core.platform.rendering.misc.ColoringVertexBuilder;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.tooltips.ILangHelper;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.InputEvent.Key;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MultiBlockOverlay implements IWorldOverlay, ILangHelper {
   public static final MultiBlockOverlay INSTANCE = new MultiBlockOverlay();
   Map<MultiBlock, MultiBlockOverlay.Renderer> renderer = CollectionUtils.<MultiBlock, MultiBlockOverlay.Renderer>createMap();
   MultiBlock selectedMultiBlock = null;
   BlockPos selectedPos = BlockPos.ZERO;
   Rotation selectedRotation = Rotation.NONE;

   public void addMultiBlock(MultiBlock block, BlockPos pos) {
      this.addMultiBlock(block, pos, Rotation.NONE);
   }

   public void addMultiBlock(MultiBlock block, BlockPos pos, Rotation rotation) {
      this.addMultiBlock(block, (Supplier<BlockPos>)(() -> pos), (Supplier<Rotation>)(() -> rotation));
   }

   public void addMultiBlock(MultiBlock block, Supplier<BlockPos> pos) {
      this.addMultiBlock(block, pos, () -> Rotation.NONE);
   }

   public void addMultiBlock(MultiBlock block, Supplier<BlockPos> pos, Supplier<Rotation> rotation) {
      this.renderer.put(block, new MultiBlockOverlay.Renderer(block, pos, rotation));
   }

   public void removeMultiBlock(MultiBlock block) {
      this.renderer.remove(block);
   }

   public void setSelectedMultiBlock(MultiBlock multiBlock) {
      if (this.selectedMultiBlock == null) {
         this.renderer.remove(multiBlock);
         this.renderer.put(multiBlock, new MultiBlockOverlay.Renderer(multiBlock, this::selectedPos, this::selectedRotation));
         this.selectedMultiBlock = multiBlock;
      }
   }

   public boolean hasSelectedMultiBlock() {
      return this.selectedMultiBlock != null;
   }

   public void clearSelectedMultiBlock() {
      if (this.selectedMultiBlock != null) {
         this.renderer.remove(this.selectedMultiBlock);
         this.selectedMultiBlock = null;
         this.selectedRotation = Rotation.NONE;
         this.selectedPos = BlockPos.ZERO;
      }
   }

   public void onRenderUI(PoseStack stack) {
      if (this.hasSelectedMultiBlock()) {
         Minecraft mc = Minecraft.getInstance();
         Font font = mc.font;
         MultiBufferSource.BufferSource source = mc.renderBuffers().bufferSource();
         org.joml.Matrix4f mat = stack.last().pose();
         font.drawInBatch(this.buildKeyDescription(new Object[]{this.combineKeys(new KeyMapping[]{mc.options.keyAttack}), "remove"}), 2.0F, 2.0F, -1, true, mat, source, Font.DisplayMode.NORMAL, 0, 15728880);
         font.drawInBatch(this.buildKeyDescription(new Object[]{this.combineKeys(new KeyMapping[]{mc.options.keyUse}), "place"}), 2.0F, 11.0F, -1, true, mat, source, Font.DisplayMode.NORMAL, 0, 15728880);
         font.drawInBatch(
            this.buildKeyDescription(
               new Object[]{
                  this.combineKeys(new KeyMapping[]{((KeyboardClient)IC2.KEYBOARD).expandInfoKey, mc.options.keyPickItem}), "pick existing structure"
               }
            ),
            2.0F,
            20.0F,
            -1,
            true,
            mat,
            source,
            Font.DisplayMode.NORMAL,
            0,
            15728880
         );
         font.drawInBatch(
            this.buildKeyDescription(new Object[]{Type.KEYSYM.getOrCreate(82).getDisplayName().copy().withStyle(ChatFormatting.GOLD), "rotate"}),
            2.0F,
            29.0F,
            -1,
            true,
            mat,
            source,
            Font.DisplayMode.NORMAL,
            0,
            15728880
         );
         source.endBatch();
      }
   }

   @SubscribeEvent
   public void onKeyPressed(InputEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.screen == null) {
         if (this.hasSelectedMultiBlock()) {
            if (mc.options.keyUse.consumeClick()) {
               mc.options.keyUse.setDown(false);
               MultiBlock toPlace = this.selectedMultiBlock;
               Rotation newRotation = this.selectedRotation;
               BlockPos pos = this.selectedPos;
               this.clearSelectedMultiBlock();
               this.addMultiBlock(toPlace, pos, newRotation);
            } else if (mc.options.keyAttack.consumeClick()) {
               mc.options.keyAttack.setDown(false);
               this.clearSelectedMultiBlock();
            } else if (event instanceof Key key && key.getAction() == 1 && key.getKey() == 82) {
               KeyMapping.set(Type.KEYSYM.getOrCreate(82), false);
               this.selectedRotation = this.selectedRotation.getRotated(Rotation.CLOCKWISE_90);
            }
         } else if (PlayerHandler.getClientHandler().hudModeKeyDown && mc.options.keyPickItem.consumeClick()) {
            MultiBlock block = this.findLookingAt(mc.player, 100.0);
            if (block != null) {
               this.selectedRotation = this.renderer.get(block).rotation.get();
               this.setSelectedMultiBlock(block);
            }
         }
      }
   }

   @Override
   public void cleanup() {
      this.renderer.clear();
      this.clearSelectedMultiBlock();
   }

   @Override
   public void update(Level world, Player player) {
      if (this.selectedMultiBlock != null) {
         this.selectedPos = this.getViewPosition(player, 50.0);
      }

      if (!this.renderer.isEmpty()) {
         if (world.getGameTime() % 20L == 0L) {
            Iterator<MultiBlockOverlay.Renderer> iter = this.renderer.values().iterator();

            while (iter.hasNext()) {
               if (iter.next().isFullyBuild(world)) {
                  iter.remove();
               }
            }
         }
      }
   }

   @Override
   public void render(Level world, Player player, RenderLevelStageEvent event, Frustum helper) {
      if (!this.renderer.isEmpty()) {
         boolean formed = !IC2.KEYBOARD.isHudModeKeyDown(player);
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderUtils.draw(event.getPoseStack(), (B, M) -> {
            BufferSource buffer = MultiBufferSource.immediate(B);
            VertexConsumer builder = new ColoringVertexBuilder(buffer.getBuffer(RenderType.translucent()), 128);

            for (MultiBlockOverlay.Renderer render : this.renderer.values()) {
               render.render(M, builder, world, formed);
            }

            buffer.endBatch();
         });
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      }
   }

   private BlockPos getViewPosition(Player player, double range) {
      HitResult result = player.pick(range, 1.0F, false);
      if (result instanceof BlockHitResult && result.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
         MutableBlockPos end = new MutableBlockPos().set(((BlockHitResult)result).getBlockPos()).move(Direction.UP);

         while (this.isEmptyBlock(player.level(), end) && end.getY() > 0) {
            end.move(Direction.DOWN);
         }

         return end.move(Direction.UP).immutable();
      } else {
         Vec3 lookingDir = player.getViewVector(1.0F);
         MutableBlockPos end = new MutableBlockPos()
            .set(BlockPos.containing(player.getEyePosition(1.0F).add(lookingDir.x() * range, lookingDir.y() * range, lookingDir.z() * range)));

         while (this.isEmptyBlock(player.level(), end) && end.getY() > 0) {
            end.move(Direction.DOWN);
         }

         return end.move(Direction.UP).immutable();
      }
   }

   private boolean isEmptyBlock(Level world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      return state.isAir() || !state.blocksMotion();
   }

   private MultiBlock findLookingAt(Player player, double range) {
      if (this.renderer.isEmpty()) {
         return null;
      } else {
         Vec3 lookingDir = player.getViewVector(1.0F);
         Vec3 start = player.getEyePosition(1.0F);
         Vec3 end = start.add(lookingDir.x() * range, lookingDir.y() * range, lookingDir.z() * range);
         MultiBlock current = null;
         double distance = 1.7976931348623157E308;

         for (MultiBlockOverlay.Renderer block : this.renderer.values()) {
            Optional<Vec3> clip = block.getBox().clip(start, end);
            if (!clip.isEmpty()) {
               double d1 = start.distanceToSqr(clip.get());
               if (d1 < distance) {
                  current = block.block;
                  distance = d1;
               }
            }
         }

         return current;
      }
   }

   private BlockPos selectedPos() {
      return this.selectedPos;
   }

   private Rotation selectedRotation() {
      return this.selectedRotation;
   }

   private static class Renderer {
      MultiBlock block;
      Supplier<BlockPos> position;
      Supplier<Rotation> rotation;

      public Renderer(MultiBlock block, Supplier<BlockPos> position, Supplier<Rotation> rotation) {
         this.block = block;
         this.position = position;
         this.rotation = rotation;
      }

      public AABB getBox() {
         return this.block.getBounds().move(this.position.get());
      }

      public boolean isFullyBuild(Level world) {
         return this.block.isFullyBuild(world, this.position.get());
      }

      public void render(PoseStack stack, VertexConsumer builder, Level world, boolean formed) {
         BlockPos pos = this.position.get();
         stack.pushPose();
         stack.translate((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
         this.block.renderMultiBlock(stack, builder, world, pos, this.rotation.get(), formed);
         stack.popPose();
      }
   }
}
