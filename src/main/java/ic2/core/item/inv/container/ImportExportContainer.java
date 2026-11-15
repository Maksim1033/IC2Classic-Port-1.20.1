package ic2.core.item.inv.container;

import ic2.core.inventory.container.ItemContainer;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.item.inv.components.ImportExportComponent;
import ic2.core.item.inv.inventory.ImportExportInventory;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ImportExportContainer extends ItemContainer<ImportExportInventory> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/items/gui_import_export.png");

   public ImportExportContainer(ImportExportInventory key, Player player, int id, int windowID) {
      super(key, player, id, windowID);
      this.addDataSlots(key);

      for (int i = 0; i < 9; i++) {
         this.addSlot(new GhostSlot(key, i, 8 + 18 * i, 16, SpecialFilters.ALWAYS_TRUE));
      }

      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 8);
      this.addComponent(new FilterComponent(this.getInventoryOffset()));
      this.addComponent(new ImportExportComponent(key));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.modifySize(0, 8);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (slotId >= 0 && slotId < this.getInventorySize()) {
         ItemStack stack = this.getCarried();
         this.getHolder().setStackInSlot(slotId, stack.isEmpty() ? ItemStack.EMPTY : StackUtil.copyWithSize(stack, 1));
      } else {
         super.clicked(slotId, dragType, clickTypeIn, player);
      }
   }
}
