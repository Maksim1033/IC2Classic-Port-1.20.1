package ic2.core.item.wearable.armor;

import ic2.api.items.armor.IFoamSupplier;
import ic2.core.item.base.PropertiesBuilder;
import ic2.core.item.wearable.base.IC2AdvancedArmorBase;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CFPack extends IC2AdvancedArmorBase implements IFoamSupplier {
   public CFPack() {
      super("cf_pack", EquipmentSlot.CHEST, new PropertiesBuilder().setNoRepair().maxDamage(3400));
   }

   @Override
   public boolean canProvideFoam(Player player, ItemStack stack, IFoamSupplier.InventoryType inv, int amount) {
      return inv != IFoamSupplier.InventoryType.ARMOR && inv != IFoamSupplier.InventoryType.CURIO
         ? false
         : stack.getMaxDamage() - stack.getDamageValue() >= amount;
   }

   @Override
   public void useFoam(Player player, ItemStack stack, int amount) {
      stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + amount));
   }

   @Override
   public int getFreeFoamSpace(ItemStack stack) {
      return stack.getDamageValue();
   }

   @Override
   public void fillFoam(ItemStack stack, int amount) {
      stack.setDamageValue(Math.max(0, stack.getDamageValue() - amount));
   }

   @Override
   public Ingredient getRepairMaterial() {
      return Ingredient.EMPTY;
   }

   @Override
   public String getTextureFolder() {
      return "armor/packs";
   }

   @Override
   public String getTextureName() {
      return "cf";
   }

   @Override
   public String getArmorTexture() {
      return "ic2:textures/models/armor/batpack";
   }

   public void m_6787_(CreativeModeTab group, NonNullList<ItemStack> items) {
      if (this.m_220152_(group)) {
         ItemStack empty = new ItemStack(this);
         empty.setDamageValue(3400);
         items.add(empty);
         items.add(new ItemStack(this));
      }
   }

   public boolean isDamaged(ItemStack stack) {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
      tooltip.add(
         this.translate("tooltip.item.ic2.cf_pack.sprayer_uses", new Object[]{stack.getMaxDamage() - stack.getDamageValue()}).withStyle(ChatFormatting.GRAY)
      );
   }
}
