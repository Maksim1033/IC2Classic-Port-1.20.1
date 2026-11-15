package ic2.core.block.resource;

import ic2.core.block.base.IC2Block;
import ic2.core.block.rendering.block.SaplingModel;
import ic2.core.item.block.RubberwoodSaplingItem;
import ic2.core.platform.events.WorldGenerator;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

public class RubberwoodSaplingBlock extends IC2Block implements IPlantable, BonemealableBlock, ICustomBlockModel, IRenderType {
   public static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

   public RubberwoodSaplingBlock() {
      super("rubber_sapling", Properties.of().sound(SoundType.GRASS).randomTicks());
   }

   @Override
   public BlockItem createItem() {
      return new RubberwoodSaplingItem(this);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.cutout();
   }

   public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
      if (!this.isValidGround(worldIn.getBlockState(pos.below()), worldIn, pos.below())) {
         Block.dropResources(state, worldIn, pos);
         worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
      } else {
         if (worldIn.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            this.performBonemeal(worldIn, random, pos, state);
         }
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   @Override
   public boolean isValidBonemealTarget(LevelReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
      return true;
   }

   public boolean isBonemealSuccess(Level worldIn, RandomSource rand, BlockPos pos, BlockState state) {
      return true;
   }

   public void performBonemeal(ServerLevel worldIn, RandomSource rand, BlockPos pos, BlockState state) {
      worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);
      if (!WorldGenerator.RUBBER_TREE_SAPLING.place(worldIn, worldIn.getChunkSource().getGenerator(), rand, pos)) {
         worldIn.setBlock(pos, state, 4);
      }
   }

   public BlockState getPlant(BlockGetter world, BlockPos pos) {
      return this.defaultBlockState();
   }

   protected boolean isValidGround(BlockState state, BlockGetter worldIn, BlockPos pos) {
      Block block = state.getBlock();
      return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.PODZOL || block == Blocks.FARMLAND;
   }

   public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
      return !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      BlockPos blockpos = pos.below();
      return state.getBlock() == this
         ? worldIn.getBlockState(blockpos).canSustainPlant(worldIn, blockpos, Direction.UP, this)
         : this.isValidGround(worldIn.getBlockState(blockpos), worldIn, blockpos);
   }

   public PlantType getPlantType(BlockGetter world, BlockPos pos) {
      return PlantType.PLAINS;
   }

   @Override
   public List<BlockState> getCustomStates() {
      return this.getStateDefinition().getPossibleStates();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new SaplingModel(IC2Textures.getMappedEntriesBlockIC2("resources/rubberwood").get("sapling"));
   }
}
