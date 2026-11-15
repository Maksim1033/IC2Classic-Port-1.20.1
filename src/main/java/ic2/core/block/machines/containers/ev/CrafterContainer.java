package ic2.core.block.machines.containers.ev;

import ic2.core.block.machines.components.ev.CrafterComponent;
import ic2.core.block.machines.tiles.ev.CrafterTileEntity;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.ChargeBarComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.MemorySlot;
import ic2.core.inventory.slot.SlotBase;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CrafterContainer extends ContainerComponent<CrafterTileEntity> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/machines/ev/gui_crafter.png");

   public CrafterContainer(CrafterTileEntity key, Player player, int id) {
      super(key, player, id);

      for (int i = 0; i < 18; i++) {
         this.addSlot(new SlotBase(key, i, 8 + i % 9 * 18, 50 + i / 9 * 18));
      }

      this.addSlot(new FilterSlot(key.upgrades, 0, 8, 24, new SimpleFilter(IC2Items.CRAFTING_UPGRADE)));
      this.addSlot(new FilterSlot(key.upgrades, 1, 26, 24, new SimpleFilter(IC2Items.OVERCLOCKER_UPGRADE)));
      this.addSlot(new FilterSlot(key.upgrades, 2, 152, 24, new SimpleFilter(IC2Items.MEMORY_STICK)));

      for (int y = 0; y < 3; y++) {
         for (int x = 0; x < 3; x++) {
            this.addSlot(new MemorySlot(key, x + y * 3, 180 + 18 * x, 6 + 18 * y));
         }
      }

      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 13);
      this.addComponent(new CrafterComponent());
      this.addComponent(new ChargeBarComponent(new Box2i(45, 24, 14, 14), key, new Vec2i(0, 179), true));
   }

   @Override
   public int getInventorySize() {
      return 30;
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setYSize(179);
   }
}
