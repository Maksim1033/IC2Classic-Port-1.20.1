package ic2.core.block.machines.containers.mv;

import ic2.core.block.base.tiles.impls.machine.single.BaseAdvMachineTileEntity;
import ic2.core.block.machines.tiles.mv.CompactingRecyclerTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.components.simple.ChargeBarComponent;
import ic2.core.inventory.gui.components.simple.ProgressComponent;
import ic2.core.inventory.gui.components.simple.SpeedComponent;
import ic2.core.inventory.slot.UpgradeSlot;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AdvMachineContainer extends ContainerComponent<BaseAdvMachineTileEntity> {
   public static final Vec2i CHARGE_POS = new Vec2i(176, 0);
   public static final Vec2i PROGRESS_POS = new Vec2i(176, 14);

   public AdvMachineContainer(BaseAdvMachineTileEntity key, Player player, int id) {
      super(key, player, id);

      for (Slot slot : key.addSlots(player)) {
         this.addSlot(slot);
      }

      for (int i = 0; i < key.upgradeSlots; i++) {
         this.addSlot(new UpgradeSlot(key, i + key.inventory.size() - key.upgradeSlots, 152, 8 + i * 18));
      }

      this.addPlayerInventory(player.getInventory());
      this.addComponent(new ChargeBarComponent(key.getChargeBox(), key, CHARGE_POS, true));
      this.addComponent(new ProgressComponent(key.getProgressBox(), key, PROGRESS_POS, false));
      this.addComponent(new SpeedComponent(key, key.getSpeedName(), new Vec2i(8, 36)));
      key.addComponents(this);
   }

   @Override
   public ResourceLocation getTexture() {
      return this.getHolder().getTexture();
   }

   @Override
   protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
      return this.getHolder() instanceof CompactingRecyclerTileEntity
         ? this.moveItemStackToPriorizeUpgradeSlots(stack, startIndex, endIndex, reverseDirection)
         : super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
   }
}
