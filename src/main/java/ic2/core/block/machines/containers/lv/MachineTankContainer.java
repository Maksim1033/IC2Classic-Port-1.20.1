package ic2.core.block.machines.containers.lv;

import ic2.core.block.machines.tiles.lv.MachineTankTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.components.simple.BufferBoxComponent;
import ic2.core.inventory.gui.components.simple.ChargeBarComponent;
import ic2.core.inventory.gui.components.simple.MultiTankComponent;
import ic2.core.inventory.gui.components.simple.TankVoiderComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.UpgradeSlot;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class MachineTankContainer extends ContainerComponent<MachineTankTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/lv/gui_machine_tank.png");
   public static final Vec2i CHARGE_POS = new Vec2i(176, 0);
   public static final Box2i CHARGE_BOX = new Box2i(25, 35, 14, 14);
   public static final Box2i TANK_BOX = new Box2i(80, 15, 16, 58);
   public static final Vec2i TANK_POS = new Vec2i(176, 14);
   public static final Vec2i VOID_POS = new Vec2i(176, 0);

   public MachineTankContainer(MachineTankTileEntity key, Player player, int id) {
      super(key, player, id);

      for (int i = 0; i < 4; i++) {
         this.addSlot(new UpgradeSlot(key, i, 152, 8 + 18 * i));
      }

      this.addSlot(FilterSlot.createFluidDrainSlot(key, 4, 49, 15, key.tank));
      this.addSlot(FilterSlot.createFluidOutputSlot(key, 5, 49, 57));
      this.addSlot(FilterSlot.createFluidFillSlot(key, 6, 111, 15, key.tank));
      this.addSlot(FilterSlot.createFluidOutputSlot(key, 7, 111, 57));
      this.addPlayerInventory(player.getInventory());
      this.addComponent(new ChargeBarComponent(CHARGE_BOX, key, CHARGE_POS, true));
      this.addComponent(new BufferBoxComponent(key));
      this.addComponent(new MultiTankComponent<>(TANK_BOX, TANK_POS, key.tank));
      this.addComponent(new TankVoiderComponent(key, VOID_POS, 3, 2));
   }

   @Override
   public Vec2i getInvButtonOffset() {
      return new Vec2i(0, 0);
   }

   @Override
   public Vec2i getComparatorButtonOffset() {
      return new Vec2i(0, 0);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }
}
