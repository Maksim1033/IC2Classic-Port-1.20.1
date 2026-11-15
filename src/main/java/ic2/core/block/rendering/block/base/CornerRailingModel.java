package ic2.core.block.rendering.block.base;

import ic2.core.block.rendering.props.ColorProperty;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class CornerRailingModel extends BaseModel {
   static final ShapeBuilder INNER = new ShapeBuilder()
      .newQuad(0.0, 0.0, 0.0, 2.0, 16.0, 2.0)
      .addCube(
         new float[]{1.0F, 1.0F, 3.0F, 3.0F},
         new float[]{1.0F, 1.0F, 3.0F, 3.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F},
         new float[]{1.0F, 1.0F, 3.0F, 15.0F}
      )
      .finish();
   private Map<String, TextureAtlasSprite> sprites;

   public CornerRailingModel(String path) {
      this.sprites = IC2Textures.getMappedEntriesBlockIC2(path);
      this.setParticleTexture(this.sprites.get(DyeColor.BLUE.getName()));
   }

   @Override
   public void init() {
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      if (side != null) {
         return empty();
      } else {
         List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();
         DyeColor color = (DyeColor)extraData.get(ColorProperty.FIRST);
         if (color != null) {
            INNER.buildQuads(this.sprites.get(color.getName()), BlockModelRotation.X0_Y0, null, true, quads);
         }

         color = (DyeColor)extraData.get(ColorProperty.SECOND);
         if (color != null) {
            INNER.buildQuads(this.sprites.get(color.getName()), BlockModelRotation.X0_Y90, null, true, quads);
         }

         color = (DyeColor)extraData.get(ColorProperty.THIRD);
         if (color != null) {
            INNER.buildQuads(this.sprites.get(color.getName()), BlockModelRotation.X0_Y180, null, true, quads);
         }

         color = (DyeColor)extraData.get(ColorProperty.FOURTH);
         if (color != null) {
            INNER.buildQuads(this.sprites.get(color.getName()), BlockModelRotation.X0_Y270, null, true, quads);
         }

         return quads;
      }
   }

   @Override
   public TextureAtlasSprite getParticleIcon(ModelData data) {
      DyeColor color = (DyeColor)data.get(ColorProperty.FIRST);
      if (color != null) {
         return this.sprites.get(color.getName());
      } else {
         color = (DyeColor)data.get(ColorProperty.SECOND);
         if (color != null) {
            return this.sprites.get(color.getName());
         } else {
            color = (DyeColor)data.get(ColorProperty.THIRD);
            if (color != null) {
               return this.sprites.get(color.getName());
            } else {
               color = (DyeColor)data.get(ColorProperty.FOURTH);
               return color != null ? this.sprites.get(color.getName()) : super.getParticleIcon(data);
            }
         }
      }
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }
}
