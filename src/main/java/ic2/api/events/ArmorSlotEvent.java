package ic2.api.events;

import ic2.api.items.armor.IArmorModule;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.Event;

public class ArmorSlotEvent extends Event {
   Item item;
   String id;
   EquipmentSlot slot;
   Object2IntMap<IArmorModule.ModuleType> slots;

   public ArmorSlotEvent(Item item, String id, EquipmentSlot slot, Object2IntMap<IArmorModule.ModuleType> slots) {
      this.item = item;
      this.id = id;
      this.slot = slot;
      this.slots = slots;
   }

   public Item getItem() {
      return this.item;
   }

   public String getId() {
      return this.id;
   }

   public EquipmentSlot getEquipmentSlot() {
      return this.slot;
   }

   public Object2IntMap<IArmorModule.ModuleType> getSlots() {
      return this.slots;
   }

   public void addSlots(IArmorModule.ModuleType type, int amount) {
      this.slots.computeInt(type, (T, V) -> Math.min(9, (V == null ? 0 : V) + amount));
   }
}
