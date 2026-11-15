package ic2.core.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.entity.rockets.MiningRocketEntity;
import ic2.core.platform.registries.IC2Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class RocketMinerRenderer extends EntityRenderer<MiningRocketEntity> {
   public RocketMinerRenderer(Context context) {
      super(context);
   }

   public ResourceLocation getTextureLocation(MiningRocketEntity entity) {
      return InventoryMenu.BLOCK_ATLAS;
   }

   public void render(MiningRocketEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
      matrixStackIn.pushPose();
      matrixStackIn.translate(-0.5, 0.0, -0.5);
      BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
      BlockState state = IC2Blocks.ROCKET_MINER.defaultBlockState();
      BlockPos pos = BlockPos.containing(entityIn.getX(), entityIn.getBoundingBox().maxY, entityIn.getZ());
      dispatcher.getModelRenderer()
         .tesselateBlock(
            entityIn.level(),
            dispatcher.getBlockModel(state),
            state,
            pos,
            matrixStackIn,
            bufferIn.getBuffer(RenderType.solid()),
            false,
            RandomSource.create(),
            state.getSeed(entityIn.blockPosition()),
            OverlayTexture.NO_OVERLAY,
            ModelData.EMPTY,
            null
         );
      matrixStackIn.popPose();
   }
}
