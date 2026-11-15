package ic2.core.block.base.blocks;

import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.features.ITextureProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class BaseActivityBlock<T extends BaseTileEntity> extends BaseFacingBlock<T> {
   public static final BooleanProperty ACTIVE = IC2Properties.ACTIVE;

   public BaseActivityBlock(String blockName, Properties properties, ITextureProvider provider, BlockEntityType<? extends BlockEntity> tile) {
      super(blockName, properties, provider, tile);
   }

   @Override
   protected void setDefaultState() {
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(ACTIVE, false));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{ACTIVE});
   }

   @Override
   public void onStateUpdate(Level world, BlockPos pos, BlockState state, T tile) {
      tile.setState((BlockState)((BlockState)state.setValue(FACING, tile.getFacing())).setValue(ACTIVE, tile.isActive()));
   }
}
