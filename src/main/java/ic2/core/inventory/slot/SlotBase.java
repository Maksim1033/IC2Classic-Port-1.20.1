package ic2.core.inventory.slot;

import com.mojang.datafixers.util.Pair;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.inv.INotifyInventory;
import ic2.core.platform.corehacks.mixins.server.SlotMixin;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SlotBase extends Slot {
   public static final Container EMPTY_INVENTORY = new SimpleContainer(0);
   protected Pair<ResourceLocation, ResourceLocation> backgroundPair;
   protected IHasInventory inventory;
   protected int slot;
   protected int lastCount = 0;
   protected boolean markQuickCraft = false;

   public SlotBase(IHasInventory inv, int index, int xPosition, int yPosition) {
      super(EMPTY_INVENTORY, index, xPosition, yPosition);
      this.inventory = inv;
      this.slot = index;
   }

   public IHasInventory getInventory() {
      return this.inventory;
   }

   public ItemStack getItem() {
      return this.getInventory().getStackInSlot(this.slot);
   }

   public int getMaxStackSize() {
      return this.getInventory().getMaxStackSize(this.slot);
   }

   public void setChanged() {
      IHasInventory inventory = this.getInventory();
      if (inventory instanceof INotifyInventory) {
         ((INotifyInventory)inventory).onNotify(inventory, this.slot);
      }
   }

   public ItemStack remove(int amount) {
      ItemStack stack = this.getInventory().getStackInSlot(this.slot);
      if (stack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack split = stack.split(amount);
         this.getInventory().setStackInSlot(this.slot, stack);
         return split;
      }
   }

   public void set(ItemStack stack) {
      this.lastCount = stack.getCount();
      this.getInventory().setStackInSlot(this.slot, stack);
      int max = this.inventory.getMaxStackSize(this.slot);
      if (stack.getCount() > max) {
         stack.setCount(max);
      }

      if (this.markQuickCraft) {
         this.markQuickCraft = false;
         this.setChanged();
      }
   }

   public void m_219996_(ItemStack stack) {
      this.set(stack);
   }

   public void markQuickCraft() {
      this.markQuickCraft = true;
   }

   public boolean isSameInventory(Slot other) {
      return other instanceof SlotBase && ((SlotBase)other).getInventory() == this.getInventory();
   }

   public SlotBase setX(int x) {
      ((SlotMixin)this).setX(x);
      return this;
   }

   public SlotBase setY(int y) {
      ((SlotMixin)this).setY(y);
      return this;
   }

   @OnlyIn(Dist.CLIENT)
   public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
      return this.backgroundPair;
   }

   public SlotBase setBackground(ResourceLocation sprite) {
      return this.setBackground(InventoryMenu.BLOCK_ATLAS, sprite);
   }

   @Nonnull
   public SlotBase setBackground(@Nullable ResourceLocation atlas, @Nullable ResourceLocation sprite) {
      this.backgroundPair = atlas != null && sprite != null ? Pair.of(atlas, sprite) : null;
      return this;
   }
}
