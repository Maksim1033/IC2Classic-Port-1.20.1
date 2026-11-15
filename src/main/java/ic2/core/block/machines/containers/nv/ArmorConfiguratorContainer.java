package ic2.core.block.machines.containers.nv;

import ic2.api.items.armor.IArmorModule;
import ic2.core.block.machines.components.nv.ArmorComponent;
import ic2.core.block.machines.tiles.nv.ArmorConfiguratorTileEntity;
import ic2.core.block.storage.container.ContainerEnergyStorage;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.IQuickMoveBlocker;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ArmorConfiguratorContainer extends ContainerComponent<ArmorConfiguratorTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/stone/gui_armor_configurator.png");

   public ArmorConfiguratorContainer(ArmorConfiguratorTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addSlot(
         new ArmorConfiguratorContainer.ArmorSlot(key, 0, 80, 117)
            .setBackground(InventoryMenu.BLOCK_ATLAS, new ResourceLocation("item/empty_armor_slot_chestplate"))
      );

      for (int y = 0; y < 3; y++) {
         for (int x = 0; x < 9; x++) {
            this.addSlot(new ArmorConfiguratorContainer.ModuleSlot(key, y, x, 8 + 18 * x, 29 + y * 30));
         }
      }

      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 56);

      for (int i = 0; i < 4; i++) {
         this.addSlot(new ic2.core.inventory.slot.ArmorSlot(player, ContainerEnergyStorage.VALID_EQUIPMENT_SLOTS[i], 39 - i, 116 + i * 18, 117));
      }

      this.addComponent(new ArmorComponent(key));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setMaxSize(194, 222);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public int getInventorySize() {
      return 32;
   }

   @Override
   public Vec2i getInvButtonOffset() {
      return new Vec2i(0, -11);
   }

   public static class ArmorSlot extends FilterSlot {
      ArmorConfiguratorTileEntity inv;

      public ArmorSlot(ArmorConfiguratorTileEntity inv, int index, int xPosition, int yPosition) {
         super(inv.armory.getArmor(), index, xPosition, yPosition, IArmorModule.IArmorModuleHolder::isValidModularArmor);
         this.inv = inv;
      }

      @Override
      public void set(ItemStack stack) {
         super.set(stack);
         this.inv.armory.onSlotSet();
      }

      public void onTake(Player thePlayer, ItemStack stack) {
         super.onTake(thePlayer, stack);
         this.inv.armory.reset();
      }
   }

   public static class ModuleSlot extends FilterSlot implements IQuickMoveBlocker {
      final ArmorConfiguratorTileEntity slot;
      final int offset;

      public ModuleSlot(ArmorConfiguratorTileEntity inv, int offset, int index, int xPosition, int yPosition) {
         super(inv, index, xPosition, yPosition, null);
         this.slot = inv;
         this.offset = offset;
      }

      public boolean isActive() {
         return this.slot.armory.getSize(this.offset) > this.getSlotIndex();
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
         return this.slot.armory.canInsert(stack, this.offset);
      }

      @Override
      public IHasInventory getInventory() {
         return this.slot.armory.getSlotInventory(this.offset);
      }

      @Override
      public void set(ItemStack stack) {
         ItemStack prev = this.getItem();
         super.set(stack);
         if (!stack.isEmpty() && !StackUtil.isStackEqual(stack, prev)) {
            if (!prev.isEmpty()) {
               this.slot.armory.onExtracted(prev);
            }

            this.slot.armory.onInserted(stack);
         }
      }

      public void onTake(Player thePlayer, ItemStack stack) {
         this.slot.armory.onExtracted(stack);
         super.onTake(thePlayer, stack);
      }
   }
}
