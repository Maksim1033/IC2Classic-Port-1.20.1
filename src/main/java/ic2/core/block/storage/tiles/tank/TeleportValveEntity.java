package ic2.core.block.storage.tiles.tank;

import ic2.api.tiles.teleporter.TeleporterTarget;
import ic2.core.block.base.features.ISpecialWrenchable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TeleportValveEntity extends BaseValveTileEntity implements ITickListener, ISpecialWrenchable {
   public TeleporterTarget target = null;
   private BlockEntity targetCache = null;
   boolean emit = false;

   public TeleportValveEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.TELEPORTER_VALVE;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.target = TeleporterTarget.read(compound.getCompound("target"));
      this.emit = compound.getBoolean("emit");
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      if (this.target != null) {
         compound.put("target", this.target.write());
      }

      compound.putBoolean("emit", this.emit);
   }

   @Override
   public void onTick() {
      if (this.emit && this.target != null && !this.invClock(5)) {
         IFluidHandler source = this.getTank(this);
         if (source != null) {
            IFluidHandler target = this.getTank(this.getTargetCache());
            if (target != null) {
               source.drain(target.fill(source.drain(500, FluidAction.SIMULATE), FluidAction.EXECUTE), FluidAction.EXECUTE);
            }
         }
      }
   }

   private IFluidHandler getTank(BlockEntity target) {
      if (target instanceof TeleportValveEntity teleport) {
         BlockEntity tile = teleport.getMaster();
         return tile == null ? null : (IFluidHandler)tile.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.DOWN).orElse(null);
      } else {
         return null;
      }
   }

   private BlockEntity getTargetCache() {
      if (this.target == null) {
         return null;
      } else {
         if (this.targetCache == null || this.targetCache.isRemoved()) {
            this.targetCache = this.target.getTile();
         }

         return this.targetCache;
      }
   }

   @Override
   public boolean doSpecialAction(Direction side, Vec3 hit, Player player) {
      this.emit = !this.emit;
      if (this.isSimulating()) {
         player.sendSystemMessage(this.translate(this.emit ? "gui.ic2.teleport_valve.send" : "gui.ic2.teleport_valve.receive"));
      }

      return true;
   }

   @Override
   public AABB hasSpecialAction(Direction side, Vec3 hit, Player player) {
      return new AABB(BlockPos.ZERO);
   }

   public boolean setTarget(TeleporterTarget target) {
      if (this.target != null && this.target.equals(target)) {
         return false;
      } else {
         this.target = new TeleporterTarget(target);
         this.targetCache = null;
         return true;
      }
   }

   public boolean hasTarget(TeleporterTarget target) {
      return this.target != null && this.target.equals(target);
   }
}
