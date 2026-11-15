package ic2.core.block.personal.container;

import ic2.core.block.personal.components.PersonalComponent;
import ic2.core.block.personal.tile.PersonalTankTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.TankComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.platform.player.friends.Action;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PersonalTankContainer extends ContainerComponent<PersonalTankTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/personal/gui_personal_tank.png");
   public static final Box2i TANK_BOX = new Box2i(80, 14, 16, 58);

   public PersonalTankContainer(PersonalTankTileEntity key, Player player, int id) {
      super(key, player, id);
      if (!key.canDoAction(player.getUUID(), Action.PERSONAL_BLOCKS_EDIT, false)) {
         this.addSlot(new LockedSlot(key.inventory, 0, 44, 14).setBackground(new ResourceLocation("ic2", "misc/gui/fluid_drain")));
         this.addSlot(new LockedSlot(key.inventory, 1, 44, 56).setBackground(new ResourceLocation("ic2", "misc/gui/fluid_drain")));
         this.addSlot(new LockedSlot(key.inventory, 2, 116, 14).setBackground(new ResourceLocation("ic2", "misc/gui/fluid_drain")));
         this.addSlot(new LockedSlot(key.inventory, 3, 116, 56).setBackground(new ResourceLocation("ic2", "misc/gui/fluid_drain")));
      } else {
         this.addSlot(FilterSlot.createFluidDrainSlot(key.inventory, 0, 44, 14, key.tank));
         this.addSlot(FilterSlot.createFluidOutputSlot(key.inventory, 1, 44, 56));
         this.addSlot(FilterSlot.createFluidFillSlot(key.inventory, 2, 116, 14, key.tank));
         this.addSlot(FilterSlot.createFluidOutputSlot(key.inventory, 3, 116, 56));
      }

      this.addPlayerInventory(player.getInventory());
      this.addComponent(new TankComponent(TANK_BOX, key.tank));
      if (key.canDoAction(player.getUUID(), Action.PERSONAL_BLOCKS_ADMIN, false)) {
         this.addComponent(new PersonalComponent(key));
      }
   }

   @Override
   public Vec2i getPreviewButtonOffset() {
      return new Vec2i(0, -11);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setContainerOffset(0, -2);
   }

   @Override
   public int getInventorySize() {
      return 4;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }
}
