package ic2.core.block.machines.tiles.hv;

import org.joml.Vector3f;
import ic2.api.tiles.IEnergyStorage;
import ic2.api.tiles.teleporter.ITeleporterTarget;
import ic2.api.tiles.teleporter.TeleporterTarget;
import ic2.api.util.DirectionList;
import ic2.core.IC2;
import ic2.core.block.base.cache.ICache;
import ic2.core.block.base.cache.InterfaceCache;
import ic2.core.block.base.features.ITickListener;
import ic2.core.block.base.features.IWrenchableTile;
import ic2.core.block.base.tiles.BaseTileEntity;
import ic2.core.inventory.filter.IFilter;
import ic2.core.inventory.filter.SetItemFilter;
import ic2.core.inventory.filter.StackFilter;
import ic2.core.inventory.transporter.IItemTransporter;
import ic2.core.inventory.transporter.TransporterManager;
import ic2.core.platform.registries.IC2Advancements;
import ic2.core.platform.registries.IC2Stats;
import ic2.core.platform.registries.IC2Tiles;
import ic2.core.utils.collection.CollectionUtils;
import ic2.core.utils.helpers.AABBUtil;
import ic2.core.utils.helpers.TeleportUtil;
import ic2.core.utils.math.ColorUtils;
import it.unimi.dsi.fastutil.PriorityQueue;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

public class TeleporterTileEntity extends BaseTileEntity implements ITickListener, ITeleporterTarget, IWrenchableTile {
   public TeleporterTarget target = null;
   private int delay = 0;
   private int activeDelay = 0;
   boolean blockChange = false;
   EnumSet<ITeleporterTarget.TeleportType> types = EnumSet.noneOf(ITeleporterTarget.TeleportType.class);
   InterfaceCache<IEnergyStorage> powerSources = new InterfaceCache<>(this, DirectionList.ALL, IEnergyStorage.class);

   public TeleporterTileEntity(BlockPos pos, BlockState state) {
      super(pos, state);
      this.addCaches(new ICache[]{this.powerSources});
   }

   @Override
   public BlockEntityType<?> createType() {
      return IC2Tiles.TELEPORTER;
   }

   @Override
   public boolean canSetFacing(Direction dir) {
      return this.getFacing() != dir;
   }

   @Override
   public boolean canRemoveBlock(Player player) {
      return true;
   }

   @Override
   public double getDropRate(Player player) {
      return 0.7;
   }

   @Override
   public void onTick() {
      if (this.blockChange) {
         this.blockChange = false;
         this.updateTargetTypes();
      }

      if (this.target == null) {
         this.setActive(false);
      } else {
         if (this.activeDelay > 0) {
            this.activeDelay--;
            if (this.activeDelay == 0) {
               this.setActive(false);
            }
         }

         this.handleRedstone();
         if (!this.isRedstonePowered()) {
            if (this.delay > 0) {
               this.delay--;
            }
         } else if (this.delay <= 0) {
            ServerLevel world = this.target.getWorld();
            if (world == null) {
               this.target = null;
               this.setActive(false);
            } else {
               this.delay = 20;
               this.activeDelay = 10;
               boolean result = this.teleport(world);
               if (result) {
                  IC2.AUDIO.playSound(this, new ResourceLocation("ic2", "sounds/machines/teleport.ogg"));
               }

               this.setActive(result);
            }
         }
      }
   }

   protected boolean isSamePos(BlockEntity target, Direction targetDirection) {
      return this.getBlockPos().relative(this.getFacing()).equals(target.getBlockPos().relative(targetDirection));
   }

   protected boolean teleport(ServerLevel world) {
      switch (this.getSendType()) {
         case ENERGY:
            return this.handleEnergy(world);
         case ENTITY:
            return this.handleEntity(world);
         case FLUID:
            return this.handleFluids(world);
         case ITEM:
            return this.handleItems(world);
         case SPAWNER:
            return this.handleSpawner(world);
         default:
            return false;
      }
   }

   protected boolean handleSpawner(ServerLevel targetWorld) {
      SpawnerBlockEntity source = DirectionList.getNeighborInterface(this, this.getFacing(), SpawnerBlockEntity.class);
      if (source == null) {
         return false;
      } else {
         BlockEntity targetTile = targetWorld.getBlockEntity(this.target.getTargetPosition());
         if (targetTile instanceof ITeleporterTarget iTeleporterTarget) {
            if (!iTeleporterTarget.canReceive(ITeleporterTarget.TeleportType.SPAWNER)) {
               return false;
            } else {
               BlockPos targetPos = iTeleporterTarget.getPosition().relative(iTeleporterTarget.getFacing());
               if (targetWorld.isEmptyBlock(targetPos) && !source.getBlockPos().equals(targetPos)) {
                  double distanceCost = this.getBaseCost(targetWorld) * 25000.0;
                  if ((double)this.getAvailableEnergy() < distanceCost) {
                     return false;
                  } else {
                     this.consumeEnergy((int)distanceCost);
                     targetWorld.setBlockAndUpdate(targetPos, Blocks.SPAWNER.defaultBlockState());
                     SpawnerBlockEntity spawner = DirectionList.getNeighborInterface(targetTile, iTeleporterTarget.getFacing(), SpawnerBlockEntity.class);
                     if (spawner == null) {
                        return false;
                     } else {
                        spawner.getSpawner().load(targetWorld, targetPos, source.getSpawner().save(new CompoundTag()));
                        this.getLevel().setBlockAndUpdate(this.worldPosition.relative(this.getFacing()), Blocks.AIR.defaultBlockState());
                        return true;
                     }
                  }
               } else {
                  return false;
               }
            }
         } else {
            this.target = null;
            return false;
         }
      }
   }

   protected boolean handleEnergy(ServerLevel targetWorld) {
      IEnergyStorage source = DirectionList.getNeighborInterface(this, this.getFacing(), IEnergyStorage.class);
      if (source == null) {
         return false;
      } else {
         BlockEntity targetTile = targetWorld.getBlockEntity(this.target.getTargetPosition());
         if (targetTile instanceof ITeleporterTarget iTeleporterTarget) {
            if (!iTeleporterTarget.canReceive(ITeleporterTarget.TeleportType.ENERGY)) {
               return false;
            } else if (this.isSamePos(targetTile, iTeleporterTarget.getFacing())) {
               return false;
            } else {
               IEnergyStorage receiver = DirectionList.getNeighborInterface(targetTile, iTeleporterTarget.getFacing(), IEnergyStorage.class);
               if (receiver == null) {
                  return false;
               } else {
                  double distanceCost = this.getBaseCost(targetWorld);
                  if ((double)this.getAvailableEnergy() < distanceCost) {
                     return false;
                  } else {
                     this.consumeEnergy((int)distanceCost);
                     int provided = source.getStoredEU();
                     if (provided <= 0) {
                        return false;
                     } else {
                        source.drawEnergy(receiver.addEnergy(provided));
                        return true;
                     }
                  }
               }
            }
         } else {
            this.target = null;
            return false;
         }
      }
   }

   protected boolean handleFluids(ServerLevel targetWorld) {
      LazyOptional<IFluidHandler> lazySource = DirectionList.getNeighborCapability(this, this.getFacing(), ForgeCapabilities.FLUID_HANDLER);
      if (!lazySource.isPresent()) {
         return false;
      } else {
         BlockEntity targetTile = targetWorld.getBlockEntity(this.target.getTargetPosition());
         if (!(targetTile instanceof ITeleporterTarget iTeleporterTarget)) {
            this.target = null;
            return false;
         } else if (!iTeleporterTarget.canReceive(ITeleporterTarget.TeleportType.FLUID)) {
            return false;
         } else if (this.isSamePos(targetTile, iTeleporterTarget.getFacing())) {
            return false;
         } else {
            LazyOptional<IFluidHandler> lazyReceiver = DirectionList.getNeighborCapability(
               targetTile, iTeleporterTarget.getFacing(), ForgeCapabilities.FLUID_HANDLER
            );
            if (!lazyReceiver.isPresent()) {
               return false;
            } else {
               double distanceCost = this.getBaseCost(targetWorld);
               int availableWeight = (int)((double)this.getAvailableEnergy() / distanceCost);
               if (availableWeight <= 0) {
                  return false;
               } else {
                  int usedWeight = 0;
                  int tankIndex = 0;
                  IFluidHandler source = (IFluidHandler)lazySource.orElse(EmptyFluidHandler.INSTANCE);
                  IFluidHandler sink = (IFluidHandler)lazyReceiver.orElse(EmptyFluidHandler.INSTANCE);

                  while (availableWeight > usedWeight) {
                     FluidStack stack = source.drain(1000, FluidAction.SIMULATE);
                     if (stack.isEmpty()) {
                        if (++tankIndex >= source.getTanks()) {
                           break;
                        }
                     } else {
                        int weight = TeleportUtil.getWeightOfFluid(stack);
                        if (weight + usedWeight > availableWeight) {
                           break;
                        }

                        stack.setAmount(sink.fill(stack, FluidAction.EXECUTE));
                        if (stack.getAmount() > 0) {
                           usedWeight += TeleportUtil.getWeightOfFluid(stack);
                           source.drain(stack, FluidAction.EXECUTE);
                        }
                     }
                  }

                  this.consumeEnergy((int)((double)usedWeight * distanceCost));
                  return usedWeight > 0;
               }
            }
         }
      }
   }

   protected boolean handleItems(ServerLevel targetWorld) {
      IItemTransporter source = TransporterManager.getTransporter(DirectionList.getNeighborTile(this, this.getFacing()));
      if (source == null) {
         return false;
      } else {
         BlockEntity tile = targetWorld.getBlockEntity(this.target.getTargetPosition());
         if (!(tile instanceof ITeleporterTarget iTeleporterTarget)) {
            this.target = null;
            return false;
         } else if (!iTeleporterTarget.canReceive(ITeleporterTarget.TeleportType.ITEM)) {
            return false;
         } else if (this.isSamePos(tile, iTeleporterTarget.getFacing())) {
            return false;
         } else {
            IItemTransporter receiver = TransporterManager.getTransporter(DirectionList.getNeighborTile(tile, iTeleporterTarget.getFacing()));
            if (receiver == null) {
               return false;
            } else {
               double distanceCost = this.getBaseCost(targetWorld);
               int availableWeight = (int)((double)this.getAvailableEnergy() / distanceCost);
               if (availableWeight <= 0) {
                  return false;
               } else {
                  int usedWeight = 0;
                  Set<Item> itemFilter = CollectionUtils.<Item>createSet();
                  IFilter filter = new SetItemFilter(itemFilter, false);

                  while (availableWeight > usedWeight) {
                     ItemStack stack = source.removeItem(filter, this.getFacing().getOpposite(), 64, true);
                     if (stack.isEmpty()) {
                        break;
                     }

                     int weight = TeleportUtil.getWeightOfItem(stack);
                     if (weight + usedWeight > availableWeight) {
                        break;
                     }

                     int added = receiver.addItem(stack, iTeleporterTarget.getFacing().getOpposite(), false);
                     if (added <= 0) {
                        itemFilter.add(stack.getItem());
                     } else {
                        stack.setCount(added);
                        source.removeItem(StackFilter.defaultCompare(stack), this.getFacing().getOpposite(), 64, false);
                        usedWeight += TeleportUtil.getWeightOfItem(stack);
                     }
                  }

                  this.consumeEnergy((int)((double)usedWeight * distanceCost));
                  return usedWeight > 0;
               }
            }
         }
      }
   }

   protected boolean handleEntity(ServerLevel targetWorld) {
      Entity entity = AABBUtil.getClosestEntity(
         this.getLevel().getEntitiesOfClass(Entity.class, new AABB(this.getBlockPos()).inflate(2.0, 3.0, 2.0)),
         Vec3.atCenterOf(this.getBlockPos().relative(this.getFacing()))
      );
      if (entity == null) {
         return false;
      } else {
         BlockEntity tile = targetWorld.getBlockEntity(this.target.getTargetPosition());
         if (tile instanceof ITeleporterTarget type) {
            if (!type.canReceive(ITeleporterTarget.TeleportType.ENTITY)) {
               return false;
            } else if (this.isSamePos(tile, type.getFacing())) {
               return false;
            } else {
               boolean keepItems = IC2.CONFIG.teleporterKeepItems.get();
               int weight = TeleportUtil.getWeightOfEntity(entity, keepItems);
               if (weight == 0) {
                  return false;
               } else {
                  int energyCost = (int)((double)weight * this.getBaseCost(targetWorld) * 5.0);
                  if ((long)energyCost > this.getAvailableEnergy()) {
                     return false;
                  } else {
                     this.consumeEnergy(energyCost);
                     if (entity instanceof Player player) {
                        if (!keepItems) {
                           player.getInventory().dropAll();
                        }

                        double distance = Math.sqrt(this.getBlockPos().distSqr(this.target.getTargetPosition()));
                        player.awardStat(IC2Stats.DISTANCE_TELEPORTED, (int)distance * 1000);
                        if (distance >= 1000.0) {
                           IC2Advancements.ID_TRIGGER.onTrigger(player, new ResourceLocation("ic2:teleport_far"));
                        }
                     }

                     if (tile instanceof TeleporterTileEntity) {
                        ((TeleporterTileEntity)tile).delay = 20;
                     }

                     AABB axis = entity.getBoundingBox();
                     ((ServerLevel)this.getLevel())
                        .sendParticles(
                           new DustParticleOptions(new Vector3f(ColorUtils.rgbFloat((int)(this.getPosition().asLong() ^ this.getLevel().getGameTime()))), 1.0F),
                           axis.min(Axis.X) + axis.getXsize() * 0.5,
                           axis.min(Axis.Y) + axis.getYsize() * 0.5,
                           axis.min(Axis.Z) + axis.getZsize() * 0.5,
                           50,
                           axis.getXsize() * 0.5,
                           axis.getYsize() * 0.5,
                           axis.getZsize() * 0.5,
                           0.0
                        );
                     TeleportUtil.teleportEntity(entity, targetWorld, this.target.getTargetPosition(), type.getFacing());
                     axis = entity.getBoundingBox();
                     targetWorld.sendParticles(
                        new DustParticleOptions(new Vector3f(ColorUtils.rgbFloat((int)(this.getPosition().asLong() ^ this.getLevel().getGameTime()))), 1.0F),
                        axis.min(Axis.X) + axis.getXsize() * 0.5,
                        axis.min(Axis.Y) + axis.getYsize() * 0.5,
                        axis.min(Axis.Z) + axis.getZsize() * 0.5,
                        50,
                        axis.getXsize() * 0.5,
                        axis.getYsize() * 0.25,
                        axis.getZsize() * 0.5,
                        0.0
                     );
                     return true;
                  }
               }
            }
         } else {
            this.target = null;
            return false;
         }
      }
   }

   @Override
   public void onBlockUpdate(Block block, BlockPos from) {
      super.onBlockUpdate(block, from);
      this.blockChange = true;
   }

   protected void updateTargetTypes() {
      this.types.clear();
      BlockEntity tile = DirectionList.getNeighborTile(this, this.getFacing());
      if (tile == null) {
         this.types.add(ITeleporterTarget.TeleportType.ENTITY);
         this.types.add(ITeleporterTarget.TeleportType.SPAWNER);
      } else {
         if (tile instanceof Container || tile.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            this.types.add(ITeleporterTarget.TeleportType.ITEM);
         }

         if (tile.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
            this.types.add(ITeleporterTarget.TeleportType.FLUID);
         }

         if (tile instanceof IEnergyStorage) {
            this.types.add(ITeleporterTarget.TeleportType.ENERGY);
         }

         if (tile instanceof SpawnerBlockEntity) {
            this.types.add(ITeleporterTarget.TeleportType.NOTHING);
         }

         if (this.types.isEmpty()) {
            this.types.add(ITeleporterTarget.TeleportType.ENTITY);
         }
      }
   }

   public boolean hasMatchingType() {
      if (this.target != null && this.target.getWorld() != null) {
         BlockEntity tile = this.target.getTile();
         return !(tile instanceof ITeleporterTarget) ? false : ((ITeleporterTarget)tile).getSendType().matches(this.getSendType());
      } else {
         return false;
      }
   }

   public double getBaseCost(ServerLevel target) {
      return TeleportUtil.getDistanceCost(this.getLevel(), this.getBlockPos(), target, this.target.getTargetPosition());
   }

   public int getBaseCost() {
      if (this.target == null) {
         return 0;
      } else {
         ServerLevel server = this.target.getWorld();
         return server == null ? 0 : (int)TeleportUtil.getDistanceCost(this.getLevel(), this.getBlockPos(), server, this.target.getTargetPosition());
      }
   }

   public long getAvailableEnergy() {
      long stored = 0L;

      for (Direction dir : this.powerSources.getPresentSides().remove(this.getFacing())) {
         IEnergyStorage storage = this.powerSources.getHandler(dir);
         if (storage != null) {
            stored += (long)storage.getStoredEU();
         }
      }

      return stored;
   }

   public long getMaxEnergy() {
      long stored = 0L;

      for (Direction dir : this.powerSources.getPresentSides().remove(this.getFacing())) {
         IEnergyStorage storage = this.powerSources.getHandler(dir);
         if (storage != null) {
            stored += (long)storage.getMaxEU();
         }
      }

      return stored;
   }

   public void consumeEnergy(int energy) {
      PriorityQueue<IEnergyStorage> queue = CollectionUtils.createInsertionQueue();

      for (Direction dir : this.powerSources.getPresentSides().remove(this.getFacing())) {
         IEnergyStorage storage = this.powerSources.getHandler(dir);
         if (storage != null && storage.getStoredEU() > 0) {
            queue.enqueue(storage);
         }
      }

      while (energy > 0) {
         int drain = (energy + queue.size() - 1) / queue.size();
         int i = 0;

         for (int m = queue.size(); i < m && energy > 0; i++) {
            IEnergyStorage storage = (IEnergyStorage)queue.dequeue();
            drain = Math.min(drain, energy);
            energy -= storage.drawEnergy(drain);
            if (storage.getStoredEU() > 0) {
               queue.enqueue(storage);
            }
         }
      }
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.target = TeleporterTarget.read(compound.getCompound("target"));
   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      if (this.target != null) {
         compound.put("target", this.target.write(new CompoundTag()));
      }
   }

   @Override
   public ITeleporterTarget.TeleportType getSendType() {
      if (this.target == null) {
         return ITeleporterTarget.TeleportType.NOTHING;
      } else {
         BlockEntity tile = DirectionList.getNeighborTile(this, this.getFacing());
         if (tile != null) {
            if (tile instanceof IEnergyStorage) {
               return ITeleporterTarget.TeleportType.ENERGY;
            }

            if (tile.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
               return ITeleporterTarget.TeleportType.FLUID;
            }

            if (tile.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
               return ITeleporterTarget.TeleportType.ITEM;
            }

            if (tile instanceof SpawnerBlockEntity) {
               return ITeleporterTarget.TeleportType.SPAWNER;
            }
         }

         return ITeleporterTarget.TeleportType.ENTITY;
      }
   }

   public ITeleporterTarget.TeleportType getProbeSendType() {
      BlockEntity tile = DirectionList.getNeighborTile(this, this.getFacing());
      if (tile != null) {
         if (tile instanceof IEnergyStorage) {
            return ITeleporterTarget.TeleportType.ENERGY;
         }

         if (tile.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
            return ITeleporterTarget.TeleportType.FLUID;
         }

         if (tile.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            return ITeleporterTarget.TeleportType.ITEM;
         }

         if (tile instanceof SpawnerBlockEntity) {
            return ITeleporterTarget.TeleportType.SPAWNER;
         }
      }

      return ITeleporterTarget.TeleportType.ENTITY;
   }

   @Override
   public boolean canReceive(ITeleporterTarget.TeleportType type) {
      if (this.types.isEmpty()) {
         this.updateTargetTypes();
      }

      return this.types.contains(type);
   }

   @Override
   public boolean setTarget(TeleporterTarget target) {
      if (this.target != null && this.target.equals(target)) {
         return false;
      } else {
         this.target = new TeleporterTarget(target);
         return true;
      }
   }

   @Override
   public boolean hasTarget(TeleporterTarget target) {
      return this.target != null && this.target.equals(target);
   }
}
