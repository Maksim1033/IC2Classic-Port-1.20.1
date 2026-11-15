package ic2.core.block.personal.container;

import ic2.core.block.personal.components.PersonalComponent;
import ic2.core.block.personal.tile.PersonalChestTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.inventory.slot.SlotBase;
import ic2.core.platform.player.friends.Action;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PersonalChestContainer extends ContainerComponent<PersonalChestTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/personal/gui_personal_chest.png");

   public PersonalChestContainer(PersonalChestTileEntity key, Player player, int id) {
      super(key, player, id);
      boolean interact = key.canDoAction(player.getUUID(), Action.PERSONAL_BLOCKS_EDIT, false);

      for (int y = 0; y < 6; y++) {
         for (int x = 0; x < 9; x++) {
            this.addSlot(
               (Slot)(interact
                  ? new SlotBase(key.inventory, y * 9 + x, 8 + x * 18, 18 + y * 18)
                  : new LockedSlot(key.inventory, y * 9 + x, 8 + x * 18, 18 + y * 18))
            );
         }
      }

      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 56);
      if (key.canDoAction(player.getUUID(), Action.PERSONAL_BLOCKS_ADMIN, false)) {
         this.addComponent(new PersonalComponent(key));
      }
   }

   @Override
   public int getInventorySize() {
      return this.getHolder().inventory.getSlotCount();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.modifySize(0, 56);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }
}
