package ic2.core.block.storage;

import ic2.core.block.base.IC2ContainerBlock;
import ic2.core.block.base.IStateController;
import ic2.core.block.base.tiles.impls.BaseChargingBenchTileEntity;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
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

public class ChargingBenchBlock extends IC2ContainerBlock implements IStateController<BaseChargingBenchTileEntity>, IBlockModel {
   public static final IntegerProperty STATE = IC2Properties.ACTIVE_0_3;
   BlockEntityType<?> type;
   String textureName;

   public ChargingBenchBlock(String blockName, String textureName, BlockEntityType<?> type) {
      super(blockName, BaseMachineBlock.BASE_MACHINE);
      this.type = type;
      this.textureName = textureName;
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(STATE, 0));
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{STATE});
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.type.create(pos, state);
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, BaseChargingBenchTileEntity tile) {
      tile.setState((BlockState)state.setValue(STATE, tile.getState()));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2(this.textureName)
         .get(side.getAxis().isVertical() ? side.getName() : calculateState((Integer)state.getValue(STATE)) + "_" + side.getName());
   }

   public static String calculateState(int state) {
      switch (state) {
         case 0:
            return "empty";
         case 1:
            return "missing";
         case 2:
            return "charging";
         default:
            return "charged";
      }
   }

   public static int calculateMode(int state) {
      switch (state) {
         case 0:
            return 3;
         case 1:
            return 4;
         case 2:
            return 1;
         default:
            return 0;
      }
   }
}
