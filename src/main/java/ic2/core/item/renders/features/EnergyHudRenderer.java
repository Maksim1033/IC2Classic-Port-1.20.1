package ic2.core.item.renders.features;

import carbonconfiglib.gui.screens.WidgetAlignerScreen.OverlayRenderer;
import carbonconfiglib.impl.entries.WidgetAligner;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import ic2.api.items.armor.IArmorHud;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.ICustomElectricItem;
import ic2.api.items.electric.IElectricItem;
import ic2.core.IC2;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnergyHudRenderer {
   public static final EnergyHudRenderer INSTANCE = new EnergyHudRenderer();

   @OnlyIn(Dist.CLIENT)
   public void renderOverlay(PoseStack stack, int width, int height) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         ItemStack head = mc.player.getItemBySlot(EquipmentSlot.HEAD);
         if (head.getItem() instanceof IArmorHud hud && hud.isHudEnabled(head)) {
            stack.pushPose();
            stack.translate(0.0, 0.0, 1.0);
            WidgetAligner aligner = (WidgetAligner)IC2.CONFIG.hud.get();
            boolean vertical = IC2.CONFIG.hudOrientation.get() == EnergyHudRenderer.RenderOrientation.VERTICAL;
            aligner.applyToPose(stack, (double)width, (double)height, vertical ? 16.0 : 64.0, vertical ? 64.0 : 16.0);
            ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack legs = mc.player.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack feet = mc.player.getItemBySlot(EquipmentSlot.FEET);
            this.renderOverlay(mc, stack, vertical, head, chest, legs, feet);
            stack.popPose();
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   protected void renderOverlay(Minecraft mc, PoseStack stack, boolean vertical, ItemStack... items) {
      Lighting.setupForFlatItems();
      this.renderItemStack(mc, stack, items[0], 0, vertical);
      this.renderItemStack(mc, stack, items[1], 1, vertical);
      this.renderItemStack(mc, stack, items[2], 2, vertical);
      this.renderItemStack(mc, stack, items[3], 3, vertical);
   }

   @OnlyIn(Dist.CLIENT)
   private void renderItemStack(Minecraft mc, PoseStack matrix, ItemStack stack, int slotNum, boolean vertical) {
      if (stack.getItem() instanceof IElectricItem || stack.getItem() instanceof ICustomElectricItem) {
         int x = vertical ? 0 : 16 * slotNum;
         int y = vertical ? 16 * slotNum : 0;
         RenderUtils.renderGuiItem(mc.getItemRenderer(), matrix, stack, (float)x, (float)y);
         RenderUtils.renderGuiItemDecorations(matrix, mc.font, stack, x, y);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class HudRenderer implements OverlayRenderer {
      public void render(GuiGraphics graphics, int width, int height, float partialTime, WidgetAligner aligner) {
         PoseStack stack = graphics.pose();
         stack.pushPose();
         stack.translate(0.0, 0.0, 1.0);
         boolean vertical = IC2.CONFIG.hudOrientation.get() == EnergyHudRenderer.RenderOrientation.VERTICAL;
         aligner.applyToPose(stack, (double)width, (double)height, vertical ? 16.0 : 64.0, vertical ? 64.0 : 16.0);
         ItemStack head = this.items(EquipmentSlot.HEAD, IC2Items.QUANTUM_SUIT_HELMET);
         ItemStack chest = this.items(EquipmentSlot.CHEST, IC2Items.QUANTUM_SUIT_CHESTPLATE);
         ItemStack legs = this.items(EquipmentSlot.LEGS, IC2Items.QUANTUM_SUIT_LEGGINGS);
         ItemStack feet = this.items(EquipmentSlot.FEET, IC2Items.QUANTUM_SUIT_BOOTS);
         EnergyHudRenderer.INSTANCE.renderOverlay(Minecraft.getInstance(), stack, vertical, head, chest, legs, feet);
         stack.popPose();
      }

      private ItemStack items(EquipmentSlot slots, ItemLike replacement) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player == null) {
            return this.createFull(replacement);
         } else {
            ItemStack stack = mc.player.getItemBySlot(slots);
            return stack.isEmpty() ? this.createFull(replacement) : stack;
         }
      }

      private ItemStack createFull(ItemLike replacement) {
         ItemStack stack = new ItemStack(replacement, 1);
         ElectricItem.MANAGER.charge(stack, 2147483647, 2147483647, true, false);
         return stack;
      }

      public double unscaledHeight() {
         return IC2.CONFIG.hudOrientation.get() == EnergyHudRenderer.RenderOrientation.VERTICAL ? 64.0 : 16.0;
      }

      public double unscaledWidth() {
         return IC2.CONFIG.hudOrientation.get() == EnergyHudRenderer.RenderOrientation.VERTICAL ? 16.0 : 64.0;
      }
   }

   public static enum RenderOrientation {
      HORIZONTAL,
      VERTICAL;
   }
}
