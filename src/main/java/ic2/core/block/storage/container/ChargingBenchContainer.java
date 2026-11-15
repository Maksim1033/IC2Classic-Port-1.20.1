package ic2.core.block.storage.container;

import ic2.core.block.base.tiles.impls.BaseChargingBenchTileEntity;
import ic2.core.block.storage.components.ChargingBenchComponent;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.gui.IC2Screen;
import ic2.core.inventory.gui.components.simple.ChargeBarComponent;
import ic2.core.inventory.slot.ArmorSlot;
import ic2.core.inventory.slot.FilterSlot;
import ic2.core.inventory.slot.SlotBase;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.utils.math.geometry.Box2i;
import ic2.core.utils.math.geometry.Vec2i;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChargingBenchContainer extends ContainerComponent<BaseChargingBenchTileEntity> {
   public static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   public static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui_sprites/blocks/storage/gui_charge_bench.png");
   public static final Box2i CHARGE_BOX = new Box2i(30, 48, 12, 66);
   public static final Vec2i CHARGE_POS = new Vec2i(176, 0);

   public ChargingBenchContainer(BaseChargingBenchTileEntity key, Player player, int id) {
      super(key, player, id);

      for (int i = 0; i < 16; i++) {
         this.addSlot(new FilterSlot(key, i, 49 + i % 4 * 18, 46 + i / 4 * 18, key.createFilter(i)));
      }

      this.addSlot(FilterSlot.createDischargeSlot(key, key.tier, 16, 153, 100));
      this.addPlayerInventoryWithOffset(player.getInventory(), 0, 48);

      for (int i = 0; i < 4; i++) {
         this.addSlot(new ArmorSlot(player, VALID_EQUIPMENT_SLOTS[i], 39 - i, 8, 46 + i * 18));
      }

      this.addCurioSlots(new Vec2i(0, 0));
      this.applySlots(null);
      this.addComponent(new ChargeBarComponent(CHARGE_BOX, key, CHARGE_POS, true).setStepSize(5, 1));
      this.addComponent(new ChargingBenchComponent(key));
   }

   public void applySlots(Player player) {
      for (int i = 0; i < 16; i++) {
         SlotBase slot = (SlotBase)this.slots.get(i);
         slot.setBackground(InventoryMenu.BLOCK_ATLAS, new ResourceLocation("ic2:misc/gui/charge_slot"));
         slot.setX(49 + i % 4 * 18);
      }

      if (this.getHolder().sortedMode) {
         for (int i = 0; i < 4; i++) {
            Slot slot = (Slot)this.slots.get(i * 4);
            slot.setBackground(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(ArmorSlot.ARMOR_SLOT_TEXTURES[3 - i]));
         }

         SlotBase slot = (SlotBase)this.slots.get(5);
         slot.setBackground(null, null);
         slot = (SlotBase)this.slots.get(9);
         slot.setBackground(InventoryMenu.BLOCK_ATLAS, new ResourceLocation("item/empty_armor_slot_shield"));
         if (player != null && this.getHolder().isSimulating()) {
            NonNullList<ItemStack> toInsert = NonNullList.create();

            for (int i = 0; i < 4; i++) {
               slot = (SlotBase)this.slots.get(i * 4);
               if (slot.hasItem() && !slot.mayPlace(slot.getItem())) {
                  toInsert.add(slot.getItem());
                  slot.set(ItemStack.EMPTY);
               }
            }

            if (!toInsert.isEmpty()) {
               IItemTransporter transporter = TransporterManager.getTransporter(player);
               int ix = 0;

               for (int m = toInsert.size(); ix < m; ix++) {
                  ItemStack stack = (ItemStack)toInsert.get(ix);

                  for (int j = 0; j < 16; j++) {
                     slot = (SlotBase)this.slots.get(j);
                     if (!slot.hasItem() && slot.mayPlace(stack)) {
                        slot.set(stack);
                        stack = ItemStack.EMPTY;
                        break;
                     }
                  }

                  if (!stack.isEmpty()) {
                     stack.shrink(transporter.addItem(stack, Direction.DOWN, false));
                     if (stack.getCount() > 0) {
                        player.drop(stack, false);
                     }
                  }
               }
            }
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public void onGuiLoaded(IC2Screen screen) {
      screen.setMaxSize(176, 214);
   }

   @Override
   public ResourceLocation getTexture() {
      return TEXTURE;
   }
}
