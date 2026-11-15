package ic2.core.item.wearable.material;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2ArmorMaterial implements ArmorMaterial {
   private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
   private final String name;
   private final int[] durabilityAmounts;
   private final int[] damageReductionAmounts;
   private final int enchantability;
   private final SoundEvent soundEvent;
   private final float toughness;
   private final LazyLoadedValue<Ingredient> repairMaterial;

   public IC2ArmorMaterial(
      String name,
      int maxDamageFactor,
      int[] damageReductionAmounts,
      int enchantability,
      SoundEvent soundEvent,
      Supplier<Ingredient> repairMaterial,
      float toughness
   ) {
      this(name, buildDurabilities(maxDamageFactor), damageReductionAmounts, enchantability, soundEvent, repairMaterial, toughness);
   }

   public IC2ArmorMaterial(
      String name,
      int[] durabilityAmounts,
      int[] damageReductionAmounts,
      int enchantability,
      SoundEvent soundEvent,
      Supplier<Ingredient> repairMaterial,
      float toughness
   ) {
      this.name = name;
      this.durabilityAmounts = durabilityAmounts;
      this.damageReductionAmounts = damageReductionAmounts;
      this.enchantability = enchantability;
      this.soundEvent = soundEvent;
      this.toughness = toughness;
      this.repairMaterial = new LazyLoadedValue(repairMaterial);
   }

   private static int[] buildDurabilities(int maxDamageFactor) {
      int[] durability = new int[MAX_DAMAGE_ARRAY.length];

      for (int i = 0; i < MAX_DAMAGE_ARRAY.length; i++) {
         durability[i] = MAX_DAMAGE_ARRAY[i] * maxDamageFactor;
      }

      return durability;
   }

   @Override
   public int getDurabilityForType(ArmorItem.Type type) {
      return this.durabilityAmounts[type.getSlot().getIndex()];
   }

   @Override
   public int getDefenseForType(ArmorItem.Type type) {
      return this.damageReductionAmounts[type.getSlot().getIndex()];
   }

   public int getDurabilityForSlot(EquipmentSlot slotIn) {
      return this.durabilityAmounts[slotIn.getIndex()];
   }

   public int getDefenseForSlot(EquipmentSlot slotIn) {
      return this.damageReductionAmounts[slotIn.getIndex()];
   }

   @Override
   public int getEnchantmentValue() {
      return this.enchantability;
   }

   @Override
   public SoundEvent getEquipSound() {
      return this.soundEvent;
   }

   @Override
   public Ingredient getRepairIngredient() {
      return (Ingredient)this.repairMaterial.get();
   }

   @Override
   @OnlyIn(Dist.CLIENT)
   public String getName() {
      return this.name;
   }

   @Override
   public float getKnockbackResistance() {
      return 0.0F;
   }

   @Override
   public float getToughness() {
      return this.toughness;
   }
}
