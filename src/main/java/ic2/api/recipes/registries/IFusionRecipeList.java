package ic2.api.recipes.registries;

import java.util.List;
import net.minecraft.world.item.Item;

public interface IFusionRecipeList extends IListenableRegistry<IFusionRecipeList> {
   void addFuel(Item var1, int var2, float var3, boolean var4);

   IFusionRecipeList.FusionFuel getFuel(Item var1);

   void removeFuel(Item var1);

   List<IFusionRecipeList.FusionFuel> getFuels();

   public static record FusionFuel(Item fuel, int fuelValue, float productionRate, boolean consumed) {
   }
}
