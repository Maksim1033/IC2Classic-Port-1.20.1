package ic2.core.block.personal.blocks;

import ic2.core.IC2;
import ic2.core.block.base.misc.IDualLogged;
import ic2.core.block.personal.PersonalBlock;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IOptionalTileModel;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class PersonalChestBlock extends PersonalBlock implements IOptionalTileModel, IDualLogged {
   protected static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty LAVA = IC2Properties.LAVA_LOGGED;

   public PersonalChestBlock() {
      super(
         "personal_chest",
         ITextureProvider.noStateIC2("personal/chest"),
         IC2Tiles.PERSONAL_CHEST,
         Properties.of().strength(-1.0F, 6000000.0F)
      );
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(LAVA, false)).setValue(WATER, false));
   }

   @Override
   public boolean shouldTileRender() {
      return true;
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this, new net.minecraft.world.item.Item.Properties()) {
         public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(IC2.PLATFORM.getTileRenderer());
         }
      };
   }

   @Override
   public RenderShape getRenderShape(BlockState state) {
      return this.shouldTileRender() ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState state = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)((BlockState)super.getStateForPlacement(context).setValue(WATER, state.getType() == Fluids.WATER))
         .setValue(LAVA, state.getType() == Fluids.LAVA);
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{WATER}).add(new Property[]{LAVA});
   }

   @Override
   public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
      if ((Boolean)state.getValue(WATER)) {
         world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
      } else if ((Boolean)state.getValue(LAVA)) {
         world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
      }
   }

   public FluidState getFluidState(BlockState state) {
      return IDualLogged.getFluidState(state);
   }
}
