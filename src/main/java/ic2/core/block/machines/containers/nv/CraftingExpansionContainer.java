package ic2.core.block.machines.containers.nv;

import ic2.core.block.machines.tiles.nv.MemoryExpansionTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.ClassFilter;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.inventory.slot.SlotBase;
import ic2.core.item.misc.MemoryStickItem;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;

public class CraftingExpansionContainer extends ContainerComponent<MemoryExpansionTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/mv/gui_crafting_expansion.png");

   public CraftingExpansionContainer(MemoryExpansionTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addSlot(new FilterSlot(key.inventory, 0, 80, 18, new ClassFilter(MemoryStickItem.class)));
      this.addSlot(new FilterSlot(key.inventory, 1, 80, 54, new ClassFilter(MemoryStickItem.class)));

      for (int y = 0; y < 3; y++) {
         for (int x = 0; x < 3; x++) {
            SlotBase slot = new LockedSlot(key.crafting, x + y * 3, 8 + 18 * x, 18 + 18 * y);
            slot.setBackground(InventoryMenu.BLOCK_ATLAS, new ResourceLocation("ic2:misc/gui/memory_stick"));
            this.addSlot(slot);
         }
      }

      for (int y = 0; y < 3; y++) {
         for (int x = 0; x < 3; x++) {
            SlotBase slot = new LockedSlot(key.crafting, 9 + x + y * 3, 116 + 18 * x, 18 + 18 * y);
            slot.setBackground(InventoryMenu.BLOCK_ATLAS, new ResourceLocation("ic2:misc/gui/memory_stick"));
            this.addSlot(slot);
         }
      }

      this.addPlayerInventory(player.getInventory());
   }

   @Override
   public int getInventorySize() {
      return 20;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public Vec2i getInvButtonOffset() {
      return new Vec2i(0, -10);
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      super.clicked(slotId, dragType, clickTypeIn, player);
      this.broadcastChanges();
   }
}
