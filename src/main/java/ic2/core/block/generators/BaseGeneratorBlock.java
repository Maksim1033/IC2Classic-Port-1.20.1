package ic2.core.block.generators;

import ic2.core.block.base.blocks.BaseActivityBlock;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.utils.helpers.Tool;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;


public class BaseGeneratorBlock extends BaseActivityBlock<BaseTileEntity> {
   public static final Properties BASE_GENERATOR = Properties.of()
      .sound(SoundType.METAL)
      .strength(5.0F, 25.0F)
      .requiresCorrectToolForDrops();

   public BaseGeneratorBlock(String blockName, IBlockDropProvider drops, ITextureProvider provider, BlockEntityType<? extends BlockEntity> tile) {
      super(blockName, BASE_GENERATOR, provider, tile);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
      this.setDropProvider(drops);
   }
}
