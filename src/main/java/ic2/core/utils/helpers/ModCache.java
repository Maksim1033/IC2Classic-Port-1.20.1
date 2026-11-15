package ic2.core.utils.helpers;

import java.util.Optional;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public class ModCache {
   ModContainer cached = null;

   public void setActiveMod(String name) {
      Optional<? extends ModContainer> container = ModList.get().getModContainerById(name);
      if (!container.isEmpty() && !ModLoadingContext.get().getActiveNamespace().equals(name)) {
         if (this.cached != null) {
            throw new IllegalStateException("Cache already set");
         } else {
            ModLoadingContext context = ModLoadingContext.get();
            this.cached = context.getActiveContainer();
            context.setActiveContainer(container.orElse(null));
         }
      }
   }

   public void reset() {
      if (this.cached != null) {
         ModLoadingContext.get().setActiveContainer(this.cached);
         this.cached = null;
      }
   }
}
