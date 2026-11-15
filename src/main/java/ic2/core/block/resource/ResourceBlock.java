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

public class ResourceBlock extends IC2Block implements IBlockModel {
   private String name;

   public ResourceBlock(String blockName, ResourceBlock.Resource resource) {
      super(blockName, getProperties(resource));
      this.setHarvestTool(Tool.PICKAXE.withLevel(resource.harvestLevel));
      this.name = blockName;
   }

   private static Properties getProperties(ResourceBlock.Resource resource) {
      return Properties.of().sound(SoundType.METAL).strength(resource.hardness, resource.explosion).requiresCorrectToolForDrops();
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
      return IC2Textures.getMappedEntriesBlockIC2("resources/storage")
         .get(this.name.equals("uranium_block") ? (side == Direction.UP ? this.name + "_top" : this.name + "_sides") : this.name);
   }

   public static class Resource {
      public static final ResourceBlock.Resource CHARCOAL = new ResourceBlock.Resource(0, 5.0F, 6.0F);
      public static final ResourceBlock.Resource WEAK_METAL = new ResourceBlock.Resource(1, 4.0F, 6.0F);
      public static final ResourceBlock.Resource STRONG_METAL = new ResourceBlock.Resource(2, 5.0F, 6.0F);
      int harvestLevel;
      float hardness;
      float explosion;

      public Resource(int harvestLevel, float hardness, float explosion) {
         this.harvestLevel = harvestLevel;
         this.hardness = hardness;
         this.explosion = explosion;
      }
   }
}
