package ic2.core.block.rendering.block;

import ic2.api.util.DirectionList;
import ic2.core.block.crops.PlanterPotBlock;
import ic2.core.block.rendering.props.BlockProperty;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.utils.collection.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class PlanterPotModel extends BaseModel {
   static final ShapeBuilder SHAPE = new ShapeBuilder()
      .newQuad(2.0, 0.0, 2.0, 14.0, 4.0, 14.0)
      .addFaces(DirectionList.VERTICAL, 2.0F, 2.0F, 14.0F, 14.0F)
      .addFaces(DirectionList.HORIZONTAL, 2.0F, 12.0F, 14.0F, 16.0F)
      .finish()
      .newQuad(1.0, 4.0, 1.0, 15.0, 9.0, 15.0)
      .addFaces(DirectionList.VERTICAL, 1.0F, 1.0F, 15.0F, 15.0F)
      .addFaces(DirectionList.HORIZONTAL, 1.0F, 7.0F, 15.0F, 12.0F)
      .finish()
      .newQuad(0.0, 9.0, 0.0, 16.0, 15.0, 16.0)
      .addFaces(DirectionList.DOWN, 0.0F, 0.0F, 16.0F, 16.0F)
      .addFaces(DirectionList.HORIZONTAL, 0.0F, 1.0F, 16.0F, 7.0F)
      .finish()
      .newQuad(0.0, 15.0, 0.0, 16.0, 16.0, 1.0)
      .addCube(
         new float[]{0.0F, 0.0F, 16.0F, 1.0F},
         new float[]{0.0F, 0.0F, 16.0F, 1.0F},
         new float[]{0.0F, 0.0F, 16.0F, 1.0F},
         new float[]{0.0F, 0.0F, 16.0F, 1.0F},
         new float[]{15.0F, 0.0F, 16.0F, 1.0F},
         new float[]{0.0F, 0.0F, 1.0F, 1.0F}
      )
      .finish()
      .newQuad(0.0, 15.0, 15.0, 16.0, 16.0, 16.0)
      .addCube(
         new float[]{0.0F, 15.0F, 16.0F, 16.0F},
         new float[]{0.0F, 15.0F, 16.0F, 16.0F},
         new float[]{0.0F, 0.0F, 16.0F, 1.0F},
         new float[]{0.0F, 0.0F, 16.0F, 1.0F},
         new float[]{0.0F, 0.0F, 1.0F, 1.0F},
         new float[]{15.0F, 0.0F, 16.0F, 1.0F}
      )
      .finish()
      .newQuad(0.0, 15.0, 1.0, 1.0, 16.0, 15.0)
      .addCube(
         new float[]{0.0F, 1.0F, 1.0F, 15.0F},
         new float[]{0.0F, 1.0F, 1.0F, 15.0F},
         new float[]{0.0F, 0.0F, 1.0F, 1.0F},
         new float[]{15.0F, 0.0F, 16.0F, 1.0F},
         new float[]{1.0F, 0.0F, 15.0F, 1.0F},
         new float[]{1.0F, 0.0F, 15.0F, 1.0F}
      )
      .finish()
      .newQuad(15.0, 15.0, 1.0, 16.0, 16.0, 15.0)
      .addCube(
         new float[]{15.0F, 1.0F, 16.0F, 15.0F},
         new float[]{15.0F, 1.0F, 16.0F, 15.0F},
         new float[]{0.0F, 0.0F, 1.0F, 1.0F},
         new float[]{15.0F, 0.0F, 16.0F, 1.0F},
         new float[]{1.0F, 0.0F, 15.0F, 1.0F},
         new float[]{1.0F, 0.0F, 15.0F, 1.0F}
      )
      .finish();
   static final ShapeBuilder SOIL = new ShapeBuilder().newQuad(0.0, 9.0, 0.0, 16.0, 15.0, 16.0).addFace(Direction.UP, 0.0F, 0.0F, 16.0F, 16.0F).finish();
   TextureAtlasSprite[] textures;
   Block block;
   Map<Block, List<BakedQuad>> mappedQuads = CollectionUtils.<Block, List<BakedQuad>>createMap();
   List<BakedQuad> quads = CollectionUtils.<BakedQuad>createList();

   public PlanterPotModel(TextureAtlasSprite[] textures) {
      this.textures = textures;
      this.setParticleTexture(textures[0]);
   }

   @Override
   public void init() {
      SHAPE.buildQuads(this.textures, BlockModelRotation.X0_Y0, null, true, this.quads);
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType type) {
      if (side != null) {
         return empty();
      } else {
         List<BakedQuad> result = new ObjectArrayList(this.quads);
         if (extraData.has(BlockProperty.INSTANCE)) {
            this.block = (Block)extraData.get(BlockProperty.INSTANCE);
         }

         if (this.block != null && this.block != Blocks.AIR) {
            List<BakedQuad> mapped = this.mappedQuads.get(this.block);
            if (mapped == null) {
               mapped = new ObjectArrayList();
               this.mappedQuads.put(this.block, mapped);

               for (BakedQuad quad : Minecraft.getInstance()
                  .getBlockRenderer()
                  .getBlockModel(this.block.defaultBlockState())
                  .getQuads(this.block.defaultBlockState(), Direction.UP, rand, ModelData.EMPTY, type)) {
                  SOIL.buildQuads(quad.getSprite(), BlockModelRotation.X0_Y0, null, true, mapped);
               }
            }

            result.addAll(mapped);
         }

         return result;
      }
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }

   @Override
   public ItemOverrides getOverrides() {
      return new PlanterPotModel.PlotOverride();
   }

   public class PlotOverride extends ItemOverrides {
      public BakedModel resolve(BakedModel p_239290_1_, ItemStack p_239290_2_, ClientLevel p_239290_3_, LivingEntity p_239290_4_, int randomValue) {
         PlanterPotModel.this.block = PlanterPotBlock.getId(p_239290_2_);
         return super.resolve(p_239290_1_, p_239290_2_, p_239290_3_, p_239290_4_, randomValue);
      }
   }
}
