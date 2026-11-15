package ic2.core.block.base.blocks;

import ic2.core.block.base.ICamouflageBlock;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.blocks.CamouflageableBlock;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.Tool;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public class ValveBlock extends BaseTexturedBlock implements ICamouflageBlock, ICustomBlockModel {
   ITextureProvider overlay;

   public ValveBlock(String blockName, ITextureProvider provider, ITextureProvider overlay, Properties properties, BlockEntityType<? extends BlockEntity> type) {
      super(blockName, properties.noOcclusion().isRedstoneConductor((T, V, X) -> false).sound(SoundType.METAL).strength(5.0F, 25.0F), provider, type);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
      this.overlay = overlay;
   }

   public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
      return true;
   }

   public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
      consumer.accept(new ICamouflageBlock.CamouflageWrapper(this));
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new CamouflageableBlock(state, this, this.overlay, false);
   }

   @Override
   public List<BlockState> getModelStates() {
      return CollectionUtils.<BlockState>createList();
   }
}
