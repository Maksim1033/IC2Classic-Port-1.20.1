package ic2.core.block.multi;

import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.block.rendering.block.ColossalModel;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.blocks.SimpleBlockModel;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.math.ConnectionState;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ColossalMultiBlock extends BaseMultiBlock implements IBlockModifiers, ICustomBlockModel {
   public static final IntegerProperty RANGE = IC2Properties.DYNAMIC_STATE;
   public static final IntegerProperty STATE = IC2Properties.FORMED_STATE4X4;
   public static final float[][][][] UVS = IC2Properties.UVS_2_4;

   public ColossalMultiBlock(String name, ITextureProvider unformedTexture, BlockEntityType<? extends BlockEntity> tile) {
      super(name, unformedTexture, tile);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   @Override
   protected void setDefaultState() {
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(ACTIVE, false))
               .setValue(STATE, 0))
            .setValue(RANGE, 0)
      );
   }

   public static BlockState buildState(Block block, int range, Direction facing, int index) {
      return (BlockState)((BlockState)((BlockState)((BlockState)block.defaultBlockState().setValue(FACING, facing)).setValue(ACTIVE, false))
            .setValue(STATE, index))
         .setValue(RANGE, range);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{STATE}).add(new Property[]{RANGE});
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, BaseLinkingTileEntity tile) {
      tile.setState(
         (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(ACTIVE, tile.isActive())).setValue(FACING, tile.getFacing()))
               .setValue(STATE, tile.getState() >> 2))
            .setValue(RANGE, tile.getState() & 3)
      );
   }

   @Override
   public List<BlockState> getModelStates() {
      return Collections.emptyList();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return (BaseModel)(state.getValue(STATE) > 0 ? new ColossalModel(state, this) : new SimpleBlockModel(state, this));
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
      return (Integer)state.getValue(STATE) > 0;
   }

   @Override
   public float[] getCustomTextureUVs(BlockState state, Direction side) {
      int range = (Integer)state.getValue(RANGE);
      return ConnectionState.createQuads(UVS[range], side, (Integer)state.getValue(STATE) + 1, range + 2, range + 2, range + 2);
   }
}
