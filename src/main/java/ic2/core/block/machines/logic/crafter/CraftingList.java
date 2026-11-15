package ic2.core.block.machines.logic.crafter;

import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.base.INBTSavable;
import ic2.core.inventory.filter.IFilter;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CraftingList implements IHasInventory, INBTSavable {
   int slots;
   CraftRecipe[] recipes;
   NonNullList<ItemStack> displayItems;
   IntSet savedSlots = new IntOpenHashSet();
   IntList nonEmpty = new IntArrayList();

   public CraftingList(int slots) {
      if (slots > 64) {
         throw new ArrayIndexOutOfBoundsException("Crafting Lists can't have more then 64 Recipes stored");
      } else {
         this.slots = slots;
         this.recipes = new CraftRecipe[slots];
         this.displayItems = NonNullList.withSize(slots, ItemStack.EMPTY);
      }
   }

   public CraftingList setSlotsSaved(int from, int to) {
      this.savedSlots.addAll(IntArrayList.wrap(MathUtils.fromTo(from, to)));
      return this;
   }

   public CraftingList setSlotsSaved(int... slots) {
      this.savedSlots.addAll(IntArrayList.wrap(slots));
      return this;
   }

   @Override
   public CompoundTag save(CompoundTag nbt) {
      ListTag list = new ListTag();
      int i = 0;

      for (int m = this.nonEmpty.size(); i < m; i++) {
         int index = this.nonEmpty.getInt(i);
         if (this.recipes[index] != null && (this.savedSlots.isEmpty() || this.savedSlots.contains(index))) {
            CompoundTag data = this.recipes[index].writeRecipe(new CompoundTag());
            data.putInt("list_entry", index);
            list.add(data);
         }
      }

      NBTUtils.put(nbt, "recipes", list);
      return nbt;
   }

   @Override
   public void load(CompoundTag nbt) {
      this.nonEmpty.clear();

      for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("recipes", 10), CompoundTag.class)) {
         int index = data.getInt("list_entry");
         CraftRecipe recipe = new CraftRecipe();
         recipe.readRecipe(data);
         this.recipes[index] = recipe;
         this.nonEmpty.add(index);
      }
   }

   public void validateRecipes(Level world) {
      int i = 0;

      for (int m = this.nonEmpty.size(); i < m; i++) {
         int index = this.nonEmpty.getInt(i);
         if (this.recipes[index] == null) {
            this.nonEmpty.rem(i--);
            m--;
            this.displayItems.set(index, ItemStack.EMPTY);
         } else if (this.recipes[index].validate(world)) {
            this.displayItems.set(index, this.recipes[index].getDisplayItem());
         } else {
            m--;
            this.nonEmpty.rem(i--);
            this.displayItems.set(index, ItemStack.EMPTY);
         }
      }
   }

   public void saveRecipe(int slot, CraftRecipe recipe) {
      if (slot >= 0 && slot < this.slots) {
         this.recipes[slot] = recipe;
         this.displayItems.set(slot, recipe.getDisplayItem());
         if (!this.nonEmpty.contains(slot)) {
            this.nonEmpty.add(slot);
         }
      }
   }

   public void removeRecipe(int slot) {
      if (slot >= 0 && slot < this.slots) {
         this.recipes[slot] = null;
         this.displayItems.set(slot, ItemStack.EMPTY);
         this.nonEmpty.rem(slot);
      }
   }

   public boolean isEmpty() {
      return this.nonEmpty.isEmpty();
   }

   public CraftRecipe getRecipe(int slot) {
      return this.recipes[slot];
   }

   public CraftRecipe getRecipeFrom(IFilter filter) {
      int i = 0;

      for (int m = this.nonEmpty.size(); i < m; i++) {
         int slot = this.nonEmpty.getInt(i);
         if (filter.matches((ItemStack)this.displayItems.get(slot))) {
            return this.recipes[slot];
         }
      }

      return null;
   }

   @Override
   public int getSlotCount() {
      return this.slots;
   }

   @Override
   public ItemStack getStackInSlot(int slot) {
      return (ItemStack)this.displayItems.get(slot);
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      this.displayItems.set(slot, stack);
   }

   @Override
   public int getMaxStackSize(int slot) {
      return 64;
   }

   @Override
   public boolean canInsert(int slot, ItemStack stack) {
      return false;
   }

   @Override
   public boolean canExtract(int slot, ItemStack stack) {
      return false;
   }

   public CraftRecipe getCopy(int index) {
      return this.recipes[index] != null ? this.recipes[index].copy() : null;
   }
}
