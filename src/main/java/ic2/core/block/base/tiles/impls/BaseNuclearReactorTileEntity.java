package ic2.core.block.base.tiles.impls;

import ic2.api.network.buffer.NetworkInfo;
import ic2.api.reactor.IChamberReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.api.reactor.IReactorComponent;
import ic2.api.reactor.IReactorProduct;
import ic2.api.util.DirectionList;
import ic2.api.util.IC2DamageSource;
import ic2.core.IC2;
import ic2.core.audio.IAudioSource;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.ITileActivityProvider;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.misc.comparator.ComparatorNames;
import ic2.core.block.base.misc.comparator.types.base.FlagComparator;
import ic2.core.block.base.misc.comparator.types.special.HeatComparator;
import ic2.core.block.base.misc.readers.IHeatProvider;
import ic2.core.block.base.tiles.BaseInventoryTileEntity;
import ic2.core.block.generators.containers.ReactorContainer;
import ic2.core.block.machines.logic.planner.encoder.EncoderRegistry;
import ic2.core.block.machines.logic.planner.encoder.ReactorSetup;
import ic2.core.entity.explosion.IC2Explosion;
import ic2.core.inventory.base.ITileGui;
import ic2.core.inventory.container.IC2Container;
import ic2.core.inventory.handler.AccessRule;
import ic2.core.inventory.handler.InventoryHandler;
import ic2.core.inventory.handler.SlotType;
import ic2.core.inventory.handler.slots.ReactorSlots;
import ic2.core.inventory.inv.SyncedInventory;
import ic2.core.platform.registries.IC2Sounds;
import ic2.core.utils.helpers.NBTUtils;
import ic2.core.utils.math.MathUtils;
import java.util.Arrays;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class BaseNuclearReactorTileEntity
   extends BaseInventoryTileEntity
   implements ITickListener,
   IChamberReactor,
   ITileGui,
   IHeatProvider,
   IWrenchableTile,
   ITileActivityProvider {
   public static final int[][] SLOT_ARRAY = createArray();
   @NetworkInfo
   public float output;
   @NetworkInfo
   public int heat;
   @NetworkInfo
   public int maxHeat = 10000;
   public float hem = 1.0F;
   public int size = 3;
   @NetworkInfo
   public SyncedInventory filter = new SyncedInventory(54);
   protected boolean refreshChambers = true;
   protected boolean inventoryChanged = false;
   IAudioSource mainSource;
   IAudioSource[] subSources = new IAudioSource[3];

   public BaseNuclearReactorTileEntity(BlockPos pos, BlockState state) {
      super(pos, state, 54);
      this.addNetworkFields(new String[]{"heat", "maxHeat", "output"});
      this.addGuiFields(new String[]{"filter"});
      this.addComparator(new HeatComparator("heat", ComparatorNames.HEAT, this));
      this.addComparator(FlagComparator.createWorker("producing", ComparatorNames.PRODUCING, () -> this.output > 0.0F));
   }

   @Override
   protected void addSlotInfo(InventoryHandler handler) {
      int[] slots = MathUtils.fromTo(0, 54);
      handler.registerBlockSides(DirectionList.ALL);
      handler.registerBlockAccess(DirectionList.ALL, AccessRule.BOTH);
      handler.registerCustomSlot(new ReactorSlots(this));
      handler.registerInputFilter(this::isUsefulItem, slots);
      handler.registerSlotAccess(AccessRule.BOTH, slots);
      handler.registerNamedSlot(SlotType.REACTOR, slots);
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      NBTUtils.putFloat(compound, "output", this.output, 0.0F);
      NBTUtils.putInt(compound, "heat", this.heat, 0);
      NBTUtils.put(compound, "filter", this.filter.save(new CompoundTag()));
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.output = compound.getFloat("output");
      this.heat = compound.getInt("heat");
      this.filter.load(compound.getCompound("filter"));
   }

   @Override
   public void setStackInSlot(int slot, ItemStack stack) {
      super.setStackInSlot(slot, stack);
      if (!stack.isEmpty()) {
         this.filter.setStackInSlot(slot, ItemStack.EMPTY);
      }
   }

   @Override
   public void onUnloaded(boolean chunk) {
      this.mainSource = null;
      Arrays.fill(this.subSources, null);
      IC2.AUDIO.removeSource(this);
      super.onUnloaded(chunk);
   }

   @Override
   public void onNetworkFieldChanged(Set<String> fields, Player player) {
      super.onNetworkFieldChanged(fields, player);
      if (fields.contains("output")) {
         if (this.mainSource == null || !this.mainSource.isValid()) {
            this.mainSource = this.getSounds().createSource(this, IC2Sounds.REACTOR_PROCESSING);
         }

         for (int i = 0; i < 3; i++) {
            if (this.subSources[i] == null || !this.subSources[i].isValid()) {
               this.subSources[i] = this.getSounds().createSource(this, this.getGeigerSound(i));
            }
         }

         this.playStop(this.mainSource, this.output > 0.0F);
         this.playStop(this.subSources[0], this.output > 0.0F && this.output < 40.0F);
         this.playStop(this.subSources[1], this.output >= 40.0F && this.output < 80.0F);
         this.playStop(this.subSources[2], this.output >= 80.0F);
      }
   }

   protected ResourceLocation getGeigerSound(int level) {
      switch (level) {
         case 1:
            return IC2Sounds.REACTOR_MEDIUM;
         case 2:
            return IC2Sounds.REACTOR_HIGH;
         default:
            return IC2Sounds.REACTOR_LOW;
      }
   }

   public boolean applyFilter(String id) {
      ReactorSetup setup = EncoderRegistry.INSTANCE.createDecodedSetup(id, false);
      if (setup != null) {
         this.filter.copyFrom(setup.items);
         this.updateGuiField("filter");
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isActivated() {
      return this.isProducingEnergy();
   }

   @Override
   public IC2Container createContainer(Player player, InteractionHand hand, Direction side, int windowID) {
      return new ReactorContainer(this, player, windowID);
   }

   static int[][] createArray() {
      int[][] array = new int[10][];

      for (int size = 1; size < 10; size++) {
         array[size] = new int[6 * size];
         int slot = 0;

         for (int y = 0; y < 6; y++) {
            for (int x = 0; x < size; x++) {
               array[size][slot] = x + y * 9;
               slot++;
            }
         }
      }

      return array;
   }

   @Override
   public int getTickRate() {
      return 20;
   }

   public boolean isDamageTick() {
      return true;
   }

   @Override
   protected boolean needsInitialRedstoneCheck() {
      return true;
   }

   @Override
   public void onTick() {
      this.handleComparators();
      if (this.getLevel().getGameTime() % (long)this.getTickRate() == 0L) {
         if (!this.isAreaLoaded(1)) {
            this.output = 0.0F;
            this.updateTileField("output");
         } else {
            this.handleRedstone();
            this.dropUselessItems();
            this.output = 0.0F;
            this.maxHeat = 10000;
            this.hem = 1.0F;
            this.processChambers();
            if (!this.calculateHeatEffects()) {
               this.setActive(this.heat >= 1000 || this.output > 0.0F);
               this.updateTileFields(new String[]{"heat", "output", "maxHeat"});
            }
         }
      }
   }

   @Override
   protected boolean handleComparators(boolean ignoreTick) {
      boolean result = super.handleComparators(ignoreTick);
      if (result) {
         for (Direction dir : DirectionList.ALL) {
            this.getLevel().updateNeighbourForOutputSignal(this.getBlockPos().relative(dir), this.getBlockState().getBlock());
         }
      }

      return result;
   }

   @Override
   public void handleRedstone() {
      if (this.needsRedstoneUpdate && this.sensitive) {
         this.needsRedstoneUpdate = false;
         if (this.directionalSignal) {
            byte max = 0;

            for (Direction dir : DirectionList.ALL) {
               byte strength = (byte)Mth.clamp(this.getLevel().getSignal(this.getBlockPos(), dir), 0, 15);
               max = (byte)Math.max(max, strength);
               this.sidedSignals[dir.get3DDataValue()] = strength;
            }

            this.signal = max;
            return;
         }

         this.signal = (byte)Mth.clamp(this.getLevel().getBestNeighborSignal(this.worldPosition), 0, 15);

         for (Direction dir : DirectionList.ALL) {
            BlockEntity tile = DirectionList.getNeighborTile(this, dir);
            if (this.isValidChamber(tile)) {
               this.signal = (byte)Math.max(this.signal, Mth.clamp(this.getLevel().getBestNeighborSignal(tile.getBlockPos()), 0, 15));
            }
         }
      }
   }

   public void processChambers() {
      int size = this.getWidth();
      boolean isDamageTick = this.isDamageTick();

      for (int pass = 0; pass < 2; pass++) {
         for (int y = 0; y < 6; y++) {
            for (int x = 0; x < size; x++) {
               ItemStack thing = this.getStackInReactor(x, y);
               if (thing.getItem() instanceof IReactorComponent comp) {
                  comp.processChamber(thing, this, x, y, pass == 0, pass == 1 && isDamageTick);
               }
            }
         }
      }
   }

   public boolean calculateHeatEffects() {
      if (this.heat < 4000) {
         return false;
      } else if (IC2.CONFIG.reactorDamage.get() <= 0.0) {
         return false;
      } else {
         float power = (float)this.heat / (float)this.maxHeat;
         if (power >= 1.0F) {
            this.explode();
            return true;
         } else {
            RandomSource rand = this.getLevel().random;
            if (power >= 0.85F && rand.nextFloat() <= 0.2F * this.hem) {
               BlockPos nextPos = this.getRandomPos(rand, 2);
               if (nextPos != null) {
                  BlockState state = this.getLevel().getBlockState(nextPos);
                  if (state.getBlock() == Blocks.AIR) {
                     this.getLevel().setBlockAndUpdate(nextPos, Blocks.FIRE.defaultBlockState());
                  } else {
                     float hardness = state.getDestroySpeed(this.getLevel(), nextPos);
                     if (hardness >= 0.0F && hardness <= 10.0F && this.getLevel().getBlockEntity(this.worldPosition) == null) {
                        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                           this.getLevel()
                              .setBlockAndUpdate(
                                 nextPos,
                                 ((FluidState)Fluids.FLOWING_LAVA.defaultFluidState().setValue(BlockStateProperties.LEVEL_FLOWING, 8)).createLegacyBlock()
                              );
                        } else {
                           this.getLevel().setBlockAndUpdate(nextPos, Blocks.FIRE.defaultBlockState());
                        }
                     }
                  }
               }
            }

            if (power >= 0.7F) {
               for (LivingEntity entity : this.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(this.worldPosition).inflate(3.0))) {
                  entity.hurt(IC2DamageSource.RADIATION, (float)rand.nextInt(4) * this.hem);
               }
            }

            if (power >= 0.5F && rand.nextFloat() <= this.hem) {
               BlockPos nextPos = this.getRandomPos(rand, 2);
               if (nextPos != null) {
                  BlockState state = this.getLevel().getBlockState(nextPos);
                  Fluid fluid = state.getFluidState().getType();
                  if (state.getBlock() instanceof BucketPickup && fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
                     ((BucketPickup)state.getBlock()).pickupBlock(this.getLevel(), nextPos, state);
                  }
               }
            }

            if (power >= 0.4F && rand.nextFloat() <= this.hem) {
               BlockPos nextPos = this.getRandomPos(rand, 2);
               if (nextPos != null) {
                  BlockState state = this.getLevel().getBlockState(nextPos);
                  if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES) || state.ignitedByLava()) {
                     this.getLevel().setBlockAndUpdate(nextPos, Blocks.FIRE.defaultBlockState());
                  }
               }
            }

            return false;
         }
      }
   }

   public BlockPos getRandomPos(RandomSource rand, int radius) {
      if (radius <= 0) {
         return null;
      } else {
         int x = rand.nextInt(2 * radius + 1) - radius;
         int y = rand.nextInt(2 * radius + 1) - radius;
         int z = rand.nextInt(2 * radius + 1) - radius;
         BlockPos nextPos = this.worldPosition.offset(x, y, z);
         return this.worldPosition.equals(nextPos) ? null : nextPos;
      }
   }

   public void dropUselessItems() {
      int size = this.getWidth();
      if (this.inventoryChanged) {
         this.inventoryChanged = false;

         for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 6; y++) {
               ItemStack stack = this.getStackInReactor(x, y);
               if (!stack.isEmpty() && (x >= size || !this.isUsefulItem(stack))) {
                  this.eject(stack);
                  this.setStackInReactor(x, y, ItemStack.EMPTY);
               }
            }
         }
      }
   }

   public boolean isUsefulItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         Item item = stack.getItem();
         if (item instanceof IReactorComponent) {
            return true;
         } else {
            return item instanceof IReactorProduct ? ((IReactorProduct)item).isValidForReactor(stack, this) : false;
         }
      }
   }

   @Override
   public int getHeat() {
      return this.heat;
   }

   @Override
   public void setHeat(int newHeat) {
      this.heat = newHeat;
   }

   @Override
   public void addHeat(int heat) {
      this.heat += heat;
   }

   @Override
   public int getMaxHeat() {
      return this.maxHeat;
   }

   @Override
   public void setMaxHeat(int heat) {
      this.maxHeat = heat;
   }

   @Override
   public float getHeatEffectModifier() {
      return this.hem;
   }

   @Override
   public void setHeatEffectModifier(float hem) {
      this.hem = hem;
   }

   @Override
   public double getEnergyOutput() {
      return (double)this.output;
   }

   @Override
   public void addOutput(float output) {
      this.output += output;
   }

   @Override
   public ItemStack getStackInReactor(int x, int y) {
      return x >= 0 && x < this.getWidth() && y >= 0 && y < this.getHeight() ? this.getStackInSlot(y * 9 + x) : ItemStack.EMPTY;
   }

   @Override
   public void setStackInReactor(int x, int y, ItemStack stack) {
      if (x >= 0 && x < this.getWidth() && y >= 0 && y < this.getHeight()) {
         this.setStackInSlot(y * 9 + x, stack);
      }
   }

   @Override
   public void explode() {
      float boomPower = 10.0F;
      float boomMod = 1.0F;
      int y = 0;

      for (int m = this.getHeight(); y < m; y++) {
         int x = 0;

         for (int n = this.getWidth(); x < n; x++) {
            ItemStack stack = this.getStackInReactor(x, y);
            if (stack.getItem() instanceof IReactorComponent) {
               float f = ((IReactorComponent)stack.getItem()).getExplosionInfluence(stack, this);
               if (f > 0.0F && f < 1.0F) {
                  boomMod *= f;
               } else {
                  boomPower += f;
               }
            }

            this.setStackInReactor(x, y, ItemStack.EMPTY);
         }
      }

      IC2.LOGGER.info("Exploded: {}, {}, {}", boomPower, this.hem, boomMod);
      boomPower *= this.hem * boomMod;
      IC2.LOGGER.info("Total Power: {}", boomPower);
      boomPower = (float)Math.min((double)boomPower, IC2.CONFIG.reactorDamage.get());
      IC2.LOGGER
         .info(
            "Nuclear Reactor at {}:({},{},{}) melted (explosion power {})",
            this.getLevel().dimension().location(),
            this.worldPosition.getX(),
            this.worldPosition.getY(),
            this.worldPosition.getZ(),
            boomPower
         );

      for (Direction direction : DirectionList.ALL) {
         BlockEntity target = DirectionList.getNeighborTile(this, direction);
         if (target instanceof IReactorChamber) {
            this.getLevel().removeBlock(this.worldPosition, false);
         }
      }

      this.getLevel().removeBlock(this.worldPosition, false);
      IC2Explosion explosion = new IC2Explosion(this.getLevel(), null, Vec3.atLowerCornerOf(this.worldPosition), boomPower, 0.01F, IC2DamageSource.NUKE);
      explosion.doExplosion();
   }

   @Override
   public boolean isProducingEnergy() {
      return this.isRedstonePowered();
   }

   @Override
   public int getHeight() {
      return 6;
   }

   @Override
   public int getWidth() {
      this.onChamberRefresh();
      return this.size;
   }

   public int[] getSlotArray() {
      return SLOT_ARRAY[this.getWidth()];
   }

   @Override
   public boolean canSetFacing(Direction dir) {
      return dir != this.getFacing() && dir.getAxis().isHorizontal();
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.75;
   }

   @Override
   public void refreshChambers() {
      this.refreshChambers = true;
   }

   public void onChamberRefresh() {
      if (this.refreshChambers) {
         this.refreshChambers = false;
         int lastSize = this.size;
         this.size = 3;

         for (Direction dir : DirectionList.ALL) {
            BlockEntity tile = DirectionList.getNeighborTile(this, dir);
            if (this.isValidChamber(tile)) {
               this.size++;
            }
         }

         if (lastSize != this.size) {
            this.inventoryChanged = true;
            if (this.size < lastSize) {
               for (int x = this.size; x < 9; x++) {
                  for (int y = 0; y < 6; y++) {
                     ItemStack stack = this.getStackInSlot(y * 9 + x);
                     if (!stack.isEmpty()) {
                        this.eject(stack);
                        this.setStackInSlot(y * 9 + x, ItemStack.EMPTY);
                     }
                  }
               }
            }
         }
      }
   }

   public void eject(ItemStack stack) {
      if (this.isSimulating()) {
         Block.popResource(this.getLevel(), this.getBlockPos(), stack);
      }
   }

   public boolean isValidChamber(BlockEntity tile) {
      return tile instanceof IReactorChamber;
   }
}
