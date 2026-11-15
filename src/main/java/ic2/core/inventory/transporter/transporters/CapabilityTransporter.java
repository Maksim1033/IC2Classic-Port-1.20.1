package ic2.core.inventory.transporter.transporters;

import ic2.core.block.machines.recipes.ItemStackStrategy;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.utils.helpers.StackUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

public class CapabilityTransporter extends BaseTransporter {
   ICapabilityProvider prov;

   public CapabilityTransporter(ICapabilityProvider prov) {
      this.prov = prov;
   }

   @Override
   public int addItem(ItemStack stack, Direction dir, boolean simulate) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         IItemHandler handler = (IItemHandler)this.prov.getCapability(ForgeCapabilities.ITEM_HANDLER, dir).orElse(EmptyHandler.INSTANCE);
         int size = handler.getSlots();
         if (size <= 0) {
            return 0;
         } else {
            int stackSize = stack.getCount();
            IntList emptySlots = new IntArrayList(size);
            int added = 0;

            for (int i = 0; i < size; i++) {
               ItemStack inv = handler.getStackInSlot(i);
               if (inv.isEmpty()) {
                  emptySlots.add(i);
               } else if (StackUtil.isStackEqual(inv, stack)) {
                  int adding = stackSize - added;
                  added += adding - handler.insertItem(i, StackUtil.copyWithSize(stack, adding), simulate).getCount();
                  if (added >= stackSize) {
                     return added;
                  }
               }
            }

            size = emptySlots.size();

            for (int ix = 0; ix < size; ix++) {
               int adding = Math.min(stack.getMaxStackSize(), stackSize - added);
               added += adding - handler.insertItem(emptySlots.getInt(ix), StackUtil.copyWithSize(stack, adding), simulate).getCount();
               if (added >= stackSize) {
                  return added;
               }
            }

            return added;
         }
      }
   }

   @Override
   public ItemStack removeItem(IFilter filter, Direction dir, int amount, boolean simulate) {
      if (amount <= 0) {
         return ItemStack.EMPTY;
      } else {
         IItemHandler handler = (IItemHandler)this.prov.getCapability(ForgeCapabilities.ITEM_HANDLER, dir).orElse(EmptyHandler.INSTANCE);
         ItemStack stack = ItemStack.EMPTY;
         int size = handler.getSlots();
         if (size <= 0) {
            return stack;
         } else {
            for (int i = 0; i < size; i++) {
               ItemStack inv = handler.getStackInSlot(i);
               if (!inv.isEmpty() && filter.matches(inv) && (stack.isEmpty() || StackUtil.isStackEqual(stack, inv))) {
                  if (stack.isEmpty()) {
                     stack = handler.extractItem(i, amount - stack.getCount(), simulate);
                  } else {
                     stack.grow(handler.extractItem(i, amount - stack.getCount(), simulate).getCount());
                  }

                  if (stack.getCount() >= amount) {
                     return stack;
                  }
               }
            }

            return stack;
         }
      }
   }

   @Override
   public int getInventorySize(Direction dir) {
      return ((IItemHandler)this.prov.getCapability(ForgeCapabilities.ITEM_HANDLER, dir).orElse(EmptyHandler.INSTANCE)).getSlots();
   }

   @Override
   public Object2IntMap<ItemStack> getAllItems(Direction dir, boolean compareNBT) {
      IItemHandler handler = (IItemHandler)this.prov.getCapability(ForgeCapabilities.ITEM_HANDLER, dir).orElse(EmptyHandler.INSTANCE);
      int slots = handler.getSlots();
      if (slots <= 0) {
         return Object2IntMaps.emptyMap();
      } else {
         Object2IntLinkedOpenCustomHashMap<ItemStack> items = new Object2IntLinkedOpenCustomHashMap(ItemStackStrategy.getStrategy(compareNBT));

         for (int i = 0; i < slots; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
               items.addTo(StackUtil.copyWithSize(stack, 1), stack.getCount());
            }
         }

         return items;
      }
   }

   @Override
   public IItemTransporter.InvResult getInventory(Direction dir, boolean compareNBT) {
      IItemTransporter.InvResult result = new IItemTransporter.InvResult(compareNBT);
      IItemHandler handler = (IItemHandler)this.prov.getCapability(ForgeCapabilities.ITEM_HANDLER, dir).orElse(EmptyHandler.INSTANCE);
      int slots = handler.getSlots();
      if (slots > 0) {
         for (int i = 0; i < slots; i++) {
            result.add(handler.getStackInSlot(i), handler.getSlotLimit(i));
         }
      }

      return result;
   }
}
