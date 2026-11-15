package ic2.core.block.base.blocks;

import ic2.core.block.base.IC2ContainerBlock;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IBlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BaseTexturedBlock extends IC2ContainerBlock implements IBlockModel {
   protected BlockEntityType<? extends BlockEntity> type;
   protected ITextureProvider provider;

   public BaseTexturedBlock(String blockName, Properties properties, ITextureProvider provider, BlockEntityType<? extends BlockEntity> type) {
      super(blockName, properties);
      this.provider = provider;
      this.type = type;
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.type.create(pos, state);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return this.provider.getTexture(state, side);
   }
}
