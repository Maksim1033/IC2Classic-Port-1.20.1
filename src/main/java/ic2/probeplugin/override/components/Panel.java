package ic2.probeplugin.override.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.probeplugin.override.IExpandedProbeInfo;
import ic2.probeplugin.override.styles.ILayoutStyleBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.ILayoutStyle;
import mcjty.theoneprobe.apiimpl.ProbeInfo;
import mcjty.theoneprobe.rendering.RenderHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Panel implements IExpandedProbeInfo, IElement {
   public static final ResourceLocation ELEMENT_ID = new ResourceLocation("ic2", "panel");
   List<IElement> children = new ObjectArrayList();
   protected ElementAlignment horizontal;
   protected ElementAlignment vertical;
   protected ILayoutStyle layout;
   protected Integer borderColor;
   protected int spacing;

   public Panel(Panel.Type type) {
      this(ILayoutStyleBuilder.create(), type);
   }

   public Panel(ILayoutStyle layout, Panel.Type type) {
      this(layout, type == Panel.Type.VERTICAL);
   }

   public Panel(boolean vertical) {
      this(ILayoutStyleBuilder.create(), vertical);
   }

   public Panel(ILayoutStyle layout, boolean vertical) {
      if (vertical) {
         this.vertical = layout.getAlignment();
      } else {
         this.horizontal = layout.getAlignment();
      }

      this.layout = layout;
      this.borderColor = layout.getBorderColor();
      this.spacing = layout.getSpacing();
   }

   public Panel(FriendlyByteBuf buffer) {
      if (buffer.readBoolean()) {
         this.vertical = (ElementAlignment)buffer.readEnum(ElementAlignment.class);
      } else {
         this.horizontal = (ElementAlignment)buffer.readEnum(ElementAlignment.class);
      }

      if (buffer.readBoolean()) {
         this.borderColor = buffer.readInt();
      }

      this.spacing = buffer.readInt();
      this.layout = ILayoutStyleBuilder.create()
         .topPadding(buffer.readInt())
         .bottomPadding(buffer.readInt())
         .leftPadding(buffer.readInt())
         .rightPadding(buffer.readInt());
      this.children.addAll(ProbeInfo.createElements(buffer));
   }

   public ILayoutStyle getLayout() {
      return this.layout;
   }

   public void toBytes(FriendlyByteBuf buffer) {
      buffer.writeBoolean(this.vertical != null);
      buffer.writeEnum(this.vertical != null ? this.vertical : this.horizontal);
      buffer.writeBoolean(this.borderColor != null);
      if (this.borderColor != null) {
         buffer.writeInt(this.borderColor);
      }

      buffer.writeInt(this.spacing)
         .writeInt(this.layout.getTopPadding())
         .writeInt(this.layout.getBottomPadding())
         .writeInt(this.layout.getLeftPadding())
         .writeInt(this.layout.getRightPadding());
      ProbeInfo.writeElements(this.children, buffer);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void render(GuiGraphics matrix, int x, int y) {
      if (this.borderColor != null) {
         int w = this.getWidth();
         int h = this.getHeight();
         RenderHelper.drawHorizontalLine(matrix, x, y, x + w - 1, this.borderColor);
         RenderHelper.drawHorizontalLine(matrix, x, y + h - 1, x + w - 1, this.borderColor);
         RenderHelper.drawVerticalLine(matrix, x, y, y + h - 1, this.borderColor);
         RenderHelper.drawVerticalLine(matrix, x + w - 1, y, y + h, this.borderColor);
         x += 3;
         y += 3;
      }

      if (this.horizontal != null) {
         x += this.layout.getLeftPadding();
         int totHeight = this.getHeight() - this.getYPadding();

         for (IElement element : this.children) {
            int h = element.getHeight();

            element.render(matrix, x, switch (this.horizontal) {
               case ALIGN_CENTER -> y + (totHeight - h) / 2;
               case ALIGN_BOTTOMRIGHT -> y + totHeight - h;
               default -> y;
            } + this.layout.getTopPadding());
            x += element.getWidth() + this.spacing;
         }
      } else if (this.vertical != null) {
         y += this.layout.getTopPadding();
         int totWidth = this.getWidth() - this.getXPadding();

         for (IElement element : this.children) {
            int w = element.getWidth();

            element.render(matrix, switch (this.vertical) {
               case ALIGN_CENTER -> x + (totWidth - w) / 2;
               case ALIGN_BOTTOMRIGHT -> x + totWidth - w;
               default -> x;
            } + this.layout.getLeftPadding(), y);
            y += element.getHeight() + this.spacing;
         }
      }
   }

   public int getHeight() {
      int h = 0;
      if (this.vertical != null) {
         for (IElement element : this.children) {
            h += element.getHeight();
         }

         return h + this.spacing * (this.children.size() - 1) + this.getBorderSpacing() + this.getYPadding();
      } else {
         for (IElement element : this.children) {
            h = Math.max(element.getHeight(), h);
         }

         return h + this.getBorderSpacing() + this.getYPadding();
      }
   }

   public int getWidth() {
      int w = 0;
      if (this.horizontal != null) {
         for (IElement element : this.children) {
            w += element.getWidth();
         }

         return w + this.spacing * (this.children.size() - 1) + this.getBorderSpacing() + this.getXPadding();
      } else {
         for (IElement element : this.children) {
            w = Math.max(element.getWidth(), w);
         }

         return w + this.getBorderSpacing() + this.getXPadding();
      }
   }

   protected int getYPadding() {
      return this.layout.getBottomPadding() + this.layout.getTopPadding();
   }

   protected int getXPadding() {
      return this.layout.getLeftPadding() + this.layout.getRightPadding();
   }

   protected int getBorderSpacing() {
      return this.borderColor == null ? 0 : 6;
   }

   public ResourceLocation getID() {
      return ELEMENT_ID;
   }

   @Override
   public IExpandedProbeInfo element(IElement element) {
      this.children.add(element);
      return this;
   }

   @Override
   public List<IElement> getChildren() {
      return this.children;
   }

   public List<IElement> getElements() {
      return this.getChildren();
   }

   public static enum Type {
      VERTICAL,
      HORIZONTAL;
   }
}
