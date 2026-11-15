package ic2.core.block.misc;

import ic2.core.IC2;
import ic2.core.audio.AudioManager;
import ic2.core.block.base.IC2Block;
import ic2.core.item.base.IC2BlockItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.helpers.Tool;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RubberSheet extends IC2Block implements IBlockModel, IBlockModifiers {
   public static final ResourceLocation AUDIO_FILE = new ResourceLocation("ic2", "sounds/misc/trampoline.ogg");
   public static final BooleanProperty SUPPORTED = BooleanProperty.create("supported");
   public static final AABB BOX = new AABB(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
   public static final VoxelShape SHAPE = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);

   public RubberSheet() {
      super("rubber_sheet", Properties.of().sound(SoundType.WOOL).strength(0.8F, 2.0F));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(SUPPORTED, false));
      this.setHarvestTool(Tool.AXE);
   }

   @Override
   public BlockItem createItem() {
      return new IC2BlockItem(this);
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return false;
   }

   @Override
   public TextureAtlasSprite getSpriteForState(BlockState state, Direction side) {
      return IC2Textures.getMappedEntriesBlockIC2("misc").get("rubber_sheet");
   }

   @Override
   public AABB getModelBounds(BlockState state) {
      return BOX;
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{SUPPORTED});
   }

   public void fallOn(Level worldIn, BlockState state, BlockPos pos, Entity entityIn, float fallDistance) {
      if (worldIn.getBlockState(pos.below()).isRedstoneConductor(worldIn, pos.below())) {
         super.fallOn(worldIn, state, pos, entityIn, fallDistance);
      } else if (!this.isSupporting(worldIn, pos)) {
         super.fallOn(worldIn, state, pos, entityIn, fallDistance);
         Block.popResource(worldIn, pos, new ItemStack(IC2Blocks.RUBBER_SHEET));
         worldIn.removeBlock(pos, false);
      } else {
         entityIn.fallDistance = 0.0F;
      }
   }

   public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
      BlockPos pos = entityIn.blockPosition().below(2);
      if (entityIn.getDeltaMovement().y() < -0.4 && !worldIn.getBlockState(pos).isRedstoneConductor(worldIn, pos)) {
         IC2.AUDIO.playSound(entityIn, AUDIO_FILE, AudioManager.SoundType.STATIC, 1.0F, 1.0F);
         entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(1.1, entityIn.isShiftKeyDown() ? -0.1 : -0.8, 1.1));
      } else {
         super.updateEntityAfterFallOn(worldIn, entityIn);
      }
   }

   public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if ((Boolean)state.getValue(SUPPORTED)) {
         worldIn.setBlockAndUpdate(pos, this.defaultBlockState());
         state = this.defaultBlockState();
      }

      if (!this.canSurvive(state, worldIn, pos)) {
         Block.popResource(worldIn, pos, new ItemStack(IC2Blocks.RUBBER_SHEET));
         worldIn.removeBlock(pos, isMoving);
      }
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      return this.isBlockSupporter(worldIn, pos.east(), false)
         || this.isBlockSupporter(worldIn, pos.west(), false)
         || this.isBlockSupporter(worldIn, pos.north(), false)
         || this.isBlockSupporter(worldIn, pos.south(), false);
   }

   public boolean isBlockSupporter(LevelReader world, BlockPos pos, boolean rubberOnly) {
      BlockState state = world.getBlockState(pos);
      return rubberOnly ? state.getBlock() == this : state.isRedstoneConductor(world, pos) || state.getBlock() == this;
   }

   public boolean isSupporting(Level world, BlockPos pos) {
      if ((Boolean)world.getBlockState(pos).getValue(SUPPORTED)) {
         return true;
      } else {
         boolean up = false;
         boolean down = false;
         BlockPos west = pos;

         while (true) {
            BlockState state = world.getBlockState(west);
            if (state.isRedstoneConductor(world, west)) {
               down = true;
               break;
            }

            if (state.getBlock() != this) {
               break;
            }

            west = west.west();
         }

         west = pos;

         while (true) {
            BlockState statex = world.getBlockState(west);
            if (statex.isRedstoneConductor(world, west)) {
               up = true;
               break;
            }

            if (statex.getBlock() != this) {
               break;
            }

            west = west.east();
         }

         if (up && down) {
            world.setBlockAndUpdate(pos, (BlockState)this.defaultBlockState().setValue(SUPPORTED, true));
            return true;
         } else {
            down = false;
            up = false;
            west = pos;

            while (true) {
               BlockState statexx = world.getBlockState(west);
               if (statexx.isRedstoneConductor(world, west)) {
                  down = true;
                  break;
               }

               if (statexx.getBlock() != this) {
                  break;
               }

               west = west.north();
            }

            west = pos;

            while (true) {
               BlockState statexxx = world.getBlockState(west);
               if (statexxx.isRedstoneConductor(world, west)) {
                  up = true;
                  break;
               }

               if (statexxx.getBlock() != this) {
                  break;
               }

               west = west.south();
            }

            if (up && down) {
               world.setBlockAndUpdate(pos, (BlockState)this.defaultBlockState().setValue(SUPPORTED, true));
               return true;
            } else {
               return false;
            }
         }
      }
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
      return new float[]{0.0F, 14.0F, 16.0F, 16.0F};
   }
}
