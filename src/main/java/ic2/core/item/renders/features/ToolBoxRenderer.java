package ic2.core.item.renders.features;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.core.IC2;
import ic2.core.item.base.features.IScrollableInventory;
import ic2.core.platform.player.PlayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ToolBoxRenderer {
   public static final ResourceLocation WIDGETS_TEX_PATH = new ResourceLocation("textures/gui/widgets.png");
   public static final ToolBoxRenderer INSTANCE = new ToolBoxRenderer();

   @OnlyIn(Dist.CLIENT)
   public void renderOverlay(GuiGraphics guiGraphics, int width, int height) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.screen == null && IC2.KEYBOARD.isHudModeKeyDown(mc.player)) {
         Inventory player = mc.player.getInventory();
         if (!(player.getSelected().getItem() instanceof IScrollableInventory)) {
            ItemStack stack = this.getToolBox(player);
            if (!stack.isEmpty()) {
               IScrollableInventory.IndexedStack[] data = ((IScrollableInventory)stack.getItem()).getItemsToSwapWith(stack, mc.player.isShiftKeyDown());
               PoseStack matrix = guiGraphics.pose();
               matrix.translate(0.0, 0.0, 1.0);
               int pos = width / 2 - 90 - 1 + player.selected * 20;
               int top = height - 66 - 1;
               int bottom = height - 44 - 1;
               guiGraphics.blit(WIDGETS_TEX_PATH, pos, bottom, 60, 22, 22, 24);
               guiGraphics.blit(WIDGETS_TEX_PATH, pos, top, 60, 22, 22, 24);
               guiGraphics.renderItem(data[0].getStack(), pos + 3, bottom + 4);
               guiGraphics.renderItemDecorations(mc.font, data[0].getStack(), pos + 3, bottom + 4);
               guiGraphics.renderItem(data[1].getStack(), pos + 3, top + 4);
               guiGraphics.renderItemDecorations(mc.font, data[1].getStack(), pos + 3, top + 4);
               matrix.translate(0.0, 0.0, -1.0);
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public boolean onScrollToolbox() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.screen == null && IC2.KEYBOARD.isHudModeKeyDown(mc.player)) {
         Inventory player = mc.player.getInventory();
         return player.getSelected().getItem() instanceof IScrollableInventory ? false : !this.getToolBox(player).isEmpty();
      } else {
         return false;
      }
   }

   public boolean onServerScrollToolbox(Player owner, boolean result) {
      if (owner.getInventory().getSelected().getItem() instanceof IScrollableInventory) {
         return false;
      } else {
         PlayerHandler handler = PlayerHandler.getHandler(owner);
         if (handler.hudModeKeyDown && !handler.screenOpen) {
            ItemStack stack = this.getToolBox(owner.getInventory());
            if (stack.isEmpty()) {
               return false;
            } else {
               IScrollableInventory item = (IScrollableInventory)stack.getItem();
               IScrollableInventory.IndexedStack[] data = item.getItemsToSwapWith(stack, owner.isShiftKeyDown());
               int slot = item.getScrollIndex(stack);
               int index = result ? 1 : 0;
               ItemStack toSwap = data[index].getStack();
               ItemStack toInsert = owner.getInventory().getSelected();
               if (item.swapItems(stack, data[index].getSlot(), slot, toInsert)) {
                  owner.getInventory().setItem(owner.getInventory().selected, toSwap);
                  if (result) {
                     item.setScrollIndex(stack, data[index].getSlot());
                  } else {
                     int newSlot = data[index].getSlot();
                     item.setScrollIndex(stack, (newSlot == 0 ? item.getSlotCount(stack) : newSlot) - 1);
                  }
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }

   public ItemStack getToolBox(Inventory inv) {
      ItemStack current = inv.getSelected();

      for (int i = 0; i < 9; i++) {
         if (i != inv.selected) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof IScrollableInventory scroll
               && scroll.canScrollInventory(stack)
               && (current.isEmpty() || scroll.isValidItem(stack, current))) {
               return stack;
            }
         }
      }

      return ItemStack.EMPTY;
   }
}
