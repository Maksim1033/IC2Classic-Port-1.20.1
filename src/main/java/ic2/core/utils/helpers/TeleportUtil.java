package ic2.core.utils.helpers;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fluids.FluidStack;

public class TeleportUtil {
   public static double getDistanceCost(Level sourceDim, BlockPos sourcePos, Level targetDim, BlockPos targetPos) {
      boolean dimSwitch = !sourceDim.dimension().equals(targetDim.dimension());
      return Math.pow(Math.sqrt(sourcePos.distSqr(targetPos) * getDistanceScale(sourceDim, targetDim)) + 10.0, dimSwitch ? 0.9 : 0.7);
   }

   public static double getDistanceScale(Level sourceDim, Level targetDim) {
      double source = getScale(sourceDim);
      double target = getScale(targetDim);
      return Math.min(source, target) / Math.max(source, target);
   }

   @OnlyIn(Dist.CLIENT)
   public static double getDistanceCost(ResourceKey<Level> sourceDim, BlockPos sourcePos, ResourceKey<Level> targetDim, BlockPos targetPos) {
      boolean dimSwitch = !sourceDim.equals(targetDim);
      return Math.pow(Math.sqrt(sourcePos.distSqr(targetPos) * getDistanceScale(sourceDim, targetDim)) + 10.0, dimSwitch ? 0.9 : 0.7);
   }

   @OnlyIn(Dist.CLIENT)
   public static double getDistanceScale(ResourceKey<Level> sourceDim, ResourceKey<Level> targetDim) {
      if (Minecraft.getInstance().getConnection() != null) {
         Optional<Registry<DimensionType>> reg = Minecraft.getInstance().getConnection().registryAccess().registry(Registries.DIMENSION_TYPE);
         if (reg.isPresent()) {
            double source = reg.get().getOptional(sourceDim.location()).map(DimensionType::coordinateScale).orElse(1.0);
            double target = reg.get().getOptional(targetDim.location()).map(DimensionType::coordinateScale).orElse(1.0);
            return Math.min(source, target) / Math.max(source, target);
         }
      }
      return 1.0;
   }

   private static double getScale(Level level) {
      return level == null ? 1.0 : level.dimensionType().coordinateScale();
   }

   public static void teleportEntity(Entity entity, ServerLevel world, BlockPos destination, Direction dir) {
      entity.unRide();
      double x = (double)((float)(destination.getX() + dir.getStepX()) + 0.5F);
      double y = (double)(destination.getY() + dir.getStepY() + (dir.getAxis() == Axis.Y && dir.getAxisDirection() == AxisDirection.NEGATIVE ? -1 : 0));
      double z = (double)((float)(destination.getZ() + dir.getStepZ()) + 0.5F);
      if (entity.level().dimension() != world.dimension()) {
         entity.changeDimension(world, new TeleportUtil.TeleportInfo(x, y, z));
      } else {
         entity.teleportTo(x, y, z);
      }
   }

   public static void teleportEntity(Entity entity, ServerLevel world, BlockPos destination) {
      entity.unRide();
      double x = (double)destination.getX();
      double y = (double)destination.getY();
      double z = (double)destination.getZ();
      if (entity.level().dimension() != world.dimension()) {
         entity.changeDimension(world, new TeleportUtil.TeleportInfo(x, y, z));
      } else {
         entity.teleportTo(x, y, z);
      }
   }

   public static int getWeightOfFluid(FluidStack fluid) {
      return 1 + fluid.getAmount() / 10;
   }

   public static int getWeightOfItem(ItemStack stack) {
      return 100 * (stack.getCount() / stack.getMaxStackSize());
   }

   public static int getWeightOfEntity(Entity entity, boolean includeInv) {
      int totalWeight = 0;
      if (entity instanceof ItemEntity) {
         ItemStack is = ((ItemEntity)entity).getItem();
         totalWeight += 100 * is.getCount() / is.getMaxStackSize();
      } else if (entity instanceof Animal || entity instanceof Minecart || entity instanceof Boat) {
         totalWeight += 100;
      } else if (entity instanceof Player) {
         totalWeight += 1000;
         if (includeInv) {
            Inventory inv = ((Player)entity).getInventory();
            int i = 0;

            for (int m = inv.getContainerSize(); i < m; i++) {
               ItemStack stack = inv.getItem(i);
               if (!stack.isEmpty()) {
                  totalWeight += 100 * (stack.getCount() / stack.getMaxStackSize());
               }
            }
         }
      } else if (entity instanceof Ghast) {
         totalWeight += 2500;
      } else if (entity instanceof EnderDragon || entity instanceof WitherBoss) {
         totalWeight += 10000;
      } else if (entity instanceof PathfinderMob) {
         totalWeight += 500;
      }

      if (includeInv && entity instanceof LivingEntity living && !(entity instanceof Player)) {
         for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = living.getItemBySlot(slot);
            if (!stack.isEmpty()) {
               totalWeight += 100 * (stack.getCount() / stack.getMaxStackSize());
            }
         }
      }

      return totalWeight;
   }

   private static final class TeleportInfo implements ITeleporter {
      private final double x;
      private final double y;
      private final double z;

      public TeleportInfo(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
         return repositionEntity.apply(false);
      }

      public PortalInfo getPortalInfo(Entity entity, ServerLevel destinationWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
         return new PortalInfo(new Vec3(this.x, this.y, this.z), Vec3.ZERO, entity.getYRot(), entity.getXRot());
      }
   }
}
