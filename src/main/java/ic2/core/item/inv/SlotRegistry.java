package ic2.core.item.inv;

import ic2.core.block.machines.containers.mv.ReactorPlannerContainer;
import ic2.core.block.machines.containers.nv.ArmorConfiguratorContainer;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.base.IPortableInventory;
import ic2.core.inventory.slot.SlotBase;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.StackUtil;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SlotRegistry {
   public static final SlotRegistry INSTANCE = new SlotRegistry();
   List<BiPredicate<AbstractContainerMenu, Slot>> invalidators = CollectionUtils.<BiPredicate<AbstractContainerMenu, Slot>>createList();

   private SlotRegistry() {
      this.registerInvalidator(new SlotRegistry.PortableInventoryInvalidator());
      this.registerInvalidator(new SlotRegistry.ContainerInvalidator(ArmorConfiguratorContainer.class));
      this.registerInvalidator(new SlotRegistry.ContainerInvalidator(ReactorPlannerContainer.class));
   }

   public void registerInvalidator(BiPredicate<AbstractContainerMenu, Slot> invalidator) {
      this.invalidators.add(invalidator);
   }

   public boolean isInvalidSlot(AbstractContainerMenu container, Slot slot) {
      int i = 0;

      for (int m = this.invalidators.size(); i < m; i++) {
         if (this.invalidators.get(i).test(container, slot)) {
            return true;
         }
      }

      return false;
   }

   public static class ContainerInvalidator implements BiPredicate<AbstractContainerMenu, Slot> {
      Class<? extends AbstractContainerMenu> clz;

      public ContainerInvalidator(Class<? extends AbstractContainerMenu> clz) {
         this.clz = clz;
      }

      public boolean test(AbstractContainerMenu arg0, Slot arg1) {
         return this.clz.isInstance(arg0);
      }
   }

   static class PortableInventoryInvalidator implements BiPredicate<AbstractContainerMenu, Slot> {
      public boolean test(AbstractContainerMenu t, Slot u) {
         if (u instanceof SlotBase slot && slot.getInventory() instanceof IPortableInventory) {
            return true;
         }

         ItemStack stack = u.getItem();
         return !(stack.getItem() instanceof IHasHeldGui) ? false : StackUtil.getNbtData(stack).contains("GUI_ID");
      }
   }
}
