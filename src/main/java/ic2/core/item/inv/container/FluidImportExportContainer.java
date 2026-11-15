package ic2.core.item.inv.container;

import ic2.core.inventory.container.ItemContainer;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.slot.FluidSlot;
import ic2.core.item.inv.components.FluidImportExportComponent;
import ic2.core.item.inv.inventory.FluidImportExportInventory;
import ic2.core.utils.helpers.FluidHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FluidImportExportContainer extends ItemContainer<FluidImportExportInventory> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/items/gui_import_export.png");

   public FluidImportExportContainer(FluidImportExportInventory key, Player player, int id, int windowID) {
      super(key, player, id, windowID);
      this.addDataSlots(key);

      for (int i = 0; i < 9; i++) {
         this.addSlot(new FluidSlot(key, i, 8 + 18 * i, 16));
      }

      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 8);
      this.addComponent(new FilterComponent(this.getInventoryOffset()).setFluid());
      this.addComponent(new FluidImportExportComponent(key));
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.modifySize(0, 8);
   }

   @Override
   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (slotId >= 0 && slotId < this.getInventorySize()) {
         this.getHolder().setFluidInSlot(slotId, FluidHelper.getDisplayFluid(this.getCarried()));
      } else {
         super.clicked(slotId, dragType, clickTypeIn, player);
      }
   }
}
