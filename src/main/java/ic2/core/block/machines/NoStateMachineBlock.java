package ic2.core.block.machines;

import ic2.core.block.base.blocks.BaseFacingBlock;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.utils.helpers.Tool;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class NoStateMachineBlock extends BaseFacingBlock<BaseTileEntity> {
   public NoStateMachineBlock(String blockName, IBlockDropProvider drops, ITextureProvider provider, BlockEntityType<? extends BaseTileEntity> type) {
      super(blockName, BaseMachineBlock.BASE_MACHINE, provider, type);
      this.setDropProvider(drops);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   public NoStateMachineBlock(
      String blockName, IBlockDropProvider drops, ITextureProvider provider, Properties prop, BlockEntityType<? extends BaseTileEntity> type
   ) {
      super(blockName, prop, provider, type);
      this.setDropProvider(drops);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }
}
