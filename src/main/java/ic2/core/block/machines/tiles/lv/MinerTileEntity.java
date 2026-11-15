package ic2.core.block.machines.tiles.lv;

import ic2.api.core.IC2Classic;
import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricItem;
import ic2.api.items.electric.IMiningDrill;
import ic2.api.items.electric.IScanner;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.readers.IProgressMachine;
import ic2.api.tiles.tubes.ITube;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.audio.IAudioSource;
import ic2.core.block.base.cache.CapabilityCache;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.cache.TileCache;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.ProgressComparator;
import ic2.core.block.base.tiles.BaseElectricTileEntity;
import ic2.core.block.machines.containers.lv.MinerContainer;
import ic2.core.block.machines.logic.miner.BlockMiningTarget;
import ic2.core.block.machines.logic.miner.FluidMiningTarget;
import ic2.core.block.machines.logic.miner.IMiningTarget;
import ic2.core.block.machines.logic.miner.MiningQueue;
import ic2.core.block.machines.tiles.hv.RocketMinerTileEntity;
import ic2.core.block.misc.MiningPipeBlock;
import ic2.core.block.rendering.world.impl.BlockHighlighter;
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
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.math.ColorUtils;
import ic2.core.utils.math.geometry.Box;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public class MinerTileEntity extends BaseElectricTileEntity implements ITickListener, ITileGui, IProgressMachine, ITileActivityProvider {
   TileCache<PumpTileEntity> pumps = new TileCache<>(this, DirectionList.HORIZONTAL, PumpTileEntity.class);
   ICache<IItemHandler> inventoryCache = new CapabilityCache<>(this, DirectionList.HORIZONTAL, ForgeCapabilities.ITEM_HANDLER);
   ICache<ITube> tubeCache = new CapabilityCache<>(this, DirectionList.DOWN.invert(), IC2Classic.TUBE_CAPABILITY);
   protected LinkedList<IMiningTarget> queue = new LinkedList<>();
   protected MiningQueue masterWorkingQueue;
   @NetworkInfo
   protected BlockPos masterPos;
   protected boolean validatedMining = false;
   protected int progress = 0;
   @NetworkInfo
   protected BlockPos stuckOn = null;
   protected IMiningTarget current = null;
   protected IMiningTarget mining = null;
   IAudioSource source;

   public MinerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 4, 128, 10000);
      this.setFuelSlot(0);
      this.addGuiFields(new String[]{"stuckOn", "masterPos"});
      this.addCaches(new ICache[]{this.pumps, this.inventoryCache, this.tubeCache});
      this.addComparator(new ProgressComparator("progress", ComparatorNames.PROGRESS, this));
      this.addComparator(FlagComparator.createTile("active", ComparatorNames.ACTIVE, this));
   }

   protected MinerTileEntity(BlockPos pos, BlockState state, int size, int maxInput, int maxEnergy) {
      super(pos, state, size, maxInput, maxEnergy);
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockSides(DirectionList.DOWN.invert());
      handler.registerBlockAccess(DirectionList.DOWN.invert(), AccessRule.BOTH);
      handler.registerBlockAccess(DirectionList.DOWN, AccessRule.DISABLED);
      handler.registerSlotAccess(AccessRule.BOTH, 0, this.getScannerSlot(), this.getMiningPipeSlot(), this.getDrillSlot());
      handler.registerSlotsForSide(DirectionList.UP.invert(), this.getMiningPipeSlot());
      handler.registerSlotsForSide(DirectionList.HORIZONTAL, 0, this.getScannerSlot(), this.getDrillSlot());
      handler.registerInputFilter(SpecialFilters.createChargeFilter(), 0);
      handler.registerOutputFilter(ElectricItemFilter.NOT_DISCHARGE_FILTER, 0);
      handler.registerInputFilter(new ClassFilter(IScanner.class), this.getScannerSlot());
      handler.registerInputFilter(SpecialFilters.BLOCK_FILTER, this.getMiningPipeSlot());
      handler.registerInputFilter(new ClassFilter(IMiningDrill.class), this.getDrillSlot());
      handler.registerNamedSlot(SlotType.BATTERY, 0);
      handler.registerNamedSlot(SlotType.TOOLS, this.getScannerSlot(), this.getDrillSlot());
      handler.registerNamedSlot(SlotType.MINING_PIPES, this.getMiningPipeSlot());
   }

   @Override
   public boolean supportsNotify() {
      return false;
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.MINER;
   }

   public int getDrillSlot() {
      return 3;
   }

   protected int getMiningPipeSlot() {
      return 2;
   }

   protected int getScannerSlot() {
      return 1;
   }

   @Override
   public float getProgress() {
      return (float)this.progress;
   }

   @Override
   public float getMaxProgress() {
      return ((ItemStack)this.inventory.get(this.getDrillSlot())).isEmpty() ? 5.0F : 200.0F;
   }

   protected void drainMiningPipe(int amount) {
      ((ItemStack)this.inventory.get(this.getMiningPipeSlot())).shrink(1);
   }

   protected boolean hasMiningPipe() {
      ItemStack stack = (ItemStack)this.inventory.get(this.getMiningPipeSlot());
      return stack.getItem() == IC2Blocks.MINING_PIPE_SHAFT.asItem() && stack.getCount() > 0;
   }

   public boolean isSlave() {
      return this.masterPos != null;
   }

   public boolean setMaster(BlockPos masterPos, MiningQueue queue) {
      if (this.masterPos != null) {
         return false;
      } else {
         this.masterPos = masterPos;
         this.updateGuiField("masterPos");
         this.masterWorkingQueue = queue;
         return true;
      }
   }

   public void setQueue(MiningQueue queue) {
      if (queue.getMasterPos().equals(this.masterPos)) {
         this.masterWorkingQueue = queue;
      }
   }

   public void clear(BlockPos master) {
      if (master.equals(this.masterPos)) {
         this.masterWorkingQueue = null;
         this.masterPos = null;
         this.updateGuiField("masterPos");
      }
   }

   public float getPowerLeft() {
      int current = this.energy + ElectricItem.MANAGER.getCharge((ItemStack)this.inventory.get(0));
      int max = this.maxEnergy + ElectricItem.MANAGER.getCapacity((ItemStack)this.inventory.get(0));
      return (float)current / (float)max * 100.0F;
   }

   public MiningQueue getMasterQueue() {
      return this.masterWorkingQueue;
   }

   public void assistMiner(MinerTileEntity source, int progress) {
      this.progress += progress;
      if (this.progress >= 200) {
         if (this.mining.canMine(this, true)) {
            this.validatedMining = false;
            this.progress = 0;
            this.getLevel().destroyBlockProgress(this.getBlockPos().hashCode(), this.mining.getPos(), -1);
            this.mineBlock(source);
         } else {
            this.validatedMining = false;
            this.progress = -1;
            this.setStuck(this.mining.getPos());
         }
      } else if (this.mining != null) {
         this.getLevel().destroyBlockProgress(this.getBlockPos().hashCode(), this.mining.getPos(), Mth.clamp(progress / 20, 0, 10));
      }
   }

   public void assistMiner(MinerTileEntity source, ItemStack stack, IMiningDrill drill) {
      this.assistMiner(source, Math.max(1, 1 + drill.getMiningBoost(stack, this.mining.getState())));
   }

   public boolean canAssist(MinerTileEntity other) {
      return this.mining != null && this.mining.canMine(other, false) && this.mining.canContinue(other);
   }

   public int getEnergyUsage() {
      ItemStack stack = (ItemStack)this.inventory.get(this.getDrillSlot());
      return stack.getItem() instanceof IMiningDrill ? Math.max(1, 1 + ((IMiningDrill)stack.getItem()).getExtraEnergyCost(stack)) : 1;
   }

   @Override
   public void onGuiFieldChanged(Set<String> fields, Player player) {
      super.onGuiFieldChanged(fields, player);
      if (fields.contains("stuckOn") && this.stuckOn != null) {
         BlockHighlighter.INSTANCE.addHighlight(this.stuckOn, 600, ColorUtils.rgb(255, 0, 0, 128));
      }
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("isActive")) {
         if (this.source == null || !this.source.isValid()) {
            this.source = IC2.AUDIO.createSource(this, IC2Sounds.MINER_PROCESSING);
            if (this.source == null) {
               return;
            }
         }

         this.source.playStop(this.isActive() && this.isOperating());
      }
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 0 && this.masterPos == null) {
         if (this.masterWorkingQueue == null) {
            this.masterWorkingQueue = new MiningQueue(this.getBlockPos());
            this.findMinersInRange(this.masterWorkingQueue::addWorker);
            entity.sendSystemMessage(Component.literal("Setup Workgroup with [" + this.masterWorkingQueue.size() + "] helpers"));
         } else {
            this.masterWorkingQueue.remove(this.getLevel());
            this.masterWorkingQueue = null;
            entity.sendSystemMessage(Component.literal("Cleared Workgroup"));
         }
      }
   }

   private void findMinersInRange(Consumer<MinerTileEntity> tiles) {
      for (BlockPos pos : Box.withRange(this.getBlockPos(), 5)) {
         BlockEntity entity = this.getLevel().getBlockEntity(pos);
         if (entity instanceof MinerTileEntity) {
            MinerTileEntity miner = (MinerTileEntity)entity;
            if (!(entity instanceof RocketMinerTileEntity) && miner.getMasterQueue() == null) {
               tiles.accept(miner);
            }
         }
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      this.source = null;
      IC2.AUDIO.removeSource(this);
      if (!chunk) {
         if (this.masterPos == null && this.masterWorkingQueue != null) {
            this.masterWorkingQueue.remove(this.getLevel());
            this.masterWorkingQueue = null;
         } else if (this.masterPos != null && this.masterWorkingQueue != null) {
            this.masterWorkingQueue.removeWorker(this);
            this.masterWorkingQueue = null;
         }
      }

      super.onUnloaded(chunk);
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new MinerContainer(this, player, windowID);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      ListTag list = new ListTag();

      for (IMiningTarget target : this.queue) {
         CompoundTag nbt = target.save();
         nbt.putByte("type", target.getID());
         list.add(nbt);
      }

      if (!list.isEmpty()) {
         compound.put("queue", list);
      }

      compound.putInt("progress", this.progress);
      if (this.masterPos != null) {
         compound.putLong("master_pos", this.masterPos.asLong());
      } else if (this.masterWorkingQueue != null) {
         compound.put("master_queue", this.masterWorkingQueue.save());
      }

      if (this.current != null) {
         CompoundTag nbt = this.current.save();
         nbt.putByte("type", this.current.getID());
         compound.put("current", nbt);
         nbt = this.mining.save();
         nbt.putByte("type", this.mining.getID());
         compound.put("mining", nbt);
      }
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);

      for (CompoundTag nbt : NBTListWrapper.wrap(compound.getList("queue", 10), CompoundTag.class)) {
         this.queue.add((IMiningTarget)(nbt.getByte("type") == 1 ? new FluidMiningTarget(nbt) : new BlockMiningTarget(nbt)));
      }

      this.progress = compound.getInt("progress");
      this.masterPos = compound.contains("master_pos") ? BlockPos.of(compound.getLong("master_pos")) : null;
      this.masterWorkingQueue = compound.contains("master_queue") ? new MiningQueue(this.getBlockPos(), compound.getCompound("master_queue")) : null;
      if (compound.contains("current")) {
         CompoundTag nbt = compound.getCompound("current");
         this.current = (IMiningTarget)(nbt.getByte("type") == 1 ? new FluidMiningTarget(nbt) : new BlockMiningTarget(nbt));
         nbt = compound.getCompound("mining");
         this.mining = (IMiningTarget)(nbt.getByte("type") == 1 ? new FluidMiningTarget(nbt) : new BlockMiningTarget(nbt));
      }
   }

   @Override
   public void onLoaded() {
      super.onLoaded();
      if (this.masterPos != null && this.getLevel().isLoaded(this.masterPos)) {
         if (this.getLevel().getBlockEntity(this.masterPos) instanceof MinerTileEntity miner) {
            if (miner.getMasterQueue() == null) {
               this.masterPos = null;
               this.updateGuiField("masterPos");
               return;
            }

            this.masterWorkingQueue = miner.getMasterQueue();
         }
      } else if (this.masterPos == null && this.masterWorkingQueue != null) {
         this.masterWorkingQueue.loadIfPossible(this.getLevel());
      }
   }

   @Override
   public void onTick() {
      this.handleRedstone();
      this.handleChargeSlot((int)((float)this.maxEnergy * 0.9F));
      if (this.hasEnergy(100)) {
         if (((ItemStack)this.inventory.get(this.getScannerSlot())).getItem() instanceof IElectricItem) {
            this.energy = this.energy - ElectricItem.MANAGER.charge((ItemStack)this.inventory.get(this.getScannerSlot()), this.energy, 2147483647, true, false);
         }

         if (((ItemStack)this.inventory.get(this.getDrillSlot())).getItem() instanceof IElectricItem) {
            this.energy = this.energy - ElectricItem.MANAGER.charge((ItemStack)this.inventory.get(this.getDrillSlot()), this.energy, 2147483647, true, false);
         }
      }

      this.handleChargeSlot((int)((float)this.maxEnergy * 0.9F));
      boolean operating = this.isOperating();
      if (operating) {
         this.work();
      } else if (this.hasEnergy(1) && ((ItemStack)this.inventory.get(this.getDrillSlot())).isEmpty()) {
         if (MiningPipeBlock.isMiningPipe(this.getLevel().getBlockState(this.getBlockPos().below()))) {
            operating = true;
            this.setStuck(null);
            this.current = null;
            this.progress++;
            this.useEnergy(1);
            if (this.progress >= 5) {
               this.progress = 0;
               this.withdrawPipe();
            }

            this.queue.clear();
         } else if (this.isStuck()) {
            this.progress = 0;
         }
      } else if (this.isStuck() && this.clock(120)) {
         this.progress = 0;
      }

      this.setActive(operating);
      this.handleComparators();
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

      if (((ItemStack)this.inventory.get(this.getMiningPipeSlot())).getItem() != IC2Blocks.MINING_PIPE_SHAFT.asItem()
         && Block.byItem(((ItemStack)this.inventory.get(this.getMiningPipeSlot())).getItem()) != Blocks.AIR) {
         this.getLevel().setBlockAndUpdate(target, Block.byItem(((ItemStack)this.inventory.get(this.getMiningPipeSlot())).getItem()).defaultBlockState());
         ((ItemStack)this.inventory.get(this.getMiningPipeSlot())).shrink(1);
      }

      this.updateTip(target.getY() + 1);
   }

   public void setStuck(BlockPos pos) {
      this.stuckOn = pos;
      this.updateGuiField("stuckOn");
   }

   public boolean isStuck() {
      return this.progress < 0;
   }

   public boolean isOperating() {
      return this.hasEnergy(100) && this.canOperate() && !this.isRedstonePowered();
   }

   public boolean canOperate() {
      return !((ItemStack)this.inventory.get(this.getDrillSlot())).isEmpty()
         && (this.hasMiningPipe() || !this.queue.isEmpty() || this.masterWorkingQueue != null && !this.masterWorkingQueue.getTaskQueue().isEmpty())
         && this.canMiningDrillBeUsed()
         && !this.isStuck();
   }

   public boolean canMiningDrillBeUsed() {
      return !(((ItemStack)this.inventory.get(this.getDrillSlot())).getItem() instanceof IMiningDrill)
         ? false
         : ((IMiningDrill)((ItemStack)this.inventory.get(this.getDrillSlot())).getItem()).canDrillBeUsed((ItemStack)this.inventory.get(this.getDrillSlot()));
   }

   protected void work() {
      if (this.current == null) {
         this.findTask();
         if (this.current == null) {
            return;
         }
      }

      if (this.mining == null) {
         this.mining = this.current;
      }

      if (!this.validatedMining) {
         int checksLeft = 20;
         MutableBlockPos miningTarget = this.mining.getPos().mutable();
         BlockPos target = this.mining.getPos();

         while (!this.canReachTarget(miningTarget, true)) {
            int x_diff = target.getX() - this.getBlockPos().getX();
            int z_diff = target.getZ() - this.getBlockPos().getZ();
            if (Math.abs(x_diff) > Math.abs(z_diff)) {
               target = target.relative(x_diff > 0 ? Direction.WEST : Direction.EAST);
            } else {
               target = target.relative(z_diff > 0 ? Direction.NORTH : Direction.SOUTH);
            }

            miningTarget.set(target);
            if (checksLeft-- <= 0) {
               break;
            }
         }

         if (this.mining.getPos().equals(target)) {
            this.validatedMining = true;
            return;
         }

         if (this.masterWorkingQueue != null) {
            this.masterWorkingQueue.releaseTarget(this.mining.getPos(), this);
            this.masterWorkingQueue.claimTarget(target, this);
         }

         this.mining = this.createTarget(target);
         this.validatedMining = false;
      }

      if (this.masterWorkingQueue != null && this.masterWorkingQueue.isAssisting(this.mining.getPos(), this)) {
         if (this.mining.canMine(this, false) && this.mining.canContinue(this)) {
            IMiningDrill drill = (IMiningDrill)((ItemStack)this.inventory.get(this.getDrillSlot())).getItem();
            this.masterWorkingQueue
               .assist(
                  this, this.mining.getPos(), Math.max(1, 1 + drill.getMiningBoost((ItemStack)this.inventory.get(this.getDrillSlot()), this.mining.getState()))
               );
            this.useEnergy(Math.max(1, 1 + drill.getExtraEnergyCost((ItemStack)this.inventory.get(this.getDrillSlot()))));
         }

         if (this.getLevel().isEmptyBlock(this.mining.getPos())) {
            this.clearMiningPos(this.mining.getPos());
         }
      } else {
         if (!this.mining.canMine(this, false)) {
            this.validatedMining = false;
            this.progress = -1;
            this.setStuck(this.mining.getPos());
         } else if (this.mining.canContinue(this)) {
            this.setStuck(null);
            IMiningDrill drill = (IMiningDrill)((ItemStack)this.inventory.get(this.getDrillSlot())).getItem();
            this.progress = this.progress + Math.max(1, 1 + drill.getMiningBoost((ItemStack)this.inventory.get(this.getDrillSlot()), this.mining.getState()));
            this.useEnergy(Math.max(1, 1 + drill.getExtraEnergyCost((ItemStack)this.inventory.get(this.getDrillSlot()))));
            if (this.progress >= 200) {
               if (this.mining.canMine(this, true)) {
                  this.validatedMining = false;
                  this.progress = 0;
                  this.getLevel().destroyBlockProgress(this.getBlockPos().hashCode(), this.mining.getPos(), -1);
                  this.mineBlock(this);
               } else {
                  this.validatedMining = false;
                  this.progress = -1;
                  this.setStuck(this.mining.getPos());
               }
            }

            if (this.mining != null) {
               this.getLevel().destroyBlockProgress(this.getBlockPos().hashCode(), this.mining.getPos(), Mth.clamp(this.progress / 20, 0, 10));
            }
         }
      }
   }

   public void mineBlock(MinerTileEntity entity) {
      this.pumps.validateExisting();
      ((IMiningDrill)((ItemStack)entity.inventory.get(this.getDrillSlot())).getItem()).onDrillUsed((ItemStack)entity.inventory.get(this.getDrillSlot()));
      List<ItemStack> list = this.mining.createDrops(entity);
      BlockPos miningPos = this.mining.getPos();
      if (!this.sendItemsAway(list)) {
         for (int i = 0; i < list.size(); i++) {
            Block.popResource(this.getLevel(), entity.getBlockPos(), list.get(i));
         }
      }

      if (miningPos.getX() == this.getBlockPos().getX() && miningPos.getZ() == this.getBlockPos().getZ()) {
         this.getLevel().setBlockAndUpdate(miningPos, IC2Blocks.MINING_PIPE_BOTTOM.defaultBlockState());
         this.drainMiningPipe(1);
         this.useEnergy(10);
      }

      this.updateTip(miningPos.getY());
      if (miningPos.getY() != this.getPipeTip().getY()) {
         this.current = null;
         this.mining = null;
         this.queue.clear();
         if (this.masterWorkingQueue != null) {
            this.masterWorkingQueue.releaseTarget(miningPos, this);
         }
      } else {
         this.clearMiningPos(miningPos);
      }
   }

   protected void clearMiningPos(BlockPos miningPos) {
      BlockPos targetPos = this.current.getPos();
      if (miningPos.equals(targetPos) || !this.getLevel().isLoaded(targetPos) || this.getLevel().isEmptyBlock(targetPos) || !this.isOreValuable(targetPos)) {
         this.current = null;
         this.mining = null;
         if (this.masterWorkingQueue != null) {
            this.masterWorkingQueue.releaseTarget(miningPos, this);
         }
      } else if (this.masterWorkingQueue != null) {
         this.masterWorkingQueue.releaseTarget(miningPos, this);
         this.mining = this.current;
         this.masterWorkingQueue.claimTarget(targetPos, this);
      } else {
         this.mining = this.current;
      }
   }

   public boolean sendItemsAway(List<ItemStack> list) {
      if (list.isEmpty()) {
         return true;
      } else {
         InventoryHandler handler = this.getInventoryHandler();
         if (handler.areAllSidesBlocked()) {
            return false;
         } else {
            for (Direction dir : this.tubeCache) {
               if (handler.isBlockSideUnlocked(dir) && handler.getBlockAccess(dir).canExport()) {
                  IItemTransporter trans = TransporterManager.getTransporter(this.tubeCache.getHandler(dir));
                  if (trans != null) {
                     this.sendItems(trans, dir.getOpposite(), list);
                  }
               }
            }

            if (!list.isEmpty()) {
               for (Direction dirx : this.inventoryCache) {
                  if (handler.isBlockSideUnlocked(dirx) && handler.getBlockAccess(dirx).canExport()) {
                     IItemTransporter trans = TransporterManager.getTransporter(this.inventoryCache.getHandler(dirx));
                     if (trans != null) {
                        this.sendItems(trans, dirx.getOpposite(), list);
                     }
                  }
               }
            }

            return list.isEmpty();
         }
      }
   }

   private void sendItems(IItemTransporter transporter, Direction dir, List<ItemStack> list) {
      for (int i = 0; i < list.size(); i++) {
         ItemStack stack = list.get(i);
         if (stack.isEmpty()) {
            list.remove(i--);
         } else {
            stack.shrink(transporter.addItem(stack, dir, false));
            if (stack.getCount() <= 0) {
               list.remove(i--);
            }
         }
      }
   }

   public boolean isOreValuable(BlockPos pos) {
      ItemStack stack = (ItemStack)this.inventory.get(this.getScannerSlot());
      return !(stack.getItem() instanceof IScanner)
         ? false
         : ((IScanner)stack.getItem()).isOreValuable(stack, this.getLevel().getBlockState(pos), this.getLevel(), pos);
   }

   public void updateTip(int low) {
      if (low != this.getBlockPos().getY()) {
         MutableBlockPos target;
         for (target = this.getBlockPos().mutable().move(Direction.DOWN); target.getY() > low; target.setY(target.getY() - 1)) {
            BlockState state = this.getLevel().getBlockState(target);
            boolean miningPipe = MiningPipeBlock.isMiningPipe(state);
            boolean isTip = state.getBlock() == IC2Blocks.MINING_PIPE_BOTTOM;
            if (!miningPipe && this.hasMiningPipe() || isTip) {
               this.getLevel().setBlockAndUpdate(target, IC2Blocks.MINING_PIPE_SHAFT.defaultBlockState());
               if (!miningPipe) {
                  this.drainMiningPipe(1);
               }
            }
         }

         this.getLevel().setBlockAndUpdate(target, IC2Blocks.MINING_PIPE_BOTTOM.defaultBlockState());
      }
   }

   public boolean canReachTarget(MutableBlockPos targetPos, boolean ignore) {
      int x_diff = targetPos.getX() - this.getBlockPos().getX();
      int z_diff = targetPos.getZ() - this.getBlockPos().getZ();
      if (x_diff == 0 && z_diff == 0) {
         return true;
      } else {
         if (!ignore) {
            BlockState state = this.getLevel().getBlockState(targetPos);
            if (!state.isAir() && !this.canPass(state, this.getLevel(), targetPos)) {
               return false;
            }
         }

         if (Math.abs(x_diff) > Math.abs(z_diff)) {
            targetPos.move(x_diff > 0 ? Direction.WEST : Direction.EAST);
         } else {
            targetPos.move(z_diff > 0 ? Direction.NORTH : Direction.SOUTH);
         }

         return this.canReachTarget(targetPos, false);
      }
   }

   public boolean canPass(BlockState state, LevelReader world, BlockPos pos) {
      FluidState fluid = state.getFluidState();
      return !fluid.isEmpty() ? !fluid.isSource() : MiningPipeBlock.isMiningPipe(state) || state.getBlock() == IC2Blocks.MINER;
   }

   protected void findTask() {
      if (this.masterPos == null || this.masterWorkingQueue != null) {
         if (this.masterWorkingQueue != null) {
            BlockPos pos = this.getPipeTip();
            if (this.masterWorkingQueue.getYLevel() < pos.getY()) {
               this.setTarget(this.createTarget(pos.below()));
            } else {
               LinkedList<IMiningTarget> queue = this.masterWorkingQueue.getTaskQueue();
               if (!queue.isEmpty() && queue.peekFirst().getPos().getY() == pos.getY()) {
                  this.setTarget(queue.removeFirst());
               } else if (this.masterPos == null) {
                  if (((ItemStack)this.inventory.get(this.getScannerSlot())).getItem() instanceof IScanner) {
                     this.findTargets(pos, (ItemStack)this.inventory.get(this.getScannerSlot()), queue, this.masterWorkingQueue.hasPumps());
                     if (!queue.isEmpty()) {
                        this.masterWorkingQueue.setYLevel(pos.getY());
                        this.setTarget(queue.removeFirst());
                        return;
                     }

                     this.setTarget(this.createTarget(pos.below()));
                     this.masterWorkingQueue.setYLevel(pos.getY() - 1);
                  }
               } else {
                  if (((ItemStack)this.inventory.get(this.getDrillSlot())).getItem() instanceof IMiningDrill drill
                     && this.masterWorkingQueue.assistRandomly(this, (ItemStack)this.inventory.get(this.getDrillSlot()), drill)) {
                     this.useEnergy(Math.max(1, 1 + drill.getExtraEnergyCost((ItemStack)this.inventory.get(this.getDrillSlot()))));
                  }
               }
            }
         } else {
            BlockPos pos = this.getPipeTip();
            if (pos.getY() >= this.getBlockPos().getY()) {
               this.setTarget(this.createTarget(this.getBlockPos().below()));
            } else if (!(((ItemStack)this.inventory.get(this.getScannerSlot())).getItem() instanceof IScanner)) {
               this.setTarget(this.createTarget(pos.below()));
            } else if (!this.queue.isEmpty()) {
               this.setTarget(this.queue.removeFirst());
            } else {
               this.findTargets(pos, (ItemStack)this.inventory.get(this.getScannerSlot()), this.queue, this.hasPumps());
               if (!this.queue.isEmpty()) {
                  this.setTarget(this.queue.removeFirst());
               } else {
                  this.setTarget(this.createTarget(pos.below()));
               }
            }
         }
      }
   }

   public void setTarget(IMiningTarget target) {
      if (target.isValid(this)) {
         this.current = target;
         this.mining = target;
         if (this.masterWorkingQueue != null) {
            this.masterWorkingQueue.claimTarget(target.getPos(), this);
         }
      } else {
         this.current = null;
         this.mining = null;
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

   protected void findTargets(BlockPos pos, ItemStack stack, LinkedList<IMiningTarget> queue, boolean canPump) {
      IScanner item = (IScanner)stack.getItem();
      int radius = item.getScanRadius(stack, true);
      if (radius >= 0) {
         this.pumps.validateExisting();
         Box box = Box.withRange(pos, radius);
         PriorityQueue<BlockPos> toCheck = new ObjectArrayFIFOQueue();
         Set<BlockPos> alreadyDone = CollectionUtils.<BlockPos>createSet();
         this.addToQueue(pos, toCheck, alreadyDone);

         while (!toCheck.isEmpty()) {
            BlockPos task = (BlockPos)toCheck.dequeue();
            if (box.intersectsWith(task) && alreadyDone.add(task) && this.getLevel().isLoaded(task)) {
               BlockState state = this.getLevel().getBlockState(task);
               boolean isMiningPipe = MiningPipeBlock.isMiningPipe(state);
               if (this.canMine(task, state, canPump) || isMiningPipe) {
                  if (!isMiningPipe) {
                     FluidState fluid = state.getFluidState();
                     if (!fluid.isEmpty() && !fluid.isSource()) {
                        queue.add(this.createTarget(task));
                     }

                     if (item.isOreValuable(stack, state, this.getLevel(), task)) {
                        queue.add(this.createTarget(task));
                     }
                  }

                  this.addToQueue(task, toCheck, alreadyDone);
               }
            }
         }
      }
   }

   public boolean canMine(BlockPos pos, BlockState state, boolean canPump) {
      if (state.isAir()) {
         return true;
      } else if (MiningPipeBlock.isMiningPipe(state)) {
         return false;
      } else {
         FluidState fluid = state.getFluidState();
         if (!fluid.isEmpty()) {
            if (fluid.isSource()) {
               return canPump ? this.canMineEvent(pos, state) : false;
            } else {
               return true;
            }
         } else if (state.getDestroySpeed(this.getLevel(), pos) < 0.0F) {
            return false;
         } else if (!state.getFluidState().isEmpty() && state.requiresCorrectToolForDrops()) {
            if (((ItemStack)this.inventory.get(this.getDrillSlot())).getItem() instanceof IMiningDrill drill) {
               return !drill.canMineBlock((ItemStack)this.inventory.get(this.getDrillSlot()), state, this.getLevel(), pos)
                     && !((ItemStack)this.inventory.get(this.getDrillSlot())).isCorrectToolForDrops(state)
                  ? false
                  : this.canMineEvent(pos, state);
            } else {
               return false;
            }
         } else {
            return this.canMineEvent(pos, state);
         }
      }
   }

   public boolean canMineEvent(BlockPos target, BlockState state) {
      BreakEvent evt = new BreakEvent(this.getLevel(), target, state, FakePlayerFactory.getMinecraft((ServerLevel)this.getLevel()));
      evt.setExpToDrop(0);
      MinecraftForge.EVENT_BUS.post(evt);
      return !evt.isCanceled();
   }

   protected void addToQueue(BlockPos pos, PriorityQueue<BlockPos> queue, Set<BlockPos> alreadyDone) {
      for (Direction dir : DirectionList.HORIZONTAL) {
         BlockPos thePos = pos.relative(dir);
         if (!alreadyDone.contains(thePos)) {
            queue.enqueue(thePos);
         }
      }
   }

   public IMiningTarget createTarget(BlockPos pos) {
      return this.createTarget(pos, false);
   }

   public IMiningTarget createTarget(BlockPos pos, boolean force) {
      BlockState state = this.getLevel().getBlockState(pos);
      FluidState fluid = state.getFluidState();
      return (IMiningTarget)(!fluid.isEmpty() && fluid.isSource() && !force ? new FluidMiningTarget(pos, state) : new BlockMiningTarget(pos, state));
   }

   public boolean hasPumps() {
      return !this.pumps.isEmpty();
   }

   public InteractionResult pumpFluid(FluidStack fluidStack, boolean doPump) {
      if (fluidStack.isEmpty()) {
         return InteractionResult.FAIL;
      } else {
         InteractionResult result = InteractionResult.FAIL;

         for (Direction dir : this.pumps) {
            PumpTileEntity pump = this.pumps.getHandler(dir);
            InteractionResult type = pump.pumpExternal(fluidStack, doPump);
            if (type == InteractionResult.SUCCESS) {
               return InteractionResult.SUCCESS;
            }

            if (type == InteractionResult.PASS) {
               result = InteractionResult.PASS;
            }
         }

         return result;
      }
   }
}
