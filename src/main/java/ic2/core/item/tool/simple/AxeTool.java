package ic2.core.item.tool.simple;

import ic2.core.IC2;
import ic2.core.block.resource.RubberwoodLogBlock;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.item.ISimpleItemModel;
import ic2.core.platform.rendering.features.item.IToolModel;
import ic2.core.utils.plugins.IRegistryProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.GameData;

public class AxeTool extends AxeItem implements ISimpleItemModel, IToolModel, IRegistryProvider {
   private String textureName;
   private String textureFolder;
   private ResourceLocation id;

   public AxeTool(String itemName, Properties properties, String textureFolder, String textureName, Tier tier) {
      super(tier, 6.0F, -3.1F, properties);
      this.id = GameData.checkPrefix(itemName, false);
      this.textureFolder = textureFolder;
      this.textureName = textureName;
   }

   public AxeTool(String itemName, String textureFolder, String textureName, Tier tier) {
      this(itemName, new Properties(), textureFolder, textureName, tier);
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.id;
   }

   public InteractionResult useOn(UseOnContext context) {
      InteractionResult result = super.useOn(context);
      if (result != InteractionResult.PASS) {
         return result;
      } else {
         Level world = context.getLevel();
         BlockPos pos = context.getClickedPos();
         BlockState state = context.getLevel().getBlockState(pos);
         if (!world.isClientSide()) {
            if (state.getBlock() == IC2Blocks.RUBBERWOOD_LOG) {
               world.setBlock(
                  pos,
                  (BlockState)((BlockState)((BlockState)((BlockState)IC2Blocks.RUBBER_LOG_STRIPPED
                              .defaultBlockState()
                              .setValue(RubberwoodLogBlock.AXIS, (Axis)state.getValue(RubberwoodLogBlock.AXIS)))
                           .setValue(RubberwoodLogBlock.RESIN, (Boolean)state.getValue(RubberwoodLogBlock.RESIN)))
                        .setValue(RubberwoodLogBlock.COLLECTABLE, (Boolean)state.getValue(RubberwoodLogBlock.COLLECTABLE)))
                     .setValue(RubberwoodLogBlock.RESIN_FACING, (Direction)state.getValue(RubberwoodLogBlock.RESIN_FACING)),
                  11
               );
               return InteractionResult.SUCCESS;
            }

            if (state.getBlock() == IC2Blocks.RUBBERWOOD_LOG_BARKED) {
               world.setBlock(
                  pos,
                  (BlockState)IC2Blocks.RUBBER_LOG_BARKED_STRIPPED
                     .defaultBlockState()
                     .setValue(RubberwoodLogBlock.AXIS, (Axis)state.getValue(RubberwoodLogBlock.AXIS)),
                  11
               );
               return InteractionResult.SUCCESS;
            }
         }

         return InteractionResult.FAIL;
      }
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture() {
      return IC2Textures.getMappedEntriesItem(this.id.getNamespace(), this.textureFolder).get(this.textureName);
   }
}
