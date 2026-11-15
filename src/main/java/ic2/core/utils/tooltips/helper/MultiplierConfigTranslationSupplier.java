package ic2.core.utils.tooltips.helper;

import carbonconfiglib.config.ConfigEntry;
import java.text.NumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class MultiplierConfigTranslationSupplier extends ConfigTranslationSupplier {
   private double value;
   private ConfigEntry<? extends Number> multiplier;
   private boolean multiply;

   public MultiplierConfigTranslationSupplier(String key, NumberFormat formatter, double value, ConfigEntry<? extends Number> multiplier, boolean multiply) {
      super(key, formatter);
      this.value = value;
      this.multiplier = multiplier;
      this.multiply = multiply;
   }

   @Override
   protected Component buildTextComponent() {
      double mul = ((Number)this.multiplier.getValue()).doubleValue();
      return Component.translatable(this.key, new Object[]{this.formatter.format(this.multiply ? this.value * mul : this.value / mul)})
         .withStyle(ChatFormatting.GRAY);
   }
}
