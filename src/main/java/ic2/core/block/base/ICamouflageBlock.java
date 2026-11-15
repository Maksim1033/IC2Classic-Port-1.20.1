package ic2.core.block.base;

import ic2.core.block.base.features.ICamouflagable;
import ic2.core.block.base.misc.color.IBlockColorListener;
import ic2.core.block.rendering.camouflage.CamouflageStorage;
import ic2.core.platform.rendering.features.ICamouflageRenderType;
import ic2.core.utils.helpers.StackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.minecraftforge.common.extensions.IForgeBlock;

public interface ICamouflageBlock extends IForgeBlock, IBlockColorListener, ICamouflageRenderType {
   private Block self() {
      return (Block)this;
   }

   default CamouflageStorage getTile(LevelAccessor world, BlockPos pos, Direction dir) {
      BlockEntity tile = world.getBlockEntity(pos);
      return tile instanceof ICamouflagable && ((ICamouflagable)tile).isSideEnabled(dir) ? ((ICamouflagable)tile).getStorage() : null;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   default int getItemColor(ItemStack stack, int tintIndex) {
      return -1;
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   default int getBlockColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
      if (world != null && pos != null) {
         BlockEntity tile = world.getBlockEntity(pos);
         if (tile instanceof ICamouflagable) {
            CamouflageStorage storage = ((ICamouflagable)tile).getStorage();
            int dir = tintIndex / 50;
            int index = tintIndex % 50;
            int[] colors = storage.getCache(Direction.from3DDataValue(dir)).getColors();
            return colors.length == 0 ? -1 : colors[index % colors.length];
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   default boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
      if (player.isShiftKeyDown() && world.getBlockEntity(pos) instanceof ICamouflagable cam) {
         ItemLike like = cam.getCamouflageDrop();
         if (cam.removeCamouflage()) {
            if (like != Items.AIR) {
               StackUtil.pop(world, pos, new ItemStack(like));
            }

            this.self().playerWillDestroy(world, pos, state, player);
            return false;
         }
      }

      return IForgeBlock.super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
   }

   @OnlyIn(Dist.CLIENT)
   default Direction getHitSide() {
      try {
         HitResult result = Minecraft.getInstance().hitResult;
         return result instanceof BlockHitResult ? ((BlockHitResult)result).getDirection() : Direction.NORTH;
      } catch (Exception var2) {
         var2.printStackTrace();
         return Direction.NORTH;
      }
   }

   default boolean addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity) {
      CamouflageStorage tile = this.getTile(world, pos, Direction.UP);
      if (tile == null) {
         return false;
      } else {
         try {
            BlockState newState = tile.getCache(Direction.UP).getDisplayBlock();
            if (newState.getRenderShape() != RenderShape.INVISIBLE) {
               Vec3 motion = entity.getDeltaMovement();
               world.addParticle(
                  new BlockParticleOption(ParticleTypes.BLOCK, newState).setPos(pos),
                  entity.getX() + ((double)world.random.nextFloat() - 0.5) * (double)entity.getDimensions(entity.getPose()).width,
                  entity.getY() + 0.1,
                  entity.getZ() + ((double)world.random.nextFloat() - 0.5) * (double)entity.getDimensions(entity.getPose()).width,
                  motion.x * -4.0,
                  1.5,
                  motion.z * -4.0
               );
            }

            return true;
         } catch (Exception var8) {
            return false;
         }
      }
   }

   default boolean addLandingEffects(BlockState state1, ServerLevel serverWorld, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
      CamouflageStorage tile = this.getTile(serverWorld, pos, Direction.UP);
      if (tile == null) {
         return false;
      } else {
         try {
            serverWorld.sendParticles(
               new BlockParticleOption(ParticleTypes.BLOCK, tile.getCache(Direction.UP).getDisplayBlock()),
               (double)pos.getX() + 0.5,
               (double)pos.getY() + 1.0,
               (double)pos.getZ() + 0.5,
               numberOfParticles,
               0.0,
               0.0,
               0.0,
               0.15
            );
            return true;
         } catch (Exception var9) {
            return false;
         }
      }
   }

   public static class CamouflageWrapper implements IClientBlockExtensions {
      ICamouflageBlock owner;

      public CamouflageWrapper(ICamouflageBlock owner) {
         this.owner = owner;
      }

      @OnlyIn(Dist.CLIENT)
      public boolean addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
         Direction dir = this.owner.getHitSide();
         CamouflageStorage tile = this.owner.getTile(world, pos, dir);
         if (tile != null) {
            BlockState newState = tile.getCache(dir).getDisplayBlock();

            try {
               VoxelShape voxelshape = state.getShape(world, pos);
               voxelshape.forAllBoxes(
                  (p_199284_3_, p_199284_5_, p_199284_7_, p_199284_9_, p_199284_11_, p_199284_13_) -> {
                     double d1 = Math.min(1.0, p_199284_9_ - p_199284_3_);
                     double d2 = Math.min(1.0, p_199284_11_ - p_199284_5_);
                     double d3 = Math.min(1.0, p_199284_13_ - p_199284_7_);
                     int i = Math.max(2, Mth.ceil(d1 / 0.25));
                     int j = Math.max(2, Mth.ceil(d2 / 0.25));
                     int k = Math.max(2, Mth.ceil(d3 / 0.25));

                     for (int l = 0; l < i; l++) {
                        for (int i1 = 0; i1 < j; i1++) {
                           for (int j1 = 0; j1 < k; j1++) {
                              double d4 = ((double)l + 0.5) / (double)i;
                              double d5 = ((double)i1 + 0.5) / (double)j;
                              double d6 = ((double)j1 + 0.5) / (double)k;
                              double d7 = d4 * d1 + p_199284_3_;
                              double d8 = d5 * d2 + p_199284_5_;
                              double d9 = d6 * d3 + p_199284_7_;
                              manager.add(
                                 new TerrainParticle(
                                    (ClientLevel)world,
                                    (double)pos.getX() + d7,
                                    (double)pos.getY() + d8,
                                    (double)pos.getZ() + d9,
                                    d4 - 0.5,
                                    d5 - 0.5,
                                    d6 - 0.5,
                                    newState,
                                    pos
                                 )
                              );
                           }
                        }
                     }
                  }
               );
               return true;
            } catch (Exception var9) {
               return false;
            }
         } else {
            return false;
         }
      }

      @OnlyIn(Dist.CLIENT)
      public boolean addHitEffects(BlockState state, Level worldObj, HitResult target, ParticleEngine manager) {
         if (target instanceof BlockHitResult result) {
            CamouflageStorage tile = this.owner.getTile(worldObj, result.getBlockPos(), result.getDirection());
            if (tile == null) {
               return false;
            } else {
               try {
                  BlockState blockstate = tile.getCache(result.getDirection()).getDisplayBlock();
                  BlockPos pos = result.getBlockPos();
                  Direction side = result.getDirection();
                  if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                     int i = pos.getX();
                     int j = pos.getY();
                     int k = pos.getZ();
                     AABB axisAlignedBB = state.getShape(worldObj, pos).bounds();
                     double d0 = (double)i
                        + worldObj.random.nextDouble() * (axisAlignedBB.maxX - axisAlignedBB.minX - 0.20000000298023224)
                        + 0.10000000149011612
                        + axisAlignedBB.minX;
                     double d1 = (double)j
                        + worldObj.random.nextDouble() * (axisAlignedBB.maxY - axisAlignedBB.minY - 0.20000000298023224)
                        + 0.10000000149011612
                        + axisAlignedBB.minY;
                     double d2 = (double)k
                        + worldObj.random.nextDouble() * (axisAlignedBB.maxZ - axisAlignedBB.minZ - 0.20000000298023224)
                        + 0.10000000149011612
                        + axisAlignedBB.minZ;
                     if (side == Direction.DOWN) {
                        d1 = (double)j + axisAlignedBB.minY - 0.10000000149011612;
                     }

                     if (side == Direction.UP) {
                        d1 = (double)j + axisAlignedBB.maxY + 0.10000000149011612;
                     }

                     if (side == Direction.NORTH) {
                        d2 = (double)k + axisAlignedBB.minZ - 0.10000000149011612;
                     }

                     if (side == Direction.SOUTH) {
                        d2 = (double)k + axisAlignedBB.maxZ + 0.10000000149011612;
                     }

                     if (side == Direction.WEST) {
                        d0 = (double)i + axisAlignedBB.minX - 0.10000000149011612;
                     }

                     if (side == Direction.EAST) {
                        d0 = (double)i + axisAlignedBB.maxX + 0.10000000149011612;
                     }

                     manager.add(new TerrainParticle((ClientLevel)worldObj, d0, d1, d2, 0.0, 0.0, 0.0, blockstate, pos).setPower(0.2F).scale(0.6F));
                     return true;
                  }
               } catch (Exception var20) {
               }

               return false;
            }
         } else {
            return false;
         }
      }
   }
}
