package ic2.core.block.storage.container;

import ic2.core.block.base.tiles.impls.BaseBatteryStationTileEntity;
import ic2.core.block.storage.components.BatteryStationComponent;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.ChargeBarComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BatteryStationContainer extends ContainerComponent<BaseBatteryStationTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/storage/gui_battery_station.png");
   public static final Box2i CHARGE_BOX = new Box2i(30, 48, 12, 66);
   public static final Vec2i CHARGE_POS = new Vec2i(176, 0);

   public BatteryStationContainer(BaseBatteryStationTileEntity key, Player player, int id) {
      super(key, player, id);

      for (int i = 0; i < 16; i++) {
         this.addSlot(FilterSlot.createDischargeSlot(key, i, 49 + i % 4 * 18, 46 + i / 4 * 18));
      }

      this.addSlot(FilterSlot.createChargeSlot(key, 16, 153, 100));
      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 48);
      this.addCurioSlots(new Vec2i(0, 0));
      this.addComponent(new ChargeBarComponent(CHARGE_BOX, key, CHARGE_POS, true).setStepSize(5, 1));
      this.addComponent(new BatteryStationComponent(key));
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setMaxSize(176, 214);
   }
}
