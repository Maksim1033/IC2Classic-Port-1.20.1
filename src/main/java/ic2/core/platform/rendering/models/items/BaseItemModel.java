package ic2.core.platform.rendering.models.items;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.platform.rendering.features.item.IColoredItemModel;
import ic2.core.platform.rendering.models.BaseModel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public abstract class BaseItemModel<T extends BaseItemModel<T>> extends BaseModel {
   protected T other;
   public List<BakedQuad> quads = new ArrayList<>();

   protected void initOther(T other) {
      this.other = other;
      other.setTransforms(this.getTransformMap());
      other.setParticleTexture(this.getParticleIcon());

      for (BakedQuad quad : this.quads) {
         if (quad.getDirection() == Direction.SOUTH) {
            other.quads.add(quad);
         }
      }
   }

   public void syncQuads() {
      if (this.other != null) {
         this.other.quads.clear();

         for (BakedQuad quad : this.quads) {
            if (quad.getDirection() == Direction.SOUTH) {
               this.other.quads.add(quad);
            }
         }
      }
   }

   @Override
   public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType type) {
      return side == null ? this.quads : empty();
   }

   @Override
   public BakedModel applyTransform(ItemDisplayContext ItemDisplayContext, PoseStack poseStack, boolean applyLeftHandTransform) {
      return ItemDisplayContext == ItemDisplayContext.GUI && this.other != null
         ? this.other.applyTransform(ItemDisplayContext, poseStack, applyLeftHandTransform)
         : super.applyTransform(ItemDisplayContext, poseStack, applyLeftHandTransform);
   }

   @Override
   public boolean isGui3d() {
      return false;
   }

   protected int getTintedIndex(ItemStack stack, int layer) {
      return stack.getItem() instanceof IColoredItemModel ? ((IColoredItemModel)stack.getItem()).getTintedIndexForModel(stack, layer) : -1;
   }
}
