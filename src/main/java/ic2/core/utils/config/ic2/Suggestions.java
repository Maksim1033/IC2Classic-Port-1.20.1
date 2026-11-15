package ic2.core.utils.config.ic2;

import carbonconfiglib.api.ISuggestionProvider;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.ForgeRegistries;

public class Suggestions {
   public static final ISuggestionProvider INSTANCE = new Suggestions.EnchantmentSuggestion();

   public static class EnchantmentSuggestion implements ISuggestionProvider {
      public void provideSuggestions(Consumer<Suggestion> consumer, Predicate<Suggestion> filter) {
         for (IModInfo info : ModList.get().getMods()) {
            Suggestion suggestion = Suggestion.namedValue(info.getDisplayName(), info.getModId());
            if (filter.test(suggestion)) {
               consumer.accept(suggestion);
            }
         }

         for (Enchantment ench : ForgeRegistries.ENCHANTMENTS) {
            Suggestion suggestion = Suggestion.namedTypeValue(
               I18n.get(ench.getDescriptionId(), new Object[0]), ForgeRegistryWrapper.getEnchantmentKey(ench).toString(), Enchantment.class
            );
            if (filter.test(suggestion)) {
               consumer.accept(suggestion);
            }
         }
      }
   }
}
