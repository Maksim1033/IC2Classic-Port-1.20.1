package ic2.core.block.rendering.block.base;

import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.ShapeBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class DoorModel extends BaseModel {
   private static final ShapeBuilder OPEN_MODEL = new ShapeBuilder()
      .newQuad(0.0, 0.0, 0.0, 3.0, 16.0, 16.0)
      .addCube(
         new float[]{13.0F, 0.0F, 16.0F, 16.0F},
         new float[]{13.0F, 0.0F, 16.0F, 16.0F},
         new float[]{3.0F, 0.0F, 0.0F, 16.0F},
         new float[]{0.0F, 0.0F, 3.0F, 16.0F},
         new float[]{16.0F, 0.0F, 0.0F, 16.0F},
         new float[]{0.0F, 0.0F, 16.0F, 16.0F}
      )
      .finish();
   private static final ShapeBuilder CLOSE_MODEL = new ShapeBuilder()
      .newQuad(0.0, 0.0, 0.0, 3.0, 16.0, 16.0)
      .addCube(
         new float[]{13.0F, 0.0F, 16.0F, 16.0F},
         new float[]{13.0F, 0.0F, 16.0F, 16.0F},
         new float[]{3.0F, 0.0F, 0.0F, 16.0F},
         new float[]{0.0F, 0.0F, 3.0F, 16.0F},
         new float[]{0.0F, 0.0F, 16.0F, 16.0F},
         new float[]{16.0F, 0.0F, 0.0F, 16.0F}
      )
      .finish();
   private BlockState state;
   private String textureNameUpper;
   private String textureNameLower;
   private String textureFolder;
   private List<BakedQuad> quads = new ArrayList<>();

   public DoorModel(BlockState state, String textureFolder, String textureNameUpper, String textureNameLower) {
      this.state = state;
      this.textureFolder = textureFolder;
      this.textureNameUpper = textureNameUpper;
      this.textureNameLower = textureNameLower;
   }

   @Override
   public void init() {
      Map<String, TextureAtlasSprite> textureMap = IC2Textures.getMappedEntriesBlockIC2(this.textureFolder);
      this.setParticleTexture(
         this.state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? textureMap.get(this.textureNameUpper) : textureMap.get(this.textureNameLower)
      );
      boolean up = this.state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER;
      boolean open = (Boolean)this.state.getValue(DoorBlock.OPEN);
      boolean leftSided = this.state.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT;
      BlockModelRotation rotation = BlockModelRotation.by(
         0, (((Direction)this.state.getValue(DoorBlock.FACING)).get2DDataValue() + 1 + (open ? (leftSided ? 1 : 3) : 0) & 3) * 90
      );
      (leftSided ? (open ? OPEN_MODEL : CLOSE_MODEL) : (open ? CLOSE_MODEL : OPEN_MODEL))
         .buildQuads(textureMap.get(up ? this.textureNameUpper : this.textureNameLower), rotation, null, true, this.quads);
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType type) {
      return state != null && side == null ? this.quads : empty();
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
