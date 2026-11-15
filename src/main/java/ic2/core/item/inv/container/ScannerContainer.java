package ic2.core.item.inv.container;

import ic2.core.inventory.container.ItemContainer;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.item.inv.inventory.ScannerInventory;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ScannerContainer extends ItemContainer<ScannerInventory> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/items/gui_filter_scanner.png");

   public ScannerContainer(ScannerInventory key, Player player, int id, int windowID) {
      super(key, player, id, windowID);

      for (int i = 0; i < 9; i++) {
         this.addSlot(new GhostSlot(key, i, 8 + 18 * i, 24, SpecialFilters.BLOCK_TAG_FILTER));
      }

      this.addPlayerInventoryWithOffset(player.getInventory(), 0, -17);
      this.addComponent(new FilterComponent(this.getPreviewOffset()).setBlockTags());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setMaxSize(176, 149);
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
