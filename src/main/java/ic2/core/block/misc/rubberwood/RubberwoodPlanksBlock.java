package ic2.core.block.misc.rubberwood;

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
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RubberwoodPlanksBlock extends IC2Block implements IBlockModel {
   public RubberwoodPlanksBlock() {
      super("rubberwood_planks", Properties.of().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 3.0F).sound(SoundType.WOOD));
      this.setHarvestTool(Tool.AXE.withLevel(1));
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
      return IC2Textures.getMappedEntriesBlockIC2("resources/rubberwood").get("planks");
   }
}
