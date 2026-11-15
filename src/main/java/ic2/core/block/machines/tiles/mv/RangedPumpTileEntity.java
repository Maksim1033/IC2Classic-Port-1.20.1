package ic2.core.block.machines.tiles.mv;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IFluidScanner;
import ic2.api.tiles.readers.IProgressMachine;
import ic2.api.util.DirectionList;
import ic2.core.block.base.cache.CapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.misc.comparator.types.base.TankComparator;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.containers.mv.RangedPumpContainer;
import ic2.core.block.misc.MiningPipeBlock;
import ic2.core.block.transport.fluid.graph.FluidNet;
import ic2.core.block.transport.fluid.graph.ISimpleFluidSource;
import ic2.core.fluid.ExtractionTank;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.ClassFilter;
import ic2.core.inventory.filter.SpecialFilters;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.AABBUtil;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;

public class RangedPumpTileEntity extends BaseElectricTileEntity implements ITickListener, ITileGui, IProgressMachine, ITileActivityProvider, ISimpleFluidSource {
   ExtractionTank tank = new ExtractionTank(4000);
   public int progress = 0;
   RangedPumpTileEntity.PumpTarget target = null;
   int delay = 0;
   boolean isSearching = false;
   boolean searchDone = false;
   boolean addedToFluidNet = false;
   LinkedList<RangedPumpTileEntity.PumpTarget> targets = new LinkedList<>();
   ICache<IFluidHandler> fluids = new CapabilityCache<>(this, DirectionList.DOWN.invert(), ForgeCapabilities.FLUID_HANDLER);
   ICache<IItemHandler> inventories = new CapabilityCache<>(this, DirectionList.HORIZONTAL, ForgeCapabilities.ITEM_HANDLER);

   public RangedPumpTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 3, 128, 5000);
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, this.tank);
      this.addCaches(new ICache[]{this.fluids, this.inventories});
      this.setFuelSlot(0);
      this.addComparator(new TankComparator("tank", ComparatorNames.TANK, this.tank));
      this.addComparator(new ProgressComparator("progress", ComparatorNames.PROGRESS, this));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.DOWN.invert());
      handler.registerBlockAccess(DirectionList.DOWN.invert(), AccessRule.BOTH);
      handler.registerBlockAccess(DirectionList.DOWN, AccessRule.DISABLED);
      handler.registerSlotAccess(AccessRule.BOTH, 0, 1, 2);
      handler.registerSlotsForSide(DirectionList.UP.invert(), 1);
      handler.registerSlotsForSide(DirectionList.HORIZONTAL, 0, 2);
      handler.registerInputFilter(SpecialFilters.createChargeFilter(), 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_DISCHARGE_FILTER, 0);
      handler.registerInputFilter(SpecialFilters.BLOCK_FILTER, 1);
      handler.registerInputFilter(new ClassFilter(IFluidScanner.class), 2);
      handler.registerNamedSlot(SlotType.BATTERY, 0);
      handler.registerNamedSlot(SlotType.MINING_PIPES, 1);
      handler.registerNamedSlot(SlotType.TOOLS, 2);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("tank", this.tank.writeToNBT(new CompoundTag()));
      compound.putByte("progress", (byte)this.progress);
      if (this.target != null) {
         compound.put("target", this.target.write());
      }

      ListTag list = new ListTag();

      for (RangedPumpTileEntity.PumpTarget target : this.targets) {
         list.add(target.write());
      }

      compound.put("queue", list);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.tank.readFromNBT(compound.getCompound("tank"));
      this.progress = compound.getInt("progress");
      if (compound.contains("target")) {
         CompoundTag nbt = compound.getCompound("target");
         this.target = (RangedPumpTileEntity.PumpTarget)(nbt.getBoolean("dig")
            ? new RangedPumpTileEntity.DownTarget(nbt)
            : new RangedPumpTileEntity.PumpTarget(nbt));
      }

      this.targets.clear();

      for (CompoundTag nbt : NBTListWrapper.wrap(compound.getList("queue", 10), CompoundTag.class)) {
         this.targets.add(new RangedPumpTileEntity.PumpTarget(nbt));
      }
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.isSimulating() && !this.addedToFluidNet) {
         FluidNet.INSTANCE.addPipe(this);
         this.addedToFluidNet = true;
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.addedToFluidNet) {
         FluidNet.INSTANCE.removePipe(this);
         this.addedToFluidNet = false;
      }

      super.onUnloaded(chunk);
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.RANGED_PUMP;
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new RangedPumpContainer(this, player, windowID);
   }

   @Override
   public float getProgress() {
      return (float)this.progress;
   }

   @Override
   public float getMaxProgress() {
      return 5.0F;
   }

   @Override
   public void onTick() {
      this.handleChargeSlot(this.maxEnergy);
      if (this.hasEnergy(1)) {
         ItemStack stack = (ItemStack)this.inventory.get(2);
         if (!stack.isEmpty()) {
            this.useEnergy(ElectricItem.MANAGER.charge(stack, this.energy, 2, true, false));
         }
      }

      this.exportFluids();
      if (this.tank.getFluidAmount() > 0) {
         int drained = FluidNet.INSTANCE.sendFluid(this, null, this.tank.getFluid(), 100);
         if (drained > 0) {
            this.tank.drain(drained, FluidAction.EXECUTE);
         }
      }

      if (this.delay > 0) {
         this.delay--;
         this.setActive(false);
         this.handleComparators();
      } else if (this.isOperating()) {
         this.work();
         this.setActive(true);
         this.handleComparators();
      } else if (((ItemStack)this.inventory.get(2)).isEmpty()
         && this.hasEnergy(2)
         && MiningPipeBlock.isMiningPipe(this.getLevel().getBlockState(this.worldPosition.below()))) {
         this.useEnergy(2);
         this.progress++;
         this.targets.clear();
         this.target = null;
         if (this.progress >= 5) {
            this.progress = 0;
            this.withdrawPipe();
         }

         this.setActive(true);
         this.handleComparators();
      } else {
         this.setActive(false);
         this.handleComparators();
      }
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.isSimulating() && slot == 2) {
         this.progress = 0;
         this.targets.clear();
         this.target = null;
      }
   }

   @Override
   public boolean canReceiveFluid(Direction dir) {
      return false;
   }

   public void exportFluids() {
      InventoryHandler handler = this.getInventoryHandler();

      for (Direction dir : this.fluids) {
         if (handler.isBlockSideUnlocked(dir) && handler.getBlockAccess(dir).canExport()) {
            IFluidHandler entry = this.fluids.getHandler(dir);
            if (entry != null) {
               this.tank.drain(entry.fill(this.tank.drain(1000, FluidAction.SIMULATE), FluidAction.EXECUTE), FluidAction.EXECUTE);
               if (this.tank.getFluidAmount() <= 0) {
                  return;
               }
            }
         }
      }
   }

   public void withdrawPipe() {
      BlockPos target = this.getPipeTip();
      if (!this.getLevel().isEmptyBlock(target)) {
         if (this.getBlockPos().getY() - target.getY() > 0) {
            List<ItemStack> list = Block.getDrops(this.getLevel().getBlockState(target), (ServerLevel)this.getLevel(), target, null);
            if (!this.sendItemsAway(list)) {
               for (int i = 0; i < list.size(); i++) {
                  Block.popResource(this.getLevel(), this.getBlockPos(), list.get(i));
               }
            }
         }

         this.getLevel().removeBlock(target, false);
      }

      if (((ItemStack)this.inventory.get(1)).getItem() != IC2Blocks.MINING_PIPE_SHAFT.asItem()
         && Block.byItem(((ItemStack)this.inventory.get(1)).getItem()) != Blocks.AIR) {
         this.getLevel().setBlockAndUpdate(target, Block.byItem(((ItemStack)this.inventory.get(1)).getItem()).defaultBlockState());
         ((ItemStack)this.inventory.get(1)).shrink(1);
      }

      this.updateTip(target.getY() + 1);
   }

   public boolean sendItemsAway(List<ItemStack> list) {
      InventoryHandler handler = this.getInventoryHandler();

      for (Direction dir : this.inventories) {
         if (handler.isBlockSideUnlocked(dir) && handler.getBlockAccess(dir).canExport()) {
            IItemTransporter trans = TransporterManager.getTransporter(this.inventories.getHandler(dir));
            if (trans != null) {
               for (int i = 0; i < list.size(); i++) {
                  ItemStack stack = list.get(i);
                  if (stack.isEmpty()) {
                     list.remove(i--);
                  } else {
                     stack.shrink(trans.addItem(stack, dir.getOpposite(), false));
                     if (stack.getCount() <= 0) {
                        list.remove(i--);
                     }
                  }
               }
            }
         }
      }

      return list.isEmpty();
   }

   public boolean isOperating() {
      return this.hasEnergy(100) && this.canOperate();
   }

   private boolean canOperate() {
      return (((ItemStack)this.inventory.get(1)).getItem() == IC2Blocks.MINING_PIPE_SHAFT.asItem() || !this.targets.isEmpty())
         && ((ItemStack)this.inventory.get(2)).getItem() instanceof IFluidScanner;
   }

   public void work() {
      if (this.target == null) {
         this.findTarget();
         if (this.target == null) {
            this.delay = 50;
         }
      } else if (this.progress < 5) {
         this.progress++;
         this.useEnergy(10);
         if (!this.target.isDigTarget()) {
            BlockPos pos = this.target.getPos();
            ((ServerLevel)this.getLevel())
               .sendParticles(ParticleTypes.BUBBLE, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 5, 0.2, 0.0, 0.2, 0.2);
         }
      } else if (!this.target.isDigTarget() && this.tank.fillInternal(this.target.getFluids(), FluidAction.SIMULATE) < this.target.getFluids().getAmount()) {
         this.delay = 5;
      } else {
         this.tank.fillInternal(this.target.getFluids(), FluidAction.EXECUTE);
         this.useEnergy(10);
         this.progress = 0;
         if (this.target.getPos().getX() == this.getBlockPos().getX() && this.target.getPos().getZ() == this.getBlockPos().getZ()) {
            this.getLevel().setBlockAndUpdate(this.target.getPos(), IC2Blocks.MINING_PIPE_SHAFT.defaultBlockState());
            ((ItemStack)this.inventory.get(1)).shrink(1);
            this.updateTip(this.target.getPos().getY());
            this.targets.clear();
            this.target = null;
         } else {
            BlockState state = this.getLevel().getBlockState(this.target.getPos());
            if (state.getBlock() instanceof BucketPickup) {
               if (this.target.isDigTarget()) {
                  this.getLevel().setBlock(this.target.getPos(), Blocks.AIR.defaultBlockState(), 11);
               } else {
                  ((BucketPickup)state.getBlock()).pickupBlock(this.getLevel(), this.target.getPos(), state);
               }

               if (this.getLevel() instanceof ServerLevel server) {
                  server.getFluidTicks().clearArea(BoundingBox.fromCorners(this.target.getPos().offset(-2, -2, -2), this.target.getPos().offset(2, 2, 2)));
               }
            }

            if (this.target.getPos().getY() <= this.getPipeTip().getY()) {
               this.updateTip(this.target.getPos().getY());
            }

            this.target = null;
         }
      }
   }

   public void updateTip(int low) {
      if (low != this.getBlockPos().getY()) {
         MutableBlockPos target;
         for (target = this.getBlockPos().mutable().move(Direction.DOWN); target.getY() > low; target.setY(target.getY() - 1)) {
            BlockState state = this.getLevel().getBlockState(target);
            boolean miningPipe = MiningPipeBlock.isMiningPipe(state);
            boolean isTip = state.getBlock() == IC2Blocks.MINING_PIPE_BOTTOM;
            if (!miningPipe && ((ItemStack)this.inventory.get(1)).getCount() > 0 || isTip) {
               this.getLevel().setBlockAndUpdate(target, IC2Blocks.MINING_PIPE_SHAFT.defaultBlockState());
               if (!miningPipe) {
                  ((ItemStack)this.inventory.get(1)).shrink(1);
               }
            }
         }

         this.getLevel().setBlockAndUpdate(target, IC2Blocks.MINING_PIPE_BOTTOM.defaultBlockState());
      }
   }

   public void setTarget(RangedPumpTileEntity.PumpTarget target) {
      if (target.isTargetValid(this.getLevel())) {
         this.target = target;
      } else {
         this.target = null;
      }
   }

   public BlockPos getPipeTip() {
      MutableBlockPos pipe = this.getBlockPos().mutable().move(Direction.DOWN);

      while (pipe.getY() >= this.getLevel().getMinBuildHeight()) {
         BlockState state = this.getLevel().getBlockState(pipe);
         if (!MiningPipeBlock.isMiningPipe(state)) {
            pipe.move(Direction.UP);
            break;
         }

         pipe.move(Direction.DOWN);
      }

      return pipe.immutable();
   }

   public void findTarget() {
      if (!this.isSearching) {
         BlockPos pos = this.getPipeTip();
         if (pos.getY() >= this.getBlockPos().getY() || !(((ItemStack)this.inventory.get(2)).getItem() instanceof IFluidScanner)) {
            pos = pos.below();
            BlockState state = this.getLevel().getBlockState(pos);
            this.setTarget(
               (RangedPumpTileEntity.PumpTarget)(this.hasFluids(state, pos)
                  ? new RangedPumpTileEntity.PumpTarget(pos, state, new FluidStack(state.getFluidState().getType(), 1000))
                  : new RangedPumpTileEntity.DownTarget(pos))
            );
         } else if (!this.targets.isEmpty()) {
            this.setTarget(this.targets.removeFirst());
         } else {
            if (!this.searchDone) {
               this.findTargets(pos, (ItemStack)this.inventory.get(2));
            }

            if (!this.isSearching) {
               this.searchDone = false;
               if (!this.targets.isEmpty()) {
                  this.setTarget(this.targets.removeFirst());
               } else {
                  pos = pos.below();
                  BlockState state = this.getLevel().getBlockState(pos);
                  this.setTarget(
                     (RangedPumpTileEntity.PumpTarget)(this.hasFluids(state, pos)
                        ? new RangedPumpTileEntity.PumpTarget(pos, state, new FluidStack(state.getFluidState().getType(), 1000))
                        : new RangedPumpTileEntity.DownTarget(pos))
                  );
               }
            }
         }
      }
   }

   public boolean hasFluids(BlockState state, BlockPos pos) {
      Block block = state.getBlock();
      if (!(block instanceof LiquidBlock) && !(block instanceof BubbleColumnBlock)) {
         return false;
      } else {
         FluidState fluid = state.getFluidState();
         return fluid.getType() != Fluids.EMPTY && fluid.isSource();
      }
   }

   public void findTargets(BlockPos start, ItemStack stack) {
      IFluidScanner scanner = (IFluidScanner)stack.getItem();
      int radius = scanner.getScanRadius(stack, true);
      if (radius > 0) {
         this.isSearching = true;
         AABBUtil.createOffThreadTask(
            this.getLevel(),
            start,
            Box.fromPos(start, this.getBlockPos()).expand(DirectionList.HORIZONTAL, radius),
            new RangedPumpTileEntity.PumpSearch(scanner, stack),
            32,
            DirectionList.DOWN.invert(),
            2147483647,
            T -> {
               ObjectIterator var2x = Object2ObjectMaps.fastIterable(T.getMappedPositions()).iterator();

               while (var2x.hasNext()) {
                  Entry<Block, List<BlockPos>> entry = (Entry<Block, List<BlockPos>>)var2x.next();

                  for (BlockPos pos : entry.getValue()) {
                     BlockState state = this.getLevel().getBlockState(pos);
                     if (this.hasFluids(state, pos)) {
                        this.targets.add(new RangedPumpTileEntity.PumpTarget(pos, state, new FluidStack(state.getFluidState().getType(), 1000)));
                     } else {
                        this.targets.add(new RangedPumpTileEntity.DownTarget(pos));
                     }
                  }
               }

               this.isSearching = false;
               this.searchDone = true;
            }
         );
      }
   }

   public static class DownTarget extends RangedPumpTileEntity.PumpTarget {
      public DownTarget(CompoundTag nbt) {
         this.pos = BlockPos.of(nbt.getLong("pos"));
      }

      public DownTarget(BlockPos pos) {
         this.pos = pos;
      }

      @Override
      public boolean isValid() {
         return true;
      }

      @Override
      public boolean isDigTarget() {
         return true;
      }

      @Override
      public boolean isTargetValid(Level world) {
         if (world.isEmptyBlock(this.pos)) {
            return true;
         } else {
            FluidState state = world.getFluidState(this.pos);
            return state.getType() != Fluids.EMPTY && !state.isSource();
         }
      }

      @Override
      public FluidStack getFluids() {
         return FluidStack.EMPTY;
      }

      @Override
      public CompoundTag write() {
         CompoundTag data = new CompoundTag();
         data.putLong("pos", this.pos.asLong());
         data.putBoolean("dig", true);
         return data;
      }
   }

   public static class PumpSearch implements AABBUtil.IBlockMapper {
      IFluidScanner scanner;
      ItemStack stack;

      public PumpSearch(IFluidScanner scanner, ItemStack stack) {
         this.scanner = scanner;
         this.stack = stack;
      }

      @Override
      public boolean isValid(BlockState state) {
         return state.getBlock() == Blocks.AIR || state.getBlock() instanceof BucketPickup;
      }

      @Override
      public void mapBlocks(LevelReader reader, BlockPos pos, Map<Block, List<BlockPos>> mapper) {
         BlockState state = reader.getBlockState(pos);
         if (state.getBlock() != Blocks.AIR) {
            if (state.getBlock() instanceof BucketPickup) {
               FluidState fluid = state.getFluidState();
               if (this.scanner.isValuableFluid(this.stack, fluid, reader, pos)) {
                  AABBUtil.mapResult(state.getBlock(), pos, mapper);
               } else if (fluid.getType() != Fluids.EMPTY && !fluid.isSource()) {
                  AABBUtil.mapResult(state.getBlock(), pos, mapper);
               }
            }
         }
      }
   }

   public static class PumpTarget {
      BlockPos pos;
      BlockState state;
      FluidStack fluid;

      PumpTarget() {
      }

      public PumpTarget(CompoundTag compound) {
         this.pos = BlockPos.of(compound.getLong("pos"));
         this.fluid = FluidStack.loadFluidStackFromNBT(compound.getCompound("fluid_data"));
         this.state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), compound.getCompound("block_data"));
      }

      public PumpTarget(BlockPos pos, BlockState state, FluidStack fluid) {
         this.pos = pos;
         this.state = state;
         this.fluid = fluid;
      }

      public CompoundTag write() {
         CompoundTag data = new CompoundTag();
         data.putLong("pos", this.pos.asLong());
         data.put("block_data", NbtUtils.writeBlockState(this.state));
         data.put("fluid_data", this.fluid.writeToNBT(new CompoundTag()));
         data.putBoolean("dig", false);
         return data;
      }

      public boolean isValid() {
         return !this.fluid.isEmpty() && this.state.getBlock() != Blocks.AIR;
      }

      public boolean isTargetValid(Level world) {
         return world.getBlockState(this.pos) == this.state;
      }

      public boolean isDigTarget() {
         return false;
      }

      public FluidStack getFluids() {
         return this.fluid;
      }

      public BlockPos getPos() {
         return this.pos;
      }
   }
}
