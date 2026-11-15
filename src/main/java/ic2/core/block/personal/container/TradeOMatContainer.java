package ic2.core.block.personal.container;

import ic2.core.block.personal.components.TradeOMatClosedComponent;
import ic2.core.block.personal.components.TradeOMatOpenComponent;
import ic2.core.block.personal.provider.IItemTradeProvider;
import ic2.core.block.personal.tile.TradeOMatTileEntity;
import ic2.core.block.personal.trade.ItemTrade;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.inventory.slot.SlotBase;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TradeOMatContainer extends ContainerComponent<TradeOMatTileEntity> {
   public static final ResourceLocation OPEN_TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/personal/gui_trade_o_mat_open.png");
   public static final ResourceLocation CLOSE_TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/personal/gui_trade_o_mat_close.png");
   final boolean open;
   final boolean perPlayer;

   public TradeOMatContainer(TradeOMatTileEntity key, Player player, int id, boolean open) {
      super(key, player, id);
      this.open = open;
      this.perPlayer = key.getTrade().perPlayer;
      ItemTrade trade = key.getTrade();
      if (open) {
         for (int i = 0; i < 4; i++) {
            this.addSlot(new GhostSlot(trade.neededItems, i, i * 18 + 8, 18, SpecialFilters.ANY_ITEM));
         }

         for (int i = 0; i < 4; i++) {
            this.addSlot(new GhostSlot(trade.offered, i, i * 18 + 98, 18, SpecialFilters.ANY_ITEM));
         }

         this.addComponent(new TradeOMatOpenComponent(key));
         this.addComponent(new FilterComponent(this.getInventoryOffset()));
         this.addPlayerInventoryWithOffset(player.getInventory(), 0, 7);
      } else {
         IItemTradeProvider provider = (IItemTradeProvider)(key.isSimulating() && key.getTrade().perPlayer ? key.getOrCreate(player) : key);
         this.disablePreviewer();

         for (int i = 0; i < 4; i++) {
            this.addSlot(new LockedSlot(trade.neededItems, i, i * 18 + 7, 21));
         }

         for (int i = 0; i < 4; i++) {
            this.addSlot(new LockedSlot(trade.offered, i, i * 18 + 99, 21));
         }

         for (int i = 0; i < 4; i++) {
            this.addSlot(new SlotBase(provider.getTradeInput(), i, i % 2 * 18 + 26, 45 + i / 2 * 18));
         }

         for (int i = 0; i < 8; i++) {
            this.addSlot(FilterSlot.createOutputSlot(provider.getTradeOutput(), i, i % 4 * 18 + 80, 45 + i / 4 * 18));
         }

         this.addComponent(new TradeOMatClosedComponent(key));
         this.addPlayerInventoryWithOffset(player.getInventory(), 0, 22);
      }
   }

   @Override
   public boolean stillValid(Player playerIn) {
      return super.stillValid(playerIn) && (this.open || this.perPlayer == this.getHolder().getTrade().perPlayer);
   }

   @Override
   public void removed(Player playerIn) {
      if (!this.open && this.perPlayer) {
         IItemTradeProvider provider = this.getHolder().getOrCreate(playerIn);
         this.clearContainer(playerIn, provider.getTradeInput());
         this.clearContainer(playerIn, provider.getTradeOutput());
      }

      super.removed(playerIn);
   }

   protected void clearContainer(Player player, IHasInventory inventory) {
      if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
         for (int j = 0; j < inventory.getSlotCount(); j++) {
            player.drop(inventory.getStackInSlot(j), false);
            inventory.setStackInSlot(j, ItemStack.EMPTY);
         }
      } else {
         for (int i = 0; i < inventory.getSlotCount(); i++) {
            player.getInventory().placeItemBackInInventory(inventory.getStackInSlot(i));
            inventory.setStackInSlot(i, ItemStack.EMPTY);
         }
      }
   }

   @Override
   public ResourceLocation getTexture() {
      return this.open ? OPEN_TEXTURE : CLOSE_TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.modifySize(0, this.open ? 7 : 22);
   }

   @Override
   public Vec2i getInvButtonOffset() {
      return new Vec2i(1, -2);
   }

   @Override
   public Vec2i getComparatorButtonOffset() {
      return new Vec2i(1, -2);
   }

   @Override
   public int getInventorySize() {
      return this.open ? 8 : 20;
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (this.open && slotId >= 0 && slotId < 8) {
         ItemStack stack = this.getCarried();
         ItemTrade trade = this.getHolder().getTrade();
         if (slotId < 4) {
            trade.neededItems.setStackInSlot(slotId, this.getStack(stack, this.getSlot(slotId).getItem(), dragType, clickTypeIn == ClickType.QUICK_MOVE));
         } else {
            trade.offered.setStackInSlot(slotId - 4, this.getStack(stack, this.getSlot(slotId).getItem(), dragType, clickTypeIn == ClickType.QUICK_MOVE));
         }
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
