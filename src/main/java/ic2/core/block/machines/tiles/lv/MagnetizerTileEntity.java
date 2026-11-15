package ic2.core.block.machines.tiles.lv;

import ic2.core.IC2;
import ic2.core.block.base.features.IClickable;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.misc.IronFenceBlock;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MagnetizerTileEntity extends BaseElectricTileEntity implements ITickListener, IClickable {
   int delay = 0;
   boolean upgraded = false;

   public MagnetizerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 0, 32, 100);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putBoolean("upgraded", this.upgraded);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.upgraded = compound.getBoolean("upgraded");
      this.maxEnergy = this.upgraded ? 1000 : 100;
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public boolean allowsUI() {
      return false;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.MAGNETIZER;
   }

   @Override
   public boolean canSetFacing(Direction dir) {
      return false;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.8;
   }

   @Override
   public boolean onRightClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
      if (!this.upgraded && player.getItemInHand(hand).getItem() == IC2Items.OVERCLOCKER_UPGRADE) {
         this.upgraded = true;
         this.maxEnergy = 1000;
         if (!player.isCreative()) {
            player.getItemInHand(hand).shrink(1);
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onTick() {
      if (this.energy > 0 && this.delay-- <= 0) {
         if (this.delay < 0) {
            this.delay++;
         }

         this.handleRedstone();
         if (this.isRedstonePowered()) {
            this.delay = 5;
         } else {
            boolean notChanged = true;
            MutableBlockPos mutable = new MutableBlockPos();

            for (int i = 1; this.isInRange(i); i++) {
               BlockPos pos = mutable.setWithOffset(this.getBlockPos(), 0, i, 0);
               BlockState state = this.getLevel().getBlockState(pos);
               if (!(state.getBlock() instanceof IronFenceBlock block)) {
                  break;
               }

               int charge = block.getCharge(state);
               int needed = 15 - charge;
               needed = (int)Math.min((float)needed, (float)this.energy / (float)(this.upgraded ? Math.min(100, needed * 10) : needed));
               if (needed > 0) {
                  block.setCharge(this.getLevel(), pos, state, charge + needed);
                  this.useEnergy(this.upgraded ? needed * 10 : needed);
                  notChanged = false;
               }
            }

            for (int i = 1; this.isInRange(i); i++) {
               BlockPos posx = mutable.setWithOffset(this.getBlockPos(), 0, -i, 0);
               BlockState statex = this.getLevel().getBlockState(posx);
               if (!(statex.getBlock() instanceof IronFenceBlock block)) {
                  break;
               }

               int charge = block.getCharge(statex);
               int needed = 15 - charge;
               needed = (int)Math.min((float)needed, (float)this.energy / (float)(this.upgraded ? Math.min(100, needed * 10) : needed));
               if (needed > 0) {
                  block.setCharge(this.getLevel(), posx, statex, charge + needed);
                  this.useEnergy(this.upgraded ? needed * 10 : needed);
                  notChanged = false;
               }
            }

            if (notChanged) {
               this.delay = 5;
            } else if (this.delay == 0) {
               IC2.AUDIO.playSound(this, IC2Sounds.MAGNETIZER_PROCESSING);
               this.delay = -5;
            }
         }
      }
   }

   public boolean isInRange(int i) {
      return (this.upgraded || i <= 20) && this.energy >= (this.upgraded ? 10 : 1);
   }
}
