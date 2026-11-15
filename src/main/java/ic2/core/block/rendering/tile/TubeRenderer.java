package ic2.core.block.rendering.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import ic2.api.tiles.tubes.TransportedItem;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.block.transport.item.tubes.ColorTubeTileEntity;
import ic2.core.platform.rendering.RenderShapes;
import ic2.core.platform.rendering.misc.IC2RenderTypes;
import ic2.core.utils.math.ColorUtils;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TubeRenderer implements BlockEntityRenderer<TubeTileEntity> {
   static final Vec3[] OFFSETS = new Vec3[]{
      new Vec3(0.0, -0.5, 0.0), new Vec3(0.0, 0.5, 0.0), new Vec3(0.0, 0.0, -0.5), new Vec3(0.0, 0.0, 0.5), new Vec3(-0.5, 0.0, 0.0), new Vec3(0.5, 0.0, 0.0)
   };
   static final AABB CUBE_BOX = new AABB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5);
   final int[] dyeColors = new int[16];

   public TubeRenderer(Context context) {
      for (int i = 0; i < 16; i++) {
         float[] color = DyeColor.byId(i).getTextureDiffuseColors();
         this.dyeColors[i] = ColorUtils.rgb(color[0], color[1], color[2], 0.5F);
      }
   }

   public void render(
      TubeTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn
   ) {
      ObjectList<TransportedItem> items = tileEntityIn.items;
      if (!items.isEmpty() && tileEntityIn.synchronize) {
         matrixStackIn.translate(0.5, 0.5, 0.5);
         Player player = Minecraft.getInstance().player;
         Level world = Minecraft.getInstance().level;
         ItemRenderer render = Minecraft.getInstance().getItemRenderer();
         int i = 0;

         for (int m = Math.min(items.size(), 200); i < m; i++) {
            TransportedItem item = (TransportedItem)items.get(i);
            Direction dir = item.getTransferDirection();
            Vec3 vec = OFFSETS[dir.get3DDataValue()]
               .scale(
                  (double)Mth.clamp(item.isCentering() ? 1.0F - ((float)item.getProgress() + 100.0F) / 100.0F : (float)item.getProgress() / 100.0F, 0.0F, 1.0F)
               );
            matrixStackIn.translate(vec.x, vec.y, vec.z);
            matrixStackIn.scale(0.25F, 0.25F, 0.25F);
            dir = item.isCentering() ? dir.getOpposite() : dir;
            matrixStackIn.mulPose(this.getRotation(dir, 1.0F));
            render.renderStatic(player, item.getStack(), ItemDisplayContext.FIXED, false, matrixStackIn, bufferIn, world, combinedLightIn, combinedOverlayIn, 0);
            DyeColor color = item.getColor();
            if (color != null) {
               RenderShapes.renderColorCube(CUBE_BOX, this.dyeColors[color.getId()], bufferIn.getBuffer(IC2RenderTypes.TUBE_ITEM_HIGHLIGHT), matrixStackIn);
            }

            matrixStackIn.mulPose(this.getRotation(dir, -1.0F));
            matrixStackIn.scale(4.0F, 4.0F, 4.0F);
            matrixStackIn.translate(-vec.x, -vec.y, -vec.z);
         }

         matrixStackIn.translate(-0.5, -0.5, -0.5);
      }
   }

   public Quaternionf getRotation(Direction dir, float value) {
      return dir.getAxis().isHorizontal()
         ? com.mojang.math.Axis.YP.rotationDegrees(dir.toYRot() * value)
         : com.mojang.math.Axis.XP.rotationDegrees((dir.getAxisDirection() == AxisDirection.NEGATIVE ? 90.0F : 270.0F) * value);
   }

   public static class ColorTubeRenderer extends TubeRenderer {
      public ColorTubeRenderer(Context context) {
         super(context);
      }

      @Override
      public void render(
         TubeTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn
      ) {
         if (tileEntityIn instanceof ColorTubeTileEntity tile && tile.color != 0) {
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.scale(0.4F, 0.4F, 0.4F);
            RenderShapes.renderColorCube(CUBE_BOX, this.dyeColors[tile.color - 1], bufferIn.getBuffer(IC2RenderTypes.TUBE_ITEM_HIGHLIGHT), matrixStackIn);
            matrixStackIn.scale(2.5F, 2.5F, 2.5F);
            matrixStackIn.translate(-0.5, -0.5, -0.5);
         }

         super.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
      }
   }
}
