package ic2.core.item.inv.inventory;

import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.PortableInventory;
import ic2.core.item.inv.container.ImportExportContainer;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ImportExportInventory extends PortableInventory implements ContainerData {
   public int direction;
   public int durability;
   public int flags;
   public boolean whiteList;
   public int transfer;

   public ImportExportInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot) {
      super(owner, held, stack, slot);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ImportExportContainer(this, player, this.getID(), windowID);
   }

   public void onDataReceived(int key, int value) {
      switch (key) {
         case 0:
            this.direction = Mth.clamp(value == this.direction ? -1 : value, -1, 5);
            break;
         case 1:
            this.durability = Mth.clamp(value, -1, 10);
            break;
         case 2:
            this.flags ^= value;
            if (value == 256) {
               this.durability = (this.flags & 256) != 0 ? 0 : -1;
            }
            break;
         case 3:
            this.whiteList = value == 1;
            break;
         case 4:
            this.transfer = Mth.clamp(value, 1, 64);
      }

      this.markDirty();
   }

   @Override
   public void load(CompoundTag nbt) {
      NBTUtils.loadItems(nbt, "filters", this.inventory);
      this.direction = NBTUtils.getInt(nbt, "facing", -1);
      this.flags = nbt.getInt("filter_flags");
      this.durability = (this.flags & 256) != 0 ? NBTUtils.getInt(nbt, "filter_durability", -1) : -1;
      this.whiteList = NBTUtils.getBoolean(nbt, "filter_inverted", true);
      this.transfer = NBTUtils.getInt(nbt, "transfer_size", 64);
   }

   @Override
   public void save(CompoundTag nbt) {
      NBTUtils.saveItems(nbt, "filters", this.inventory);
      NBTUtils.putInt(nbt, "facing", this.direction, -1);
      NBTUtils.putInt(nbt, "filter_flags", this.flags, 0);
      NBTUtils.putInt(nbt, "filter_durability", this.durability, -1);
      NBTUtils.putBoolean(nbt, "filter_inverted", this.whiteList, true);
      NBTUtils.putInt(nbt, "transfer_size", this.transfer, 64);
   }

   @Override
   public int getSlotCount() {
      return 9;
   }

   public int get(int index) {
      switch (index) {
         case 0:
            return this.direction;
         case 1:
            return this.durability;
         case 2:
            return this.flags;
         case 3:
            return this.whiteList ? 1 : 0;
         case 4:
            return this.transfer;
         default:
            return 0;
      }
   }

   public void set(int index, int value) {
      switch (index) {
         case 0:
            this.direction = value;
            break;
         case 1:
            this.durability = value;
            break;
         case 2:
            this.flags = value;
            break;
         case 3:
            this.whiteList = value == 1;
            break;
         case 4:
            this.transfer = value;
      }
   }

   public int getCount() {
      return 5;
   }
}
