package ic2.core.item.inv.inventory;

import ic2.api.items.ITagBlock;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.PortableInventory;
import ic2.core.item.inv.container.ScannerContainer;
import ic2.core.platform.registries.ForgeRegistryWrapper;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ScannerInventory extends PortableInventory {
   public ScannerInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot) {
      super(owner, held, stack, slot);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ScannerContainer(this, player, this.getID(), windowID);
   }

   @Override
   public int getSlotCount() {
      return 9;
   }

   @Override
   public void load(CompoundTag nbt) {
      for (CompoundTag data : NBTListWrapper.wrap(nbt.getList("blocks", 10), CompoundTag.class)) {
         int slot = data.getInt("slot");
         if (slot >= 0 && slot < 9) {
            if (data.getBoolean("isTag")) {
               this.inventory.set(slot, ItemStack.of(data));
            } else {
               Block block = ForgeRegistryWrapper.getBlockValue(new ResourceLocation(data.getString("block")));
               if (block != Blocks.AIR) {
                  this.inventory.set(slot, new ItemStack(block));
               }
            }
         }
      }
   }

   @Override
   public void save(CompoundTag nbt) {
      ListTag list = new ListTag();

      for (int i = 0; i < 9; i++) {
         ItemStack stack = (ItemStack)this.inventory.get(i);
         if (!stack.isEmpty()) {
            if (stack.getItem() instanceof ITagBlock) {
               CompoundTag data = stack.save(new CompoundTag());
               data.putByte("slot", (byte)i);
               data.putBoolean("isTag", true);
               list.add(data);
            } else {
               CompoundTag data = new CompoundTag();
               data.putString("block", ForgeRegistryWrapper.getBlockKey(Block.byItem(stack.getItem())).toString());
               data.putByte("slot", (byte)i);
               list.add(data);
            }
         }
      }

      NBTUtils.put(nbt, "blocks", list);
   }
}
