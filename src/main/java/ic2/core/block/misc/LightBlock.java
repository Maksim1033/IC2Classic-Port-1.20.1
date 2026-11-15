package ic2.core.block.misc;

import ic2.core.IC2;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.rendering.features.block.IEmptyBlockModel;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.plugins.IRegistryProvider;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.SavedData;

public class LightBlock extends AirBlock implements IEmptyBlockModel, IRegistryProvider {
   public static final BooleanProperty EXTRA = BooleanProperty.create("extra");
   public static final BooleanProperty WATER = BlockStateProperties.WATERLOGGED;

   public LightBlock() {
      super(Block.Properties.of().noCollission().air().noLootTable().lightLevel(T -> 15).randomTicks());
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(EXTRA, false)).setValue(WATER, false));
   }

   @Override
   public ResourceLocation getRegistryName() {
      return new ResourceLocation("ic2", "light_block");
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{EXTRA}).add(new Property[]{WATER});
   }

   public RenderShape getRenderShape(BlockState state) {
      return state.getValue(WATER) ? RenderShape.MODEL : RenderShape.INVISIBLE;
   }

   public FluidState getFluidState(BlockState state) {
      return state.getValue(WATER) ? Fluids.WATER.defaultFluidState() : Fluids.EMPTY.defaultFluidState();
   }

   public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
      scheduleTick(worldIn, pos, this);
   }

   public static boolean placeLightBlock(Level world, BlockPos pos, int tickRate, BooleanSupplier canPlace) {
      if (!world.isClientSide && world.getGameTime() % (long)tickRate == 0L && (canPlace == null || canPlace.getAsBoolean())) {
         BlockState state = world.getBlockState(pos);
         if (state.isAir() && state.getBlock() != IC2Blocks.LIGHT_SOURCE) {
            return world.setBlockAndUpdate(pos, IC2Blocks.LIGHT_SOURCE.defaultBlockState());
         }

         if (state.is(IC2Blocks.LIGHT_SOURCE) && !(Boolean)state.getValue(EXTRA)) {
            return world.setBlockAndUpdate(pos, (BlockState)state.setValue(EXTRA, true));
         }

         if (state.is(Blocks.WATER) && state.getFluidState().is(Fluids.WATER)) {
            return world.setBlockAndUpdate(pos, (BlockState)IC2Blocks.LIGHT_SOURCE.defaultBlockState().setValue(WATER, true));
         }
      }

      return false;
   }

   public boolean useShapeForLightOcclusion(BlockState state) {
      return !(Boolean)state.getValue(EXTRA);
   }

   public static void scheduleTick(Level world, BlockPos pos, Block block) {
      if (!world.isClientSide) {
         if (LightBlock.WorldData.addTick((ServerLevel)world, pos)) {
            IC2.TICK_HANDLER.addWorldCallback(world, T -> onTick(T, pos, block), 10);
         }
      }
   }

   public static void onChunkLoaded(ServerLevel world, ChunkPos pos) {
      LongSet list = LightBlock.WorldData.onChunkLoaded(world, pos);
      if (list != null) {
         LongIterator iter = list.iterator();

         while (iter.hasNext()) {
            scheduleTick(world, BlockPos.of(iter.nextLong()), IC2Blocks.LIGHT_SOURCE);
         }
      }
   }

   public static int onTick(Level world, BlockPos pos, Block block) {
      if (world.isLoaded(pos) && world instanceof ServerLevel) {
         BlockState state = world.getBlockState(pos);
         if (state.getBlock() != block) {
            return -1;
         } else if ((Boolean)state.getValue(EXTRA)) {
            world.setBlock(pos, (BlockState)state.setValue(EXTRA, false), 50);
            return 10;
         } else {
            world.setBlock(pos, (state.getValue(WATER) ? Blocks.WATER : Blocks.AIR).defaultBlockState(), 50);
            LightBlock.WorldData.onUnloaded((ServerLevel)world, pos);
            return -1;
         }
      } else {
         return -1;
      }
   }

   private static class WorldData extends SavedData {
      Long2ObjectMap<LongSet> blocksToTick = new Long2ObjectLinkedOpenHashMap();

      public WorldData() {
      }

      public WorldData(CompoundTag compound) {
         for (CompoundTag data : NBTListWrapper.wrap(compound.getList("lights", 10), CompoundTag.class)) {
            this.blocksToTick.put(data.getLong("chunk"), new LongLinkedOpenHashSet(data.getLongArray("pos")));
         }
      }

      public static LightBlock.WorldData get(ServerLevel world) {
         return (LightBlock.WorldData)world.getDataStorage().computeIfAbsent(LightBlock.WorldData::new, LightBlock.WorldData::new, "light_storage");
      }

      public static boolean addTick(ServerLevel world, BlockPos pos) {
         return get(world).add(pos);
      }

      public static void onUnloaded(ServerLevel world, BlockPos pos) {
         get(world).remove(pos);
      }

      public static LongSet onChunkLoaded(ServerLevel world, ChunkPos pos) {
         return get(world).get(pos);
      }

      private boolean add(BlockPos pos) {
         this.setDirty();
         return ((LongSet)this.blocksToTick.computeIfAbsent(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4), T -> new LongLinkedOpenHashSet()))
            .add(pos.asLong());
      }

      private void remove(BlockPos pos) {
         long chunk = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
         LongSet list = (LongSet)this.blocksToTick.get(chunk);
         if (list != null) {
            if (list.remove(pos.asLong())) {
               if (list.isEmpty()) {
                  this.blocksToTick.remove(chunk);
               }

               this.setDirty();
            }
         }
      }

      private LongSet get(ChunkPos pos) {
         this.setDirty();
         return (LongSet)this.blocksToTick.remove(pos.toLong());
      }

      public CompoundTag save(CompoundTag compound) {
         ListTag list = new ListTag();
         ObjectIterator var3 = Long2ObjectMaps.fastIterable(this.blocksToTick).iterator();

         while (var3.hasNext()) {
            Entry<LongSet> entry = (Entry<LongSet>)var3.next();
            CompoundTag nbt = new CompoundTag();
            nbt.putLong("chunk", entry.getLongKey());
            nbt.putLongArray("pos", ((LongSet)entry.getValue()).toLongArray());
            list.add(nbt);
         }

         compound.put("lights", list);
         return compound;
      }
   }
}
