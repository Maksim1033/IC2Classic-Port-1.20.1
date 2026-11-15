package ic2.jeiplugin.core.transferhandler;

import ic2.core.IC2;
import ic2.core.item.inv.container.CraftingUpgradeContainer;
import ic2.core.networking.buffers.data.NBTBuffer;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

public class CraftingUpgradeTransferHandler implements IRecipeTransferHandler<CraftingUpgradeContainer, CraftingRecipe> {
   public Class<CraftingUpgradeContainer> getContainerClass() {
      return CraftingUpgradeContainer.class;
   }

   public IRecipeTransferError transferRecipe(
      CraftingUpgradeContainer container, CraftingRecipe recipe, IRecipeSlotsView recipeLayout, Player player, boolean maxTransfer, boolean doTransfer
   ) {
      if (doTransfer) {
         List<IRecipeSlotView> slotViews = recipeLayout.getSlotViews(RecipeIngredientRole.INPUT);
         Map<ItemStack, IntList> slots = CollectionUtils.<ItemStack, IntList>createLinkedMap();

         for (int i = 0; i < 9; i++) {
            IRecipeSlotView view = slotViews.get(i);
            if (view != null && !view.isEmpty()) {
               this.getSlot(slots, view.getDisplayedIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY)).add(i);
            }
         }

         ListTag list = new ListTag();

         for (Entry<ItemStack, IntList> entry : slots.entrySet()) {
            CompoundTag data = entry.getKey().save(new CompoundTag());
            data.putIntArray("slots", entry.getValue().toIntArray());
            list.add(data);
         }

         IC2.NETWORKING.get(false).sendClientItemBuffer(new ItemStack(IC2Items.CRAFTING_UPGRADE), "jei", new NBTBuffer("RecipeItems", list));
      }

      return null;
   }

   public IntList getSlot(Map<ItemStack, IntList> slots, ItemStack stack) {
      for (Entry<ItemStack, IntList> entry : slots.entrySet()) {
         if (StackUtil.isStackEqual(entry.getKey(), stack) && entry.getKey().getCount() == stack.getCount()) {
            return entry.getValue();
         }
      }

      IntList set = new IntArrayList();
      slots.put(stack, set);
      return set;
   }

   public Optional<MenuType<CraftingUpgradeContainer>> getMenuType() {
      return Optional.empty();
   }

   public RecipeType<CraftingRecipe> getRecipeType() {
      return RecipeTypes.CRAFTING;
   }
}
