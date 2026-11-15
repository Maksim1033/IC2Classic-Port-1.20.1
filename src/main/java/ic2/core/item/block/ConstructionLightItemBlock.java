package ic2.core.item.block;

import ic2.core.block.cables.luminator.ConstructionLightBlock;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ConstructionLightItemBlock extends IC2BlockItem implements ISimpleItemModel {
   public ConstructionLightItemBlock(Block block) {
      super(block);
   }

   protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
      if (!context.getLevel().getBlockState(context.getClickedPos().above()).canBeReplaced(context)) {
         return false;
      } else {
         FluidState bottom = context.getLevel().getFluidState(context.getClickedPos());
         FluidState top = context.getLevel().getFluidState(context.getClickedPos().above());
         return context.getLevel()
               .setBlock(
                  context.getClickedPos(),
                  (BlockState)((BlockState)((BlockState)state.setValue(ConstructionLightBlock.TOP, false))
                        .setValue(ConstructionLightBlock.WATER, bottom.getType() == Fluids.WATER))
                     .setValue(ConstructionLightBlock.LAVA, bottom.getType() == Fluids.LAVA),
                  11
               )
            && context.getLevel()
               .setBlock(
                  context.getClickedPos().above(),
                  (BlockState)((BlockState)((BlockState)state.setValue(ConstructionLightBlock.TOP, true))
                        .setValue(ConstructionLightBlock.WATER, top.getType() == Fluids.WATER))
                     .setValue(ConstructionLightBlock.LAVA, top.getType() == Fluids.LAVA),
                  11
               );
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItemIC2("misc").get("construction_light");
   }
}
