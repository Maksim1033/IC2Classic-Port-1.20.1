package ic2.core.block.rendering.block.base;

import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class ButtonModel extends BaseModel {
   private static final ShapeBuilder ACTIVE = new ShapeBuilder()
      .newQuad(5.0, 0.0, 6.0, 11.0, 1.0, 10.0)
      .addHorizontalCube(
         new float[]{5.0F, 14.0F, 11.0F, 15.0F},
         new float[]{5.0F, 14.0F, 11.0F, 15.0F},
         new float[]{6.0F, 14.0F, 10.0F, 16.0F},
         new float[]{6.0F, 14.0F, 10.0F, 16.0F}
      )
      .addFace(Direction.DOWN, 5.0F, 6.0F, 11.0F, 10.0F)
      .addFace(Direction.UP, 5.0F, 10.0F, 11.0F, 6.0F)
      .finish();
   private static final ShapeBuilder INACTIVE = new ShapeBuilder()
      .newQuad(5.0, 0.0, 6.0, 11.0, 2.0, 10.0)
      .addHorizontalCube(
         new float[]{5.0F, 14.0F, 11.0F, 16.0F},
         new float[]{5.0F, 14.0F, 11.0F, 16.0F},
         new float[]{6.0F, 14.0F, 10.0F, 16.0F},
         new float[]{6.0F, 14.0F, 10.0F, 16.0F}
      )
      .addFace(Direction.DOWN, 5.0F, 6.0F, 11.0F, 10.0F)
      .addFace(Direction.UP, 5.0F, 10.0F, 11.0F, 6.0F)
      .finish();
   private static final ShapeBuilder ITEM = new ShapeBuilder()
      .newQuad(5.0, 6.0, 6.0, 11.0, 10.0, 10.0)
      .addHorizontalCube(
         new float[]{5.0F, 12.0F, 11.0F, 16.0F},
         new float[]{5.0F, 12.0F, 11.0F, 16.0F},
         new float[]{6.0F, 12.0F, 10.0F, 16.0F},
         new float[]{6.0F, 12.0F, 10.0F, 16.0F}
      )
      .addFace(Direction.DOWN, 5.0F, 6.0F, 11.0F, 10.0F)
      .addFace(Direction.UP, 5.0F, 10.0F, 11.0F, 6.0F)
      .finish();
   private BlockState state;
   private String textureName;
   private String textureFolder;
   private List<BakedQuad> blockQuads = CollectionUtils.<BakedQuad>createList();
   private List<BakedQuad> itemQuads = CollectionUtils.<BakedQuad>createList();

   public ButtonModel(BlockState state, String textureFolder, String textureName) {
      this.state = state;
      this.textureFolder = textureFolder;
      this.textureName = textureName;
   }

   @Override
   public void init() {
      TextureAtlasSprite texture = IC2Textures.getMappedEntriesBlockIC2(this.textureFolder).get(this.textureName);
      this.setParticleTexture(texture);
      boolean powered = (Boolean)this.state.getValue(ButtonBlock.POWERED);
      AttachFace face = (AttachFace)this.state.getValue(ButtonBlock.FACE);
      BlockModelRotation rotation = BlockModelRotation.by(
         face == AttachFace.WALL ? -90 : (face == AttachFace.CEILING ? 180 : 0),
         (((Direction)this.state.getValue(ButtonBlock.FACING)).get2DDataValue() + (face == AttachFace.FLOOR ? 2 : 0)) * 90
      );
      (powered ? ACTIVE : INACTIVE).buildQuads(texture, rotation, null, true, this.blockQuads);
      ITEM.buildQuads(texture, rotation, null, true, this.itemQuads);
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType type) {
      if (side != null) {
         return empty();
      } else {
         return state == null ? this.itemQuads : this.blockQuads;
      }
   }

   @Override
   public boolean isGui3d() {
      return false;
   }

   @Override
   public boolean usesBlockLight() {
      return true;
   }
}
