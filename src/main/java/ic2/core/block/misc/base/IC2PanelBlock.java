package ic2.core.block.misc.base;

import ic2.api.blocks.DyeableMap;
import ic2.api.blocks.PainterHelper;
import ic2.core.block.base.IAutoCreator;
import ic2.core.block.base.IC2Block;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.rendering.block.base.RailingModel;
import ic2.core.item.block.RailingItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IC2PanelBlock extends IC2Block implements ICustomBlockModel, IAutoCreator, IDualLogged, PainterHelper.IPaintable {
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;
   public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   private static final VoxelShape[][] SHAPES = new VoxelShape[][]{
      {
            Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0),
            Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0)
      },
      {
            Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 2.0),
            Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 2.0),
            Block.box(14.0, 0.0, 14.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 0.0, 14.0, 2.0, 16.0, 16.0)
      },
      {
            Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 2.0),
            Block.box(14.0, 0.0, 14.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 0.0, 14.0, 2.0, 16.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 2.0)
      },
      {
            Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0), Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0)),
            Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0), Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0)),
            Shapes.or(Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0), Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0)),
            Shapes.or(Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0), Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0))
      },
      {
            Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0), Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0)),
            Shapes.or(Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0), Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0)),
            Shapes.or(Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0), Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0)),
            Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0), Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0))
      }
   };
   private String textureName;
   private String textureFolder;
   private Block material;
   protected DyeableMap colorMap;
   private DyeColor color;
   private Block straight;
   private Block edge;
   private Block corner;

   public IC2PanelBlock(String name, Block material, String textureFolder, String textureName) {
      super(name, Properties.copy(material));
      this.textureName = textureName;
      this.textureFolder = textureFolder;
      this.material = material;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.SOUTH)).setValue(WATER, false))
            .setValue(LAVA, false)
      );
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   public IC2PanelBlock(DyeableBlock material, String name, String texture, DyeableMap colorMap) {
      this(name + material.getColor().getName(), material, texture, material.getColor().getName());
      this.color = material.getColor();
      this.colorMap = colorMap;
      colorMap.addBlock(this, this.color);
   }

   public static IC2PanelBlock createFoamPanel(DyeableBlock material, DyeableMap colorMap) {
      return new IC2PanelBlock(material, "cfoam_panel_", "cfoam/normal", colorMap);
   }

   public void setEdge(Block edge) {
      this.edge = edge;
   }

   public void setCorner(Block corner) {
      this.corner = corner;
   }

   public void setStraight(Block straight) {
      this.straight = straight;
   }

   public Block getCorner() {
      return this.corner;
   }

   public DyeColor getColor() {
      return this.color;
   }

   @Override
   public boolean recolor(BlockState state, Level world, BlockPos pos, Vec3 exactClick, Direction dir, DyeColor color) {
      if (this.colorMap == null) {
         return false;
      } else {
         Block block = this.colorMap.getBlock(color);
         return block != null && world.setBlockAndUpdate(pos, PainterHelper.copyProperties(state, block.defaultBlockState()));
      }
   }

   @Override
   public DyeColor getColor(BlockState state) {
      return this.color;
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      return SHAPES[((StairsShape)state.getValue(SHAPE)).ordinal()][((Direction)state.getValue(FACING)).get2DDataValue()];
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new RailingModel(state, this.textureFolder, this.textureName);
   }

   @Override
   public BlockItem createItem() {
      return new RailingItem(this, new net.minecraft.world.item.Item.Properties());
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING}).add(new Property[]{SHAPE}).add(new Property[]{WATER}).add(new Property[]{LAVA});
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
      if (Block.byItem(context.getItemInHand().getItem()) instanceof IC2PanelBlock && context.getPlayer() != null & context.getPlayer().isShiftKeyDown()) {
         StairsShape shape = (StairsShape)state.getValue(SHAPE);
         if (this.straight != null && shape == StairsShape.STRAIGHT) {
            return true;
         } else {
            return this.edge == null || shape != StairsShape.OUTER_LEFT && shape != StairsShape.OUTER_RIGHT
               ? this.corner != null && (shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT)
               : true;
         }
      } else {
         return false;
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
      StairsShape shape = context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
         ? StairsShape.STRAIGHT
         : this.getShapeProperty(state, context.getLevel(), context.getClickedPos(), context.getClickLocation());
      if (shape == StairsShape.STRAIGHT) {
         BlockState original = context.getLevel().getBlockState(context.getClickedPos());
         if (original.getBlock() instanceof IC2PanelBlock) {
            StairsShape otherShape = (StairsShape)original.getValue(SHAPE);
            Direction facing = (Direction)original.getValue(FACING);
            if (this.straight != null && otherShape == StairsShape.STRAIGHT) {
               DyeColor orColor = ((IC2PanelBlock)original.getBlock()).color;
               boolean negative = facing.getAxisDirection() == AxisDirection.NEGATIVE;
               return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.straight
                              .defaultBlockState()
                              .setValue(IC2DoubleRailingBlock.AXIS, facing.getAxis() == Axis.Z))
                           .setValue(IC2DoubleRailingBlock.MAIN_COLOR, negative ? this.color : orColor))
                        .setValue(IC2DoubleRailingBlock.SECOND_COLOR, negative ? orColor : this.color))
                     .setValue(WATER, (Boolean)original.getValue(WATER)))
                  .setValue(LAVA, (Boolean)original.getValue(LAVA));
            }

            if (this.edge == null || otherShape != StairsShape.OUTER_LEFT && otherShape != StairsShape.OUTER_RIGHT) {
               if (this.corner == null || otherShape != StairsShape.INNER_LEFT && otherShape != StairsShape.INNER_RIGHT) {
                  return original;
               }

               return (BlockState)((BlockState)this.corner.defaultBlockState().setValue(WATER, (Boolean)original.getValue(WATER)))
                  .setValue(LAVA, (Boolean)original.getValue(LAVA));
            }

            DyeColor orColor = ((IC2PanelBlock)original.getBlock()).color;
            return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.edge
                              .defaultBlockState()
                              .setValue(IC2EdgeRailingBlock.FACING, facing))
                           .setValue(IC2EdgeRailingBlock.RIGHT, otherShape == StairsShape.OUTER_RIGHT))
                        .setValue(IC2EdgeRailingBlock.MAIN_COLOR, orColor))
                     .setValue(IC2EdgeRailingBlock.SECOND_COLOR, this.color))
                  .setValue(WATER, (Boolean)original.getValue(WATER)))
               .setValue(LAVA, (Boolean)original.getValue(LAVA));
         }
      }

      FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)((BlockState)((BlockState)state.setValue(SHAPE, shape)).setValue(WATER, fluid.getType() == Fluids.WATER))
         .setValue(LAVA, fluid.getType() == Fluids.LAVA);
   }

   public FluidState getFluidState(BlockState state) {
      return IDualLogged.getFluidState(state);
   }

   public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if ((Boolean)state.getValue(WATER)) {
         world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
      } else if ((Boolean)state.getValue(LAVA)) {
         world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
      }
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return (BlockState)state.setValue(FACING, mirror.mirror((Direction)state.getValue(FACING)));
   }

   public StairsShape getShapeProperty(BlockState state, BlockGetter blockReader, BlockPos pos, Vec3 exactClick) {
      Direction direction = (Direction)state.getValue(FACING);
      Direction place = direction.getClockWise();
      double exact = exactClick.subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ())
         .multiply((double)Math.abs(place.getStepX()), 0.0, (double)Math.abs(place.getStepZ()))
         .length();
      boolean right = direction.getAxisDirection() == (direction.getAxis() == Axis.X ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE)
         ? exact > 0.5
         : exact < 0.5;
      BlockState otherState = blockReader.getBlockState(pos.relative(direction));
      if (this.isRailing(otherState)) {
         Direction otherFacing = (Direction)otherState.getValue(FACING);
         if (otherFacing.getAxis() != ((Direction)state.getValue(FACING)).getAxis()
            && this.isDifferentRailing(state, blockReader, pos, otherFacing.getOpposite())) {
            return otherFacing == direction.getCounterClockWise() ? StairsShape.OUTER_LEFT : StairsShape.OUTER_RIGHT;
         }
      } else if (this.isDoubleRailing(otherState, direction) || this.isEdgeRailing(otherState, direction) || this.isCornerRailing(otherState, direction, right)
         )
       {
         return right ? StairsShape.OUTER_RIGHT : StairsShape.OUTER_LEFT;
      }

      otherState = blockReader.getBlockState(pos.relative(direction.getOpposite()));
      if (this.isRailing(otherState)) {
         Direction otherFacing = (Direction)otherState.getValue(FACING);
         if (otherFacing.getAxis() != ((Direction)state.getValue(FACING)).getAxis() && this.isDifferentRailing(state, blockReader, pos, otherFacing)) {
            return otherFacing == direction.getCounterClockWise() ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT;
         }
      } else if (this.isDoubleRailing(otherState, direction) || this.isEdgeRailing(otherState, direction) || this.isCornerRailing(otherState, direction, right)
         )
       {
         return right ? StairsShape.INNER_LEFT : StairsShape.INNER_RIGHT;
      }

      return StairsShape.STRAIGHT;
   }

   public boolean isDifferentRailing(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
      BlockState blockstate = world.getBlockState(pos.relative(face));
      return !this.isRailing(blockstate) || blockstate.getValue(FACING) != state.getValue(FACING);
   }

   public boolean isCornerRailing(BlockState state, Direction direction, boolean right) {
      return state.getBlock() instanceof IC2CornerRailingBlock
         && ((Integer)state.getValue(IC2CornerRailingBlock.COLLISION) & 1 << this.calcIndex(direction, right)) != 0;
   }

   public boolean isEdgeRailing(BlockState state, Direction direction) {
      if (!(state.getBlock() instanceof IC2EdgeRailingBlock)) {
         return false;
      } else {
         Direction facing = (Direction)state.getValue(IC2EdgeRailingBlock.FACING);
         return facing.getOpposite() == direction
            || (state.getValue(IC2EdgeRailingBlock.RIGHT) ? facing.getCounterClockWise() : facing.getClockWise()) == direction;
      }
   }

   public boolean isDoubleRailing(BlockState state, Direction direction) {
      return state.getBlock() instanceof IC2DoubleRailingBlock && direction.getAxis() == Axis.Z != (Boolean)state.getValue(IC2DoubleRailingBlock.AXIS);
   }

   public int calcIndex(Direction dir, boolean right) {
      return (dir.get2DDataValue() + (right ? 1 : 0)) % 4;
   }

   public boolean isRailing(BlockState state) {
      return state.getBlock() instanceof IC2PanelBlock;
   }
}
