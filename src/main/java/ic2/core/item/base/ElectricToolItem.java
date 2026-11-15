package ic2.core.item.base;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricEnchantable;
import ic2.core.platform.rendering.features.item.IToolModel;
import ic2.core.utils.helpers.Tool;
import java.util.EnumMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ElectricToolItem extends IC2ElectricItem implements IToolModel, IElectricEnchantable {
   protected float miningSpeed;
   protected float hitDamage;
   protected float hitSpeed;
   protected EnumMap<Tool.ToolType, Tool> tools = new EnumMap<>(Tool.ToolType.class);

   public ElectricToolItem(String itemName, float miningSpeed, float hitDamage, float hitSpeed, @Nullable PropertiesBuilder properties) {
      super(itemName, properties);
      this.miningSpeed = miningSpeed;
      this.hitDamage = hitDamage;
      this.hitSpeed = hitSpeed;
   }

   protected void registerTools(Tool... tools) {
      for (Tool tool : tools) {
         this.tools.put(tool.getType(), tool);
      }
   }

   public float getDestroySpeed(ItemStack stack, BlockState state) {
      if (!ElectricItem.MANAGER.canUse(stack, this.getEnergyCost(stack))) {
         return 1.0F;
      } else {
         return this.isCorrectToolForDrops(stack, state) ? this.miningSpeed : super.getDestroySpeed(stack, state);
      }
   }

   public boolean isCorrectToolForDrops(BlockState p_41450_) {
      if (this.tools.isEmpty()) {
         return false;
      } else {
         for (Tool tool : this.tools.values()) {
            if (tool.isCorrectTool(p_41450_)) {
               return true;
            }
         }

         return false;
      }
   }

   public int getEnchantmentValue() {
      return 14;
   }

   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot, ItemStack stack) {
      Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
      if (equipmentSlot == EquipmentSlot.MAINHAND) {
         multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double)this.hitDamage, Operation.ADDITION));
         multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)this.hitSpeed, Operation.ADDITION));
      }

      return multimap;
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      return true;
   }

   public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
      if (state.getDestroySpeed(worldIn, pos) != 0.0F) {
         ElectricItem.MANAGER.use(stack, this.getEnergyCost(stack), entityLiving);
      }

      return true;
   }

   public boolean isEnchantable(ItemStack stack) {
      return false;
   }

   @Override
   public InteractionResult getEnchantmentCompatibility(ItemStack stack, Enchantment enchantment) {
      return enchantment == Enchantments.MENDING
         ? InteractionResult.FAIL
         : (enchantment == Enchantments.UNBREAKING ? InteractionResult.SUCCESS : InteractionResult.PASS);
   }

   @Override
   public EnchantmentCategory getEnchantmentType(ItemStack stack) {
      return EnchantmentCategory.DIGGER;
   }
}
