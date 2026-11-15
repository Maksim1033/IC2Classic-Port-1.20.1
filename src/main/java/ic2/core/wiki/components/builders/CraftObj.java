package ic2.core.wiki.components.builders;

import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.wiki.base.IPageBuilder;
import ic2.core.wiki.components.CraftingComponent;
import ic2.core.wiki.components.MultiCraftingComponent;
import ic2.core.wiki.recipes.providers.IRecipeReference;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class CraftObj implements IWikiObj {
   List<ItemStack> items = CollectionUtils.<ItemStack>createList();
   boolean forceSingle = false;
   boolean ignoreHidden = true;

   public CraftObj() {
   }

   public CraftObj(ItemStack... items) {
      this.addItems(items);
   }

   public CraftObj(ItemLike... items) {
      this.addItems(items);
   }

   public CraftObj addItems(ItemStack... items) {
      this.items.addAll(CollectionUtils.asList(items));
      return this;
   }

   public CraftObj addItems(ItemLike... items) {
      for (ItemLike item : items) {
         this.items.add(new ItemStack(item));
      }

      return this;
   }

   public CraftObj disableHidden() {
      this.ignoreHidden = false;
      return this;
   }

   public CraftObj forceSingleItem() {
      this.forceSingle = true;
      return this;
   }

   @Override
   public void buildComponents(IPageBuilder builder) {
      if (!this.forceSingle && this.items.size() != 1) {
         Map<ItemStack, IRecipeReference> references = new Object2ObjectLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(true));

         for (ItemStack stack : this.items) {
            references.put(stack, builder.getRecipes(ObjectLists.singleton(stack), this.ignoreHidden));
         }

         builder.addComponent(new MultiCraftingComponent(references));
      } else {
         builder.addComponent(new CraftingComponent(builder.getRecipes(this.items, this.ignoreHidden)));
      }
   }
}
