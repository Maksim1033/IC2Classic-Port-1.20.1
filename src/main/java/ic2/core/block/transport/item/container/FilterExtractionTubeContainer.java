package ic2.core.block.transport.item.container;

import ic2.core.block.transport.item.components.FilteredExtractionTubeComponent;
import ic2.core.block.transport.item.tubes.FilteredExtractionTubeTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FilterExtractionTubeContainer extends ContainerComponent<FilteredExtractionTubeTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/tubes/gui_filter_extraction_tube.png");

   public FilterExtractionTubeContainer(FilteredExtractionTubeTileEntity key, Player player, int id) {
      super(key, player, id);
      SimpleInventory inventory = new SimpleInventory(16);
      this.addSlot(new GhostSlot(key.filterInventory, 0, 142, 17, SpecialFilters.ANY_ITEM));
      this.addSlot(new FilterSlot(key.upgradeInventory, 0, 168, 17, new SimpleFilter(IC2Items.OVERCLOCKER_UPGRADE)));
      this.addSlot(new FilterSlot(key.upgradeInventory, 1, 168, 35, new SimpleFilter(IC2Items.IMPORT_UPGRADE_SIMPLE)));

      for (int i = 0; i < 16; i++) {
         int xPos = i % 4;
         int yPos = i / 4;
         this.addSlot(new LockedSlot(inventory, i, 8 + xPos * 18, 17 + yPos * 18));
      }

      this.addPlayerInventoryWithOffset(player.getInventory(), 8, 9);
      this.addComponent(new FilteredExtractionTubeComponent(key, inventory));
      this.addComponent(new FilterComponent(this.getPreviewOffset()));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setMaxSize(192, 175);
      screen.setPlayerInventoryOffset(119, 0);
      screen.setFlag(4);
      screen.clearFlag(1);
   }

   @Override
   public Vec2i getPreviewButtonOffset() {
      return new Vec2i(0, -11);
   }

   @Override
   public int getInventorySize() {
      return 19;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (slotId == 0) {
         ItemStack stack = this.getCarried();
         this.getHolder().filterInventory.setStackInSlot(slotId, stack.isEmpty() ? ItemStack.EMPTY : StackUtil.copyWithSize(stack, 1));
      } else if (slotId < 3 || slotId >= 19) {
         super.clicked(slotId, dragType, clickTypeIn, player);
      }
   }
}
