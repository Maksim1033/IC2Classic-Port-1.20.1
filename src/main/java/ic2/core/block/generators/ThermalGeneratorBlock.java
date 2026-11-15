package ic2.core.block.generators;

import ic2.core.block.base.blocks.BaseFacingBlock;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.generators.tiles.ThermalGeneratorTileEntity;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.providers.EmptyProvider;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

public class ThermalGeneratorBlock extends BaseFacingBlock<ThermalGeneratorTileEntity> {
   public static final IntegerProperty ACTIVE = IC2Properties.ACTIVE_0_2;

   public ThermalGeneratorBlock(BlockEntityType<? extends BlockEntity> tile) {
      super("thermal_generator", BaseMachineBlock.BASE_MACHINE, EmptyProvider.INSTANCE, tile);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
      this.setDropProvider(IBlockDropProvider.SELF_OR_GENERATOR);
   }

   @Override
   protected void setDefaultState() {
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(ACTIVE, 0));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{ACTIVE});
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("electric/generator/thermal").get(this.getState((Integer)state.getValue(ACTIVE)) + "_" + side.getName());
   }

   private String getState(int state) {
      switch (state) {
         case 0:
            return "passive";
         case 1:
            return "inactive";
         default:
            return "active";
      }
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, ThermalGeneratorTileEntity tile) {
      tile.setState((BlockState)((BlockState)state.setValue(FACING, tile.getFacing())).setValue(ACTIVE, tile.isThermalEnergy() ? 0 : (tile.isActive() ? 2 : 1)));
   }
}
