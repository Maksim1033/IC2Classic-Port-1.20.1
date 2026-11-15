package ic2.core.item.inv.inventory;

import ic2.core.IC2;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.IC2CraftingInventory;
import ic2.core.inventory.inv.PortableInventory;
import ic2.core.item.inv.container.CraftingUpgradeContainer;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;

public class CraftingUpgradeInventory extends PortableInventory implements ContainerData {
   static final CraftingContainer CRAFTING = new IC2CraftingInventory(3, 3);
   Object2ObjectMap<ItemStack, IntList> mappedItems;
   public int flags = 4;
   public boolean valid = false;
   public int direction;
   int typeCount = 0;
   Component output;
   String recipeId;

   public CraftingUpgradeInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot) {
      super(owner, held, stack, slot);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new CraftingUpgradeContainer(this, player, this.getID(), windowID);
   }

   public int get(int index) {
      return index == 0 ? this.flags : this.direction;
   }

   public void set(int index, int value) {
      if (index == 0) {
         this.flags = value;
      } else {
         this.direction = value;
      }
   }

   public int getCount() {
      return 2;
   }

   @Override
   public int getSlotCount() {
      return 10;
   }

   @Override
   public void load(CompoundTag nbt) {
      super.load(nbt);
      this.mappedItems = CollectionUtils.createMap();
      this.valid = nbt.getBoolean("valid");
      this.flags = nbt.getInt("flags");
      this.direction = nbt.getInt("facing");
      if (this.valid) {
         for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("mappedItems", 10), CompoundTag.class)) {
            ItemStack stack = ItemStack.of(data);
            if (!stack.isEmpty()) {
               this.mappedItems.put(stack, IntArrayList.wrap(data.getIntArray("slots")));
            }
         }

         this.recipeId = nbt.getString("recipeId");
         this.output = Serializer.fromJson(nbt.getString("output"));
         this.typeCount = nbt.getInt("typeCount");
      }
   }

   @Override
   public void save(CompoundTag nbt) {
      super.save(nbt);
      nbt.putBoolean("valid", this.valid);
      nbt.putInt("flags", this.flags);
      nbt.putInt("facing", this.direction);
      if (!this.valid) {
         nbt.remove("mappedItems");
         nbt.remove("recipeId");
         nbt.remove("output");
         nbt.remove("typeCount");
      } else {
         ListTag list = new ListTag();
         ObjectIterator var3 = Object2ObjectMaps.fastIterable(this.mappedItems).iterator();

         while (var3.hasNext()) {
            Entry<ItemStack, IntList> entry = (Entry<ItemStack, IntList>)var3.next();
            CompoundTag data = ((ItemStack)entry.getKey()).save(new CompoundTag());
            data.putIntArray("slots", ((IntList)entry.getValue()).toIntArray());
            list.add(data);
         }

         nbt.put("mappedItems", list);
         nbt.putString("recipeId", this.recipeId);
         nbt.putString("output", Serializer.toJson(this.output));
         nbt.putByte("typeCount", (byte)this.typeCount);
      }
   }

   public void injectRecipe(ListTag list, Player player) {
      this.valid = false;
      this.inventory.clear();
      this.recipeId = null;

      for (CompoundTag data : NBTListWrapper.wrap(list, CompoundTag.class)) {
         ItemStack stack = ItemStack.of(data);
         if (!stack.isEmpty()) {
            int[] slots = data.getIntArray("slots");

            for (int i : slots) {
               this.inventory.set(i, stack.copy());
            }
         }
      }

      this.validateRecipe(player);
   }

   public void validateRecipe(Player player) {
      this.valid = false;
      this.typeCount = 0;
      this.mappedItems.clear();
      CRAFTING.clearContent();

      for (int i = 0; i < 9; i++) {
         CRAFTING.setItem(i, ((ItemStack)this.inventory.get(i)).copy());
      }

      ForgeHooks.setCraftingPlayer(player);
      CraftingRecipe recipe = (CraftingRecipe)IC2.PLATFORM.getRecipes().getRecipeFor(RecipeType.CRAFTING, CRAFTING, player.level()).orElse(null);
      this.inventory.set(9, recipe == null ? ItemStack.EMPTY : recipe.assemble(CRAFTING, player.level().registryAccess()).copy());
      ForgeHooks.setCraftingPlayer(null);
      if (((ItemStack)this.inventory.get(9)).isEmpty()) {
         this.output = null;
         this.recipeId = null;
      } else {
         for (int i = 0; i < 9; i++) {
            this.addSlot(i);
         }

         this.calculateData();
         this.output = ((ItemStack)this.inventory.get(9)).getHoverName();
         this.recipeId = recipe.getId().toString();
         this.valid = true;
         this.markDirty();
      }
   }

   public void setFacing(int index) {
      if (this.direction != index) {
         this.direction = index;
         this.markDirty();
      }
   }

   public void setFlag(int index) {
      this.flags ^= index;
      this.markDirty();
   }

   public void clearResult() {
      this.valid = false;
      this.inventory.set(9, ItemStack.EMPTY);
   }

   protected void addSlot(int index) {
      ItemStack stack = (ItemStack)this.inventory.get(index);
      if (!stack.isEmpty()) {
         ObjectIterator var3 = Object2ObjectMaps.fastIterable(this.mappedItems).iterator();

         while (var3.hasNext()) {
            Entry<ItemStack, IntList> entry = (Entry<ItemStack, IntList>)var3.next();
            if (StackUtil.isStackEqual((ItemStack)entry.getKey(), stack) && ((ItemStack)entry.getKey()).getCount() == stack.getCount()) {
               ((IntList)entry.getValue()).add(index);
               return;
            }
         }

         this.mappedItems.put(stack, IntArrayList.wrap(new int[]{index}));
      }
   }

   protected void calculateData() {
      Object2IntMap<ItemStack> result = new Object2IntLinkedOpenHashMap();

      for (int i = 0; i < 9; i++) {
         ItemStack stack = (ItemStack)this.inventory.get(i);
         if (!stack.isEmpty()) {
            boolean found = false;
            ObjectIterator var5 = Object2IntMaps.fastIterable(result).iterator();

            while (var5.hasNext()) {
               it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<ItemStack> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<ItemStack>)var5.next();
               if (StackUtil.isStackEqual((ItemStack)entry.getKey(), stack)) {
                  found = true;
                  entry.setValue(entry.getIntValue() + stack.getCount());
                  break;
               }
            }

            if (!found) {
               result.put(stack, stack.getCount());
            }
         }
      }

      this.typeCount = result.size();
   }
}
