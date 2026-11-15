package ic2.core.block.machines.containers.nv;

import ic2.core.block.machines.components.nv.UUExpansionComponent;
import ic2.core.block.machines.tiles.nv.UUMatterExpansionTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UUMatterExpansionContainer extends ContainerComponent<UUMatterExpansionTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/mv/gui_uu_expansion.png");
   public static final Vec2i OFFSET = new Vec2i(156, -12);
   public static final Vec2i OFFSET_COMPARATOR = new Vec2i(134, -1);

   public UUMatterExpansionContainer(UUMatterExpansionTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addSlot(new FilterSlot(key.uuInsertion, 0, 8, 14, new SimpleFilter(IC2Items.UUMATTER)));

      for (int i = 0; i < 9; i++) {
         this.addSlot(new GhostSlot(key.filter, i, 8 + 18 * i, 38, SpecialFilters.UU_OUTPUT));
      }

      this.addPlayerInventoryWithOffset(player.getInventory(), 0, -18);
      this.addComponent(new UUExpansionComponent(key));
      this.addComponent(new FilterComponent(this.getPreviewOffset()));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setPlayerInventoryOffset(0, -18);
   }

   @Override
   public Vec2i getPreviewButtonOffset() {
      return OFFSET;
   }

   @Override
   public Vec2i getComparatorButtonOffset() {
      return OFFSET_COMPARATOR;
   }

   @Override
   public int getInventorySize() {
      return 10;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (slotId >= 0 && slotId < this.getInventorySize()) {
         Slot slot = this.getSlot(slotId);
         if (slot instanceof GhostSlot) {
            ItemStack stack = this.getCarried();
            this.getHolder().filter.setStackInSlot(slot.getSlotIndex(), stack.isEmpty() ? ItemStack.EMPTY : StackUtil.copyWithSize(stack, 1));
            return;
         }
      }

      super.clicked(slotId, dragType, clickTypeIn, player);
   }
}
