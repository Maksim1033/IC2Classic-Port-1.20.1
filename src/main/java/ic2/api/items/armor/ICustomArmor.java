package ic2.api.items.armor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ICustomArmor {
   ICustomArmor.AbsorptionProperties getProperties(LivingEntity var1, ItemStack var2, DamageSource var3, double var4, EquipmentSlot var6);

   void damageArmor(LivingEntity var1, ItemStack var2, DamageSource var3, int var4, EquipmentSlot var5, ICustomArmor.DamageType var6);

   default boolean canBlockDamageSource(LivingEntity player, ItemStack stack, DamageSource source, EquipmentSlot slot) {
      return !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR);
   }

   public static class AbsorptionProperties implements Comparable<ICustomArmor.AbsorptionProperties> {
      public int priority;
      public int absorbMax;
      public double absorbRatio;
      public EquipmentSlot slot = null;

      public AbsorptionProperties(int priority, double ratio, int max) {
         this.priority = priority;
         this.absorbMax = max;
         this.absorbRatio = ratio;
      }

      private AbsorptionProperties(ICustomArmor.AbsorptionProperties orig) {
         this.priority = orig.priority;
         this.absorbMax = orig.absorbMax;
         this.absorbRatio = orig.absorbRatio;
         this.slot = orig.slot;
      }

      private int calcMaxAbsorption() {
         return (int)(this.absorbRatio == 0.0 ? 0.0 : (double)this.absorbMax * 100.0 / this.absorbRatio);
      }

      public int compareTo(ICustomArmor.AbsorptionProperties o) {
         return o.priority != this.priority ? o.priority - this.priority : this.calcMaxAbsorption() - o.calcMaxAbsorption();
      }

      @Override
      public String toString() {
         return String.format("%d, %d, %f, %d", this.priority, this.absorbMax, this.absorbRatio, this.calcMaxAbsorption());
      }

      public ICustomArmor.AbsorptionProperties copy() {
         return new ICustomArmor.AbsorptionProperties(this);
      }
   }

   public static enum DamageType {
      MODDED,
      VANILLA,
      THORNS_SELF;
   }
}
