package ic2.core.block.transport.item.container;

import ic2.core.block.transport.item.components.ProviderTubeComponent;
import ic2.core.block.transport.item.tubes.ProviderTubeTileEntity;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ProviderTubeContainer extends ContainerComponent<ProviderTubeTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/tubes/gui_provider_tube.png");

   public ProviderTubeContainer(ProviderTubeTileEntity key, Player player, int id) {
      super(key, player, id);
      IHasInventory inv = new SimpleInventory(18);
      this.addSlot(new GhostSlot(key.filterInv, 0, 8, 78, SpecialFilters.NO_TAGS));

      for (int i = 0; i < 18; i++) {
         int xPos = i % 9;
         int yPos = i / 9;
         this.addSlot(new LockedSlot(inv, i, 8 + xPos * 18, 17 + yPos * 18));
      }

      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 16);
      this.addComponent(new ProviderTubeComponent(key, inv));
      this.addComponent(new FilterComponent(this.getInventoryOffset()));
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setMaxSize(177, 182);
      screen.setFlag(4);
      screen.clearFlag(1);
   }

   @Override
   public int getInventorySize() {
      return 0;
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (slotId == 0) {
         ItemStack stack = this.getCarried();
         this.getHolder().filterInv.setStackInSlot(slotId, stack.isEmpty() ? ItemStack.EMPTY : StackUtil.copyWithSize(stack, 1));
      } else if (slotId < 1 || slotId >= 19) {
         super.clicked(slotId, dragType, clickTypeIn, player);
      }
   }
}
