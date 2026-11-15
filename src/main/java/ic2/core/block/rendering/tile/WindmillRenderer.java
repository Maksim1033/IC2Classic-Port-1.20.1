package ic2.core.block.rendering.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import ic2.core.block.generators.tiles.WindmillTileEntity;
import ic2.core.block.rendering.models.RotorModel;
import ic2.core.item.misc.WindmillBladeItem;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WindmillRenderer implements BlockEntityRenderer<WindmillTileEntity> {
   Int2ObjectMap<RotorModel> models = new Int2ObjectOpenHashMap();
   static final Map<ResourceLocation, Material> MATERIALS = CollectionUtils.<ResourceLocation, Material>createMap();

   public WindmillRenderer(Context context) {
   }

   public boolean shouldRenderOffScreen(WindmillTileEntity p_188185_1_) {
      return true;
   }

   public void render(
      WindmillTileEntity tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn
   ) {
      ResourceLocation location = tile.getTexture();
      int radius = tile.getRadius();
      if (radius > 0 && location != null) {
         tile.rotation = (tile.rotation + tile.speed) % 360.0F;
         RotorModel model = (RotorModel)this.models.computeIfAbsent(radius, RotorModel::new);
         matrixStackIn.pushPose();
         matrixStackIn.translate(0.5, 0.5, 0.5);
         matrixStackIn.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-tile.getFacing().toYRot() + 90.0F));
         matrixStackIn.mulPose(com.mojang.math.Axis.XP.rotationDegrees(tile.rotation));
         matrixStackIn.translate(-0.25, 0.0, 0.0);
         model.renderToBuffer(
            matrixStackIn,
            getMaterial(location).buffer(bufferIn, model::renderType),
            LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(tile.getFacing())),
            combinedOverlayIn,
            1.0F,
            1.0F,
            1.0F,
            1.0F
         );
         matrixStackIn.popPose();
      }
   }

   public static Material getMaterial(ResourceLocation location) {
      return MATERIALS.computeIfAbsent(location, WindmillRenderer::createMaterial);
   }

   private static Material createMaterial(ResourceLocation location) {
      return new Material(WindmillBladeItem.ROTOR_SHEET, location);
   }
}
