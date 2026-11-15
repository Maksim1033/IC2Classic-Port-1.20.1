package ic2.core.entity.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.items.armor.IEnergyShieldArmor;
import ic2.core.platform.rendering.misc.IC2RenderTypes;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class EnergyShieldLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
   protected static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/models/misc/electric_shield_texture.png");
   HumanoidModel<T> model;

   public EnergyShieldLayer(RenderLayerParent<T, M> entityRendererIn, HumanoidModel<T> model) {
      super(entityRendererIn);
      this.model = model;
   }

   public void render(
      PoseStack matrix,
      MultiBufferSource bufferIn,
      int packedLightIn,
      T entityIn,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      if (IEnergyShieldArmor.addsEnergyShieldEffect(entityIn)) {
         boolean always = IEnergyShieldArmor.shouldAlwaysShowEffect(entityIn);
         if (entityIn.hurtTime != 0 || always) {
            float f = (float)entityIn.tickCount * 0.015F;
            int time = always ? 0 : Math.abs(entityIn.hurtTime);
            float visibility = time == 0 ? 1.0F : (time >= 8 ? 1.0F : (float)time / 7.0F);
            float r = 0.245F * visibility;
            float g = 0.708F * visibility;
            float b = 0.803F * visibility;
            ((HumanoidModel)this.getParentModel()).copyPropertiesTo(this.model);
            this.model.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTicks);
            this.model.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            packedLightIn = 4194368;

            for (int i = 0; i < 3; i++) {
               RenderType type = IC2RenderTypes.getShieldType(TEXTURE, 2.0F, (float)i, f);
               this.model.renderToBuffer(matrix, bufferIn.getBuffer(type), packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
               if (bufferIn instanceof BufferSource bufferInfo) {
                  RenderSystem.setShaderColor(r, g, b, 1.0F);
                  bufferInfo.endBatch(type);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               }
            }
         }
      }
   }
}
