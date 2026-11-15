package ic2.core.block.base.misc;

import ic2.api.items.IUpgradeItem;
import ic2.api.recipes.ingridients.queue.IStackOutput;
import ic2.api.recipes.ingridients.recipes.IRecipeOutput;
import ic2.api.recipes.registries.IMachineRecipeList;
import ic2.api.tiles.IMachine;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class UpgradeStorage {
   EnumMap<IUpgradeItem.Functions, IntList> function_slots = new EnumMap<>(IUpgradeItem.Functions.class);

   public void clear() {
      this.function_slots.clear();
   }

   public void validate(int slot, EnumSet<IUpgradeItem.Functions> functions) {
      if (functions != null) {
         for (IUpgradeItem.Functions entry : functions) {
            this.function_slots.computeIfAbsent(entry, k -> new IntArrayList()).add(slot);
         }
      }
   }

   public boolean has(IUpgradeItem.Functions function) {
      return this.function_slots.get(function) != null;
   }

   public boolean match(IUpgradeItem.Functions main, IUpgradeItem.Functions other) {
      return Objects.equals(this.function_slots.get(main), this.function_slots.get(other));
   }

   public void onTick(NonNullList<ItemStack> items, IMachine machine) {
      IntList list = this.function_slots.get(IUpgradeItem.Functions.TICK);
      if (list != null) {
         int i = 0;

         for (int m = list.size(); i < m; i++) {
            ItemStack stack = (ItemStack)items.get(list.getInt(i));
            if (stack.getItem() instanceof IUpgradeItem) {
               ((IUpgradeItem)stack.getItem()).onTick(stack, machine);
            }
         }
      }
   }

   public void onRecipeFinished(NonNullList<ItemStack> items, IMachine machine) {
      IntList list = this.function_slots.get(IUpgradeItem.Functions.RECIPE);
      if (list != null) {
         int i = 0;

         for (int m = list.size(); i < m; i++) {
            ItemStack stack = (ItemStack)items.get(list.getInt(i));
            if (stack.getItem() instanceof IUpgradeItem) {
               ((IUpgradeItem)stack.getItem()).onMachineProcessed(stack, machine);
            }
         }
      }
   }

   public void onRecipeFinishedPre(NonNullList<ItemStack> items, IMachine machine, IRecipeOutput output, CompoundTag recipeData) {
      IntList list = this.function_slots.get(IUpgradeItem.Functions.RECIPE);
      if (list != null) {
         int i = 0;

         for (int m = list.size(); i < m; i++) {
            ItemStack stack = (ItemStack)items.get(list.getInt(i));
            if (stack.getItem() instanceof IUpgradeItem) {
               ((IUpgradeItem)stack.getItem()).onMachineFinishedRecipePre(stack, machine, output, recipeData);
            }
         }
      }
   }

   public void onRecipeFinishedPost(NonNullList<ItemStack> items, IMachine machine, IMachineRecipeList.RecipeEntry entry, List<IStackOutput> drops) {
      IntList list = this.function_slots.get(IUpgradeItem.Functions.RECIPE);
      if (list != null) {
         int i = 0;

         for (int m = list.size(); i < m; i++) {
            ItemStack stack = (ItemStack)items.get(list.getInt(i));
            if (stack.getItem() instanceof IUpgradeItem) {
               ((IUpgradeItem)stack.getItem()).onMachineFinishedRecipePost(stack, machine, entry, drops);
            }
         }
      }
   }
}
