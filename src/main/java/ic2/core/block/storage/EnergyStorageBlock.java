package ic2.core.block.storage;

import ic2.core.block.base.blocks.BaseFacingBlock;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.impls.BaseEnergyStorageTileEntity;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.utils.helpers.Tool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;


public class EnergyStorageBlock extends BaseFacingBlock<BaseEnergyStorageTileEntity> {
   public static final Properties BASE_ENERGY_STORAGE = Properties.of()
      .sound(SoundType.METAL)
      .strength(1.5F, 7.5F)
      .requiresCorrectToolForDrops();
   public static final IntegerProperty STATE = IC2Properties.ACTIVE_0_3;

   public EnergyStorageBlock(String blockName, IBlockDropProvider drop, ITextureProvider provider, BlockEntityType<? extends BlockEntity> type) {
      super(blockName, BASE_ENERGY_STORAGE, provider, type);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
      this.setDropProvider(drop);
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(STATE, 0));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{STATE});
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, BaseEnergyStorageTileEntity tile) {
      tile.setState((BlockState)((BlockState)state.setValue(FACING, tile.getFacing())).setValue(STATE, Integer.valueOf(tile.state)));
   }

   @Override
   protected Direction getFacing(BlockPlaceContext context) {
      if (context.getPlayer() == null) {
         return Direction.NORTH;
      } else {
         int pitch = Math.round(context.getPlayer().getXRot());
         return pitch >= 65 ? Direction.UP : (pitch <= -65 ? Direction.DOWN : context.getHorizontalDirection().getOpposite());
      }
   }
}
