package ic2.core.utils.tooltips.helper;

import carbonconfiglib.config.ConfigEntry;
import java.text.NumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class SimpleConfigTranslationSupplier extends ConfigTranslationSupplier {
   private ConfigEntry<? extends Number> value;
   float multiplier;

   public SimpleConfigTranslationSupplier(String key, NumberFormat formatter, ConfigEntry<? extends Number> value) {
      this(key, formatter, value, 1.0F);
   }

   public SimpleConfigTranslationSupplier(String key, NumberFormat formatter, ConfigEntry<? extends Number> value, float multiplier) {
      super(key, formatter);
      this.value = value;
      this.multiplier = multiplier;
   }

   @Override
   protected Component buildTextComponent() {
      return Component.translatable(this.key, new Object[]{this.formatter.format((double)(((Number)this.value.getValue()).floatValue() * this.multiplier))})
         .withStyle(ChatFormatting.GRAY);
   }
}
