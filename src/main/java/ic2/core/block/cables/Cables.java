package ic2.core.block.cables;

import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class Cables {
   public static ItemStack getTinDrop(int insulation) {
      return new ItemStack(IC2Items.TIN_CABLE);
   }

   public static ItemStack getCopperDrop(int insulation) {
      return insulation == 1 ? new ItemStack(IC2Items.COPPER_CABLE_1X_INSULATED) : new ItemStack(IC2Items.COPPER_CABLE);
   }

   public static ItemStack getGoldDrop(int insulation) {
      switch (insulation) {
         case 1:
            return new ItemStack(IC2Items.GOLD_CABLE_1X_INSULATED);
         case 2:
            return new ItemStack(IC2Items.GOLD_CABLE_2X_INSULATED);
         default:
            return new ItemStack(IC2Items.GOLD_CABLE);
      }
   }

   public static ItemStack getBronzeDrop(int insulation) {
      switch (insulation) {
         case 1:
            return new ItemStack(IC2Items.BRONZE_CABLE_1X_INSULATED);
         case 2:
            return new ItemStack(IC2Items.BRONZE_CABLE_2X_INSULATED);
         default:
            return new ItemStack(IC2Items.BRONZE_CABLE);
      }
   }

   public static ItemStack getIronDrop(int insulation) {
      switch (insulation) {
         case 1:
            return new ItemStack(IC2Items.IRON_CABLE_1X_INSULATED);
         case 2:
            return new ItemStack(IC2Items.IRON_CABLE_2X_INSULATED);
         case 3:
            return new ItemStack(IC2Items.IRON_CABLE_4X_INSULATED);
         default:
            return new ItemStack(IC2Items.IRON_CABLE);
      }
   }

   public static ItemStack getAluminiumDrop(int insulation) {
      switch (insulation) {
         case 1:
            return new ItemStack(IC2Items.ALUMINIUM_CABLE_1X_INSULATED);
         case 2:
            return new ItemStack(IC2Items.ALUMINIUM_CABLE_2X_INSULATED);
         case 3:
            return new ItemStack(IC2Items.ALUMINIUM_CABLE_4X_INSULATED);
         case 4:
            return new ItemStack(IC2Items.ALUMINIUM_CABLE_8X_INSULATED);
         default:
            return new ItemStack(IC2Items.ALUMINIUM_CABLE);
      }
   }

   public static ItemStack getGlassDrop(int insulation) {
      return new ItemStack(IC2Items.GLASSFIBER_CABLE);
   }

   public static ItemStack getSuperConductor(int insulation) {
      return new ItemStack(IC2Items.SUPER_CABLE);
   }

   public static ItemStack getPlasmaDrop(int insulation) {
      return new ItemStack(IC2Items.PLASMA_CABLE);
   }

   public static ItemStack getDetectorDrop(int insulation) {
      return new ItemStack(IC2Items.DETECTOR_CABLE);
   }

   public static ItemStack getSplitterDrop(int insulation) {
      return new ItemStack(IC2Items.SPLITTER_CABLE);
   }

   public static ItemStack getComparingDrop(int insulation) {
      return new ItemStack(IC2Items.COMPARING_CABLE);
   }

   public static class AluminiumCable extends CableTileEntity {
      public AluminiumCable(BlockPos pos, BlockState state) {
         super(pos, state, 4);
      }

      @Override
      public double getConductionLoss() {
         switch (this.insulation) {
            case 0:
               return 1.2;
            case 1:
               return 1.15;
            case 2:
               return 1.1;
            case 3:
               return 1.05;
            case 4:
               return 1.0;
            default:
               return 0.0;
         }
      }

      @Override
      public int getInsulationEnergyAbsorption() {
         switch (this.insulation) {
            case 0:
               return 8;
            case 1:
               return 128;
            case 2:
               return 512;
            case 3:
               return 2048;
            case 4:
               return 8192;
            default:
               return 0;
         }
      }

      @Override
      public int getInsulationBreakdownEnergy() {
         return 9001;
      }

      @Override
      public int getConductorBreakdownEnergy() {
         return 8193;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.ALUMINIUM_CABLE;
      }
   }

   public static class BronzeCable extends CableTileEntity {
      public BronzeCable(BlockPos pos, BlockState state) {
         super(pos, state, 2);
      }

      @Override
      public double getConductionLoss() {
         switch (this.insulation) {
            case 0:
               return 0.699999988079071;
            case 1:
               return 0.6499999761581421;
            case 2:
               return 0.6000000238418579;
            default:
               return 0.0;
         }
      }

      @Override
      public int getInsulationEnergyAbsorption() {
         switch (this.insulation) {
            case 0:
               return 8;
            case 1:
               return 32;
            case 2:
               return 128;
            default:
               return 0;
         }
      }

      @Override
      public int getInsulationBreakdownEnergy() {
         return 9001;
      }

      @Override
      public int getConductorBreakdownEnergy() {
         return 129;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.BRONZE_CABLE;
      }
   }

   public static class CopperCable extends CableTileEntity {
      public CopperCable(BlockPos pos, BlockState state) {
         super(pos, state, 1);
      }

      @Override
      public double getConductionLoss() {
         return this.insulation == 1 ? 0.2 : 0.3;
      }

      @Override
      public int getInsulationEnergyAbsorption() {
         return this.insulation == 1 ? 32 : 8;
      }

      @Override
      public int getInsulationBreakdownEnergy() {
         return 9001;
      }

      @Override
      public int getConductorBreakdownEnergy() {
         return 33;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.COPPER_CABLE;
      }
   }

   public static class GlassCable extends CableTileEntity {
      public GlassCable(BlockPos pos, BlockState state) {
         super(pos, state, 0);
      }

      @Override
      public boolean canDye(int insulation) {
         return true;
      }

      @Override
      public double getConductionLoss() {
         return 0.025;
      }

      @Override
      public int getInsulationEnergyAbsorption() {
         return 9001;
      }

      @Override
      public int getInsulationBreakdownEnergy() {
         return 9001;
      }

      @Override
      public int getConductorBreakdownEnergy() {
         return 513;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.GLASSFIBER_CABLE;
      }
   }

   public static class GoldCable extends CableTileEntity {
      public GoldCable(BlockPos pos, BlockState state) {
         super(pos, state, 2);
      }

      @Override
      public double getConductionLoss() {
         switch (this.insulation) {
            case 0:
               return 0.5;
            case 1:
               return 0.44999998807907104;
            case 2:
               return 0.4000000059604645;
            default:
               return 0.0;
         }
      }

      @Override
      public int getInsulationEnergyAbsorption() {
         switch (this.insulation) {
            case 0:
               return 8;
            case 1:
               return 32;
            case 2:
               return 128;
            default:
               return 0;
         }
      }

      @Override
      public int getInsulationBreakdownEnergy() {
         return 9001;
      }

      @Override
      public int getConductorBreakdownEnergy() {
         return 129;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.GOLD_CABLE;
      }
   }

   public static class IronCable extends CableTileEntity {
      public IronCable(BlockPos pos, BlockState state) {
         super(pos, state, 3);
      }

      @Override
      public double getConductionLoss() {
         switch (this.insulation) {
            case 0:
               return 1.0;
            case 1:
               return 0.95;
            case 2:
               return 0.9;
            case 3:
               return 0.8;
            default:
               return 0.0;
         }
      }

      @Override
      public int getInsulationEnergyAbsorption() {
         switch (this.insulation) {
            case 0:
               return 0;
            case 1:
               return 128;
            case 2:
               return 512;
            case 3:
               return 2048;
            default:
               return 0;
         }
      }

      @Override
      public int getInsulationBreakdownEnergy() {
         return 9001;
      }

      @Override
      public int getConductorBreakdownEnergy() {
         return 2049;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.IRON_CABLE;
      }
   }

   public static class PlasmaCable extends CableTileEntity {
      public PlasmaCable(BlockPos pos, BlockState state) {
         super(pos, state, 0);
      }

      @Override
      public boolean canDye(int insulation) {
         return true;
      }

      @Override
      public double getConductionLoss() {
         return 1.2;
      }

      @Override
      public int getInsulationEnergyAbsorption() {
         return 32769;
      }

      @Override
      public int getInsulationBreakdownEnergy() {
         return 32769;
      }

      @Override
      public int getConductorBreakdownEnergy() {
         return 32769;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.PLASMA_CABLE;
      }
   }

   public static class SuperCable extends CableTileEntity {
      public SuperCable(BlockPos pos, BlockState state) {
         super(pos, state, 0);
      }

      @Override
      public double getConductionLoss() {
         return 0.001;
      }

      @Override
      public int getInsulationEnergyAbsorption() {
         return 9001;
      }

      @Override
      public int getInsulationBreakdownEnergy() {
         return 9001;
      }

      @Override
      public int getConductorBreakdownEnergy() {
         return 8193;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.SUPER_CABLE;
      }
   }

   public static class TinCable extends CableTileEntity {
      public TinCable(BlockPos pos, BlockState state) {
         super(pos, state, 0);
      }

      @Override
      public double getConductionLoss() {
         return 0.025;
      }

      @Override
      public int getInsulationEnergyAbsorption() {
         return 3;
      }

      @Override
      public int getInsulationBreakdownEnergy() {
         return 9001;
      }

      @Override
      public int getConductorBreakdownEnergy() {
         return 6;
      }

      @Override
      public BlockEntityType<?> createType() {
         return IC2Tiles.TIN_CABLE;
      }
   }
}
