package ic2.jeiplugin.core.transferhandler;

import ic2.core.IC2;
import ic2.core.block.machines.containers.nv.IndustrialWorkbenchContainer;
import ic2.core.networking.buffers.data.NBTBuffer;
import java.util.List;
import java.util.Optional;
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

public class IndustrialWorkbenchTransferHandler implements IRecipeTransferHandler<IndustrialWorkbenchContainer, CraftingRecipe> {
   public Class<IndustrialWorkbenchContainer> getContainerClass() {
      return IndustrialWorkbenchContainer.class;
   }

   public Optional<MenuType<IndustrialWorkbenchContainer>> getMenuType() {
      return Optional.empty();
   }

   public RecipeType<CraftingRecipe> getRecipeType() {
      return RecipeTypes.CRAFTING;
   }

   public IRecipeTransferError transferRecipe(
      IndustrialWorkbenchContainer container, CraftingRecipe recipe, IRecipeSlotsView recipeLayout, Player player, boolean maxTransfer, boolean doTransfer
   ) {
      if (doTransfer) {
         ListTag list = new ListTag();
         List<IRecipeSlotView> slotViews = recipeLayout.getSlotViews(RecipeIngredientRole.INPUT);

         for (int i = 0; i < 9; i++) {
            IRecipeSlotView view = slotViews.get(i);
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("SlotID", i);
            if (view != null && view.getRole() == RecipeIngredientRole.INPUT && !view.isEmpty()) {
               view.getDisplayedIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY).save(nbt);
            }

            list.add(nbt);
         }

         IC2.NETWORKING.get(false).sendClientTileDataBufferEvent(container.getHolder(), "jei", new NBTBuffer("RecipeData", list));
      }

      return null;
   }
}
