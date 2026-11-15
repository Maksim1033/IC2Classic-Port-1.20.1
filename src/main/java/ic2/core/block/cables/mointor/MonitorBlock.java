package ic2.core.block.cables.mointor;

import ic2.api.util.DirectionList;
import ic2.core.block.base.blocks.BaseActivityBlock;
import ic2.core.block.base.drops.IBlockDropProvider;
import ic2.core.block.base.misc.color.IBlockColorListener;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IColoredBlockModel;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.math.ConnectionState;
import ic2.core.utils.tooltips.helper.ITooltipProvider;
import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MonitorBlock extends BaseActivityBlock<BaseTileEntity> implements IColoredBlockModel, IBlockColorListener {
   public static final BooleanProperty MASTER = BooleanProperty.create("master");
   public static final BooleanProperty LOCKED = BooleanProperty.create("locked");
   public static final IntegerProperty CONNECTION = IntegerProperty.create("connections", 0, 15);

   public MonitorBlock() {
      super("monitor", BaseMachineBlock.BASE_MACHINE, ITextureProvider.triToggleIC2("electric/monitor"), IC2Tiles.MONITOR);
      this.setDropProvider(IBlockDropProvider.SELF_OR_MACHINE);
      this.addTooltip(ITooltipProvider.IV_MACHINE);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   @Override
   protected void setDefaultState() {
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH)).setValue(ACTIVE, false))
                  .setValue(MASTER, false))
               .setValue(LOCKED, false))
            .setValue(CONNECTION, 15)
      );
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{MASTER}).add(new Property[]{LOCKED}).add(new Property[]{CONNECTION});
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getItemColor(ItemStack stack, int tintIndex) {
      return DyeColor.LIME.getTextColor() | 0xFF000000;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public int getBlockColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
      if (world != null && pos != null) {
         BlockEntity tile = world.getBlockEntity(pos);
         return (tile instanceof MonitorTileEntity ? ((MonitorTileEntity)tile).getScreenColor() : DyeColor.LIME).getTextColor() | 0xFF000000;
      } else {
         return DyeColor.LIME.getTextColor() | 0xFF000000;
      }
   }

   @Override
   protected Direction getFacing(BlockPlaceContext context) {
      return context.getPlayer() == null ? Direction.NORTH : context.getHorizontalDirection().getOpposite();
   }

   @Override
   protected void setPlaceData(BaseTileEntity tile, BlockState state, LivingEntity placer, ItemStack stack) {
      DirectionList connections = getConnections(tile.getLevel(), tile.getBlockPos(), tile.getFacing(), -1);
      tile.setState(
         (BlockState)((BlockState)state.setValue(MASTER, isMaster(connections, tile.getFacing())))
            .setValue(CONNECTION, ConnectionState.fromList(connections, tile.getFacing()).getIndex())
      );
   }

   public static DirectionList getConnections(Level world, BlockPos pos, Direction axis, int id) {
      DirectionList connections = DirectionList.EMPTY;

      for (Direction dir : DirectionList.ofAxis(axis.getAxis()).invert()) {
         BlockPos other = pos.relative(dir);
         BlockState state = world.getBlockState(other);
         if (isBlock(state, axis)) {
            BlockEntity tile = world.getBlockEntity(other);
            if (tile instanceof MonitorTileEntity && ((MonitorTileEntity)tile).gridId == id) {
               connections = connections.add(dir);
            }
         }
      }

      return connections;
   }

   public static boolean isMaster(DirectionList list, Direction dir) {
      boolean positive = dir.getAxisDirection() == AxisDirection.POSITIVE;
      return dir.getAxis().isVertical()
         ? list.notContains(positive ? Direction.SOUTH : Direction.NORTH) && list.notContains(positive ? Direction.EAST : Direction.WEST)
         : list.notContains(dir.getClockWise()) && list.notContains(Direction.UP);
   }

   public static boolean isMaster(Level world, BlockPos pos, Direction dir) {
      boolean positive = dir.getAxisDirection() == AxisDirection.POSITIVE;
      return dir.getAxis().isVertical()
         ? !isBlock(world.getBlockState(pos.relative(positive ? Direction.SOUTH : Direction.NORTH)), dir)
            && !isBlock(world.getBlockState(pos.relative(positive ? Direction.EAST : Direction.WEST)), dir)
         : !isBlock(world.getBlockState(pos.relative(dir.getClockWise())), dir) && !isBlock(world.getBlockState(pos.relative(Direction.UP)), dir);
   }

   public static Direction getSearchDir(Direction baseDir, boolean height) {
      boolean positive = baseDir.getAxisDirection() == AxisDirection.POSITIVE;
      if (baseDir.getAxis().isVertical()) {
         return height ? (positive ? Direction.WEST : Direction.EAST) : (positive ? Direction.NORTH : Direction.SOUTH);
      } else {
         return height ? Direction.DOWN : baseDir.getCounterClockWise();
      }
   }

   public static boolean isBlock(BlockState state, Direction dir) {
      return state.getBlock() instanceof MonitorBlock && state.getValue(FACING) == dir;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      if (side != Direction.SOUTH) {
         return super.getSpriteForState(state, side);
      } else {
         Map<String, TextureAtlasSprite> texture = IC2Textures.getMappedEntriesBlockIC2(
            "electric/monitor/" + (state.getValue(ACTIVE) ? "active_front" : "inactive_front")
         );
         return texture.get(ConnectionState.fromNumber((Integer)state.getValue(CONNECTION)).getTypeName());
      }
   }

   @Override
   public int getTintedIndexFor(BlockState state, Direction dir, int layer) {
      return dir == Direction.SOUTH ? 0 : -1;
   }
}
