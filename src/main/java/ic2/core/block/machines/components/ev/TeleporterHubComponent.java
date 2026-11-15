package ic2.core.block.machines.components.ev;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.api.tiles.teleporter.TeleporterTarget;
import ic2.core.block.machines.tiles.luv.TeleporterHubTileEntity;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.IconButton;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.math.geometry.Box2i;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class TeleporterHubComponent extends GuiWidget {
   static final Box2i TELEPORT_BOX = new Box2i(17, 17, 100, 9);
   TeleporterHubTileEntity tile;
   int offset = 0;
   boolean rename;
   int renameIndex;

   public TeleporterHubComponent(TeleporterHubTileEntity tile) {
      super(Box2i.EMPTY_BOX);
      this.tile = tile;
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.TOOLTIP);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
      requests.add(GuiWidget.ActionRequest.MOUSE_SCROLL);
      requests.add(GuiWidget.ActionRequest.KEY_INPUT);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();
      List<Tuple<TeleporterTarget, String>> targets = this.tile.targets.getClientTargets();

      for (int i = 0; i < 8; i++) {
         int index = i;
         gui.addRenderableWidget(
               i,
               new IconButton(x + 7, y + 17 + i * 11, 10, 10, IC2Items.ICON_DISPLAY.create(1), T -> this.delete(index))
                  .setIconOnly()
                  .setToolTip("gui.ic2.teleporter_hub.delete")
            )
            .visible = targets.size() > index;
         gui.addRenderableWidget(
               8 + i, new ExtendedButton(x + 118, y + 17 + i * 11, 50, 10, this.translate("gui.ic2.teleporter_hub.rename"), T -> this.openRename(index))
            )
            .visible = targets.size() > index;
      }

      gui.addRenderableWidget(100, new ExtendedButton(x + 6, y + 106, 20, 10, this.string("+"), T -> this.move(1))).active = this.offset
         < Math.max(targets.size() - 8, 0);
      gui.addRenderableWidget(101, new ExtendedButton(x + 6, y + 5, 20, 10, this.string("-"), T -> this.move(-1))).active = this.offset > 0;
      gui.addRenderableWidget(102, new ExtendedButton(x + 27, y + 106, 50, 10, this.translate("gui.ic2.teleporter_hub.rename"), T -> this.openRename(-1)));
      EditBox text = gui.addRenderableWidget(103, new EditBox(gui.getFont(), x + 80, y + 106, 70, 10, this.string()));
      text.setMaxLength(19);
      text.visible = false;
      gui.addRenderableWidget(
            104,
            new IconButton(x + 152, y + 106, 16, 16, IC2Items.ICON_DISPLAY.create(1), T -> this.cancelRename())
               .setToolTip("gui.ic2.teleporter_hub.rename_cancel")
         )
         .visible = false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      List<Tuple<TeleporterTarget, String>> targets = this.tile.targets.getClientTargets();

      for (int i = 0; i < 8; i++) {
         boolean visible = targets.size() > i + this.offset;
         gui.getButton(i).visible = visible;
         gui.getButton(i + 8).visible = visible;
      }

      gui.getButton(100).active = this.offset < Math.max(targets.size() - 8, 0);
      gui.getButton(101).active = this.offset > 0;
      EditBox text = gui.getCastedButton(103, EditBox.class);
      text.visible = this.rename;
      if (!this.rename) {
         gui.getButton(104).setFocused(false);
      }

      gui.getButton(104).visible = this.rename;
      if (this.rename && gui.getFocused() != text) {
         gui.setFocused(text);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      List<Tuple<TeleporterTarget, String>> targets = this.tile.targets.getClientTargets();

      for (int i = 0; i < 8 && i + this.offset < targets.size(); i++) {
         Tuple<TeleporterTarget, String> target = targets.get(i + this.offset);
         this.gui
            .drawString(
               matrix,
               Component.literal((String)target.getB()),
               18,
               18 + 11 * i,
               this.tile.hasEnergy(this.tile.getCost((TeleporterTarget)targets.get(i + this.offset).getA(), this.gui.getPlayer())) ? -1 : -61696
            );
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void addTooltips(PoseStack matrix, int mouseX, int mouseY, Consumer<Component> tooltips) {
      if (this.rename && this.gui.getButton(103).isMouseOver((double)(mouseX + this.gui.getGuiLeft()), (double)(mouseY + this.gui.getGuiTop()))) {
         tooltips.accept(this.translate("gui.ic2.teleporter_hub.enter"));
      }

      Player player = this.gui.getPlayer();
      List<Tuple<TeleporterTarget, String>> targets = this.tile.targets.getClientTargets();

      for (int i = 0; i < 8; i++) {
         int index = i + this.offset;
         if (targets.size() > index
            && TELEPORT_BOX.isInBox(mouseX, mouseY - 11 * i)
            && TELEPORT_BOX.getX() >= mouseX - this.gui.getFont().width((String)targets.get(index).getB())) {
            int cost = this.tile.getCost((TeleporterTarget)targets.get(i + this.offset).getA(), player);
            tooltips.accept(this.translate("gui.ic2.teleporter_hub.cost", new Object[]{Formatters.EU_FORMAT.format((long)cost)}));
            if (this.tile.hasEnergy(cost)) {
               tooltips.accept(this.translate("gui.ic2.teleporter_hub.teleport"));
            } else {
               tooltips.accept(this.translate("gui.ic2.teleporter_hub.missing"));
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      Player player = this.gui.getPlayer();
      List<Tuple<TeleporterTarget, String>> targets = this.tile.targets.getClientTargets();

      for (int i = 0; i < 8; i++) {
         int index = i + this.offset;
         if (targets.size() > index
            && TELEPORT_BOX.isInBox(mouseX, mouseY - 11 * i)
            && TELEPORT_BOX.getX() >= mouseX - this.gui.getFont().width((String)targets.get(index).getB())
            && this.tile.hasEnergy(this.tile.getCost((TeleporterTarget)targets.get(index).getA(), player))) {
            this.teleport(i);
            return true;
         }
      }

      return false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onKeyTyped(int keyCode) {
      if (this.rename) {
         if (keyCode == 69) {
            return true;
         }

         if (keyCode == 256) {
            this.cancelRename();
            return true;
         }

         if (keyCode == 257) {
            this.applyRename(this.gui);
            return true;
         }
      }

      return false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseScroll(int mouseX, int mouseY, int scroll) {
      if (this.rename) {
         return false;
      } else {
         this.offset = Mth.clamp(this.offset - scroll, 0, Math.max(0, this.tile.targets.getClientTargets().size() - 8));
         return true;
      }
   }

   protected void move(int index) {
      if (!this.rename) {
         this.offset = Mth.clamp(this.offset + index, 0, Math.max(0, this.tile.targets.getClientTargets().size() - 8));
      }
   }

   @OnlyIn(Dist.CLIENT)
   protected void applyRename(IC2Screen gui) {
      this.rename = false;
      String name = ((EditBox)gui.getCastedButton(103, EditBox.class)).getValue();
      if (this.renameIndex < 0) {
         this.tile.sendToServer("internal_rename", new NBTBuffer("newName", StringTag.valueOf(name)));
      } else {
         CompoundTag nbt = new CompoundTag();
         nbt.putString("newName", name);
         ((TeleporterTarget)this.tile.targets.getClientTargets().get(this.offset + this.renameIndex).getA()).write(nbt);
         this.tile.sendToServer("rename", new NBTBuffer(nbt));
      }
   }

   protected void cancelRename() {
      this.rename = false;
   }

   @OnlyIn(Dist.CLIENT)
   protected void openRename(int index) {
      this.rename = true;
      this.renameIndex = index;
      EditBox text = this.gui.getCastedButton(103, EditBox.class);
      if (index < 0) {
         text.setValue((this.tile.hasCustomName() ? this.tile.getCustomName() : this.tile.getDisplayName()).getString());
      } else {
         text.setValue((String)this.tile.targets.getClientTargets().get(index + this.offset).getB());
      }
   }

   protected void teleport(int index) {
      this.tile
         .sendToServer(
            "teleport", new NBTBuffer(((TeleporterTarget)this.tile.targets.getClientTargets().get(this.offset + index).getA()).write(new CompoundTag()))
         );
   }

   protected void delete(int index) {
      this.tile
         .sendToServer(
            "delete", new NBTBuffer(((TeleporterTarget)this.tile.targets.getClientTargets().get(this.offset + index).getA()).write(new CompoundTag()))
         );
   }
}
