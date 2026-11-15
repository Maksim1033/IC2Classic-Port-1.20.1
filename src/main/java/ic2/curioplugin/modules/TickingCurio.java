package ic2.curioplugin.modules;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class TickingCurio implements ICurio {
   ItemStack stack;
   boolean rightClickEquip;
   boolean tick;
   boolean canSync;

   public TickingCurio(ItemStack stack, boolean rightClickEquip, boolean tick, boolean canSync) {
      this.stack = stack;
      this.rightClickEquip = rightClickEquip;
      this.tick = tick;
      this.canSync = canSync;
   }

   public boolean canEquipFromUse(SlotContext slotContext) {
      return this.rightClickEquip;
   }

   public void curioTick(String identifier, int index, LivingEntity livingEntity) {
      if (this.tick && livingEntity instanceof Player player) {
         this.stack.onArmorTick(livingEntity.level(), player);
      }
   }

   public boolean canSync(String identifier, int index, LivingEntity livingEntity) {
      return this.canSync;
   }

   public CompoundTag writeSyncData() {
      return this.stack.getShareTag();
   }

   public void readSyncData(CompoundTag compound) {
      this.stack.readShareTag(compound != null && !compound.isEmpty() ? compound : null);
   }

   public ItemStack getStack() {
      return this.stack;
   }
}
