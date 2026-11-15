package ic2.core.block.rendering.block.base;

import ic2.api.util.DirectionList;
import ic2.core.platform.rendering.IC2Models;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class TrapDoorModel extends BaseModel {
   private static final ResourceLocation TRAPDOOR = new ResourceLocation("ic2:trapdoor");
   private static final ResourceLocation TRAPDOOR_LOCATION = new ResourceLocation("models/block/thin_block");
   private static final ShapeBuilder TOP = new ShapeBuilder()
      .newQuad(0.0, 13.0, 0.0, 16.0, 16.0, 16.0)
      .addFace(Direction.UP, 0.0F, 0.0F, 16.0F, 16.0F)
      .addFace(Direction.DOWN, 0.0F, 16.0F, 16.0F, 0.0F)
      .addFaces(DirectionList.HORIZONTAL, 16.0F, 16.0F, 0.0F, 13.0F)
      .finish();
   private static final ShapeBuilder BOTTOM = new ShapeBuilder()
      .newQuad(0.0, 0.0, 0.0, 16.0, 3.0, 16.0)
      .addFace(Direction.UP, 0.0F, 0.0F, 16.0F, 16.0F)
      .addFace(Direction.DOWN, 0.0F, 16.0F, 16.0F, 0.0F)
      .addFaces(DirectionList.HORIZONTAL, 16.0F, 16.0F, 0.0F, 13.0F)
      .finish();
   private BlockState state;
   private String textureName;
   private String textureFolder;
   private List<BakedQuad> quads = new ArrayList<>();

   public TrapDoorModel(BlockState state, String textureFolder, String textureName) {
      this.state = state;
      this.textureFolder = textureFolder;
      this.textureName = textureName;
   }

   @Override
   public void init() {
      Map<String, TextureAtlasSprite> textureMap = IC2Textures.getMappedEntriesBlockIC2(this.textureFolder);
      this.setTransforms(IC2Models.getTransformMap(IC2Models.BLOCK_LOCATION, TRAPDOOR, TRAPDOOR_LOCATION));
      this.setParticleTexture(textureMap.get(this.textureName));
      boolean open = (Boolean)this.state.getValue(TrapDoorBlock.OPEN);
      Half half = (Half)this.state.getValue(TrapDoorBlock.HALF);
      (half == Half.BOTTOM ? BOTTOM : TOP)
         .buildQuads(
            this.getParticleIcon(),
            BlockModelRotation.by(open ? (half == Half.BOTTOM ? 90 : -90) : 0, (((Direction)this.state.getValue(DoorBlock.FACING)).get2DDataValue() + 2) * 90),
            null,
            true,
            this.quads
         );
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType type) {
      return side != null ? empty() : this.quads;
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
