package ic2.core.block.machines.logic.crafter;

import ic2.api.network.buffer.IInputBuffer;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.api.network.buffer.IOutputBuffer;
import ic2.core.IC2;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.IngredientNBTFilter;
import ic2.core.inventory.inv.IC2CraftingInventory;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.NBTUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Iterator;
import java.util.LinkedList;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;

public class CraftRecipe implements INetworkDataBuffer {
   static final CraftingContainer CRAFTING = new IC2CraftingInventory(3, 3);
   CraftingRecipe recipe;
   NonNullList<ItemStack> displayItems = NonNullList.withSize(9, ItemStack.EMPTY);
   ItemStack output = ItemStack.EMPTY;
   boolean isValid = false;
   public int flags = 4;
   int[] toDrain = new int[9];
   Object2ObjectMap<IFilter, int[]> filters = null;
   Object2ObjectMap<IFilter, ItemStack> displayFilters = null;

   public void consumeItems(int slot, int amount) {
      this.toDrain[slot] = this.toDrain[slot] + amount;
   }

   public int[] getToConsume() {
      int[] oldArray = this.toDrain;
      this.toDrain = new int[9];
      return oldArray;
   }

   public boolean validate(Level world) {
      this.recipe = this.getRecipe(world);
      this.isValid = this.recipe != null;
      if (this.isValid) {
         this.output = this.recipe.getResultItem(world.registryAccess()).copy();
         this.getFilters();
         return true;
      } else {
         return false;
      }
   }

   public boolean isValid() {
      return this.recipe != null && this.isValid;
   }

   public Recipe<?> getRecipe() {
      return this.recipe;
   }

   public ItemStack getFilter(IFilter filter) {
      return this.displayFilters == null ? ItemStack.EMPTY : (ItemStack)this.displayFilters.getOrDefault(filter, ItemStack.EMPTY);
   }

   public Object2ObjectMap<IFilter, int[]> getFilters() {
      if (this.filters == null) {
         this.filters = CollectionUtils.<IFilter, int[]>createLinkedMap();
         this.displayFilters = CollectionUtils.createMap();
         if (this.recipe != null) {
            boolean nbt = (this.flags & 16) != 0;
            if (this.recipe instanceof IShapedRecipe<?> shaped) {
               int width = shaped.getRecipeWidth();
               NonNullList<Ingredient> items = shaped.getIngredients();
               boolean done = false;
               boolean success = false;

               for (int xOff = 0; xOff < 3 && !done; xOff++) {
                  for (int yOff = 0; yOff < 3 && !done; yOff++) {
                     if (((Ingredient)items.get(0)).test((ItemStack)this.displayItems.get(yOff * 3 + xOff))) {
                        boolean retry = false;

                        for (int i = 0; i < items.size(); i++) {
                           int xSlot = i % width + xOff;
                           int ySlot = i / width + yOff;
                           if (xSlot < 0 || xSlot >= 3 || ySlot < 0 || ySlot >= 3) {
                              retry = true;
                              break;
                           }

                           ItemStack item = ((ItemStack)this.displayItems.get(xSlot + ySlot * 3)).copy();
                           if (!((Ingredient)items.get(i)).test(item)) {
                              retry = true;
                              break;
                           }

                           if (!item.isEmpty()) {
                              IFilter filter = new IngredientNBTFilter((Ingredient)items.get(i), item, false);
                              this.filters.put(filter, new int[]{xSlot + ySlot * 3, item.getCount()});
                              this.displayFilters.put(filter, item);
                           }
                        }

                        if (!retry) {
                           done = true;
                           success = true;
                        }
                     }
                  }
               }

               if (!success) {
                  this.filters.clear();
                  this.displayFilters.clear();
               }
            } else {
               LinkedList<Ingredient> items = new LinkedList<>(this.recipe.getIngredients());

               for (int i = 0; i < 9; i++) {
                  if (!((ItemStack)this.displayItems.get(i)).isEmpty()) {
                     Iterator<Ingredient> iter = items.iterator();

                     while (iter.hasNext()) {
                        Ingredient ingredient = iter.next();
                        if (ingredient.test((ItemStack)this.displayItems.get(i))) {
                           ItemStack stack = ((ItemStack)this.displayItems.get(i)).copy();
                           IFilter filter = new IngredientNBTFilter(ingredient, stack, nbt);
                           this.filters.put(filter, new int[]{i, stack.getCount()});
                           this.displayFilters.put(filter, stack);
                           iter.remove();
                           break;
                        }
                     }
                  }
               }

               if (!items.isEmpty()) {
                  this.filters.clear();
                  this.displayFilters.clear();
               }
            }
         }
      }

      return this.filters;
   }

   public ItemStack getDisplayItem() {
      return this.output;
   }

   public ItemStack getOutput(CraftingContainer inventory, Level world) {
      return this.recipe != null && this.recipe.matches(inventory, world) ? this.recipe.assemble(inventory, world.registryAccess()).copy() : ItemStack.EMPTY;
   }

   public ItemStack getFakeOutput(Level world) {
      for (int i = 0; i < 9; i++) {
         CRAFTING.setItem(i, ((ItemStack)this.displayItems.get(i)).copy());
      }

      ItemStack output = this.recipe.assemble(CRAFTING, world.registryAccess()).copy();
      CRAFTING.clearContent();
      return output;
   }

   public NonNullList<ItemStack> getRemainingItems(CraftingContainer inventory, Level world) {
      return this.recipe != null && this.recipe.matches(inventory, world)
         ? this.recipe.getRemainingItems(inventory)
         : NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
   }

   protected CraftingRecipe getRecipe(Level world) {
      if (!this.isValid) {
         return null;
      } else {
         for (int i = 0; i < 9; i++) {
            CRAFTING.setItem(i, ((ItemStack)this.displayItems.get(i)).copy());
         }

         return this.recipe != null && this.recipe.matches(CRAFTING, world)
            ? this.recipe
            : (CraftingRecipe)IC2.PLATFORM.getRecipes().getRecipeFor(RecipeType.CRAFTING, CRAFTING, world).orElse(null);
      }
   }

   public void applyTo(IHasInventory inventory, Level world) {
      for (int i = 0; i < 9; i++) {
         inventory.setStackInSlot(i, ((ItemStack)this.displayItems.get(i)).copy());
      }

      inventory.setStackInSlot(9, this.getFakeOutput(world).copy());
   }

   public void setRecipe(CraftingRecipe recipe, IHasInventory inv) {
      for (int i = 0; i < 9; i++) {
         this.displayItems.set(i, inv.getStackInSlot(i).copy());
      }

      this.recipe = recipe;
      this.isValid = true;
      this.filters = null;
      this.displayFilters = null;
      net.minecraft.core.RegistryAccess registryAccess = (inv instanceof ic2.core.block.base.tiles.BaseTileEntity bte && bte.getLevel() != null)
         ? bte.getLevel().registryAccess()
         : net.minecraft.core.RegistryAccess.EMPTY;
      this.output = recipe.getResultItem(registryAccess).copy();
   }

   public void clear() {
      for (int i = 0; i < 9; i++) {
         this.displayItems.set(i, ItemStack.EMPTY);
      }

      this.displayFilters = null;
      this.filters = null;
      this.recipe = null;
      this.output = ItemStack.EMPTY;
      this.isValid = false;
   }

   public void clearFilters() {
      this.filters = null;
      this.displayFilters = null;
   }

   public CraftRecipe copy() {
      CraftRecipe newRecipe = new CraftRecipe();
      newRecipe.readRecipe(this.writeRecipe(new CompoundTag()).copy());
      return newRecipe;
   }

   public CompoundTag writeRecipe(CompoundTag nbt) {
      nbt.putBoolean("valid", this.isValid);
      nbt.putString("recipe", this.recipe == null ? "empty" : this.recipe.getId().toString());
      nbt.putByte("flags", (byte)this.flags);
      if (!this.isValid) {
         return nbt;
      } else {
         NBTUtils.saveItems(nbt, "items", this.displayItems);
         return nbt;
      }
   }

   public void readRecipe(CompoundTag nbt) {
      this.isValid = nbt.getBoolean("valid");
      String recipeID = nbt.getString("recipe");
      this.flags = nbt.getInt("flags");
      this.recipe = null;
      if (!recipeID.equalsIgnoreCase("empty")) {
         this.recipe = (CraftingRecipe)IC2.PLATFORM.getRecipes().byKey(new ResourceLocation(recipeID)).orElse(null);
      }

      this.displayItems.clear();
      if (this.isValid) {
         NBTUtils.loadItems(nbt, "items", this.displayItems);
      }
   }

   @Override
   public void write(IOutputBuffer buffer) {
      buffer.writeByte((byte)this.flags);
   }

   @Override
   public void read(IInputBuffer buffer) {
      this.flags = buffer.readByte();
   }
}
