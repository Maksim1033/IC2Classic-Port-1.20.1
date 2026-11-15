package ic2.core.block.transport.item.container;

import ic2.core.block.transport.item.components.FilterTubeComponent;
import ic2.core.block.transport.item.tubes.FilterTubeTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FilterTubeContainer extends ContainerComponent<FilterTubeTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/tubes/gui_filter_tube.png");

   public FilterTubeContainer(FilterTubeTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addSlot(new GhostSlot(key.inventory, 0, 152, 133, SpecialFilters.NO_TAGS));
      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 70);
      this.addComponent(new FilterTubeComponent(key));
      this.addComponent(new FilterComponent(this.getPreviewOffset()));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setYSize(236);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public int getInventorySize() {
      return 1;
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (slotId == 0) {
         ItemStack stack = this.getCarried();
         this.getHolder().inventory.setStackInSlot(slotId, stack.isEmpty() ? ItemStack.EMPTY : StackUtil.copyWithSize(stack, 1));
      } else {
         super.clicked(slotId, dragType, clickTypeIn, player);
      }
   }
}
