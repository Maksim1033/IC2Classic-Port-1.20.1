package ic2.core.utils.tooltips;

import ic2.core.utils.collection.CollectionUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToolTipHelper {
   List<Component> components;
   List<Component>[] key_components = CollectionUtils.createLists(3);
   int endIndex = 1;
   int index = 1;
   int flags = 0;

   public ToolTipHelper(List<Component> components) {
      this.components = components;
      this.flags = this.flags | (Screen.hasControlDown() ? 1 : 0);
      this.flags = this.flags | (Screen.hasShiftDown() ? 2 : 0);
      this.flags = this.flags | (Screen.hasAltDown() ? 4 : 0);
   }

   public boolean hasAltDown() {
      return (this.flags & 4) != 0;
   }

   public boolean hasShiftDown() {
      return (this.flags & 2) != 0;
   }

   public boolean hasCtrlDown() {
      return (this.flags & 1) != 0;
   }

   public void applyKeyInfo() {
      if (!this.key_components[0].isEmpty()) {
         if (this.hasCtrlDown()) {
            this.components.addAll(this.endIndex, this.key_components[0]);
            this.endIndex = this.endIndex + this.key_components[0].size();
         } else {
            this.components.add(this.endIndex++, Component.translatable("tooltip.ic2.press_ctrl.name").withStyle(ChatFormatting.GOLD));
         }
      }

      if (!this.key_components[1].isEmpty()) {
         if (this.hasShiftDown()) {
            this.components.addAll(this.endIndex, this.key_components[1]);
            this.endIndex = this.endIndex + this.key_components[1].size();
         } else {
            this.components.add(this.endIndex++, Component.translatable("tooltip.ic2.press_shift.name").withStyle(ChatFormatting.GOLD));
         }
      }

      if (!this.key_components[2].isEmpty()) {
         if (this.hasAltDown()) {
            this.components.addAll(this.endIndex, this.key_components[2]);
            this.endIndex = this.endIndex + this.key_components[2].size();
         } else {
            this.components.add(this.endIndex++, Component.translatable("tooltip.ic2.press_alt.name").withStyle(ChatFormatting.GOLD));
         }
      }
   }

   public void setAddIndex(int index) {
      this.index = Mth.clamp(index, 0, this.components.size());
   }

   public void setEnd(int endIndex) {
      this.endIndex = Mth.clamp(endIndex, 0, this.components.size());
   }

   public List<Component> getTooltips() {
      return this.components;
   }

   public ToolTipHelper addSimpleToolTip(MutableComponent component) {
      this.components.add(this.index++, component);
      this.endIndex++;
      Style style = component.getStyle();
      if (style.getColor() == null) {
         component.setStyle(style.applyFormat(ChatFormatting.GRAY));
      }

      return this;
   }

   public ToolTipHelper addSimpleToolTip(String translationKey, Object... args) {
      return this.addSimpleToolTip(Component.translatable(translationKey, args));
   }

   public ToolTipHelper addKeybindingTooltip(MutableComponent component) {
      this.key_components[0].add(component);
      Style style = component.getStyle();
      if (style.getColor() == null) {
         component.setStyle(style.applyFormat(ChatFormatting.GRAY));
      }

      return this;
   }

   public ToolTipHelper addKeybindingTooltip(String translationKey, Object... args) {
      return this.addKeybindingTooltip(Component.translatable(translationKey, args));
   }

   public ToolTipHelper addDataTooltip(MutableComponent component) {
      this.key_components[1].add(component);
      Style style = component.getStyle();
      if (style.getColor() == null) {
         component.setStyle(style.applyFormat(ChatFormatting.GRAY));
      }

      return this;
   }

   public ToolTipHelper addDataTooltip(String translationKey, Object... args) {
      return this.addDataTooltip(Component.translatable(translationKey, args));
   }

   public ToolTipHelper addStatTooltip(MutableComponent component) {
      this.key_components[2].add(component);
      Style style = component.getStyle();
      if (style.getColor() == null) {
         component.setStyle(style.applyFormat(ChatFormatting.GRAY));
      }

      return this;
   }

   public ToolTipHelper addStatTooltip(String translationKey, Object... args) {
      return this.addStatTooltip(Component.translatable(translationKey, args));
   }
}
