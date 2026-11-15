package ic2.core.entity.inv.container;

import ic2.api.network.container.IContainerDataEvent;
import ic2.api.tiles.IEnergyStorage;
import ic2.core.IC2;
import ic2.core.block.storage.container.ContainerEnergyStorage;
import ic2.core.entity.inv.components.EnergyMinecartComponent;
import ic2.core.entity.minecarts.ElectricMinecart;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.slot.ArmorSlot;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.networking.PacketManager;
import ic2.core.networking.packets.server.gui.sync.ContainerSyncPacket;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnergyMinecartContainer extends ContainerComponent<ElectricMinecart> implements IContainerDataEvent {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/storage/gui_electric_block.png");
   public static final Vec2i BUTTON_OFFSET_INV = new Vec2i(143, 67);
   public static final Vec2i BUTTON_OFFSET_PREVIEW = new Vec2i(154, 56);
   int lastEnergy = -1;

   public EnergyMinecartContainer(ElectricMinecart key, Player player, int id) {
      super(key, player, id);
      this.addSlot(FilterSlot.createChargeSlot(key, key.getTier(), 0, 56 + key.getGuiOffset(), 17));
      this.addSlot(FilterSlot.createDischargeSlot(key, key.getTier(), 1, 56 + key.getGuiOffset(), 53));
      this.addPlayerInventory(player.getInventory());

      for (int i = 0; i < 4; i++) {
         this.addSlot(new ArmorSlot(player, ContainerEnergyStorage.VALID_EQUIPMENT_SLOTS[i], 39 - i, 8, 8 + i * 18));
      }

      this.addCurioSlots(new Vec2i(132, 56));
      this.addComponent(new EnergyMinecartComponent(key));
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      if (IC2.PLATFORM.isSimulating()) {
         IEnergyStorage storage = this.getHolder();
         int next = storage.getStoredEU();
         if (next != this.lastEnergy) {
            this.lastEnergy = next;
            PacketManager.INSTANCE.sendToPlayer(new ContainerSyncPacket(0, this.lastEnergy), this.getPlayer());
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      super.onGuiLoaded(screen);
      screen.setPlayerInventoryOffset(20, 0);
   }

   @Override
   public Vec2i getInvButtonOffset() {
      return BUTTON_OFFSET_INV;
   }

   @Override
   public Vec2i getPreviewButtonOffset() {
      return BUTTON_OFFSET_PREVIEW;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @Override
   public void onDataReceived(int key, int value) {
      if (key == 0) {
         this.getHolder().storedEU = value;
      }
   }
}
