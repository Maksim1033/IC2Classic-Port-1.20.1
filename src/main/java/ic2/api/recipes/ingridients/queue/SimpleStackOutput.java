package ic2.api.recipes.ingridients.queue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class SimpleStackOutput implements IStackOutput {
   ItemStack output;
   int slot;

   public SimpleStackOutput(CompoundTag nbt) {
      this.output = ItemStack.of(nbt.getCompound("stack"));
      this.slot = nbt.getInt("slot");
   }

   public SimpleStackOutput(ItemStack output, int slot) {
      this.output = output;
      this.slot = slot;
   }

   @Override
   public boolean addToInventory(IInputter inventory) {
      inventory.addItemIntoSlot(this.slot, this.output);
      return this.output.isEmpty();
   }

   @Override
   public ItemStack getStack() {
      return this.output;
   }

   @Override
   public void save(CompoundTag save) {
      save.putByte("slot", (byte)this.slot);
      save.put("stack", this.output.save(new CompoundTag()));
   }
}
