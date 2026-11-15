package ic2.core.block.crops.renderer;

import org.joml.Vector3f;
import ic2.api.crops.ICrop;
import ic2.api.crops.ICropRenderer;
import ic2.api.util.DirectionList;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DefaultCropRenderer implements ICropRenderer {
   @OnlyIn(Dist.CLIENT)
   public static final Vector3f[][] SIDES = new Vector3f[][]{
      {new Vector3f(0.0F, -1.0F, 12.0F), new Vector3f(16.0F, 15.0F, 12.0F)},
      {new Vector3f(4.0F, -1.0F, 0.0F), new Vector3f(4.0F, 15.0F, 16.0F)},
      {new Vector3f(0.0F, -1.0F, 4.0F), new Vector3f(16.0F, 15.0F, 4.0F)},
      {new Vector3f(12.0F, -1.0F, 0.0F), new Vector3f(12.0F, 15.0F, 16.0F)}
   };
   ICrop crop;

   public DefaultCropRenderer(ICrop crop) {
      this.crop = crop;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public List<BakedQuad> createQuadsForStage(int stage, boolean fancy, FaceBakery baker) {
      List<ResourceLocation> list = this.crop.getTextures();
      if (list.size() <= stage) {
         return null;
      } else {
         ResourceLocation location = new ResourceLocation("ic2:crop_model");
         List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();
         BlockElementFace face = new BlockElementFace(null, -1, "", new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0));
         TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(list.get(stage));

         for (Direction facing : DirectionList.HORIZONTAL) {
            Vector3f min = SIDES[facing.get2DDataValue()][0];
            Vector3f max = SIDES[facing.get2DDataValue()][1];
            if (facing.getAxisDirection() == AxisDirection.POSITIVE) {
               float offset = 0.002F;
               max = new Vector3f(max).add((float)facing.getStepX() * offset, (float)facing.getStepY() * offset, (float)facing.getStepZ() * offset);
            } else {
               float offset = 0.002F;
               min = new Vector3f(min).add((float)facing.getStepX() * offset, (float)facing.getStepY() * offset, (float)facing.getStepZ() * offset);
            }

            quads.add(baker.bakeQuad(min, max, face, sprite, facing, BlockModelRotation.X0_Y0, null, false, location));
            quads.add(baker.bakeQuad(min, max, face, sprite, facing.getOpposite(), BlockModelRotation.X0_Y0, null, false, location));
         }

         return quads;
      }
   }
}
