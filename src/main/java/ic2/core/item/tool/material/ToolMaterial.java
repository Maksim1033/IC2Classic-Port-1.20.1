package ic2.core.item.tool.material;

import ic2.core.platform.registries.IC2Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class ToolMaterial implements Tier {
   public static final ToolMaterial BRONZE = new ToolMaterial(350, 6.0F, 2.0F, 2, 13, Ingredient.of(new ItemLike[]{IC2Items.INGOT_BRONZE}));
   public static final ToolMaterial ALUMINIUM = new ToolMaterial(700, 10.0F, 2.0F, 2, 13, Ingredient.of(new ItemLike[]{IC2Items.INGOT_ALUMINIUM}));
   private final int maxUses;
   private final float efficiency;
   private final float attackDamage;
   private final int harvestLevel;
   private final int enchantability;
   private final Ingredient repairMaterial;

   public ToolMaterial(int maxUses, float efficiency, float attackDamage, int harvestLevel, int enchantability, Ingredient repairMaterial) {
      this.maxUses = maxUses;
      this.efficiency = efficiency;
      this.attackDamage = attackDamage;
      this.harvestLevel = harvestLevel;
      this.enchantability = enchantability;
      this.repairMaterial = repairMaterial;
   }

   public int getUses() {
      return this.maxUses;
   }

   public float getSpeed() {
      return this.efficiency;
   }

   public float getAttackDamageBonus() {
      return this.attackDamage;
   }

   public int getLevel() {
      return this.harvestLevel;
   }

   public int getEnchantmentValue() {
      return this.enchantability;
   }

   public Ingredient getRepairIngredient() {
      return this.repairMaterial;
   }
}
