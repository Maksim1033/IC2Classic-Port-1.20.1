package ic2.core.block.machines.containers.mv;

import ic2.core.block.machines.components.mv.RefineryComponent;
import ic2.core.block.machines.tiles.mv.RefineryTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.ChargeBarComponent;
import ic2.core.inventory.gui.components.simple.MultiTankComponent;
import ic2.core.inventory.gui.components.simple.ProgressComponent;
import ic2.core.inventory.gui.components.simple.TankComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.UpgradeSlot;
import ic2.core.inventory.slot.XPSlot;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RefineryContainer extends ContainerComponent<RefineryTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/mv/gui_refinery.png");
   public static final Vec2i CHARGE_POS = new Vec2i(176, 0);
   public static final Box2i CHARGE_BOX = new Box2i(56, 40, 14, 14);
   public static final Vec2i PROGRESS_POS = new Vec2i(176, 14);
   public static final Box2i PROGRESS_BOX = new Box2i(79, 38, 24, 16);

   public RefineryContainer(RefineryTileEntity key, Player player, int id) {
      super(key, player, id);
      this.addSlot(FilterSlot.createDischargeSlot(key, key.tier, 0, 57, 57));
      this.addSlot(new FilterSlot(key, 1, 57, 21, key.getRecipeList()::isValidCatalyst));

      for (int i = 0; i < 3; i++) {
         this.addSlot(new XPSlot(key, 2 + i, 110, 21 + i * 18));
      }

      for (int i = 0; i < 4; i++) {
         this.addSlot(new UpgradeSlot(key, 5 + i, 152, 12 + i * 18));
      }

      this.addComponent(new TankComponent(new Box2i(16, 18, 16, 58), new Vec2i(176, 31), key.firstTank).setTankName("PrimaryTank"));
      this.addComponent(new TankComponent(new Box2i(36, 18, 16, 58), new Vec2i(176, 31), key.secondTank).setTankName("Secondary Tank"));
      this.addComponent(new MultiTankComponent<>(new Box2i(131, 18, 16, 58), new Vec2i(176, 31), key.output));
      this.addComponent(new ChargeBarComponent(CHARGE_BOX, key, CHARGE_POS, true));
      this.addComponent(new ProgressComponent(PROGRESS_BOX, key, PROGRESS_POS, false));
      this.addComponent(new RefineryComponent(key));
      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 16);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setYSize(182);
   }
}
