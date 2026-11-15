package ic2.core.wiki.components;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ic2.core.block.rendering.world.impl.MultiBlock;
import ic2.core.block.rendering.world.impl.MultiBlockOverlay;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.base.TexturedButton;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.item.inv.container.WikiBoxContainer;
import ic2.core.utils.math.SmootherFloat;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.wiki.base.managers.IWikiProvider;
import ic2.core.wiki.gui.BookMarkComponent;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class MultiBlockComponent extends BaseWikiComponent {
   SmootherFloat xRotation = new SmootherFloat(30.0F, 0.1F);
   SmootherFloat yRotation = new SmootherFloat(45.0F, 0.1F);
   SmootherFloat scale = new SmootherFloat(1.0F, 0.1F);
   float lastMouseX = 0.0F;
   float lastMouseY = 0.0F;
   float initialScale;
   int yLimit = 0;
   boolean dragging = false;
   boolean disablePlacement;
   MultiBlock block;
   Button button;
   ToolTipButton down = new TexturedButton(0, 0, 16, 16, WikiBoxContainer.TEXTURE, new Box2i(50, 192, 12, 16), T -> this.yLimit++).setToolTip("Remove Layer");
   ToolTipButton up = new TexturedButton(0, 0, 16, 16, WikiBoxContainer.TEXTURE, new Box2i(34, 192, 12, 16), T -> this.yLimit--).setToolTip("Add Layer");
   ToolTipButton reset = new TexturedButton(0, 0, 16, 16, WikiBoxContainer.TEXTURE, new Box2i(117, 219, 16, 16), T -> {
      this.yLimit = 0;
      this.scale.setTarget(this.initialScale);
      this.yRotation.setTarget(45.0F);
      this.xRotation.setTarget(30.0F);
   }).setFull().setToolTip("Reset");
   ItemCheckBox rotate;
   ItemCheckBox formed;

   public MultiBlockComponent(MultiBlock block, boolean disablePlacement) {
      super(disablePlacement ? 109 : 119);
      this.block = block;
      this.disablePlacement = disablePlacement;
      AABB axis = block.getBounds().inflate(0.5);
      int sizeX = Mth.floor(axis.getXsize());
      int sizeZ = Mth.floor(axis.getZsize());
      float maxX = 85.0F;
      float maxY = 85.0F;
      double baseScale = Math.sqrt((double)(sizeX * sizeX + sizeZ * sizeZ));
      this.scale.setTarget((float)(-Math.min((double)maxX / baseScale, (double)(maxY / (float)Mth.floor(axis.getYsize())))));
      this.scale.forceFinish();
      this.initialScale = this.scale.getTarget();
   }

   @OnlyIn(Dist.CLIENT)
   private void render(IC2Screen screen, PoseStack stack, Button button, int mouseX, int mouseY, int x, int y, float partialTicks) {
      button.setX(x);
      button.setY(y);
      button.render(screen.guiGraphics != null ? screen.guiGraphics : new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource()), mouseX + screen.getGuiLeft(), mouseY + screen.getGuiTop(), partialTicks);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void renderBackground(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
      if (this.button == null) {
         this.button = new ExtendedButton(0, 0, 100, 14, this.translate("tooltip.ic2.multiblock.show"), T -> {
            MultiBlockOverlay.INSTANCE.setSelectedMultiBlock(this.block.copy());
            Minecraft.getInstance().screen.onClose();
         });
         this.button.visible = !this.disablePlacement;
         this.rotate = new ItemCheckBox(0, 0, 16, 16, null, new ItemStack(Items.CLOCK), false).setToolTip("Rotate");
         this.formed = new ItemCheckBox(0, 0, 16, 16, null, new ItemStack(Items.LEVER), true).setToolTip("Formed");
      }

      this.button.active = !MultiBlockOverlay.INSTANCE.hasSelectedMultiBlock();
      this.down.active = (double)this.yLimit < this.block.getBounds().getYsize() - 1.0;
      this.up.active = this.yLimit > 0;
      this.render(screen, stack, this.button, mouseX, mouseY, x + 8, y + 105, partialTicks);
      this.render(screen, stack, this.down, mouseX, mouseY, x + 8, y + 88, partialTicks);
      this.render(screen, stack, this.up, mouseX, mouseY, x + 25, y + 88, partialTicks);
      this.render(screen, stack, this.reset, mouseX, mouseY, x + 42, y + 88, partialTicks);
      this.render(screen, stack, this.rotate, mouseX, mouseY, x + 59, y + 88, partialTicks);
      this.render(screen, stack, this.formed, mouseX, mouseY, x + 76, y + 88, partialTicks);
      screen.bindTexture(BookMarkComponent.TEXTURE);
      screen.drawTextureRegion(stack, (float)x, (float)y, 0.0F, 94.0F, 118.0F, 85.0F, 74.0F, 1.0F);
      stack.pushPose();
      if (this.rotate.isChecked()) {
         this.yRotation.addTarget(1.0F);
      }

      if (this.dragging) {
         this.xRotation.addTarget((float)mouseY - this.lastMouseY);
         this.yRotation.addTarget((float)mouseX - this.lastMouseX);
         this.lastMouseX = (float)mouseX;
         this.lastMouseY = (float)mouseY;
      }

      this.yRotation.update(partialTicks);
      this.xRotation.update(partialTicks);
      this.scale.update(partialTicks);
      Matrix4f rotMat = new Matrix4f();
      rotMat.identity();
      this.setupMatrix(stack, rotMat, this.xRotation.getValue(), this.yRotation.getValue(), x, y - 8);
      Vector4f eye = new Vector4f(0.0F, 0.0F, -100.0F, 1.0F);
      rotMat.transform(eye);
      eye.div(eye.w);
      BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
      RenderSystem.enableDepthTest();
      RenderSystem.disableCull();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      this.enableScissors(x, y, 118, 85);
      this.block.setYLimit(this.yLimit);
      stack.pushPose();
      this.block
         .renderMultiBlock(
            stack, buffers.getBuffer(RenderType.cutout()), screen.getMinecraft().level, BlockPos.ZERO.offset(0, 512, 0), Rotation.NONE, this.formed.isChecked()
         );
      this.block.setYLimit(0);
      stack.popPose();
      RenderSystem.disableBlend();
      buffers.endBatch();
      RenderSystem.disableDepthTest();
      RenderSystem.enableCull();
      stack.popPose();
   }

   protected final void enableScissors(int x, int y, int width, int height) {
      Window window = Minecraft.getInstance().getWindow();
      double scaledWidth = (double)window.getWidth() / (double)window.getGuiScaledWidth();
      double scaledHeight = (double)window.getHeight() / (double)window.getGuiScaledHeight();
      int bottom = y + height;
      RenderSystem.enableScissor(
         (int)((double)x * scaledWidth),
         (int)((double)window.getHeight() - (double)bottom * scaledHeight),
         (int)((double)width * scaledWidth),
         (int)((double)height * scaledHeight)
      );
   }

   @OnlyIn(Dist.CLIENT)
   public void setupMatrix(PoseStack stack, Matrix4f matrix, float xRotation, float yRotation, int x, int y) {
      AABB axis = this.block.getBounds();
      Vec3 offset = this.block.getOffset();
      int sizeX = Mth.floor(axis.getXsize());
      int sizeY = Mth.floor(axis.getYsize());
      boolean odd = (sizeX & 1) == 1;
      float scale = Math.min(-1.0F, this.scale.getValue());
      stack.translate(47.0, (double)(sizeY * 20), 100.0);
      stack.translate(
         (double)x + offset.x() + (double)(45.0F - (float)Math.min(sizeX, 3) * 15.0F), (double)y + offset.y() + (60.0 - axis.getYsize() * 20.0), offset.z()
      );
      stack.scale(scale, scale, scale);
      stack.translate((double)(-sizeX / 2), (double)(-sizeY / 2), 0.0);
      float offsetX = odd ? 0.5F : 0.0F;
      float offsetZ = odd ? 0.5F : 1.0F;
      stack.translate(0.0, (double)((float)sizeY * 0.5F - 0.5F), 0.0);
      stack.mulPose(Axis.XP.rotationDegrees(-xRotation));
      matrix.rotateX((float)Math.toRadians(xRotation));
      stack.translate((double)offsetX, (double)((float)sizeY * -0.5F + 0.5F), (double)offsetZ);
      stack.mulPose(Axis.YP.rotationDegrees(yRotation));
      matrix.rotateY((float)Math.toRadians(-yRotation));
      stack.translate((double)(-offsetX), 0.0, (double)(-offsetZ));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(IC2Screen screen, int x, int y, int mouseX, int mouseY, IWikiProvider provider) {
      int xOff = screen.getGuiLeft();
      int yOff = screen.getGuiTop();
      if (this.button != null && this.button.mouseClicked((double)(mouseX + xOff), (double)(mouseY + yOff), 0)) {
         return true;
      } else if (this.down.mouseClicked((double)(mouseX + xOff), (double)(mouseY + yOff), 0)
         || this.up.mouseClicked((double)(mouseX + xOff), (double)(mouseY + yOff), 0)
         || this.reset.mouseClicked((double)(mouseX + xOff), (double)(mouseY + yOff), 0)
         || this.rotate.mouseClicked((double)(mouseX + xOff), (double)(mouseY + yOff), 0)
         || this.formed.mouseClicked((double)(mouseX + xOff), (double)(mouseY + yOff), 0)) {
         return true;
      } else if (mouseX >= x && mouseX < x + 118 && mouseY >= y && mouseY <= y + 85) {
         this.lastMouseX = (float)mouseX;
         this.lastMouseY = (float)mouseY;
         this.dragging = true;
         return true;
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addToolTips(IC2Screen screen, int x, int y, PoseStack stack, int mouseX, int mouseY, Consumer<Component> tooltips) {
      this.down.addToolTip(screen, mouseX, mouseY, tooltips);
      this.up.addToolTip(screen, mouseX, mouseY, tooltips);
      this.reset.addToolTip(screen, mouseX, mouseY, tooltips);
      this.rotate.addToolTip(screen, mouseX, mouseY, tooltips);
      this.formed.addToolTip(screen, mouseX, mouseY, tooltips);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(IC2Screen screen, int x, int y, int mouseX, int mouseY, int scroll, IWikiProvider provider) {
      if (mouseX >= x && mouseX < x + 118 && mouseY >= y && mouseY <= y + 85) {
         this.scale.setTarget(Math.min(-1.0F, this.scale.getTarget() + (float)scroll * -1.0F));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean onMouseRelease(IC2Screen screen, int x, int y, int mouseX, int mouseY) {
      if (this.dragging) {
         this.dragging = false;
         return true;
      } else {
         return false;
      }
   }
}
