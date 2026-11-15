package ic2.core.block.machines.containers.hv;

import ic2.core.block.machines.components.base.BaseCropLibraryComponent;
import ic2.core.block.machines.tiles.hv.CropLibraryTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.ChargeBarComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.utils.math.MathUtils;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CropLibraryContainer extends ContainerComponent<CropLibraryTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/hv/gui_seed_library.png");
   public static final Vec2i OFFSET = new Vec2i(0, 101);
   public static final Vec2i COMPARATOR_OFFSET = new Vec2i(-11, 123);
   public static final Box2i CHARGE_BOX = new Box2i(100, 109, 14, 14);
   public static final Vec2i CHARGE_POS = new Vec2i(176, 44);

   public CropLibraryContainer(CropLibraryTileEntity key, Player player, int id) {
      super(key, player, id);

      for (int y = 0; y < 2; y++) {
         for (int x = 0; x < 2; x++) {
            this.addSlot(new FilterSlot(key, x + y * 2, 134 + x * 18, 101 + y * 18, SpecialFilters.CROP_FILTER_SCANNED));
         }
      }

      for (int y = 0; y < 5; y++) {
         for (int x = 0; x < 5; x++) {
            this.addSlot(new LockedSlot(key.storage.getInventory(), x + y * 5, 87 + x * 17, 5 + y * 17));
         }
      }

      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 56);
      this.addComponent(new BaseCropLibraryComponent(key));
      this.addComponent(new ChargeBarComponent(CHARGE_BOX, key, CHARGE_POS, true));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setFlag(4);
      screen.setMaxSize(screen.getXSize(), 222);
      screen.setContainerOffset(-20, 85);
      screen.setPlayerInventoryOffset(10, 0);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public Vec2i getInvButtonOffset() {
      return OFFSET;
   }

   @Override
   public Vec2i getComparatorButtonOffset() {
      return COMPARATOR_OFFSET;
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (slotId >= 0 && this.getSlot(slotId) instanceof LockedSlot) {
         Slot slot = this.getSlot(slotId);
         if (this.getHolder().isRendering() && slot.hasItem()) {
            this.handleClick(slot.getSlotIndex());
         }
      } else {
         super.clicked(slotId, dragType, clickTypeIn, player);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void handleClick(int slot) {
      int amount = Screen.hasShiftDown() ? 64 : 1;
      if (Screen.hasControlDown()) {
         amount = amount == 1 ? 8 : 24;
      }

      this.getHolder().sendToServer(6, MathUtils.putInt(slot, amount));
   }
}
