package ic2.core.block.misc;

import ic2.core.block.base.IC2Block;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

public class ResinSheet extends IC2Block implements IBlockModel, IBlockModifiers {
   public static final AABB BOX = new AABB(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
   public static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);

   public ResinSheet() {
      super("resin_sheet", Properties.of().sound(SoundType.SAND).strength(0.5F, 1.6F));
      this.setHarvestTool(Tool.AXE);
   }

   @Override
   public BlockItem createItem() {
      return null;
   }

   @Override
   public AABB getModelBounds(BlockState state) {
      return BOX;
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("misc").get("sticky_resin");
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      pos = pos.below();
      return worldIn.getBlockState(pos).isRedstoneConductor(worldIn, pos);
   }

   public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!this.canSurvive(state, worldIn, pos)) {
         Block.popResource(worldIn, pos, new ItemStack(IC2Items.STICKY_RESIN));
         worldIn.removeBlock(pos, false);
      }
   }

   public void stepOn(Level worldIn, BlockPos pos, BlockState state, Entity entityIn) {
      entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(0.6, 0.8, 0.6));
   }

   public void fallOn(Level worldIn, BlockState state, BlockPos pos, Entity entityIn, float fallDistance) {
      super.fallOn(worldIn, state, pos, entityIn, fallDistance * 0.5F);
   }

   @Override
   public boolean hasTextureRotation(BlockState state, Direction side) {
      return false;
   }

   @Override
   public int getTextureRotation(BlockState state, Direction side) {
      return 0;
   }

   @Override
   public boolean hasCustomTextureUVs(BlockState state, Direction side) {
      return side.getAxis().isHorizontal();
   }

   @Override
   public float[] getCustomTextureUVs(BlockState state, Direction side) {
      return new float[]{0.0F, 12.0F, 16.0F, 16.0F};
   }

   @Override
   public ItemStack createDrop(BlockState state, ItemStack tool, RandomSource rand, BlockEntity tile) {
      return new ItemStack(IC2Items.STICKY_RESIN);
   }
}
