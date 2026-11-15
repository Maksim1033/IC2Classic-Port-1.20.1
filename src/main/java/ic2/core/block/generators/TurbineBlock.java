package ic2.core.block.generators;

import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.generators.tiles.SteamTunnelTileEntity;
import ic2.core.block.rendering.block.TurbineBladeModel;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TurbineBlock extends BaseGeneratorBlock implements IBlockModel, ICustomBlockModel, IRenderType {
   public static final IntegerProperty FORMED = IC2Properties.FORMED_STATE3X3_FLAT;

   public TurbineBlock() {
      super("steam_tunnel", IBlockDropProvider.SELF, ITextureProvider.noStateIC2("electric/generator/steam_tunnel/base"), IC2Tiles.STEAM_TUNNEL);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{FORMED});
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return super.getSpriteForState(state, side);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.cutout();
   }

   @Override
   public void onStateUpdate(Level world, BlockPos pos, BlockState state, BaseTileEntity tile) {
      SteamTunnelTileEntity tunnel = (SteamTunnelTileEntity)tile;
      tile.setState(
         (BlockState)((BlockState)((BlockState)state.setValue(FACING, tile.getFacing())).setValue(ACTIVE, tile.isActive()))
            .setValue(FORMED, tunnel.isValid ? 5 : 0)
      );
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new TurbineBladeModel(
         state,
         this,
         IC2Textures.getMappedEntriesBlockIC2("electric/generator/steam_tunnel").get(state.getValue(ACTIVE) ? "active_overlay" : "inactive_overlay")
      );
   }

   @Override
   public List<BlockState> getModelStates() {
      return Collections.emptyList();
   }
}
