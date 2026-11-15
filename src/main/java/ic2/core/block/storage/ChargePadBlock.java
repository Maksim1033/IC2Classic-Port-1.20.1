package ic2.core.block.storage;

import ic2.api.util.IC2DamageSource;
import ic2.core.block.base.IStateController;
import ic2.core.block.base.blocks.BaseTexturedBlock;
import ic2.core.block.base.tiles.impls.BaseChargePadTileEntity;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.platform.rendering.features.ITextureProvider;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.features.block.IBlockModifiers;
import ic2.core.utils.helpers.Tool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChargePadBlock extends BaseTexturedBlock implements IStateController<BaseChargePadTileEntity>, IBlockModifiers, IBlockModel {
   public static final BooleanProperty ACTIVE = BaseMachineBlock.ACTIVE;
   public static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
   public static final AABB MODEL = new AABB(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
   public static final Properties CHARGE_PAD = Properties.of().sound(SoundType.METAL).strength(1.5F).requiresCorrectToolForDrops();

   public ChargePadBlock(String blockName, BlockEntityType<?> type, ITextureProvider provider) {
      super(blockName, CHARGE_PAD, provider, (BlockEntityType<? extends BlockEntity>)type);
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(ACTIVE, false));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{ACTIVE});
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   @Override
   public AABB getModelBounds(BlockState state) {
      return MODEL;
   }

   @Override
   public boolean isFullCube(BlockState state) {
      return false;
   }

   public void onStateUpdate(Level world, BlockPos pos, BlockState state, BaseChargePadTileEntity tile) {
      tile.setState((BlockState)state.setValue(ACTIVE, tile.isActive()));
   }

   @Override
   public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
      if ((Boolean)stateIn.getValue(ACTIVE)) {
         BlockEntity tile = worldIn.getBlockEntity(pos);
         if (tile instanceof BaseChargePadTileEntity) {
            ((BaseChargePadTileEntity)tile).spawnParticles(rand);
         }
      }
   }

   public void fallOn(Level worldIn, BlockState state, BlockPos pos, Entity entityIn, float fallDistance) {
      if (!worldIn.isClientSide && entityIn instanceof LivingEntity) {
         if (worldIn.getBlockEntity(pos) instanceof BaseChargePadTileEntity pad && pad.data.getMaxImpact() < fallDistance) {
            entityIn.hurt(IC2DamageSource.ELECTRICITY, (float)(3 * pad.data.getBaseTier()));
         }
      }
   }

   public void stepOn(Level worldIn, BlockPos pos, BlockState state, Entity entityIn) {
      if (!worldIn.isClientSide && entityIn instanceof LivingEntity) {
         if (worldIn.getBlockEntity(pos) instanceof BaseChargePadTileEntity pad) {
            if (pad.inactiveTime <= 0) {
               if (pad.isActive()) {
                  pad.activeTime = 20;
               } else {
                  pad.setActive(true);
                  pad.activeTime = 20;
                  pad.tick();
               }
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
      return new float[]{0.0F, 12.0F, 16.0F, 16.0F};
   }
}
