package ic2.core.item.inv.container;

import ic2.core.block.machines.components.mv.CropBreederComponent;
import ic2.core.inventory.container.ItemContainer;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.item.inv.inventory.CropBreederInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CropBreederContainer extends ItemContainer<CropBreederInventory> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/items/gui_breeding.png");

   public CropBreederContainer(CropBreederInventory key, Player player, int id, int windowID) {
      super(key, player, id, windowID);

      for (int i = 0; i < 9; i++) {
         this.addSlot(new LockedSlot(key.allCrops, i, 8 + 18 * i, 8));
      }

      for (int i = 0; i < 4; i++) {
         this.addSlot(new GhostSlot(key, i, 26 + 36 * i, 55, SpecialFilters.ALWAYS_FALSE));
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new LockedSlot(key.results, i, 8 + 18 * i, 120));
      }

      this.addComponent(new CropBreederComponent(key));
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.clearFlag(3);
   }

   @Override
   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (slotId != -999) {
         Slot slot = this.getSlot(slotId);
         if (slot.index < 9) {
            this.getHolder().addBreed(slot.getItem());
         } else if (slot instanceof GhostSlot) {
            slot.set(ItemStack.EMPTY);
         }
      } else {
         super.clicked(slotId, dragType, clickTypeIn, player);
      }
   }
}
