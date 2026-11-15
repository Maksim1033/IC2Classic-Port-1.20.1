package ic2.api.recipes.ingridients.queue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class MultiStackOutput implements IStackOutput {
   ItemStack output;
   int[] slots;

   public MultiStackOutput(CompoundTag nbt) {
      this.output = ItemStack.of(nbt.getCompound("stack"));
      this.slots = nbt.getIntArray("slots");
   }

   public MultiStackOutput(ItemStack output, int... slots) {
      this.output = output;
      this.slots = slots;
   }

   @Override
   public boolean addToInventory(IInputter inventory) {
      for (int slot : this.slots) {
         inventory.addItemIntoSlot(slot, this.output);
         if (this.output.isEmpty()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public ItemStack getStack() {
      return this.output;
   }

   @Override
   public void save(CompoundTag save) {
      save.putIntArray("slots", this.slots);
      save.put("stack", this.output.save(new CompoundTag()));
   }
}
