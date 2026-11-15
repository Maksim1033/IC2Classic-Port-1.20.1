package ic2.core.item.wearable.modules;

import ic2.api.items.armor.IArmorModule;
import ic2.core.IC2;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WaterWalkerModuleItem extends BaseModuleItem {
   public WaterWalkerModuleItem(String textureFolder, String textureName) {
      super("water_walker_module", null, textureFolder, textureName, IArmorModule.ModuleType.MOVEMENT);
   }

   @Override
   public boolean canInstallInArmor(ItemStack stack, ItemStack armor, EquipmentSlot type) {
      return type == EquipmentSlot.FEET;
   }

   @Override
   public void onTick(ItemStack stack, ItemStack armor, Level world, Player player) {
      if (!player.isShiftKeyDown()
         && player.getDeltaMovement().y() < 0.0
         && this.canUseEnergy(armor, 100)
         && world.isEmptyBlock(player.blockPosition())
         && player.level().containsAnyLiquid(new AABB(player.blockPosition().below()))
         && Math.abs(player.getY() - (double)player.blockPosition().getY()) < 0.1) {
         if (world.getGameTime() % 10L == 0L) {
            this.useEnergy(armor, 100, player);
         }

         Vec3 motion = player.getDeltaMovement();
         player.setDeltaMovement(motion.x(), 0.0, motion.z());
         player.setOnGround(true);
         Vec3 pos = player.position();

         for (int i = 0; i < 2; i++) {
            world.addAlwaysVisibleParticle(
               ParticleTypes.BUBBLE_COLUMN_UP, pos.x(), pos.y() - 0.2, pos.z(), world.random.nextDouble() - 0.5, 0.04, world.random.nextDouble() - 0.5
            );
         }

         IC2.PLATFORM.resetPlayerInAirTime(player);
      }
   }
}
