package ic2.core.entity.minecarts;

import ic2.api.items.electric.ElectricItem;
import ic2.api.tiles.IEnergyStorage;
import ic2.core.IC2;
import ic2.core.entity.inv.container.EnergyMinecartContainer;
import ic2.core.inventory.base.IHasGui;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.gui.ComponentContainerScreen;
import ic2.core.inventory.inv.IHasItemHandler;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.NBTUtils;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.vehicle.AbstractMinecart.Type;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public abstract class ElectricMinecart extends Minecart implements IEnergyStorage, IHasGui, IHasInventory {
   public int storedEU;
   NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
   private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new IHasItemHandler(this));

   public ElectricMinecart(EntityType<?> type, Level level) {
      super(type, level);
   }

   public ElectricMinecart(EntityType<?> type, Level level, double x, double y, double z) {
      super(type, level);
      this.setPos(x, y, z);
      this.xo = x;
      this.yo = y;
      this.zo = z;
   }

   public InteractionResult interact(Player player, InteractionHand hand) {
      return IC2.PLATFORM.launchGui(player, hand, null, this) ? InteractionResult.SUCCESS : InteractionResult.PASS;
   }

   public Type getMinecartType() {
      return Type.CHEST;
   }

   public abstract BlockState getDefaultDisplayBlockState();

   public ItemStack getPickResult() {
      return new ItemStack(this.getDropItem());
   }

   protected abstract Item getDropItem();

   @Override
   public int getStoredEU() {
      return this.storedEU;
   }

   @Override
   public int addEnergy(int power) {
      int added = Math.min(this.getMaxEU() - this.storedEU, power);
      this.storedEU += added;
      return added;
   }

   @Override
   public int drawEnergy(int power) {
      int draw = Math.min(this.storedEU, power);
      this.storedEU -= draw;
      return draw;
   }

   @Override
   public int getSlotCount() {
      return 2;
   }

   @Override
   public ItemStack getStackInSlot(int slot) {
      return (ItemStack)this.inventory.get(slot);
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      this.inventory.set(slot, stack);
   }

   @Override
   public int getMaxStackSize(int slot) {
      return 64;
   }

   @Override
   public boolean canInsert(int slot, ItemStack stack) {
      return slot == 0
         ? ElectricItemFilter.CHARGE_FILTER.matches(stack)
         : ElectricItemFilter.DISCHARGE_EXTERNAL_FILTER.matches(stack)
            || stack.is(Items.REDSTONE)
            || stack.is(Items.REDSTONE_BLOCK)
            || stack.is(IC2Items.SU_BATTERY);
   }

   @Override
   public boolean canExtract(int slot, ItemStack stack) {
      return slot == 0 ? ElectricItemFilter.NOT_CHARGE_FILTER.matches(stack) : ElectricItemFilter.NOT_DISCHARGE_FILTER.matches(stack);
   }

   public int getGuiOffset() {
      return 0;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new EnergyMinecartContainer(this, player, windowID);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public Screen createGui(Player player, InteractionHand hand, Direction side, IC2Container container) {
      return new ComponentContainerScreen((ContainerComponent<?>)container);
   }

   @Override
   public boolean canInteractWith(Player player) {
      return !this.isRemoved() && player.isAlive();
   }

   protected void addAdditionalSaveData(CompoundTag nbt) {
      super.addAdditionalSaveData(nbt);
      NBTUtils.saveItems(nbt, this.inventory);
      NBTUtils.putInt(nbt, "energy", this.storedEU, 0);
   }

   protected void readAdditionalSaveData(CompoundTag nbt) {
      super.readAdditionalSaveData(nbt);
      NBTUtils.loadItems(nbt, this.inventory);
      this.storedEU = NBTUtils.getInt(nbt, "energy", 0);
   }

   public void remove(RemovalReason reason) {
      if (!this.level().isClientSide && reason.shouldDestroy()) {
         Containers.dropContents(this.level(), this.blockPosition(), this.inventory);
      }

      super.remove(reason);
   }

   public void tick() {
      super.tick();
      if (!((ItemStack)this.inventory.get(0)).isEmpty()) {
         this.drawEnergy(ElectricItem.MANAGER.charge((ItemStack)this.inventory.get(0), this.storedEU, this.getTier(), false, false));
      }

      if (!((ItemStack)this.inventory.get(1)).isEmpty()) {
         ItemStack fuel = (ItemStack)this.inventory.get(1);
         Item item = fuel.getItem();
         if (item == Items.REDSTONE) {
            this.addEnergy(500);
            fuel.shrink(1);
         } else if (item == Items.REDSTONE_BLOCK) {
            this.addEnergy(5000);
            fuel.shrink(1);
         } else if (item == IC2Items.SU_BATTERY) {
            this.addEnergy(1000);
            fuel.shrink(1);
         } else {
            this.addEnergy(ElectricItem.MANAGER.discharge(fuel, this.getMaxEU() - this.storedEU, this.getTier(), false, true, false));
         }
      }
   }

   public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
      return this.isAlive() && capability == ForgeCapabilities.ITEM_HANDLER ? this.itemHandler.cast() : super.getCapability(capability, facing);
   }

   public void invalidateCaps() {
      super.invalidateCaps();
      this.itemHandler.invalidate();
   }

   public void reviveCaps() {
      super.reviveCaps();
      this.itemHandler = LazyOptional.of(() -> new IHasItemHandler(this));
   }

   protected void applyNaturalSlowdown() {
      float f = 0.98F + (float)(15 - (int)((float)this.storedEU / (float)this.getMaxEU() * 15.0F)) * 0.001F;
      if (this.isInWater()) {
         f *= 0.95F;
      }

      this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.0, (double)f));
   }
}
