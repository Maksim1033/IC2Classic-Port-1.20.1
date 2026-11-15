package ic2.curioplugin.core;

import ic2.api.items.armor.IFoamSupplier;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.ICustomElectricItem;
import ic2.api.items.electric.IElectricItem;
import ic2.core.inventory.base.ICurioGetter;
import ic2.core.inventory.container.ContainerComponent;
import ic2.core.inventory.container.ContainerHasGui;
import ic2.core.item.wearable.base.IC2JetpackBase;
import ic2.core.platform.corehacks.mixins.server.ContainerMixin;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.math.geometry.Vec2i;
import ic2.curioplugin.core.client.CurioComponent;
import ic2.curioplugin.core.slot.ToggleCosmeticCurioSlot;
import ic2.curioplugin.core.slot.ToggleCurioSlot;
import ic2.curioplugin.modules.TickingCurio;
import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.apache.commons.lang3.mutable.MutableBoolean;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.common.capability.CurioItemCapability;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class CurioPlugin implements ICurioGetter {
   public void loadIMC(InterModEnqueueEvent ignoredMod) {
      InterModComms.sendTo("ic2", "curios", "register_type", () -> SlotTypePreset.BACK.getMessageBuilder().build());
      InterModComms.sendTo("ic2", "curios", "register_type", () -> SlotTypePreset.BELT.getMessageBuilder().build());
   }

   @Override
   public boolean addCurioSlots(ContainerHasGui<?> container, Player player, Vec2i pos, Vec2i buttonPos, int offset, boolean update) {
      if (!(container instanceof ContainerComponent<?> comp)) {
         return false;
      } else {
         if (!update) {
            comp.addComponent(new CurioComponent(player, buttonPos));
         }

         if (container.slots.removeIf(T -> T instanceof CurioSlot)) {
            NonNullList<ItemStack> items = ((ContainerMixin)container).getLastSlots();
            items.subList(container.slots.size(), items.size()).clear();
         }

         MutableBoolean visible = comp.getComponent(CurioComponent.class).getVisible();
         ICuriosItemHandler handler = (ICuriosItemHandler)CuriosApi.getCuriosHelper().getCuriosHandler(player).orElse(null);
         if (handler == null) {
            return false;
         } else {
            Map<String, ICurioStacksHandler> curioMap = handler.getCurios();
            int slots = 0;
            int index = 0;
            int yOffset = 12 + pos.getY();

            for (String identifier : curioMap.keySet()) {
               ICurioStacksHandler stacksHandler = curioMap.get(identifier);
               IDynamicStackHandler stackHandler = stacksHandler.getStacks();
               if (stacksHandler.isVisible()) {
                  for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {
                     if (index >= offset) {
                        container.addCurioSlots(
                           new ToggleCurioSlot(player, stackHandler, i, identifier, pos.getX() - 18, yOffset, stacksHandler.getRenders(), visible)
                        );
                        if (stacksHandler.hasCosmetic()) {
                           container.cosmeticCurio = true;
                           container.addCurioSlots(
                              new ToggleCosmeticCurioSlot(player, stacksHandler.getCosmeticStacks(), i, identifier, pos.getX() - 37, yOffset, visible)
                           );
                        }

                        yOffset += 18;
                        slots++;
                     }

                     index++;
                  }
               }
            }

            return true;
         }
      }
   }

   @Override
   public IItemHandler getCurioHandler(Player player) {
      return (IItemHandler)CuriosApi.getCuriosHelper().getEquippedCurios(player).orElse((IItemHandlerModifiable)EmptyHandler.INSTANCE);
   }

   @Override
   public IItemHandlerModifiable getCurioInv(Player player, String id) {
      ICuriosItemHandler handler = (ICuriosItemHandler)CuriosApi.getCuriosHelper().getCuriosHandler(player).orElse(null);
      if (handler == null) {
         return (IItemHandlerModifiable)EmptyHandler.INSTANCE;
      } else {
         ICurioStacksHandler item = (ICurioStacksHandler)handler.getStacksHandler(id).orElse(null);
         return (IItemHandlerModifiable)(item == null ? (IItemHandlerModifiable)EmptyHandler.INSTANCE : item.getStacks());
      }
   }

   @Override
   public int chargeFromArmor(Player player, ItemStack stack, int needed) {
      IItemHandler handler = this.getCurioHandler(player);
      int added = 0;
      int i = 0;

      for (int m = handler.getSlots(); i < m && needed > 0; i++) {
         ItemStack slot = handler.getStackInSlot(i);
         if (slot.getItem() instanceof IElectricItem || slot.getItem() instanceof ICustomElectricItem) {
            int provided = ElectricItem.MANAGER.discharge(slot, needed, 2147483647, true, true, false);
            if (provided > 0) {
               ElectricItem.MANAGER.charge(stack, provided, 2147483647, true, false);
               needed -= provided;
               added += provided;
            }
         }
      }

      return added;
   }

   @Override
   public ItemStack getFoamSupplier(Player player, int amount) {
      IItemHandler handler = this.getCurioHandler(player);
      int i = 0;

      for (int m = handler.getSlots(); i < m; i++) {
         ItemStack stack = handler.getStackInSlot(i);
         if (stack.getItem() instanceof IFoamSupplier foam && foam.canProvideFoam(player, stack, IFoamSupplier.InventoryType.CURIO, amount)) {
            return stack;
         }
      }

      return ItemStack.EMPTY;
   }

   @Override
   public ItemStack getJetpack(Player player) {
      IItemHandler handler = this.getCurioHandler(player);
      int i = 0;

      for (int m = handler.getSlots(); i < m; i++) {
         ItemStack stack = handler.getStackInSlot(i);
         if (stack.getItem() instanceof IC2JetpackBase || stack.getItem() instanceof IC2JetpackBase.IIndirectJetpack) {
            return stack;
         }
      }

      return ItemStack.EMPTY;
   }

   @SubscribeEvent
   public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> evt) {
      ICurio curio = this.createForItem((ItemStack)evt.getObject());
      if (curio != null) {
         evt.addCapability(CuriosCapability.ID_ITEM, CurioItemCapability.createProvider(curio));
      }
   }

   public ICurio createForItem(ItemStack stack) {
      Item item = stack.getItem();
      if (item == IC2Items.RE_BATTERY
         || item == IC2Items.BATTERY_BELT
         || item == IC2Items.BAT_PACK
         || item == IC2Items.LAP_PACK
         || item == IC2Items.QUANTUM_PACK
         || item == IC2Items.CF_PACK) {
         return new TickingCurio(stack, false, false, true);
      } else if (item == IC2Items.ENERGY_CRYSTAL || item == IC2Items.LAPOTRON_CRYSTAL) {
         return new TickingCurio(stack, false, false, true);
      } else if (item == IC2Items.QUANTUM_ACCUMULATOR || item == IC2Items.QUANTUM_ACCUMULATOR_BIG) {
         return new TickingCurio(stack, false, false, true);
      } else if (item == IC2Items.PESD) {
         return new TickingCurio(stack, true, false, true);
      } else {
         return item != IC2Items.JETPACK_ELECTRIC
               && item != IC2Items.JETPACK_ELECTRIC_COMPACT
               && item != IC2Items.JETPACK_FUEL
               && item != IC2Items.JETPACK_FUEL_COMPACT
               && item != IC2Items.JETPACK_NUCLEAR
               && item != IC2Items.JETPACK_NUCLEAR_COMPACT
            ? null
            : new TickingCurio(stack, false, true, true);
      }
   }
}
