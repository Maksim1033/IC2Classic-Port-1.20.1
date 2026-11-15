package ic2.core.block.machines.components.ev;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.machines.components.base.BaseCropLibraryComponent;
import ic2.core.block.machines.tiles.ev.UUCropLibraryTileEntity;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UUCropLibraryComponent extends BaseCropLibraryComponent {
   public static final Box2i UU_BOX = new Box2i(141, 112, 7, 16);
   public static final int COLOR = ColorUtils.rgb(219, 0, 205);

   public UUCropLibraryComponent(UUCropLibraryTileEntity tile) {
      super(tile);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      super.drawBackground(matrix, mouseX, mouseY, partialTicks);
      float pixels = (float)UU_BOX.getHeight() * ((float)((UUCropLibraryTileEntity)this.tile).uu_matter / 512.0F);
      if (pixels > 0.0F) {
         this.gui
            .drawColoredRegion(
               matrix,
               (float)(this.gui.getGuiLeft() + UU_BOX.getX()),
               (float)(this.gui.getGuiTop() + UU_BOX.getY()) + ((float)UU_BOX.getHeight() - pixels),
               (float)UU_BOX.getWidth(),
               pixels,
               COLOR
            );
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      super.addTooltips(matrix, mouseX, mouseY, tooltips);
      if (UU_BOX.isInBox(mouseX, mouseY)) {
         tooltips.accept(this.translate("gui.ic2.uu_crop_library.stored", new Object[]{((UUCropLibraryTileEntity)this.tile).uu_matter}));
      }
   }
}
