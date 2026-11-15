package ic2.core.item.inv.inventory;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.api.reactor.IReactorProduct;
import ic2.core.inventory.base.IHasHeldGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.inv.PortableInventory;
import ic2.core.item.inv.container.NuclearJetpackContainer;
import ic2.core.item.wearable.base.IC2ElectricJetpackBase;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class NuclearJetpackInventory extends PortableInventory implements IReactor {
   public NuclearJetpackInventory(Player owner, IHasHeldGui held, ItemStack stack, Slot slot) {
      super(owner, held, stack, slot);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new NuclearJetpackContainer(this, player, this.getID(), windowID);
   }

   @Override
   public int getSlotCount() {
      return 25;
   }

   @Override
   public CompoundTag getNBTData(ItemStack stack, boolean create) {
      CompoundTag data = super.getNBTData(stack, create);
      if (!(stack.getItem() instanceof IC2ElectricJetpackBase)) {
         CompoundTag subData = data.getCompound("jetpack_data");
         if (create) {
            data.put("jetpack_data", subData);
         }

         return subData;
      } else {
         return data;
      }
   }

   @Override
   public int getHeat() {
      return 0;
   }

   @Override
   public void setHeat(int newHeat) {
   }

   @Override
   public void addHeat(int heat) {
   }

   @Override
   public int getMaxHeat() {
      return 0;
   }

   @Override
   public void setMaxHeat(int heat) {
   }

   @Override
   public float getHeatEffectModifier() {
      return 0.0F;
   }

   @Override
   public void setHeatEffectModifier(float hem) {
   }

   @Override
   public double getEnergyOutput() {
      return 0.0;
   }

   @Override
   public void addOutput(float output) {
   }

   @Override
   public ItemStack getStackInReactor(int x, int y) {
      return x >= 0 && x <= 4 && y >= 0 && y <= 4 ? (ItemStack)this.inventory.get(x + y * 5) : ItemStack.EMPTY;
   }

   @Override
   public void setStackInReactor(int x, int y, ItemStack stack) {
      if (x >= 0 && x <= 4 && y >= 0 && y <= 4) {
         this.inventory.set(x + y * 5, stack);
      }
   }

   @Override
   public void explode() {
   }

   @Override
   public int getTickRate() {
      return 0;
   }

   @Override
   public boolean isProducingEnergy() {
      return false;
   }

   public boolean isUsefulItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         Item item = stack.getItem();
         if (item instanceof IReactorComponent) {
            return true;
         } else {
            return item instanceof IReactorProduct ? ((IReactorProduct)item).isValidForReactor(stack, this) : false;
         }
      }
   }
}
