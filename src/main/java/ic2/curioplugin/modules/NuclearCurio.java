package ic2.curioplugin.modules;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class NuclearCurio implements ICurio {
   ItemStack stack;

   public NuclearCurio(ItemStack stack) {
      this.stack = stack;
   }

   public void curioTick(String identifier, int index, LivingEntity livingEntity) {
      if (livingEntity instanceof Player player) {
         CompoundTag data = this.stack.getOrCreateTagElement("curio_index");
         data.putInt("index", index);
         data.putString("id", identifier);
         this.stack.onArmorTick(livingEntity.level(), player);
         this.stack.removeTagKey("curio_index");
      }
   }

   public boolean canSync(String identifier, int index, LivingEntity livingEntity) {
      return true;
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
