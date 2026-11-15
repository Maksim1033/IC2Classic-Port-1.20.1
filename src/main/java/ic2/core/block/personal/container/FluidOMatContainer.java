package ic2.core.block.personal.container;

import ic2.core.block.personal.components.FluidOMatClosedComponent;
import ic2.core.block.personal.components.FluidOMatOpenComponent;
import ic2.core.block.personal.tile.FluidOMatTileEntity;
import ic2.core.block.personal.trade.FluidTrade;
import ic2.core.block.personal.trade.Trade;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.FilterComponent;
import ic2.core.inventory.gui.components.simple.TankComponent;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.FluidSlot;
import ic2.core.inventory.slot.GhostSlot;
import ic2.core.inventory.slot.LockedSlot;
import ic2.core.inventory.slot.SlotBase;
import ic2.core.utils.helpers.FluidHelper;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FluidOMatContainer extends ContainerComponent<FluidOMatTileEntity> {
   public static final ResourceLocation OPEN_TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/personal/gui_fluid_o_mat_open.png");
   public static final ResourceLocation CLOSE_TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/personal/gui_fluid_o_mat_close.png");
   public static final Box2i OFFER_BOX = new Box2i(40, 21, 16, 58);
   public static final Box2i PAYED_BOX = new Box2i(120, 21, 16, 58);
   final boolean open;

   public FluidOMatContainer(FluidOMatTileEntity key, Player player, int id, boolean open) {
      super(key, player, id);
      this.open = open;
      if (open) {
         this.getComponents().clear();
         Trade trade = key.getTrade();

         for (int i = 0; i < 4; i++) {
            this.addSlot(new GhostSlot(trade.neededItems, i, 98 + i % 2 * 18, 18 + i / 2 * 18, SpecialFilters.ANY_ITEM));
         }

         this.addSlot(new FluidSlot(key.helper, 0, 107, 69));
         this.addComponent(new FilterComponent(this.getInventoryOffset()).setFluid());
         this.addComponent(new FluidOMatOpenComponent(key));
         this.addComponent(new TankComponent(new Box2i(143, 27, 16, 58), key.getTrade().offered).setTankName("Offering Tank:"));
         this.addPlayerInventoryWithOffset(player.getInventory(), 0, 22);
      } else {
         this.disablePreviewer();

         for (int i = 0; i < 4; i++) {
            this.addSlot(new LockedSlot(key.getTrade().neededItems, i, 8, 16 + i * 18));
         }

         for (int i = 0; i < 4; i++) {
            this.addSlot(new SlotBase(key, i, 62, 16 + i * 18));
         }

         for (int i = 0; i < 4; i++) {
            this.addSlot(FilterSlot.createOutputSlot(key, i + 4, 98, 16 + i * 18));
         }

         this.addSlot(FilterSlot.createFluidFillSlot(key, 8, 152, 21, key.tank));
         this.addSlot(FilterSlot.createFluidOutputSlot(key, 9, 152, 63));
         this.addComponent(new TankComponent(OFFER_BOX, key.getTrade().offered).setTankName("Offering Tank:"));
         this.addComponent(new TankComponent(PAYED_BOX, key.tank).setTankName("Output Tank:"));
         this.addComponent(new FluidOMatClosedComponent(key));
         this.addPlayerInventoryWithOffset(player.getInventory(), 0, 26);
      }
   }

   @Override
   public ResourceLocation getTexture() {
      return this.open ? OPEN_TEXTURE : CLOSE_TEXTURE;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.modifySize(0, this.open ? 21 : 26);
   }

   @Override
   public Vec2i getInvButtonOffset() {
      return new Vec2i(0, -1);
   }

   @Override
   public Vec2i getComparatorButtonOffset() {
      return new Vec2i(0, -1);
   }

   @Override
   public int getInventorySize() {
      return this.open ? 8 : 14;
   }

   public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
      if (this.open) {
         if (slotId >= 0 && slotId < 4) {
            ItemStack stack = this.getCarried();
            FluidTrade trade = this.getHolder().getTrade();
            trade.neededItems.setStackInSlot(slotId, this.getStack(stack, this.getSlot(slotId).getItem(), dragType, clickTypeIn == ClickType.QUICK_MOVE));
            return;
         }

         if (slotId == 4) {
            this.getHolder().helper.setFluidInSlot(slotId, FluidHelper.getDisplayFluid(this.getCarried()));
            return;
         }
      }

      super.clicked(slotId, dragType, clickTypeIn, player);
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
