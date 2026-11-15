package ic2.core.block.storage.tiles.pad;

import ic2.core.block.base.tiles.impls.BaseChargePadTileEntity;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LVChargepadTileEntity extends BaseChargePadTileEntity {
   public LVChargepadTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, new BaseChargePadTileEntity.ChargePadData(40000, 1, 0, 1, 0.0F, 3.5F));
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.LV_CHARGEPAD;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   protected int getMaxParticleAge() {
      return 5;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   protected float[] getParticleColour(RandomSource rand) {
      float red = (this.installedUpgrades & 1 << BaseChargePadTileEntity.PadUpgrade.DRAIN.getIndex()) == 0
            && (this.installedUpgrades & 1 << BaseChargePadTileEntity.PadUpgrade.DAMAGE.getIndex()) == 0
         ? 0.0F
         : 1.0F;
      float green = (this.installedUpgrades & 1 << BaseChargePadTileEntity.PadUpgrade.DAMAGE.getIndex()) != 0 ? 0.0F : 0.6F;
      return new float[]{red, green + rand.nextFloat() * 0.4F, 0.0F};
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   protected double[] getParticleVelocity(RandomSource rand) {
      return new double[]{0.0, 3.0, 0.0};
   }
}
