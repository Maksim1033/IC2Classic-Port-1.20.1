package ic2.core.block.generators;

import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseMultiBlockTileEntity;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.multi.SimpleMultiBlock;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.math.ConnectionState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class FuelBoilerBlock extends BaseGeneratorBlock implements IBlockModifiers {
   public static final BooleanProperty FORMED = IC2Properties.STRUCTURE_FORMED;

   public FuelBoilerBlock() {
      super("fuel_boiler", IBlockDropProvider.SELF_OR_GENERATOR, ITextureProvider.toggleIC2("electric/generator/fuel_boiler"), IC2Tiles.GENERATOR_FUEL_BOILER);
      this.setOverrideRarity(Rarity.UNCOMMON);
   }

   @Override
   protected void setDefaultState() {
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(ACTIVE, false)).setValue(FORMED, false)
      );
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{FORMED});
   }

   @Override
   public void onStateUpdate(Level world, BlockPos pos, BlockState state, BaseTileEntity tile) {
      tile.setState(
         (BlockState)((BlockState)((BlockState)state.setValue(FACING, tile.getFacing())).setValue(ACTIVE, tile.isActive()))
            .setValue(FORMED, ((BaseMultiBlockTileEntity)tile).isValid)
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
      return ConnectionState.createQuads(SimpleMultiBlock.UVS, side, 1, 2, 2, 2);
   }
}
