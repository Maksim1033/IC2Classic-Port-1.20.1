package ic2.core.item.wearable.material;

import java.util.Arrays;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2SpecialArmorMaterial implements ArmorMaterial {
   private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
   private ArmorMaterial baseMaterial;
   private String nameOverride = null;
   private int[] durabilityOverride = null;
   private int[] damageReductionOverride = null;
   private Integer enchantabilityOverride = null;
   private SoundEvent soundEventOverride = null;
   private Float knockBackOverride = null;
   private Float toughnessOverride = null;
   private LazyLoadedValue<Ingredient> repairMaterialOverride = null;

   public IC2SpecialArmorMaterial(ArmorMaterial baseMaterial) {
      this.baseMaterial = baseMaterial;
   }

   public IC2SpecialArmorMaterial setMaxDamageFactor(int maxDamageFactor) {
      this.durabilityOverride = new int[MAX_DAMAGE_ARRAY.length];

      for (int i = 0; i < MAX_DAMAGE_ARRAY.length; i++) {
         this.durabilityOverride[i] = MAX_DAMAGE_ARRAY[i] * maxDamageFactor;
      }

      return this;
   }

   public IC2SpecialArmorMaterial setDurability(int durability) {
      this.durabilityOverride = new int[MAX_DAMAGE_ARRAY.length];
      Arrays.fill(this.durabilityOverride, durability);
      return this;
   }

   public IC2SpecialArmorMaterial setDurability(int[] durability) {
      this.durabilityOverride = durability;
      return this;
   }

   public IC2SpecialArmorMaterial setDamageReduction(int[] damageReduction) {
      this.damageReductionOverride = damageReduction;
      return this;
   }

   public IC2SpecialArmorMaterial setEnchantability(Integer enchantability) {
      this.enchantabilityOverride = enchantability;
      return this;
   }

   public IC2SpecialArmorMaterial setSoundEvent(SoundEvent soundEvent) {
      this.soundEventOverride = soundEvent;
      return this;
   }

   public IC2SpecialArmorMaterial setRepairMaterial(Supplier<Ingredient> repairMaterial) {
      this.repairMaterialOverride = new LazyLoadedValue(repairMaterial);
      return this;
   }

   public IC2SpecialArmorMaterial setName(String name) {
      this.nameOverride = name;
      return this;
   }

   public IC2SpecialArmorMaterial setToughness(Float toughness) {
      this.toughnessOverride = toughness;
      return this;
   }

   public IC2SpecialArmorMaterial setKnockbackResistance(Float knockback) {
      this.knockBackOverride = knockback;
      return this;
   }

   @Override
   public int getDurabilityForType(net.minecraft.world.item.ArmorItem.Type type) {
      return this.durabilityOverride == null ? this.baseMaterial.getDurabilityForType(type) : this.durabilityOverride[type.getSlot().getIndex()];
   }

   @Override
   public int getDefenseForType(net.minecraft.world.item.ArmorItem.Type type) {
      return this.damageReductionOverride == null ? this.baseMaterial.getDefenseForType(type) : this.damageReductionOverride[type.getSlot().getIndex()];
   }

   public int getDurabilityForSlot(EquipmentSlot slot) {
      return this.durabilityOverride == null ? this.durabilityOverride[slot.getIndex()] : this.durabilityOverride[slot.getIndex()];
   }

   public int getDefenseForSlot(EquipmentSlot slot) {
      return this.damageReductionOverride == null ? this.damageReductionOverride[slot.getIndex()] : this.damageReductionOverride[slot.getIndex()];
   }

   public int getEnchantmentValue() {
      return this.enchantabilityOverride == null ? this.baseMaterial.getEnchantmentValue() : this.enchantabilityOverride;
   }

   public SoundEvent getEquipSound() {
      return this.soundEventOverride == null ? this.baseMaterial.getEquipSound() : this.soundEventOverride;
   }

   public Ingredient getRepairIngredient() {
      return this.repairMaterialOverride == null ? this.baseMaterial.getRepairIngredient() : (Ingredient)this.repairMaterialOverride.get();
   }

   @OnlyIn(Dist.CLIENT)
   public String getName() {
      return this.nameOverride == null ? this.baseMaterial.getName() : this.nameOverride;
   }

   public float getToughness() {
      return this.toughnessOverride == null ? this.baseMaterial.getToughness() : this.toughnessOverride;
   }

   public float getKnockbackResistance() {
      return this.knockBackOverride == null ? this.baseMaterial.getKnockbackResistance() : this.knockBackOverride;
   }
}
