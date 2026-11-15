package ic2.core.block.machines;

import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseMultiBlockTileEntity;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.base.tiles.impls.machine.multi.BaseColossalMachineTileEntity;
import ic2.core.block.multi.ColossalMultiBlock;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.math.ConnectionState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class ColossalMachineBlock extends BaseMachineBlock implements IBlockModifiers {
   public static final BooleanProperty FORMED = IC2Properties.STRUCTURE_FORMED;
   public static final IntegerProperty SIZE = IC2Properties.DYNAMIC_STATE;

   public ColossalMachineBlock(String blockName, IBlockDropProvider drop, ITextureProvider provider, BlockEntityType<? extends BaseTileEntity> type) {
      super(blockName, drop, provider, type);
   }

   @Override
   protected void setDefaultState() {
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(ACTIVE, false))
               .setValue(FORMED, false))
            .setValue(SIZE, 0)
      );
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{FORMED}).add(new Property[]{SIZE});
   }

   @Override
   public void onStateUpdate(Level world, BlockPos pos, BlockState state, BaseTileEntity tile) {
      tile.setState(
         (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(FACING, tile.getFacing())).setValue(ACTIVE, tile.isActive()))
               .setValue(FORMED, ((BaseMultiBlockTileEntity)tile).isValid))
            .setValue(SIZE, ((BaseColossalMachineTileEntity)tile).getStructureSize())
      );
   }

   @Override
   public boolean hasTextureRotation(BlockState state, Direction side) {
      return false;
   }

   @Override
   public int getTextureRotation(BlockState state, Direction side) {
      return 0;
   }

   @Override
   public boolean hasCustomTextureUVs(BlockState state, Direction side) {
      return (Boolean)state.getValue(FORMED);
   }

   @Override
   public float[] getCustomTextureUVs(BlockState state, Direction side) {
      int size = (Integer)state.getValue(SIZE);
      return ConnectionState.createQuads(ColossalMultiBlock.UVS[size], side, 1, size + 2, size + 2, size + 2);
   }
}
