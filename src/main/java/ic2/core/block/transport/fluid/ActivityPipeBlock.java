package ic2.core.block.transport.fluid;

import ic2.core.block.transport.fluid.tiles.PipeTileEntity;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActivityPipeBlock extends PipeBlock {
   public static final BooleanProperty ACTIVE = IC2Properties.ACTIVE;
   String activeTexture;
   String inactiveTexture;

   public ActivityPipeBlock(String blockName, BlockEntityType<? extends BlockEntity> type, String texture) {
      super(blockName, type, "");
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(ACTIVE, false));
      this.activeTexture = texture + "_active";
      this.inactiveTexture = texture + "_inactive";
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getTexture(BlockState state, boolean center, Direction dir, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("transport/pipes").get(state.getValue(ACTIVE) ? this.activeTexture : this.inactiveTexture);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{ACTIVE});
   }

   @Override
   public void onStateUpdate(Level world, BlockPos pos, BlockState state, PipeTileEntity tile) {
      tile.setState(
         (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(FOAMED, Integer.valueOf(tile.foamed)))
                  .setValue(WATER, (Boolean)state.getValue(WATER) && tile.foamed == 0))
               .setValue(LAVA, (Boolean)state.getValue(LAVA) && tile.foamed == 0))
            .setValue(ACTIVE, tile.isActive())
      );
   }
}
