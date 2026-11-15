package ic2.core.block.transport.item.container;

import ic2.core.block.transport.item.components.ColorFilterTubeComponent;
import ic2.core.block.transport.item.tubes.ColorFilterTubeTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ColorFilterTubeContainer extends ContainerComponent<ColorFilterTubeTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/tubes/gui_color_filter_tube.png");
   IHasInventory simple = new SimpleInventory(1);

   public ColorFilterTubeContainer(ColorFilterTubeTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addSlot(new GhostSlot(this.simple, 0, 152, 133, T -> T.getItem() instanceof DyeItem));
      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 70);
      this.addComponent(new ColorFilterTubeComponent(key, this.simple));
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
         this.simple.setStackInSlot(slotId, stack.isEmpty() ? ItemStack.EMPTY : StackUtil.copyWithSize(stack, 1));
      } else {
         super.clicked(slotId, dragType, clickTypeIn, player);
      }
   }
}
