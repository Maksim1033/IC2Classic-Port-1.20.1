package ic2.core.block.rendering.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ic2.api.blocks.IWrenchable;
import ic2.api.blocks.WrenchHelper;
import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.RenderShapes;
import ic2.core.platform.rendering.misc.IC2RenderTypes;
import ic2.core.utils.math.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WrenchOverlay {
   private static final int CHANGE_FACING_COLOR = -1977136975;
   private static final int REMOVE_BLOCK_COLOR = -1967911903;
   private static final int SPECIAL_ACTION_COLOR = -1967870452;
   public static final WrenchOverlay INSTANCE = new WrenchOverlay();

   @OnlyIn(Dist.CLIENT)
   @SubscribeEvent
   public void onHighlightEvent(Block event) {
      BlockHitResult result = event.getTarget();
      if (result.getType() != Type.MISS) {
         Minecraft mc = Minecraft.getInstance();
         BlockPos pos = result.getBlockPos();
         BlockState state = mc.level.getBlockState(pos);
         IWrenchable wrench = IWrenchable.WrenchRegistry.INSTANCE.getWrenchable(state);
         if (wrench != null && WrenchHelper.hasWrench(mc.player)) {
            DirectionList validSides = this.canSetAnyFacing(wrench, state, pos, mc.player);
            boolean remove = wrench.canRemoveBlock(state, mc.level, pos, mc.player);
            int slot = !validSides.isEmpty() ? WrenchHelper.getDirectionIndex(result) : (remove ? 1 : 0);
            Direction currentDir = result.getDirection();
            Direction nextDir = WrenchHelper.getFacingFromIndex(currentDir, slot, mc.player);
            if (nextDir == null || validSides.notContains(nextDir)) {
               AABB box = wrench.hasSpecialAction(state, mc.level, pos, currentDir, mc.player, result.getLocation().subtract(Vec3.atLowerCornerOf(pos)));
               if (box != null) {
                  PoseStack stack = event.getPoseStack();
                  stack.pushPose();
                  Vec3 info = event.getCamera().getPosition();
                  stack.translate(-info.x(), -info.y(), -info.z());
                  stack.translate((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
                  RenderShapes.renderColorCube(
                     DirectionList.ALL,
                     box.inflate(0.0010000000474974513),
                     -1967870452,
                     event.getMultiBufferSource().getBuffer(IC2RenderTypes.POS_COLOR_TRANSLUCENT),
                     stack
                  );
                  stack.popPose();
                  return;
               }

               if (!remove) {
                  return;
               }
            }

            PoseStack stack = event.getPoseStack();
            stack.pushPose();
            Vec3 info = event.getCamera().getPosition();
            stack.translate(-info.x(), -info.y(), -info.z());
            stack.translate((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            float offset = result.getDirection().getAxisDirection() == AxisDirection.POSITIVE
               ? (float)(1.0 - state.getShape(mc.level, pos).max(result.getDirection().getAxis()))
               : (float)state.getShape(mc.level, pos).min(result.getDirection().getAxis());
            Vec3i dirInfo = result.getDirection().getNormal();
            stack.translate(
               (double)(-((float)dirInfo.getX() * offset)), (double)(-((float)dirInfo.getY() * offset)), (double)(-((float)dirInfo.getZ() * offset))
            );
            switch (currentDir) {
               case DOWN:
                  stack.mulPose(Axis.XP.rotationDegrees(90.0F));
                  stack.translate(0.0, 0.0, -1.0);
                  break;
               case UP:
                  stack.mulPose(Axis.XP.rotationDegrees(270.0F));
                  stack.translate(0.0, -1.0, 0.0);
               case SOUTH:
               default:
                  break;
               case NORTH:
                  stack.mulPose(Axis.YP.rotationDegrees(180.0F));
                  stack.translate(-1.0, 0.0, -1.0);
                  break;
               case EAST:
                  stack.mulPose(Axis.YP.rotationDegrees(90.0F));
                  stack.translate(-1.0, 0.0, 0.0);
                  break;
               case WEST:
                  stack.mulPose(Axis.YP.rotationDegrees(270.0F));
                  stack.translate(0.0, 0.0, -1.0);
            }

            MultiBufferSource buffer = event.getMultiBufferSource();
            this.drawFrame(buffer.getBuffer(RenderType.lines()), stack, -16777216);
            this.drawSide(
               buffer.getBuffer(IC2RenderTypes.POS_COLOR_TRANSLUCENT),
               stack,
               nextDir != null && !validSides.isEmpty() && !validSides.notContains(nextDir) ? -1977136975 : -1967911903,
               slot
            );
            stack.popPose();
         }
      }
   }

   private DirectionList canSetAnyFacing(IWrenchable wrench, BlockState state, BlockPos pos, Player player) {
      DirectionList list = DirectionList.EMPTY;

      for (Direction dir : DirectionList.ALL) {
         if (wrench.canSetFacing(state, player.level(), pos, player, dir)) {
            list = list.add(dir);
         }
      }

      return list;
   }

   private void drawSide(VertexConsumer builder, PoseStack stack, int color, int side) {
      Matrix4f matrix = stack.last().pose();
      float r = ColorUtils.getFloatR(color);
      float g = ColorUtils.getFloatG(color);
      float b = ColorUtils.getFloatB(color);
      float a = ColorUtils.getFloatA(color);
      if ((side & 1) != 0) {
         RenderShapes.renderColorQuad(Direction.SOUTH, 0.2F, 0.2F, 0.0F, 0.8F, 0.8F, 1.01F, color, builder, stack);
      }

      if ((side & 2) != 0) {
         RenderShapes.renderColorQuad(Direction.SOUTH, 0.2F, 0.0F, 0.0F, 0.8F, 0.2F, 1.01F, color, builder, stack);
         builder.vertex(matrix, 0.0F, 0.0F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.2F, 0.0F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.2F, 0.2F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 1.0F, 0.0F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.8F, 0.2F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.8F, 0.0F, 1.01F).color(r, g, b, a).endVertex();
      }

      if ((side & 4) != 0) {
         RenderShapes.renderColorQuad(Direction.SOUTH, 0.2F, 0.8F, 0.0F, 0.8F, 1.0F, 1.01F, color, builder, stack);
         builder.vertex(matrix, 0.0F, 1.0F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.2F, 0.8F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.2F, 1.0F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 1.0F, 1.0F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.8F, 1.0F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.8F, 0.8F, 1.01F).color(r, g, b, a).endVertex();
      }

      if ((side & 8) != 0) {
         RenderShapes.renderColorQuad(Direction.SOUTH, 0.0F, 0.2F, 0.0F, 0.2F, 0.8F, 1.01F, color, builder, stack);
         builder.vertex(matrix, 0.0F, 0.8F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.2F, 0.8F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.0F, 1.0F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.0F, 0.0F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.2F, 0.2F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.0F, 0.2F, 1.01F).color(r, g, b, a).endVertex();
      }

      if ((side & 16) != 0) {
         RenderShapes.renderColorQuad(Direction.SOUTH, 0.8F, 0.2F, 0.0F, 1.0F, 0.8F, 1.01F, color, builder, stack);
         builder.vertex(matrix, 0.8F, 0.8F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 1.0F, 0.8F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 1.0F, 1.0F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 1.0F, 0.0F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 1.0F, 0.2F, 1.01F).color(r, g, b, a).endVertex();
         builder.vertex(matrix, 0.8F, 0.2F, 1.01F).color(r, g, b, a).endVertex();
      }
   }

   private void drawFrame(VertexConsumer builder, PoseStack pose, int color) {
      Matrix4f matrix = pose.last().pose();
      Matrix3f normal = pose.last().normal();
      float r = ColorUtils.getFloatR(color);
      float g = ColorUtils.getFloatG(color);
      float b = ColorUtils.getFloatB(color);
      float a = ColorUtils.getFloatA(color);
      builder.vertex(matrix, 0.0F, 0.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.0F, 1.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 1.0F, 1.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.0F, 1.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 1.0F, 0.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.0F, 0.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 1.0F, 0.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 1.0F, 1.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.2F, 0.2F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.2F, 0.8F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.8F, 0.8F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.2F, 0.8F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.8F, 0.2F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.2F, 0.2F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.8F, 0.2F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.8F, 0.8F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.0F, 0.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.2F, 0.2F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.0F, 1.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.2F, 0.8F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 1.0F, 1.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.8F, 0.8F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 1.0F, 0.0F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
      builder.vertex(matrix, 0.8F, 0.2F, 1.01F).color(r, g, b, a).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
   }
}
