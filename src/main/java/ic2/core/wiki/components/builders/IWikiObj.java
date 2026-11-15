package ic2.core.wiki.components.builders;

import ic2.core.wiki.base.IPageBuilder;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public interface IWikiObj {
   void buildComponents(IPageBuilder var1);

   default void provideLang(Consumer<Component> comp) {
   }
}
