package ic2.core.block.multi;

import ic2.core.block.base.tiles.BaseLinkingTileEntity;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.math.ConnectionState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
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

public class SimpleMultiBlock extends BaseMultiBlock implements IBlockModifiers {
   public static final IntegerProperty STATE = IC2Properties.FORMED_STATE2X2;
   public static final float[][][] UVS = IC2Properties.UVS_2_4[0];
   ITextureProvider provider;

   public SimpleMultiBlock(String name, ITextureProvider unformedTexture, ITextureProvider formedTexture, BlockEntityType<? extends BlockEntity> tile) {
      super(name, unformedTexture, tile);
      this.provider = formedTexture;
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   public static BlockState buildState(Block block, Direction facing, int index) {
      return (BlockState)((BlockState)((BlockState)block.defaultBlockState().setValue(FACING, facing)).setValue(ACTIVE, false)).setValue(STATE, index);
   }

   public static BlockPos buildPos(int index, Direction facing) {
      return new MutableBlockPos().move(facing.getClockWise(), 1 - index % 2).move(facing, 1 - index / 2 % 2).move(Direction.DOWN, index / 4).immutable();
   }

   @Override
   protected void setDefaultState() {
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(ACTIVE, false)).setValue(STATE, 0)
      );
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{STATE});
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, BaseLinkingTileEntity tile) {
      tile.setState(
         (BlockState)((BlockState)((BlockState)state.setValue(ACTIVE, tile.isActive())).setValue(FACING, tile.getFacing())).setValue(STATE, tile.getState())
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return state.getValue(STATE) > 0 ? this.provider.getTexture(state, side) : super.getSpriteForState(state, side);
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
      return ConnectionState.createQuads(UVS, side, (Integer)state.getValue(STATE) + 1, 2, 2, 2);
   }
}
