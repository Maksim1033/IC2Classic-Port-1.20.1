package ic2.core.inventory.custom.components;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.base.ItemCheckBox;
import ic2.core.inventory.gui.components.base.SubItemCheckbox;
import ic2.core.inventory.gui.components.simple.SliderComponent;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.client.friend.FriendSyncRequest;
import ic2.core.networking.packets.client.friend.FriendUpdatePacket;
import ic2.core.platform.player.friends.Action;
import ic2.core.platform.player.friends.Friend;
import ic2.core.platform.player.friends.FriendManager;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box2i;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class FriendComponent extends GuiWidget {
   static final int GRAY = ColorUtils.darker(ColorUtils.GRAY);
   public static final Box2i LEFT_SLIDER = new Box2i(104, 32, 12, 60);
   public static final Box2i RIGHT_SLIDER = new Box2i(166, 32, 12, 60);
   public static final Box2i TEXTURE = new Box2i(0, 241, 12, 15);
   UUID current = null;
   Friend friend = null;
   List<Action> allActions = Action.getAllActions();
   int lastRight = 0;
   SliderComponent leftSlider = this.addChild(new SliderComponent(LEFT_SLIDER, TEXTURE).setNonEmptyRows(3));
   SliderComponent rightSlider = this.addChild(new SliderComponent(RIGHT_SLIDER, TEXTURE).setNonEmptyRows(3));

   public FriendComponent() {
      super(Box2i.EMPTY_BOX);
   }

   @Override
   protected void addRequests(Set<GuiWidget.ActionRequest> requests) {
      requests.add(GuiWidget.ActionRequest.GUI_INIT);
      requests.add(GuiWidget.ActionRequest.GUI_TICK);
      requests.add(GuiWidget.ActionRequest.GUI_CLOSE);
      requests.add(GuiWidget.ActionRequest.DRAW_BACKGROUND);
      requests.add(GuiWidget.ActionRequest.DRAW_FOREGROUND);
      requests.add(GuiWidget.ActionRequest.MOUSE_INPUT);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void init(IC2Screen gui) {
      int x = gui.getGuiLeft();
      int y = gui.getGuiTop();

      for (int i = 0; i + this.lastRight < this.allActions.size() && i < 6; i++) {
         int index = i;
         Action action = this.allActions.get(i + this.lastRight);
         ((SubItemCheckbox)gui.addRenderableWidget(
               10 + i,
               new SubItemCheckbox(x + 123 + i % 2 * 20, y + 32 + i / 2 * 20, 20, 20, T -> this.toggleAction(index), action.getMainIcon(), action.getSubIcon())
            ))
            .setToolTip(action.getLocal());
      }

      FriendManager friend = FriendManager.getClientFriends();
      friend.setIgnoreUpdates(true);
      List<PlayerInfo> infos = this.getInfos();

      for (int i = 0; i < 4; i++) {
         int index = i;
         boolean visible = i < infos.size();
         boolean value = visible && friend.getFriend(gui.getPlayerID(), infos.get(i + this.leftSlider.getCurrent()).getProfile().getId()) != null;
         ((ItemCheckBox)gui.addRenderableWidget(
               i, new ItemCheckBox(x + 87, y + 32 + i * 15, 14, 14, T -> this.toggleFriend(index), new ItemStack(Items.SHIELD), value)
            ))
            .setToolTip("Friend?")
            .visible = visible;
      }

      gui.addRenderableWidget(7, new ExtendedButton(x + 50, y + 95, 80, 13, this.translate("gui.ic2.friends.save"), T -> this.save())).active = this.current
         != null;
      this.rightSlider.setMax(Mth.ceil((double)this.allActions.size() / 2.0));
   }

   @OnlyIn(Dist.CLIENT)
   private void toggleFriend(int index) {
      index += this.leftSlider.getCurrent();
      List<PlayerInfo> infos = this.getInfos();
      if (index >= 0 && index < infos.size()) {
         FriendManager friend = FriendManager.getClientFriends();
         GameProfile profile = infos.get(index).getProfile();
         if (friend.getFriend(this.gui.getPlayerID(), profile.getId()) == null) {
            friend.addFriend(this.gui.getPlayerID(), new Friend(profile));
         } else {
            friend.removeFriend(this.gui.getPlayerID(), profile.getId());
         }

         PacketManager.INSTANCE.sendToServer(new FriendUpdatePacket(friend.getFriends(this.gui.getPlayerID())));
      }
   }

   private void toggleAction(int index) {
      index += this.rightSlider.getCurrent() * 2;
      if (index >= 0 && index < this.allActions.size() && this.friend != null) {
         Action action = this.allActions.get(index);
         if (this.friend.canApplyAction(action)) {
            this.friend.removeAction(action);
         } else {
            this.friend.addAction(action);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   private void save() {
      if (this.current != null) {
         PacketManager.INSTANCE.sendToServer(new FriendUpdatePacket(FriendManager.getClientFriends().getFriends(this.gui.getPlayerID())));
         this.friend = null;
         this.current = null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onClose() {
      FriendManager.getClientFriends().setIgnoreUpdates(false);
      PacketManager.INSTANCE.sendToServer(new FriendSyncRequest());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void tick(IC2Screen gui) {
      gui.getButton(7).active = this.current != null;
      this.leftSlider.setMax(this.getInfos().size());
      FriendManager friend = FriendManager.getClientFriends();
      List<PlayerInfo> infos = this.getInfos();

      for (int i = 0; i + this.leftSlider.getCurrent() < infos.size() && i < 4; i++) {
         if (i + this.leftSlider.getCurrent() >= infos.size()) {
            gui.getCastedButton(i, ItemCheckBox.class).visible = false;
         } else {
            UUID entry = infos.get(i + this.leftSlider.getCurrent()).getProfile().getId();
            boolean value = friend.getFriend(gui.getPlayerID(), entry) != null;
            ItemCheckBox checkbox = ((ItemCheckBox)gui.getCastedButton(i, ItemCheckBox.class)).setChecked(value);
            checkbox.active = this.current == null || !this.current.equals(entry);
            checkbox.visible = true;
         }
      }

      if (this.lastRight != this.rightSlider.getCurrent()) {
         this.lastRight = this.rightSlider.getCurrent();
         int actual = this.lastRight * 2;

         for (int ix = 0; ix + actual < this.allActions.size() && ix < 6; ix++) {
            Action action = this.allActions.get(ix + actual);
            ((SubItemCheckbox)gui.getCastedButton(10 + ix, SubItemCheckbox.class))
               .setIcons(action.getMainIcon(), action.getSubIcon())
               .setToolTip(action.getLocal());
         }
      }

      int actual = this.lastRight * 2;
      int left = 6;

      for (int ix = 0; ix + actual < this.allActions.size() && ix < 6; ix++) {
         SubItemCheckbox box = gui.getCastedButton(10 + ix, SubItemCheckbox.class);
         box.setChecked(this.friend != null && this.friend.canApplyAction(this.allActions.get(ix + actual))).active = this.current != null;
         box.visible = true;
         left--;
      }

      if (left > 0) {
         for (int ix = 6 - left; ix < 6; ix++) {
            gui.getCastedButton(10 + ix, SubItemCheckbox.class).visible = false;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
      int x = this.gui.getGuiLeft();
      int y = this.gui.getGuiTop();
      List<PlayerInfo> infos = this.getInfos();

      for (int i = 0; i + this.leftSlider.getCurrent() < infos.size() && i < 4; i++) {
         PlayerInfo info = infos.get(i + this.leftSlider.getCurrent());
         UUID id = info.getProfile().getId();
         if (id != null && mouseX >= 6 && mouseX <= 86 && mouseY >= 32 + i * 15 && mouseY < 47 + i * 15) {
            this.gui.drawColoredRegion(matrix, (float)(x + 6), (float)(y + 32 + i * 15), 95.0F, 14.0F, ColorUtils.LIGHT_GRAY);
         } else if (id != null && id.equals(this.current)) {
            this.gui.drawColoredRegion(matrix, (float)(x + 6), (float)(y + 32 + i * 15), 95.0F, 14.0F, GRAY);
         }

         this.gui.bindTexture(info.getSkinLocation());
         IC2Screen.blit(matrix, x + 6, y + 32 + i * 15, 14, 14, 8.0F, 8.0F, 8, 8, 64, 64);
         RenderSystem.enableBlend();
         IC2Screen.blit(matrix, x + 6, y + 32 + i * 15, 14, 14, 40.0F, 8.0F, 8, 8, 64, 64);
         RenderSystem.defaultBlendFunc();
         this.gui.drawColoredRegion(matrix, (float)(x + 6), (float)(y + 46 + i * 15), 95.0F, 1.0F, -13158601);
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void drawForeground(PoseStack matrix, int mouseX, int mouseY) {
      int index = 0;
      List<PlayerInfo> infos = this.getInfos();
      this.gui.drawRightString(matrix, this.string(this.leftSlider.getCurrent() + "/" + Math.max(0, infos.size() - 3)), 117, 22, 4210752);
      this.gui.drawRightString(matrix, this.string(this.rightSlider.getCurrent() + "/" + Math.max(0, (this.allActions.size() - 6) / 2)), 179, 22, 4210752);

      for (int i = 0; i + this.leftSlider.getCurrent() < infos.size() && i < 4; i++) {
         PlayerInfo info = infos.get(i + this.leftSlider.getCurrent());
         Component text = this.string(info.getProfile().getName());
         if (text != null) {
            this.gui.drawString(matrix, text, 27, 35 + index * 15, 4210752);
            index++;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public List<PlayerInfo> getInfos() {
      List<PlayerInfo> infos = CollectionUtils.<PlayerInfo>createList();
      infos.addAll(this.gui.getPlayer().connection.getOnlinePlayers());
      infos.removeIf(T -> this.gui.getPlayerID().equals(T.getProfile().getId()));
      return infos;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
      if (mouseY >= 32 && mouseX >= 6 && mouseX <= 86) {
         int index = (mouseY - 32) / 15;
         List<PlayerInfo> infos = this.getInfos();
         if (index < 4 && index + this.leftSlider.getCurrent() < infos.size()) {
            UUID id = infos.get(index + this.leftSlider.getCurrent()).getProfile().getId();
            if (id != null) {
               Friend friend = FriendManager.getClientFriends().getFriend(this.gui.getPlayerID(), id);
               if (friend != null) {
                  IC2.AUDIO.playSound(this.gui.getPlayer(), SoundEvents.UI_BUTTON_CLICK.get().getLocation(), AudioManager.SoundType.STATIC, 1.0F, 1.0F);
                  this.current = id;
                  this.friend = friend;
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
