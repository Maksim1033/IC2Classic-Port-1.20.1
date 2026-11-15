package ic2.core.block.multi;

import ic2.api.util.DirectionList;
import ic2.core.block.base.blocks.BaseCTBlock;
import ic2.core.block.base.blocks.ValveBlock;
import ic2.core.block.generators.TurbineBladeBlock;
import ic2.core.block.machines.BaseMachineBlock;
import ic2.core.block.rendering.block.TurbineMultiModel;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Properties;
import ic2.core.platform.rendering.IC2Textures;
import ic2.core.platform.rendering.features.IRenderType;
import ic2.core.platform.rendering.features.block.ICustomBlockModel;
import ic2.core.platform.rendering.models.BaseModel;
import ic2.core.utils.helpers.Tool;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TurbineMultiBlock extends BaseCTBlock implements IRenderType, ICustomBlockModel {
   public static final BooleanProperty ACTIVE = IC2Properties.ACTIVE;
   public static final IntegerProperty FORMED = IntegerProperty.create("formed", 0, 4);

   public TurbineMultiBlock() {
      super("turbine_multi_block", BaseMachineBlock.BASE_MACHINE, "electric/generator/steam_tunnel/structure", null);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(ACTIVE, false)).setValue(FORMED, 4)).setValue(CONNECTION, 0)
      );
      this.setHarvestTool(Tool.PICKAXE.withLevel(1));
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{ACTIVE, FORMED});
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public RenderType getType() {
      return RenderType.cutout();
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public BaseModel getForCustomState(BlockState state) {
      return new TurbineMultiModel(
         state,
         this,
         IC2Textures.getMappedEntriesBlockIC2("electric/generator/steam_tunnel").get(state.getValue(ACTIVE) ? "active_overlay" : "inactive_overlay")
      );
   }

   @Override
   public List<BlockState> getModelStates() {
      return Collections.emptyList();
   }

   @Override
   protected boolean isBlockValid(BlockState state, BlockState self) {
      return state.is(this) || state.is(IC2Blocks.STEAM_TUNNEL) || state.getBlock() instanceof ValveBlock;
   }

   public static Long2ObjectMap<BlockState> createMultiBlockFrame(Box box) {
      Long2ObjectMap<BlockState> states = new Long2ObjectLinkedOpenHashMap();

      for (BlockPos pos : box.getHollowIterator()) {
         if (pos.getX() < 1 || box.isEdge(pos)) {
            states.put(pos.asLong(), IC2Blocks.STEAM_TUNNEL_MULTIBLOCK.defaultBlockState());
         }
      }

      for (int i = 0; i < 9; i++) {
         if (i != 4) {
            states.put(
               BlockPos.asLong(2, i / 3, i % 3 - 1),
               (BlockState)IC2Blocks.STEAM_TUNNEL_MULTIBLOCK.defaultBlockState().setValue(FORMED, Direction.EAST.get2DDataValue())
            );
         }
      }

      states.put(BlockPos.asLong(0, 1, -1), IC2Blocks.VALVE.defaultBlockState());
      states.put(BlockPos.asLong(0, 1, 1), IC2Blocks.VALVE.defaultBlockState());
      states.put(BlockPos.asLong(2, 1, 0), TurbineBladeBlock.createState(IC2Blocks.STEAM_TUNNEL.defaultBlockState(), false, Direction.EAST, 5));
      ObjectIterator var6 = Long2ObjectMaps.fastIterable(states).iterator();

      while (var6.hasNext()) {
         Entry<BlockState> entry = (Entry<BlockState>)var6.next();
         BlockState state = (BlockState)entry.getValue();
         if (state.is(IC2Blocks.STEAM_TUNNEL_MULTIBLOCK)) {
            entry.setValue((BlockState)state.setValue(CONNECTION, getConnections(entry.getLongKey(), states).getCode()));
         }
      }

      return states;
   }

   public static BlockState create(int facing, boolean active) {
      return (BlockState)((BlockState)IC2Blocks.STEAM_TUNNEL_MULTIBLOCK.defaultBlockState().setValue(ACTIVE, active)).setValue(FORMED, facing);
   }

   public static DirectionList getConnections(long pos, Long2ObjectMap<BlockState> states) {
      DirectionList connections = DirectionList.EMPTY;

      for (Direction dir : DirectionList.ALL) {
         BlockState state = (BlockState)states.get(BlockPos.offset(pos, dir));
         if (state != null && (state.is(IC2Blocks.STEAM_TUNNEL_MULTIBLOCK) || state.is(IC2Blocks.STEAM_TUNNEL) || state.getBlock() instanceof ValveBlock)) {
            connections = connections.add(dir);
         }
      }

      return connections;
   }
}
