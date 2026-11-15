package ic2.core.block.resource;

import ic2.core.block.base.IC2Block;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.utils.helpers.Tool;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RawOreBlock extends IC2Block implements IBlockModel {
   private String name;

   public RawOreBlock(String blockName, float hardness, float resistance, int harvestLevel) {
      super(blockName, Properties.of().sound(SoundType.STONE).strength(hardness, resistance).requiresCorrectToolForDrops());
      this.setHarvestTool(Tool.PICKAXE.withLevel(harvestLevel));
      this.name = blockName;
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @Override
   public List<BlockState> getModelStates() {
      return Collections.singletonList(this.defaultBlockState());
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("resources/raw").get(this.name);
   }
}
