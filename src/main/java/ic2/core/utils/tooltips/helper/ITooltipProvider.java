package ic2.core.utils.tooltips.helper;

import carbonconfiglib.config.ConfigEntry;
import ic2.core.utils.config.ic2.PassiveGeneratorSetting;
import ic2.core.utils.helpers.Formatters;
import ic2.core.utils.tooltips.ILangHelper;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ITooltipProvider extends ILangHelper {
   ITooltipProvider LV_MACHINE = euReaderTooltip("tooltip.item.ic2.eu_reader.sink_input", Formatters.EU_READER_FORMAT.format(32L));
   ITooltipProvider MV_MACHINE = euReaderTooltip("tooltip.item.ic2.eu_reader.sink_input", Formatters.EU_READER_FORMAT.format(128L));
   ITooltipProvider HV_MACHINE = euReaderTooltip("tooltip.item.ic2.eu_reader.sink_input", Formatters.EU_READER_FORMAT.format(512L));
   ITooltipProvider EV_MACHINE = euReaderTooltip("tooltip.item.ic2.eu_reader.sink_input", Formatters.EU_READER_FORMAT.format(2048L));
   ITooltipProvider IV_MACHINE = euReaderTooltip("tooltip.item.ic2.eu_reader.sink_input", Formatters.EU_READER_FORMAT.format(8192L));
   ITooltipProvider LuV_MACHINE = euReaderTooltip("tooltip.item.ic2.eu_reader.sink_input", Formatters.EU_READER_FORMAT.format(32768L));
   ITooltipProvider LV_OUT = output(32);
   ITooltipProvider MV_OUT = output(128);
   ITooltipProvider HV_OUT = output(512);
   ITooltipProvider EV_OUT = output(2048);
   ITooltipProvider IV_OUT = output(8192);
   ITooltipProvider LuV_OUT = output(32768);
   ITooltipProvider TRANSFORMER_PACKETS = euReaderTooltip("tooltip.item.ic2.eu_reader.source_packets", 4);
   ITooltipProvider UNLOADER_PACKETS = euReaderTooltip("tooltip.item.ic2.eu_reader.source_packets", 10);

   @OnlyIn(Dist.CLIENT)
   void addInformation(ItemStack var1, BlockGetter var2, List<Component> var3, TooltipFlag var4);

   static ITooltipProvider tooltip(String s) {
      return new BasicTextToolTip(s);
   }

   static ITooltipProvider tooltip(String s, Object... args) {
      return new BasicTextToolTip(s, args);
   }

   static ITooltipProvider euReaderTooltip(String s) {
      return new EUReaderTooltip(s);
   }

   static ITooltipProvider euReaderTooltip(String s, Object... args) {
      return new EUReaderTooltip(s, args);
   }

   static ITooltipProvider output(int euATick) {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.source_output", euATick);
   }

   static ITooltipProvider productionVariable() {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.production.variable");
   }

   static ITooltipProvider production(int euATick) {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.production", Formatters.EU_READER_FORMAT.format((long)euATick));
   }

   static ITooltipProvider production(int minEU, int maxEU) {
      return new EUReaderTooltip(
         "tooltip.item.ic2.eu_reader.production.range", Formatters.EU_READER_FORMAT.format((long)minEU), Formatters.EU_READER_FORMAT.format((long)maxEU)
      );
   }

   static ITooltipProvider productionPassive(int euATick) {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.production.passive", Formatters.EU_READER_FORMAT.format((long)euATick));
   }

   static ITooltipProvider productionPassive(int minEU, int maxEU) {
      return new EUReaderTooltip(
         "tooltip.item.ic2.eu_reader.production.passive.range",
         Formatters.EU_READER_FORMAT.format((long)minEU),
         Formatters.EU_READER_FORMAT.format((long)maxEU)
      );
   }

   static ITooltipProvider consumption(int euATick) {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.consumption", Formatters.EU_READER_FORMAT.format((long)euATick));
   }

   static ITooltipProvider consumptionUse(int euPerUse) {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.use", Formatters.EU_READER_FORMAT.format((long)euPerUse));
   }

   static ITooltipProvider consumption(int minEU, int maxEU) {
      return new EUReaderTooltip(
         "tooltip.item.ic2.eu_reader.consumption.range", Formatters.EU_READER_FORMAT.format((long)minEU), Formatters.EU_READER_FORMAT.format((long)maxEU)
      );
   }

   static ITooltipProvider storage(int storage) {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.storage", Formatters.EU_READER_FORMAT.format((long)storage));
   }

   static ITooltipProvider production(float euATick) {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.production", Formatters.EU_READER_FORMAT.format((double)euATick));
   }

   static ITooltipProvider production(float minEU, float maxEU) {
      return new EUReaderTooltip(
         "tooltip.item.ic2.eu_reader.production.range", Formatters.EU_READER_FORMAT.format((double)minEU), Formatters.EU_READER_FORMAT.format((double)maxEU)
      );
   }

   static ITooltipProvider productionPassive(float euATick) {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.production.passive", Formatters.EU_READER_FORMAT.format((double)euATick));
   }

   static ITooltipProvider productionPassive(float minEU, float maxEU) {
      return new EUReaderTooltip(
         "tooltip.item.ic2.eu_reader.production.passive.range",
         Formatters.EU_READER_FORMAT.format((double)minEU),
         Formatters.EU_READER_FORMAT.format((double)maxEU)
      );
   }

   static ITooltipProvider consumption(float euATick) {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.consumption", Formatters.EU_READER_FORMAT.format((double)euATick));
   }

   static ITooltipProvider consumptionUse(float euPerUse) {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.use", Formatters.EU_READER_FORMAT.format((double)euPerUse));
   }

   static ITooltipProvider consumption(float minEU, float maxEU) {
      return new EUReaderTooltip(
         "tooltip.item.ic2.eu_reader.consumption.range", Formatters.EU_READER_FORMAT.format((double)minEU), Formatters.EU_READER_FORMAT.format((double)maxEU)
      );
   }

   static ITooltipProvider storage(float storage) {
      return new EUReaderTooltip("tooltip.item.ic2.eu_reader.storage", Formatters.EU_READER_FORMAT.format((double)storage));
   }

   static ITooltipProvider production(ConfigEntry<? extends Number> euATick) {
      return new EUReaderTooltip(new SimpleConfigTranslationSupplier("tooltip.item.ic2.eu_reader.production", Formatters.EU_READER_FORMAT, euATick));
   }

   static ITooltipProvider production(ConfigEntry<? extends Number> euATick, float multiplier) {
      return new EUReaderTooltip(new SimpleConfigTranslationSupplier("tooltip.item.ic2.eu_reader.production", Formatters.EU_READER_FORMAT, euATick, multiplier));
   }

   static ITooltipProvider productionPassive(ConfigEntry<? extends Number> euATick) {
      return new EUReaderTooltip(new SimpleConfigTranslationSupplier("tooltip.item.ic2.eu_reader.production.passive", Formatters.EU_READER_FORMAT, euATick));
   }

   static ITooltipProvider consumption(ConfigEntry<? extends Number> euATick) {
      return new EUReaderTooltip(new SimpleConfigTranslationSupplier("tooltip.item.ic2.eu_reader.consumption", Formatters.EU_READER_FORMAT, euATick));
   }

   static ITooltipProvider consumptionUse(ConfigEntry<? extends Number> euPerUse) {
      return new EUReaderTooltip(new SimpleConfigTranslationSupplier("tooltip.item.ic2.eu_reader.use", Formatters.EU_READER_FORMAT, euPerUse));
   }

   static ITooltipProvider storage(ConfigEntry<? extends Number> storage) {
      return new EUReaderTooltip(new SimpleConfigTranslationSupplier("tooltip.item.ic2.eu_reader.storage", Formatters.EU_READER_FORMAT, storage));
   }

   static ITooltipProvider production(double value, ConfigEntry<? extends Number> multiplier, boolean multiply) {
      return new EUReaderTooltip(
         new MultiplierConfigTranslationSupplier("tooltip.item.ic2.eu_reader.production", Formatters.EU_READER_FORMAT, value, multiplier, multiply)
      );
   }

   static ITooltipProvider production(double minValue, double maxValue, ConfigEntry<? extends Number> multiplier, boolean multiply) {
      return new EUReaderTooltip(
         new RangeConfigTranslationSupplier(
            "tooltip.item.ic2.eu_reader.production.range", Formatters.EU_READER_FORMAT, minValue, maxValue, multiplier, multiply
         )
      );
   }

   static ITooltipProvider productionPassive(double minValue, double maxValue, PassiveGeneratorSetting generatorSettings, boolean isFuelProduction) {
      return new EUReaderTooltip(
         new PassiveConfigTranslationSupplier(
            "tooltip.item.ic2.eu_reader.production.passive.range", Formatters.EU_READER_FORMAT, minValue, maxValue, generatorSettings, isFuelProduction
         )
      );
   }
}
