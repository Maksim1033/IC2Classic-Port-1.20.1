package ic2.core.wiki.base;

import ic2.api.recipes.ingridients.inputs.IInput;
import ic2.core.wiki.components.IWikiComponent;
import ic2.core.wiki.recipes.providers.IItemReference;
import ic2.core.wiki.recipes.providers.IRecipeReference;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface IPageBuilder {
   void addComponent(IWikiComponent var1);

   void setEndOfPage();

   void setEndOfDoublePage();

   void disableSearch(boolean var1);

   int getHeightLeft();

   boolean hasComponents();

   void clearPage();

   void setPageLink(ResourceLocation var1);

   void markPreview(ItemLike var1);

   IRecipeReference getRecipes(List<ItemStack> var1, boolean var2);

   IItemReference getItemList(List<IInput> var1);
}
