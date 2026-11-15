package ic2.core.item.inv.container;

import ic2.core.inventory.container.ItemContainer;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.slot.FluidSlot;
import ic2.core.item.inv.inventory.FluidScannerInventory;
import ic2.core.utils.helpers.FluidHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FluidScannerContainer extends ItemContainer<FluidScannerInventory> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/items/gui_fluid_filter_scanner.png");

   public FluidScannerContainer(FluidScannerInventory key, Player player, int id, int windowID) {
      super(key, player, id, windowID);

      for (int i = 0; i < 9; i++) {
         this.addSlot(new FluidSlot(key, i, 8 + 18 * i, 24));
      }

      this.addPlayerInventoryWithOffset(player.getInventory(), 0, -17);
      this.addComponent(new FilterComponent(this.getPreviewOffset()).setFluid());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.modifySize(0, -17);
   }

   @Override
   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (slotId >= 0 && slotId < this.getInventorySize()) {
         this.getHolder().setFluidInSlot(slotId, FluidHelper.getDisplayFluid(this.getCarried()));
      } else {
         super.clicked(slotId, dragType, clickTypeIn, player);
      }
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }
}
