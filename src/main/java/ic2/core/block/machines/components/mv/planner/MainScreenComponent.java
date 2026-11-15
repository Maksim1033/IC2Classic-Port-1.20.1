package ic2.core.block.machines.components.mv.planner;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.planner.SimulatedStack;
import ic2.core.block.machines.containers.mv.ReactorPlannerContainer;
import ic2.core.block.machines.logic.planner.ComponentInfo;
import ic2.core.block.machines.logic.planner.PlannerRegistry;
import ic2.core.block.machines.logic.planner.encoder.EncoderRegistry;
import ic2.core.block.machines.logic.planner.newLogic.ReactorLogic;
import ic2.core.block.machines.tiles.mv.ReactorPlannerTileEntity;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ToolTipButton;
import ic2.core.inventory.gui.components.simple.SliderComponent;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.networking.buffers.data.StringDataBuffer;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class MainScreenComponent extends GuiWidget {
   ReactorPlannerTileEntity tile;
   SliderComponent slider = this.addChild(new SliderComponent(new Box2i(277, 21, 14, 106), new Box2i(90, 212, 12, 15), 1).setListener(this::setOffset))
      .setNonEmptyRows(5);
   int lastIndex = -1;

   public MainScreenComponent(ReactorPlannerTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      requests.add(GuiWidget.ActionRequest.MOUSE_SCROLL);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      gui.setContainerOffset(-18, 2);
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      gui.addRenderableWidget(0, new ExtendedButton(x + 6, y + 7, 10, 10, this.string("-"), T -> this.tile.sendToServer(1, -1)));
      gui.addRenderableWidget(1, new ExtendedButton(x + 157, y + 7, 10, 10, this.string("+"), T -> this.tile.sendToServer(1, 1)));
      gui.addRenderableWidget(
         2, new ToolTipButton(x + 220, y + 7, 70, 10, this.getName(), T -> this.tile.sendToServer(0, Screen.hasShiftDown() ? 1 : 0)).setToolTip(this.getName())
      );
      ((ToolTipButton)gui.addRenderableWidget(
            3, new ToolTipButton(x + 220, y + 166, 55, 12, this.translate("gui.ic2.reactor_planner.button.import"), T -> this.importSetup())
         ))
         .setToolTip("gui.ic2.reactor_planner.button.import.desc");
      ((ToolTipButton)gui.addRenderableWidget(
            4, new ToolTipButton(x + 220, y + 181, 55, 12, this.translate("gui.ic2.reactor_planner.button.export"), T -> this.exportSetup())
         ))
         .setToolTip("gui.ic2.reactor_planner.button.export.desc");
      ((ToolTipButton)gui.addRenderableWidget(
            5, new ToolTipButton(x + 220, y + 196, 55, 12, this.translate("gui.ic2.reactor_planner.button.export_item"), T -> this.exportToItem())
         ))
         .setToolTip("gui.ic2.reactor_planner.button.export_item.desc");
      this.slider.setCurrent(this.tile.selection.getOffset()).setMax(this.tile.selection.getTotalItems() / 3);
   }

   private Component getName() {
      int index = this.tile.selection.getCurrentIndex();
      return (Component)(index == -1 ? this.translate("gui.ic2.reactor_planner.component.any") : IReactorPlannerComponent.ComponentType.byID(index).getName());
   }

   private void importSetup() {
      Tuple<String, CompoundTag> data = EncoderRegistry.INSTANCE.createDecodedMessage(Minecraft.getInstance().keyboardHandler.getClipboard().trim());
      if (data == null) {
         this.gui.getPlayer().sendSystemMessage(this.string("No Decoder found"));
      } else {
         CompoundTag nbt = new CompoundTag();
         nbt.putString("decoder", (String)data.getA());
         nbt.put("data", (Tag)data.getB());
         this.tile.sendToServer("import", new NBTBuffer(nbt));
      }
   }

   private void exportToItem() {
      this.tile.sendToServer("export_to_item", new StringDataBuffer(3, EncoderRegistry.INSTANCE.getDefaultEncoderID()));
   }

   private void exportSetup() {
      String encoder = EncoderRegistry.INSTANCE.getDefaultEncoderID();
      if (Screen.hasShiftDown()) {
         StringBuilder joiner = new StringBuilder();
         joiner.append("Please Select one of the Following Encoders: ");
         joiner.append(this.buildEmptyRange((240 - this.gui.getFont().width("Please Select one of the Following Encoders: ")) / 4));
         ObjectListIterator result = new ObjectArrayList(EncoderRegistry.INSTANCE.getChoiceList().iterator()).iterator();

         while (result.hasNext()) {
            Tuple<String, String> entry = (Tuple<String, String>)result.next();
            String s = (String)entry.getB() + ": [ " + (String)entry.getA() + " ],";
            joiner.append(s);
            joiner.append(this.buildEmptyRange((290 - this.gui.getFont().width(s)) / 4));
         }

         String resultx = TinyFileDialogs.tinyfd_inputBox("Select Encoder", joiner.toString(), encoder);
         if (EncoderRegistry.INSTANCE.getEncoderFromID(resultx) == null) {
            this.gui.getPlayer().displayClientMessage(this.translate("gui.ic2.reactor_planner.io.no_encoder"), false);
            return;
         }

         encoder = resultx;
      }

      this.tile.sendToServer("export", new StringDataBuffer(1, encoder));
   }

   private String buildEmptyRange(int length) {
      return " ".repeat(Math.max(0, length));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(0).active = this.tile.getReactorSize() > 0;
      gui.getButton(1).active = this.tile.getReactorSize() < 6;
      if (this.tile.selection.getCurrentIndex() != this.lastIndex) {
         this.lastIndex = this.tile.selection.getCurrentIndex();
         Component name = this.getName();
         ((ToolTipButton)gui.getCastedButton(2, ToolTipButton.class)).setToolTip(name).setMessage(name);
      }

      this.slider.setCurrent(this.tile.selection.getOffset()).setMax(this.tile.selection.getTotalItems() / 3);
      gui.getButton(4).active = this.tile.simulation.isValid;
      if (gui.getClock() % 10 == 0) {
         gui.getCastedContainer(ReactorPlannerContainer.class).onSlotSelected();
      }

      gui.getButton(5).active = gui.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).is(IC2Items.MEMORY_STICK);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int remove = this.tile.getReactorSize() * 9;
      int i = 0;

      for (int m = 3 + this.tile.getReactorSize(); i < m; i++) {
         this.drawRow(matrix, 60 + i * 18 - remove, 20, 6, i);
      }

      i = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      this.gui.drawTextureRegion(matrix, (float)(i + 200), (float)y, 117.0F, 0.0F, 95.0F, 212.0F);

      for (int ix = 0; ix < 3; ix++) {
         this.drawRow(matrix, 220 + ix * 18, 20, 6, -1);
      }

      this.gui.drawTextureRegion(matrix, (float)(i + 276), (float)(y + 20), 75.0F, 212.0F, 14.0F, 1.0F);
      this.gui.drawTextureRegion(matrix, (float)(i + 276), (float)(y + 21), 75.0F, 213.0F, 14.0F, 106.0F, 14.0F, 31.0F);
      this.gui.drawTextureRegion(matrix, (float)(i + 276), (float)(y + 127), 75.0F, 244.0F, 14.0F, 1.0F);
      if (this.tile.selectedSlot != -1) {
         int row = this.tile.selectedSlot / 3;
         int offset = this.tile.selection.getOffset();
         if (row >= offset && row < offset + 6) {
            row -= offset;
            int slot = this.tile.selectedSlot % 3;
            this.gui.drawTextureRegion(matrix, (float)(i + 220 + slot * 18), (float)(y + 20 + 18 * row), 0.0F, 230.0F, 18.0F, 18.0F);
         }
      }

      Slot slot = this.gui.getSlotUnderMouse();
      if (slot instanceof ReactorPlannerContainer.SimulationSlot && !((IC2Container)this.gui.getMenu()).getCarried().isEmpty()) {
         ItemStack stack = ((IC2Container)this.gui.getMenu()).getCarried();
         if (stack.getItem() instanceof IReactorPlannerComponent comp) {
            IntSet slots = new IntLinkedOpenHashSet();
            int index = slot.getSlotIndex();
            comp.addAffectedSlots(index % 9, index / 9, (X, Y) -> {
               if (X >= 0 && X < 9 && Y >= 0 && Y < 6) {
                  slots.add(Y * 9 + X);
                  return true;
               } else {
                  return false;
               }
            });

            for (int slotId : slots.toIntArray()) {
               Slot drawSlot = ((IC2Container)this.gui.getMenu()).getSlot(slotId);
               RenderSystem.disableDepthTest();
               this.gui
                  .drawColoredRegion(
                     matrix,
                     (float)(drawSlot.x + this.gui.getGuiLeft()),
                     (float)(drawSlot.y + this.gui.getGuiTop()),
                     16.0F,
                     16.0F,
                     ColorUtils.rgb(255, 255, 0, 150)
                  );
               RenderSystem.enableDepthTest();
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      Slot slot = this.gui.getSlotUnderMouse();
      if (slot != null && slot.hasItem() && ((IC2Container)this.gui.getMenu()).getCarried().isEmpty()) {
         if (slot instanceof ReactorPlannerContainer.SimulationSlot) {
            for (Component comp : slot.getItem()
               .getTooltipLines(this.gui.getPlayer(), this.gui.getMinecraft().options.advancedItemTooltips ? Default.ADVANCED : Default.NORMAL)) {
               tooltips.accept(comp);
            }
         } else {
            tooltips.accept(slot.getItem().getHoverName());
         }

         ItemStack stack = slot.getItem();
         ComponentInfo info = PlannerRegistry.getInfo(stack);
         if (info == null) {
            return;
         }

         if (stack.getItem() instanceof IReactorPlannerComponent comp) {
            comp.addToolTip(stack, tooltips);
         }

         Object2ObjectMap<IReactorPlannerComponent.ReactorStat, NumericTag> stats = this.getStats(info, slot);
         if (stats.isEmpty()) {
            return;
         }

         tooltips.accept(this.string());
         tooltips.accept(this.translate("gui.ic2.reactor_planner.component.stats"));
         StringBuilder builder = new StringBuilder();
         ObjectIterator var10 = stats.object2ObjectEntrySet().iterator();

         while (var10.hasNext()) {
            Entry<IReactorPlannerComponent.ReactorStat, NumericTag> entry = (Entry<IReactorPlannerComponent.ReactorStat, NumericTag>)var10.next();
            tooltips.accept(
               this.string(
                  builder.append(((IReactorPlannerComponent.ReactorStat)entry.getKey()).getName((NumericTag)entry.getValue()).getString())
                     .append(" ")
                     .toString()
               )
            );
            builder.setLength(0);
         }
      }
   }

   private Object2ObjectMap<IReactorPlannerComponent.ReactorStat, NumericTag> getStats(ComponentInfo info, Slot slot) {
      if (slot instanceof ReactorPlannerContainer.SimulationSlot && slot.getItem().getItem() instanceof IReactorPlannerComponent) {
         int index = slot.getSlotIndex();
         int x = index % 9;
         int y = index / 9;
         SimulatedStack stack = this.tile.simulation.getItem(x, y);
         if (stack == null) {
            return Object2ObjectMaps.emptyMap();
         } else {
            Object2ObjectMap<IReactorPlannerComponent.ReactorStat, NumericTag> map = CollectionUtils.<IReactorPlannerComponent.ReactorStat, NumericTag>createLinkedMap();
            ReactorLogic logic = this.tile.simulation;
            int fuelPulse = logic.fuelPulse;
            int breedingPulse = logic.breedingPulse;
            float production = logic.production;
            logic.production = 0.0F;
            logic.fuelPulse = 0;
            logic.breedingPulse = 0;
            ObjectIterator var12 = info.getStats().keySet().iterator();

            while (var12.hasNext()) {
               IReactorPlannerComponent.ReactorStat stat = (IReactorPlannerComponent.ReactorStat)var12.next();
               map.put(stat, stack.getStat(stat, logic, x, y));
            }

            logic.fuelPulse = fuelPulse;
            logic.breedingPulse = breedingPulse;
            logic.production = production;
            return map;
         }
      } else {
         return info.getStats();
      }
   }

   private void setOffset(int value) {
      this.tile.sendToServer(3, value);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      Slot slot = this.gui.getSlotUnderMouse();
      if (slot instanceof LockedSlot && !(slot instanceof ReactorPlannerContainer.SimulationSlot)) {
         this.tile.sendToServer(4, mouseButton == 1 ? -1 : this.tile.selection.getOffset() * 3 + slot.getSlotIndex());
         return true;
      } else if (slot == null && mouseButton == 1 && this.tile.selectedSlot != -1) {
         this.tile.sendToServer(4, -1);
         return true;
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(int mouseX, int mouseY, int scroll) {
      Slot slot = this.gui.getSlotUnderMouse();
      if ((!(slot instanceof LockedSlot) || slot instanceof ReactorPlannerContainer.SimulationSlot) && !this.box.isInBox(mouseX, mouseY)) {
         return false;
      } else {
         this.tile.sendToServer(2, -scroll);
         return true;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void drawRow(PoseStack matrix, int xOffset, int yOffset, int count, int index) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();

      for (int i = 0; i < count; i++) {
         boolean broken = index >= 0 && i * 9 + index == this.tile.simulation.brokenSlot;
         this.gui
            .drawTextureRegion(
               matrix, (float)(x + xOffset), (float)(y + yOffset + 18 * i), 0.0F, 212.0F, 18.0F, 18.0F, 18.0F, 18.0F, broken ? ColorUtils.RED : -1
            );
      }
   }
}
