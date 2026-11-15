package ic2.core.platform.events;

import ic2.api.blocks.PainterHelper;
import ic2.api.events.FoamEvent;
import ic2.api.events.RetextureEvent;
import ic2.core.block.base.features.ICamouflagable;
import ic2.core.block.cables.CableBlock;
import ic2.core.block.cables.CableTileEntity;
import ic2.core.block.cables.luminator.LuminatorTileEntity;
import ic2.core.block.misc.CFoamBlock;
import ic2.core.block.misc.PlayerDetectorBlock;
import ic2.core.block.misc.base.DyeableBlock;
import ic2.core.block.misc.base.IC2SlabBlock;
import ic2.core.block.misc.base.IC2StairsBlock;
import ic2.core.block.misc.base.IC2WallBlock;
import ic2.core.block.misc.base.ScaffoldBlock;
import ic2.core.block.misc.tiles.PlayerDetectorTileEntity;
import ic2.core.block.misc.tiles.TexturedBlockTileEntity;
import ic2.core.block.transport.fluid.PipeBlock;
import ic2.core.block.transport.fluid.tiles.PipeTileEntity;
import ic2.core.block.transport.item.TubeBlock;
import ic2.core.block.transport.item.TubeTileEntity;
import ic2.core.platform.registries.ColorMaps;
import ic2.core.platform.registries.IC2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class InternalEventHandler {
   public static final InternalEventHandler INSTANCE = new InternalEventHandler();

   public static BlockState applyCamouflage(
      BlockState ownerState, BlockGetter world, BlockPos position, Direction side, BlockPos offsetPos, BlockState neighborState
   ) {
      try {
         BlockEntity tile = world.getBlockEntity(offsetPos);
         if (tile instanceof ICamouflagable able) {
            if (able.isSideEnabled(side.getOpposite())) {
               BlockState state = able.getStorage().getCache(side.getOpposite()).getDisplayBlock();
               if (state != null) {
                  return state;
               }
            }
         } else if (tile instanceof LuminatorTileEntity) {
            BlockState glassState = ((LuminatorTileEntity)tile).getGlassState();
            if (glassState != null) {
               return glassState;
            }
         }
      } catch (Exception var9) {
      }

      return neighborState;
   }

   @SubscribeEvent
   public void onFoamCheck(FoamEvent.Check event) {
      if (!event.isCustomPlacement()) {
         Block block = event.getState().getBlock();
         if (block instanceof ScaffoldBlock) {
            event.setCustomTarget(FoamEvent.TargetType.SCAFFOLD);
         } else if (block instanceof CableBlock) {
            if (event.getBlockEntity() instanceof CableTileEntity cable && cable.foamed == 0) {
               event.setCustomTarget(FoamEvent.TargetType.CABLE);
            }
         } else if (block instanceof TubeBlock) {
            if (event.getBlockEntity() instanceof TubeTileEntity cable && cable.foamed == 0) {
               event.setCustomTarget(FoamEvent.TargetType.TUBE);
            }
         } else if (block instanceof PipeBlock) {
            if (event.getBlockEntity() instanceof PipeTileEntity cable && cable.foamed == 0) {
               event.setCustomTarget(FoamEvent.TargetType.PIPE);
            }
         } else if (block instanceof PlayerDetectorBlock && event.getBlockEntity() instanceof PlayerDetectorTileEntity cable && cable.foamed == 0) {
            event.setCustomTarget(FoamEvent.TargetType.CUSTOM);
         }
      }
   }

   @SubscribeEvent
   public void onFoamPlaced(FoamEvent.Place event) {
      BlockState state = event.getState();
      Block block = state.getBlock();
      if (block == IC2Blocks.SCAFFOLD_IRON) {
         event.getLevel()
            .setBlock(event.getPos(), (BlockState)IC2Blocks.CFOAM_WET.defaultBlockState().setValue(CFoamBlock.FOAM_TYPE, CFoamBlock.FoamType.REINFORCED), 3);
         event.setCanceled(true);
      } else if (block == IC2Blocks.SCAFFOLD_WOOD) {
         Block.dropResources(state, (ServerLevel)event.getLevel(), event.getPos());
         event.requestFoamPlacement();
         event.setCanceled(true);
      } else if (block instanceof CableBlock) {
         if (event.getBlockEntity() instanceof CableTileEntity cable && cable.changeFoam((byte)1, false)) {
            event.setCanceled(true);
         }
      } else if (block instanceof TubeBlock) {
         if (event.getBlockEntity() instanceof TubeTileEntity cable && cable.changeFoam((byte)1, false)) {
            event.setCanceled(true);
         }
      } else if (block instanceof PipeBlock) {
         if (event.getBlockEntity() instanceof PipeTileEntity cable && cable.changeFoam((byte)1, false)) {
            event.setCanceled(true);
         }
      } else if (block instanceof PlayerDetectorBlock && event.getBlockEntity() instanceof PlayerDetectorTileEntity cable && cable.changeFoam((byte)1, false)) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onTextured(RetextureEvent event) {
      if (!event.isApplied() && !event.isCanceled()) {
         BlockState state = event.getBlockState();
         Block block = state.getBlock();
         if (block instanceof DyeableBlock && ((DyeableBlock)block).getColorMap() == ColorMaps.CFOAM_BLOCKS) {
            event.getLevel().setBlock(event.getPos(), IC2Blocks.TEXTURED_BLOCK.defaultBlockState(), 3);
            if (event.getBlockEntity() instanceof TexturedBlockTileEntity tex) {
               tex.setColor(null, ((DyeableBlock)block).getColor(), true);
               if (tex.applyTexture(event.getSide(), event.getContainer())) {
                  event.setApplied(true);
               }
            }
         } else if (block instanceof IC2SlabBlock && ((IC2SlabBlock)block).getColorMap() == ColorMaps.CFOAM_SLABS) {
            event.getLevel().setBlock(event.getPos(), PainterHelper.copyProperties(state, IC2Blocks.TEXTURED_SLAB.defaultBlockState()), 3);
            if (event.getBlockEntity() instanceof TexturedBlockTileEntity texx) {
               texx.setColor(null, ((IC2SlabBlock)block).getColor(), true);
               if (texx.applyTexture(event.getSide(), event.getContainer())) {
                  event.setApplied(true);
               }
            }
         } else if (block instanceof IC2StairsBlock && ((IC2StairsBlock)block).getColorMap() == ColorMaps.CFOAM_STAIRS) {
            event.getLevel().setBlock(event.getPos(), PainterHelper.copyProperties(state, IC2Blocks.TEXTURED_STAIRS.defaultBlockState()), 3);
            if (event.getBlockEntity() instanceof TexturedBlockTileEntity texxx) {
               texxx.setColor(null, ((IC2StairsBlock)block).getColor(), true);
               if (texxx.applyTexture(event.getSide(), event.getContainer())) {
                  event.setApplied(true);
               }
            }
         } else if (block instanceof IC2WallBlock && ((IC2WallBlock)block).getColorMap() == ColorMaps.CFOAM_WALLS) {
            event.getLevel().setBlock(event.getPos(), PainterHelper.copyProperties(state, IC2Blocks.TEXTURED_WALL.defaultBlockState()), 3);
            if (event.getBlockEntity() instanceof TexturedBlockTileEntity texxxx) {
               texxxx.setColor(null, ((IC2WallBlock)block).getColor(), true);
               if (texxxx.applyTexture(event.getSide(), event.getContainer())) {
                  event.setApplied(true);
               }
            }
         } else {
            BlockEntity tile = event.getBlockEntity();
            if (tile instanceof ICamouflagable && ((ICamouflagable)tile).applyTexture(event.getSide(), event.getContainer())) {
               event.setApplied(true);
            }
         }
      }
   }
}
