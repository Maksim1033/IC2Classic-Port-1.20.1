package ic2.core.block.misc.explosive;

import ic2.api.util.DirectionList;
import ic2.core.block.misc.base.IC2ExplosiveBlock;
import ic2.core.block.rendering.block.DynamiteModel;
import ic2.core.entity.explosion.DynamiteEntity;
import ic2.core.item.block.DynamiteItem;
import ic2.core.platform.registries.IC2Entities;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DynamiteBlock extends IC2ExplosiveBlock implements ICustomBlockModel, IRenderType {
   public static final VoxelShape[] SHAPES = new VoxelShape[]{
      Block.box(6.0, 0.0, 6.0, 10.0, 10.0, 10.0),
      null,
      Block.box(5.5, 3.0, 11.0, 10.5, 13.0, 16.0),
      Block.box(5.5, 3.0, 0.0, 10.5, 13.0, 5.0),
      Block.box(11.0, 3.0, 5.5, 16.0, 13.0, 10.5),
      Block.box(0.0, 3.0, 5.5, 5.0, 13.0, 10.5)
   };
   public static final AABB[] MODELS = new AABB[]{
      new AABB(6.0, 0.0, 6.0, 10.0, 10.0, 10.0),
      null,
      new AABB(5.5, 3.0, 11.0, 10.5, 13.0, 16.0),
      new AABB(5.5, 3.0, 0.0, 10.5, 13.0, 5.0),
      new AABB(11.0, 3.0, 5.5, 16.0, 13.0, 10.5),
      new AABB(0.0, 3.0, 5.5, 5.0, 13.0, 10.5)
   };
   public static final BooleanProperty LINKED = BooleanProperty.create("linked");
   public static final DirectionProperty DIRECTION = DirectionProperty.create("side", DirectionList.UP.invert().toFacings());

   public DynamiteBlock() {
      super("dynamite", null, null, true);
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(LINKED, false)).setValue(DIRECTION, Direction.DOWN));
   }

   @Override
   public BlockItem createItem() {
      return new DynamiteItem(this);
   }

   @Override
   public Entity getExplosiveEntity(Level world, double x, double y, double z, BlockState state, boolean explosion) {
      DynamiteEntity dynamite = new DynamiteEntity(
         IC2Entities.DYNAMITE, world, x, y, z, state.getValue(DIRECTION) != Direction.DOWN ? BlockPos.containing(x, y, z) : null
      );
      dynamite.setItem(new ItemStack(this));
      dynamite.setFuse(explosion ? 40 : 5);
      return dynamite;
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPES[((Direction)state.getValue(DIRECTION)).get3DDataValue()];
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.cutout();
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = this.defaultBlockState();
      Direction facing = context.getClickedFace();
      if (facing != Direction.DOWN
         && this.canSurvive(
            (BlockState)state.setValue(DIRECTION, facing == Direction.UP ? facing.getOpposite() : facing), context.getLevel(), context.getClickedPos()
         )) {
         state = (BlockState)state.setValue(DIRECTION, facing == Direction.UP ? facing.getOpposite() : facing);
      }

      return state;
   }

   public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
      return !this.canSurvive(stateIn, worldIn, currentPos)
         ? Blocks.AIR.defaultBlockState()
         : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      Direction dir = (Direction)state.getValue(DIRECTION);
      if (dir.getAxis().isVertical()) {
         dir = Direction.UP;
      }

      BlockPos newPos = pos.relative(dir.getOpposite());
      return worldIn.getBlockState(newPos).isFaceSturdy(worldIn, newPos, dir, SupportType.RIGID);
   }

   @Override
   public AABB getModelBounds(BlockState state) {
      return MODELS[((Direction)state.getValue(DIRECTION)).get3DDataValue()];
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("misc/explosive/dynamite").get(state.getValue(LINKED) ? "linked_dynamite" : "unlinked_dynamite");
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{LINKED}).add(new Property[]{DIRECTION});
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new DynamiteModel(this.getSpriteForState(state, Direction.NORTH), (Direction)state.getValue(DIRECTION));
   }

   @Override
   public List<BlockState> getModelStates() {
      return Collections.emptyList();
   }

   public static void setLinked(BlockState state, Level world, BlockPos pos, boolean linked) {
      world.setBlockAndUpdate(pos, (BlockState)state.setValue(LINKED, linked));
   }

   public static boolean isLinked(BlockState state) {
      return (Boolean)state.getValue(LINKED);
   }

   public static void ignite(BlockState state, Level world, BlockPos pos) {
      world.addFreshEntity(
         ((DynamiteBlock)state.getBlock()).getExplosiveEntity(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, state, true)
      );
      world.removeBlock(pos, false);
   }
}
