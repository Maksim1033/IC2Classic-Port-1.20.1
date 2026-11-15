package ic2.core.item.wearable.armor.electric;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import ic2.api.items.armor.IArmorModule;
import ic2.api.items.armor.IMetalArmor;
import ic2.api.items.electric.ElectricItem;
import ic2.api.network.buffer.INetworkDataBuffer;
import ic2.core.item.wearable.base.IBaseArmorModule;
import ic2.core.item.wearable.base.IC2AdvancedArmorBase;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.tooltips.ToolTipHelper;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;

public class StaticBoots extends IC2AdvancedArmorBase implements IBaseArmorModule, IMetalArmor {
   public StaticBoots() {
      super("static_boots", EquipmentSlot.FEET, null);
   }

   @Override
   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
      Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
      if (equipmentSlot == EquipmentSlot.FEET) {
         modifiers.put(
            Attributes.ARMOR, new AttributeModifier(UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), "Armor modifier", 1.0, Operation.ADDITION)
         );
      }

      return modifiers;
   }

   @Override
   public Ingredient getRepairMaterial() {
      return Ingredient.of(new ItemLike[]{IC2Items.INGOT_REFINED_IRON});
   }

   @Override
   public String getTextureFolder() {
      return "armor/hazmat";
   }

   @Override
   public String getTextureName() {
      return "rubber_boots";
   }

   @Override
   public String getArmorTexture() {
      return "ic2:textures/models/armor/rubber";
   }

   @Override
   public boolean isMetalArmor(ItemStack stack, Player player, EquipmentSlot targetSlot) {
      return true;
   }

   @Override
   public void addToolTip(ItemStack stack, Player player, TooltipFlag type, ToolTipHelper helper) {
      helper.addDataTooltip("tooltip.item.ic2.static_boots.produce_eu");
      this.handleToolTip(stack, helper::addSimpleToolTip);
   }

   public void onArmorTick(ItemStack stack, Level world, Player player) {
      int charge = this.getCharge(stack);
      if (world.getGameTime() % 10L == 0L && charge > 0) {
         this.chargeItems(player, stack);
      }

      if (charge < 20) {
         CompoundTag nbt = stack.getOrCreateTag();
         boolean isNotWalking = player.getVehicle() != null || player.isInWater() || !player.onGround();
         int posX;
         if (!isNotWalking && nbt.contains("x")) {
            posX = nbt.getInt("x");
         } else {
            posX = (int)player.getX();
            nbt.putInt("x", posX);
         }

         int posZ;
         if (!isNotWalking && nbt.contains("z")) {
            posZ = nbt.getInt("z");
         } else {
            posZ = (int)player.getZ();
            nbt.putInt("z", posZ);
         }

         int distX = posX - (int)player.getX();
         int distZ = posZ - (int)player.getZ();
         double distance = Math.sqrt((double)(distX * distX + distZ * distZ));
         if (distance >= 5.0) {
            nbt.putInt("x", (int)player.getX());
            nbt.putInt("z", (int)player.getZ());
            boolean cloth = player.getFeetBlockState().is(BlockTags.WOOL);
            this.setCharge(stack, charge + Math.min(3, Mth.floor(cloth ? distance : distance / 5.0)));
         }
      }
   }

   public void chargeItems(Player player, ItemStack stack) {
      int charge = this.getCharge(stack);
      charge = ElectricItem.chargeArmor(player, charge, false, EquipmentSlot.FEET);
      this.setCharge(stack, charge);
   }

   public int getCharge(ItemStack stack) {
      return StackUtil.getNbtData(stack).getInt("Charge");
   }

   public void setCharge(ItemStack stack, int charge) {
      stack.getOrCreateTag().putInt("Charge", charge);
   }

   @Override
   public IArmorModule.ModuleType getType(ItemStack stack) {
      return IArmorModule.ModuleType.CHARGER;
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.FEET;
   }

   @Override
   public void onUninstall(ItemStack stack, ItemStack armor, IArmorModule.IArmorModuleHolder holder) {
      armor.removeTagKey("static");
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      CompoundTag nbt = armor.getOrCreateTagElement("static");
      int energy = nbt.getInt("energy");
      if (world.getGameTime() % 10L == 0L && energy > 0) {
         energy = ElectricItem.chargeArmor(player, energy);
         nbt.putInt("energy", energy);
      }

      if (energy < 20) {
         boolean isNotWalking = player.getVehicle() != null || player.isInWater() || !player.onGround();
         if (isNotWalking || !nbt.contains("x")) {
            nbt.putInt("x", (int)player.getX());
         }

         if (isNotWalking || !nbt.contains("z")) {
            nbt.putInt("z", (int)player.getZ());
         }

         int x = nbt.getInt("x");
         int z = nbt.getInt("z");
         double distance = Math.sqrt((double)((x - (int)player.getX()) * (x - (int)player.getX()) + (z - (int)player.getZ()) * (z - (int)player.getZ())));
         if (distance >= 5.0) {
            nbt.putInt("x", (int)player.getX());
            nbt.putInt("z", (int)player.getZ());
            boolean cloth = player.getFeetBlockState().is(BlockTags.WOOL);
            nbt.putInt("energy", energy + Math.min(3, Mth.floor(cloth ? distance : distance / 5.0)));
         }
      }
   }

   @Override
   public boolean handlePacket(Player player, ItemStack module, ItemStack armor, String id, INetworkDataBuffer buffer, Dist targetSide) {
      return false;
   }
}
