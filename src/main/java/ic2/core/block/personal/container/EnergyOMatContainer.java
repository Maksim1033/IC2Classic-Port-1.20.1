package ic2.core.block.personal.container;

import ic2.core.block.personal.components.EnergyOMatClosedComponent;
import ic2.core.block.personal.components.EnergyOMatOpenComponent;
import ic2.core.block.personal.tile.EnergyOMatTileEntity;
import ic2.core.block.personal.trade.EnergyTrade;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.inventory.slot.SlotBase;
import ic2.core.inventory.slot.UpgradeSlot;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnergyOMatContainer extends ContainerComponent<EnergyOMatTileEntity> {
   public static final ResourceLocation OPEN_TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/personal/gui_energy_o_mat_open.png");
   public static final ResourceLocation CLOSE_TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/personal/gui_energy_o_mat_close.png");
   final boolean open;

   public EnergyOMatContainer(EnergyOMatTileEntity key, Player player, int id, boolean open) {
      super(key, player, id);
      this.open = open;
      EnergyTrade trade = key.getTrade();
      if (open) {
         this.getComponents().clear();

         for (int i = 0; i < 4; i++) {
            this.addSlot(new GhostSlot(trade.neededItems, i, i * 18 + 8, 18, SpecialFilters.ANY_ITEM));
         }

         for (int i = 0; i < 2; i++) {
            this.addSlot(new UpgradeSlot(key, key.upgrades, i, 152, i * 18 + 18));
         }

         this.addComponent(new FilterComponent(this.getInventoryOffset()));
         this.addComponent(new EnergyOMatOpenComponent(key));
         this.addPlayerInventoryWithOffset(player.getInventory(), 0, 21);
      } else {
         this.getComponents().clear();
         this.disablePreviewer();

         for (int i = 0; i < 4; i++) {
            this.addSlot(new LockedSlot(trade.neededItems, i, i * 18 + 7, 21));
         }

         for (int i = 0; i < 4; i++) {
            this.addSlot(new SlotBase(key, i, i % 2 * 18 + 26, 45 + i / 2 * 18));
         }

         for (int i = 0; i < 4; i++) {
            this.addSlot(FilterSlot.createOutputSlot(key, 4 + i, 134 + i % 2 * 18, i / 2 * 18 + 45));
         }

         this.addPlayerInventoryWithOffset(player.getInventory(), 0, 22);
         this.addComponent(new EnergyOMatClosedComponent(key));
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.modifySize(0, this.open ? 21 : 22);
   }

   @Override
   public int getInventorySize() {
      return this.open ? 6 : 8;
   }

   @Override
   public ResourceLocation getTexture() {
      return this.open ? OPEN_TEXTURE : CLOSE_TEXTURE;
   }

   @Override
   public Vec2i getInvButtonOffset() {
      return new Vec2i(1, -13);
   }

   @Override
   public Vec2i getComparatorButtonOffset() {
      return new Vec2i(1, -13);
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (this.open && slotId >= 0 && slotId < 4) {
         ItemStack stack = this.getCarried();
         EnergyTrade trade = this.getHolder().getTrade();
         trade.neededItems.setStackInSlot(slotId, this.getStack(stack, this.getSlot(slotId).getItem(), dragType, clickTypeIn == ClickType.QUICK_MOVE));
      } else {
         super.clicked(slotId, dragType, clickTypeIn, player);
      }
   }

   protected ItemStack getStack(ItemStack input, ItemStack present, int action, boolean half) {
      switch (action) {
         case 0:
            if (present.isEmpty() && !input.isEmpty()) {
               return StackUtil.copyWithSize(input, half ? input.getMaxStackSize() : input.getCount());
            }

            if (!present.isEmpty()) {
               present.shrink(half && present.getCount() > 1 ? present.getCount() / 2 : 1);
            }

            return present;
         case 1:
            if (present.isEmpty() && !input.isEmpty()) {
               return StackUtil.copyWithSize(input, half ? 1 : input.getCount());
            }

            if (!present.isEmpty()) {
               present.setCount(Math.min(present.getMaxStackSize(), present.getCount() + (half && present.getCount() > 1 ? present.getCount() / 2 : 1)));
            }

            return present;
         case 2:
            return ItemStack.EMPTY;
         default:
            return present;
      }
   }
}
