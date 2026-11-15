package ic2.core.block.storage.tiles.pad;

import ic2.api.reactor.IReactorPlannerComponent;
import ic2.api.reactor.IUsableUranium;
import ic2.core.block.base.tiles.impls.BaseChargePadTileEntity;
import ic2.core.block.storage.container.FissionChargePadContainer;
import ic2.core.inventory.container.IC2Container;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.helpers.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FissionChargePadTileEntity extends BaseChargePadTileEntity {
   public int nuclearTicks = 0;
   public int production = 0;

   public FissionChargePadTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, new BaseChargePadTileEntity.ChargePadData(15000000, 4, 2, 3, 1.0F, 1.5F));
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.FISSION_CHARGEPAD;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new FissionChargePadContainer(this, player, windowID);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putInt(compound, "uranium_ticks", this.nuclearTicks, 0);
      NBTUtils.putInt(compound, "production", this.production, 0);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.nuclearTicks = compound.getInt("uranium_ticks");
      this.production = compound.getInt("production");
   }

   @Override
   protected int getMaxParticleAge() {
      return 14;
   }

   @Override
   protected float[] getParticleColour(RandomSource rand) {
      float red = 0.9804688F;
      float green = 0.835938F;
      float blue = 0.1367188F;
      if ((this.installedUpgrades & 1 << BaseChargePadTileEntity.PadUpgrade.DRAIN.getIndex()) != 0) {
         green /= 2.0F;
         blue /= 2.0F;
      }

      if ((this.installedUpgrades & 1 << BaseChargePadTileEntity.PadUpgrade.DAMAGE.getIndex()) != 0) {
         blue = 0.4F;
         green = 0.4F;
      } else {
         red *= 0.6F + rand.nextFloat() * 0.4F;
      }

      green *= 0.6F + rand.nextFloat() * 0.4F;
      blue *= 0.6F + rand.nextFloat() * 0.4F;
      return new float[]{red, green, blue};
   }

   @Override
   protected double[] getParticleVelocity(RandomSource rand) {
      return new double[]{0.0, 7.6, 0.0};
   }

   @Override
   public void provideEnergy() {
      super.provideEnergy();
      int needed = this.maxEnergy - this.energy;
      if (needed > 0) {
         if (this.nuclearTicks == 0) {
            ItemStack stack = this.inventory.getStackInSlot(0);
            if (!this.useCell(stack)) {
               return;
            }

            this.nuclearTicks = 20;
            this.production = this.getEnergy(stack);
         } else if (this.nuclearTicks > 0) {
            this.nuclearTicks--;
            this.energy = this.energy + this.production;
            this.updateGuiField("energy");
         }
      }
   }

   public int getEnergy(ItemStack stack) {
      if (stack.getItem() instanceof IReactorPlannerComponent component) {
         return component.getType(stack) != IReactorPlannerComponent.ComponentType.FUEL_ROD
            ? 0
            : (int)component.getReactorStat(IReactorPlannerComponent.ReactorStat.ENERGY_PRODUCTION, stack).getAsFloat();
      } else {
         return 0;
      }
   }

   protected boolean useCell(ItemStack stack) {
      if (!this.isCell(stack)) {
         return false;
      } else {
         if (stack.hurt(1, this.getLevel().random, null)) {
            this.inventory.setStackInSlot(0, ((IUsableUranium)stack.getItem()).createDepletedUraniumRod());
         }

         return true;
      }
   }

   protected boolean isCell(ItemStack stack) {
      return stack.getItem() instanceof IUsableUranium;
   }
}
