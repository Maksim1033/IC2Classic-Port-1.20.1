package ic2.core.block.misc;

import ic2.core.block.base.IC2Block;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MiningPipeBlock extends IC2Block implements IBlockModel {
   public static final VoxelShape FULL_HIT = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   public static final VoxelShape SLIM_HIT = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
   public static final AABB SLIM_BOX = new AABB(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
   final boolean slim;

   public MiningPipeBlock(boolean slim) {
      super("miningpipe" + (slim ? "_slim" : ""), Block.Properties.of().sound(SoundType.METAL).strength(6.0F, 10.0F));
      this.setHarvestTool(Tool.PICKAXE);
      this.slim = slim;
   }

   @Override
   public BlockItem createItem() {
      IC2BlockItem item = new IC2BlockItem(this);
      if (!this.slim) {
         item.disableCreative();
      }

      return item;
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      return false;
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return this.slim ? SLIM_HIT : FULL_HIT;
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile) {
      return new ItemStack(IC2Blocks.MINING_PIPE_SHAFT);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("misc").get("mining_pipe_" + (this.slim ? "shaft" : "bottom"));
   }

   @Override
   public AABB getModelBounds(BlockState state) {
      return this.slim ? SLIM_BOX : DEFAULT_BOUNDS;
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return !this.slim;
   }

   public static boolean isMiningPipe(BlockState state) {
      return state.getBlock() == IC2Blocks.MINING_PIPE_BOTTOM || state.getBlock() == IC2Blocks.MINING_PIPE_SHAFT;
   }
}
