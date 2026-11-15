package ic2.core.inventory.container;

import ic2.core.block.base.features.redstone.IComparable;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.inventory.base.IHasGui;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.GuiWidget;
import ic2.core.inventory.gui.components.simple.ComparatorComponent;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.gui.components.simple.InventorySettingsComponent;
import ic2.core.inventory.handler.IHasInventoryHandler;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.IGhostSlot;
import ic2.core.utils.math.geometry.Vec2i;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ContainerComponent<T extends IHasGui> extends ContainerHasGui<T> {
   public static final Vec2i DEFAULT_OFFSET = new Vec2i();
   List<GuiWidget> components = new ArrayList<>();
   private boolean addedPreviewer = false;

   public ContainerComponent(T key, Player player, int id) {
      super(key, player, id);
      if (this.gui instanceof IHasInventoryHandler && ((IHasInventoryHandler)this.gui).allowsUI()) {
         this.addComponent(
            new InventorySettingsComponent(((IHasInventoryHandler)this.gui).getInventoryHandler(), this.getInventoryOffset(), this.getInvButtonOffset())
         );
      }

      if (this.gui instanceof IComparable && this.gui instanceof BaseTileEntity && ((IComparable)this.gui).isAllowingUI()) {
         this.addComponent(new ComparatorComponent((BaseTileEntity & IComparable)this.gui, this.getComparatorButtonOffset(), this.getComparatorOffset()));
      }
   }

   @Override
   protected void addInternalSlots(Inventory player) {
      InventorySettingsComponent comp = this.getComponent(InventorySettingsComponent.class);
      if (comp != null) {
         IHasInventory inv = ((IHasInventoryHandler)this.gui).getInventoryHandler().getFilters();

         for (int i = 0; i < 5; i++) {
            this.addSlot(new ContainerComponent.SettingsSlot(comp, inv, i, -94 + i * 18, 91));
         }

         this.addExtraSlots(5);
      }
   }

   public Slot addSlot(Slot slot) {
      if (slot instanceof IGhostSlot && ((IGhostSlot)slot).getType() == IGhostSlot.GhostType.NORMAL && !this.addedPreviewer) {
         this.addedPreviewer = true;
         this.addComponent(new FilterComponent(this.getPreviewOffset(), this.getPreviewButtonOffset()));
      }

      return super.addSlot(slot);
   }

   @Override
   public int getInventorySize() {
      if (this.getHolder() instanceof IHasInventory) {
         return ((IHasInventory)this.getHolder()).getSlotCount();
      } else {
         throw new IllegalStateException("Implement [getInventorySize] in [" + this.getClass() + "]");
      }
   }

   @Override
   public void removed(Player playerIn) {
      if (this.gui instanceof IComparable) {
         ((IComparable)this.gui).getManager().setSync(false);
      }

      super.removed(playerIn);
   }

   @OnlyIn(Dist.CLIENT)
   public void onGuiLoaded(IC2Screen screen) {
   }

   public void addComponent(GuiWidget component) {
      this.components.add(0, component);
   }

   public List<GuiWidget> getComponents() {
      return this.components;
   }

   public <S extends GuiWidget> S getComponent(Class<S> clz) {
      for (GuiWidget comp : this.components) {
         if (clz.isInstance(comp)) {
            return clz.cast(comp);
         }
      }

      return null;
   }

   public Component getName() {
      IHasGui gui = this.getHolder();
      if (gui instanceof Nameable name) {
         return name.hasCustomName() ? name.getCustomName() : name.getName();
      } else {
         throw new RuntimeException("Container[" + gui.getClass() + "] needs to implement Nameable or override getName() in [" + this.getClass() + "]");
      }
   }

   public void disablePreviewer() {
      this.addedPreviewer = true;
   }

   public abstract ResourceLocation getTexture();

   public Vec2i getInventoryOffset() {
      return DEFAULT_OFFSET;
   }

   public Vec2i getInvButtonOffset() {
      return DEFAULT_OFFSET;
   }

   public Vec2i getPreviewOffset() {
      return this.getInventoryOffset();
   }

   public Vec2i getPreviewButtonOffset() {
      return this.getInvButtonOffset();
   }

   public Vec2i getComparatorOffset() {
      return this.getInventoryOffset();
   }

   public Vec2i getComparatorButtonOffset() {
      return this.getInvButtonOffset();
   }

   public static class SettingsSlot extends FilterSlot {
      InventorySettingsComponent comp;

      public SettingsSlot(InventorySettingsComponent comp, IHasInventory inv, int index, int xPosition, int yPosition) {
         super(inv, index, xPosition + comp.getOffset().getX(), yPosition + comp.getOffset().getY(), SpecialFilters.HANDLER_ITEM);
         this.comp = comp;
      }

      public boolean mayPickup(Player p_40228_) {
         return super.mayPickup(p_40228_) && !this.comp.getHandler().isLocked();
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
         return super.mayPlace(stack) && !this.comp.getHandler().isLocked();
      }

      @OnlyIn(Dist.CLIENT)
      public boolean isActive() {
         return this.comp.isVisible();
      }
   }
}
