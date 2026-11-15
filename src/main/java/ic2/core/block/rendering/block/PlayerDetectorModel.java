package ic2.core.block.rendering.block;

import ic2.core.block.misc.PlayerDetectorBlock;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class PlayerDetectorModel extends BaseModel {
   private static final ShapeBuilder MODEL = new ShapeBuilder()
      .newQuad(5.0, 5.0, 0.0, 11.0, 11.0, 2.0)
      .addCube(
         new float[]{2.0F, 8.0F, 8.0F, 10.0F},
         new float[]{2.0F, 0.0F, 8.0F, 2.0F},
         new float[]{10.0F, 2.0F, 16.0F, 8.0F},
         new float[]{2.0F, 2.0F, 8.0F, 8.0F},
         new float[]{0.0F, 2.0F, 2.0F, 8.0F},
         new float[]{8.0F, 2.0F, 10.0F, 8.0F}
      )
      .finish();
   private static final ShapeBuilder ITEM = new ShapeBuilder()
      .newQuad(5.0, 5.0, 7.0, 11.0, 11.0, 9.0)
      .addCube(
         new float[]{2.0F, 8.0F, 8.0F, 10.0F},
         new float[]{2.0F, 0.0F, 8.0F, 2.0F},
         new float[]{10.0F, 2.0F, 16.0F, 8.0F},
         new float[]{2.0F, 2.0F, 8.0F, 8.0F},
         new float[]{0.0F, 2.0F, 2.0F, 8.0F},
         new float[]{8.0F, 2.0F, 10.0F, 8.0F}
      )
      .finish();
   private BlockState state;
   private String activeTextureName;
   private String inactiveTextureName;
   private String textureFolder;
   private List<BakedQuad> blockQuads = new ArrayList<>();
   private List<BakedQuad> itemQuads = new ArrayList<>();

   public PlayerDetectorModel(BlockState state, String textureFolder, String activeTextureName, String inactiveTextureName) {
      this.state = state;
      this.textureFolder = textureFolder;
      this.activeTextureName = activeTextureName;
      this.inactiveTextureName = inactiveTextureName;
   }

   @Override
   public void init() {
      boolean powered = (Boolean)this.state.getValue(PlayerDetectorBlock.ACTIVE);
      TextureAtlasSprite texture = IC2Textures.getMappedEntriesBlockIC2(this.textureFolder).get(powered ? this.activeTextureName : this.inactiveTextureName);
      this.setParticleTexture(texture);
      Direction facing = (Direction)this.state.getValue(PlayerDetectorBlock.FACING);
      BlockModelRotation rotation = BlockModelRotation.by(
         facing.getAxis().isVertical() ? (facing.get3DDataValue() == 0 ? -90 : 90) : 0, facing.get2DDataValue() == -1 ? 0 : facing.get2DDataValue() * 90
      );
      MODEL.buildQuads(texture, rotation, null, true, this.blockQuads);
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
