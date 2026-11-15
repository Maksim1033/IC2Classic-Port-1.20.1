package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

public class StepAssistModuleItem extends BaseModuleItem {
   private static final UUID ID = UUID.fromString("0aef79b0-07f9-41b4-87d0-9010da8ca5ab");

   public StepAssistModuleItem(String textureFolder, String textureName) {
      super("step_assist_module", null, textureFolder, textureName, IArmorModule.ModuleType.MOVEMENT);
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.LEGS || type == EquipmentSlot.FEET;
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      if (world.getGameTime() % 20L == 0L) {
         CompoundTag data = armor.getOrCreateTag();
         double stepY = data.getDouble("step_last_y");
         if (stepY < player.getY() && player.onGround()) {
            this.useEnergy(armor, 250, player);
         }

         data.putDouble("step_last_y", player.getY());
      }
   }

   @Override
   public void onEquipped(ItemStack stack, ItemStack armor, Player entity) {
      AttributeInstance instance = entity.getAttribute((Attribute)ForgeMod.STEP_HEIGHT_ADDITION.get());
      if (instance != null && instance.getModifier(ID) == null) {
         instance.addPermanentModifier(new AttributeModifier(ID, "IC2StepAssist", 0.6000000238418579, Operation.ADDITION));
      }
   }

   @Override
   public void onUnequipped(ItemStack stack, ItemStack armor, Player entity) {
      entity.getAttribute((Attribute)ForgeMod.STEP_HEIGHT_ADDITION.get()).removeModifier(ID);
   }
}
