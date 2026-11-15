package ic2.core.item.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.cables.AdvancedComparatorTileEntity;
import ic2.core.block.personal.tile.PersonalChestTileEntity;
import ic2.core.item.renders.entitymodel.ModelShield;
import ic2.core.item.wearable.base.IC2ShieldBase;
import ic2.core.platform.registries.IC2Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IC2TileEntityStackRenderer extends BlockEntityWithoutLevelRenderer {
   public static final IC2TileEntityStackRenderer INSTANCE = new IC2TileEntityStackRenderer();
   private ModelShield shield = new ModelShield();
   private PersonalChestTileEntity personal_chest = new PersonalChestTileEntity(BlockPos.ZERO, IC2Blocks.PERSONAL_CHEST.defaultBlockState());
   private AdvancedComparatorTileEntity comparator = new AdvancedComparatorTileEntity(BlockPos.ZERO, IC2Blocks.ADVANCED_COMPARATOR.defaultBlockState());
   private BlockEntityRenderDispatcher dispatcher;

   public IC2TileEntityStackRenderer() {
      this(Minecraft.getInstance());
   }

   public IC2TileEntityStackRenderer(Minecraft mc) {
      super(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
      this.dispatcher = mc.getBlockEntityRenderDispatcher();
      this.personal_chest.setFacingSilent(Direction.NORTH);
   }

   public void renderByItem(ItemStack stack, ItemDisplayContext p_239207_2_, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
      if (stack.getItem() instanceof IC2ShieldBase) {
         ResourceLocation location = ((IC2ShieldBase)stack.getItem()).getTexture(stack);
         matrixStack.pushPose();
         matrixStack.scale(1.0F, -1.0F, -1.0F);
         this.shield.render(matrixStack, buffer.getBuffer(RenderType.entityCutout(location)), combinedLight, combinedOverlay);
         matrixStack.popPose();
      } else {
         Item item = stack.getItem();
         if (item != Items.AIR) {
            if (item == IC2Blocks.PERSONAL_CHEST.asItem()) {
               this.dispatcher.renderItem(this.personal_chest, matrixStack, buffer, combinedLight, combinedOverlay);
               return;
            }

            if (item == IC2Blocks.ADVANCED_COMPARATOR.asItem()) {
               this.dispatcher.renderItem(this.comparator, matrixStack, buffer, combinedLight, combinedOverlay);
               return;
            }
         }

         super.renderByItem(stack, p_239207_2_, matrixStack, buffer, combinedLight, combinedOverlay);
      }
   }
}
