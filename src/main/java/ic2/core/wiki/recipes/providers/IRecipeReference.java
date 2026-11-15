package ic2.core.wiki.recipes.providers;

import ic2.core.wiki.recipes.renderers.IRecipeRenderer;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public interface IRecipeReference {
   List<IRecipeRenderer> getInputs();

   List<IRecipeRenderer> getOutputs();

   void setListener(Runnable var1);

   void addSearchInfo(Consumer<Component> var1);
}
