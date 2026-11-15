package ic2.core.item.inv.components;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.block.rendering.world.impl.BlockHighlighter;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconCheckBox;
import ic2.core.inventory.gui.components.simple.SliderComponent;
import ic2.core.item.inv.inventory.EUReaderInventory;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EUReaderComponent extends GuiWidget {
   public static final int BORDER_COLOR = 0;
   public static final int BACKGROUND_COLOR = ColorUtils.DARK_GRAY;
   public static final int PROGRESS_COLOR = ColorUtils.BLUE;
   public static final int TEXT_COLOR = ColorUtils.darker(-1);
   public static final Box2i LEFT_SLIDER = new Box2i(104, 32, 12, 156);
   public static final Box2i RIGHT_SLIDER = new Box2i(225, 32, 12, 156);
   public static final Box2i TEXTURE = new Box2i(0, 241, 12, 15);
   boolean mergeSources = false;
   boolean mergeSinks = false;
   EUReaderInventory inventory;
   SliderComponent leftSlider = this.addChild(new SliderComponent(LEFT_SLIDER, TEXTURE).setNonEmptyRows(5));
   SliderComponent rightSlider = this.addChild(new SliderComponent(RIGHT_SLIDER, TEXTURE).setNonEmptyRows(5));

   public EUReaderComponent(EUReaderInventory inventory) {
      super(Box2i.EMPTY_BOX);
      this.inventory = inventory;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      requests.add(GuiWidget.ActionRequest.MOUSE_SCROLL);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @Override
   public boolean isMouseOver(int x, int y) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(
         new IconCheckBox(x + 5, y + 190, 14, 14, this.string(), this.mergeSources)
            .setToolTip("Merge Sources")
            .setListener(T -> this.mergeSources = !this.mergeSources)
      );
      gui.addRenderableWidget(
         new IconCheckBox(x + 126, y + 190, 14, 14, this.string(), this.mergeSinks)
            .setToolTip("Merge Sinks")
            .setListener(T -> this.mergeSinks = !this.mergeSinks)
      );
   }

   protected List<EUReaderInventory.SourceInfo> mergeSources() {
      if (!this.mergeSources) {
         return this.inventory.sortedEmitters;
      } else {
         Map<Item, List<EUReaderInventory.SourceInfo>> infos = CollectionUtils.<Item, List<EUReaderInventory.SourceInfo>>createLinkedMap();
         int i = 0;

         for (int m = this.inventory.sortedEmitters.size(); i < m; i++) {
            EUReaderInventory.SourceInfo info = this.inventory.sortedEmitters.get(i);
            List<EUReaderInventory.SourceInfo> entry = infos.computeIfAbsent(info.item.getItem(), k -> new ObjectArrayList());
            entry.add(info);
         }

         List<EUReaderInventory.SourceInfo> compactedInfo = CollectionUtils.<EUReaderInventory.SourceInfo>createList();

         for (List<EUReaderInventory.SourceInfo> info : infos.values()) {
            compactedInfo.add(new EUReaderInventory.SourceInfo(info));
         }

         return compactedInfo;
      }
   }

   protected List<EUReaderInventory.SinkInfo> mergeSinks() {
      if (!this.mergeSinks) {
         return this.inventory.sortedReceivers;
      } else {
         Map<Item, List<EUReaderInventory.SinkInfo>> infos = CollectionUtils.<Item, List<EUReaderInventory.SinkInfo>>createLinkedMap();
         int i = 0;

         for (int m = this.inventory.sortedReceivers.size(); i < m; i++) {
            EUReaderInventory.SinkInfo info = this.inventory.sortedReceivers.get(i);
            List<EUReaderInventory.SinkInfo> entry = infos.computeIfAbsent(info.item.getItem(), k -> new ObjectArrayList());
            entry.add(info);
         }

         List<EUReaderInventory.SinkInfo> compactedInfo = CollectionUtils.<EUReaderInventory.SinkInfo>createList();

         for (List<EUReaderInventory.SinkInfo> info : infos.values()) {
            compactedInfo.add(new EUReaderInventory.SinkInfo(info));
         }

         return compactedInfo;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      Lighting.setupForFlatItems();
      List<EUReaderInventory.SourceInfo> infos = this.mergeSources();
      List<EUReaderInventory.SinkInfo> receivers = this.mergeSinks();
      this.leftSlider.setMax(infos.size());
      this.rightSlider.setMax(receivers.size());
      int current = this.leftSlider.getCurrent();
      Lighting.setupFor3DItems();

      for (int i = 0; i < 5 && i + current < infos.size(); i++) {
         int offset = 31 * i + 10;
         EUReaderInventory.SourceInfo info = infos.get(i + current);
         double amount = Math.min(1.0, info.getEnergy() / (double)info.output);
         this.gui.drawItemStack(matrix, (float)(this.gui.getGuiLeft() + 8), (float)(this.gui.getGuiTop() + 30 + offset), info.item);
         this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 30), (float)(this.gui.getGuiTop() + 44 + offset), 60.0F, 9.0F, 0);
         this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 31), (float)(this.gui.getGuiTop() + 45 + offset), 58.0F, 7.0F, BACKGROUND_COLOR);
         this.gui
            .drawColoredRegion(
               matrix, (float)(this.gui.getGuiLeft() + 31), (float)(this.gui.getGuiTop() + 45 + offset), (float)((int)(58.0 * amount)), 7.0F, PROGRESS_COLOR
            );
         amount = info.maxEU == 0 ? 0.0 : Math.min(1.0, (double)info.storedEU / (double)info.maxEU);
         this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 30), (float)(this.gui.getGuiTop() + 34 + offset), 60.0F, 9.0F, 0);
         this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 31), (float)(this.gui.getGuiTop() + 35 + offset), 58.0F, 7.0F, BACKGROUND_COLOR);
         this.gui
            .drawColoredRegion(
               matrix, (float)(this.gui.getGuiLeft() + 31), (float)(this.gui.getGuiTop() + 35 + offset), (float)((int)(58.0 * amount)), 7.0F, PROGRESS_COLOR
            );
         if (i != 4) {
            this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 5), (float)(this.gui.getGuiTop() + 54 + offset), 96.0F, 1.0F, -13158601);
         }
      }

      current = this.rightSlider.getCurrent();

      for (int ix = 0; ix < 5 && ix + current < receivers.size(); ix++) {
         int offset = 31 * ix + 10;
         EUReaderInventory.SinkInfo info = receivers.get(ix + current);
         double amount = Mth.clamp(info.getEnergy() / (double)((long)info.max * info.getPackets()), 0.0, 1.0);
         this.gui.drawItemStack(matrix, (float)(this.gui.getGuiLeft() + 129), (float)(this.gui.getGuiTop() + 30 + offset), info.item);
         this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 148), (float)(this.gui.getGuiTop() + 44 + offset), 65.0F, 9.0F, 0);
         this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 149), (float)(this.gui.getGuiTop() + 45 + offset), 63.0F, 7.0F, BACKGROUND_COLOR);
         this.gui
            .drawColoredRegion(
               matrix, (float)(this.gui.getGuiLeft() + 149), (float)(this.gui.getGuiTop() + 45 + offset), (float)((int)(63.0 * amount)), 7.0F, PROGRESS_COLOR
            );
         amount = info.maxEU == 0 ? 0.0 : Math.min(1.0, (double)info.storedEU / (double)info.maxEU);
         this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 148), (float)(this.gui.getGuiTop() + 34 + offset), 65.0F, 9.0F, 0);
         this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 149), (float)(this.gui.getGuiTop() + 35 + offset), 63.0F, 7.0F, BACKGROUND_COLOR);
         this.gui
            .drawColoredRegion(
               matrix, (float)(this.gui.getGuiLeft() + 149), (float)(this.gui.getGuiTop() + 35 + offset), (float)((int)(63.0 * amount)), 7.0F, PROGRESS_COLOR
            );
         if (ix != 4) {
            this.gui.drawColoredRegion(matrix, (float)(this.gui.getGuiLeft() + 127), (float)(this.gui.getGuiTop() + 54 + offset), 95.0F, 1.0F, -13158601);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      DecimalFormat format = ItemStack.ATTRIBUTE_MODIFIER_FORMAT;
      List<EUReaderInventory.SourceInfo> infos = this.mergeSources();
      this.gui.drawRightString(matrix, this.string(this.leftSlider.getCurrent() + "/" + Math.max(0, infos.size() - 5)), 117, 22, 4210752);
      this.gui.drawString(matrix, this.translate("gui.ic2.eu_reader.emitter"), 5, 22, 4210752);
      int current = this.leftSlider.getCurrent();

      for (int i = 0; i < 5 && i + current < infos.size(); i++) {
         EUReaderInventory.SourceInfo info = infos.get(i + current);
         int offset = 31 * i + 10;
         this.gui.drawCenterString(matrix, this.translate("misc.ic2.eu", new Object[]{format.format((long)info.output)}), 62, 25 + offset, 4210752);
         this.gui.drawCenterString(matrix, this.translate("misc.ic2.eu", new Object[]{format.format((long)info.storedEU)}), 62, 35 + offset, TEXT_COLOR);
         this.gui.drawCenterString(matrix, this.translate("misc.ic2.eu", new Object[]{format.format(info.getEnergy())}), 62, 45 + offset, TEXT_COLOR);
      }

      List<EUReaderInventory.SinkInfo> receivers = this.mergeSinks();
      this.gui.drawRightString(matrix, this.string(this.rightSlider.getCurrent() + "/" + Math.max(0, receivers.size() - 5)), 237, 22, 4210752);
      this.gui.drawString(matrix, this.translate("gui.ic2.eu_reader.receiver"), 126, 22, 4210752);
      current = this.rightSlider.getCurrent();

      for (int i = 0; i < 5 && i + current < receivers.size(); i++) {
         EUReaderInventory.SinkInfo info = receivers.get(i + current);
         int offset = 31 * i + 10;
         this.gui.drawCenterString(matrix, this.translate("misc.ic2.eu", new Object[]{format.format((long)info.max)}), 180, 25 + offset, 4210752);
         this.gui.drawCenterString(matrix, this.translate("misc.ic2.eu", new Object[]{format.format((long)info.storedEU)}), 180, 35 + offset, TEXT_COLOR);
         this.gui.drawCenterString(matrix, this.translate("misc.ic2.eu", new Object[]{format.format(info.getEnergy())}), 181, 45 + offset, TEXT_COLOR);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      DecimalFormat format = ItemStack.ATTRIBUTE_MODIFIER_FORMAT;
      List<EUReaderInventory.SourceInfo> infos = this.mergeSources();
      int current = this.leftSlider.getCurrent();

      for (int i = 0; i < 5 && i + current < infos.size(); i++) {
         int offset = 31 * i + 10;
         EUReaderInventory.SourceInfo info = infos.get(i + current);
         int minX = 8;
         int minY = 30 + offset;
         if (mouseX >= minX && mouseX <= minX + 16 && mouseY >= minY && mouseY <= minY + 16) {
            tooltips.accept(info.name);
            if (!this.mergeSources) {
               tooltips.accept(this.translate("gui.ic2.eu_reader.position", new Object[]{info.position.getX(), info.position.getY(), info.position.getZ()}));
            }

            tooltips.accept(this.translate("gui.ic2.eu_reader.power.out", new Object[]{format.format(info.getEnergy()), info.output}));
            tooltips.accept(this.translate("gui.ic2.eu_reader.power.stored", new Object[]{format.format((long)info.storedEU), format.format((long)info.maxEU)}));
            tooltips.accept(this.translate("gui.ic2.eu_reader.tier", new Object[]{info.tier, info.max}));
            tooltips.accept(this.translate("gui.ic2.eu_reader.packets.send", new Object[]{info.getPackets()}));
            if (!this.mergeSources) {
               tooltips.accept(this.translate("gui.ic2.eu_reader.highlight"));
            }
         }
      }

      current = this.rightSlider.getCurrent();
      List<EUReaderInventory.SinkInfo> receivers = this.mergeSinks();

      for (int ix = 0; ix < 5 && ix + current < receivers.size(); ix++) {
         int offset = 31 * ix + 10;
         EUReaderInventory.SinkInfo info = receivers.get(ix + current);
         int minX = 129;
         int minY = 30 + offset;
         if (mouseX >= minX && mouseX <= minX + 16 && mouseY >= minY && mouseY <= minY + 16) {
            tooltips.accept(info.name);
            if (!this.mergeSinks) {
               tooltips.accept(this.translate("gui.ic2.eu_reader.position", new Object[]{info.position.getX(), info.position.getY(), info.position.getZ()}));
            }

            tooltips.accept(this.translate("gui.ic2.eu_reader.power.in", new Object[]{format.format(info.getEnergy())}));
            tooltips.accept(this.translate("gui.ic2.eu_reader.power.stored", new Object[]{format.format((long)info.storedEU), format.format((long)info.maxEU)}));
            tooltips.accept(this.translate("gui.ic2.eu_reader.tier", new Object[]{info.tier, info.max}));
            tooltips.accept(this.translate("gui.ic2.eu_reader.packets.received", new Object[]{info.getPackets()}));
            if (!this.mergeSinks) {
               tooltips.accept(this.translate("gui.ic2.eu_reader.highlight"));
            }
         }
      }
   }

   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (!this.mergeSources) {
         List<EUReaderInventory.SourceInfo> infos = this.inventory.sortedEmitters;
         int current = this.leftSlider.getCurrent();

         for (int i = 0; i < 5 && i + current < infos.size(); i++) {
            int minX = 8;
            int minY = 40 + 31 * i;
            if (mouseX >= minX && mouseX <= minX + 16 && mouseY >= minY && mouseY <= minY + 16) {
               EUReaderInventory.SourceInfo info = infos.get(i + current);
               BlockHighlighter.INSTANCE.addHighlight(info.position);
               this.gui.getMinecraft().player.closeContainer();
               return true;
            }
         }
      }

      if (!this.mergeSinks) {
         List<EUReaderInventory.SinkInfo> receivers = this.inventory.sortedReceivers;
         int current = this.rightSlider.getCurrent();

         for (int ix = 0; ix < 5 && ix + current < receivers.size(); ix++) {
            int minX = 129;
            int minY = 40 + 31 * ix;
            if (mouseX >= minX && mouseX <= minX + 16 && mouseY >= minY && mouseY <= minY + 16) {
               EUReaderInventory.SinkInfo info = receivers.get(ix + current);
               BlockHighlighter.INSTANCE.addHighlight(info.position);
               this.gui.getMinecraft().player.closeContainer();
               return true;
            }
         }
      }

      return false;
   }
}
