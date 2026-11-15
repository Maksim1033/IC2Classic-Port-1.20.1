package ic2.core.block.transport.item;

import ic2.api.core.IC2Classic;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.features.personal.IPersonalTile;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.block.misc.CFoamBlock;
import ic2.core.block.rendering.block.tubes.MultiTubeModel;
import ic2.core.block.rendering.block.tubes.PipeModel;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.block.IBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.platform.rendering.models.blocks.NonTranslucentBlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class TubeBlocks {
   public static class ActivatableTubeBlock extends TubeBlock {
      String[] textures;
      public static final BooleanProperty ACTIVE = IC2Properties.ACTIVE;

      public ActivatableTubeBlock(String name, BlockEntityType<? extends BlockEntity> type, String... textures) {
         super(name, type, "");
         this.textures = textures;
         this.registerDefaultState((BlockState)this.defaultBlockState().setValue(ACTIVE, false));
      }

      @Override
      protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
         super.createBlockStateDefinition(builder);
         builder.add(new Property[]{ACTIVE});
      }

      @Override
      public void onStateUpdate(Level world, BlockPos pos, BlockState state, TubeTileEntity tile) {
         tile.setState(
            (BlockState)((BlockState)((BlockState)state.setValue(FOAMED, Integer.valueOf(tile.foamed)))
                  .setValue(WATER, (Boolean)state.getValue(WATER) && tile.foamed == 0))
               .setValue(ACTIVE, tile.isActive())
         );
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public TextureAtlasSprite getTexture(BlockState state, boolean center, Direction dir, Direction side) {
         return IC2Textures.getMappedEntriesBlockIC2("transport/tubes").get(this.textures[state.getValue(ACTIVE) ? 1 : 0]);
      }
   }

   public static class ClosedDirectionalTube extends TubeBlocks.DirectionalTubeBlock {
      public ClosedDirectionalTube(String name, BlockEntityType<? extends BlockEntity> type, int searchState, String... texture) {
         super(name, type, searchState, texture);
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public TextureAtlasSprite getTexture(BlockState state, boolean center, Direction dir, Direction side) {
         return IC2Textures.getMappedEntriesBlockIC2("transport/tubes").get(this.textures[side == state.getValue(FACING) ? 1 : 0]);
      }
   }

   public static class DirectionalTubeBlock extends TubeBlock {
      public static final int AUTO_CONNECT_TUBE = 1;
      public static final int AUTO_CONNECT_INVENTORY = 2;
      public static final int AUTO_CONNECT_INVENTORY_TUBE = 3;
      public static final int AUTO_CONNECT_NOTHING = 4;
      public static final DirectionProperty FACING = IC2Properties.ALL_FACINGS;
      String[] textures;
      int searchState;

      public DirectionalTubeBlock(String name, BlockEntityType<? extends BlockEntity> type, int searchState, String... texture) {
         super(name, type, "");
         this.searchState = searchState;
         this.textures = texture;
      }

      @Override
      protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
         super.createBlockStateDefinition(builder);
         builder.add(new Property[]{FACING});
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public TextureAtlasSprite getTexture(BlockState state, boolean center, Direction dir, Direction side) {
         return IC2Textures.getMappedEntriesBlockIC2("transport/tubes").get(this.textures[dir == state.getValue(FACING) ? 1 : 0]);
      }

      @Override
      public void onStateUpdate(Level world, BlockPos pos, BlockState state, TubeTileEntity tile) {
         tile.setState(
            (BlockState)((BlockState)((BlockState)state.setValue(FOAMED, Integer.valueOf(tile.foamed)))
                  .setValue(WATER, (Boolean)state.getValue(WATER) && tile.foamed == 0))
               .setValue(FACING, tile.getFacing())
         );
      }

      @Override
      public BlockState getStateForPlacement(BlockPlaceContext context) {
         return (BlockState)this.defaultBlockState().setValue(FACING, this.createList(context.getLevel(), context.getClickedPos()).getRandomFacing());
      }

      protected DirectionList createList(Level world, BlockPos pos) {
         DirectionList list = DirectionList.EMPTY;

         for (Direction dir : DirectionList.ALL) {
            BlockEntity tile = DirectionList.getNeighborTile(world, pos, dir);
            if (tile == null
               || ((this.searchState & 1) == 0 || !tile.getCapability(IC2Classic.TUBE_CAPABILITY, dir.getOpposite()).isPresent())
                  && ((this.searchState & 2) == 0 || !tile.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).isPresent())) {
               if ((this.searchState & 4) != 0) {
                  list = list.add(dir);
               }
            } else {
               list = list.add(dir);
            }
         }

         return list.isEmpty() ? DirectionList.NORTH : list;
      }

      public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         if (!IC2.PLATFORM.isRendering()) {
            if (worldIn.getBlockEntity(pos) instanceof BaseTileEntity base) {
               base.lock();
               base.setFacing((Direction)state.getValue(FACING));
               if (stack.hasCustomHoverName()) {
                  base.setCustomName(stack.getHoverName());
               }

               base.unlock();
            }
         }
      }
   }

   public static class MultiDirectionalTubeBlock extends TubeBlocks.DirectionalTubeBlock {
      public MultiDirectionalTubeBlock(String name, BlockEntityType<? extends BlockEntity> type, int searchState, String... texture) {
         super(name, type, searchState, texture);
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public BaseModel getForCustomState(BlockState state) {
         return (BaseModel)(state.getValue(FOAMED) == 1 ? super.getForCustomState(state) : new MultiTubeModel(this, state, (Integer)state.getValue(FOAMED) > 0));
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public TextureAtlasSprite getTexture(BlockState state, boolean center, Direction dir, Direction side) {
         return IC2Textures.getMappedEntriesBlockIC2("transport/tubes").get(this.textures[dir == state.getValue(FACING) ? 1 : 0]);
      }
   }

   public static class PersonalTubeBlock extends TubeBlock {
      public PersonalTubeBlock(String name, BlockEntityType<? extends BlockEntity> type, String texture) {
         super(name, type, texture);
      }

      public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         if (!IC2.PLATFORM.isRendering() && placer instanceof Player) {
            if (worldIn.getBlockEntity(pos) instanceof IPersonalTile base) {
               base.setOwner(placer.getUUID());
            }
         }
      }
   }

   public static class PipeTubeBlock extends TubeBlock {
      public PipeTubeBlock(String name, BlockEntityType<? extends BlockEntity> type, String texture) {
         super(name, type, texture);
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public BaseModel getForCustomState(BlockState state) {
         return (BaseModel)(state.getValue(FOAMED) == 1
            ? new NonTranslucentBlockModel(
               (BlockState)IC2Blocks.CFOAM_WET.defaultBlockState().setValue(CFoamBlock.FOAM_TYPE, CFoamBlock.FoamType.CABLE), (IBlockModel)IC2Blocks.CFOAM_WET
            )
            : new PipeModel(this, state));
      }
   }

   public static class PriorityMultiDirTubeBlock extends TubeBlocks.MultiDirectionalTubeBlock {
      public static final IntegerProperty PRIORITY = IntegerProperty.create("priority", 0, 6);

      public PriorityMultiDirTubeBlock(String name, BlockEntityType<? extends BlockEntity> type, int searchState, String... texture) {
         super(name, type, searchState, texture);
      }

      @Override
      protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
         super.createBlockStateDefinition(builder);
         builder.add(new Property[]{PRIORITY});
      }

      @Override
      public void onStateUpdate(Level world, BlockPos pos, BlockState state, TubeTileEntity tile) {
         tile.setState(
            (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(FOAMED, Integer.valueOf(tile.foamed)))
                     .setValue(WATER, (Boolean)state.getValue(WATER) && tile.foamed == 0))
                  .setValue(FACING, tile.getFacing()))
               .setValue(PRIORITY, tile.getPrioritySide())
         );
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public TextureAtlasSprite getTexture(BlockState state, boolean center, Direction dir, Direction side) {
         int priority = (Integer)state.getValue(PRIORITY);
         return IC2Textures.getMappedEntriesBlockIC2("transport/tubes")
            .get(this.textures[dir == state.getValue(FACING) ? 1 : (priority > 0 && Direction.from3DDataValue(priority - 1) == dir ? 2 : 0)]);
      }
   }

   public static class PriorityTubeBlock extends TubeBlocks.DirectionalTubeBlock {
      public static final IntegerProperty PRIORITY = IntegerProperty.create("priority", 0, 6);

      public PriorityTubeBlock(String name, BlockEntityType<? extends BlockEntity> type, int searchState, String... texture) {
         super(name, type, searchState, texture);
      }

      @Override
      protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
         super.createBlockStateDefinition(builder);
         builder.add(new Property[]{PRIORITY});
      }

      @Override
      public void onStateUpdate(Level world, BlockPos pos, BlockState state, TubeTileEntity tile) {
         tile.setState(
            (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(FOAMED, Integer.valueOf(tile.foamed)))
                     .setValue(WATER, (Boolean)state.getValue(WATER) && tile.foamed == 0))
                  .setValue(FACING, tile.getFacing()))
               .setValue(PRIORITY, tile.getPrioritySide())
         );
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public TextureAtlasSprite getTexture(BlockState state, boolean center, Direction dir, Direction side) {
         int priority = (Integer)state.getValue(PRIORITY);
         return IC2Textures.getMappedEntriesBlockIC2("transport/tubes")
            .get(this.textures[dir == state.getValue(FACING) ? 1 : (priority > 0 && Direction.from3DDataValue(priority - 1) == dir ? 2 : 0)]);
      }
   }

   public static class SixSidedTubeBlock extends TubeBlock {
      String[] textures;

      public SixSidedTubeBlock(String name, BlockEntityType<? extends BlockEntity> type, String... textures) {
         super(name, type, "");
         if (textures.length != 7) {
            throw new IllegalStateException("7 Textures are needed");
         } else {
            this.textures = textures;
         }
      }

      @OnlyIn(Dist.CLIENT)
      @Override
      public TextureAtlasSprite getTexture(BlockState state, boolean center, Direction dir, Direction side) {
         return IC2Textures.getMappedEntriesBlockIC2("transport/tubes").get(this.textures[dir == null ? 6 : dir.get3DDataValue()]);
      }
   }

   public static class StickyTubeBlock extends TubeBlocks.PriorityTubeBlock {
      public StickyTubeBlock(String name, BlockEntityType<? extends BlockEntity> type, String... texture) {
         super(name, type, 0, texture);
      }

      @Override
      protected DirectionList createList(Level world, BlockPos pos) {
         return DirectionList.UP;
      }
   }
}
