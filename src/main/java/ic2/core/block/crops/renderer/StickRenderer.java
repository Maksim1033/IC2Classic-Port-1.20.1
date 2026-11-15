package ic2.core.block.crops.renderer;

import org.joml.Vector3f;
import ic2.api.crops.ICropRenderer;
import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.IC2Textures;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StickRenderer implements ICropRenderer {
   @OnlyIn(Dist.CLIENT)
   @Override
   public List<BakedQuad> createQuadsForStage(int stage, boolean fancy, FaceBakery baker) {
      ResourceLocation location = new ResourceLocation("ic2:crop_sticks");
      List<BakedQuad> quads = new ArrayList<>();
      TextureAtlasSprite sprite = IC2Textures.getMappedEntriesBlockIC2("crops").get(stage == 0 ? "cropsticks" : "crossed_cropsticks");
      BlockElementFace face = new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0));

      for (Direction facing : DirectionList.HORIZONTAL) {
         Vector3f min = DefaultCropRenderer.SIDES[facing.get2DDataValue()][0];
         Vector3f max = DefaultCropRenderer.SIDES[facing.get2DDataValue()][1];
         quads.add(baker.bakeQuad(min, max, face, sprite, facing, BlockModelRotation.X0_Y0, null, false, location));
         quads.add(baker.bakeQuad(min, max, face, sprite, facing.getOpposite(), BlockModelRotation.X0_Y0, null, false, location));
      }

      return quads;
   }
}
