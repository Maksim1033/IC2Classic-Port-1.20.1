package ic2.core.block.machines.logic.crafter;

import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.inventory.transporter.transporters.special.ListTransporter;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap.BasicEntry;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.List;

public class CraftingCache {
   IItemTransporter output;
   IItemTransporter source;
   List<CraftingList> recipes;
   List<IWorkbenchExpansion> expansions;
   Entry<IFilter> missingItem;

   public CraftingCache(IItemTransporter output, IItemTransporter source, List<CraftingList> recipes, List<IWorkbenchExpansion> expansions) {
      this.output = output;
      this.source = source;
      this.recipes = recipes;
      this.expansions = expansions;
   }

   public CraftingCache createSubCache(SimpleInventory tempTrash) {
      IItemTransporter transporter = TransporterManager.getTransporter(tempTrash);
      return new CraftingCache(new ListTransporter(this.output, transporter), new ListTransporter(this.source, transporter), this.recipes, this.expansions);
   }

   public IItemTransporter getOutput() {
      return this.output;
   }

   public IItemTransporter getProvider() {
      return this.source;
   }

   public Entry<IFilter> getMissingItem() {
      return this.missingItem;
   }

   public void setMissingItem(IFilter missing, int amount) {
      if (this.missingItem == null) {
         this.missingItem = new BasicEntry(missing, amount);
      }
   }

   public void setActiveRecipe(CraftRecipe recipe, int slot) {
      int i = 0;

      for (int m = this.expansions.size(); i < m; i++) {
         this.expansions.get(i).setCurrentCraftingRecipe(recipe, slot);
      }
   }

   public void clearActiveRecipe() {
      int i = 0;

      for (int m = this.expansions.size(); i < m; i++) {
         this.expansions.get(i).clearCurrentRecipe();
      }
   }

   public CraftRecipe getRecipeFromFilter(IFilter filter) {
      int i = 0;

      for (int m = this.recipes.size(); i < m; i++) {
         CraftRecipe recipe = this.recipes.get(i).getRecipeFrom(filter);
         if (recipe != null) {
            return recipe;
         }
      }

      return null;
   }
}
