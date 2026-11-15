package ic2.core.platform.rendering.models;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.rendering.camouflage.shape.CamouflageShape;
import ic2.core.block.rendering.props.ParticleOverride;
import ic2.core.platform.rendering.IC2Models;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public abstract class BaseModel implements BakedModel {
   public static final ChunkRenderTypeSet DEFAULT_SET = ChunkRenderTypeSet.of(new RenderType[]{RenderType.solid()});
   public static final ResourceLocation LOCATION = new ResourceLocation("ic2:internal");
   TextureAtlasSprite particles;
   ItemTransforms transforms;
   ChunkRenderTypeSet renderTypes = DEFAULT_SET;

   public abstract void init();

   public void setTransforms(ItemTransforms transforms) {
      this.transforms = transforms;
   }

   public ItemTransforms getTransformMap() {
      return this.transforms;
   }

   protected void setParticleTexture(TextureAtlasSprite sprite) {
      this.particles = IC2Models.getSafeIcon(sprite);
   }

   public void setRenderTypes(ChunkRenderTypeSet renderTypes) {
      this.renderTypes = renderTypes;
   }

   public ChunkRenderTypeSet getRenderTypes() {
      return this.renderTypes;
   }

   @Override
   public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
      return this.renderTypes != null ? this.renderTypes : BakedModel.super.getRenderTypes(state, rand, data);
   }

   public ItemOverrides getOverrides() {
      return BaseModel.EmptyOverrides.INSTANCE;
   }

   public TextureAtlasSprite getParticleIcon() {
      return this.particles;
   }

   public TextureAtlasSprite getParticleIcon(ModelData data) {
      BlockState state = (BlockState)data.get(ParticleOverride.INSTANCE);
      return state != null ? Minecraft.getInstance().getBlockRenderer().getBlockModel(state).getParticleIcon(ModelData.EMPTY) : BakedModel.super.getParticleIcon(data);
   }

   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {
      return empty();
   }

   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
      return this.getQuads(state, side, rand, ModelData.EMPTY, null);
   }

   public boolean useAmbientOcclusion() {
      return true;
   }

   public boolean isCustomRenderer() {
      return false;
   }

   public boolean isGui3d() {
      return true;
   }

   public boolean usesBlockLight() {
      return false;
   }

   protected boolean isTranslucent(RenderType type) {
      return CamouflageShape.isTranslucent(type);
   }

   protected static <T> List<T> empty() {
      return Collections.emptyList();
   }

   protected static List<BakedQuad>[] multiList(int count) {
      List<BakedQuad>[] array = new List[count];

      for (int i = 0; i < count; i++) {
         array[i] = new ArrayList<>();
      }

      return array;
   }

   public BakedModel applyTransform(ItemDisplayContext ItemDisplayContext, PoseStack poseStack, boolean applyLeftHandTransform) {
      return this.handlePerspective(this, ItemDisplayContext, poseStack, applyLeftHandTransform);
   }

   protected BakedModel handlePerspective(BakedModel model, ItemDisplayContext type, PoseStack stack, boolean leftHandedness) {
      this.transforms.getTransform(type).apply(leftHandedness, stack);
      return model;
   }

   public static class EmptyOverrides extends ItemOverrides {
      public static final BaseModel.EmptyOverrides INSTANCE = new BaseModel.EmptyOverrides();
   }
}
