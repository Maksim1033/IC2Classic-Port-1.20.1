package ic2.core.item.wearable.shields;

import ic2.core.IC2;
import ic2.core.item.base.IC2Item;
import ic2.core.item.wearable.base.IC2ShieldBase;
import ic2.core.platform.registries.IC2Items;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AluminiumShield extends IC2ShieldBase {
   private ResourceLocation texture = new ResourceLocation(IC2.MOD_ID, "textures/models/armor/shield_aluminium.png");

   public AluminiumShield() {
      super("aluminium_shield", 2000);
   }

   @Override
   public void damageShield(ItemStack stack, int amount, LivingEntity entity) {
      stack.hurtAndBreak(amount, entity, IC2Item.get(entity.getUsedItemHand()));
   }

   @Override
   public ResourceLocation getTexture(ItemStack stack) {
      return this.texture;
   }

   public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
      return StackUtil.isStackEqual(new ItemStack(IC2Items.INGOT_ALUMINIUM), repair, 36);
   }
}
