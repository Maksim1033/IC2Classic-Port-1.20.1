package ic2.core.block.machines.tiles.hv;

import ic2.api.items.electric.ElectricItem;
import ic2.api.items.electric.IElectricItem;
import ic2.api.items.electric.IFluidScanner;
import ic2.api.items.electric.IMiningDrill;
import ic2.api.items.electric.IScanner;
import ic2.api.network.buffer.NetworkInfo;
import ic2.api.tiles.readers.IWorkProvider;
import ic2.api.util.DirectionList;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.base.InventoryComparator;
import ic2.core.block.base.misc.comparator.types.base.TankComparator;
import ic2.core.block.machines.containers.hv.RocketMinerContainer;
import ic2.core.block.machines.tiles.lv.MinerTileEntity;
import ic2.core.block.misc.MiningPipeBlock;
import ic2.core.entity.rockets.MiningRocketEntity;
import ic2.core.fluid.LayeredFluidTank;
import ic2.core.fluid.WrapperFluidHandler;
import ic2.core.inventory.base.IHasInventory;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.filter.ClassFilter;
import ic2.core.inventory.filter.FluidFilter;
import ic2.core.inventory.filter.SimpleFilter;
import ic2.core.inventory.filter.special.ElectricItemFilter;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.inv.RangedInventory;
import ic2.core.inventory.inv.SimpleInventory;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.item.tool.MinerRemoteItem;
import ic2.core.platform.registries.IC2Blocks;
import ic2.core.platform.registries.IC2Entities;
import ic2.core.platform.registries.IC2Items;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.NBTListWrapper;
import ic2.core.utils.helpers.FluidHelper;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.helpers.StackUtil;
import ic2.core.utils.math.MathUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class RocketMinerTileEntity extends MinerTileEntity implements IWorkProvider {
   public OptionalLong afterRecharge = OptionalLong.empty();
   public RocketMinerTileEntity.MinerState state = RocketMinerTileEntity.MinerState.IDLE;
   public Direction nextDirection = Direction.SOUTH;
   public int maxJumps = 1;
   public int jumpProgress = 0;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_16)
   public int storedMiningPipes = 0;
   @NetworkInfo(NetworkInfo.BitLevel.BIT_16)
   public int storedCells = 0;
   public LayeredFluidTank tank = new LayeredFluidTank(128000);
   public UUID owner;
   IHasInventory pipeInventory = new RangedInventory(this, 3);
   IHasInventory cellInventory = new RangedInventory(this, 8);
   IHasInventory dropsInventory = new RangedInventory(this, MathUtils.fromTo(11, 47));
   UUID currentChunkLoaderID = UUID.randomUUID();
   UUID nextChunkLoaderID = UUID.randomUUID();
   Object2LongMap<UUID> loadedChunks = new Object2LongLinkedOpenHashMap();
   boolean ignoreChunkRemoval = false;

   public RocketMinerTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 47, 512, 10000);
      this.setFuelSlot(0);
      this.addGuiFields(new String[]{"storedMiningPipes", "storedCells"});
      this.addCapability(ForgeCapabilities.FLUID_HANDLER, new WrapperFluidHandler.DrainHandlerWrapper(this.tank));
      this.addComparator(FlagComparator.createWorker("refueled", ComparatorNames.REFUELED, this));
      this.addComparator(FlagComparator.createWorker("cargo_full", ComparatorNames.HAS_CARGO, this::hasCargo));
      this.addComparator(FlagComparator.createWorker("fuel_full", ComparatorNames.HAS_CHARGE, this::hasEnoughCharge));
      this.addComparator(new InventoryComparator("cell_slots", ComparatorNames.CELL_FILLED_SLOTS, this.cellInventory, true));
      this.addComparator(new InventoryComparator("cell_items", ComparatorNames.CELL_FILLED_INVENTORY, this.cellInventory, false));
      this.addComparator(new TankComparator("tank", ComparatorNames.TANK, this.tank));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerBlockSides(DirectionList.ALL);
      IntList list = new IntArrayList(MathUtils.fromTo(0, 11));
      list.rem(4);
      list.rem(7);
      int[] logic = list.toIntArray();
      int[] slots = MathUtils.fromTo(11, 47);
      handler.registerSlotAccess(AccessRule.EXPORT, slots);
      handler.registerSlotAccess(AccessRule.BOTH, logic);
      handler.registerSlotsForSide(DirectionList.ALL, slots);
      handler.registerSlotsForSide(DirectionList.ALL, logic);
      handler.registerInputFilter(ElectricItemFilter.DISCHARGE_EXTERNAL_FILTER, 0);
      handler.registerInputFilter(new ClassFilter(IScanner.class), 1);
      handler.registerInputFilter(new ClassFilter(IMiningDrill.class), 2);
      handler.registerInputFilter(new SimpleFilter(IC2Blocks.MINING_PIPE_SHAFT), 3, 4);
      handler.registerInputFilter(new SimpleFilter(IC2Blocks.RECYCLER), 5);
      handler.registerInputFilter(new SimpleFilter(IC2Items.COBBLEGEN_UPGRADE), 6);
      handler.registerInputFilter(FluidFilter.EMPTY_CONTAINER, 7, 8);
      handler.registerInputFilter(new SimpleFilter(IC2Items.MINER_REMOTE), 9);
      handler.registerInputFilter(new SimpleFilter(IC2Blocks.CHUNKLOADER), 10);
      handler.registerNamedSlot(SlotType.BATTERY, 0);
      handler.registerNamedSlot(SlotType.SCANNER, 1);
      handler.registerNamedSlot(SlotType.TOOLS, 2);
      handler.registerNamedSlot(SlotType.MINING_PIPES, 3, 4);
      handler.registerNamedSlot(SlotType.RECYCLER, 5);
      handler.registerNamedSlot(SlotType.FILLER, 6);
      handler.registerNamedSlot(SlotType.CONTAINER, 7, 8);
      handler.registerNamedSlot(SlotType.REFUEL_REMOTE, 9);
      handler.registerNamedSlot(SlotType.CHUNKLOADER, 10);
      handler.registerNamedSlot(SlotType.STORAGE, slots);

      for (int slot : logic) {
         handler.setSlotSide(slot, DirectionList.EMPTY);
      }
   }

   protected boolean hasCargo() {
      int i = 0;

      for (int m = this.dropsInventory.getSlotCount(); i < m; i++) {
         if (!this.dropsInventory.getStackInSlot(i).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   protected boolean hasEnoughCharge() {
      ItemStack stack = this.getStackInSlot(0);
      return stack.isEmpty() ? false : (float)ElectricItem.MANAGER.getCharge(stack) / (float)ElectricItem.MANAGER.getCapacity(stack) >= 0.9F;
   }

   @Override
   public int getMaxStackSize(int slot) {
      return slot == 0 ? 1 : super.getMaxStackSize(slot);
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.ROCKET_MINER;
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putByte("state", (byte)this.state.ordinal());
      compound.putByte("next_dir", (byte)this.nextDirection.get2DDataValue());
      compound.putInt("jump", this.jumpProgress);
      compound.putInt("max_jumps", this.maxJumps);
      if (this.afterRecharge.isPresent()) {
         compound.putLong("after_recharge", this.afterRecharge.getAsLong());
      }

      compound.putUUID("current_id", this.currentChunkLoaderID);
      compound.putUUID("next_id", this.nextChunkLoaderID);
      compound.putInt("pipes", this.storedMiningPipes);
      compound.putInt("cells", this.storedCells);
      if (this.owner != null) {
         compound.putUUID("owner", this.owner);
      }

      NBTUtils.put(compound, "tank", this.tank.write(new CompoundTag()));
      ListTag list = new ListTag();
      ObjectIterator var3 = this.loadedChunks.object2LongEntrySet().iterator();

      while (var3.hasNext()) {
         Entry<UUID> entry = (Entry<UUID>)var3.next();
         CompoundTag data = new CompoundTag();
         data.putLong("pos", entry.getLongValue());
         data.putUUID("id", (UUID)entry.getKey());
         list.add(data);
      }

      compound.put("loaded_chunks", list);
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.state = RocketMinerTileEntity.MinerState.byId(compound.getInt("state"));
      this.nextDirection = Direction.from2DDataValue(compound.getInt("next_dir"));
      this.jumpProgress = compound.getInt("jump");
      this.maxJumps = compound.getInt("max_jumps");
      this.storedCells = compound.getInt("cells");
      this.storedMiningPipes = compound.getInt("pipes");
      this.afterRecharge = compound.contains("after_recharge") ? OptionalLong.of(compound.getLong("after_recharge")) : OptionalLong.empty();
      this.tank.read(compound.getCompound("tank"));
      this.owner = compound.hasUUID("owner") ? compound.getUUID("owner") : null;
      if (compound.hasUUID("current_id")) {
         this.currentChunkLoaderID = compound.getUUID("current_id");
         this.nextChunkLoaderID = compound.getUUID("next_id");

         for (CompoundTag data : NBTListWrapper.wrap(compound.getList("loaded_chunks", 10), CompoundTag.class)) {
            this.loadedChunks.put(data.getUUID("id"), data.getLong("pos"));
         }
      }
   }

   @Override
   public void addDrops(List<ItemStack> drops) {
      super.addDrops(drops);
      StackUtil.expand(new ItemStack(IC2Blocks.MINING_PIPE_SHAFT), this.storedMiningPipes, drops);
      StackUtil.expand(new ItemStack(IC2Items.CELL_EMPTY), this.storedCells, drops);
   }

   @Override
   public int getDrillSlot() {
      return 2;
   }

   @Override
   protected int getMiningPipeSlot() {
      return 3;
   }

   @Override
   public boolean isWorking() {
      if (!this.isRefueling()) {
         return false;
      } else {
         ItemStack stack = this.getStackInSlot(0);
         if (stack.isEmpty()) {
            return false;
         } else {
            float charge = (float)ElectricItem.MANAGER.getCharge(stack) / (float)ElectricItem.MANAGER.getCapacity(stack);
            if (charge <= 0.9F) {
               return false;
            } else {
               int i = 0;

               for (int m = this.dropsInventory.getSlotCount(); i < m; i++) {
                  if (!this.dropsInventory.getStackInSlot(i).isEmpty()) {
                     return false;
                  }
               }

               return this.storedCells == 0 || this.storedCells >= 1024;
            }
         }
      }
   }

   @Override
   protected void drainMiningPipe(int amount) {
      int last = this.storedMiningPipes;
      this.storedMiningPipes = Mth.clamp(this.storedMiningPipes - amount, 0, 512);
      if (last == this.storedMiningPipes && amount < 0) {
         Block.popResource(this.getLevel(), this.getBlockPos(), new ItemStack(IC2Blocks.MINING_PIPE_SHAFT));
      }

      this.updateGuiField("storedMiningPipes");
   }

   @Override
   protected boolean hasMiningPipe() {
      return this.storedMiningPipes > 0;
   }

   public boolean hasCells() {
      return this.storedCells < 1024;
   }

   @Override
   public int getEnergyUsage() {
      return super.getEnergyUsage() + (this.isChunkLoading() ? 15 : 0);
   }

   @Override
   public void onClientDataReceived(Player entity, int key, int value) {
      super.onClientDataReceived(entity, key, value);
      if (key == 1) {
         if (this.storedMiningPipes > 0) {
            while (this.storedMiningPipes > 0) {
               int left = Math.min(this.storedMiningPipes, 64);
               this.storedMiningPipes -= left;
               StackUtil.addOrDrop(entity, new ItemStack(IC2Blocks.MINING_PIPE_SHAFT, left));
            }

            this.storedMiningPipes = 0;
            this.updateGuiField("storedMiningPipes");
         }
      } else if (key == 2 && this.storedCells > 0) {
         while (this.storedCells > 0) {
            int left = Math.min(this.storedCells, 64);
            this.storedCells -= left;
            StackUtil.addOrDrop(entity, new ItemStack(IC2Items.CELL_EMPTY, left));
         }

         this.storedCells = 0;
         this.updateGuiField("storedCells");
      }
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new RocketMinerContainer(this, player, windowID);
   }

   public boolean isRefueling() {
      return this.state == RocketMinerTileEntity.MinerState.REFUELING && this.afterRecharge.isPresent();
   }

   public boolean isChunkLoading() {
      return ((ItemStack)this.inventory.get(10)).getItem() == IC2Blocks.CHUNKLOADER.asItem();
   }

   public void onLanded(boolean triggerChunkload) {
      this.charge_slot = true;
      this.comparators.disableIgnore();
      this.comparators.onLoaded();
      if (triggerChunkload && this.isChunkLoading()) {
         this.unloadChunks(this.loadedChunks.removeLong(this.currentChunkLoaderID), this.currentChunkLoaderID);
         this.loadChunks(this.loadedChunks.getLong(this.nextChunkLoaderID), this.currentChunkLoaderID);
         this.unloadChunks(this.loadedChunks.removeLong(this.nextChunkLoaderID), this.nextChunkLoaderID);
      }
   }

   public void sendHome() {
      this.state = RocketMinerTileEntity.MinerState.REFUELING;
   }

   @Override
   public boolean isOperating() {
      return this.hasEnergy(100) && this.canOperate();
   }

   @Override
   public void onTick() {
      this.handleSlots();
      this.handleItemRecharging();
      this.handleComparators();
      if ((!this.handleChargeSlot((int)((float)this.maxEnergy * 0.9F)) && this.energy <= 4000 || this.isNearEmpty((ItemStack)this.inventory.get(0)))
         && this.state.isWorkingState()) {
         this.state = RocketMinerTileEntity.MinerState.OUT_OF_POWER;
      }

      this.handleChunkloading();
      if (this.state != RocketMinerTileEntity.MinerState.NO_REFUEL_STATION && this.state != RocketMinerTileEntity.MinerState.RESET) {
         if (this.isRefueling()) {
            this.handleRedstone();
            if (this.getRedstoneStrength() == 15) {
               if (this.isChunkLoading() && this.loadedChunks.isEmpty()) {
                  this.loadChunks(new ChunkPos(this.getPreviousTarget()).toLong(), this.currentChunkLoaderID);
               }

               this.state = RocketMinerTileEntity.MinerState.IDLE;
               MiningRocketEntity rocket = new MiningRocketEntity(IC2Entities.ROCKET_MINER, this.getLevel(), this.getBlockPos(), false);
               rocket.setTarget(this.getPreviousTarget());
               this.afterRecharge = OptionalLong.empty();
               rocket.nbt = this.saveWithoutMetadata();
               this.ignoreChunkRemoval = true;
               this.getLevel().setBlockAndUpdate(this.getBlockPos(), Blocks.AIR.defaultBlockState());
               this.getLevel().addFreshEntity(rocket);
            }
         } else {
            if (this.isOperating()) {
               if (this.state == RocketMinerTileEntity.MinerState.IDLE) {
                  this.state = RocketMinerTileEntity.MinerState.WORKING;
               } else if (this.state == RocketMinerTileEntity.MinerState.WORKING) {
                  boolean wasNull = this.current == null;
                  this.work();
                  if (this.current == null && wasNull && !this.hasMiningPipe()) {
                     this.state = RocketMinerTileEntity.MinerState.MOVE_TO_NEXT_TARGET;
                  } else if (((ItemStack)this.inventory.get(this.getScannerSlot())).getItem() instanceof IFluidScanner && this.storedCells <= 0) {
                     this.state = RocketMinerTileEntity.MinerState.FULL_INVENTORY;
                  }
               }
            } else if (this.state == RocketMinerTileEntity.MinerState.WORKING && this.current == null && !this.hasMiningPipe()) {
               this.state = RocketMinerTileEntity.MinerState.MOVE_TO_NEXT_TARGET;
            } else if (((ItemStack)this.inventory.get(this.getScannerSlot())).getItem() instanceof IFluidScanner && this.storedCells <= 0) {
               this.state = RocketMinerTileEntity.MinerState.FULL_INVENTORY;
            } else if (this.state == RocketMinerTileEntity.MinerState.IDLE && this.isStuck()) {
               this.state = RocketMinerTileEntity.MinerState.STUCK_ON_TARGET;
            }

            boolean retracting = false;
            if (this.state.isFailureState() && this.hasEnergy(1)) {
               if (!MiningPipeBlock.isMiningPipe(this.getLevel().getBlockState(this.getBlockPos().below()))) {
                  this.progress = 0;
                  this.launchRocket(this.getRefuelPosition());
                  return;
               }

               retracting = true;
               this.stuckOn = null;
               this.current = null;
               this.progress++;
               this.useEnergy(1);
               if (this.progress >= 5) {
                  this.progress = 0;
                  this.withdrawPipe();
               }

               this.queue.clear();
            }

            this.setActive(this.state == RocketMinerTileEntity.MinerState.WORKING || retracting);
            this.handleComparators();
         }
      }
   }

   protected void handleSlots() {
      if (this.clock(10)) {
         int drained = this.drainCount(this.cellInventory, 1024 - this.storedCells);
         if (drained > 0) {
            this.storedCells += drained;
            this.updateGuiField("storedCells");
         }

         drained = this.drainCount(this.pipeInventory, 512 - this.storedMiningPipes);
         if (drained > 0) {
            this.storedMiningPipes += drained;
            this.updateGuiField("storedMiningPipes");
         }
      }
   }

   protected void handleItemRecharging() {
      if (this.hasEnergy(100)) {
         if (this.isRefueling()) {
            this.energy = this.energy - ElectricItem.MANAGER.charge((ItemStack)this.inventory.get(0), this.energy, 2147483647, true, false);
         }

         if (((ItemStack)this.inventory.get(this.getScannerSlot())).getItem() instanceof IElectricItem) {
            this.energy = this.energy - ElectricItem.MANAGER.charge((ItemStack)this.inventory.get(this.getScannerSlot()), this.energy, 2147483647, true, false);
         }

         if (((ItemStack)this.inventory.get(this.getDrillSlot())).getItem() instanceof IElectricItem) {
            this.energy = this.energy - ElectricItem.MANAGER.charge((ItemStack)this.inventory.get(this.getDrillSlot()), this.energy, 2147483647, true, false);
         }
      }
   }

   protected void handleChunkloading() {
      if (this.isChunkLoading()) {
         if (this.useEnergy(15) == 0) {
            this.clearLoadedChunks();
         } else if (this.loadedChunks.isEmpty() && !this.isRefueling()) {
            this.loadChunks(new ChunkPos(this.getBlockPos()).toLong(), this.currentChunkLoaderID);
         }
      } else {
         this.clearLoadedChunks();
      }
   }

   public boolean isNearEmpty(ItemStack stack) {
      return ElectricItem.MANAGER.getCharge(stack) < (int)Math.min(10000.0, (double)ElectricItem.MANAGER.getCapacity(stack) * 0.02);
   }

   @Override
   public void setStuck(BlockPos pos) {
      super.setStuck(pos);
      if (pos != null) {
         this.state = RocketMinerTileEntity.MinerState.STUCK_ON_TARGET;
      }
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (this.isSimulating() && this.state != RocketMinerTileEntity.MinerState.REFUELING && (slot >= 0 && slot <= 2 || slot == 9)) {
         this.state = RocketMinerTileEntity.MinerState.IDLE;
      }
   }

   private int drainCount(IHasInventory inventory, int limit) {
      if (limit <= 0) {
         return 0;
      } else {
         int result = 0;
         int i = 0;

         for (int m = inventory.getSlotCount(); i < m; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
               int left = Math.min(limit - result, stack.getCount());
               if (left > 0) {
                  result += left;
                  stack.shrink(left);
               }
            }
         }

         return result;
      }
   }

   protected void launchRocket(BlockPos refuel) {
      if (!this.state.isFailureState() || this.state.isWorkingState()) {
         ItemStack stack = (ItemStack)this.inventory.get(this.getScannerSlot());
         if (stack.getItem() instanceof IScanner scanner) {
            int var9 = scanner.getScanRadius(stack, false) * 2 + 1;
            Vec3i offset = this.nextDirection.getNormal();
            BlockPos launchPos = this.getBlockPos().offset(offset.getX() * var9, offset.getY() * var9, offset.getZ() * var9);
            this.jumpProgress++;
            if (this.jumpProgress == this.maxJumps) {
               this.jumpProgress = 0;
               this.nextDirection = this.nextDirection.getClockWise();
               this.maxJumps = this.maxJumps + this.nextDirection.get2DDataValue() % 2;
            }

            if (this.isChunkLoading()) {
               this.loadChunks(new ChunkPos(launchPos).toLong(), this.nextChunkLoaderID);
            }

            this.state = RocketMinerTileEntity.MinerState.IDLE;
            MiningRocketEntity rocket = new MiningRocketEntity(IC2Entities.ROCKET_MINER, this.getLevel(), this.getBlockPos(), true);
            rocket.setTarget(launchPos);
            rocket.nbt = this.saveWithoutMetadata();
            this.ignoreChunkRemoval = true;
            this.getLevel().setBlockAndUpdate(this.getBlockPos(), Blocks.AIR.defaultBlockState());
            this.getLevel().addFreshEntity(rocket);
         } else {
            this.state = RocketMinerTileEntity.MinerState.NO_SCANNER;
         }
      } else if (refuel == null) {
         this.state = RocketMinerTileEntity.MinerState.NO_REFUEL_STATION;
      } else {
         this.afterRecharge = OptionalLong.of(this.getBlockPos().asLong());
         this.state = RocketMinerTileEntity.MinerState.REFUELING;
         MiningRocketEntity rocket = new MiningRocketEntity(IC2Entities.ROCKET_MINER, this.getLevel(), this.getBlockPos(), false);
         rocket.setTarget(refuel);
         rocket.nbt = this.saveWithoutMetadata();
         this.ignoreChunkRemoval = true;
         this.getLevel().setBlockAndUpdate(this.getBlockPos(), Blocks.AIR.defaultBlockState());
         this.getLevel().addFreshEntity(rocket);
      }
   }

   @Override
   public void withdrawPipe() {
      BlockPos target = this.getPipeTip();
      if (!this.getLevel().isEmptyBlock(target)) {
         if (this.getBlockPos().getY() - target.getY() > 0) {
            this.drainMiningPipe(-1);
         }

         this.getLevel().removeBlock(target, false);
      }

      if (!((ItemStack)this.inventory.get(6)).isEmpty()) {
         this.getLevel().setBlockAndUpdate(target, Blocks.COBBLESTONE.defaultBlockState());
      }

      this.updateTip(target.getY() + 1);
   }

   @Override
   public void mineBlock(MinerTileEntity tile) {
      ((IMiningDrill)((ItemStack)this.inventory.get(this.getDrillSlot())).getItem()).onDrillUsed((ItemStack)this.inventory.get(this.getDrillSlot()));
      BlockPos miningPos = this.mining.getPos();
      BlockState state = this.getLevel().getBlockState(miningPos);
      boolean valuable = ((IScanner)((ItemStack)this.inventory.get(this.getScannerSlot())).getItem())
         .isOreValuable((ItemStack)this.inventory.get(this.getScannerSlot()), state, this.getLevel(), miningPos);
      List<ItemStack> list = this.mining.createDrops(this);
      boolean shouldGetStuck = false;
      if (!valuable && !((ItemStack)this.inventory.get(5)).isEmpty()) {
         this.recycle(StackUtil.countItems(list));
         list.clear();
      } else {
         shouldGetStuck = this.sendItemsAway(TransporterManager.getTransporter(this.dropsInventory), list, true);
      }

      if (shouldGetStuck || !this.hasInventoryAnEmptySlot(this.dropsInventory)) {
         this.state = RocketMinerTileEntity.MinerState.FULL_INVENTORY;
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
      } else {
         BlockPos targetPos = this.current.getPos();
         if (!miningPos.equals(targetPos) && this.getLevel().isLoaded(targetPos) && !this.getLevel().isEmptyBlock(targetPos) && this.isOreValuable(targetPos)) {
            this.mining = this.current;
         } else {
            this.current = null;
            this.mining = null;
         }
      }
   }

   protected void recycle(int times) {
      this.useEnergy(times * 100);
      int count = 0;

      for (int i = 0; i < times; i++) {
         if (this.getLevel().random.nextInt(4) == 0) {
            count++;
         }
      }

      IItemTransporter transporter = TransporterManager.getTransporter(this.dropsInventory);

      while (count > 0) {
         int removed = Math.min(64, count);
         count -= removed;
         transporter.addItem(new ItemStack(IC2Items.SCRAP, removed), null, false);
      }

      if (this.hasEnergy(100)) {
         while (true) {
            ItemStack scrap = transporter.removeItem(new SimpleFilter(IC2Items.SCRAP), null, 63, true);
            int scrapCount = scrap.getCount() / 9;
            if (scrapCount <= 0) {
               break;
            }

            transporter.removeItem(new SimpleFilter(IC2Items.SCRAP), null, scrapCount * 9, false);
            transporter.addItem(new ItemStack(IC2Items.SCRAPBOX, scrapCount), null, false);
            this.useEnergy(100 * scrapCount);
         }
      }
   }

   protected boolean sendItemsAway(IItemTransporter transporter, List<ItemStack> items, boolean setStuck) {
      boolean shouldBeStuck = false;
      int i = 0;

      for (int m = items.size(); i < m; i++) {
         ItemStack stack = items.get(i);
         if (!stack.isEmpty()) {
            stack.shrink(transporter.addItem(stack, null, false));
            if (stack.getCount() > 0) {
               Block.popResource(this.getLevel(), this.getBlockPos(), stack);
               if (setStuck) {
                  shouldBeStuck = true;
               }
            }
         }
      }

      items.clear();
      return shouldBeStuck;
   }

   protected boolean hasInventoryAnEmptySlot(IHasInventory inventory) {
      int i = 0;

      for (int m = inventory.getSlotCount(); i < m; i++) {
         if (inventory.getStackInSlot(i).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   public BlockPos getRefuelPosition() {
      ItemStack stack = (ItemStack)this.inventory.get(9);
      if (stack.getItem() instanceof MinerRemoteItem) {
         BlockPos pos = MinerRemoteItem.getPos(stack);
         return pos.asLong() == BlockPos.ZERO.asLong() ? null : pos;
      } else {
         return null;
      }
   }

   public BlockPos getPreviousTarget() {
      return BlockPos.of(this.afterRecharge.orElse(0L));
   }

   @Override
   public float getMaxProgress() {
      return this.state.isFailureState() ? 5.0F : 200.0F;
   }

   @Override
   public boolean canOperate() {
      return super.canOperate() && this.getStackInSlot(this.getScannerSlot()).getItem() instanceof IScanner;
   }

   @Override
   public boolean hasPumps() {
      return true;
   }

   @Override
   public InteractionResult pumpFluid(FluidStack fluidStack, boolean doPump) {
      if (fluidStack.isEmpty()) {
         return InteractionResult.FAIL;
      } else {
         Fluid fluid = fluidStack.getFluid();
         FluidTank tank = new FluidTank(1000);
         tank.setFluid(fluidStack);
         IHasInventory inv = new SimpleInventory(1);
         if (this.storedCells > 0) {
            inv.setStackInSlot(0, new ItemStack(IC2Items.CELL_EMPTY));
         }

         if (FluidHelper.fillContainer(inv, 0, this.dropsInventory, tank, !doPump)) {
            if (doPump) {
               this.storedCells--;
               this.updateGuiField("storedCells");
            }

            return InteractionResult.SUCCESS;
         } else if (fluid != Fluids.WATER && fluid != Fluids.LAVA) {
            int amount = this.tank.fill(fluidStack, doPump ? FluidAction.EXECUTE : FluidAction.SIMULATE);
            return amount > 0 ? InteractionResult.SUCCESS : InteractionResult.FAIL;
         } else {
            return InteractionResult.FAIL;
         }
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      if (this.isSimulating() && this.isChunkLoading() && !chunk && !this.ignoreChunkRemoval) {
         this.clearLoadedChunks();
      }

      super.onUnloaded(chunk);
   }

   public void clearLoadedChunks() {
      if (!this.loadedChunks.isEmpty()) {
         ObjectIterator var1 = this.loadedChunks.object2LongEntrySet().iterator();

         while (var1.hasNext()) {
            Entry<UUID> entry = (Entry<UUID>)var1.next();
            this.unloadChunks(entry.getLongValue(), (UUID)entry.getKey());
         }

         this.loadedChunks.clear();
      }
   }

   public void loadChunks(long chunkPos, UUID id) {
      this.loadedChunks.put(id, chunkPos);

      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            ForgeChunkManager.forceChunk((ServerLevel)this.getLevel(), "ic2", id, x + ChunkPos.getX(chunkPos), z + ChunkPos.getZ(chunkPos), true, false);
         }
      }
   }

   public void unloadChunks(long chunkPos, UUID id) {
      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            ForgeChunkManager.forceChunk((ServerLevel)this.getLevel(), "ic2", id, x + ChunkPos.getX(chunkPos), z + ChunkPos.getZ(chunkPos), false, false);
         }
      }
   }

   public static enum MinerState {
      IDLE(1, "idle"),
      WORKING(1, "working"),
      MOVE_TO_NEXT_TARGET(3, "moving"),
      STUCK_ON_TARGET(3, "stuck"),
      OUT_OF_POWER(2, "power"),
      FULL_INVENTORY(2, "full"),
      CANT_WORK(2, "no_work"),
      REFUELING(2, "refuel"),
      NO_REFUEL_STATION(2, "no_refuel"),
      NO_SCANNER(2, "no_scanner"),
      RESET(2, "reset");

      private static final RocketMinerTileEntity.MinerState[] VALUES = values();
      final int state;
      final String translation;

      private MinerState(int state, String suffix) {
         this.state = state;
         this.translation = "ic2.probe.rocket_miner.state." + suffix;
      }

      public MutableComponent getState() {
         return Component.translatable(this.translation);
      }

      public boolean isWorkingState() {
         return (this.state & 1) != 0;
      }

      public boolean isFailureState() {
         return (this.state & 2) != 0;
      }

      public static RocketMinerTileEntity.MinerState byId(int value) {
         return VALUES[Mth.clamp(value, 0, VALUES.length - 1)];
      }
   }
}
