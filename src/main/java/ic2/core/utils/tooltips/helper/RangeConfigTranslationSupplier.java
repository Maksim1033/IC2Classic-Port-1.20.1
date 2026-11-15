package ic2.core.utils.tooltips.helper;

import carbonconfiglib.config.ConfigEntry;
import java.text.NumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class RangeConfigTranslationSupplier extends ConfigTranslationSupplier {
   private double minValue;
   private double maxValue;
   private ConfigEntry<? extends Number> multiplier;
   private boolean multiply;

   public RangeConfigTranslationSupplier(
      String key, NumberFormat formatter, double minValue, double maxValue, ConfigEntry<? extends Number> multiplier, boolean multiply
   ) {
      super(key, formatter);
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.multiplier = multiplier;
      this.multiply = multiply;
   }

   @Override
   protected Component buildTextComponent() {
      double mul = ((Number)this.multiplier.getValue()).doubleValue();
      return Component.translatable(
            this.key,
            new Object[]{
               this.formatter.format(this.multiply ? this.minValue * mul : this.minValue / mul),
               this.formatter.format(this.multiply ? this.maxValue * mul : this.maxValue / mul)
            }
         )
         .withStyle(ChatFormatting.GRAY);
   }
}
